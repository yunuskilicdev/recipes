package com.kilic.yunus.recipes.exception;

public class RecipeNotFoundException extends RuntimeException {

    public RecipeNotFoundException(String id) {
        super("Could not find recipe " + id);
    }
}
