package com.example.gandh.inclass09a;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import com.bumptech.glide.Glide;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.signature.StringSignature;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.UUID;

import static com.example.gandh.inclass09a.R.id.imageView;

public class user_data_manage extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthlist;
    DatabaseReference mDatabse;
    FirebaseUser cur_user;
    User user;
    ValueEventListener user_listener;
    TextView fn,ln;
    Spinner sp;
    Button done;
    FloatingActionButton fb1;
    ImageView iv;
    StorageReference fs;
    Bitmap selected_image;
    LayoutInflater inflater;
    View vi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_data_manage);
        Log.d("find","e");
        inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        fn = (TextView) findViewById(R.id.fname_view);
        ln = (TextView) findViewById(R.id.lname_view);
        sp = (Spinner) findViewById(R.id.spinner);
        done = (Button) findViewById(R.id.button_done);
        fb1 = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        iv = (ImageView) findViewById(R.id.profile_pic);
        sp.setEnabled(false);
        mAuth = FirebaseAuth.getInstance();
        fs = FirebaseStorage.getInstance().getReference();



        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabse.removeEventListener(user_listener);
                mAuth.removeAuthStateListener(mAuthlist);
                finish();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 300) {
            try {
                if(data!=null) {
                    final Uri imageUri = data.getData();
                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    ((ImageView) vi.findViewById(R.id.imageView3)).setImageBitmap(selectedImage);
                    selected_image = selectedImage;

                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
            }

        }
    }

    private void storeImageToFirebase(Bitmap imagefile)
    {
       final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Updating data");
        pd.show();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imagefile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bytes = baos.toByteArray();
        String base64Image = Base64.encodeToString(bytes, Base64.DEFAULT);
        UploadTask uploadTask = fs.child("inclass09").child("user_data").child(mAuth.getCurrentUser().getUid()).putBytes(bytes);
        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//                if(task.isSuccessful())
                user.image_url = fs.child("inclass09").child("user_data").child(mAuth.getCurrentUser().getUid()).toString();
                mDatabse.setValue(user).addOnCompleteListener(user_data_manage.this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        pd.dismiss();
                        mDatabse.removeEventListener(user_listener);
                        mAuth.removeAuthStateListener(mAuthlist);
                        finish();
                    }
                });
            }
        });

    }
    @Override
    protected void onStart() {
        super.onStart();

        mAuthlist= new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (mAuth.getCurrentUser() != null) {
                    cur_user = mAuth.getCurrentUser();
                    mDatabse = FirebaseDatabase.getInstance().getReference().child("inclass09").child("users").child(cur_user.getUid());
                    Log.d("uid",cur_user.getUid());
                    mDatabse.addListenerForSingleValueEvent(user_listener);
                    Log.d("find","f");
                }
            }
        };

        mAuth.addAuthStateListener(mAuthlist);

        user_listener= new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user=dataSnapshot.getValue(User.class);
                Log.d("find","g");
                fn.setText(user.first_name);
                ln.setText(user.last_name);
                if(user.gender)sp.setSelection(1);
                else sp.setSelection(0);
                if(user.image_url!=null){
                    StorageReference sf1 = FirebaseStorage.getInstance().getReferenceFromUrl(user.image_url);
                    iv.setDrawingCacheEnabled(false);
                    Glide.with(user_data_manage.this)
                            .using(new FirebaseImageLoader())
                            .load(sf1)
                            .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                            .into(iv);
                    mDatabse.removeEventListener(user_listener);
                }
                mDatabse.removeEventListener(user_listener);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };


        fb1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vi = inflater.inflate(R.layout.activity_user,null);

                ((ImageView) vi.findViewById(R.id.imageView3)).setImageDrawable(iv.getDrawable());
                ((EditText) vi.findViewById(R.id.editText)).setText(user.first_name);
                ((EditText) vi.findViewById(R.id.editText3)).setText(user.last_name);
                ((Spinner) vi.findViewById(R.id.spinner2)).setSelection(user.gender?1:0);
                vi.findViewById(R.id.imageView4).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                        photoPickerIntent.setType("image/*");
                        startActivityForResult(photoPickerIntent,300);
                    }
                });
                new AlertDialog.Builder(user_data_manage.this)
                        .setView(vi)
                        .setPositiveButton("Update Profile", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                user.first_name = ((EditText) vi.findViewById(R.id.editText)).getText().toString();
                                user.last_name = ((EditText) vi.findViewById(R.id.editText3)).getText().toString();
                                user.gender = ((Spinner) vi.findViewById(R.id.spinner2)).getSelectedItemPosition()==1?true:false;
                                BitmapDrawable drawable = (BitmapDrawable) ((ImageView)vi.findViewById(R.id.imageView4)).getDrawable();


                                if(selected_image==null) {
                                    if(user.image_url==null) {
                                        selected_image = drawable.getBitmap();
                                        storeImageToFirebase(selected_image);
                                    }
                                    else
                                    {
                                        mDatabse.setValue(user).addOnCompleteListener(user_data_manage.this, new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                mDatabse.removeEventListener(user_listener);
                                                mAuth.removeAuthStateListener(mAuthlist);
                                                finish();
                                            }
                                        });
                                    }

                                }
                                else
                                storeImageToFirebase(selected_image);

                            }
                        }).create().show();
            }
        });


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK) {
            mDatabse.removeEventListener(user_listener);
            mAuth.removeAuthStateListener(mAuthlist);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
