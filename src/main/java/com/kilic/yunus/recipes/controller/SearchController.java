package com.kilic.yunus.recipes.controller;


import com.kilic.yunus.recipes.model.RecipeDto;
import com.kilic.yunus.recipes.model.RecipeElastic;
import com.kilic.yunus.recipes.model.SearchDto;
import com.kilic.yunus.recipes.service.SearchService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/search")
@RestController
public class SearchController {

    private final SearchService service;

    private final ModelMapper mapper;

    public SearchController(SearchService service, ModelMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping("/all")
    public ResponseEntity<List<RecipeDto>> findAll() {
        List<RecipeElastic> recipes = service.findAll();
        List<RecipeDto> dtos = recipes.stream().map(x -> mapper.map(x, RecipeDto.class)).collect(Collectors.toList());
        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<RecipeDto>> search(SearchDto searchDto) {
        List<RecipeElastic> recipes = service.search(searchDto);
        List<RecipeDto> dtos = recipes.stream().map(x -> mapper.map(x, RecipeDto.class)).collect(Collectors.toList());
        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }
}
