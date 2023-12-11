package com.kaedea.mediastore.dualappcompat.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kaede
 * @since 11/12/2023
 */
public class FileProviderOps {
    public static List<ProviderInfo> getAllFileProviders(Context context) {
        List<ProviderInfo> fileProviderList = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(PackageManager.GET_PROVIDERS | PackageManager.GET_META_DATA);

        for (PackageInfo packageInfo : packageInfoList) {
            if (packageInfo.providers != null) {
                for (ProviderInfo providerInfo : packageInfo.providers) {
                    if (String.valueOf(providerInfo.authority).contains("fileprovider")) {
                        fileProviderList.add(providerInfo);
                    }
                }
            }
        }
        return fileProviderList;
    }

    public static List<ProviderInfo> getAllFileProvidersR2(Context context) {
        List<ProviderInfo> fileProviderList = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();
        List<ProviderInfo> resolveInfoList = packageManager.queryContentProviders(null, 0, PackageManager.GET_PROVIDERS | PackageManager.GET_META_DATA);

        for (ProviderInfo providerInfo : resolveInfoList) {
            if (String.valueOf(providerInfo.authority).contains("fileprovider")) {
                fileProviderList.add(providerInfo);
            }
        }

        return fileProviderList;
    }

    public static List<ProviderInfo> getProvider(Context context, String authority) {
        List<ProviderInfo> fileProviderList = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();
        List<ProviderInfo> resolveInfoList = packageManager.queryContentProviders(null, 0, PackageManager.GET_PROVIDERS | PackageManager.GET_META_DATA);
        for (ProviderInfo providerInfo : resolveInfoList) {
            if (String.valueOf(providerInfo.authority).contains(authority)) {
                fileProviderList.add(providerInfo);
            }
        }
        return fileProviderList;
    }

    public static List<Uri> getFilesFromProvider(Context context, ProviderInfo providerInfo) {
        List<Uri> fileList = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();

        try {
            Uri contentUri = Uri.parse("content://" + providerInfo.authority);
            Cursor cursor = contentResolver.query(contentUri, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                do {
                    String filePath = cursor.getString(columnIndex);
                    Uri fileUri = Uri.parse(contentUri + "/" + filePath);
                    fileList.add(fileUri);
                } while (cursor.moveToNext());

                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fileList;
    }

    public static List<Uri> getFilesFromProvider(Context context, Uri contentUri) {
        List<Uri> fileList = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        try {
            Cursor cursor = contentResolver.query(contentUri, new String[]{MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DISPLAY_NAME, /*MediaStore.MediaColumns.RELATIVE_PATH,*/ MediaStore.MediaColumns.DATA}
                    , null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                do {
                    String filePath = cursor.getString(columnIndex);
                    Uri fileUri = Uri.parse(contentUri + "/" + filePath);
                    fileList.add(fileUri);
                } while (cursor.moveToNext());

                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fileList;
    }
}
