package com.infantechar.application;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.infantechar.application.ar.ArActivity;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.message.BasicHeader;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final String DOMAIN = "http://34.209.105.118";
    private static final String URL = DOMAIN + "/getavailablepromoitems";
    private Location lastLocation;

    Button walletButton;
    Button arButton;
    Button testButton;

    static String TEST_URL = DOMAIN + "/getavailablepromoitemswithinarea/";
    static float PROXIMITY = 100.0f;    // maximum distance of objects from current location to be displayed
    static Boolean mapActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
        walletButton = (Button) findViewById(R.id.walletButton);
        arButton = (Button) findViewById(R.id.arButton);

        addARButtonListener();
        addWalletButtonListener();
        addLocationUpdateListener();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        double latitude = 0;
        double longitude = 0;

        ApplicationLocationListener locationListener = new ApplicationLocationListener(this);

        if (locationListener.canGetLocation()) {
            latitude = locationListener.getLatitude();
            longitude = locationListener.getLongitude();
        } else {
            locationListener.showSettingsAlert();
        }
        LatLng currLoc = new LatLng(latitude, longitude);
        /*Circle circle = mMap.addCircle(new CircleOptions()
                .center(currLoc)
                .radius(500).fillColor(Color.argb(50,0,0,100))
                .strokeColor(Color.argb(50,0, 0, 200)));*/

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currLoc, 15.0f));

        loadLocations();
        addMarkerClickListener();
    }

    public void addMarkerClickListener(){
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                LatLng latLng = marker.getPosition();
                double latitude = latLng.latitude;
                double longitude = latLng.longitude;

                if(!marker.getTitle().equals("You are here")){
                    String title[] = marker.getTitle().split(":");
                    Toast.makeText(MapsActivity.this, "Id: " + title[0] + "\nDescription: " +  title[1] + "\nLatitude: "
                            + latitude + "\nLongitude: " + longitude, Toast.LENGTH_SHORT).show();
                }

                return false;
            }
        });
    }

    public void loadLocations() {
        if(!mapActive) {
            mapActive = true;
            if (mMap != null) {
                mMap.clear();
            }
            Toast.makeText(MapsActivity.this, "Loading/Updating map markers", Toast.LENGTH_SHORT).show();
            List<Header> headers = new ArrayList<>();
            headers.add(new BasicHeader("Accept", "application/json"));
            ApplicationCallService.get(this, URL, headers.toArray(new Header[headers.size()]), null, new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONArray response) {

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            double latitude = (Double) obj.get("latitude");
                            double longitude = (Double) obj.get("longitude");
                            String keyword = (String) obj.get("keyword");
                            LatLng latLng = new LatLng(latitude, longitude);
                            int n = BitmapEnum.getEnum(keyword).getBitmap();
                            BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(n);

                            mMap.addMarker(new MarkerOptions().position(latLng).title((Integer) obj.get("promotionItemId") + ":" + (String) obj.get("description")).icon(bitmap));


                        } catch (JSONException e) {
                            Toast.makeText(MapsActivity.this, "JSONException encountered", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(MapsActivity.this, "Exception encountered: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Toast.makeText(MapsActivity.this, "Failed to call the service: " + errorResponse.toString(), Toast.LENGTH_SHORT).show();
                }


            });
        }
    }

    public void addLocationUpdateListener(){
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                lastLocation = location;
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        }
    }

    public void addWalletButtonListener(){
        walletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), WalletActivity.class);
                startActivity(i);
            }
        });
    }

    public void addARButtonListener(){
        arButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ApplicationUtil.checkCameraHardware(MapsActivity.this)) {
                    Intent i = new Intent(getApplicationContext(), ArActivity.class);
                    startActivity(i);
                    //Toast.makeText(MapsActivity.this, "Switching to AR. Camera found and permission to access is granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MapsActivity.this, "Camera not detected or you don't have permission to access the camera", Toast.LENGTH_SHORT).show();
                }
                /*launchApplication("com.infantechar.promoitemloader");
                launchApplication("com.ar.infantechar.infantecharwallet");*/
            }
        });
    }

    public void launchApplication(String packageName){
        // Verify it resolves
        PackageManager packageManager = getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(packageName);

        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

        if(activities.size() > 0){
            startActivity(intent);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        mapActive = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLocations();
    }

}