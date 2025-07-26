package com.trukea.service;

import com.trukea.repository.CityRepository;
import java.util.List;
import java.util.Map;

public class CityService {
    private CityRepository cityRepository;

    public CityService() {
        this.cityRepository = new CityRepository();
    }

    public List<Map<String, Object>> getAllCities() {
        return cityRepository.findAll();
    }
}