package com.kordic.ivan.poorstudentscookbook.Adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.kordic.ivan.poorstudentscookbook.Model.Recipe;
import com.kordic.ivan.poorstudentscookbook.R;

//Adapter gets data from the source into the recyclerview

public class RecipeAdapter extends FirestoreRecyclerAdapter<Recipe, RecipeAdapter.RecipeHolder>
{
    private OnItemClickListener mListener;

    private OnItemLongClickListener lListener;

    public RecipeAdapter(@NonNull FirestoreRecyclerOptions<Recipe> options)
    {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull RecipeHolder holder, int position, @NonNull Recipe model)
    {
        //Adding in the parameters in the holder
        holder.textViewRecipeNameCard.setText(model.getRecipeName());
        holder.textViewRecipeDescriptionCard.setText(model.getRecipeDescription());
        Glide.with(holder.imageViewRecipeCard.getContext()).load(model.getRecipeImage()).into(holder.imageViewRecipeCard);
    }

    @NonNull
    @Override
    public RecipeHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        //Setting the inflater
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recipe_card_element, viewGroup, false);
        return new RecipeHolder(v);
    }

    //Viewholder parameters
    class RecipeHolder extends RecyclerView.ViewHolder
    {
        private TextView textViewRecipeNameCard;
        private TextView textViewRecipeDescriptionCard;
        private ImageView imageViewRecipeCard;

        public RecipeHolder(@NonNull View itemView)
        {
            super(itemView);
            //itemView is the card
            textViewRecipeNameCard = itemView.findViewById(R.id.textViewRecipeNameCard);
            textViewRecipeDescriptionCard = itemView.findViewById(R.id.textViewRecipeDescriptionCard);
            imageViewRecipeCard = itemView.findViewById(R.id.imageViewRecipeCard);

            itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && mListener != null)
                    {
                        mListener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View view)
                {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && mListener != null)
                    {
                        lListener.onItemLongClick(getSnapshots().getSnapshot(position), position);
                    }
                    return false;
                }
            });

        }
    }

    //Deleting recipe
    public void deleteItem(int position)
    {
        getSnapshots().getSnapshot(position).getReference().delete();
    }

    //Click transfer interface
    public interface OnItemClickListener
    {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener)
    {
        this.mListener = listener;
    }

    //Long click interface

    public interface OnItemLongClickListener
    {
        void onItemLongClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(OnItemLongClickListener listener)
    {
        this.lListener = listener;
    }
}
