package com.kordic.ivan.poorstudentscookbook;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kordic.ivan.poorstudentscookbook.Model.Recipe;
import com.kordic.ivan.poorstudentscookbook.Model.User;

import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import java.util.ArrayList;
import java.util.Objects;

public class AddNewRecipeActivity extends AppCompatActivity
{
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth userAuth = FirebaseAuth.getInstance();
    private CollectionReference recipeRef = db.collection("Recipe");
    private CollectionReference userRef = db.collection("User");
    private StorageReference recipeStorageRef;

    private EditText editTextNewRecipeName;
    private EditText editTextNewRecipeDescription;
    private Button buttonSaveNewRecipe;
    private ImageView imageViewNewRecipe;
    private TextView textViewAddFromDevice;
    private EditText editTextNewIgredient;
    private ListView listViewIngredients;
    private Button buttonAddNewIgredient;
    private ProgressBar progressBar;
    private ArrayAdapter adapter;
    private EditText editTextNewRecipePreparationSteps;
    EditText input;

    ViewGroup progressView;
    protected boolean isProgressShowing = false;

    //Constant for image selection
    private static final int PICK_IMAGE_REQUEST = 1;

    //URI which points to image for the imageview
    private Uri recipeImageUri;

    //Used
    private Task recipeUploadTask;

    String recipeId;
    Boolean editRecipe = false;

