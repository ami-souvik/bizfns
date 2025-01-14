package com.bizfns.services.FCM;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class FcmTokenRepositoryImpl implements FcmTokenRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void saveFcmToken(FcmToken fcmToken) {
        String sql = "INSERT INTO \"Bizfns\".\"FCM_TOKEN\" (\"USER_ID\", \"DEVICE_ID\", \"FCM_TOKEN\") VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, fcmToken.getUserId(), fcmToken.getDeviceId(), fcmToken.getFcmToken());
    }

    @Override
    public void deleteByUserId(int userId) {
        String sql = "DELETE FROM \"Bizfns\".\"FCM_TOKEN\" WHERE \"USER_ID\" = ?";
        jdbcTemplate.update(sql, userId);
    }

    @Override
    public FcmToken findByUserId(int userId) {
        String sql = "SELECT * FROM \"Bizfns\".\"FCM_TOKEN\" WHERE \"USER_ID\" = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{userId}, new FcmTokenRowMapper());
    }
}
