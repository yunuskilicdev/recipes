package com.kilic.yunus.recipes;

import com.kilic.yunus.recipes.service.RecipeService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static com.kilic.yunus.recipes.TestContainers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = RecipesApplication.class)
public class SearchControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private RecipeService recipeService;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        mongoDBContainer.start();
        kafkaContainer.start();
        List<String> elasticEnvList = new ArrayList<>();
        elasticEnvList.add("xpack.security.enabled=false");
        elasticEnvList.add("discovery.type=single-node");
        elasticsearchContainer.setEnv(elasticEnvList);
        elasticsearchContainer.start();
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("spring.elasticsearch.host", elasticsearchContainer::getHttpHostAddress);
    }

    @BeforeAll
    void start() throws IOException, NoSuchAlgorithmException, InterruptedException {
        recipeService.bulkInsert();
        Thread.sleep(3000);
    }

    @Test
    public void vegetarian() throws Exception {
        mvc.perform(get("/search").param("vegetarian", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(809));
    }

    @Test
    public void ovenInstAndWithoutSalmonIng() throws Exception {
        mvc.perform(get("/search").param("instructions", "oven").param("ingredientExc", "salmon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(840));
    }
}
