/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package com.kaedea.mediastore.dualappcompat.home;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kaedea.mediastore.dualappcompat.R;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by kaede on 2015/10/23.
 */
public class DemoListFragment extends Fragment {
    private static final String BUNDLE_INDEX = "BUNDLE_INDEX";

    private int index;
    private ActivityHolder activityHolder;

    public static DemoListFragment newInstance(int index) {
        DemoListFragment fragment = new DemoListFragment();
        Bundle args = new Bundle();
        args.putInt(BUNDLE_INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            index = getArguments().getInt(BUNDLE_INDEX);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RecyclerView recyclerView = new RecyclerView(getActivity());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        MyAdapter adapter = new MyAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        activityHolder = DemoProvider.demos.valueAt(index);
        adapter.notifyDataSetChanged();
        return recyclerView;
    }

    class MyAdapter extends RecyclerView.Adapter<ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = getItemViewLayout(getActivity());
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.bind(position);
        }

        @Override
        public int getItemCount() {
            return activityHolder == null ? 0 : activityHolder.getCount();
        }
    }

    @SuppressWarnings("ResourceType")
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public int position;
        public TextView tvTitle;
        public TextView tvSubTitle;

        public ViewHolder(View itemView) {
            super(itemView);
            tvTitle = (TextView) itemView.findViewById(ID_TITLE);
            tvSubTitle = (TextView) itemView.findViewById(ID_SUBTITLE);
        }

        public void bind(int position) {
            this.position = position;
            tvTitle.setText(activityHolder.getActivityName(position) == null ? "" : activityHolder.getActivityName(position));
            tvSubTitle.setText(activityHolder.getActivityDesc(position) == null ? "" : activityHolder.getActivityDesc(position));
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            activityHolder.startActivity(getActivity(), position);
        }
    }

    public static final int ID_TITLE = 100;
    public static final int ID_SUBTITLE = 200;

    ////////////////
    //  Title     //
    //  Sub Title //
    ////////////////
    @SuppressWarnings("ResourceType")
    public static View getItemViewLayout(Context context) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER_VERTICAL);
        int[] attrs = new int[]{R.attr.selectableItemBackground};
        TypedArray typedArray = context.obtainStyledAttributes(attrs);
        int backgroundResource = typedArray.getResourceId(0, 0);
        linearLayout.setBackgroundResource(backgroundResource);
        typedArray.recycle();
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayout.setLayoutParams(layoutParams);
        // Title
        TextView tvTitle = new TextView(context);
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f);
        tvTitle.setMaxLines(1);
        tvTitle.setTextColor(Color.parseColor("#212121"));
        tvTitle.setId(ID_TITLE);
        layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(InternalUtils.dpToPx(context, 20f), InternalUtils.dpToPx(context, 10f),
                InternalUtils.dpToPx(context, 20f), 0);
        linearLayout.addView(tvTitle, layoutParams);
        // Sub Title
        TextView tvSubTitle = new TextView(context);
        tvSubTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f);
        tvSubTitle.setMaxLines(2);
        tvSubTitle.setTextColor(Color.parseColor("#757575"));
        tvSubTitle.setId(ID_SUBTITLE);
        layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(InternalUtils.dpToPx(context, 20f), 0,
                InternalUtils.dpToPx(context, 20f), InternalUtils.dpToPx(context, 10f));
        linearLayout.addView(tvSubTitle, layoutParams);
        return linearLayout;
    }
}
