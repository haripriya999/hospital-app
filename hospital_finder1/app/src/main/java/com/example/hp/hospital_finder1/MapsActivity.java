package com.example.hp.hospital_finder1;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
private GoogleMap mMap;
private GoogleApiClient googlApiClient;
private LocationRequest locationRequest;
private Location lastLocation;
private int proximityRaidus=10000;
private Marker currentLocationMarker;

    double latitude,longitude;
private static final int Request_user_location=99;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            checkUserLocationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    private boolean CheckGooglePlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if(result != ConnectionResult.SUCCESS) {
            if(googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        0).show();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);//optional

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }
    public void onClick(View view)
    {
        String hospital="hospital";
        Object transferData[]=new Object[2];
        NearByHospitals getNearByHospitals=new NearByHospitals();


        switch(view.getId())
        {
            case R.id.search_here:
                EditText address_text=(EditText)findViewById(R.id.search_location);
                String address=address_text.getText().toString();
                List<Address> addressList=null;
                MarkerOptions markerOptions=new MarkerOptions();
                if(!TextUtils.isEmpty(address))
                {
                    Geocoder geocoder=new Geocoder(this);
                    try {
                        addressList =geocoder.getFromLocationName(address,6);
                        if(addressList!=null){
                            for(int i=0;i<addressList.size();i++)
                            {
                                Address user_address=addressList.get(i);
                                LatLng latLng=new LatLng(user_address.getLatitude(),user_address.getLongitude());
                                markerOptions.position(latLng);
                                markerOptions.title(address);
                                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                                mMap.addMarker(markerOptions);
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
                            }}
                        else
                        {
                            Toast.makeText(this,"location not found..",Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }
                else{
                    Toast.makeText(this,"enter the location you want to search for.",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.hospital:
                mMap.clear();
                String url=getUrl(latitude,longitude,hospital);
                transferData[0]=mMap;
                transferData[1]=url;
                getNearByHospitals.execute(transferData);
                Toast.makeText(this,"Searching for near by hospitals",Toast.LENGTH_SHORT).show();
                Toast.makeText(this,"Showing near by hospitals",Toast.LENGTH_SHORT).show();
                break;
        }
    }
    protected synchronized void buildGoogleApiClient(){
        googlApiClient=new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googlApiClient.connect();

    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest=new LocationRequest();
        locationRequest.setInterval(1100);
        locationRequest.setFastestInterval(1100);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
            LocationServices.FusedLocationApi.requestLocationUpdates(googlApiClient,locationRequest,this);
        }
    }
    private String getUrl(double latitude,double longitude,String hospital)
    {
        StringBuilder googleUrl=new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googleUrl.append("location="+latitude+","+longitude);
        googleUrl.append("&radius="+ proximityRaidus);
        googleUrl.append("&type="+hospital);
        googleUrl.append("&sensor=true");
        googleUrl.append("&key="+"AIzaSyC8N5CeyLKrgsAJ7lUqp2QJxJJcWIW3wsk");
        Log.d("MapsActivity","url="+googleUrl.toString());
        return googleUrl.toString();

    }
    @Override
public void onLocationChanged(Location location) {
    latitude=location.getLatitude();
    longitude=location.getLongitude();
    lastLocation=location;
            if(currentLocationMarker!=null)
            {
                currentLocationMarker.remove();
            }
            LatLng latLng =new LatLng(location.getLatitude(),location.getLongitude());
            MarkerOptions markerOptions=new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("you are here");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
         currentLocationMarker=mMap.addMarker(markerOptions);
         mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
         mMap.animateCamera(CameraUpdateFactory.zoomBy(10));
if(googlApiClient!=null)
{
LocationServices.FusedLocationApi.removeLocationUpdates(googlApiClient,this);

}

        }
    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }





    public boolean checkUserLocationPermission()
        {
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
            {
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION))
                {
                    ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},Request_user_location);
                }
                else
                {
                    ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},Request_user_location);
                }
                return false;
            }
            else
            {
                return true;
            }
        }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode)
        {
            case Request_user_location:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {
                    if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)
                    {
                        if(googlApiClient==null)
                        {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                }
                else
                {
                  Toast.makeText(this,"permission denied..",Toast.LENGTH_SHORT).show();
                }



        }
    }




/**
 * Manipulates the map once available.
 * This callback is triggered when the map is ready to be used.
 * This is where we can add markers or lines, add listeners or move the camera. In this case,
 * we just add a marker near Sydney, Australia.
 * If Google Play services is not installed on the device, the user will be prompted to install
 * it inside the SupportMapFragment. This method will only be triggered once the user has
 * installed Google Play services and returned to the app.
 */






}
