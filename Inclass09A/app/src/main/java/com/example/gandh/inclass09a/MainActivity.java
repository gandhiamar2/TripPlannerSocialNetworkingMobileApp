package com.example.gandh.inclass09a;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements Adaptor_recycler.fromadaptor {
    Toolbar t;
    Adaptor_recycler adaptor_recycler;
    RecyclerView rv1;
    TextView my_trips, discover;
    FloatingActionButton fb;
    EditText find_frnds;
    ImageView search, new_request;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private  String uid;
    View vi;
    User cur_user;
    Trip trip;
    ValueEventListener trips_listener,curuser_listener,frndrqst_listener,alluser_listener;
    DatabaseReference mDatabase;
    Bitmap selected_image;
    LayoutInflater inflater;
    StorageReference fs;
    ArrayList<Trip> triplist, my_tripslist,discover_list ;
    Boolean discover_b = false;
    ProgressDialog pd;
    Map<String,User> all_user_map;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        match_views();

        new_request.setVisibility(View.INVISIBLE);
        my_tripslist = new ArrayList<>();
        fs = FirebaseStorage.getInstance().getReference();
        setSupportActionBar(t);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        rv1.setLayoutManager(lm);
        pd = new ProgressDialog(this);

       // mAuth.signOut();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("inclass09");

        inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vi = inflater.inflate(R.layout.activity_trip,null);
                 trip = new Trip();
                trip.unique = UUID.randomUUID().toString();
                vi.findViewById(R.id.imageView4).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                                photoPickerIntent.setType("image/*");
                                startActivityForResult(photoPickerIntent, 400);
                    }
                });
               new AlertDialog.Builder(MainActivity.this)
               .setView(vi)
                       .setPositiveButton("Add Trip", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                trip.title = ((EditText) vi.findViewById(R.id.editText)).getText().toString();
                                trip.place = ((EditText) vi.findViewById(R.id.editText3)).getText().toString();
                                trip.owner_uid = uid;
                                if(trip.title==null||trip.place==null||selected_image==null)
                                    Toast.makeText(MainActivity.this,"all fields are manadatory",Toast.LENGTH_SHORT).show();
                                else {
                                    trip.members.put(trip.owner_uid, trip.owner_uid);
                                    storeImageToFirebase(selected_image);
                                    selected_image= null;
                                }

                            }
                        }).create().show();
            }
        });

        my_trips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discover_b = false;
                adaptor_recycler = new Adaptor_recycler(MainActivity.this,my_tripslist,MainActivity.this,discover_b);
                my_trips.setBackgroundColor(Color.parseColor("#2196f3"));
                discover.setBackgroundColor(Color.parseColor("#ffffff"));
                rv1.setAdapter(adaptor_recycler);

            }
        });
        discover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discover_b=true;
                adaptor_recycler = new Adaptor_recycler(MainActivity.this,discover_list,MainActivity.this,discover_b);
                discover.setBackgroundColor(Color.parseColor("#2196f3"));
                my_trips.setBackgroundColor(Color.parseColor("#ffffff"));
                rv1.setAdapter(adaptor_recycler);
            }
        });
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ib = new Intent(MainActivity.this,Friends_activity.class);
                ib.putExtra("search",find_frnds.getText().toString());
                find_frnds.setText("");
                startActivity(ib);
            }
        });
        find_frnds.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                    Log.d("dem1o",s+"");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        new_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ib = new Intent(MainActivity.this,Friends_activity.class);
                ib.putExtra("search","requestwatcher");
                new_request.setVisibility(View.INVISIBLE);
                startActivity(ib);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        frndrqst_listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot2) {
                Log.d("find","d");
                if(dataSnapshot2.exists())
                {
                    User req_wat_user = new User();
                    req_wat_user.relationship = (Map<String, Integer>) dataSnapshot2.getValue();

                    if(req_wat_user.relationship.values().toString().contains("-1"))
                    {
                        new_request.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        trips_listener =   new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                my_tripslist = new ArrayList<Trip>();
                discover_list = new ArrayList<Trip>();
                triplist = new ArrayList<Trip>();
                Log.d("find","c");
                for (DataSnapshot snapshot :
                        dataSnapshot.getChildren()) {
                    Trip trip1;
                    trip1 = snapshot.getValue(Trip.class);
                    triplist.add(trip1);
                    if (trip1.members.containsValue(uid)) {
                        my_tripslist.add(trip1);

                    } else {
                        if (cur_user.relationship.containsKey(trip1.owner_uid))
                            if (cur_user.relationship.get(trip1.owner_uid) == 0)
                                discover_list.add(trip1);
                    }


                }
                if (discover_b) {
                    adaptor_recycler = new Adaptor_recycler(MainActivity.this, discover_list, MainActivity.this, discover_b);
                    discover.setBackgroundColor(Color.parseColor("#2196f3"));
                    my_trips.setBackgroundColor(Color.parseColor("#ffffff"));
                }
                else {
                    adaptor_recycler = new Adaptor_recycler(MainActivity.this, my_tripslist, MainActivity.this, discover_b);
                    my_trips.setBackgroundColor(Color.parseColor("#2196f3"));
                    discover.setBackgroundColor(Color.parseColor("#ffffff"));
                }

                rv1.setAdapter(adaptor_recycler);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        alluser_listener= new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                all_user_map = new HashMap<String, User>();
                for (DataSnapshot snapshot :
                        dataSnapshot.getChildren()) {

                    all_user_map.put(snapshot.getKey(),snapshot.getValue(User.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        curuser_listener =    new ValueEventListener() {
             @Override
             public void onDataChange(final DataSnapshot dataSnapshot1) {
                     runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(!dataSnapshot1.exists())
                    {
                        Toast.makeText(MainActivity.this,"Signup is not proper!",Toast.LENGTH_SHORT).show();
                        mAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                MainActivity.this.recreate();
                            }
                        });

                    }
                    else
                    {
                        Log.d("find","b");
                        cur_user = dataSnapshot1.getValue(User.class);
                        pd.dismiss();
                        mDatabase.child("users").addListenerForSingleValueEvent(alluser_listener);
                        mDatabase.child("trips").addValueEventListener(trips_listener);
                        mDatabase.child("users").child(uid).child("relationship").addValueEventListener(frndrqst_listener);

                    }

                }

            });

            }
             @Override
                 public void onCancelled(DatabaseError databaseError) {

                 }
        };
        if(mAuth.getCurrentUser()==null)
        {
            Intent ia = new Intent(this,Login.class);
            startActivityForResult(ia,500);

//            finish();
        }
        else
        {
            Log.d("find","a");
            pd.show();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    uid = mAuth.getCurrentUser().getUid();
                }
            });

            mDatabase.child("users").child(uid).addValueEventListener(curuser_listener);
        }



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 400) {
            try {
                if (data != null) {
                    final Uri imageUri = data.getData();
                selected_image = null;
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                ((ImageView) vi.findViewById(R.id.imageView3)).setImageBitmap(selectedImage);
                selected_image = selectedImage;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
            }

        }else if(requestCode==500) {
            this.recreate();
        }
    }

    private void storeImageToFirebase(Bitmap imagefile)
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        // Bitmap bitmap = BitmapFactory.decodeFile(imagefile, options);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imagefile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bytes = baos.toByteArray();
        String base64Image = Base64.encodeToString(bytes, Base64.DEFAULT);
        UploadTask uploadTask = fs.child("inclass09").child("trips").child(trip.unique).putBytes(bytes);
        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful())
                {
                    trip.image_url=fs.child("inclass09").child("trips").child(trip.unique).toString();
                    if(trip.notnull())
                    {
                        mDatabase.child("trips").child(trip.unique).setValue(trip);

                    }
                    else
                        Toast.makeText(MainActivity.this,"all fields are manadatory",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    void match_views()
    {
        t = (Toolbar) findViewById(R.id.tool_bar);
        search = (ImageView) findViewById(R.id.imageView);
        new_request = (ImageView) findViewById(R.id.imageView12);
        my_trips = (TextView) findViewById(R.id.textView4_f);
        discover = (TextView) findViewById(R.id.textView5_f);
        fb = (FloatingActionButton) findViewById(R.id.floatingActionButton4);
        find_frnds = (EditText) findViewById(R.id.editText_email);
        rv1 = (RecyclerView) findViewById(R.id.rv1);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main,menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.friends:
                Intent ib = new Intent(MainActivity.this,Friends_activity.class);
                ib.putExtra("search","normal");
                startActivity(ib);
                break;
            case R.id.manage:
                Intent im = new Intent(MainActivity.this,user_data_manage.class);
                startActivity(im);
                break;
            case R.id.logout:
                mAuth.signOut();
                Log.d("find","sign");
                this.recreate();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void trip_adder(String unique) {
        mDatabase.child("trips").child(unique).child("members").child(uid).setValue(uid);
        Toast.makeText(this,"You have joined the trip!",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void trip_delete(String unique) {
        mDatabase.child("trips").child(unique).child("members").child(uid).removeValue();
        Toast.makeText(this,"You have Left the trip!",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void chat_open(String unique) {

                Intent ic = new Intent(this, Chat_trip_activity.class);
        ic.putExtra("trip_id",unique);
        startActivity(ic);
    }

    @Override
    public void add_ppl_chat(String unique) {

    }

    @Override
    public void frnds_to_add_list(final Trip trip3) {

       if( cur_user.relationship.containsValue(0))
       {
           final ArrayList<String> ppl_add_uid = new ArrayList<>();
           ArrayList<String> ppl_add_name = new ArrayList<>();

           for (String user_uid : cur_user.relationship.keySet()) {
               if (cur_user.relationship.get(user_uid) == 0&&!trip3.members.containsKey(user_uid))
               {

                         ppl_add_uid.add(user_uid);
                   ppl_add_name.add(all_user_map.get(user_uid).first_name);

               }
           }
        if(ppl_add_uid.size()!=0){
            String[] user_names =   ppl_add_name.toArray(new String[0]);
            new AlertDialog.Builder(this)
                    .setTitle("Select friends to add")
                    .setItems(user_names, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mDatabase.child("trips").child(trip3.unique).child("members").child(ppl_add_uid.get(which)).setValue(ppl_add_uid.get(which));
                        }
                    }).create().show();
            }
            else
            Toast.makeText(this,"All your friends are already members of this trip",Toast.LENGTH_SHORT).show();
       }
       else
           Toast.makeText(this,"You have no friends!",Toast.LENGTH_SHORT).show();

    }
}
