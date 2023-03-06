package com.kilic.yunus.recipes.repository;

import com.kilic.yunus.recipes.model.RecipeElastic;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchRepository extends ElasticsearchRepository<RecipeElastic, String> {
}
