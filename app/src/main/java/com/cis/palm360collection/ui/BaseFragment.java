package com.cis.palm360collection.ui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.transition.Slide;
import android.transition.Visibility;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.cis.palm360collection.R;


//Common Base Fragment
public abstract class BaseFragment extends Fragment {

    private ActionBar actionBar;
    private View rootView;
    private Toolbar toolbar;
    public android.widget.RelativeLayout baseLayout;

    public BaseFragment() {

    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_base, container, false);
        initView();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        actionBar = activity.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        Initialize();

//        buildEnterTransition();
        return rootView;
    }

    public abstract void Initialize();

    @SuppressLint("RestrictedApi")
    public void setTile(final String title) {
        actionBar.setTitle(title);
        actionBar.invalidateOptionsMenu();
    }

    private void initView() {
        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        baseLayout = (RelativeLayout) rootView.findViewById(R.id.baseLayout);

        baseLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private Visibility buildEnterTransition() {
        Slide enterTransition = new Slide();
        enterTransition.setDuration(500);
        enterTransition.setSlideEdge(Gravity.RIGHT);
        return enterTransition;
    }
}
