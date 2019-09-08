package com.kordic.ivan.poorstudentscookbook;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kordic.ivan.poorstudentscookbook.Model.Recipe;
import com.kordic.ivan.poorstudentscookbook.Model.User;

import java.util.Objects;

public class UserProfileActivity extends AppCompatActivity
{

    private FirebaseAuth userAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference userStorageRef = FirebaseStorage.getInstance().getReference("user");
    private CollectionReference userRef = db.collection("User");

    private ImageView imageViewUserProfileImage;
    private TextView textViewUserProfileUsername;
    private EditText editTextUserProfileEmail;
    private EditText editTextUserProfilePassword;
    private TextView textViewUserProfileChangeEmail;
    private TextView textViewUserProfileChangePassword;
    private EditText editTextUserProfileNewPassword;
    private TextView textViewUserProfileDeleteProfile;
    private Button buttonUserProfileSaveImage;

    //Constant for image selection
    private static final int PICK_IMAGE_REQUEST = 1;

    //URI which points to image for the imageview
    private Uri recipeImageUri;

    //Used
    private Task recipeUploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        this.imageViewUserProfileImage = findViewById(R.id.imageViewUserProfileImage);
        this.textViewUserProfileUsername = findViewById(R.id.textViewUserProfileUsername);
        this.editTextUserProfileEmail = findViewById(R.id.editTextUserProfileEmail);
        this.editTextUserProfilePassword = findViewById(R.id.editTextUserProfilePassword);
        this.textViewUserProfileChangeEmail = findViewById(R.id.textViewUserProfileChangeEmail);
        this.textViewUserProfileChangePassword = findViewById(R.id.textViewUserProfileChangePassword);
        this.editTextUserProfileNewPassword = findViewById(R.id.editTextUserProfileNewPassword);
        this.textViewUserProfileDeleteProfile = findViewById(R.id.textViewUserProfileDeleteProfile);
        this.buttonUserProfileSaveImage = findViewById(R.id.buttonUserProfileSaveImage);

        userStorageRef = FirebaseStorage.getInstance().getReference("user");

