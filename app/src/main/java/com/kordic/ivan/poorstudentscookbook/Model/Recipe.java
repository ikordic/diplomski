package com.kordic.ivan.poorstudentscookbook.Model;

//Model class which stores data

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Recipe
{
    private String recipeName;
    private String recipeDescription;
    private String recipeImage;
    private String recipeAuthorId;
    private String recipeAuthorUsername;
    private ArrayList<String> recipeIngredients;
    private String recipePreparationSteps;
    private String recipeComments;

    //Constructors
    public Recipe()
    {
    }

    public Recipe(String recipeName, String recipeDescription, String recipeImage, String recipeAuthorId, String recipeAuthorUsername) {
        this.recipeName = recipeName;
        this.recipeDescription = recipeDescription;
        this.recipeImage = recipeImage;
        this.recipeAuthorId = recipeAuthorId;
        this.recipeAuthorUsername = recipeAuthorUsername;
    }

    public Recipe(String recipeName, String recipeDescription, String recipeImage, String recipeAuthorId, String recipeAuthorUsername, ArrayList<String> recipeIngredients, String recipePreparationSteps, String recipeComments) {
        this.recipeName = recipeName;
        this.recipeDescription = recipeDescription;
        this.recipeImage = recipeImage;
        this.recipeAuthorId = recipeAuthorId;
        this.recipeAuthorUsername = recipeAuthorUsername;
        this.recipeIngredients = recipeIngredients;
        this.recipePreparationSteps = recipePreparationSteps;
        this.recipeComments = recipeComments;
    }

    //Getters-Setters
    public String getRecipeName()
    {
        return recipeName;
    }

    public void setRecipeName(String recipeName)
    {
        this.recipeName = recipeName;
    }

    public String getRecipeDescription()
    {
        return recipeDescription;
    }

    public void setRecipeDescription(String recipeDescription)
    {
        this.recipeDescription = recipeDescription;
    }

    public ArrayList<String> getRecipeIngredients() {
        return recipeIngredients;
    }

    public void setRecipeIngredients(ArrayList<String> recipeIngredients) {
        this.recipeIngredients = recipeIngredients;
    }

    public String getRecipeImage()
    {
        return recipeImage;
    }

    public void setRecipeImage(String recipeImage)
    {
        this.recipeImage = recipeImage;
    }

    public String getRecipeAuthorId()
    {
        return recipeAuthorId;
    }

    public void setRecipeAuthorId(String recipeAuthorId)
    {
        this.recipeAuthorId = recipeAuthorId;
    }

    public String getRecipeAuthorUsername()
    {
        return recipeAuthorUsername;
    }

    public void setRecipeAuthorUsername(String recipeAuthorUsername)
    {
        this.recipeAuthorUsername = recipeAuthorUsername;
    }

    public String getRecipePreparationSteps()
    {
        return recipePreparationSteps;
    }

    public void setRecipePreparationSteps(String recipePreparationSteps)
    {
        this.recipePreparationSteps = recipePreparationSteps;
    }

    public String getRecipeComments() {
        return recipeComments;
    }

    public void setRecipeComments(String recipeComments) {
        this.recipeComments = recipeComments;
    }
}
