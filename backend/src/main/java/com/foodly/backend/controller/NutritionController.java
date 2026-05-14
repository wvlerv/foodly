package com.foodly.backend.controller;

import com.foodly.backend.entity.NutrientLog;
import com.foodly.backend.repository.NutrientLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nutrition")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NutritionController {

    private final NutrientLogRepository repository;

    @GetMapping("/logs")
    public List<NutrientLog> getAllLogs() {
        return repository.findAll();
    }
}