package com.naysayer.ourplaces;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class MarkerInfoActivity extends AppCompatActivity
        implements OnMarkerClickFragmentDialog.OnDialogButtonsClickListener {

    public static final String TAG = "MARKER_INFO_ACTIVITY";

    String mMarkerTitle;
    String mMarkerDescription;

    protected TextView titleInCard;
    protected TextView descriptionInCard;

    ArrayList<String> mTitleAndDescription = new ArrayList<>(2);

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.take_a_photo_toolbar_menu:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate(Bundle) called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.marker_info_activity);

        // Set toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_in_marker_info_activity);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // TODO: 15.01.2018 Возврат к главной активности. Маркеры поставленые до, не отображаются
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Get links
        titleInCard = findViewById(R.id.marker_title_in_marker_info);
        descriptionInCard = findViewById(R.id.marker_description_in_marker_info);
        Button editButton = findViewById(R.id.edit_button_marker_info);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTitleAndDescription();
            }
        });
        //Button addToFavouritesButton = findViewById(R.id.add_to_fav_button_marker_info);

        Intent intent = getIntent();

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("title & description")) {
                mTitleAndDescription = savedInstanceState.getStringArrayList("title & description");
                assert mTitleAndDescription != null;
                titleInCard.setText(mTitleAndDescription.get(1));
                descriptionInCard.setText(mTitleAndDescription.get(0));
            }
        } else if (intent != null) {
            if (intent.getStringExtra("title_from_maps_activity").trim().isEmpty()) {
                titleInCard.setText(R.string.title_in_marker_info);
                mMarkerTitle = titleInCard.getText().toString();
            } else {
                mMarkerTitle = intent.getStringExtra("title_from_maps_activity");
                titleInCard.setText(mMarkerTitle);
            }
            if (intent.getStringExtra("description_from_maps_activity").trim().isEmpty()) {
                descriptionInCard.setText(R.string.description_in_marker_info);
                mMarkerDescription = descriptionInCard.getText().toString();
            } else {
                mMarkerDescription = intent.getStringExtra("description_from_maps_activity");
                descriptionInCard.setText(mMarkerDescription);
            }
        }

        mTitleAndDescription.add(mMarkerDescription);
        mTitleAndDescription.add(mMarkerTitle);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putStringArrayList("title & description", mTitleAndDescription);
    }

    // Передавать title и snippet обратно в MapsActivity
    @Override
    public void onBackPressed() {
        Intent mapsActivity = new Intent();
        mapsActivity.putExtra("Marker title from card", mMarkerTitle);
        mapsActivity.putExtra("Marker description from card", mMarkerDescription);
        setResult(RESULT_OK, mapsActivity);
        super.onBackPressed();
    }

    /**
     * The dialog that starts when the button "edit" is pressed
     */
    private void editTitleAndDescription() {
        // TODO: 18.01.2018 установить текущие значения заголовка и описания в диалог
        DialogFragment dialogFragment = OnMarkerClickFragmentDialog.newInstance();
        dialogFragment.show(getFragmentManager(), "OnMarkerClickFragmentDialog");
    }

    @Override
    public void onPositiveClick(String title, String description) {
        mMarkerTitle = title;
        mMarkerDescription = description;
        titleInCard.setText(mMarkerTitle);
        descriptionInCard.setText(mMarkerDescription);
    }

    @Override
    public void onNegativeClick(DialogFragment dialogFragment) {
        dialogFragment.dismiss();
    }
}
