package com.bizfns.services.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;

@Service
public class SchemaService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private List<String> schemaNames = Collections.emptyList(); // Initialize with an empty list

    @PostConstruct
    public void init() {
        loadSchemaNames();
    }

    @Scheduled(fixedRate = 86400000)
    public void loadSchemaNames() {
        try {
            String sql = "SELECT DISTINCT \"SCHEMA_NAME\" FROM \"Bizfns\".\"USER_MASTER\"";
            schemaNames = jdbcTemplate.queryForList(sql, String.class);
            if (schemaNames.isEmpty()) {
                System.out.println("No schema names found.");
            } else {
                System.out.println("Loaded schema names: " + schemaNames);
            }
        } catch (Exception e) {
            //System.err.println("Error loading schema names: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<String> getSchemaNames() {
        return schemaNames;
    }
}

