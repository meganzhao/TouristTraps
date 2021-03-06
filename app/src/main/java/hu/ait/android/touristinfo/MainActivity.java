package hu.ait.android.touristinfo;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import hu.ait.android.touristinfo.data.Sights;
import hu.ait.android.touristinfo.data.businesses.Business;
import hu.ait.android.touristinfo.data.businesses.BusinessesResult;
import hu.ait.android.touristinfo.network.BusinessesAPI;
import io.realm.Realm;
import io.realm.RealmResults;
import mehdi.sakout.fancybuttons.FancyButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity
        implements TouristLocationManager.NewLocationListener, OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    public static final String RETROFIT_CALLON_FAILURE = "retrofitCallonFailure";
    private TouristLocationManager touristLocationManager;
    private GoogleMap mMap;
    private DrawerLayout drawerLayout;
    private List<Business> highRatingBusinessList;
    public ArrayList<Sights> addedToAgendaList;
    private Retrofit retrofit;
    List<Sights> sightsResult;
    private double latitude;
    private double longitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((MainApplication) getApplication()).openRealm();

        touristLocationManager = new TouristLocationManager(this,this);
        addedToAgendaList = new ArrayList<>();
        highRatingBusinessList = new ArrayList<>();

        requestNeededPermission();

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.mainPageMap);
        mapFragment.getMapAsync(this);

        retrofit = new Retrofit.Builder()
                .baseUrl("https://api.yelp.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        FancyButton btnEnter = findViewById(R.id.btnEnter);

        btnEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String cityName = ((EditText) findViewById(R.id.etSearch)).getText().toString();
                businessesCallWithCityNameFood(retrofit, cityName);
                businessesCallWithCityNameMuseum(retrofit, cityName);
                businessesCallWithCityNameBar(retrofit, cityName);
                businessesCallWithCityNameCafe(retrofit, cityName);


            }


        });
        setUpDrawer();
        setUpToolBar();
    }

    private void businessesCallWithCityNameFood(Retrofit retrofit, String cityName) {
        BusinessesAPI businessesAPI = retrofit.create(BusinessesAPI.class);
        final List<Business> highRatingFood = new ArrayList<>();
        Call<BusinessesResult> call = businessesAPI.getBusinessesResult(getString(R.string.food), cityName);
        call.enqueue(new Callback<BusinessesResult>() {

            @Override
            public void onResponse(Call<BusinessesResult> call, Response<BusinessesResult> response) {
                List<Business> businessList = response.body().getBusinesses();
                for (Business business: businessList){
                    if (business.getRating() >= 4.0 && highRatingFood.size() <= 10) {
                        highRatingBusinessList.add(business);
                        highRatingFood.add(business);
                    }
                }
                placeMarkers(highRatingFood,getString(R.string.food));
            }

            @Override
            public void onFailure(Call<BusinessesResult> call, Throwable t) {
                Log.e(RETROFIT_CALLON_FAILURE,Log.getStackTraceString(t));
            }
        });
    }

    private void businessesCallWithCityNameCafe(Retrofit retrofit, String cityName) {
        BusinessesAPI businessesAPI = retrofit.create(BusinessesAPI.class);
        final List<Business> highRatingCafe = new ArrayList<>();
        Call<BusinessesResult> call = businessesAPI.getBusinessesResult("cafe", cityName);
        call.enqueue(new Callback<BusinessesResult>() {

            @Override
            public void onResponse(Call<BusinessesResult> call, Response<BusinessesResult> response) {
                List<Business> businessList = response.body().getBusinesses();
                for (Business business: businessList){
                    if (business.getRating() >= 4.0 && highRatingCafe.size() <= 10) {
                        highRatingBusinessList.add(business);
                        highRatingCafe.add(business);
                    }
                }
                placeMarkers(highRatingCafe,getString(R.string.cafe));
            }

            @Override
            public void onFailure(Call<BusinessesResult> call, Throwable t) {
                Log.e(RETROFIT_CALLON_FAILURE,Log.getStackTraceString(t));
            }
        });
    }


    private void businessesCallWithCityNameMuseum(Retrofit retrofit, String cityName) {
        BusinessesAPI businessesAPI = retrofit.create(BusinessesAPI.class);
        final List<Business> highRatingMuseum = new ArrayList<>();
        Call<BusinessesResult> call = businessesAPI.getBusinessesResult(getString(R.string.museum), cityName);
        call.enqueue(new Callback<BusinessesResult>() {

            @Override
            public void onResponse(Call<BusinessesResult> call, Response<BusinessesResult> response) {
                List<Business> businessList = response.body().getBusinesses();
                for (Business business: businessList){
                    if (business.getRating() >= 4.0 && highRatingMuseum.size() <= 10) {
                        highRatingBusinessList.add(business);
                        highRatingMuseum.add(business);
                    }
                }
                placeMarkers(highRatingMuseum, getString(R.string.museum));
            }

            @Override
            public void onFailure(Call<BusinessesResult> call, Throwable t) {
                Log.e(RETROFIT_CALLON_FAILURE,Log.getStackTraceString(t));
            }
        });
    }

    private void businessesCallWithCityNameBar(Retrofit retrofit, String cityName) {
        BusinessesAPI businessesAPI = retrofit.create(BusinessesAPI.class);
        final List<Business> highRatingBar = new ArrayList<>();
        Call<BusinessesResult> call = businessesAPI.getBusinessesResult(getString(R.string.bar), cityName);
        call.enqueue(new Callback<BusinessesResult>() {

            @Override
            public void onResponse(Call<BusinessesResult> call, Response<BusinessesResult> response) {
                List<Business> businessList = response.body().getBusinesses();
                for (Business business: businessList){
                    if (business.getRating() >= 4.0 && highRatingBar.size() <= 10) {
                        highRatingBusinessList.add(business);
                        highRatingBar.add(business);
                    }
                }
                placeMarkers(highRatingBar, getString(R.string.bar));
            }

            @Override
            public void onFailure(Call<BusinessesResult> call, Throwable t) {
                Log.e(RETROFIT_CALLON_FAILURE,Log.getStackTraceString(t));
            }
        });
    }


    public void placeMarkers(List<Business> highRatingBusinessList, String category) {
        Bitmap markerImage = BitmapFactory.decodeResource(getResources(),R.drawable.food);
        if (category == getString(R.string.museum))
        markerImage = BitmapFactory.decodeResource(getResources(),R.drawable.museum);
        if (category == getString(R.string.bar))
            markerImage = BitmapFactory.decodeResource(getResources(),R.drawable.bar);
        if (category == getString(R.string.cafe))
            markerImage = BitmapFactory.decodeResource(getResources(),R.drawable.cafe);

        Bitmap smallMarker = Bitmap.createScaledBitmap(markerImage,85, 85, false);
        if (category == getString(R.string.food)) {
            Business markerBusiness = highRatingBusinessList.get(0);
            latitude = markerBusiness.getCoordinates().getLatitude();
            longitude = markerBusiness.getCoordinates().getLongitude();
            LatLng markerBusinessL = new LatLng(latitude, longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerBusinessL, 12.0f));
        }

        setUpMarkerAtLocation(highRatingBusinessList, smallMarker);
    }

    private void setUpMarkerAtLocation(List<Business> highRatingBusinessList, Bitmap smallMarker) {
        for (Business business: highRatingBusinessList){
            latitude = business.getCoordinates().getLatitude();
            longitude = business.getCoordinates().getLongitude();
            LatLng currentBus = new LatLng(latitude, longitude);
            Marker marker = mMap.addMarker(new MarkerOptions().
                    position(currentBus).
                    title(business.getName()).
                    icon(BitmapDescriptorFactory.fromBitmap(smallMarker)).
                    snippet(business.getRating().toString() + " " + getString(R.string.stars) + " " + getString(R.string.by) + " " +
                            business.getReviewCount().intValue() + " " +
                        getString(R.string.people) +  "    " + business.getDistance().intValue() + " " + getString(R.string.kmaway))
            );
            marker.setDraggable(true);
            marker.setTag(business);
        }
    }

    @Override
    public void onNewLocation(Location location) {
        LatLng currentLocation = new LatLng(location.getLatitude(),location.getLongitude());
        Marker marker =
                mMap.addMarker(new MarkerOptions()
                        .position(currentLocation)
                        .title(getString(R.string.current_location))
                        .snippet(getString(R.string.dragmsg))
                );

        mMap.moveCamera( CameraUpdateFactory.newLatLngZoom(currentLocation , 6.0f) );

        marker.setDraggable(true);
        markerDragListener();
    }

    private void markerDragListener() {
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                LatLng dragToLocation = marker.getPosition();
                Geocoder gcd = new Geocoder(MainActivity.this, Locale.getDefault());
                List<Address> addresses = null;
                String cityName = "";
                try {
                    addresses = gcd.getFromLocation(dragToLocation.latitude, dragToLocation.longitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (addresses != null){
                    if (addresses.get(0).getLocality() != null)
                        cityName = addresses.get(0).getLocality();
                    if (addresses.get(0).getSubAdminArea() != null)
                        cityName = addresses.get(0).getSubAdminArea();
                    if (cityName != "")
                        Toast.makeText(MainActivity.this,
                                getString(R.string.set_loc_to) +
                                        cityName, Toast.LENGTH_SHORT).show();
                }
                mMap.animateCamera(CameraUpdateFactory.newLatLng(dragToLocation));
            }
        });
    }

    private void setUpToolBar() {
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    private void setUpDrawer() {
        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView
                = findViewById(R.id.navigationView);
        navigationView.bringToFront();
        navigationView.setItemIconTintList(null);

        navDrawerItemListener(navigationView);
    }

    private void navDrawerItemListener(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        switch (menuItem.getItemId()) {
                            case R.id.action_seeAgenda:
                                Intent intent = new Intent(MainActivity.this, MyAgenda.class);
                                intent.putParcelableArrayListExtra("list", addedToAgendaList);
                                startActivity(intent);

                                drawerLayout.closeDrawer(GravityCompat.START);
                                break;
                            case R.id.action_help:
                                Toast.makeText(MainActivity.this,
                                        R.string.help_msg
                                        ,
                                        Toast.LENGTH_LONG
                                ).show();
                                drawerLayout.closeDrawer(GravityCompat.START);
                                break;
                            case R.id.action_about:
                                Toast.makeText(MainActivity.this,
                                        R.string.creators
                                        ,
                                        Toast.LENGTH_LONG
                                ).show();
                                drawerLayout.closeDrawer(GravityCompat.START);
                                break;
                        }
                        return false;
                    }
                });
    }

    private void requestNeededPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Toast...
            }

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    101);
        } else {
            touristLocationManager.startLocationMonitoring();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 101) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(this, R.string.permissiongranted, Toast.LENGTH_SHORT).show();

                touristLocationManager.startLocationMonitoring();
            } else {
                Toast.makeText(this, R.string.notgranted, Toast.LENGTH_SHORT).show();
            }
        }
    }


    public Realm getRealm() {
        return ((MainApplication) getApplication()).getRealmSights();
    }

    @Override
    protected void onDestroy() {
        touristLocationManager.stopLocationMonitoring();
        super.onDestroy();
        ((MainApplication)getApplication()).closeRealm();
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setOnInfoWindowClickListener(this);
    }

    @Override
    public void onInfoWindowClick(final Marker marker) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.questionadd) + " " + marker.getTitle() + " " + getString(R.string.to_your_agenda));
        builder.setCancelable(true);

        builder.setPositiveButton(
                R.string.add,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        for(Business bus : highRatingBusinessList) {
                            if (bus.getName().equals(marker.getTitle())) {
                                Business business = (Business) marker.getTag();
                                Sights onClickSight = new Sights();
                                onClickSight.setName(business.getName());
                                onClickSight.setRating(business.getRating());
                                onClickSight.setDone(false);
                                addedToAgendaList.add(onClickSight);

                            }
                        }
                        dialog.cancel();
                    }
                });

        builder.setNegativeButton(
                R.string.negative,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }
}
