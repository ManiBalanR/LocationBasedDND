package com.example.mapgoogleplaces;


import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GeoQueryEventListener {

    private static final String TAG = "MapActivity";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;

    //widgets
    private EditText mSearchText;
    private ImageView mGps,add_location,magnify;

    //vars
    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private Marker mark;
    private List<LatLng> markedArea;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private DatabaseReference myLocationRef;
    private GeoFire geoFire;
    String latlng,address;
    LatLng ll;

    AudioManager am;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mSearchText =  findViewById(R.id.input_search);
        mSearchText.setInputType(InputType.TYPE_CLASS_TEXT| InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        mGps =  findViewById(R.id.ic_gps);
        add_location = findViewById(R.id.ic_addlocation);
        magnify = findViewById(R.id.ic_magnify);
        am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);


        getLocationPermission();


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;

        if (mLocationPermissionsGranted) {
            getDeviceLocation();

            if(mFusedLocationProviderClient != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                }
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.getUiSettings().setZoomControlsEnabled(true);

            init();
        }



    }

    @Override
    protected  void onStop(){
        //fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onStop();
    }

    @Override
    public void onKeyEntered(String key, GeoLocation location) {
        sendNotification("SilentMode",String.format("%s in marked area ",key));
        am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        Toast.makeText(this,"DND ON", Toast.LENGTH_SHORT).show();;


    }

    @Override
    public void onKeyExited(String key) {
        sendNotification("NormalMode",String.format("%s left marked area ",key));
        am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        Toast.makeText(this,"DND OFF", Toast.LENGTH_SHORT).show();;
    }

    @Override
    public void onKeyMoved(String key, GeoLocation location) {
        sendNotification("SilentMode",String.format("%s move within marked area ",key));
    }

    @Override
    public void onGeoQueryReady() {

    }

    @Override
    public void onGeoQueryError(DatabaseError error) {
        Toast.makeText(this,""+error.getMessage(), Toast.LENGTH_SHORT).show();;
    }



    private void init(){
        Log.d(TAG, "init: initializing");

        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){

                    //execute our method for searching
                    geoLocate();
//                    magnify.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//
//                        }
//                    });
                }

                return false;
            }
        });

        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked gps icon");
                getDeviceLocation();
            }
        });

        add_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mSearchText.getText().toString();
                String address = getAddress();
                String latlng = getLatlng();
                initArea();
                Intent i = new Intent(MapActivity.this,AddLocations.class);
                i.putExtra("name",name);
                i.putExtra("address",address);
                i.putExtra("latlng",latlng);
                startActivity(i);
            }
        });

        hideSoftKeyboard();
    }

    public void initArea() {

        markedArea = new ArrayList<>();
        markedArea.add(getActualLatLng());
        // markedArea.add(new LatLng(13.0691, 80.2430));
        //markedArea.add(new LatLng(13.0427, 80.2667));

        //Add circle for markedArea
        for (LatLng ltLng : markedArea)
        {
            mMap.addCircle(new CircleOptions().center(ltLng)
                    .radius(100) //500m
                    .strokeColor(Color.BLUE)
                    .fillColor(0x220000FF)  //22 is transparent code
                    .strokeWidth(5.0f)
            );

            //Create GeoQuery when user in marked location
            GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(ltLng.latitude,ltLng.longitude),0.5f);
            geoQuery.addGeoQueryEventListener(MapActivity.this);

        }
    }

    private void geoLocate(){
        Log.d(TAG, "geoLocate: geolocating");

        String searchString = mSearchText.getText().toString();
        Log.d(TAG, "geoLocate: string: "+searchString);

        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> list = new ArrayList<>();
        try{
          //  if(searchString!=null || searchString.equals(""))
            Log.d(TAG, "geoLocate: inside try: "+searchString);

            list = geocoder.getFromLocationName(searchString, 1);
          //  else
          //      Log.d(TAG, "geoLocate: null: ");
        }catch (IOException e){
            Log.d(TAG, "geoLocate: inside catch: "+searchString);
            Toast.makeText(this,"geoLocate: inside catch:"+e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "geoLocate: IOException: " + e.getMessage() );
        }

        if(list.size() > 0){
            Address address = list.get(0);

            Log.d(TAG, "geoLocate: found a location: " + address.toString());
            //Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();

            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM,
                    address.getAddressLine(0));
        }
    }

    private void settingGeoFire() {
        myLocationRef = FirebaseDatabase.getInstance().getReference("MyLocation");
        geoFire = new GeoFire(myLocationRef);
    }

    private void getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if(mLocationPermissionsGranted){

                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();
                            geoFire.setLocation("My Location",new GeoLocation(currentLocation.getLatitude(),currentLocation.getLongitude()));

                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    DEFAULT_ZOOM,
                                    "My Location");

                        }else{
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
        }
    }

    private void moveCamera(LatLng latLng, float zoom, String title){
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        setActualLatLng(latLng);
        if(!title.equals("My Location")){
            mark = mMap.addMarker( new MarkerOptions()
                    .position(latLng)
                    .title(title));
        }


        String latlng = " lat: " + latLng.latitude + ", lng: " + latLng.longitude;
        setLatlng(latlng);
        setAddress(title);

       // hideSoftKeyboard();
    }

    public void setActualLatLng(LatLng actualLatLng){
        this.ll = actualLatLng;
    }

    public LatLng getActualLatLng(){
        return ll;
    }

    public void setLatlng(String latlng) {
        this.latlng = latlng;
    }

    public String getLatlng() {
        return latlng;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    private void initMap(){
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(MapActivity.this);
    }

    private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
                initMap();
                settingGeoFire();
            }else{
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    //initialize our map
                    initMap();
                    settingGeoFire();
                }
            }
        }
    }

    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void sendNotification(String title, String content) {

        Toast.makeText(this,""+content, Toast.LENGTH_SHORT).show();

        String NOTIFICATION_CHANNEL_ID = "mark_multiple_location";
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,"My Notification",
                    NotificationManager.IMPORTANCE_DEFAULT);

            //Config
            notificationChannel.setDescription("Channel Description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0,1000,500,1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(false)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher));

        Notification notification = builder.build();
        notificationManager.notify(new Random().nextInt(),notification);
    }

}

// mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
//@Override
//public boolean onMarkerClick(Marker marker) {
//        i++;
//
//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//@Override
//public void run() {
//        if(i == 1){
//        Toast.makeText(MapActivity.this, "single click", Toast.LENGTH_SHORT).show();
//        }else if (i == 2){
//        Toast.makeText(MapActivity.this, "double click", Toast.LENGTH_SHORT).show();
//        }
//        }
//        }, 500);
//        return false;
//        }
//        });
