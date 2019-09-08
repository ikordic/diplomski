package com.kordic.ivan.poorstudentscookbook;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.kordic.ivan.poorstudentscookbook.Model.User;
import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;
import java.util.Objects;

//12:50 - 15:10 = 140min
//15:30 - 21:50 = 380min

public class RegisterActivity extends AppCompatActivity
{
    //FirebaseAuth instance and init
    private FirebaseAuth userAuth = FirebaseAuth.getInstance();


    //Firestore instance for username and id storage
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference userRef = db.collection("User");

    private EditText editTextRegisterEmail;
    private EditText editTextRegisterUsername;
    private EditText editTextRegisterPassword;
    private EditText editTextRegisterConfirmPassword;
    private ImageView imageViewPasswordMatch;
    private Button buttonRegister;

    public Boolean usernameExists = false;
    public String deafultUserImage = "https://firebasestorage.googleapis.com/v0/b/poorstudentscookbook-f9e8b.appspot.com/o/user%2Fdefault_user_profile_image.png?alt=media&token=c251ad47-1185-414d-a859-80013aff237c";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Keep the keyboard down
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        this.editTextRegisterEmail = findViewById(R.id.editTextRegisterEmail);
        this.editTextRegisterUsername = findViewById(R.id.editTextRegisterUsername);
        this.editTextRegisterPassword = findViewById(R.id.editTextRegisterPassword);
        this.editTextRegisterConfirmPassword = findViewById(R.id.editTextRegisterConfirmPassword);
        this.imageViewPasswordMatch = findViewById(R.id.imageViewPasswordMatch);
        this.buttonRegister = findViewById(R.id.buttonRegister);

        //Password and confirm password matching
        editTextRegisterPassword.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
            }

            @Override
            public void afterTextChanged(Editable editable)
            {
                String password = editTextRegisterPassword.getText().toString();
                String confirmPassword = editTextRegisterConfirmPassword.getText().toString();
                if (confirmPassword.length() > 0)
                {
                    if (password.equals(confirmPassword) && password.length() > 5)
                    {
                        Glide.with(RegisterActivity.this).load(R.drawable.correct).into(imageViewPasswordMatch);
                    }
                    else
                    {
                        Glide.with(RegisterActivity.this).load(R.drawable.incorrect).into(imageViewPasswordMatch);
                    }
                }
                else
                {
                    Glide.with(RegisterActivity.this).load("").into(imageViewPasswordMatch);
                }
            }
        });

        editTextRegisterConfirmPassword.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
            }

            @Override
            public void afterTextChanged(Editable editable)
            {
                String password = editTextRegisterPassword.getText().toString();
                String confirmPassword = editTextRegisterConfirmPassword.getText().toString();
                if (password.length() > 0)
                {
                    if (password.equals(confirmPassword) && confirmPassword.length() > 5)
                    {
                        Glide.with(RegisterActivity.this).load(R.drawable.correct).into(imageViewPasswordMatch);
                    }
                    else
                    {
                        Glide.with(RegisterActivity.this).load(R.drawable.incorrect).into(imageViewPasswordMatch);
                    }
                }
                else
                {
                    Glide.with(RegisterActivity.this).load("").into(imageViewPasswordMatch);
                }
            }
        });

        //Registration button
        buttonRegister.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                final String email = editTextRegisterEmail.getText().toString();
                final String username = editTextRegisterUsername.getText().toString();
                final String password = editTextRegisterPassword.getText().toString();
                String confirmPassword = editTextRegisterConfirmPassword.getText().toString();

                //Bool value for username check
                usernameExists = false;

                //Hide keyboard after click on buttonRegister
                UIUtil.hideKeyboard(RegisterActivity.this);

                //Crash prevention if fields are null and error indication
                if (email.trim().isEmpty() || username.trim().isEmpty() || password.trim().isEmpty() || confirmPassword.trim().isEmpty())
                {
                    if (email.trim().isEmpty())
                    {
                        Toast.makeText(RegisterActivity.this, "Please your e-mail address.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (username.trim().isEmpty())
                    {
                        Toast.makeText(RegisterActivity.this, "Please enter your username.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (password.trim().isEmpty())
                    {
                        Toast.makeText(RegisterActivity.this, "Please enter your password.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (confirmPassword.trim().isEmpty())
                    {
                        Toast.makeText(RegisterActivity.this, "Please confirm your password.", Toast.LENGTH_LONG).show();
                    }
                }
                else
                {
                    //Query username to check if it already exists
                    Query query = userRef.whereEqualTo("userUsername", username);
                    query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task)
                        {
                            if (task.isSuccessful())
                            {
                                for (DocumentSnapshot documentSnapshot : Objects.requireNonNull(task.getResult()))
                                {
                                    String user = documentSnapshot.getString("userUsername");

                                    assert user != null;
                                    if (user.equals(username))
                                    {
                                        usernameExists = true;
                                        Toast.makeText(RegisterActivity.this, "Username already exists", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }
                                createUser(email, password, username);
                            }
                        }
                    });
                }
            }
        });
    }

    //Create new user with email and password
    public void createUser(String email, String password, final String username)
    {
        if (!usernameExists)
        {
            userAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if (task.isSuccessful())
                            {
                                Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();

                                userRef.document(Objects.requireNonNull(userAuth.getUid())).set(new User(userAuth.getUid(), Objects.requireNonNull(userAuth.getCurrentUser()).getEmail(), username, deafultUserImage))
                                        .addOnSuccessListener(new OnSuccessListener<Void>()
                                        {
                                            @Override
                                            public void onSuccess(Void aVoid)
                                            {
                                                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                                finish();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener()
                                        {
                                            @Override
                                            public void onFailure(@NonNull Exception e)
                                            {
                                                Toast.makeText(RegisterActivity.this, "Failed to add to database!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                            else
                            {
                                Toast.makeText(RegisterActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}
