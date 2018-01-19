package com.naysayer.ourplaces;

import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MarkerInfoActivity extends AppCompatActivity
        implements OnMarkerClickFragmentDialog.OnDialogButtonsClickListener {

    @BindView(R.id.toolbar_in_marker_info_activity)
    Toolbar mToolbar;
    @BindView(R.id.marker_title_in_marker_info)
    TextView mTitleInCard;
    @BindView(R.id.marker_description_in_marker_info)
    TextView mDescriptionInCard;
    @BindView(R.id.edit_button_marker_info)
    Button mEditButton;
    @BindView(R.id.add_to_fav_button_marker_info)
    Button mAddToFavouritesButton;
    @BindString(R.string.title_in_marker_info)
    String mDefaultTitle;
    @BindString(R.string.description_in_marker_info)
    String mDefaultDescription;
    @BindView(R.id.marker_image_in_marker_info)
    ImageView mImageViewInCard;

    private static final String TAG = "MARKER_INFO_ACTIVITY";   // Log TAG
    private static final int REQUEST_TAKE_PHOTO = 1;            // Request code for camera result

    private String mMarkerTitle;
    private String mMarkerDescription;
    private String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate(Bundle) called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.marker_info_activity);

        ButterKnife.bind(this);

        // Set toolbar
        setSupportActionBar(mToolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTitleAndDescription();
            }
        });

        mAddToFavouritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MarkerInfoActivity.this, mCurrentPhotoPath, Toast.LENGTH_SHORT).show();
            }
        });

        Intent intent = getIntent();

        if (intent != null) {
            if (intent.getStringExtra("title_from_maps_activity").trim().isEmpty()) {
                mTitleInCard.setText(mDefaultTitle);
                mMarkerTitle = mTitleInCard.getText().toString();
            } else {
                mMarkerTitle = intent.getStringExtra("title_from_maps_activity");
                mTitleInCard.setText(mMarkerTitle);
            }
            if (intent.getStringExtra("description_from_maps_activity").trim().isEmpty()) {
                mDescriptionInCard.setText(mDefaultDescription);
                mMarkerDescription = mDescriptionInCard.getText().toString();
            } else {
                mMarkerDescription = intent.getStringExtra("description_from_maps_activity");
                mDescriptionInCard.setText(mMarkerDescription);
            }
        }
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
        if (title.trim().isEmpty()) {
            mMarkerTitle = mDefaultTitle;
        }
        if (description.trim().isEmpty()) {
            mMarkerDescription = mDefaultDescription;
        }
        mTitleInCard.setText(mMarkerTitle);
        mDescriptionInCard.setText(mMarkerDescription);
    }

    @Override
    public void onNegativeClick(DialogFragment dialogFragment) {
        dialogFragment.dismiss();
    }


    /**
     * Processing camera image
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
                imageFileName,    //prefix
                ".jpg",     //suffix
                storageDir        //directory
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
        // Get the dimensions of the View
        int targetW = mImageViewInCard.getWidth();
        int targetH = mImageViewInCard.getHeight();

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

        mImageViewInCard.setImageBitmap(BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions));
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_TAKE_PHOTO) {
            setPic();
            galleryAddPic();
        }
    }

}
