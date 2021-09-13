package com.kaedea.mediastore.dualappcompat;

import android.os.Bundle;
import android.os.Process;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.kaedea.mediastore.dualappcompat.utils.KernelCpuSpeedReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import androidx.appcompat.app.AppCompatActivity;

public class ProcStatActivity extends AppCompatActivity {
    static List<Long> sInitialKernelCpuCoreJiffies;

    static {
        sInitialKernelCpuCoreJiffies = getKernelCpuJiffies();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proc_stat);
    }

    public void onGetPidTimeInState(View view) {
        for (int i = 0; i < 7; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {}
                }
            }).start();
        }

        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Long> procSateJiffies = readKernelPidJiffies(Process.myPid());
                    long procStateSum = 0;
                    List<Float> procStateRatio = new ArrayList<>();
                    for (Long item : procSateJiffies) {
                        procStateSum += item;
                    }
                    for (Long item : procSateJiffies) {
                        procStateRatio.add(((float) item) / procStateSum);
                    }

                    List<Long> cpuStateJiffiesEnd = getKernelCpuJiffies();
                    List<Long> cpuStateJiffies = new ArrayList<>();
                    for (int i = 0; i < cpuStateJiffiesEnd.size(); i++) {
                        cpuStateJiffies.add(cpuStateJiffiesEnd.get(i) - sInitialKernelCpuCoreJiffies.get(i));
                    }

                    long cpuStateSum = 0;
                    List<Float> cpuStateRatio = new ArrayList<>();
                    for (Long item : cpuStateJiffies) {
                        cpuStateSum += item;
                    }
                    for (Long item : cpuStateJiffies) {
                        cpuStateRatio.add(((float) item) / cpuStateSum);
                    }

                    Toast.makeText(view.getContext(), ""
                                    + "procStat: " + parseWithSplits(cat("/proc/" + Process.myPid() + "/stat")).getJiffies()
                                    + "\nprocState: " + procStateSum + procStateRatio.toString()
                                    + "\ncpuSate:" + cpuStateSum + cpuStateRatio,
                            Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                view.postDelayed(this, 2000L);
            }
        }, 2000L);
    }

    private static List<Long> readKernelPidJiffies(int pid) throws IOException {
        List<Long> cpuCoreJiffies = new ArrayList<>();
        String path = "/proc/" + pid + "/time_in_state";
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(path)))) {
            TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(' ');
            String line;
            int cluster = -1;
            long kernelPidJiffies = 0;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("cpu")) {
                    if (cluster >=0) {
                        cpuCoreJiffies.add(kernelPidJiffies);
                    }
                    cluster++;
                    kernelPidJiffies = 0;
                    continue;
                }
                splitter.setString(line);
                String speed = splitter.next();
                String time = splitter.next();
                kernelPidJiffies += Long.parseLong(time);
            }
            cpuCoreJiffies.add(kernelPidJiffies);
        }
        return cpuCoreJiffies;
    }

    public static String cat(String path) {
        if (TextUtils.isEmpty(path)) return null;
        try (RandomAccessFile restrictedFile = new RandomAccessFile(path, "r")) {
            return restrictedFile.readLine();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    static ProcStat parseWithSplits(String cat) {
        ProcStat stat = new ProcStat();
        if (!TextUtils.isEmpty(cat)) {
            int index = cat.indexOf(")");
            if (index <= 0) throw new IllegalStateException(cat + " has not ')'");
            String prefix = cat.substring(0, index);
            int indexBgn = prefix.indexOf("(") + "(".length();
            stat.comm = prefix.substring(indexBgn, index);

            String suffix = cat.substring(index + ")".length());
            String[] splits = suffix.split(" ");

            stat.stat = splits[1];
            stat.utime = Long.parseLong(splits[12]);
            stat.stime = Long.parseLong(splits[13]);
            stat.cutime = Long.parseLong(splits[14]);
            stat.cstime = Long.parseLong(splits[15]);
        }
        return stat;
    }

    public static class ProcStat {
        public String comm = "";
        public String stat = "_";
        public long utime = -1;
        public long stime = -1;
        public long cutime = -1;
        public long cstime = -1;

        public long getJiffies() {
            return utime + stime + cutime + cstime;
        }
    }

    public static int getCpuCoreNum() {
        try {
            // Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            // Filter to only list the devices we care about
            File[] files = dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return Pattern.matches("cpu[0-9]+", pathname.getName());
                }
            });
            // Return the number of cores (virtual CPU devices)
            return files.length;
        } catch (Exception ignored) {
            // Default to return 1 core
            return 1;
        }
    }

    private static List<Long> getKernelCpuJiffies() {
        List<Long> cpuCoreJiffies = new ArrayList<>();
        for (int i = 0; i < getCpuCoreNum(); i++) {
            cpuCoreJiffies.add(new KernelCpuSpeedReader(i).readTotoal());
        }
        return cpuCoreJiffies;
    }
}
