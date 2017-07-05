package com.candy1126xx.superrecorder.view;

import android.content.Context;
import android.support.v4.app.Fragment;

/**
 * Created by Administrator on 2017/7/5 0005.
 */

public class BaseFragment extends Fragment {

    protected SuperRecorderActivity mParent;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mParent = (SuperRecorderActivity) context;
    }
}