        //Keep the keyboard down
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        userRef.document(Objects.requireNonNull(userAuth.getUid())).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
                {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot)
                    {
                        User user = documentSnapshot.toObject(User.class);
                        assert user != null;
                        Glide.with(UserProfileActivity.this).load(user.getUserProfileImage()).into(imageViewUserProfileImage);
                        textViewUserProfileUsername.setText(user.getUserUsername());
                        editTextUserProfileEmail.setText(Objects.requireNonNull(userAuth.getCurrentUser()).getEmail());
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast.makeText(UserProfileActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        imageViewUserProfileImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //Gets only images and the constant is used to identify the activity
                startActivityForResult(new Intent().setType("image/*").setAction(Intent.ACTION_GET_CONTENT), PICK_IMAGE_REQUEST);
            }
        });

        buttonUserProfileSaveImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                buttonUserProfileSaveImage.setClickable(false);
                if (recipeImageUri != null)
                {
                    final StorageReference fileReference = userStorageRef.child(System.currentTimeMillis()
                            + "." + getFileExtension(recipeImageUri));

                    recipeUploadTask = fileReference.putFile(recipeImageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>()
                    {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
                        {
                            if (!task.isSuccessful())
                            {
                                throw Objects.requireNonNull(task.getException());
                            }
                            return fileReference.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>()
                    {
                        @Override
                        public void onComplete(@NonNull final Task<Uri> task)
                        {
                            if (task.isSuccessful())
                            {
                                //Attach new image to user
                                userRef.document(userAuth.getUid()).update("userProfileImage", Objects.requireNonNull(task.getResult()).toString());
                                buttonUserProfileSaveImage.setClickable(true);
                            }
                            else
                            {
                                Toast.makeText(UserProfileActivity.this, "Task failed.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener()
                    {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            Toast.makeText(UserProfileActivity.this, "putFile() doesn't work", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        //Change email
        textViewUserProfileChangeEmail.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (!editTextUserProfilePassword.getText().toString().trim().isEmpty())
                {
                    AuthCredential credential = EmailAuthProvider.getCredential(userAuth.getCurrentUser().getEmail(), editTextUserProfilePassword.getText().toString());
                    userAuth.getCurrentUser().reauthenticate(credential)
                            .addOnSuccessListener(new OnSuccessListener<Void>()
                            {
                                @Override
                                public void onSuccess(Void aVoid)
                                {
                                    userAuth.getCurrentUser().updateEmail(editTextUserProfileEmail.getText().toString())
                                            .addOnSuccessListener(new OnSuccessListener<Void>()
                                            {
                                                @Override
                                                public void onSuccess(Void aVoid)
                                                {
                                                    Toast.makeText(UserProfileActivity.this, "Email updated!", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                    startActivity(new Intent(UserProfileActivity.this, UserProfileActivity.class));
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener()
                                            {
                                                @Override
                                                public void onFailure(@NonNull Exception e)
                                                {
                                                    Toast.makeText(UserProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener()
                            {
                                @Override
                                public void onFailure(@NonNull Exception e)
                                {
                                    Toast.makeText(UserProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                else
                {
                    Toast.makeText(UserProfileActivity.this, "Please enter your password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Change password
        textViewUserProfileChangePassword.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (!editTextUserProfileNewPassword.getText().toString().trim().isEmpty() && !editTextUserProfilePassword.getText().toString().trim().isEmpty())
                {
                    AuthCredential credential = EmailAuthProvider.getCredential(userAuth.getCurrentUser().getEmail(), editTextUserProfilePassword.getText().toString());
                    userAuth.getCurrentUser().reauthenticate(credential)
                            .addOnSuccessListener(new OnSuccessListener<Void>()
                            {
                                @Override
                                public void onSuccess(Void aVoid)
                                {
                                    userAuth.getCurrentUser().updatePassword(editTextUserProfileNewPassword.getText().toString())
                                            .addOnSuccessListener(new OnSuccessListener<Void>()
                                            {
                                                @Override
                                                public void onSuccess(Void aVoid)
                                                {
                                                    Toast.makeText(UserProfileActivity.this, "Password updated!", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                    startActivity(new Intent(UserProfileActivity.this, UserProfileActivity.class));
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener()
                                            {
                                                @Override
                                                public void onFailure(@NonNull Exception e)
                                                {
                                                    Toast.makeText(UserProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener()
                            {
                                @Override
                                public void onFailure(@NonNull Exception e)
                                {
                                    Toast.makeText(UserProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                else
                {
                    Toast.makeText(UserProfileActivity.this, "Please enter your old and new password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Delete profile - implement deleting all recipes and user from database
        textViewUserProfileDeleteProfile.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (!editTextUserProfilePassword.getText().toString().trim().isEmpty())
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);
                    builder.setCancelable(true);
                    builder.setTitle("Delete profile");
                    builder.setMessage("Are you sure you want to delete your profile?");
                    builder.setPositiveButton("Confirm",
                            new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i)
                                {
                                    AuthCredential credential = EmailAuthProvider.getCredential(userAuth.getCurrentUser().getEmail(), editTextUserProfilePassword.getText().toString());
                                    userAuth.getCurrentUser().reauthenticate(credential)
                                            .addOnSuccessListener(new OnSuccessListener<Void>()
                                            {
                                                @Override
                                                public void onSuccess(Void aVoid)
                                                {
                                                    userAuth.getCurrentUser().delete()
                                                            .addOnSuccessListener(new OnSuccessListener<Void>()
                                                            {
                                                                @Override
                                                                public void onSuccess(Void aVoid)
                                                                {
                                                                    Toast.makeText(UserProfileActivity.this, "Profile deleted", Toast.LENGTH_SHORT).show();
                                                                    finish();
                                                                    startActivity(new Intent(UserProfileActivity.this, RecipeCardViewActivity.class));
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener()
                                                            {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e)
                                                                {
                                                                    Toast.makeText(UserProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener()
                                            {
                                                @Override
                                                public void onFailure(@NonNull Exception e)
                                                {
                                                    Toast.makeText(UserProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            });
                    builder.setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i)
                                {
                                    Toast.makeText(UserProfileActivity.this, "Wise choice...", Toast.LENGTH_SHORT).show();
                                }
                            });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else
                {
                    Toast.makeText(UserProfileActivity.this, "Please enter your password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //Loads image into imageView after selection
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null)
        {
            recipeImageUri = data.getData();
            Glide.with(this).load(recipeImageUri).into(imageViewUserProfileImage);
        }
    }

    //Get file extension
    private String getFileExtension(Uri uri)
    {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
}
