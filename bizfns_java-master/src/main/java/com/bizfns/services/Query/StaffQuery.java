package com.bizfns.services.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class StaffQuery {
    @Autowired
    private JdbcTemplate jdbcTemplate;


    public List<Map<String, Object>> recordForStaffList(String tenantId) {

        String strQueryStaffList = "select \"PK_USER_ID\",\n" +
                "\"FK_COMPANY_ID\",\n" +
                "\"USER_FIRST_NAME\",\n" +
                "\"USER_LAST_NAME\",\n" +
                "\"USER_EMAIL\",\n" +
                "\"FK_USER_TYPE_ID\",\n" +
                "\"USER_JOINING_DATE\",\n" +
                "\"USER_PHONE_NUMBER\",\n" +
                "\"USER_STATUS\"\n" +
                " from \"" + tenantId + "\".\"company_user\"" +
                "ORDER BY \"PK_USER_ID\" DESC";
        List<Map<String, Object>> recordForStaff = jdbcTemplate.queryForList(strQueryStaffList);
        return recordForStaff;
    }


    public String staffOtpExistence(String tenantId, String userId) {


        String strOtpExistence = "select \"OTP\" from \"" + tenantId + "\".\"customer_otp\" where \"FK_CUSTOMER_ID\" = (select \"PK_USER_ID\" from \"" + tenantId + "\".\"company_user\" where \n" +
                "\"USER_EMAIL\" = ? or \n" +
                "\"USER_PHONE_NUMBER\" = ?) AND\n" +
                "\"USER_TYPE\" = (select \"PK_USER_TYPE_ID\" from \"Bizfns\".\"USER_TYPE_MASTER\" where \n" +
                "lower(\"USER_TYPE_MASTER_ENTITY\") =lower('STAFF'))  and DATE_TRUNC('day', \"OTP_CREATED_AT\") = DATE_TRUNC('day', CURRENT_DATE) ";
        List<Map<String, Object>> recordForPhStaffOtp = jdbcTemplate.queryForList(strOtpExistence, userId, userId);

        String otpValue = null;
        if (!recordForPhStaffOtp.isEmpty()) {
            Map<String, Object> firstRecord = recordForPhStaffOtp.get(0);
            otpValue = (String) firstRecord.get("OTP");

        }
        return otpValue;

    }


    public void insertFirstStaffOtp(String tenantId, String userId, String strRandomNo) {

        String queryForStaffOtpInsertion = "INSERT INTO \"" + tenantId + "\".\"customer_otp\" (\n" +
                "    \"PK_OTP_ID\",\n" +
                "    \"FK_CUSTOMER_ID\",\n" +
                "    \"OTP\",\n" +
                "    \"OTP_CREATED_AT\",\n" +
                "    \"OTP_UPDATED_AT\",\n" +
                "    \"OTP_STATUS\",\n" +
                "    \"USER_TYPE\",\n" +
                "    \"OTP_COUNT\"\n" +
                ") VALUES (\n" +
                "    (SELECT COALESCE((SELECT MAX(\"PK_OTP_ID\") FROM   \"" + tenantId + "\".\"customer_otp\"  ), 0   ) + 1),   \n" +
                "    (select \"PK_USER_ID\" from \"" + tenantId + "\".\"company_user\" where \n" +
                "\"USER_EMAIL\" = '" + userId + "' or \n" +
                "\"USER_PHONE_NUMBER\" = '" + userId + "'),       \n" +
                "    '" + strRandomNo + "',    \n" +
                "    current_timestamp,  \n" +
                "    current_timestamp,  \n" +
                "    1,      \n" +
                "    (select \"PK_USER_TYPE_ID\" from \"Bizfns\".\"USER_TYPE_MASTER\" where \n" +
                "lower(\"USER_TYPE_MASTER_ENTITY\") =lower('STAFF')),     \n" +
                "    0        \n" +
                ")";

        jdbcTemplate.update(queryForStaffOtpInsertion);


    }


    public void updateStaffOtp(String tenantId, String userId, String strRandomNo) {

        String strUpdateStaffOtp = "update\n" +
                "\"" + tenantId + "\".\"customer_otp\" \n" +
                "set \n" +
                "\"OTP\" = '" + strRandomNo + "',\n" +
                "\"OTP_UPDATED_AT\"=current_timestamp,\n" +
                "\"OTP_COUNT\" = \"OTP_COUNT\" + 1 \n" +
                "where \n" +
                "\"FK_CUSTOMER_ID\"= (select \"PK_USER_ID\" from \"" + tenantId + "\".\"company_user\" where \n" +
                "                            \"USER_EMAIL\" = '" + userId + "' or \n" +
                "                            \"USER_PHONE_NUMBER\" = '" + userId + "') \n" +
                "and\n" +
                "\"USER_TYPE\" =(select \"PK_USER_TYPE_ID\" from \"Bizfns\".\"USER_TYPE_MASTER\" where\n" +
                "                           lower(\"USER_TYPE_MASTER_ENTITY\")=lower('STAFF')) \n" +
                "AND \n" +
                "DATE_TRUNC('day', \"OTP_CREATED_AT\") = DATE_TRUNC('day', CURRENT_DATE)";

        //System.out.println(strUpdateStaffOtp);
        jdbcTemplate.update(strUpdateStaffOtp);
    }


    public String checkStaffToken(String tenantId, String userId) {
        String strCheckStaffToken = "SELECT \n" +
                "    CASE \n" +
                "        WHEN \"USER_TOKEN\" IS NULL OR \"USER_TOKEN\" = '' THEN 'n'\n" +
                "        ELSE 'y'\n" +
                "    END AS result\n" +
                "FROM\n" +
                "    \"" + tenantId + "\".\"company_user\"\n" +
                "WHERE\n" +
                "    \"USER_EMAIL\" = '" + userId + "' OR \"USER_PHONE_NUMBER\" = '" + userId + "'";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(strCheckStaffToken);
        String tokenStatus = null;
        if (!results.isEmpty()) {
            Map<String, Object> row = results.get(0);
            tokenStatus = (String) row.get("result");
            // Now you have the token status as a String ("n" or "y")
            // You can use the tokenStatus variable for further processing.
        }
        return tokenStatus;
    }


    public void updateStaffToken(String tenantId, String userId, String token) {

        String strInsertStaffToken = "update  \"" + tenantId + "\".\"company_user\" set \"USER_TOKEN\" ='" + token + "' , \"USER_UPDATED_AT\" =current_timestamp  where  \"USER_EMAIL\" = '" + userId + "'  or \n" +
                "\"USER_PHONE_NUMBER\" = '" + userId + "' ";
        jdbcTemplate.update(strInsertStaffToken);
    }

    public String staffTokenValidation(String tenantId, String userId) {
        String strStaffTokenValidation = "SELECT \n" +
                "    CASE \n" +
                "        WHEN \"USER_UPDATED_AT\" <= NOW() - INTERVAL '11 HOURS' THEN 'n'\n" +
                "        ELSE 'y'\n" +
                "    END AS RESULT\n" +
                "FROM \n" +
                "    \"" + tenantId + "\".\"company_user\"   where  \"USER_EMAIL\" =  '" + userId + "'  or \n" +
                "\"USER_PHONE_NUMBER\" =  '" + userId + "'";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(strStaffTokenValidation);


        String strStaffToken = null;

        if (!result.isEmpty()) {
            Map<String, Object> firstRow = result.get(0);
            strStaffToken = (String) firstRow.get("RESULT");
        }
        return strStaffToken;


    }


    public String staffDbPassword(String userId, String y) {
        String tenantFirstEightTLetters = null;

        if (y != null && y.length() >= 8) {
            tenantFirstEightTLetters = y.substring(0, 8);
        }

        try {

            String strStaffTokenValidation = "select \"USER_PASSWORD\" from \"" + tenantFirstEightTLetters + "\".\"company_user\" where \"USER_EMAIL\" = ? or \"USER_PHONE_NUMBER\" = ?";
            //System.out.println("SQL Query: " + strStaffTokenValidation);

            List<Map<String, Object>> result = jdbcTemplate.queryForList(strStaffTokenValidation, userId, userId);

            String a = null;

            if (!result.isEmpty()) {
                Map<String, Object> firstRow = result.get(0);
                a = (String) firstRow.get("USER_PASSWORD");
            }

            return a;
        } catch (Exception e) {
            // Properly handle and log exceptions here
            e.printStackTrace();
            return null; // or return an error response
        }
    }


    public Integer saveServiceRateData(String userId, String tenantId, String serviceName, Integer rate, Integer rateUnit) {
        String activeStatus="1";
        String strSaveServiceRateData =
                "INSERT INTO \"" + tenantId + "\".\"business_type_wise_service_master\" " +
                        "(\"ID\", \"BUSINESS_TYPE_ID\", \"SERVICE_NAME\", \"RATE\", \"RATE_UNIT\", \"STATUS\") " +
                        "VALUES " +
                        "((SELECT COALESCE((SELECT MAX(\"ID\") FROM \"" + tenantId + "\".\"business_type_wise_service_master\"), 0) + 1), " +
                        "(SELECT \"FK_BUSINESS_TYPE_ID\" FROM \"Bizfns\".\"COMPANY_BUSINESS_TYPE_MAPPING\" " +
                        "WHERE \"FK_COMPANY_ID\" = " +
                        "(SELECT \"COMPANY_ID\" FROM \"Bizfns\".\"COMPANY_MASTER\" " +
                        "WHERE (\"COMPANY_BACKUP_EMAIL\" = ? OR \"COMPANY_BACKUP_PHONE_NUMBER\" = ?) AND \"SCHEMA_ID\" = ?)), ?, ?, ?,?) " +
                        "RETURNING \"ID\"";

        Object[] params = new Object[]{userId, userId, tenantId, serviceName, rate, rateUnit,activeStatus};
        Integer pkJobId = jdbcTemplate.queryForObject(strSaveServiceRateData, params, Integer.class);

        return pkJobId;
    }

    public String getStaffMailId(String userId, String tenantFirstEightTLetters) {

        String strGetStaffMailId = "select \"USER_EMAIL\" from    \"" + tenantFirstEightTLetters + "\".\"company_user\"  where \"USER_EMAIL\" = '"+userId+"' or \"USER_PHONE_NUMBER\" = '"+userId+"'  \n";
       // System.out.println("SQL Query: " + strGetStaffMailId);

        List<Map<String, Object>> result = jdbcTemplate.queryForList(strGetStaffMailId);

        String mail = null;

        if (!result.isEmpty()) {
            Map<String, Object> firstRow = result.get(0);
            mail = (String) firstRow.get("USER_EMAIL");
        }
        return  mail;
    }

    public String userCount(String tenantId, String companyId) {

        String strUserCountQuery = "   WITH UserCount AS (\n" +
                "    SELECT COUNT(*) AS total_count\n" +
                "    FROM    \""+tenantId+"\".\"company_user\"  \n" +
                "),\n" +
                "SubscriptionInfo AS (\n" +
                "    SELECT \"SUBSCRIPTION_USER_LIMIT\"\n" +
                "    FROM \"Bizfns\".\"SUBSCRIPTION_PLAN_MASTER\"\n" +
                "    WHERE \"PK_SUBSCRIPTION_PLAN_ID\" = (\n" +
                "        SELECT \"FK_SUBSCRIPTION_PLAN_ID\"\n" +
                "        FROM \"Bizfns\".\"COMPANY_SUBSCRIPTION\"\n" +
                "        WHERE \"FK_COMPANY_BUSINESS_MAPPING_ID\" = (\n" +
                "            SELECT \"PK_COMPANY_BUSINESS_MAPPING_ID\"\n" +
                "            FROM \"Bizfns\".\"COMPANY_BUSINESS_TYPE_MAPPING\"\n" +
                "            WHERE \"FK_COMPANY_ID\" = "+companyId+" \n" +
                "        )\n" +
                "    )\n" +
                ")\n" +
                "SELECT\n" +
                "    CASE\n" +
                "        WHEN (SELECT total_count FROM UserCount) >= (SELECT \"SUBSCRIPTION_USER_LIMIT\" FROM SubscriptionInfo)\n" +
                "            THEN 'n'\n" +
                "        ELSE 'y'\n" +
                "    END AS comparison_result ";
        //System.out.println("SQL Query: " + strUserCountQuery);

        List<Map<String, Object>> result = jdbcTemplate.queryForList(strUserCountQuery);

        String checkUserCountQuery = null;

        if (!result.isEmpty()) {
            Map<String, Object> firstRow = result.get(0);
            checkUserCountQuery = (String) firstRow.get("comparison_result");
        }
        return  checkUserCountQuery;


    }

    public List<Map<String,Object>>  checkOtpExistenceForStaff(String userId, String tenantId) {

        String OtpExistence = "SELECT uo.\"USER_TYPE\" " +
                "FROM \"" + tenantId + "\".customer_otp uo " +
                "WHERE uo.\"FK_CUSTOMER_ID\" = ( " +
                "    SELECT cu.\"FK_COMPANY_ID\" " +
                "    FROM \""+tenantId+"\".\"company_user\" cu " +
                "    WHERE cu.\"USER_PHONE_NUMBER\" = ? " +
                ") " +
                "AND uo.\"USER_TYPE\" = '2' " +
                "AND DATE_TRUNC('day', uo.\"OTP_CREATED_AT\") = DATE_TRUNC('day', CURRENT_DATE)";


        List<Map<String, Object>> result = jdbcTemplate.queryForList(OtpExistence,userId);

        return result;

    }

    public int userTypeStaff(String userId, String tenantId) {
        String userTypeQuery = "SELECT uo.\"USER_TYPE\" " +
                "FROM \"" + tenantId + "\".customer_otp uo " +
                "WHERE uo.\"FK_CUSTOMER_ID\" = ( " +
                "    SELECT cu.\"FK_COMPANY_ID\" " +
                "    FROM \"" + tenantId + "\".company_user cu " +
                "    WHERE cu.\"USER_PHONE_NUMBER\" = ? " +
                " ) " +
                "AND uo.\"USER_TYPE\" = 2 " +
                "AND DATE_TRUNC('day', uo.\"OTP_CREATED_AT\") = DATE_TRUNC('day', CURRENT_DATE)";


        List<Map<String, Object>> result = jdbcTemplate.queryForList(userTypeQuery, userId);

        if (!result.isEmpty()) {
            Map<String, Object> firstRow = result.get(0);
            return (Integer) firstRow.get("USER_TYPE");
        }

        return 0;
    }

    public String fetchOtpForStaff(String userId, String tenantId) {

        String otp = "SELECT uo.\"OTP\" " +
                "FROM \""+tenantId+"\".\"customer_otp\" uo " +
                "WHERE uo.\"FK_CUSTOMER_ID\" = ( " +
                "    SELECT cu.\"FK_COMPANY_ID\" " +
                "    FROM \""+tenantId+"\".\"company_user\" cu " +
                "    WHERE cu.\"USER_PHONE_NUMBER\" = ? " +
                ") " +
                "AND uo.\"USER_TYPE\" = 2 " +
                "AND DATE_TRUNC('day', uo.\"OTP_CREATED_AT\") = DATE_TRUNC('day', CURRENT_DATE)";


        List<Map<String, Object>> result = jdbcTemplate.queryForList(otp,userId);

        String res = null;

        if (!result.isEmpty()) {
            Map<String, Object> firstRow = result.get(0);
            res = (String) firstRow.get("OTP");
        }
        return  res;
    }

    public Map<String, Object> fetchStaffUserData(String userId, String tenantId) {

        String staffData = "SELECT * FROM \"Bizfns\".\"COMPANY_MASTER\" cm " +
                "WHERE cm.\"COMPANY_ID\" = (" +
                "    SELECT cu.\"FK_COMPANY_ID\"" +
                "    FROM \""+tenantId+"\".\"company_user\" cu " +
                "    WHERE cu.\"USER_PHONE_NUMBER\" = ?" +
                ")";


        Map<String, Object> result = jdbcTemplate.queryForMap(staffData,userId);
//        String res = null;
//        Map<String, Object> userMap = new HashMap<>();
//        if (!result.isEmpty()) {
//            Map<String, Object> firstRow = result.get(0);
//            userMap.put("COMPANY_ID", firstRow.get("COMPANY_ID"));
//            userMap.put("BUSINESS_NAME", firstRow.get("BUSINESS_NAME"));
//            userMap.put("COMPANY_BACKUP_EMAIL", firstRow.get("COMPANY_BACKUP_EMAIL"));
//            userMap.put("COMPANY_BACKUP_PHONE_NUMBER", firstRow.get("COMPANY_BACKUP_PHONE_NUMBER"));
//            userMap.put("tenantId", firstRow.get("SCHEMA_ID"));
//            userMap.put("logoAddress", firstRow.get("COMPANY_LOGO"));
//        }
        return  result;
    }

    public String insertFirstLoginOtpForStaff(String randomNumber, String userId, String tenantId, int userType) {
        String sql = "INSERT INTO \"" + tenantId + "\".\"customer_otp\" (\"PK_OTP_ID\", \"FK_CUSTOMER_ID\", \"USER_TYPE\", \"OTP\", \"OTP_CREATED_AT\", \"OTP_UPDATED_AT\", \"OTP_COUNT\") " +
                "VALUES (" +
                "COALESCE((SELECT MAX(\"PK_OTP_ID\") + '1' FROM \"" + tenantId + "\".\"customer_otp\"), '1')," +
                "(SELECT \"FK_COMPANY_ID\" FROM \"" + tenantId + "\".\"company_user\" WHERE \"USER_PHONE_NUMBER\" = ? or \"USER_EMAIL\" = ?), " +
                "?, ?, current_timestamp, current_timestamp, '0') " +
                "RETURNING EXTRACT(EPOCH FROM \"OTP_UPDATED_AT\") * 1000 AS \"OTP_UPDATED_AT_MS\"";

        // Execute the query
        return jdbcTemplate.queryForObject(
                sql,
                new Object[]{userId, userId, userType, randomNumber},
                String.class
        );
    }


    public String updateLoginOtpForStaff(String randomNumber, String userId, String userType, String tenantId) {
        String sql = "UPDATE \""+tenantId+"\".\"customer_otp\" " +
                "SET \"OTP\" = ?, " +
                "    \"OTP_UPDATED_AT\" = current_timestamp, " +
                "    \"OTP_COUNT\" = \"OTP_COUNT\" + 1 " +
                "WHERE \"FK_CUSTOMER_ID\" = ( " +
                "    SELECT \"FK_COMPANY_ID\" " +
                "    FROM \""+tenantId+"\".\"company_user\" " +
                "    WHERE  \"USER_PHONE_NUMBER\" = ? or \"USER_EMAIL\" = ? " +
                ") " +
                "AND \"USER_TYPE\" = 2 " +
                "AND DATE_TRUNC('day', \"OTP_CREATED_AT\") = DATE_TRUNC('day', CURRENT_DATE) " +
                "RETURNING EXTRACT(EPOCH FROM \"OTP_UPDATED_AT\") * 1000 AS \"OTP_UPDATED_AT_MS\"";



        // Execute the update query using JdbcTemplate
        return jdbcTemplate.queryForObject(
                sql,
                new Object[]{randomNumber, userId, userId},
                String.class
        );
    }

    public String tokenDataStaff(String userId, String tenantId) {
        String sql = "SELECT CASE " +
                "    WHEN EXISTS ( " +
                "        SELECT 1 " +
                "        FROM \"Bizfns\".\"TOKEN_MASTER\" " +
                "        WHERE \"USER_ID\" = ( " +
                "            SELECT \"FK_COMPANY_ID\" " +
                "            FROM \""+tenantId+"\".\"company_user\" " +
                "            WHERE \"USER_PHONE_NUMBER\" = ? OR \"USER_EMAIL\" = ? " +
                "        ) " +
                "    ) " +
                "    THEN 'y' " +
                "    ELSE 'n' " +
                "END";


        // Execute the query using JdbcTemplate
        return jdbcTemplate.queryForObject(
                sql,
                new Object[]{userId, userId},
                String.class
        );
    }

    public String isPasswordChange(String userId, String tenantId) {

        String sql = "select cu.\"PASSWORD_CHANGE\"  \n" +
                "from "+tenantId+".company_user cu where \n" +
                "cu.\"USER_PHONE_NUMBER\" = ? \n" +
                "OR cu.\"USER_EMAIL\" = ?";



        try {
            return jdbcTemplate.queryForObject(
                    sql,
                    new Object[]{userId, userId},
                    String.class
            );
        }catch (Exception e){
            e.printStackTrace();
        }
        // Execute the query using JdbcTemplate

        return sql;
    }

    public void isertCompanyTokenForStaff(String userId, String tenantId, String token) {
        String sql = "INSERT INTO \"Bizfns\".\"TOKEN_MASTER\"\n" +
                "    (\"USER_ID\", \"SCHEMA_ID\", \"TOKEN\", \"CREATED_AT\", \"UPDATED_AT\")\n" +
                "VALUES\n" +
                "    (( \n" +
                "    SELECT \"FK_COMPANY_ID\" \n" +
                "    FROM \""+tenantId+"\".\"company_user\" \n" +
                "    WHERE  \"USER_PHONE_NUMBER\" = ? or \"USER_EMAIL\" = ? \n" +
                "), ?, ?, current_timestamp, current_timestamp)";



        //System.out.println("SQL Query: " + sql);

        // Execute the insert query using JdbcTemplate
        jdbcTemplate.update(
                sql,
                userId, userId, tenantId, token
        );
    }

    public String checkTokenValidationForStaff(String userId, String tenantId) {
        String sql = "SELECT\n" +
                "    CASE\n" +
                "        WHEN \"UPDATED_AT\" <= NOW() - INTERVAL '11 HOURS' THEN 'y'\n" +
                "        ELSE 'n'\n" +
                "    END AS y\n" +
                "FROM \"Bizfns\".\"TOKEN_MASTER\" " +
                "WHERE \"USER_ID\" = ( " +
                "    SELECT \"FK_COMPANY_ID\"\n" +
                "    FROM \""+tenantId+"\".\"company_user\"\n" +
                "    WHERE \"USER_PHONE_NUMBER\" = ? OR \"USER_EMAIL\" = ? " +
                ")";

        //System.out.println("SQL Query: " + sql);

        // Execute the query using JdbcTemplate
        return jdbcTemplate.queryForObject(
                sql,
                new Object[]{userId, userId},
                String.class
        );
    }

    public void updateCompanyTokenForStaff(String userId, String token, String tenantId) {
        String sql = "UPDATE \"Bizfns\".\"TOKEN_MASTER\"\n" +
                "SET \"TOKEN\" = ?,\n" +
                "    \"UPDATED_AT\" = current_timestamp\n" +
                "WHERE \"USER_ID\" = ( \n" +
                "    SELECT \"FK_COMPANY_ID\" \n" +
                "    FROM \""+tenantId+"\".\"company_user\" \n" +
                "    WHERE  \"USER_PHONE_NUMBER\" = ? or \"USER_EMAIL\" = ?\n" +
                ")";

        //System.out.println("SQL Query: " + sql);

        // Execute the update query using JdbcTemplate
        jdbcTemplate.update(
                sql,
                token, userId, userId
        );
    }

    public String tokenTextForStaff(String userId, String tenantId) {
        String tokenQuery = "SELECT \"TOKEN\" " +
                "FROM \"Bizfns\".\"TOKEN_MASTER\" " +
                "WHERE \"USER_ID\" = ( " +
                "    SELECT \"FK_COMPANY_ID\" " +
                "    FROM \""+tenantId+"\".\"company_user\" " +
                "    WHERE \"USER_PHONE_NUMBER\" = ? OR \"USER_EMAIL\" = ? " +
                ")";

        //System.out.println("SQL Query: " + tokenQuery);

        try {
            return jdbcTemplate.queryForObject(tokenQuery, String.class, userId, userId);
        } catch (Exception e) {
            //System.out.println("Error fetching token: " + e.getMessage());
            return null; // Handle the case where no token is found
        }
    }

    public String isOtpExpireForStaff(String userId, int userStaffType, String tenantId) {
        String sqlQuery = "SELECT CASE\n" +
                "    WHEN EXISTS (\n" +
                "        SELECT 1\n" +
                "        FROM \""+tenantId+"\".\"customer_otp\"\n" +
                "        WHERE \"FK_CUSTOMER_ID\" = (\n" +
                "            SELECT \"FK_COMPANY_ID\"\n" +
                "            FROM \""+tenantId+"\".\"company_user\"\n" +
                "            WHERE \"USER_PHONE_NUMBER\" = ? OR \"USER_EMAIL\" = ?\n" +
                "        )\n" +
                "        AND \"USER_TYPE\" = ?\n" +
                "        AND DATE_TRUNC('day', \"OTP_CREATED_AT\") = DATE_TRUNC('day', CURRENT_DATE)\n" +
                "        AND \"OTP_UPDATED_AT\" >= NOW() - INTERVAL '2 minutes'\n" +
                "    )\n" +
                "    THEN 'y'\n" +
                "    ELSE 'n'\n" +
                "END";

        //System.out.println("SQL Query: " + sqlQuery);

        // Execute the query and fetch the result
        Object[] queryParams = {userId, userId, userStaffType};
        String result = jdbcTemplate.queryForObject(sqlQuery, queryParams, String.class);

        return result;
    }

    public List<String> priviledgeChkForService(String tenantId) {
        try {
            String sql = "SELECT p.\"PREVILEDGE\" FROM \"Bizfns\".\"PRIVILEGE\" p, \"Bizfns\".\"PRIVILEGE_DTLS\" pd " +
                    "WHERE p.\"PK_PREVILEDGE_ID\" = pd.\"FK_PREVILEDGE_ID\" AND " +
                    "p.\"PREVILEDGE_TYPE\" = 'SERVICE' AND pd.\"TENANT_ID\" = ?";
            List<String> privileges = jdbcTemplate.queryForList(sql, new Object[]{tenantId}, String.class);
            return privileges;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<String> priviledgeChkForStaff(String tenantId) {
        try {
            String sql = "SELECT p.\"PREVILEDGE\" FROM \"Bizfns\".\"PRIVILEGE\" p, \"Bizfns\".\"PRIVILEGE_DTLS\" pd " +
                    "WHERE p.\"PK_PREVILEDGE_ID\" = pd.\"FK_PREVILEDGE_ID\" AND " +
                    "p.\"PREVILEDGE_TYPE\" = 'STAFF' AND pd.\"TENANT_ID\" = ?";
            List<String> privileges = jdbcTemplate.queryForList(sql, new Object[]{tenantId}, String.class);
            return privileges;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public Map<String, Object> getActiveStatusForStaff(String tenantId, String staffPhoneNumber) {
        try {
            String getActiveStatusQuery = "SELECT cu.\"USER_PHONE_NUMBER\", cu.\"USER_STATUS\" " +
                    "FROM \"" + tenantId + "\".\"company_user\" cu " +
                    "WHERE cu.\"USER_PHONE_NUMBER\" = ?";
            Map<String, Object> result = jdbcTemplate.queryForMap(getActiveStatusQuery, staffPhoneNumber);
            Map<String, Object> response = new HashMap<>();
            response.put("staffPhoneNumber", result.get("USER_PHONE_NUMBER"));
            response.put("activeStatus", result.get("USER_STATUS"));
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    public String updateActiveInactiveStatusForStaff(String tenantId, String staffPhoneNumber, String staffActiveInactiveStatus) {
        try {
            String updateStatusQuery = "UPDATE \"" + tenantId + "\".\"company_user\" " +
                    "SET \"USER_STATUS\" = ? " +
                    "WHERE \"USER_PHONE_NUMBER\" = ?";
            int rowsAffected = jdbcTemplate.update(updateStatusQuery, staffActiveInactiveStatus, staffPhoneNumber);
            if (rowsAffected > 0) {
                String statusMessage = staffActiveInactiveStatus.equals("1") ? "activated" : "deactivated";
                return "Staff Name has been " + statusMessage + " successfully";
            } else {
                return "Staff Name update failed";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error occurred while updating staff status";
        }
    }

    public void deletestaffFromDB(String tenantId, String staffPhoneNumber) {
        try {
            String strDeleteStaffFromDB1 = "DELETE FROM \"" + tenantId + "\".\"company_user\" WHERE \"USER_PHONE_NUMBER\" = ?";
            jdbcTemplate.update(strDeleteStaffFromDB1, staffPhoneNumber);

            String strDeleteStaffFromDB2 = "DELETE FROM \"Bizfns\".\"USER_MASTER\" WHERE \"MOBILE_NUMBER\" = ?";
            jdbcTemplate.update(strDeleteStaffFromDB2, staffPhoneNumber);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map<String, Object> getStaffDtlsFromDB(String tenantId, String staffPhoneNumber) {
        try {
            String getStaffDtlsFromDBQuery = "SELECT cu.\"USER_FIRST_NAME\" AS staffFirstName,\n" +
                    "cu.\"USER_LAST_NAME\" AS staffLastName,\n" +
                    "cu.\"PK_USER_ID\" AS staffId,\n" +
                    "cu.\"USER_EMAIL\" AS staffEmail,\n" +
                    "cu.\"USER_PHONE_NUMBER\" staffMobile,\n" +
                    "cu.\"FK_USER_TYPE_ID\" AS staffType,\n" +
                    "cu.\"FK_COMPANY_ID\" AS companyId,\n" +
                    "cu.\"USER_CHARGE_RATE\" AS chargeRate,\n" +
                    "cu.\"USER_CHARGE_FREQUENCY\" AS chargeFrequency,\n" +
                    "cu.\"USER_STATUS\" AS StaffActiveInactiveStatus\n" +
                    "FROM \"" + tenantId + "\".company_user cu WHERE cu.\"USER_PHONE_NUMBER\" = ?";

            Map<String, Object> result = jdbcTemplate.queryForMap(getStaffDtlsFromDBQuery, staffPhoneNumber);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean updateStaffDetailsInDB(String tenantId, Map<String, Object> request) {
        try {
            String staffLastName = (String) request.get("staffLastName");
            String staffPhoneNumber = (String) request.get("staffPhoneNumber");
            String staffId = (String) request.get("staffId");
            if (staffLastName == null || staffLastName.trim().isEmpty()) {
                throw new IllegalArgumentException("Staff last name cannot be null or empty");
            }
            if (staffPhoneNumber == null || staffPhoneNumber.trim().isEmpty()) {
                throw new IllegalArgumentException("Staff phone number cannot be null or empty");
            }
            String fetchPhoneNumberQuery = "SELECT \"USER_PHONE_NUMBER\" FROM \"" + tenantId + "\".company_user WHERE \"PK_USER_ID\" = ?";
            String currentPhoneNumber = jdbcTemplate.queryForObject(fetchPhoneNumberQuery, new Object[]{Integer.parseInt(staffId)}, String.class);
            if (currentPhoneNumber.equals(staffPhoneNumber)) {
                String updateStaffDetailsQuery = "UPDATE \"" + tenantId + "\".company_user SET " +
                        "\"USER_FIRST_NAME\" = ?, " +
                        "\"USER_LAST_NAME\" = ?, " +
                        "\"USER_EMAIL\" = ?, " +
                        "\"USER_PHONE_NUMBER\" = ?, " +
                        "\"FK_USER_TYPE_ID\" = ?, " +
                        "\"FK_COMPANY_ID\" = ?, " +
                        "\"USER_CHARGE_RATE\" = ?, " +
                        "\"USER_CHARGE_FREQUENCY\" = ?, " +
                        "\"USER_STATUS\" = ? " +
                        "WHERE \"PK_USER_ID\" = ?";
                int rowsAffected = jdbcTemplate.update(updateStaffDetailsQuery,
                        request.get("staffFirstName"),
                        staffLastName,
                        request.get("staffEmail"),
                        staffPhoneNumber,
                        Integer.parseInt((String) request.get("staffType")),
                        Integer.parseInt((String) request.get("companyId")),
                        Double.parseDouble((String) request.get("chargeRate")),
                        Integer.parseInt((String) request.get("chargeFrequency")),
                        request.get("StaffActiveStatus"),
                        Integer.parseInt(staffId));
                return rowsAffected > 0;
            } else {
                String updateUserMasterQuery = "UPDATE \"Bizfns\".\"USER_MASTER\" SET " +
                        "\"MOBILE_NUMBER\" = ? " +
                        "WHERE \"MOBILE_NUMBER\" = ?";
                jdbcTemplate.update(updateUserMasterQuery, staffPhoneNumber, currentPhoneNumber);
                String updateStaffDetailsQuery = "UPDATE \"" + tenantId + "\".company_user SET " +
                        "\"USER_FIRST_NAME\" = ?, " +
                        "\"USER_LAST_NAME\" = ?, " +
                        "\"USER_EMAIL\" = ?, " +
                        "\"USER_PHONE_NUMBER\" = ?, " +
                        "\"FK_USER_TYPE_ID\" = ?, " +
                        "\"FK_COMPANY_ID\" = ?, " +
                        "\"USER_CHARGE_RATE\" = ?, " +
                        "\"USER_CHARGE_FREQUENCY\" = ?, " +
                        "\"USER_STATUS\" = ? " +
                        "WHERE \"PK_USER_ID\" = ?";
                int rowsAffected = jdbcTemplate.update(updateStaffDetailsQuery,
                        request.get("staffFirstName"),
                        staffLastName,
                        request.get("staffEmail"),
                        staffPhoneNumber,
                        Integer.parseInt((String) request.get("staffType")),
                        Integer.parseInt((String) request.get("companyId")),
                        Double.parseDouble((String) request.get("chargeRate")),
                        Integer.parseInt((String) request.get("chargeFrequency")),
                        request.get("StaffActiveStatus"),
                        Integer.parseInt(staffId));
                return rowsAffected > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
