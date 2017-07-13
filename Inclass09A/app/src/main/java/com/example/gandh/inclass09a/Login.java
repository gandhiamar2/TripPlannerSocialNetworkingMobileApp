package com.example.gandh.inclass09a;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener{
    ImageView iv;
    RelativeLayout rl;
    GoogleSignInOptions gso;
    GoogleApiClient mGoogleApiClient;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    DatabaseReference mDatabse;
    TextView email_fixed;
    LinearLayout ll;
    EditText et;
    EditText email,password;
    Button sign_up_pass,sign_in;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        iv = (ImageView) findViewById(R.id.sign_up);
        email_fixed = (TextView) findViewById(R.id.email_fixed);
        email_fixed.setVisibility(View.INVISIBLE);
        email = (EditText) findViewById(R.id.editText_email);
        password = (EditText) findViewById(R.id.editText2);
        mAuth = FirebaseAuth.getInstance();
        sign_up_pass = (Button) findViewById(R.id.set_pass);
        sign_up_pass.setVisibility(View.INVISIBLE);
        sign_in = (Button) findViewById(R.id.button_signin);
        mDatabse = FirebaseDatabase.getInstance().getReference().child("inclass09");

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, 100);
            }
        });
        sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!email.getText().toString().equals("") && !password.getText().toString().equals(""))
                log_up();
                else
                    Toast.makeText(Login.this,"Invalid cred",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                final GoogleSignInAccount account = result.getSignInAccount();
               final AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                mAuth.signInWithCredential(credential).addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {

                            mDatabse.child("users").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists())
                                    {
                                        mAuth.signOut();
                                        Toast.makeText(Login.this,"Already signed up please login",Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                    {
                                        et = new EditText(Login.this);
                                        ll = new LinearLayout(Login.this);
                                        ll.addView(et);
                                        new AlertDialog.Builder(Login.this)
                                                .setView(ll)
                                                .setPositiveButton("set password", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                      mAuth.getCurrentUser().updatePassword(et.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                          @Override
                                                          public void onComplete(@NonNull Task<Void> task) {
                                                              if(task.isSuccessful())
                                                              {
                                                                  Log.d("demo","sucess");
                                                                  User user = new User(account.getDisplayName(),account.getFamilyName(),account.getEmail().replace(".",","));
                                                                  user.uuid = mAuth.getCurrentUser().getUid();
                                                                  mDatabse.child("users").child(mAuth.getCurrentUser().getUid()).setValue(user);
                                                                  Intent manage = new Intent(Login.this,user_data_manage.class);
                                                                  startActivity(manage);
                                                                  finish();
                                                              }
                                                              else
                                                              {
                                                                  mAuth.getCurrentUser().delete();
                                                                  Log.d("demo","failed"+task.getException());
                                                              }
                                                          }
                                                      });

                                                    }
                                                }).setCancelable(false)
                                                .create().show();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }
                    }
                });


            } else {

            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    void log_up()
    {

                mAuth.signInWithEmailAndPassword(email.getText().toString(),password.getText().toString())
                        .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()) {
                                    Log.d("demo", mAuth.getCurrentUser().getUid() + mAuth.getCurrentUser().getUid());
//                                    Intent manage = new Intent(Login.this, user_data_manage.class);
//                                    startActivity(manage);
//                                    Intent i = new Intent(Login.this,MainActivity.class);
//                                    startActivity(i);
                                    finish();
                                }
                                else
                                {
                                    try {
                                        throw task.getException();
                                    } catch(FirebaseAuthWeakPasswordException e) {

                                    } catch(FirebaseAuthInvalidCredentialsException e) {
                                        Toast.makeText(Login.this,"Invalid cred",Toast.LENGTH_SHORT).show();

                                    } catch(FirebaseAuthUserCollisionException e) {

                                    } catch(Exception e) {

                                    }
                                }

                            }
                        });
            }

}
