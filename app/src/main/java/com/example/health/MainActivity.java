package com.example.health;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser currentUser) {
        Intent intent;
        if(currentUser==null) {
            intent = new Intent(MainActivity.this,LoginActivity.class);
        }else{
            intent = new Intent(MainActivity.this,HomeActivity.class);
        }
        startActivity(intent);
        finish();
    }
}