package com.naysayer.ourplaces;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MarkerInfoActivity extends AppCompatActivity
        implements OnMarkerClickFragmentDialog.OnDialogButtonsClickListener {

    private static final String TAG = "MARKER_INFO_ACTIVITY";
    private static final int REQUEST_TAKE_PHOTO = 1;

    private String mMarkerTitle;
    private String mMarkerDescription;
    private String mCurrentPhotoPath;

    private TextView titleInCard;
    private TextView descriptionInCard;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.take_a_photo_toolbar_menu:
                dispatchTakePictureIntent();
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
            mMarkerTitle = savedInstanceState.getString("Title");
            mMarkerDescription = savedInstanceState.getString("Description");
            titleInCard.setText(mMarkerTitle);
            descriptionInCard.setText(mMarkerDescription);

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
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("Title", mMarkerTitle);
        outState.putString("Description", mMarkerDescription);
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

        LayoutInflater layoutInflater = getLayoutInflater();
        @SuppressLint("InflateParams")
        View view = layoutInflater.inflate(R.layout.on_marker_click_dialog_fragment, null);
        TextInputEditText titleText = view.findViewById(R.id.marker_title_in_on_marker_click_dialog_fragment);
        titleText.setText(mMarkerTitle);
    }

    @Override
    public void onPositiveClick(String title, String description) {
        if (title.trim().isEmpty()) {
            mMarkerTitle = getResources().getString(R.string.title_in_marker_info);
        }
        if (description.trim().isEmpty()) {
            mMarkerDescription = getResources().getString(R.string.description_in_marker_info);
        }
        titleInCard.setText(mMarkerTitle);
        descriptionInCard.setText(mMarkerDescription);
    }

    @Override
    public void onNegativeClick(DialogFragment dialogFragment) {
        dialogFragment.dismiss();
    }


    // TODO: 19.01.2018 Save the full-size photo
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_TAKE_PHOTO) {
           setPic();
        }
    }

    /**
     * Returns a folder name based on the current date/time, something
     * like "20080725.013755".
     */
    public String getBackupFolderName() {
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd.HHmmss", Locale.getDefault());
        return sdf.format(date);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = getBackupFolderName();
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.naysayer.ourplaces.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private void setPic() {
        ImageView imageView = findViewById(R.id.marker_image_in_marker_info);
        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        imageView.setImageBitmap(bitmap);
    }
}
