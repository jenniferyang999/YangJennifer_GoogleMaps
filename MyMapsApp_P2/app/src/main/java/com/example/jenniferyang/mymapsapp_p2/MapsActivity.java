package com.example.jenniferyang.mymapsapp_p2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


//how to bind buttons
// how to create a row in a database, how to display information to device screen
//how to make alert dialog boxes, how to pass information from one activity to another
//how to provide access to system location service, different types of edit text fields

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText locationSearch;
    private LocationManager locationManager;
    private Location myLocation;

    private boolean gotMyLocationOneTime;

    private static final long MIN_TIME_BW_UPDATES = 1000 * 5;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 0.0f;
    private static final int MY_LOC_ZOOM_FACTOR = 17;

    private boolean isGPSEnabled = false;
    private boolean isNETWORKEnabled = false;
    private boolean notTrackingMyLocation = true;

    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        //add a marker on the map that shows your place of birth
        //and displays the message "born here" when tapped
        LatLng morristown = new LatLng(41, -74);
        mMap.addMarker(new MarkerOptions().position(morristown).title("Born here!"));
        mMap.moveCamera((CameraUpdateFactory.newLatLng(morristown)));                                    ///////////////////////////////////////////////
        /**
         if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
         Log.d("MYMAPSAPP", "failed FINE permission check");
         ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);

         }

         if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
         Log.d("MYMAPSAPP", "failed COARSE permission check");
         ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);

         }

         if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) ||
         (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
         mMap.setMyLocationEnabled(true);
         } */

        locationSearch = (EditText) findViewById(R.id.editText_searchMap);

        gotMyLocationOneTime = false;
        getLocation();


    }

    //Add View button and method (changeView) to switch between satellite and map views
    public void changeView(View v) {
        if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
            Log.d("MYMAPSAPP", "changeView: map type = normal");
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            Log.d("MYMAPSAPP", "changeView: map type = changed from normal to satellite");
        } else {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }

    public void onSearch(View v) {
        String location = locationSearch.getText().toString();
        List<Address> addressList = null;

        //Use locationManager for user location info
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = service.getBestProvider(criteria, false);

        Log.d("MYMAPSAPP", "onSearch: location= " + location);
        Log.d("MYMAPSAPP", "onSearch: provider= " + provider);

        LatLng userLocation = null;
        try {
            //check last known location, need to specifically list provider (network or gps)
            if (locationManager != null) {
                if ((myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)) != null) {
                    userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    Log.d("MYMAPSAPP", "onSearch: try-catch: using NETWORK_PROVIDER, userLocation is: " + myLocation.getLatitude() + " " + myLocation.getLongitude());
                    Toast.makeText(this, "Userloc: " + myLocation.getLatitude() + " " + myLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                } else if ((myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)) != null) {
                    userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    Log.d("MYMAPSAPP", "onSearch: try-catch: using GPS_PROVIDER, userLocation is: " + myLocation.getLatitude() + " " + myLocation.getLongitude());
                    Toast.makeText(this, "Userloc: " + myLocation.getLatitude() + " " + myLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("MYMAPSAPP", "onSearch: try-catch: location is null =(");
                }
            }

        } catch (SecurityException | IllegalArgumentException e) {
            Log.d("MYMAPSAPP", "onSearch: try-catch: threw an exception on getLastKnownLocation");

        }

        if (!location.matches("")) {
            //create Geocoder
            Geocoder geocoder = new Geocoder(this, Locale.US);
            Log.d("MYMAPSAPP", "onSearch: location has a value");

            try {
                //get a list of addresses
                addressList = geocoder.getFromLocationName(location, 100, userLocation.latitude - (5.0 / 60.0),
                        userLocation.longitude - (5.0 / 60.0), userLocation.latitude + (5.0 / 60.0), userLocation.longitude + (5.0 / 60.0));
                Log.d("MYMAPSAPP", "onSearch: try-catch-2: created addressList");

            } catch (IOException e) {
                e.printStackTrace();
                Log.d("MYMAPSAPP", "onSearch: try-catch-2: did not create addressList");
            }

            if (!addressList.isEmpty()) {
                Log.d("MYMAPSAPP", "onSearch: try-catch-2: addressList size is: " + addressList.size());
                for (int i = 0; i < addressList.size(); i++) {
                    Address address = addressList.get(i);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(latLng).title(i + ": " + address.getSubThoroughfare()));
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                }
            }
        } else {
            Toast.makeText(this, "Search entry: no such place exists within parameters", Toast.LENGTH_SHORT).show();

        }
    }

    public void trackMyLocation(View v) {
        //kick off the location tracker using getLocation method to start the Location listeners

        if (notTrackingMyLocation) {
            getLocation();
            notTrackingMyLocation = false;
            Log.d("MYMAPSAPP", "trackMyLocation: tracking location");

        } else {
            //removeUpdates for both network and gps; n
            locationManager.removeUpdates(locationListenerNetwork);
            locationManager.removeUpdates(locationListenerGps);
            notTrackingMyLocation = true;
            Log.d("MYMAPSAPP", "trackMyLocation: not tracking location + disabled gps + network");


        }
        //if gps not on then turn on, if gps on then turn on
        /*LatLng latLng;
        latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        mMap.addMarker(new MarkerOptions().position(latLng).title("Position: " + count));
        count++;*/
    }


    public void clearMarkers(View v) {
        mMap.clear();
    }

    //method getLocation to place a marker at current location
    public void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            //get GPS status
            //isProvider enabled returns true if user has enabled gps
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSEnabled) {
                Log.d("MYMAPSAPP", "getLocation: try-catch-1: GPS is enabled");
            }

            //get network status
            //isProvider enabled returns true if user has enabled network
            isNETWORKEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isNETWORKEnabled) {
                Log.d("MYMAPSAPP", "getLocation: try-catch-1: NETWORK is enabled");
            }

            if (!isGPSEnabled && !isNETWORKEnabled) {
                Log.d("MYMAPSAPP", "getLocation: try-catch-1: NO PROVIDER ENABLED :((((((");
            } else {
                if (isNETWORKEnabled) {
                    if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) &&
                            (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                }
                if (isGPSEnabled) {
                    //launch locationListenerGps
                    // code here
                    if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) &&
                            (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGps);
                }

            }
        } catch (Exception e) {
            Log.d("MYMAPSAPP", "getLocation: try-catch-1: caught exception");
            e.printStackTrace();
        }

    }

    //locationListener is an anonymous inner class
    //setup for callbacks from the requestLocationUpdates
    LocationListener locationListenerNetwork = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            dropAmarker(LocationManager.NETWORK_PROVIDER);

            //check if doing one time via onMapReady, if so remove updates to both gps and network
            if (gotMyLocationOneTime == false) {
                locationManager.removeUpdates(this);
                locationManager.removeUpdates(locationListenerNetwork);
                gotMyLocationOneTime = true;
            } else {
                //if here, then tracking so relaunch request for network
                if ((ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) &&
                        (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);

            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            Log.d("MYMAPSAPP", "LocationListenerNetwork: status change");

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }

    };

    LocationListener locationListenerGps = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            dropAmarker(LocationManager.GPS_PROVIDER);
            locationManager.removeUpdates(this);
            locationManager.removeUpdates(locationListenerGps);

            //if doing one time remove updates to both gps and network

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            switch (i) {
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("MYMAPSAPP", "LocationListenerGps: LocationProvider.OUT_OF_SERVICE");
                    if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                    if (!notTrackingMyLocation) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGps);
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    }
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("MYMAPSAPP", "LocationListenerGps: LocationProvider.TEMPORARILY UNAVAILABLE");
                    if (!notTrackingMyLocation) {

                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGps);
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    }
                    break;
                default:
                    Log.d("MYMAPSAPP", "LocationListenerGps: Locati 67  onProvider DEFAULT");
                    if (!notTrackingMyLocation) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGps);
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    }
            }

            //switch (i)
            //printout log.d and, or toast message
            //break;
            //case LocationProvider.OUT_OF_SERVICE:
            //break;
            //case LocationProvider.TEMPORARILY_UNAVAILABLE:
            //enable both network and gps
            //break;
            //default:
            //enable both network and gps
        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    private void dropAmarker(String provider) {

        if (locationManager != null) {
            if ((ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) &&
                    (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                return;
            }
            else {
                myLocation = locationManager.getLastKnownLocation(provider);
            }
            LatLng userLocation = null;

            if (myLocation == null)
            {
                Log.d("MYMAPSAPP", "dropAmarker: myLocation is null");
            }
            else {
                Log.d("MYMAPSAPP", "dropAmarker: IT WORKS");

                userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(userLocation, MY_LOC_ZOOM_FACTOR);
                if (provider == LocationManager.GPS_PROVIDER) {
                    mMap.addCircle(new CircleOptions()
                        .center(userLocation)
                        .radius(1)
                        .strokeColor(Color.RED)
                        .strokeWidth(2)
                        .fillColor(Color.RED)
                    );
                    mMap.addCircle(new CircleOptions()
                        .center(userLocation)
                        .radius(3)
                        .strokeColor(Color.RED)
                        .strokeWidth(2)
                        .fillColor(Color.TRANSPARENT)
                    );
                    mMap.addCircle(new CircleOptions()
                        .center(userLocation)
                        .radius(5)
                        .strokeColor(Color.RED)
                        .strokeWidth(2)
                        .fillColor(Color.TRANSPARENT)
                    );
                }
                else {
                    mMap.addCircle(new CircleOptions()
                            .center(userLocation)
                            .radius(1)
                            .strokeColor(Color.BLUE)
                            .strokeWidth(2)
                            .fillColor(Color.BLUE)
                    );
                    mMap.addCircle(new CircleOptions()
                            .center(userLocation)
                            .radius(3)
                            .strokeColor(Color.BLUE)
                            .strokeWidth(2)
                            .fillColor(Color.TRANSPARENT)
                    );
                    mMap.addCircle(new CircleOptions()
                            .center(userLocation)
                            .radius(5)
                            .strokeColor(Color.BLUE)
                            .strokeWidth(2)
                            .fillColor(Color.TRANSPARENT)
                    );
                    mMap.animateCamera(update);
                }
            }
        }
        /** if(locationManager != null)
         * if (checkSelfPermission fails
         *  return
         * else (myLocation = locationManager.getLastKnownLocation(provider)
         * LatLng userLocation = null
         * if (myLocation == null) print log or toast message
         * else (
         *          userLocation = new LatLng(myLocation.getLatitude, myLocation.getLongitude)
         *          CameraUpdate update = CameraUpdateFactory.new LatLngZoom(userLocation, MY_LOC_ZOOM_FACTOR)
         *          if (provider == LocationManager.GPS_PROVIDER
         *                add circle for the marker wit 2 outer rings (red)
         *                  mMap.addCircle(new CircleOptions())
         *                        .center(userLocation)
         *                        .radius(1)
         *                        .strokeColor(Color.RED)
         *                        .strokeWidth(2)
         *                        .fillColor(Color.RED)
         *          else (add circle for the marker with 2 outer rings (blue)
         *          mMap.animateCamera(update)
         */

    }
}






























