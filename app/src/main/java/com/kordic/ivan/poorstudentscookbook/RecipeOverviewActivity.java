package com.kordic.ivan.poorstudentscookbook;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.kordic.ivan.poorstudentscookbook.Model.Recipe;
import com.kordic.ivan.poorstudentscookbook.Model.User;

import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import java.util.ArrayList;
import java.util.Objects;

public class RecipeOverviewActivity extends AppCompatActivity
{

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference recipeRef = db.collection("Recipe");
    private FirebaseAuth userAuth = FirebaseAuth.getInstance();
    private CollectionReference userRef = db.collection("User");
    
    private TextView textViewRecipeOverviewName;
    private TextView textViewRecipeOverviewDescription;    
    private ImageView imageViewRecipeOverview;
    private TextView textViewRecipeOverviewBy;
    private ListView listViewRecipeIngredients;
    private TextView textViewRecipeOverviewPreparationSteps;
    private TextView textViewRecipeComments;
    private TextView textViewComments;
    private EditText editTextAddComment;
    private Button buttonPostComment;

    String recipeId = "";
    private String username = "";
    private String addRecipeComment ="";
    private String comments = "";
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_overview);
        
        this.textViewRecipeOverviewName = findViewById(R.id.textViewRecipeOverviewName);
        this.textViewRecipeOverviewDescription = findViewById(R.id.textViewRecipeOverviewDescription);
        this.imageViewRecipeOverview = findViewById(R.id.imageViewRecipeOverview);
        this.textViewRecipeOverviewBy = findViewById(R.id.textViewRecipeOverviewBy);
        this.textViewRecipeOverviewPreparationSteps = findViewById(R.id.textViewRecipeOverviewPreparationSteps);
        this.listViewRecipeIngredients = findViewById(R.id.listViewRecipeIngredients);
        this.textViewRecipeComments = findViewById(R.id.textViewRecipeComments);
        this.textViewComments = findViewById(R.id.textViewComments);
        this.editTextAddComment = findViewById(R.id.editTextAddComment);
        this.buttonPostComment = findViewById(R.id.buttonPostComment);

        if(savedInstanceState == null)
        {
            Bundle extras = getIntent().getExtras();
            if(extras == null)
            {
                recipeId = "prazan";
            }
            else
            {
                recipeId = extras.getString("RECIPE_ID");
            }
        }
        else
        {
            recipeId= (String) savedInstanceState.getSerializable("RECIPE_ID");
        }
        
        if(recipeId.isEmpty())
        {
            Toast.makeText(this, "Critical error!", Toast.LENGTH_SHORT).show();
            return;
        }
        recipeRef.document(recipeId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
                {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot)
                    {
                        if(documentSnapshot.exists())
                        {
                            Recipe recipe = documentSnapshot.toObject(Recipe.class);
                            assert recipe != null;
                            textViewRecipeOverviewName.setText(recipe.getRecipeName());
                            textViewRecipeOverviewDescription.setText(recipe.getRecipeDescription());
                            textViewRecipeOverviewBy.setText(recipe.getRecipeAuthorUsername());
                            textViewRecipeOverviewPreparationSteps.setText(recipe.getRecipePreparationSteps());
                            username = recipe.getRecipeAuthorUsername();
                            //
                            textViewRecipeComments.setText(recipe.getRecipeComments());

                            ArrayList<String> arrayList  = new ArrayList<String>();
                            arrayList = (ArrayList)documentSnapshot.get("recipeIngredients");
                            ArrayAdapter<String> adapter  = new ArrayAdapter<String>(RecipeOverviewActivity.this, android.R.layout.simple_list_item_1,arrayList);
                            listViewRecipeIngredients.setAdapter(adapter);
                            Glide.with(RecipeOverviewActivity.this).load(recipe.getRecipeImage()).into(imageViewRecipeOverview);

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast.makeText(RecipeOverviewActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        textViewRecipeOverviewBy.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {
                startActivity(new Intent(RecipeOverviewActivity.this, ProfileRecipesActivity.class).putExtra("USERNAME", username));
            }
        });

        ViewCompat.setNestedScrollingEnabled(listViewRecipeIngredients,true);

        //Dodavanje komentara

        buttonPostComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonPostComment.setClickable(false);
                UIUtil.hideKeyboard(RecipeOverviewActivity.this);
                addRecipeComment = editTextAddComment.getText().toString();
                Toast.makeText(RecipeOverviewActivity.this, "Comment added!", Toast.LENGTH_SHORT).show();
                //
                userRef.document(Objects.requireNonNull(userAuth.getUid())).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                User user = documentSnapshot.toObject(User.class);
                                comments = textViewRecipeComments.getText().toString();
                                if (comments == " " || comments == "")
                                {
                                    comments = comments + "\n" + user.getUserUsername() + ": " + addRecipeComment;
                                }
                                else
                                {
                                        comments = comments + "\n\n" + user.getUserUsername() + ": " + addRecipeComment;
                                }
                                recipeRef.document(recipeId).update("recipeComments", comments).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Intent intent = getIntent();
                                        finish();
                                        startActivity(intent);
                                    }
                                });

                            }
                        });
            }
        });

    }


}

