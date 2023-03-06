package com.kilic.yunus.recipes.model;

import java.util.List;

public class SearchDto {

    private Boolean vegetarian;

    private Integer serving;

    private List<String> ingredientInc;

    private List<String> ingredientExc;
    private String instructions;

    public SearchDto() {
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public Boolean getVegetarian() {
        return vegetarian;
    }

    public void setVegetarian(Boolean vegetarian) {
        this.vegetarian = vegetarian;
    }

    public Integer getServing() {
        return serving;
    }

    public void setServing(Integer serving) {
        this.serving = serving;
    }

    public List<String> getIngredientInc() {
        return ingredientInc;
    }

    public void setIngredientInc(List<String> ingredientInc) {
        this.ingredientInc = ingredientInc;
    }

    public List<String> getIngredientExc() {
        return ingredientExc;
    }

    public void setIngredientExc(List<String> ingredientExc) {
        this.ingredientExc = ingredientExc;
    }
}
