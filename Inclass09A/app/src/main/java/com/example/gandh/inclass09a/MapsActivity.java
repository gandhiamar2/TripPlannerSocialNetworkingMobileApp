package com.example.gandh.inclass09a;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import im.delight.android.location.SimpleLocation;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    String uri;
    SupportMapFragment mapFragment;
    LocationManager locationManager;
    DatabaseReference mDatabaseReference;
    ValueEventListener places_listener;
    Trip trip;
    int clicked=0;
    PolylineOptions lineOptions = null;
    String trip_id;
    ImageView imageView;
    PolylineOptions rectOptions;
    Location mLastLocation;
    Polyline polyline;
    SimpleLocation location;
    LatLngBounds.Builder builder;
    LatLngBounds bound;
    List<List<HashMap<String, String>>> routes2 =null;
    CameraUpdate cu;
    LocationListener mLocationListener;
    GoogleApiClient mGoogleApiClient;
    OkHttpClient client;
    LatLng marker_1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("inclass09");
        trip_id = getIntent().getExtras().getString("trip_id");
        location = new SimpleLocation(this);
         mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        imageView = (ImageView) findViewById(R.id.imageView18);
        imageView.setVisibility(View.INVISIBLE);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse(uri));
                startActivity(intent);
            }
        });
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

    }

    @Override
    protected void onStart() {
        super.onStart();
        places_listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                trip = new Trip();
                trip.trip_place_list = (Map<String, String>) dataSnapshot.getValue();

                mapFragment.getMapAsync(MapsActivity.this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mDatabaseReference.child("trips").child(trip_id).child("trip_place_list").addValueEventListener(places_listener);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!locationManager.isProviderEnabled(locationManager.GPS_PROVIDER)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("GPS not enabled").setMessage("Would you like to enable GPS settings").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    finish();
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
        else {
            location.beginUpdates();
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        ArrayList<LatLng> all_places = new ArrayList<>();
        ArrayList<String> titles = new ArrayList<>();
        builder = new LatLngBounds.Builder();
        //all_places.add(new LatLng(location.getLatitude(),location.getLongitude()));
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling.
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                clicked++;
                if(clicked==1)
                {
                    imageView.setVisibility(View.INVISIBLE);
                    marker_1 = marker.getPosition();
                    uri = null;
                }

                else if (clicked==2)
                {
                     uri = "http://maps.google.com/maps?saddr=" + marker_1.latitude + "," + marker_1.longitude + "&daddr=" + marker.getPosition().latitude + "," + marker.getPosition().longitude;
                    imageView.setVisibility(View.VISIBLE);
                    marker_1 = null;
                    clicked=0;
                }

                return false;
            }
        });
        all_places.add(new LatLng(location.getLatitude(),location.getLongitude()));
        titles.add("My location");
        for (String s :
                trip.trip_place_list.keySet()) {
            String temp = trip.trip_place_list.get(s);
            String[] latlong = temp.replace("lat/lng: (", "").replace(")", "").split(",");
            double latitude = Double.parseDouble(latlong[0]);
            double longitude = Double.parseDouble(latlong[1]);
            LatLng a = new LatLng(latitude, longitude);
            all_places.add(a);
            titles.add(s);
        }
        all_places.add(new LatLng(location.getLatitude(),location.getLongitude()));
        titles.add("My location");
        for (int i=0; i<all_places.size()-1;i++) {
            builder.include(all_places.get(i));
             mMap.addMarker(new MarkerOptions().position(all_places.get(i)).title(titles.get(i)));
        }
        bound= builder.build();
        cu = CameraUpdateFactory.newLatLngBounds(bound, 30);
        mMap.moveCamera(cu);
