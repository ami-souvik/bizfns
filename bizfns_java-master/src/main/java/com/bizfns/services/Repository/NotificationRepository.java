package com.bizfns.services.Repository;

import com.bizfns.services.Exceptions.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;


    public List<Map<String, Object>> getNotificationQuery(int offset, int limit) {

        String sql = "SELECT \"notification_id\", TO_CHAR(\"CREATED_DATE\", 'YYYY-MM-DD HH24:MI:SS') AS \"CREATED_DATE\", " +
                "\"MESSAGE\", \"BUSINESS_NAME\", \"SCHEMA_ID\", COALESCE(\"IS_READ\", 'false') AS \"IS_READ\" " +
                "FROM \"Bizfns\".\"NOTIFICATION_MASTER\" " +
                "ORDER BY \"CREATED_DATE\" DESC LIMIT ? OFFSET ?";

        return jdbcTemplate.query(sql,new Object[]{limit, offset}, (resultSet, rowNum) -> {
            Map<String, Object> notification = new HashMap<>();
            notification.put("CREATED_DATE", resultSet.getString("CREATED_DATE"));
            notification.put("MESSAGE", resultSet.getString("MESSAGE"));
            notification.put("BUSINESS_NAME", resultSet.getString("BUSINESS_NAME"));
            notification.put("SCHEMA_ID", resultSet.getString("SCHEMA_ID"));
            notification.put("NOTIFICATION_ID", resultSet.getString("notification_id"));
            notification.put("IS_READ",resultSet.getString("IS_READ"));
            return notification;
        });

    }

    public void updateNotificationRead(int notificationID) throws DataAccessException, CustomException {

        if (!notificationExists(notificationID)) {
            throw new CustomException("Notification with ID " + notificationID + " not found.");
        }
        try {
            String strUpdateService = "UPDATE \"Bizfns\".\"NOTIFICATION_MASTER\" SET \"IS_READ\" = \'true\'" +
                    " where \"notification_id\" = " + notificationID + "\n";
            jdbcTemplate.update(strUpdateService);
        } catch (DataAccessException e) {
            throw e;
        }
    }

    private boolean notificationExists(int notificationID) {
        String sql = "SELECT COUNT(*) FROM \"Bizfns\".\"NOTIFICATION_MASTER\" WHERE \"notification_id\" = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, notificationID);
        return count != null && count > 0;
    }

    public void registrationMessageQuery(String phoneNumber, String tenantId, String message, String moduleType) {
        String sql = "SELECT \"BUSINESS_NAME\", \"COMPANY_ID\" " +
                "FROM \"Bizfns\".\"COMPANY_MASTER\" " +
                "WHERE (\"COMPANY_BACKUP_PHONE_NUMBER\" = ? OR \"COMPANY_BACKUP_EMAIL\" = ?) " +
                "AND \"SCHEMA_ID\" = ?";

        Map<String, Object> result = jdbcTemplate.queryForMap(sql, phoneNumber, phoneNumber, tenantId);
        String businessName = (String) result.get("BUSINESS_NAME");
        int companyId = (Integer) result.get("COMPANY_ID");

        String notificationQuery = "INSERT INTO \"Bizfns\".\"NOTIFICATION_MASTER\" " +
                "(\"MESSAGE\", \"BUSINESS_NAME\", \"SCHEMA_ID\", \"CREATED_DATE\", \"FK_COMPANY_ID\", \"MODULE_TYPE\") " +
                "VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?, ?)";

        jdbcTemplate.update(notificationQuery, message, businessName, tenantId,companyId, moduleType);
    }
}