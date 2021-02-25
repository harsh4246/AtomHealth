package com.example.health;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.health.models.User;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    private EditText userNameText;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        init();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void init() {
        Intent currIntent = getIntent();
        final String idToken = currIntent.getStringExtra("idToken");
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        Button continueButton = findViewById(R.id.button_register_continue);
        ImageButton backButton = findViewById(R.id.button_register_back);
        userNameText = findViewById(R.id.editText_register_username);
        continueButton.setOnClickListener(v -> {
            if (validate()){
                firebaseAuthWithGoogle(idToken);
                Toast.makeText(RegisterActivity.this,"Please wait as we log you in",Toast.LENGTH_LONG).show();
            }
        });
        backButton.setOnClickListener(v -> backToLogin());
    }

    private void backToLogin() {
        Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean validate() {
        String name = userNameText.getText().toString();
        if(name.isEmpty()){
            userNameText.setError("Please provide your name");
            return false;
        }else if (name.length()>20) {
            userNameText.setError("Your name should be less than 20 characters");
            return false;
        }
        else if (!name.matches("^[a-zA-Z\\s]*$")){
            userNameText.setError("Please use letters only");
            return false;
        }
        return true;
    }

    public void writeNewUser(String userId, String name, String email) {
        User user = new User(name, email);
        mDatabase.child("users").child(userId).setValue(user);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateDatabase(user);
                        updateUi();
                    } else {
                        Toast.makeText(RegisterActivity.this,"Unable to login with google",Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUi() {
        Intent intent = new Intent(RegisterActivity.this,HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void updateDatabase(FirebaseUser user) {
        writeNewUser(user.getUid(),userNameText.getText().toString(),user.getEmail());
    }
}



