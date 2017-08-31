package com.example.gandh.inclass09a;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlacePicker;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Chat_trip_activity extends AppCompatActivity implements  Adaptor_recycler_chat.fromadaptor{

    TextView tv;
    ArrayList<Chat> chat_list;
    RecyclerView rcl1;
    EditText input;
    DatabaseReference mDatabase;
    StorageReference str;
    Chat chat;
    TextView title;
    ImageView ib1,ib2,ib3,ib4,ib5,add,add_place;
    Adaptor_recycler_chat adaptor_recycler_chat;
    private FirebaseAuth mAuth;
    String uid, cur_name,trip_id;
    Bitmap selected_image;
    User cur_user;
    SimpleDateFormat sdf;
    Trip trip;
    StorageReference fs;
     HashMap<String, User> all_user_map;
    ValueEventListener trip_listener, curuser_listener, users_listener;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar t;
        setContentView(R.layout.activity_chat_trip_activity);

        mAuth = FirebaseAuth.getInstance();
        t = (Toolbar) findViewById(R.id.tool_bar);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("inclass09");
        str = FirebaseStorage.getInstance().getReference();
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        input = (EditText) findViewById(R.id.editText3);
        ib1= (ImageView) findViewById(R.id.imageView7);
        ib5= (ImageView) findViewById(R.id.imageView9);
        add = (ImageView) findViewById(R.id.imageView11);
        ib4= (ImageView) findViewById(R.id.imageView6);
        ib2= (ImageView) findViewById(R.id.imageButton2);
        ib3= (ImageView) findViewById(R.id.imageButton3);
        add_place= (ImageView) findViewById(R.id.imageView16);
        rcl1 = (RecyclerView) findViewById(R.id.rc1);
        title =(TextView) findViewById(R.id.textView);
        uid =mAuth.getCurrentUser().getUid();
        ib4.setVisibility(View.INVISIBLE);
        cur_name = mAuth.getCurrentUser().getDisplayName();
        trip_id = getIntent().getExtras().getString("trip_id");
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        rcl1.setLayoutManager(lm);
        pd = new ProgressDialog(this);
        setSupportActionBar(t);
        ib3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 chat = new Chat();
                String unique= UUID.randomUUID().toString();
                chat.unique=unique;
                chat.message=input.getText().toString();
                chat.post_username = cur_name;
                chat.post_uid = uid;
                chat.image=false;

                chat.time =(sdf.format(new Date()));
                trip.chat_list.put(chat.unique,chat);
                mDatabase.child("trips").child(trip_id).child("chat_list").setValue(trip.chat_list);
                ib4.setVisibility(View.INVISIBLE);
                input.setText("");
                for (String s :
                        trip.trip_closed_userlist.keySet()) {
                    mDatabase.child("trips").child(trip_id).child("chat_list").child(chat.unique).child("personalversion").child(s).setValue(s);
                }


            }
        });
        ib2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chat = new Chat();
                String unique= UUID.randomUUID().toString();
                chat.unique=unique;
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, 500);

                ib3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        if(selected_image==null)
                        {
                            chat = new Chat();
                            String unique= UUID.randomUUID().toString();
                            chat.unique = unique;
                            chat.message=input.getText().toString();
                            chat.post_username = cur_name;
                            chat.post_uid = uid;
                            chat.image=false;
                            chat.time =(sdf.format(new Date()));
                            trip.chat_list.put(chat.unique,chat);
                            mDatabase.child("trips").child(trip_id).child("chat_list").setValue(trip.chat_list);
                            ib4.setVisibility(View.INVISIBLE);
                            input.setText("");
                        }
                        else {
                            chat.message=input.getText().toString();
                            chat.post_username = cur_name;
                            chat.post_uid = uid;
                            chat.image=true;
                            chat.time =(sdf.format(new Date()));
                            storeImageToFirebase(selected_image);
                            selected_image=null;
                        }



                    }
                });


            }
        });

        ib1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(trip.owner_uid.equals(uid)) {
                    trip.closed = true;
                    mDatabase.child("trips").child(trip_id).child("closed").setValue(true);
                    Toast.makeText(Chat_trip_activity.this,"This Trip chat is closed for all now",Toast.LENGTH_SHORT).show();
                }
                else {
                    trip.trip_closed_userlist.put(uid, uid);
                    mDatabase.child("trips").child(trip_id).child("trip_closed_userlist").setValue(trip.trip_closed_userlist);
                    Toast.makeText(Chat_trip_activity.this,"You are no longer part of this chat group",Toast.LENGTH_SHORT).show();
                }
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                frnds_to_add_list();
            }
        });

        add_place.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .build(Chat_trip_activity.this);
                    startActivityForResult(intent, 600);
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }

            }
        });

        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(trip.trip_place_list.size()>0) {
                    Intent ic = new Intent(Chat_trip_activity.this, Chat_trip_places.class);
                    ic.putExtra("trip_id", trip.unique);
                    if(trip.closed||trip.trip_closed_userlist.containsKey(uid))
                    {
                       ic.putExtra("trip_on",false);
                    }
                    else
                        ic.putExtra("trip_on",true);
                        startActivity(ic);
                }
                else
                    Toast.makeText(Chat_trip_activity.this,"no places are added",Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 500) {
            try {
                if(data!=null) {
                    final Uri imageUri = data.getData();
                    selected_image = null;
                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    ib4.setImageBitmap(selectedImage);
                    ib4.setVisibility(View.VISIBLE);
                    selected_image = selectedImage;
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
            }

        }else if(requestCode==600&& resultCode == Activity.RESULT_OK)
        {
            final Place place = PlacePicker.getPlace(data, this);

            final CharSequence name = place.getName();
            final CharSequence address = place.getAddress();
            place.toString();

            mDatabase.child("trips").child(trip_id).child("trip_place_list").child(name+"").setValue(place.getLatLng().toString());
        }
        else {
            Toast.makeText(this, "You haven't picked Image",Toast.LENGTH_LONG).show();
        }
    }

    private void storeImageToFirebase(Bitmap imagefile) {
        pd.show();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        // Bitmap bitmap = BitmapFactory.decodeFile(imagefile, options);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imagefile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bytes = baos.toByteArray();
        String base64Image = Base64.encodeToString(bytes, Base64.DEFAULT);
        UploadTask uploadTask = str.child("inclass09").child("trips").child("chat").child(chat.unique).putBytes(bytes);
        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful())
                {
                   chat.image_url =  str.child("inclass09").child("trips").child("chat").child(chat.unique).toString();
                    trip.chat_list.put(chat.unique,chat);
                    mDatabase.child("trips").child(trip_id).child("chat_list").setValue(trip.chat_list);
                    ib4.setVisibility(View.INVISIBLE);
                    input.setText("");
                    pd.dismiss();

                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();


                trip_listener =new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               trip = dataSnapshot.getValue(Trip.class);
              //  Map<String,Chat> trip_chat_lisr_temp =  trip.chat_list;
                ArrayList<String> trip_chat_list_temp = new ArrayList<String>(trip.chat_list.keySet());
                for (String s : trip_chat_list_temp) {

                    if(trip.chat_list.get(s).personalversion.containsKey(uid))
                        trip.chat_list.remove(s);

                }
                List<Map.Entry<String,Chat>> entries = new LinkedList<Map.Entry<String,Chat>>(trip.chat_list.entrySet());
                Collections.sort(entries, new Comparator<Map.Entry<String,Chat>>() {

                    @Override
                    public int compare(Map.Entry<String, Chat> o1, Map.Entry<String, Chat> o2) {
                        Date date1 = null;
                        Date date2 =null;
                        try {
                            date1 = sdf.parse(o1.getValue().time);
                             date2 = sdf.parse(o2.getValue().time);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }


                        return date1.compareTo(date2) <0 ? -1
                                : date1.compareTo(date2)>0? 1   : 0;
                    }
                });
                trip.chat_list.clear();
                Map<String,Chat> sortedMap = new LinkedHashMap<>();
                for(Map.Entry<String,Chat> entry: entries){
                    sortedMap.put(entry.getKey(), entry.getValue());
                }
                trip.chat_list = sortedMap;
                fs = FirebaseStorage.getInstance().getReferenceFromUrl(trip.image_url);
                Glide.with(Chat_trip_activity.this )
                        .using(new FirebaseImageLoader())
                        .load(fs)
                        .into(ib5);
                adaptor_recycler_chat = new Adaptor_recycler_chat(Chat_trip_activity.this,trip.chat_list,Chat_trip_activity.this,false,uid);
                rcl1.setAdapter(adaptor_recycler_chat);

                title.setText(trip.title+" at "+trip.place);

                if(trip.closed||trip.trip_closed_userlist.containsKey(uid))
                {
                    ib4.setEnabled(false);
                    ib2.setEnabled(false);
                    ib3.setEnabled(false);
                    input.setEnabled(false);
                    add_place.setEnabled(false);
                }
                else
                {
                    ib4.setEnabled(true);
                    ib2.setEnabled(true);
                    ib3.setEnabled(true);
                    input.setEnabled(true);
                    add_place.setEnabled(true);

                }

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };



                curuser_listener=new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                cur_user = dataSnapshot.getValue(User.class);
                cur_name= dataSnapshot.getValue(User.class).first_name;
                mDatabase.child("trips").child(trip_id).addValueEventListener(trip_listener);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mDatabase.child("users").child(uid).addValueEventListener(curuser_listener);

                users_listener=new ValueEventListener() {

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
        mDatabase.child("users").addListenerForSingleValueEvent(users_listener);
    }

    @Override
    public void chat_delete_personal(String unique) {
        Chat c = new Chat();
        c.personalversion.put(uid,uid);
        mDatabase.child("trips").child(trip_id).child("chat_list").child(unique).child("personalversion").setValue(c.personalversion);
    }

    public void frnds_to_add_list() {

        if( cur_user.relationship.containsValue(0))
        {
            final ArrayList<String> ppl_add_uid = new ArrayList<>();
            ArrayList<String> ppl_add_name = new ArrayList<>();

            for (String user_uid : cur_user.relationship.keySet()) {
                if (cur_user.relationship.get(user_uid) == 0&&!trip.members.containsKey(user_uid))
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
                                mDatabase.child("trips").child(trip.unique).child("members").child(ppl_add_uid.get(which)).setValue(ppl_add_uid.get(which));
                            }
                        }).create().show();
            }
            else
                Toast.makeText(this,"All your friends are already members of this trip",Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(this,"You have no friends!",Toast.LENGTH_SHORT).show();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            mDatabase.child("trips").child(trip_id).removeEventListener(trip_listener);
            mDatabase.child("users").child(uid).removeEventListener(curuser_listener);


            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
