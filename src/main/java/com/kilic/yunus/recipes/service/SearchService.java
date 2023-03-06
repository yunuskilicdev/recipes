package com.kilic.yunus.recipes.service;

import com.kilic.yunus.recipes.model.RecipeElastic;
import com.kilic.yunus.recipes.model.SearchDto;
import com.kilic.yunus.recipes.repository.SearchRepository;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final SearchRepository searchRepository;

    public SearchService(ElasticsearchOperations elasticsearchOperations, SearchRepository searchRepository) {
        this.elasticsearchOperations = elasticsearchOperations;
        this.searchRepository = searchRepository;
    }

    public RecipeElastic save(RecipeElastic recipe) {
        return searchRepository.save(recipe);
    }

    public List<RecipeElastic> findAll() {
        Iterable<RecipeElastic> iterable = searchRepository.findAll();
        List<RecipeElastic> result = new ArrayList<>();
        iterable.forEach(result::add);
        return result;
    }

    public List<RecipeElastic> search(SearchDto searchDto) {
        List<Criteria> criteriaList = new ArrayList<>();
        if (searchDto.getVegetarian() != null) {
            criteriaList.add(new Criteria("vegetarian").is(searchDto.getVegetarian()));
        }

        if (searchDto.getServing() != null) {
            criteriaList.add(new Criteria("serving").is(searchDto.getServing()));
        }

        if (StringUtils.hasText(searchDto.getInstructions())) {
            criteriaList.add(new Criteria("instructions").contains(searchDto.getInstructions()));
        }

        if (!CollectionUtils.isEmpty(searchDto.getIngredientInc())) {
            criteriaList.add(new Criteria("ingredients").in(searchDto.getIngredientInc()));
        }

        if (!CollectionUtils.isEmpty(searchDto.getIngredientExc())) {
            criteriaList.add(new Criteria("ingredients").notIn(searchDto.getIngredientExc()));
        }

        Criteria[] array = new Criteria[criteriaList.size()];
        criteriaList.toArray(array);
        Criteria criteria = new Criteria().and(array);
        CriteriaQuery criteriaQuery = new CriteriaQuery(criteria);
        SearchHits<RecipeElastic> search = elasticsearchOperations.search(criteriaQuery, RecipeElastic.class);
        List<RecipeElastic> response = new ArrayList<>();
        for (SearchHit<RecipeElastic> recipeElasticSearchHit : search) {
            response.add(recipeElasticSearchHit.getContent());
        }

        return response;
    }

    public void deleteDocumentById(String id) {
        searchRepository.deleteById(id);
    }

    public void bulkSave(List<RecipeElastic> recipeElastics) {
        searchRepository.saveAll(recipeElastics);
    }

}
