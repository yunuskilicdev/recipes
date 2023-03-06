package com.kilic.yunus.recipes.controller;

import com.kilic.yunus.recipes.model.Recipe;
import com.kilic.yunus.recipes.model.RecipeDto;
import com.kilic.yunus.recipes.service.RecipeService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@RequestMapping("/recipe")
@RestController
public class RecipeController {

    private final ModelMapper mapper;

    private final RecipeService service;

    public RecipeController(ModelMapper mapper, RecipeService service) {
        this.mapper = mapper;
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<RecipeDto> createRecipe(@RequestBody RecipeDto recipeDto) {
        Recipe recipe = service.createRecipe(mapper.map(recipeDto, Recipe.class));
        return new ResponseEntity<>(mapper.map(recipe, RecipeDto.class), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeDto> findById(@PathVariable String id) {
        return new ResponseEntity<>(mapper.map(service.findById(id), RecipeDto.class), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> delete(@PathVariable String id) {
        return new ResponseEntity<>(service.delete(id), HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecipeDto> updateRecipe(@PathVariable String id,
                                                  @RequestBody RecipeDto recipeDto) {
        Recipe recipe = service.updateRecipe(id, mapper.map(recipeDto, Recipe.class));
        return new ResponseEntity<>(mapper.map(recipe, RecipeDto.class), HttpStatus.OK);
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<Recipe>> bulkInsert() throws IOException, NoSuchAlgorithmException {
        List<Recipe> recipes = service.bulkInsert();
        return new ResponseEntity<>(recipes, HttpStatus.OK);
    }
}
