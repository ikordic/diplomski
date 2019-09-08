package com.kordic.ivan.poorstudentscookbook;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kordic.ivan.poorstudentscookbook.Adapter.RecipeAdapter;
import com.kordic.ivan.poorstudentscookbook.Model.Recipe;

import java.util.Objects;

public class MyRecipesActivity extends AppCompatActivity
{

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth userAuth = FirebaseAuth.getInstance();
    private CollectionReference recipeRef = db.collection("Recipe");
    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

    private RecipeAdapter adapter;
    private FloatingActionButton buttonAddNewRecipe;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_recipes);

        this.buttonAddNewRecipe = findViewById(R.id.buttonAddNewRecipe);
        buttonAddNewRecipe.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                startActivity(new Intent(MyRecipesActivity.this, AddNewRecipeActivity.class));
            }
        });

        //Search for recipes by userAuth.getUid()
        Query query = recipeRef.whereEqualTo("recipeAuthorId", userAuth.getUid());
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task)
            {
                if(task.isSuccessful())
                {
                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult()))
                    {
                        Recipe recipe = document.toObject(Recipe.class);
                    }
                }
                else
                {
                    Toast.makeText(MyRecipesActivity.this, "error", Toast.LENGTH_SHORT).show();
                }
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

        //Slide to call delete method
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT)
        {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1)
            {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int i)
            {
                //Swipe to delete

                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(MyRecipesActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(MyRecipesActivity.this);
                }
                builder.setTitle("Delete recipe")
                        .setMessage("Are you sure you want to delete this recipe?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                StorageReference imageRef = firebaseStorage.getReferenceFromUrl(adapter.getItem(viewHolder.getAdapterPosition()).getRecipeImage());
                                adapter.deleteItem(viewHolder.getAdapterPosition());
                                imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(MyRecipesActivity.this,"Successfuly deleted recipe",Toast.LENGTH_LONG).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(MyRecipesActivity.this,"There was an error.",Toast.LENGTH_LONG).show();
                                    }
                                });
                                adapter.notifyDataSetChanged();


                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                adapter.notifyDataSetChanged();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            }
        }).attachToRecyclerView(recyclerViewRecipes);

        //Click to see recipe
        adapter.setOnItemClickListener(new RecipeAdapter.OnItemClickListener()
        {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position)
            {
                startActivity(new Intent(MyRecipesActivity.this, RecipeOverviewActivity.class).putExtra("RECIPE_ID", documentSnapshot.getId()));
            }
        });

        //Long click to update
        adapter.setOnItemClickListener(new RecipeAdapter.OnItemLongClickListener()
        {
            @Override
            public void onItemLongClick(DocumentSnapshot documentSnapshot, int position)
            {
                startActivity(new Intent(MyRecipesActivity.this, AddNewRecipeActivity.class).putExtra("RECIPE_ID", documentSnapshot.getId()));
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
