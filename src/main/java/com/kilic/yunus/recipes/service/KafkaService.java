package com.kilic.yunus.recipes.service;

import com.kilic.yunus.recipes.config.KafkaConfig;
import com.kilic.yunus.recipes.model.RecipeDto;
import com.kilic.yunus.recipes.model.RecipeElastic;
import org.modelmapper.ModelMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaService {

    private final KafkaTemplate<String, RecipeDto> kafkaTemplate;

    private final KafkaTemplate<String, String> kafkaTemplateString;

    private final SearchService searchService;

    private final ModelMapper mapper;

    public KafkaService(KafkaTemplate<String, RecipeDto> kafkaTemplate, KafkaTemplate<String, String> kafkaTemplateString, SearchService searchService, ModelMapper mapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaTemplateString = kafkaTemplateString;
        this.searchService = searchService;
        this.mapper = mapper;
    }

    public void sendMessageUpsert(RecipeDto message) {
        kafkaTemplate.send(KafkaConfig.TOPIC_RECIPE_UPSERT, message);
    }

    public void sendMessageDelete(String id) {
        kafkaTemplateString.send(KafkaConfig.TOPIC_RECIPE_DELETE, id);
    }

    @KafkaListener(id = "upsertListener", topics = KafkaConfig.TOPIC_RECIPE_UPSERT, containerFactory = "recipeKafkaListenerContainerFactory")
    public void listenUpsert(RecipeDto in) {
        searchService.save(mapper.map(in, RecipeElastic.class));
    }

    @KafkaListener(id = "deleteListener", topics = KafkaConfig.TOPIC_RECIPE_DELETE)
    public void listenDelete(String in) {
        searchService.deleteDocumentById(in);
    }
}
