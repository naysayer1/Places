package com.naysayer.ourplaces;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

class CustomSnackbar {
    private static final int DURATION = Snackbar.LENGTH_LONG;
    private Snackbar mSnackbar;
    private Context mContext;

    CustomSnackbar(View view, int resid, Context context) {
        mSnackbar = Snackbar.make(view, resid, DURATION);
        mContext = context;
    }

    private View getSnackbarView() {
        return mSnackbar.getView();
    }

    private TextView getSnackbarTextView() {
        return mSnackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
    }

    private TextView getActionButtonTextView() {
        return mSnackbar.getView().findViewById(android.support.design.R.id.snackbar_action);
    }

    private int getDeviceScreenHeightInDP() {
        return mContext.getResources().getConfiguration().screenHeightDp;
    }

    private int getDeviceScreenWightInDP() {
        return mContext.getResources().getConfiguration().screenWidthDp;
    }

    final CustomSnackbar setAction(int resid, View.OnClickListener onClickListener) {
        mSnackbar.setAction(resid, onClickListener);
        return this;
    }

    final CustomSnackbar setSnackbarColor(int resid) {
        getSnackbarView().setBackgroundResource(resid);
        return this;
    }

    final CustomSnackbar setSnackbarActionButtonColor(int resid) {
        getActionButtonTextView().setTextColor(mContext.getResources().getColor(resid));
        return this;
    }

    final CustomSnackbar setTextColor(int resid) {
        getSnackbarTextView().setTextColor(resid);
        return this;
    }

    //in material.io usual text and action button text have size 14sp
    final CustomSnackbar setTextSize(float size) {
        getSnackbarTextView().setTextSize(size);
        getActionButtonTextView().setTextSize(size);
        return this;
    }

    final CustomSnackbar setMinimumWidth(int resid) {
        getSnackbarView().setMinimumWidth(resid);
        return this;
    }

    void show() {
        mSnackbar.show();
    }

    boolean isDeviceScreenIsLarge() {
        return getDeviceScreenHeightInDP() > mContext.getResources().getDimension(R.dimen.normal_screen_height)
                & getDeviceScreenWightInDP() > mContext.getResources().getDimension(R.dimen.normal_screen_wight);
    }
}
