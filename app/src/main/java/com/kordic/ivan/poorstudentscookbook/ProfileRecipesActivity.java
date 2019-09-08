package com.kordic.ivan.poorstudentscookbook;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.kordic.ivan.poorstudentscookbook.Adapter.RecipeAdapter;
import com.kordic.ivan.poorstudentscookbook.Model.Recipe;
import com.kordic.ivan.poorstudentscookbook.Model.User;

public class ProfileRecipesActivity extends AppCompatActivity
{

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference userRef = db.collection("User");
    private CollectionReference recipeRef = db.collection("Recipe");

    private TextView textViewProfileUsername;
    private ImageView imageViewProfileUserImage;
    private RecipeAdapter adapter;

    private String username = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_recipes);

        this.textViewProfileUsername = findViewById(R.id.textViewProfileUsername);
        this.imageViewProfileUserImage = findViewById(R.id.imageViewProfileUserImage);

        if (savedInstanceState == null)
        {
            Bundle extras = getIntent().getExtras();
            if (extras == null)
            {
                username = "prazan";
            }
            else
            {
                username = extras.getString("USERNAME");
                textViewProfileUsername.setText(username);
            }
        }
        else
        {
            username = (String) savedInstanceState.getSerializable("USERNAME");
            textViewProfileUsername.setText(username);
        }

        //
        Query queryUser = userRef.whereEqualTo("userUsername", username);
        queryUser.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task)
                    {
                        if (task.isSuccessful())
                        {
                            for (QueryDocumentSnapshot document : task.getResult())
                            {
                                User user = document.toObject(User.class);
                                Glide.with(ProfileRecipesActivity.this).load(user.getUserProfileImage()).into(imageViewProfileUserImage);
                            }
                        }
                        else
                        {
                            Toast.makeText(ProfileRecipesActivity.this, "Greska u IFu", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast.makeText(ProfileRecipesActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                });
        //

        //Set recycler view for recipes via username
        Query query = recipeRef.whereEqualTo("recipeAuthorUsername", username);
        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task)
                    {
                        if (task.isSuccessful())
                        {
                            for (QueryDocumentSnapshot document : task.getResult())
                            {
                                Recipe recipe = document.toObject(Recipe.class);
                            }
                        }
                        else
                        {
                            Toast.makeText(ProfileRecipesActivity.this, "Greska u IFu", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast.makeText(ProfileRecipesActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                });

        FirestoreRecyclerOptions<Recipe> options = new FirestoreRecyclerOptions.Builder<Recipe>()
                .setQuery(query, Recipe.class)
                .build();
        adapter = new RecipeAdapter(options);
        RecyclerView recyclerViewRecipes = findViewById(R.id.recyclerViewRecipes);
        recyclerViewRecipes.setHasFixedSize(true);
        recyclerViewRecipes.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewRecipes.setAdapter(adapter);

        //Click to see recipe
        adapter.setOnItemClickListener(new RecipeAdapter.OnItemClickListener()
        {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position)
            {
                startActivity(new Intent(ProfileRecipesActivity.this, RecipeOverviewActivity.class).putExtra("RECIPE_ID", documentSnapshot.getId()));
            }
        });

    }

    @Override
    protected void onStart()
    {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        adapter.stopListening();
    }

}
