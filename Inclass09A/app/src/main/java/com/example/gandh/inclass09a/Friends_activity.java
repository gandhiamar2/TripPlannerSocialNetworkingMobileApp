package com.example.gandh.inclass09a;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class Friends_activity extends AppCompatActivity implements  Adaptor_recycler_friends.fromadaptor{

    Toolbar t;
    Adaptor_recycler_friends adaptor_recycler_friends;
    RecyclerView rv1;
    TextView all, friends, pending;
    FloatingActionButton fb;
    EditText find_frnds;
    Boolean first_check=true;
    ImageView search;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    User user;
    private  String uid;
    View vi;
    String search_word;
    int factor=0,sfatcor=0;
    Trip trip;
    DatabaseReference mDatabase;
    Bitmap selected_image;
    LayoutInflater inflater;
    StorageReference fs;
    ArrayList<User> user_list, friend_list,pending_list, idk_list,search_list ;
    Boolean discover_b = false;
    ValueEventListener users_listener, curuser_listener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_activity);
        mAuth = FirebaseAuth.getInstance();
        match_views();
        user_list = new ArrayList<>();
        fs = FirebaseStorage.getInstance().getReference();
        setSupportActionBar(t);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        rv1.setLayoutManager(lm);
        if(mAuth.getCurrentUser()==null)
        {
            Intent ia = new Intent(this,Login.class);
            startActivityForResult(ia,200);
        }
        else
        {
            uid = mAuth.getCurrentUser().getUid();
        }
        mDatabase = FirebaseDatabase.getInstance().getReference().child("inclass09");
        search_word= getIntent().getExtras().getString("search");
        if(search_word.equals("requestwatcher"))
            factor=2;

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search_the_list(find_frnds.getText().toString());
            }
        });

        friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                factor=1;
                sfatcor=0;
                adaptor_setter("");
            }
        });
        pending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                factor=2;
                sfatcor=0;
                adaptor_setter("");
            }
        });
        all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                factor=0;
                sfatcor=0;
                adaptor_setter("");
            }
        });

    }
    void search_the_list(String search_word)
    {
        search_list = new ArrayList<>();
        for (User useer: user_list ) {
            if (useer.email.contains(search_word.toLowerCase())||useer.first_name.toLowerCase().contains(search_word.toLowerCase())||useer.last_name.toLowerCase().contains(search_word.toLowerCase())
                    ||useer.email.equalsIgnoreCase(search_word)||useer.first_name.equalsIgnoreCase(search_word)||useer.last_name.equalsIgnoreCase(search_word))
                search_list.add(useer);
        }
        if(search_list.size()!=0) {
            factor = 0;
            sfatcor = 1;
            adaptor_setter("");
            Toast.makeText(this,search_list.size()+" results found",Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(this,"No results found",Toast.LENGTH_SHORT).show();
            adaptor_setter("");
        }

    }
    void match_views()
    {
        t = (Toolbar) findViewById(R.id.tool_bar);
        search = (ImageView) findViewById(R.id.imageView_f);
        all = (TextView) findViewById(R.id.textView4_f);
        friends = (TextView) findViewById(R.id.textView5_f);
        pending = (TextView) findViewById(R.id.textView3_f);
        find_frnds = (EditText) findViewById(R.id.editText_email);
        rv1 = (RecyclerView) findViewById(R.id.rv1_f);

    }


    @Override
    protected void onStart() {
        super.onStart();


        curuser_listener= new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mDatabase.child("users").child(uid).addValueEventListener(curuser_listener);

        users_listener=new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user_list  = new ArrayList<>();
                friend_list  = new ArrayList<>();
                pending_list = new ArrayList<>();
                idk_list = new ArrayList<User>();
                for (DataSnapshot snapshot  :
                        dataSnapshot.getChildren()  ) {
                    User user1;
                    user1 = snapshot.getValue(User.class);
                    user_list.add(user1);
                    if (user.relationship.containsKey(user1.uuid)) {
                        switch(user.relationship.get(user1.uuid))
                        {
                            case 0:
                                friend_list.add(user1);
                                break;
                            case -1:
                                pending_list.add(user1);
                                break;
                            case 1:
                                idk_list.add(user1);
                                break;
                        }
                    }
                    else if(uid.equals(user1.uuid))
                        user_list.remove(user1);
                }

                search_word= getIntent().getExtras().getString("search");
                if(!search_word.equals("normal")&&search_word!=null&&!search_word.equals("")&&first_check&&!search_word.equals("requestwatcher"))
                {
                    search_the_list(search_word);
                    first_check = false;
                }
                adaptor_setter("");

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mDatabase.child("users").addValueEventListener(users_listener);



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main,menu);
        menu.removeItem(R.id.friends);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.manage:
                Intent i = new Intent(Friends_activity.this,user_data_manage.class);
                startActivity(i);
                break;
            case R.id.logout:
                mAuth.signOut();
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void add_friend(User user1) {
        Toast.makeText(this,"you have sent friend request to "+user1.first_name,Toast.LENGTH_SHORT).show();
        mDatabase.child("users").child(uid).child("relationship").child(user1.uuid).setValue(1);
        mDatabase.child("users").child(user1.uuid).child("relationship").child(uid).setValue(-1);
    }

    @Override
    public void remove_friend(User user1) {
        mDatabase.child("users").child(uid).child("relationship").child(user1.uuid).removeValue();
        mDatabase.child("users").child(user1.uuid).child("relationship").child(uid).removeValue();
        Toast.makeText(this,"you and "+user1.first_name+"are no longer friends",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void respond_friend(User user1) {
        mDatabase.child("users").child(uid).child("relationship").child(user1.uuid).setValue(0);
        mDatabase.child("users").child(user1.uuid).child("relationship").child(uid).setValue(0);
        Toast.makeText(this,"you are now a friend to "+user1.first_name,Toast.LENGTH_SHORT).show();
    }

    void adaptor_setter(String s)
    {
        if(factor ==0)
        {
            all.setBackgroundColor(Color.parseColor("#2196f3"));
            friends.setBackgroundColor(Color.parseColor("#ffffff"));
            pending.setBackgroundColor(Color.parseColor("#ffffff"));
            if(sfatcor==0)
                adaptor_recycler_friends = new Adaptor_recycler_friends(this,user_list,this,user);
            else if(sfatcor==1)
                adaptor_recycler_friends = new Adaptor_recycler_friends(this,search_list,this,user);
        }
        else if(factor ==1)
        {
            friends.setBackgroundColor(Color.parseColor("#2196f3"));
            all.setBackgroundColor(Color.parseColor("#ffffff"));
            pending.setBackgroundColor(Color.parseColor("#ffffff"));
            adaptor_recycler_friends = new Adaptor_recycler_friends(this,friend_list,this,user);
        }
        else if(factor==2)
        {
            pending.setBackgroundColor(Color.parseColor("#2196f3"));
            friends.setBackgroundColor(Color.parseColor("#ffffff"));
            all.setBackgroundColor(Color.parseColor("#ffffff"));
            adaptor_recycler_friends = new Adaptor_recycler_friends(this,pending_list,this,user);
        }
       rv1.setAdapter(adaptor_recycler_friends);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK)
        {
            mDatabase.child("users").child(uid).removeEventListener(curuser_listener);
            mDatabase.child("users").removeEventListener(users_listener);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
