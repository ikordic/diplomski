package com.kordic.ivan.poorstudentscookbook;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

public class LoginActivity extends AppCompatActivity
{
    //FirebaseAuth instance
    private FirebaseAuth userAuth;

    private EditText editTextLoginEmail;
    private EditText editTextLoginPassword;
    private Button buttonLogIn;
    private Button buttonLoginToRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Keep the keyboard down
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //FirebaseAuth initialization
        userAuth = FirebaseAuth.getInstance();

        this.editTextLoginEmail = findViewById(R.id.editTextLoginEmail);
        this.editTextLoginPassword = findViewById(R.id.editTextLoginPassword);
        this.buttonLogIn = findViewById(R.id.buttonLogIn);
        this.buttonLoginToRegister = findViewById(R.id.buttonLoginToRegister);

        //Log in the user
        buttonLogIn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String email = editTextLoginEmail.getText().toString();
                String password = editTextLoginPassword.getText().toString();

                //Hide keyboard after click on buttonLogin
                UIUtil.hideKeyboard(LoginActivity.this);

                //Crash prevention if fields are null
                if (email.isEmpty() || password.isEmpty())
                {
                    Toast.makeText(LoginActivity.this, "Fill all fields.", Toast.LENGTH_SHORT).show();
                    return;
                }
                //Log in FirebaseAuth process
                userAuth.signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener(LoginActivity.this, new OnSuccessListener<AuthResult>()
                        {
                            @Override
                            public void onSuccess(AuthResult authResult)
                            {
                                editTextLoginEmail.setText("");
                                editTextLoginPassword.setText("");
                                Toast.makeText(LoginActivity.this, "Sign in successful!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginActivity.this, RecipeCardViewActivity.class));
                            }
                        })
                        .addOnFailureListener(LoginActivity.this, new OnFailureListener()
                        {
                            @Override
                            public void onFailure(@NonNull Exception e)
                            {
                                Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        //Switch to RegisterActivity
        buttonLoginToRegister.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }
}
