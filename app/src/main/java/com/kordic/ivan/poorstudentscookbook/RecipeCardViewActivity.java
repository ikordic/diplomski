package com.kordic.ivan.poorstudentscookbook;

import android.content.Intent;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.kordic.ivan.poorstudentscookbook.Adapter.RecipeAdapter;
import com.kordic.ivan.poorstudentscookbook.Model.Recipe;

public class RecipeCardViewActivity extends AppCompatActivity
{
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth userAuth = FirebaseAuth.getInstance();
    private CollectionReference recipeRef = db.collection("Recipe");

    private RecipeAdapter adapter;
    private FloatingActionButton buttonAddNewRecipe;
    private RecyclerView recyclerViewRecipes;
    private int positionIndex = 0;
    private Boolean logged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_card_view);

        this.buttonAddNewRecipe = findViewById(R.id.buttonAddNewRecipe);

        //Connecting the adapter and Recyclerview
        setUpRecyclerView();

        buttonAddNewRecipe.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                startActivity(new Intent(RecipeCardViewActivity.this, AddNewRecipeActivity.class));
            }
        });
    }

    //Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        if(logged.equals(false))
        {
            inflater.inflate(R.menu.guest_user_menu, menu);
        }
        else
        {
            inflater.inflate(R.menu.logged_user_menu, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_my_recipes:
            {
                startActivity(new Intent(RecipeCardViewActivity.this, MyRecipesActivity.class));
                break;
            }
            case R.id.menu_my_profile:
            {
                startActivity(new Intent(RecipeCardViewActivity.this, UserProfileActivity.class));
                break;
            }
            case R.id.menu_log_out:
            {
                userAuth.signOut();
                startActivity(new Intent(RecipeCardViewActivity.this, RecipeCardViewActivity.class));
                finish();
                break;
            }
            case R.id.menu_log_in:
            {
                startActivity(new Intent(RecipeCardViewActivity.this, LoginActivity.class));
                finish();
                break;
            }
            case R.id.menu_register:
            {
                startActivity(new Intent(RecipeCardViewActivity.this, RegisterActivity.class));
                finish();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    //Connecting the adapter and recyclerview
    private void setUpRecyclerView()
    {
        Query query = recipeRef.orderBy("recipeName");

        FirestoreRecyclerOptions<Recipe> options = new FirestoreRecyclerOptions.Builder<Recipe>()
                .setQuery(query, Recipe.class)
                .build();
        adapter = new RecipeAdapter(options);
        recyclerViewRecipes = findViewById(R.id.recyclerViewRecipes);
        recyclerViewRecipes.setHasFixedSize(true);
        recyclerViewRecipes.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewRecipes.setAdapter(adapter);

        //Click to see recipe
        adapter.setOnItemClickListener(new RecipeAdapter.OnItemClickListener()
        {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position)
            {
                positionIndex = position;
                startActivity(new Intent(RecipeCardViewActivity.this, RecipeOverviewActivity.class).putExtra("RECIPE_ID", documentSnapshot.getId()));
            }
        });

        //Long click to update //fix this dude
        adapter.setOnItemClickListener(new RecipeAdapter.OnItemLongClickListener()
        {
            @Override
            public void onItemLongClick(DocumentSnapshot documentSnapshot, int position)
            {
                //Empty on purpose
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        adapter.startListening();

        if(userAuth.getCurrentUser() == null)
        {
            buttonAddNewRecipe.hide();
            logged = false;
        }
        else
        {
            buttonAddNewRecipe.show();
            logged = true;
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        adapter.stopListening();
    }
}
