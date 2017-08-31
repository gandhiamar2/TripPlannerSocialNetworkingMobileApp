package com.example.gandh.inclass09a;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

public class Chat_trip_places extends AppCompatActivity implements Adaptor_recycler_trip_places.fromadaptor {
    ImageView add_places,map_places,navi;
    RecyclerView rv;
    String trip_id;
    ValueEventListener places_listener,trip_listener;
    Trip trip;
    boolean trip_on = true;
    DatabaseReference mDatabaseReference;
    String place_temp,uri;
    Adaptor_recycler_trip_places adaptor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_trip_places);
        add_places = (ImageView) findViewById(R.id.imageView16);
        navi = (ImageView) findViewById(R.id.imageView19);
        map_places = (ImageView) findViewById(R.id.imageView15);
        rv = (RecyclerView) findViewById(R.id.rv5);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        rv.setLayoutManager(lm);
        trip_id = getIntent().getExtras().getString("trip_id");
        trip_on = getIntent().getExtras().getBoolean("trip_on");
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("inclass09");
        navi.setVisibility(View.INVISIBLE);
        if(!trip_on)
        {
            add_places.setEnabled(false);
        }
        add_places.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .build(Chat_trip_places.this);
                    startActivityForResult(intent, 600);

                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }

            }
        });

        map_places.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iz = new Intent(Chat_trip_places.this,MapsActivity.class);
                iz.putExtra("trip_id",trip_id);
                startActivity(iz);
            }
        });

        navi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
//                        Uri.parse(uri));
//                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
       if((requestCode==600||requestCode==700)&& resultCode == Activity.RESULT_OK)
        {
            final Place place = PlacePicker.getPlace(data, this);

            final CharSequence name = place.getName();
            final CharSequence address = place.getAddress();
            place.toString();
            mDatabaseReference.child("trips").child(trip_id).child("trip_place_list").child(name+"").setValue(place.getLatLng().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        if(requestCode==700)
                        {
                            mDatabaseReference.child("trips").child(trip_id).child("trip_place_list").child(place_temp).removeValue();
                            Toast.makeText(Chat_trip_places.this,"place updated",Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(Chat_trip_places.this,"place added",Toast.LENGTH_SHORT).show();
                        }

                    }

                    place_temp =null;
                }
            });
        }
        else {
            Toast.makeText(this, "You haven't picked place",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        places_listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

            trip.trip_place_list = (Map<String, String>) dataSnapshot.getValue();
                if(trip.trip_place_list!=null) {
                    adaptor = new Adaptor_recycler_trip_places(Chat_trip_places.this, trip.trip_place_list, Chat_trip_places.this);
                    rv.setAdapter(adaptor);
                }
                else
                {
                    Toast.makeText(Chat_trip_places.this,"No more places in trip",Toast.LENGTH_SHORT);
                    mDatabaseReference.child("trips").child(trip_id).child("trip_place_list").removeEventListener(places_listener);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        trip_listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                trip = dataSnapshot.getValue(Trip.class);
                mDatabaseReference.child("trips").child(trip_id).child("trip_place_list").addValueEventListener(places_listener);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mDatabaseReference.child("trips").child(trip_id).addListenerForSingleValueEvent(trip_listener);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode==KeyEvent.KEYCODE_BACK)
        {
            mDatabaseReference.child("trips").child(trip_id).child("trip_place_list").removeEventListener(places_listener);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void edit_place(String title) {
        uri=null;
        try {
            if(trip_on) {
                Intent intent =
                        new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                .build(Chat_trip_places.this);
                place_temp = title;
                startActivityForResult(intent, 700);
            }
            else
                Toast.makeText(Chat_trip_places.this, "no changes are allowed", Toast.LENGTH_SHORT).show();
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }

    }

    @Override
    public void delete_place(String title) {
        uri = null;
        if(trip_on) {
            mDatabaseReference.child("trips").child(trip_id).child("trip_place_list").child(title).removeValue();
            Toast.makeText(Chat_trip_places.this, "place deleted", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(Chat_trip_places.this, "no changes are allowed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void change_position_place(String uri) {
        this.uri = uri;
    }

    @Override
    public void do_nothing() {
        uri =null;
    }


}
