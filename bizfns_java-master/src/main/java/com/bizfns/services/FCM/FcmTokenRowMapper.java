package com.bizfns.services.FCM;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FcmTokenRowMapper implements RowMapper<FcmToken> {
    @Override
    public FcmToken mapRow(ResultSet rs, int rowNum) throws SQLException {
        FcmToken fcmToken = new FcmToken();
        fcmToken.setTokenId(rs.getInt("TOKEN_ID"));
        fcmToken.setUserId(rs.getInt("USER_ID"));
        fcmToken.setDeviceId(rs.getString("DEVICE_ID"));
        fcmToken.setFcmToken(rs.getString("FCM_TOKEN"));
        fcmToken.setCreatedAt(rs.getTimestamp("CREATED_AT"));
        fcmToken.setUpdatedAt(rs.getTimestamp("UPDATED_AT"));
        return fcmToken;
    }
}
