package com.example.energy.percentage.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class UsageDataService {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public UsageDataService(@Qualifier("usageJdbcTemplate") JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }


    public JsonNode getUsageData(LocalDateTime dateTime) {
        String sql = "SELECT community_produced, community_used, grid_used FROM energy_usage WHERE hour = ?";
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(sql, dateTime);
            return objectMapper.convertValue(row, JsonNode.class);
        } catch (Exception e) {
            return null;
        }
    }
    }