    //Global variables with default values
    String newRecipeName;
    String newRecipeDescription;
    String newRecipePreparationSteps;
    //
    String newRecipeComment = " ";
    String newRecipeImageUrl = "https://firebasestorage.googleapis.com/v0/b/poorstudentscookbook-f9e8b.appspot.com/o/recipe%2Ficon_background.png?alt=media&token=5765490d-c310-4d36-8450-77cbd35e61ae";
    ArrayList<String> newIngredients;
    ArrayList<String> oldIngredients;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_recipe);

        recipeStorageRef = FirebaseStorage.getInstance().getReference("recipe");

        //Keep the keyboard down
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        this.editTextNewRecipeName = findViewById(R.id.editTextNewRecipeName);
        this.editTextNewRecipeDescription = findViewById(R.id.editTextNewRecipeDescription);
        this.buttonSaveNewRecipe = findViewById(R.id.buttonSaveNewRecipe);
        this.imageViewNewRecipe = findViewById(R.id.imageViewNewRecipe);
        this.textViewAddFromDevice = findViewById(R.id.textViewAddFromDevice);
        this.editTextNewIgredient=findViewById(R.id.editTextNewIngredient);
        this.listViewIngredients = findViewById(R.id.listViewIngredients);
        this.buttonAddNewIgredient = findViewById(R.id.buttonAddNewIgredient);
        this.progressBar = findViewById(R.id.progressBar);
        this.editTextNewRecipePreparationSteps = findViewById(R.id.editTextNewRecipePreparationSteps);

        //Getting recipeId from RecipeCardViewActivity-startActivity
        if (savedInstanceState == null)
        {
            Bundle extras = getIntent().getExtras();
            if (extras == null)
            {
                recipeId = "Empty";
            }
            else
            {
                recipeId = extras.getString("RECIPE_ID");
                editRecipe = true;
            }
        }
        else
        {
            recipeId = (String) savedInstanceState.getSerializable("RECIPE_ID");
            editRecipe = true;
        }

        assert recipeId != null;
        if (recipeId.isEmpty())
        {
            Toast.makeText(this, "Critical error!", Toast.LENGTH_SHORT).show();
            return;
        }
        else
        {
            recipeRef.document(recipeId).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
                    {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot)
                        {
                            if (documentSnapshot.exists())
                            {
                                Recipe recipe = documentSnapshot.toObject(Recipe.class);
                                assert recipe != null;
                                editTextNewRecipeName.setText(recipe.getRecipeName());
                                editTextNewRecipeDescription.setText(recipe.getRecipeDescription());
                                newIngredients = recipe.getRecipeIngredients();
                                adapter = new ArrayAdapter<String>(AddNewRecipeActivity.this, android.R.layout.simple_list_item_1, newIngredients);
                                listViewIngredients.setAdapter(adapter);
                                editTextNewRecipePreparationSteps.setText(recipe.getRecipePreparationSteps());
                                newRecipeImageUrl = recipe.getRecipeImage();
                                Glide.with(AddNewRecipeActivity.this).load(recipe.getRecipeImage()).into(imageViewNewRecipe);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener()
                    {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            Toast.makeText(AddNewRecipeActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }

        textViewAddFromDevice.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //Gets only images and the constant is used to identify the activity
                startActivityForResult(new Intent().setType("image/*").setAction(Intent.ACTION_GET_CONTENT), PICK_IMAGE_REQUEST);
            }
        });



        newIngredients = new ArrayList<String>();
         adapter = new ArrayAdapter<String>(AddNewRecipeActivity.this, android.R.layout.simple_list_item_1, newIngredients);
        listViewIngredients.setAdapter(adapter);

        buttonSaveNewRecipe.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                newRecipeName = editTextNewRecipeName.getText().toString();
                newRecipeDescription = editTextNewRecipeDescription.getText().toString();
                newRecipePreparationSteps = editTextNewRecipePreparationSteps.getText().toString();
                newRecipeComment = " ";

                if (newRecipeName.trim().isEmpty() || newRecipeDescription.trim().isEmpty() || newIngredients.isEmpty() || newRecipePreparationSteps.trim().isEmpty()) {
                    Toast.makeText(AddNewRecipeActivity.this, "Fill all fields and add at least one ingredient", Toast.LENGTH_LONG).show();
                    return;
                }else if(newRecipeName.length()>125){
                    Toast.makeText(AddNewRecipeActivity.this, "Too big name for a recipe, maximum is 125 charachters", Toast.LENGTH_SHORT).show();
                }else {

                    uploadFile();
                }
            }
        });

        newIngredients = new ArrayList<>();
         adapter = new ArrayAdapter<>(AddNewRecipeActivity.this, android.R.layout.simple_list_item_1, newIngredients);
        listViewIngredients.setAdapter(adapter);
        ViewCompat.setNestedScrollingEnabled(listViewIngredients,true);

        listViewIngredients.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(AddNewRecipeActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(AddNewRecipeActivity.this);
                }

                builder.setTitle("Edit ingredient");
                builder.setMessage("Enter the ingredient: ");
                input = new EditText(AddNewRecipeActivity.this);


                String oldIngredient = newIngredients.get(position);
                input.setText(oldIngredient);

                builder.setView(input);
                Log.i("Clicked ingredient", oldIngredient);
                builder.setPositiveButton(getResources().getString(R.string.save), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if(input.getText().equals("")){
                                    newIngredients.remove(position);
                                    adapter.notifyDataSetChanged();
                                }
                                newIngredients.set(position, input.getText().toString());
                                adapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                newIngredients.remove(position);
                                adapter.notifyDataSetChanged();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            return true;
            }
        });



    }


    public void onBtnClick(View v){
                buttonAddNewIgredient.requestFocus();
                String ingredient = editTextNewIgredient.getText().toString();
                if(ingredient.equals("")){
                    Toast.makeText(AddNewRecipeActivity.this,"Enter an ingredient",Toast.LENGTH_LONG).show();
                }else{
                    newIngredients.add(ingredient);
                    adapter.notifyDataSetChanged();
                }
                adapter.notifyDataSetChanged();
                editTextNewIgredient.setText("");
            }




    //ProgressBar
    public void showProgressingView()
    {

        if (!isProgressShowing)
        {
            isProgressShowing = true;
            progressView = (ViewGroup) getLayoutInflater().inflate(R.layout.progressbar_layout, null);
            View v = this.findViewById(android.R.id.content).getRootView();
            ViewGroup viewGroup = (ViewGroup) v;
            viewGroup.addView(progressView);
        }
    }

    //Loads image into imageView after selection
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null)
        {
            recipeImageUri = data.getData();
            Glide.with(this).load(recipeImageUri).into(imageViewNewRecipe);
        }
    }

    //Get file extension
    private String getFileExtension(Uri uri)
    {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    //Uploads all data and fetches the Firebase storage image url
    private void uploadFile()
    {
        //Edit existing recipe
        if (editRecipe)
        {
            //Anti-button-spam method
            buttonSaveNewRecipe.setClickable(false);
            UIUtil.hideKeyboard(AddNewRecipeActivity.this);
            showProgressingView();

            if (recipeImageUri != null)
            {
                final StorageReference fileReference = recipeStorageRef.child(System.currentTimeMillis()
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
                    public void onComplete(@NonNull Task<Uri> task)
                    {
                        if (task.isSuccessful())
                        {
                            recipeRef.document(recipeId).update("recipeName", newRecipeName);
                            recipeRef.document(recipeId).update("recipeDescription", newRecipeDescription);
                            recipeRef.document(recipeId).update("recipeImage",Objects.requireNonNull(task.getResult()).toString());
                            recipeRef.document(recipeId).update("recipePreparationSteps", newRecipePreparationSteps);
                            Toast.makeText(AddNewRecipeActivity.this, "Recipe updated!", Toast.LENGTH_SHORT).show();
                            recipeId = null;
                            finish();
                        }
                        else
                        {
                            Toast.makeText(AddNewRecipeActivity.this, "Task failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast.makeText(AddNewRecipeActivity.this, "putFile() doesn't work", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            //If the image hasn't been changed
            else
            {
                recipeRef.document(recipeId).update("recipeName", newRecipeName);
                recipeRef.document(recipeId).update("recipeDescription", newRecipeDescription);
                recipeRef.document(recipeId).update("recipeImage", newRecipeImageUrl);
                recipeRef.document(recipeId).update("recipeIngredients", newIngredients);
                recipeRef.document(recipeId).update("recipePreparationSteps", newRecipePreparationSteps);

                recipeRef.document(recipeId).update("recipeComment", newRecipeComment);

                Toast.makeText(AddNewRecipeActivity.this, "Recipe updated!", Toast.LENGTH_SHORT).show();
                recipeId = null;
                finish();
            }
        }

        //Create new recipe
        else
        {
            //Anti-button-spam method
            buttonSaveNewRecipe.setClickable(false);
            UIUtil.hideKeyboard(AddNewRecipeActivity.this);
            showProgressingView();

            if (recipeImageUri != null)
            {
                final StorageReference fileReference = recipeStorageRef.child(System.currentTimeMillis()
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
                            //Attach username to recipe - experimental(remove userRef and its Listeners but keep the recipeRef to revert)
                            userRef.document(Objects.requireNonNull(userAuth.getUid())).get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
                                    {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot)
                                        {
                                            User user = documentSnapshot.toObject(User.class);
                                            assert user != null;
                                            recipeRef
                                                    .add(new Recipe(newRecipeName, newRecipeDescription, Objects.requireNonNull(task.getResult()).toString(), userAuth.getUid(), user.getUserUsername(), newIngredients, newRecipePreparationSteps, newRecipeComment))
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>()
                                                    {
                                                        @Override
                                                        public void onSuccess(DocumentReference documentReference)
                                                        {
                                                            Toast.makeText(AddNewRecipeActivity.this, "Recipe added!", Toast.LENGTH_SHORT).show();
                                                            finish();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener()
                                                    {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e)
                                                        {
                                                            Toast.makeText(AddNewRecipeActivity.this, "Error: " + e, Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener()
                                    {
                                        @Override
                                        public void onFailure(@NonNull Exception e)
                                        {
                                            Toast.makeText(AddNewRecipeActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                        else
                        {
                            Toast.makeText(AddNewRecipeActivity.this, "Task failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast.makeText(AddNewRecipeActivity.this, "putFile() doesn't work", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            //If no image is selected set the default one
            else
            {
                userRef.document(Objects.requireNonNull(userAuth.getUid())).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
                        {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot)
                            {
                                User user = documentSnapshot.toObject(User.class);
                                assert user != null;
                                recipeRef
                                        .add(new Recipe(newRecipeName, newRecipeDescription, newRecipeImageUrl, userAuth.getUid(), user.getUserUsername(),newIngredients, newRecipePreparationSteps, newRecipeComment))
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>()
                                        {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference)
                                            {
                                                Toast.makeText(AddNewRecipeActivity.this, "Recipe added!", Toast.LENGTH_SHORT).show();
                                                finish();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener()
                                        {
                                            @Override
                                            public void onFailure(@NonNull Exception e)
                                            {
                                                Toast.makeText(AddNewRecipeActivity.this, "Error: " + e, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener()
                        {
                            @Override
                            public void onFailure(@NonNull Exception e)
                            {
                                Toast.makeText(AddNewRecipeActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }
}
