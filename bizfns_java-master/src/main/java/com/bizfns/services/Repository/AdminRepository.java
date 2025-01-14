package com.bizfns.services.Repository;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;


    public String dataForPasswordValidationFromAdminMaster(String userId) {
        String sql = "select \"PASSWORD\" from \"Bizfns\".\"ADMIN_MASTER\" where \"USER_ID\" = ? or \"ADMIN_EMAIL\" = ?";
        // Using jdbcTemplate.queryForObject to fetch a single value from the database
        // The second argument to jdbcTemplate.queryForObject() is an array of values to replace the placeholders in the SQL query
        try {
            String s = jdbcTemplate.queryForObject(sql, new Object[]{userId, userId}, String.class);
            System.err.println("password  :"+s);
            return s;
        } catch (Exception e) {
            // Handle exceptions or return null if no result is found
            return null;
        }
    }

    public boolean isUserTypeAdmin(String userId) {
        String sql = "SELECT COUNT(*) FROM \"Bizfns\".\"ADMIN_MASTER\" WHERE \"USER_ID\" = ? OR \"ADMIN_EMAIL\" = ? ";

        int count = jdbcTemplate.queryForObject(sql, Integer.class, userId,userId);

        // If count > 0, it means user exists in ADMIN_MASTER table, hence userType is admin
        return count > 0;
    }

    public List<JSONObject> checkOtpExistence(String userId) {
        String sql = "SELECT \"USER_TYPE\"\n" +
                "FROM \"Bizfns\".\"USER_OTP\"\n" +
                "WHERE \"FK_USER_ID\" = (\n" +
                "    SELECT \"COMPANY_ID\" \n" +
                "    FROM \"Bizfns\".\"COMPANY_MASTER\"\n" +
                "    WHERE \"COMPANY_BACKUP_PHONE_NUMBER\" = ? OR \"COMPANY_BACKUP_EMAIL\" = ? \n" +
                ") \n" +
                "AND \"USER_TYPE\" = (\n" +
                "    SELECT CAST(\"PK_USER_TYPE_ID\" AS VARCHAR) AS \"PK_USER_TYPE_ID_STRING\"\n" +
                "    FROM \"Bizfns\".\"USER_TYPE_MASTER\"\n" +
                "    WHERE LOWER(\"USER_TYPE_MASTER_ENTITY\") = 'admin'\n" +
                ") \n" +
                "AND DATE_TRUNC('day', \"OTP_CREATED_AT\") = DATE_TRUNC('day', CURRENT_DATE)";

        try {
            return jdbcTemplate.query(sql, new Object[]{userId, userId}, (rs, rowNum) -> {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("USER_TYPE", rs.getString("USER_TYPE"));
                return jsonObject;
            });
        } catch (Exception e) {
            // Handle exceptions or return null if any error occurs during database access
            return null;
        }
    }

    public String insertFirstLoginOtp(String randomNumber, String userId, String tenantId) {
        String sql = "INSERT INTO \"Bizfns\".\"USER_OTP\" (\"FK_USER_ID\", \"USER_TYPE\", \"OTP\", \"OTP_CREATED_AT\", \"OTP_COUNT\", \"SCHEMA_ID\") " +
                "VALUES ((SELECT \"COMPANY_ID\" FROM \"Bizfns\".\"COMPANY_MASTER\" WHERE \"COMPANY_BACKUP_PHONE_NUMBER\" = ? OR \"COMPANY_BACKUP_EMAIL\" = ?), " +
                "(SELECT CAST(\"PK_USER_TYPE_ID\" AS VARCHAR) AS \"PK_USER_TYPE_ID_STRING\" FROM \"Bizfns\".\"USER_TYPE_MASTER\" WHERE LOWER(\"USER_TYPE_MASTER_ENTITY\") = 'admin'), " +
                "?, current_timestamp, '0', ?) " +
                "RETURNING EXTRACT(EPOCH FROM \"OTP_UPDATED_AT\") * 1000 AS \"OTP_UPDATED_AT_MS\"";

        try {
            // Execute the INSERT statement using jdbcTemplate's update method
            return jdbcTemplate.queryForObject(sql, new Object[]{userId, userId, randomNumber, tenantId}, String.class);
        } catch (Exception e) {
            // Handle exceptions or return null if any error occurs during database access
            return null;
        }
    }

    public String updateLoginOtp(String randomNumber, String userId) {
        String sql = "UPDATE \"Bizfns\".\"USER_OTP\" " +
                "SET \"OTP\" = ?, " +
                "    \"OTP_UPDATED_AT\" = current_timestamp, " +
                "    \"OTP_COUNT\" = \"OTP_COUNT\" + 1 " +
                "WHERE \"FK_USER_ID\" = ( " +
                "    SELECT \"COMPANY_ID\" " +
                "    FROM \"Bizfns\".\"COMPANY_MASTER\" " +
                "    WHERE \"COMPANY_BACKUP_PHONE_NUMBER\" = ? or \"COMPANY_BACKUP_EMAIL\" = ? " +
                ") " +
                "AND \"USER_TYPE\" = (SELECT CAST(\"PK_USER_TYPE_ID\" AS VARCHAR) AS \"PK_USER_TYPE_ID_STRING\" " +
                "FROM \"Bizfns\".\"USER_TYPE_MASTER\" " +
                "WHERE LOWER(\"USER_TYPE_MASTER_ENTITY\") = 'admin') " +
                "AND DATE_TRUNC('day', \"OTP_CREATED_AT\") = DATE_TRUNC('day', CURRENT_DATE) " +
                "RETURNING EXTRACT(EPOCH FROM \"OTP_UPDATED_AT\") * 1000 AS \"OTP_UPDATED_AT_MS\"";

        try {
            // Execute the UPDATE statement using jdbcTemplate's update method
            return jdbcTemplate.queryForObject(sql, new Object[]{randomNumber, userId, userId}, String.class);
        } catch (Exception e) {
            // Handle exceptions or return null if any error occurs during database access
            return null;
        }
    }

    public String isOtpExpire(String userId) {
        String sql = "SELECT CASE\n" +
                "    WHEN EXISTS (\n" +
                "        SELECT 1\n" +
                "        FROM \"Bizfns\".\"USER_OTP\"\n" +
                "        WHERE \"FK_USER_ID\" =(\n" +
                "            SELECT \"COMPANY_ID\" \n" +
                "            FROM \"Bizfns\".\"COMPANY_MASTER\"\n" +
                "            WHERE \"COMPANY_BACKUP_PHONE_NUMBER\" = ? OR \"COMPANY_BACKUP_EMAIL\" = ?\n" +
                "        )\n" +
                "        AND \"USER_TYPE\" = (\n" +
                "            SELECT CAST(\"PK_USER_TYPE_ID\" AS VARCHAR) AS \"PK_USER_TYPE_ID_STRING\"\n" +
                "            FROM \"Bizfns\".\"USER_TYPE_MASTER\"\n" +
                "            WHERE LOWER(\"USER_TYPE_MASTER_ENTITY\") = 'admin'\n" +
                "        ) \n" +
                "        AND DATE_TRUNC('day', \"OTP_CREATED_AT\") = DATE_TRUNC('day', CURRENT_DATE)\n" +
                "        AND \"OTP_UPDATED_AT\" >= NOW() - INTERVAL '2 minutes'\n" +
                "    )\n" +
                "    THEN 'y'\n" +
                "    ELSE 'n'\n" +
                "end";
        return jdbcTemplate.queryForObject(sql, new Object[]{userId, userId}, String.class);
    }

    public String fetchOtp(String userId) {
        String sql = "SELECT \"OTP\" " +
                "FROM \"Bizfns\".\"USER_OTP\" " +
                "WHERE \"FK_USER_ID\" = ( " +
                "    SELECT \"COMPANY_ID\" " +
                "    FROM \"Bizfns\".\"COMPANY_MASTER\" " +
                "    WHERE \"COMPANY_BACKUP_PHONE_NUMBER\" = ? OR \"COMPANY_BACKUP_EMAIL\" = ? " +
                ") " +
                "AND \"USER_TYPE\" = ( " +
                "    SELECT CAST(\"PK_USER_TYPE_ID\" AS VARCHAR) AS \"PK_USER_TYPE_ID_STRING\" " +
                "    FROM \"Bizfns\".\"USER_TYPE_MASTER\" " +
                "    WHERE LOWER(\"USER_TYPE_MASTER_ENTITY\") = 'admin' " +
                ") " +
                "AND DATE_TRUNC('day', \"OTP_CREATED_AT\") = DATE_TRUNC('day', CURRENT_DATE)";
        try {
            // Provide userId parameter twice for placeholders in the SQL query
            return jdbcTemplate.queryForObject(sql, String.class, userId, userId);
        } catch (Exception e) {
            // Handle exceptions or return null if any error occurs during database access
            return null;
        }
    }

    public String checkOtpExistenceForPassword(String userId) {
        String sql = "SELECT \"OTP\" " +
                "FROM \"Bizfns\".\"USER_OTP\" " +
                "WHERE \"FK_USER_ID\" = ( " +
                "    SELECT \"COMPANY_ID\" " +
                "    FROM \"Bizfns\".\"COMPANY_MASTER\" " +
                "    WHERE \"COMPANY_BACKUP_PHONE_NUMBER\" = ? OR \"COMPANY_BACKUP_EMAIL\" = ? " +
                ") " +
                "AND \"USER_TYPE\" = ( " +
                "    SELECT CAST(\"PK_USER_TYPE_ID\" AS VARCHAR) AS \"PK_USER_TYPE_ID_STRING\" " +
                "    FROM \"Bizfns\".\"USER_TYPE_MASTER\" " +
                "    WHERE LOWER(\"USER_TYPE_MASTER_ENTITY\") = 'admin' " +
                ") " +
                "AND DATE_TRUNC('day', \"OTP_CREATED_AT\") = DATE_TRUNC('day', CURRENT_DATE)";

        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{userId, userId}, String.class);
        } catch (Exception e) {
            return null; // Handle appropriately, e.g., throw a custom exception
        }
    }

    public void updatePasswordAdmin(String userId, String encrypt) {
        String sql = "UPDATE \"Bizfns\".\"ADMIN_MASTER\" " +
                "SET \"PASSWORD\" = ? " +
                "WHERE \"USER_ID\" = ?";

        try {
            jdbcTemplate.update(sql, encrypt, userId);
        } catch (Exception e) {
            // Handle the exception appropriately
            e.printStackTrace();
        }
    }
}
