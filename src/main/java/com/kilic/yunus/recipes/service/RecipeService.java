package com.kilic.yunus.recipes.service;

import com.kilic.yunus.recipes.exception.RecipeNotFoundException;
import com.kilic.yunus.recipes.model.Recipe;
import com.kilic.yunus.recipes.model.RecipeDto;
import com.kilic.yunus.recipes.model.RecipeElastic;
import com.kilic.yunus.recipes.model.RecipeJson;
import com.kilic.yunus.recipes.repository.RecipeRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecipeService {

    private final RecipeRepository repository;
    private final SearchService searchService;
    private final KafkaService kafkaService;
    private final ModelMapper mapper;
    @Value(value = "${recipe.json}")
    private String recipeJsonPath;

    public RecipeService(RecipeRepository repository, SearchService searchService, KafkaService kafkaService, ModelMapper mapper) {
        this.repository = repository;
        this.searchService = searchService;
        this.kafkaService = kafkaService;
        this.mapper = mapper;
    }

    public Recipe findById(String id) {
        Optional<Recipe> byId = repository.findById(id);
        if (byId.isEmpty()) {
            throw new RecipeNotFoundException(id);
        }

        return byId.get();
    }

    public Recipe createRecipe(Recipe recipe) {
        Recipe save = repository.save(recipe);
        kafkaService.sendMessageUpsert(mapper.map(save, RecipeDto.class));
        return save;
    }

    public boolean delete(String id) {
        repository.deleteById(id);
        kafkaService.sendMessageDelete(id);
        return true;
    }

    public Recipe updateRecipe(String id, Recipe recipe) {
        recipe.setId(id);
        Recipe save = repository.save(recipe);
        kafkaService.sendMessageUpsert(mapper.map(save, RecipeDto.class));
        return save;
    }

    public List<Recipe> bulkInsert() throws IOException, NoSuchAlgorithmException {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource(recipeJsonPath);
        InputStream inputStream = resource.getInputStream();
        String content = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
        RecipeJson[] recipeData = RecipeJson.fromJsonString(content);
        List<Recipe> list = new ArrayList<>();
        Random rand = SecureRandom.getInstanceStrong();
        for (int i = 0; i < recipeData.length; i++) {
            RecipeJson recipeJson = recipeData[i];
            Recipe recipe = new Recipe();
            new RecipeElastic();
            recipe.setName(recipeJson.getName());
            int randomNumber = rand.nextInt(8);
            recipe.setVegetarian(i % 2 == 0);
            recipe.setServing(randomNumber);
            recipe.setIngredients(Arrays.asList(recipeJson.getIngredients()));
            recipe.setInstructions(String.join("\n", recipeJson.getMethod()));
            list.add(recipe);
        }

        List<Recipe> recipes = repository.saveAll(list);
        List<RecipeElastic> recipeElastics = new ArrayList<>();
        recipes.forEach(x -> {
            RecipeElastic recipeElastic = mapper.map(x, RecipeElastic.class);
            recipeElastics.add(recipeElastic);
        });

        searchService.bulkSave(recipeElastics);
        return recipes;
    }
}
