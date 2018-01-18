package com.naysayer.ourplaces;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.view.LayoutInflater;
import android.view.View;

public class OnMarkerClickFragmentDialog extends DialogFragment {

    public interface OnDialogButtonsClickListener {
        void onPositiveClick(String title, String description);

        void onNegativeClick(DialogFragment dialogFragment);
    }

    TextInputEditText mMarkerTitle;         // Marker title
    TextInputEditText mMarkerDescription;   // Marker description

    OnDialogButtonsClickListener mListener; // Interface object

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            this.mListener = (OnDialogButtonsClickListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement OnDialogButtonsClickListener");
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get layout inflater
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        // Get view which set on dialog
        @SuppressLint("InflateParams") View dialogView = layoutInflater.inflate(R.layout.on_marker_click_dialog_fragment, null);

        // Get links on view objects
        mMarkerTitle = dialogView.findViewById(R.id.marker_title_in_on_marker_click_dialog_fragment);
        mMarkerDescription = dialogView.findViewById(R.id.marker_description_in_on_marker_click_dialog_fragment);

        // Set view, positive & negative buttons
        builder.setView(dialogView)
                .setPositiveButton(R.string.add_button_in_on_marker_click_dialog_fragment, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String markerTitle = mMarkerTitle.getText().toString();
                        String markerDescription = mMarkerDescription.getText().toString();
                        mListener.onPositiveClick(markerTitle, markerDescription);
                    }
                })
                .setNegativeButton(R.string.cancel_button_in_on_marker_click_dialog_fragment, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mListener.onNegativeClick(OnMarkerClickFragmentDialog.this);
                    }
                });

        return builder.create();
    }

    static OnMarkerClickFragmentDialog newInstance() {
        return new OnMarkerClickFragmentDialog();
    }
}
