package com.candy1126xx.superrecorder.view;

import android.app.Dialog;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Administrator on 2017/11/1 0001.
 */

public class BaseActivity extends AppCompatActivity {

    private Dialog dialog;

    public void showLoadingDialog(String content) {
        dialog = new AlertDialog.Builder(this).setMessage(content).create();
        dialog.show();
    }

    public void hideLoadingDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

}
