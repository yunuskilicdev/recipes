package com.kilic.yunus.recipes;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.kilic.yunus.recipes.model.Recipe;
import com.kilic.yunus.recipes.model.RecipeElastic;
import com.kilic.yunus.recipes.repository.RecipeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static com.kilic.yunus.recipes.TestContainers.*;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = RecipesApplication.class)
public class RecipeControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private ElasticsearchRepository elasticsearchRepository;

    @BeforeAll
    public static void start() {
        mongoDBContainer.start();
        kafkaContainer.start();
        List<String> elasticEnvList = new ArrayList<>();
        elasticEnvList.add("xpack.security.enabled=false");
        elasticEnvList.add("discovery.type=single-node");
        elasticsearchContainer.setEnv(elasticEnvList);
        elasticsearchContainer.start();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("spring.elasticsearch.host", elasticsearchContainer::getHttpHostAddress);
    }

    @AfterEach
    public void deleteData() {
        recipeRepository.deleteAll();
        elasticsearchRepository.deleteAll();
    }

    @Test
    public void createRecipe_IntegrationTest() throws Exception {
        Recipe recipe = new Recipe();
        recipe.setName("TEST");
        recipe.setInstructions("TEST");
        recipe.setServing(1);
        recipe.setVegetarian(true);
        recipe.setIngredients(List.of("TEST"));
        mvc.perform(post("/recipe").content(objectMapper.writeValueAsString(recipe))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();
        Thread.sleep(3000);
        mvc.perform(get("/search").param("instructions", recipe.getInstructions())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    public void getRecipe_IntegrationTest() throws Exception {
        Recipe recipe = createRecipe();
        mvc.perform(get("/recipe/" + recipe.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.id", is(recipe.getId())));
    }

    @Test
    public void updateRecipe_IntegrationTest() throws Exception {
        Recipe recipe = createRecipe();
        Thread.sleep(3000);
        mvc.perform(get("/search").param("instructions", recipe.getInstructions())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        recipe.setInstructions("UPDATED");
        mvc.perform(put("/recipe/" + recipe.getId()).content(objectMapper.writeValueAsString(recipe))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.instructions", is("UPDATED")));
        Thread.sleep(3000);
        mvc.perform(get("/search").param("instructions", recipe.getInstructions())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        recipe.setInstructions("UPDATED");
    }

    @Test
    public void deleteRecipe_IntegrationTest() throws Exception {
        Recipe recipe = createRecipe();
        Thread.sleep(3000);
        mvc.perform(get("/search").param("instructions", recipe.getInstructions())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        mvc.perform(delete("/recipe/" + recipe.getId()).contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        Thread.sleep(3000);
        mvc.perform(get("/search").param("instructions", recipe.getInstructions())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    public void bulkInsert_IntegrationTest() throws Exception {
        mvc.perform(post("/recipe/bulk").contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1617));
    }

    private Recipe createRecipe() {
        Recipe recipe = new Recipe();
        recipe.setName("TEST");
        recipe.setInstructions("TEST");
        recipe.setServing(111);
        recipe.setVegetarian(true);
        recipe.setIngredients(List.of("TEST"));
        Recipe save = recipeRepository.save(recipe);
        elasticsearchRepository.save(modelMapper.map(save, RecipeElastic.class));
        return save;
    }

}
