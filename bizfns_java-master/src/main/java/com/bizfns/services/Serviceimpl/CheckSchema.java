package com.bizfns.services.Serviceimpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

public class CheckSchema {



    @Autowired
    private  JdbcTemplate jdbcTemplate;

    public CheckSchema(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;

    }


    public boolean doesSchemaExist(String schemaName) {
        String sql = "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = ?";
        int count = jdbcTemplate.queryForObject(sql, Integer.class, schemaName);
        return count > 0;
    }


}
