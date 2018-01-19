package com.naysayer.ourplaces;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Objects;
//TODO: сделать иконку с деревом
//TODO: сделать кастомное InfoWindow и не отображать там snippet
// TODO: 19.01.2018 если перевернуть экран телефона во время диалогового окна, то вылетает, так как поля диалого окна null и если нажать add то вылетает

public class MapsActivity extends FragmentActivity
        implements OnMarkerClickFragmentDialog.OnDialogButtonsClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnInfoWindowLongClickListener,
        NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "Maps Activity";
    private static final String WAS_LAUNCHED = "Launched";
    private static final String NOT_LAUNCHED = "Not launched";
    private static final String NO_TITLE = "No title";
    private static final String NO_DESCRIPTION = "No description";

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private Marker mMarker;

    private ArrayList<LatLng> mMarkersLatLng = new ArrayList<>();       // Store markers LatLng
    private ArrayList<String> mMarkersTitles = new ArrayList<>();       // Store markers titles
    private ArrayList<String> mMarkersTags = new ArrayList<>();         // Store markers tags
    private ArrayList<String> mMarkersSnippets = new ArrayList<>(); // Store markers snippets

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate(Bundle) called");
        setContentView(R.layout.main_activity);

        // Get GoogleMap
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;

                moveCameraToCurrentLocation(15f);
                setUpMap();

                // Check whether we're recreating a previously destroyed instance
                if (savedInstanceState != null) {
                    // Restore value of members from saved state
                    mMarkersLatLng = savedInstanceState.getParcelableArrayList("Markers LatLng");
                    mMarkersTitles = savedInstanceState.getStringArrayList("Markers titles");
                    mMarkersTags = savedInstanceState.getStringArrayList("Markers tags");
                    mMarkersSnippets = savedInstanceState.getStringArrayList("Markers snippets");
                    LatLng cameraPosition = savedInstanceState.getParcelable("MyCamera position (Latlng)");

                    // Restore markers on map
                    if (mMarkersLatLng != null) {
                        restoreMarkers(mMarkersLatLng, mMarkersTitles, mMarkersTags, mMarkersSnippets);
                    }
                    // Restore camera position
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(cameraPosition));
                }
            }
        });

        // Add PlaceSelectionListener
        PlaceAutocompleteFragment mAutocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        // Change PlaceAutocompleteFragment icon
        assert mAutocompleteFragment.getView() != null;
        ImageView mPlaceAutocompleteIcon = (ImageView) ((LinearLayout) mAutocompleteFragment.getView())
                .getChildAt(0);
        mPlaceAutocompleteIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_place_autocomplete_button_24dp));

        // Set image click listener
        mPlaceAutocompleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.openDrawer(Gravity.START);
            }
        });

        // Set AutocompleteFilter
        AutocompleteFilter mTypeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .build();
        mAutocompleteFragment.setFilter(mTypeFilter);

        // Set listener
        mAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                LatLng mPlaceLatLng = place.getLatLng();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mPlaceLatLng, 15f));
                final Marker marker = mMap.addMarker(new MarkerOptions().position(mPlaceLatLng));
                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        marker.remove();
                    }
                });
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        // Get Location FAB
        FloatingActionButton mGetMyLocationButton = findViewById(R.id.get_location_fab);
        mGetMyLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get current location (ltd, lng) and animate camera with current zoom
                moveCameraToCurrentLocation(mMap.getCameraPosition().zoom);
            }
        });

        // Navigation drawer
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        drawer.closeDrawer(Gravity.START);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    // Set map listeners and settings
    private void setUpMap() {

        // Customize map
        if (ActivityCompat.checkSelfPermission(MapsActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MapsActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        // Set UI
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setBuildingsEnabled(true);

        /*
         * Set listeners on map:
         * OnInfoWindowLongClickListener, OnInfoWindowClickListener,
         * OnMapLongClickListener,
         * OnMarkerClickListener
         */

        // Set marker on map
        mMap.setOnMapLongClickListener(this);
        // Allow to delete marker from map
        mMap.setOnInfoWindowLongClickListener(this);
        // Allow to see or change marker params
        mMap.setOnInfoWindowClickListener(this);
        // Start dialog to set marker params (title & snippet)
        mMap.setOnMarkerClickListener(this);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        //Add marker
        addMarkerOnMap(latLng);

        //Add marker LatLng, title, tag at arraylists
        mMarkersLatLng.add(latLng);
        mMarkersTitles.add(NO_TITLE);
        mMarkersTags.add(NOT_LAUNCHED);
        mMarkersSnippets.add(NO_DESCRIPTION);

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        mMarker = marker;

        if (!Objects.equals(marker.getTag(), WAS_LAUNCHED)) {
            showOnMarkerClickDialog();
        } else if (!mMarker.getTitle().equals(NO_TITLE)) {
            marker.showInfoWindow();
        }
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        mMarker = marker;

        Intent markerInfo = new Intent(this, MarkerInfoActivity.class);
        markerInfo.putExtra("title_from_maps_activity", marker.getTitle());
        markerInfo.putExtra("description_from_maps_activity", marker.getSnippet());

        // Run MarkerInfoActivity
        startActivityForResult(markerInfo, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) return;

        String titleFromMarkerInfoActivity = data.getStringExtra("Marker title from card");
        String descriptionFromMarkerInfoActivity = data.getStringExtra("Marker description from card");
        mMarker.setTitle(titleFromMarkerInfoActivity);
        mMarker.setSnippet(descriptionFromMarkerInfoActivity);

        // Get marker latlng, when get array index of latlng
        int index = mMarkersLatLng.indexOf(mMarker.getPosition());

        // Set changed title and snippet to array
        mMarkersTitles.set(index, titleFromMarkerInfoActivity);
        mMarkersSnippets.set(index, descriptionFromMarkerInfoActivity);

        // Show changes
        mMarker.showInfoWindow();
    }

    @Override
    public void onInfoWindowLongClick(Marker marker) {
        // Show dialog
        deleteMarkerDialog(marker);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart(Bundle) called");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume(Bundle) called");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause(Bundle) called");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop(Bundle) called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy(Bundle) called");
    }

    // Save marker position, title and snippet
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState(Bundle) called");

        // Adding the array of Latlng, titles, tags to Bundle
        outState.putParcelableArrayList("Markers LatLng", mMarkersLatLng);
        outState.putStringArrayList("Markers titles", mMarkersTitles);
        outState.putStringArrayList("Markers tags", mMarkersTags);
        outState.putStringArrayList("Markers snippets", mMarkersSnippets);
        outState.putParcelable("MyCamera position (Latlng)", mMap.getCameraPosition().target);

        super.onSaveInstanceState(outState);
    }

    // Add custom marker on map
    private void addMarkerOnMap(LatLng latLng) {

        // Set marker params
        mMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(bitmapDescriptorFromVector(this, R.drawable.ic_personal_marker)));
        mMarker.setTag(NOT_LAUNCHED);

        // Snackbar allows you to immediately remove the marker, if a user accidentally added it
        if (mapFragment.getView() != null) {
            //Create object of CustomSnackbar class
            CustomSnackbar snackbar = new CustomSnackbar(mapFragment.getView(),
                    R.string.add_marker_snackbar,
                    this);
            snackbar.setAction(R.string.snackbar_action, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mMarker.remove();
                    mMarkersLatLng.remove(mMarkersLatLng.size() - 1);
                }
            }).setSnackbarColor(R.color.colorPrimaryDark)
                    .setTextColor(Color.WHITE)
                    .setSnackbarActionButtonColor(R.color.secondaryColor);

            if (snackbar.isDeviceScreenIsLarge()) {
                snackbar.setTextSize(getResources().getDimension(R.dimen.snackbar_font_size))
                        .setMinimumWidth(getResources().getDimensionPixelSize(R.dimen.minimum_snackbar_wight));
            }

            snackbar.show();
        }
    }

    // Restore markers on map
    private void restoreMarkers(ArrayList<LatLng> markersLatLng, ArrayList<String> markersTitles,
                                ArrayList<String> markersTags, ArrayList<String> markersSnippets) {
        Log.d(TAG, "restoreMarkers( )");
        if (markersLatLng != null & markersTitles != null) {
            for (int i = 0; i < markersLatLng.size(); i++) {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(markersLatLng.get(i))
                        .draggable(true)
                        .icon(bitmapDescriptorFromVector(this, R.drawable.ic_personal_marker))
                        .title(markersTitles.get(i))
                        .snippet(markersSnippets.get(i));
                mMap.addMarker(markerOptions)
                        .setTag(markersTags.get(i));
            }
        }

    }

    // Get bitmap from vector img
    @NonNull
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        assert vectorDrawable != null;
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    // Make dialog to delete marker from map
    private void deleteMarkerDialog(final Marker marker) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_delete_marker)
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        marker.remove();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // The function that moves the camera to the current coordinates with a certain zoom
    private void moveCameraToCurrentLocation(float zoom) {

        CurrentLocation currentLocation = new CurrentLocation();
        //Move camera to current location
        mMap.animateCamera(CameraUpdateFactory
                .newLatLngZoom(currentLocation.getCurrentLocation(this,
                        MapsActivity.this), zoom));
    }

    // Show the dialog
    private void showOnMarkerClickDialog() {
        DialogFragment dialogFragment = OnMarkerClickFragmentDialog.newInstance();
        dialogFragment.show(getFragmentManager(), "OnMarkerClickFragmentDialog");
    }

    // Positive OnMarkerClickDialogFragment button
    // Method sends a title and a description(snippet) to the MarkerInfoActivity
    @Override
    public void onPositiveClick(String title, String description) {
        // Set marker title
        mMarker.setTitle(title);
        mMarker.setSnippet(description);

        // Set tag, which shows whether the activity was started
        mMarker.setTag(WAS_LAUNCHED);

        // Get marker latlng, when get array index of latlng
        int index = mMarkersLatLng.indexOf(mMarker.getPosition());

        // Title
        if (!mMarker.getTitle().trim().isEmpty()) {
            mMarkersTitles.set(index, mMarker.getTitle());
        }

        // Snippet
        if (!mMarker.getSnippet().trim().isEmpty()) {
            mMarkersSnippets.set(index, mMarker.getSnippet());
        }

        // Transfer data to MarkerInfoActivity
        Intent markerInfo = new Intent(MapsActivity.this, MarkerInfoActivity.class);

        // Put strings
        markerInfo.putExtra("title_from_maps_activity", title);
        markerInfo.putExtra("description_from_maps_activity", description);

        // Run MarkerInfoActivity
        startActivityForResult(markerInfo, 1);
    }

    // Negative OnMarkerClickDialogFragment button
    // Dismiss dialog
    @Override
    public void onNegativeClick(DialogFragment dialogFragment) {
        dialogFragment.dismiss();
    }

    /**
     * Navigation Drawer
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_favourites_markers) {
            Toast.makeText(this, "Favourites markers", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_gallery) {
            Toast.makeText(this, "Gallery", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_manage) {
            Toast.makeText(this, "Tools", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_rate) {
            Toast.makeText(this, "Rate", Toast.LENGTH_SHORT).show();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}