//        Collections.sort(all_places, new Comparator<LatLng>() {
//            @Override
//            public int compare(LatLng o1, LatLng o2) {
//
//                return 0;
//            }
//        });
        for (int i=0; i<all_places.size()-1;i++)
        {
            ;
            client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(getDirectionsUrl(all_places.get(i),all_places.get(i+1)))
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String data = response.body().string();


                    try {
                        JSONObject jObject = new JSONObject(data);
                       routes2=  parse(jObject);
                        if(routes2.size()!=0)
                        map_updater(routes2);
                        else
                        {
                            Toast.makeText(MapsActivity.this," unreachable places is present",Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
        }
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }


    void map_updater(List<List<HashMap<String, String>>> routes1)
    {
        ArrayList<LatLng> points = null;

        MarkerOptions markerOptions = new MarkerOptions();
        Log.d("map", routes1.get(0).get(0).toString()+routes1.get(0).get(routes1.get(0).size()-1).toString());

        // Traversing through all the routes
        for (int i = 0; i < routes1.size(); i++) {
            points = new ArrayList<LatLng>();
            lineOptions = new PolylineOptions();

            // Fetching i-th route
            List<HashMap<String, String>> path = routes1.get(i);

            // Fetching all the points in i-th route
            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);
                points.add(position);
            }

            // Adding all the points in the route to LineOptions
            lineOptions.addAll(points);
            lineOptions.width(2);
            lineOptions.color(Color.RED);
        }

        // Drawing polyline in the Google Map for the i-th route

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMap.addPolyline(lineOptions);
            }
        });

    }

        /** Receives a JSONObject and returns a list of lists containing latitude and longitude */
        public List<List<HashMap<String,String>>> parse(JSONObject jObject){

            List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String,String>>>() ;
            JSONArray jRoutes = null;
            JSONArray jLegs = null;
            JSONArray jSteps = null;

            try {

                jRoutes = jObject.getJSONArray("routes");

                /** Traversing all routes */
//                for(int i=0;i<jRoutes.length();i++){
                                for(int i=0;i<1;i++){
                    jLegs = ( (JSONObject)jRoutes.get(i)).getJSONArray("legs");
                    List path = new ArrayList<HashMap<String, String>>();

                    /** Traversing all legs */
                    for(int j=0;j<jLegs.length();j++){
                        jSteps = ( (JSONObject)jLegs.get(j)).getJSONArray("steps");

                        /** Traversing all steps */
                        for(int k=0;k<jSteps.length();k++){
                            String polyline = "";
                            polyline = (String)((JSONObject)((JSONObject)jSteps.get(k)).get("polyline")).get("points");
                            List<LatLng> list = decodePoly(polyline);

                            /** Traversing all points */
                            for(int l=0;l<list.size();l++){
                                HashMap<String, String> hm = new HashMap<String, String>();
                                hm.put("lat", Double.toString(((LatLng)list.get(l)).latitude) );
                                hm.put("lng", Double.toString(((LatLng)list.get(l)).longitude) );

                                path.add(hm);

                            }
                        }
                        routes.add(path);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }catch (Exception e){
            }

            return routes;
        }
        /**
         * Method to decode polyline points
         * Courtesy : http://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
         * */
        private List<LatLng> decodePoly(String encoded) {

            List<LatLng> poly = new ArrayList<LatLng>();
            int index = 0, len = encoded.length();
            int lat = 0, lng = 0;

            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                LatLng p = new LatLng((((double) lat / 1E5)),
                        (((double) lng / 1E5)));
                poly.add(p);
            }

            return poly;
        }

    private String getDirectionsUrl(LatLng origin,LatLng dest){

        String str_origin = "origin="+origin.latitude+","+origin.longitude;
        String str_dest = "destination="+dest.latitude+","+dest.longitude;
        String sensor = "sensor=false";
        String parameters = str_origin+"&"+str_dest+"&"+sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;
        return url;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK)
        {
            mDatabaseReference.child("trips").child(trip_id).child("trip_place_list").removeEventListener(places_listener);
            location.endUpdates();
            finish();

        }
        return super.onKeyDown(keyCode, event);
    }
}
