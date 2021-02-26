package com.example.health;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 7;
    GoogleSignInClient mGoogleSignInClient;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
    }

    private void init() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(LoginActivity.this, gso);
        mAuth = FirebaseAuth.getInstance();
        Button googleButton = findViewById(R.id.button_login_google);
        Button guestButton = findViewById(R.id.button_login_guest);
        googleButton.setOnClickListener(v -> loginWithGoogle());
        guestButton.setOnClickListener(v -> loginAsGuest());
    }

    private void loginAsGuest() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void loginWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                assert account != null;
                checkUserExists(account.getEmail(),account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(LoginActivity.this,"Google sign in failed",Toast.LENGTH_SHORT).show();
                Log.d("error",e.toString());
            }
        }
    }

    private void checkUserExists(String email, String idToken) {
        mDatabase.child("users").orderByChild("email").equalTo(email).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                goToRegistration(idToken);
            }
            else {
                if(task.getResult().getValue()==null){
                    goToRegistration(idToken);
                }else{
                    firebaseAuthWithGoogle(idToken);
                }
            }
        });
    }

    private void goToRegistration(String idToken) {
        Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
        intent.putExtra("idToken",idToken);
        startActivity(intent);
        finish();
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        assert user != null;
                        updateUi();
                    } else {
                        Toast.makeText(LoginActivity.this,"Unable to login with google",Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUi() {
        Intent intent = new Intent(LoginActivity.this,HomeActivity.class);
        startActivity(intent);
        finish();
    }

}