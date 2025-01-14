package com.bizfns.services.Repository;

import com.bizfns.services.Entity.Userinfo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class UserInfoRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserInfoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String findByUsername(String userId,String schemaID) {


        String sql = "SELECT um.\"USER_TYPE\"  FROM \"Bizfns\".\"USER_MASTER\" um where um.\"MOBILE_NUMBER\" = ? and um.\"SCHEMA_NAME\"= ?";

        try {
            List<String> userTypes = jdbcTemplate.queryForList(sql, new Object[]{userId, schemaID}, String.class);

            return userTypes.get(0);
        }catch (Exception e){
            return "staff";
        }
    }

//    public void save(UserInfo userInfo) {
//        String sql = "INSERT INTO users (username, password, roles) VALUES (?, ?, ?)";
//        jdbcTemplate.update(sql, userInfo.getUsername(), userInfo.getPassword(), userInfo.getRoles());
//    }
}