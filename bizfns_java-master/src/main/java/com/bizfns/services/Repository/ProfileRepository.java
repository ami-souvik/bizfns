package com.bizfns.services.Repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ProfileRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Map<String, Object> getProfileQuery1(String tenantId, String userId) {
        String sql = "SELECT " +
                "CM.\"COMPANY_ID\"," +
                "CM.\"BUSINESS_NAME\"," +
                "CM.\"COMPANY_BACKUP_EMAIL\"," +
                "CM.\"COMPANY_BACKUP_PHONE_NUMBER\"," +
                "CM.\"COMPANY_CREATED_AT\"," +
                "CM.\"COMPANY_LOGO\"," +
                "CM.\"BUSINESS_CONTACT_PERSON\"," +
                "CM.\"SCHEMA_ID\"," +
                "CM.\"TRUSTED_BACKUP_EMAIL\"," +
                "CM.\"COMPANY_STATUS\"," +
                "CM.\"TRUSTED_BACKUP_PHONE_NUMBER\"," +
                "B.\"BUSINESS_TYPE_ENTITY\" " +
                "FROM " +
                "\"Bizfns\".\"COMPANY_MASTER\" CM " +
                "LEFT JOIN \"Bizfns\".\"BUSINESS_TYPE_MASTER\" B " +
                "ON B.\"FK_CATEGORY_ID\" = CM.\"COMPANY_ID\" " +
                "WHERE " +
                " CM.\"SCHEMA_ID\" = ? " +
                "AND CM.\"COMPANY_BACKUP_PHONE_NUMBER\" = ? ";


        // Assuming jdbcTemplate is properly initialized in your class

        try{
            Map<String, Object> result = jdbcTemplate.queryForMap(sql, tenantId, userId);
            return result;
        }catch (Exception e){
            return new HashMap<>();
        }

    }

    public Map<String, Object> getProfileQuery2(int companyId) {

        try {


            String sql = "SELECT   \n" +
                    "    CS.\"COMPANY_SUBSCRIPTION_END_DATE\",\n" +
                    "    CS.\"COMPANY_SUBSCRIPTION_START_DATE\",\n" +
                    "    CS.\"COMPANY_SUBSCRIPTION_CATEGORY_DESCRIPTION\",\n" +
                    "    CS.\"FK_SUBSCRIPTION_PLAN_ID\",\n" +
                    "    CS.\"PK_COMPANY_SUBSCRIPTION_ID\"\n" +
                    "FROM  \n" +
                    " \n" +
                    "    \"Bizfns\".\"COMPANY_BUSINESS_TYPE_MAPPING\" CBTM \n" +
                    "JOIN \n" +
                    "    \"Bizfns\".\"COMPANY_SUBSCRIPTION\" CS ON CS.\"FK_COMPANY_BUSINESS_MAPPING_ID\" =\n" +
                    "   CBTM.\"PK_COMPANY_BUSINESS_MAPPING_ID\"  where cbtm.\"FK_COMPANY_ID\" = ? ";

            Map<String, Object> result = jdbcTemplate.queryForMap(sql, companyId);
            return result;
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    public List<Map<String, Object>> getProfileQuery3(int companyId) {

        try {
            String sql = "SELECT   \n" +
                    "    SQA.\"QUESTION\"\n" +
                    "    \n" +
                    "FROM  \n" +
                    " \n" +
                    "    \"Bizfns\".\"COMPANY_SECURITY_QUESTION_ANSWER\" CSQA \n" +
                    "JOIN \n" +
                    "    \"Bizfns\".\"SECURITY_QUESTION_MASTER\" SQA ON SQA.\"PK_QUESTION_ID\" =\n" +
                    "   CSQA.\"FK_QUESTION_ID\"  where CSQA.\"FK_COMPANY_MASTER_ID\" = ? ;";


            return jdbcTemplate.queryForList(sql, companyId);
        } catch (Exception e) {
            return null;
        }

    }


    public Map<String, Object> getMarketingQuery(Integer companyId) {

        try {

            String sql = "  SELECT   \n" +
                    "    SQA.*\n" +
                    "FROM  \n" +
                    "    \"Bizfns\".\"marketing_master\" SQA\n" +
                    "WHERE \n" +
                    "    SQA.\"fk_company_id\" = ? ";
            return jdbcTemplate.queryForMap(sql, companyId);
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    public String getCompanyId(String tenantId, String userId) {
        String sql = "SELECT SQA.\"COMPANY_ID\"\n" +
                "FROM \"Bizfns\".\"COMPANY_MASTER\" SQA\n" +
                "WHERE SQA.\"SCHEMA_ID\" = ? AND SQA.\"COMPANY_BACKUP_PHONE_NUMBER\" = ?";

        try {
            // Pass parameters as an array to jdbcTemplate.queryForObject
            String companyId = jdbcTemplate.queryForObject(sql, String.class, tenantId, userId);

            // Return the companyId
            return companyId;
        }catch (Exception e){
            return "failure";
        }
    }

    public void updateBusinessNameAndLogoQuery(String tenantId, String userId, String newBusinessName
            , String newCompanyLogo, String businessContactPerson, String businessEmail,
                                               String trustedBackupEmail, String trustedBackupMobileNumber) {
        String sql = "UPDATE \"Bizfns\".\"COMPANY_MASTER\" " +
                "SET \"BUSINESS_NAME\" = ?, " +
                "    \"COMPANY_LOGO\" = ?, " +
                "    \"COMPANY_BACKUP_EMAIL\" = ?, " +
                "    \"TRUSTED_BACKUP_EMAIL\" = ?, " +
                "    \"TRUSTED_BACKUP_MOBILE_NUMBER\" = ? " +
                "WHERE \"SCHEMA_ID\" = ? " +
                "  AND \"COMPANY_BACKUP_PHONE_NUMBER\" = ?";

        // Use JdbcTemplate to execute the update
        int rowsUpdated = jdbcTemplate.update(sql, newBusinessName, newCompanyLogo, businessEmail,
                trustedBackupEmail, trustedBackupMobileNumber, tenantId, userId);

        System.out.println(rowsUpdated + " rows updated");
    }


    public int saveMasterProfile(String tenantId, String userId, String businessContactPerson,
                                 String trustedBackupMobileNumber, String trustedBackupEmail,
                                 String businessEmail, String businessName, String businessLogo,
                                 String marketingDescription, List<String> addLocation,
                                 String address, int companyId) {

        // Update COMPANY_MASTER table
        String updateCompanySql = "UPDATE \"Bizfns\".\"COMPANY_MASTER\" " +
                "SET \"TRUSTED_BACKUP_PHONE_NUMBER\" = ?, " +
                "\"TRUSTED_BACKUP_EMAIL\" = ?, " +
                "\"COMPANY_BACKUP_EMAIL\" = ?, " +
                "\"BUSINESS_NAME\" = ?, " +
                "\"COMPANY_LOGO\" = ?, " +
                "\"BUSINESS_CONTACT_PERSON\" = ? " +
                "WHERE \"SCHEMA_ID\" = ? AND \"COMPANY_BACKUP_PHONE_NUMBER\" = ?";

        int update = jdbcTemplate.update(updateCompanySql, trustedBackupMobileNumber, trustedBackupEmail,
                businessEmail, businessName, businessLogo, businessContactPerson, tenantId, userId);

        //update business name in user_master table


            // Fetch the business details using the provided userId
        List<Map<String, Object>> results = jdbcTemplate.queryForList("SELECT\n" +
                "    CASE\n" +
                "        WHEN LOWER(\"USER_TYPE\") = LOWER('staff') THEN \"SCHEMA_NAME\" || 'st'\n" +
                "        ELSE \"SCHEMA_NAME\"\n" +
                "    END AS \"SCHEMA_NAME\",\n" +
                "    \"BUSINESS_NAME\"\n" +
                "FROM\n" +
                "    \"Bizfns\".\"USER_MASTER\"\n" +
                "WHERE\n" +
                "    (\"EMAIL_ID\" = ? OR \"MOBILE_NUMBER\" = ?) AND \"SCHEMA_NAME\" = ?", userId, userId, tenantId);


        // Assuming the query will return only one row, otherwise, handle the list accordingly
            if (!results.isEmpty()) {
                Map<String, Object> row = results.get(0);
                String schemaName = (String) row.get("SCHEMA_NAME");
                String businessName1 = (String) row.get("BUSINESS_NAME");

                // Now construct your update query
                String updateQuery = "UPDATE \"Bizfns\".\"USER_MASTER\" SET \"BUSINESS_NAME\" = ?, \"EMAIL_ID\" = ? WHERE \"SCHEMA_NAME\" = ? AND \"MOBILE_NUMBER\" = ?";

                // Execute the update query with the new businessId and schemaName
                jdbcTemplate.update(updateQuery, businessName, businessEmail, schemaName,userId);
            } else {
                // Handle case where no result found for the given userId
            }


        try {
            String sql = "SELECT DISTINCT SQA.\"fk_company_id\" " +
                    "FROM \"Bizfns\".\"marketing_master\" SQA " +
                    "WHERE SQA.\"fk_company_id\" = ?";
            String existingCompanyId = jdbcTemplate.queryForObject(sql, String.class, companyId);

            if (existingCompanyId.equalsIgnoreCase(String.valueOf(companyId))) {
                String updatedQuery = "UPDATE \"Bizfns\".\"marketing_master\" " +
                        "SET \"marketing_description\" = ?, " +
                        "    \"add_location\" = ? " +
                        "WHERE \"fk_company_id\" = ?";
                jdbcTemplate.update(updatedQuery, marketingDescription, String.join(", ", addLocation), companyId);
            }

        } catch (EmptyResultDataAccessException e) {

            String upsertMarketingSql = "INSERT INTO \"Bizfns\".\"marketing_master\" " +
                    "(\"fk_company_id\", \"marketing_description\", \"add_location\") " +
                    "VALUES (?, ?, ?)";
            jdbcTemplate.update(upsertMarketingSql, companyId, marketingDescription, String.join(", ", addLocation));
        }


        try {
            String sql = "SELECT DISTINCT SQA.\"FK_COMPANY_ID\" " +
                    "FROM \"Bizfns\".\"ADDRESS_MASTER\" SQA " +
                    "WHERE SQA.\"FK_COMPANY_ID\" = ?";
            String updatedCompanyId = jdbcTemplate.queryForObject(sql, String.class, companyId);

            if (updatedCompanyId.equalsIgnoreCase(String.valueOf(companyId))) {
                String updatedQuery = "UPDATE \"Bizfns\".\"ADDRESS_MASTER\" " +
                        "   SET \"ADDRESS\" = ? " +
                        "WHERE \"FK_COMPANY_ID\" = ?";
                jdbcTemplate.update(updatedQuery, address, companyId);
            }

        } catch (EmptyResultDataAccessException e) {

            String upsertMarketingSql = "INSERT INTO \"Bizfns\".\"ADDRESS_MASTER\" " +
                    "(\"FK_COMPANY_ID\", \"ADDRESS\") " +
                    "VALUES (?, ?)";
            jdbcTemplate.update(upsertMarketingSql, companyId, address);
        }
        return update;
    }

    public String getAddressQuery(Integer companyId) {
        try {

            String sql = "SELECT " +
                    "SQA.\"ADDRESS\" " +
                    "FROM \"Bizfns\".\"ADDRESS_MASTER\" SQA " +
                    "WHERE SQA.\"FK_COMPANY_ID\" = ?";

            return jdbcTemplate.queryForObject(sql, String.class, companyId);
        } catch (Exception e) {
            return null;
        }
    }


    public int updateMobileNumberQuery(String userIdOrOldMobileNumber, String newMobileNumber, String tenantId) {
        // Assuming 'userIdOrOldMobileNumber' can be either userId or old mobile number

        // Define the SQL query to update the mobile number
        String sql = "UPDATE \"Bizfns\".\"COMPANY_MASTER\" " +
                "SET \"COMPANY_BACKUP_PHONE_NUMBER\" = ? " +
                "WHERE \"COMPANY_BACKUP_PHONE_NUMBER\" = ? " +
                "AND \"SCHEMA_ID\" = ? ";

        try {

            int update = jdbcTemplate.update(sql, newMobileNumber, userIdOrOldMobileNumber, tenantId);
           if (update >=1){
               String sql1 = "UPDATE \"Bizfns\".\"USER_MASTER\" " +
                       "SET \"MOBILE_NUMBER\" = ? " +
                       "WHERE \"MOBILE_NUMBER\" = ? " +
                       "AND \"SCHEMA_NAME\" = ? ";
               try {
                   int updated = jdbcTemplate.update(sql1, newMobileNumber, userIdOrOldMobileNumber, tenantId);
                   if (updated > 0) {
                       System.out.println("Update successful. Rows affected: " + updated);
                   } else {
                       System.out.println("No rows updated.");
                   }
               } catch (DataAccessException e) {
                   System.err.println("Error updating data: " + e.getMessage());
               }


           }

            return update;
        } catch (Exception e) {
            return 0;
        }
    }

    public String getPasswordByUserId(String userId, String tenantId) {
        String sql = "SELECT \"PASSWORD\" " +
                "FROM \"Bizfns\".\"COMPANY_MASTER\" " +
                "WHERE \"COMPANY_BACKUP_PHONE_NUMBER\" = ? " +
                "AND \"SCHEMA_ID\" = ? ";

        try {

            String s = jdbcTemplate.queryForObject(sql, String.class, userId, tenantId);
            System.out.println("correctedddddddddddddd");
            return s;
        } catch (EmptyResultDataAccessException e) {
            System.out.println("not correctedddddddddddddd");
            return null;
        }
    }


    public Map<String, Object> checkOtpExistence(String userId) {
        // Define the SQL query
        String sql = "SELECT \"USER_TYPE\"\n" +
                "FROM \"Bizfns\".\"USER_OTP\"\n" +
                "WHERE \"FK_USER_ID\" = (\n" +
                "    SELECT \"COMPANY_ID\" \n" +
                "    FROM \"Bizfns\".\"COMPANY_MASTER\"\n" +
                "    WHERE \"COMPANY_BACKUP_PHONE_NUMBER\" = ? OR \"COMPANY_BACKUP_EMAIL\" = ? \n" +
                ") \n" +
                "AND \"USER_TYPE\" = (SELECT CAST(\"PK_USER_TYPE_ID\" AS VARCHAR) AS \"PK_USER_TYPE_ID_STRING\"\n" +
                "FROM \"Bizfns\".\"USER_TYPE_MASTER\"\n" +
                "WHERE LOWER(\"USER_TYPE_MASTER_ENTITY\") = 'company') \n" +
                "AND DATE_TRUNC('day', \"OTP_CREATED_AT\") = DATE_TRUNC('day', CURRENT_DATE)";

        // Execute the query and get the result as a Map
        return jdbcTemplate.queryForMap(sql, userId, userId);
    }

    public Long insertFirstLoginOtp(String randomNumber, String userId, String tenantId) {
        // Define the SQL query
        String sql = "INSERT INTO \"Bizfns\".\"USER_OTP\" (\"FK_USER_ID\", \"USER_TYPE\", \"OTP\", \"OTP_CREATED_AT\", \"OTP_COUNT\", \"SCHEMA_ID\") \n" +
                "VALUES (\n" +
                "    (\n" +
                "        SELECT \"COMPANY_ID\" \n" +
                "        FROM \"Bizfns\".\"COMPANY_MASTER\" \n" +
                "        WHERE \"COMPANY_BACKUP_PHONE_NUMBER\" = ? OR \"COMPANY_BACKUP_EMAIL\" = ?\n" +
                "    ), \n" +
                "    (SELECT CAST(\"PK_USER_TYPE_ID\" AS VARCHAR) AS \"PK_USER_TYPE_ID_STRING\"\n" +
                "    FROM \"Bizfns\".\"USER_TYPE_MASTER\"\n" +
                "    WHERE LOWER(\"USER_TYPE_MASTER_ENTITY\") = 'company'), \n" +
                "    ?, \n" +
                "    current_timestamp, '0', ? \n" +
                ") \n" +
                "RETURNING EXTRACT(EPOCH FROM \"OTP_UPDATED_AT\") * 1000 AS \"OTP_UPDATED_AT_MS\"";

        // Execute the query and get the result as a Long
        return jdbcTemplate.queryForObject(sql, Long.class, userId, userId, randomNumber, tenantId);
    }

    public String checkMobileExistence(Integer companyId) {
        // Define the SQL query
        String sql = "SELECT COALESCE(\"COMPANY_BACKUP_PHONE_NUMBER\", 'N') " +
                "FROM \"Bizfns\".\"COMPANY_MASTER\" " +
                "WHERE \"COMPANY_ID\" = ?";

        try {
            // Execute the query using jdbcTemplate
            return jdbcTemplate.queryForObject(sql, String.class, companyId);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            // Handle the case where no result is found (optional)
            return "failure"; // or any default value you prefer
        }
    }

    public String refreshTokenForEmail(Integer userId, String tenantId) {


        // SQL query string
        String sql = "DELETE FROM \"Bizfns\".\"TOKEN_MASTER\" " +
                "WHERE \"USER_ID\" = ? AND \"SCHEMA_ID\" = ?";

        try {
            // Execute the delete query using JdbcTemplate
            int rowsAffected = jdbcTemplate.update(sql, userId, tenantId);

            System.err.println("rowsAffected :  " + rowsAffected);
            if (rowsAffected > 0) {
                return "success";
            } else {
                return "failure"; // No rows were affected, indicating no matching record found
            }
        } catch (EmptyResultDataAccessException e) {
            // Handle the case when no matching record is found
            return "failure"; // or throw a specific exception if needed
        } catch (DataAccessException e) {
            // Handle other database-related exceptions
            e.printStackTrace(); // Log or handle the exception appropriately
            return "failure";
        }
    }

    public Map<String, Object> getProfileQuery4(Integer companyId) {
        String sql = "SELECT " +
                "SQA.\"BUSINESS_TYPE_ENTITY\" " +
                "FROM \"Bizfns\".\"BUSINESS_TYPE_MASTER\" SQA " +
                "JOIN \"Bizfns\".\"COMPANY_BUSINESS_TYPE_MAPPING\" C " +
                "ON SQA.\"PK_BUSINESS_TYPE_ID\" = C.\"FK_BUSINESS_TYPE_ID\" " +
                "WHERE C.\"FK_COMPANY_ID\" = ?";

        // Execute the query and return the result as a Map
        return jdbcTemplate.queryForMap(sql, companyId);
    }

    public int saveNotificationMessage(int companyId, String mobile, String message, Timestamp date, String tenantId, String tenantId1) {
        String sql = "INSERT INTO \"Bizfns\".\"NOTIFICATION_MASTER\" " +
                "(\"FK_COMPANY_ID\", \"MODULE_TYPE\", \"MESSAGE\", \"CREATED_DATE\", \"BUSINESS_NAME\", \"SCHEMA_ID\") " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        return jdbcTemplate.update(sql, companyId, mobile, message, date, tenantId, tenantId1);
    }

    public int saveImageName(String name, String tenantId, String companyId) {
        String sql = "UPDATE \"Bizfns\".\"COMPANY_MASTER\" " +
                "SET \"COMPANY_LOGO\" = ? " +
                "WHERE \"SCHEMA_ID\" = ? AND \"COMPANY_BACKUP_PHONE_NUMBER\" = ? ";

        try {
            int update = jdbcTemplate.update(sql, name, tenantId, companyId);
            if (update == 1) {
                System.err.println("Image name saved successfully");
            } else {
                System.err.println("No rows updated. Check your WHERE clause conditions.");
            }
            return update;
        } catch (Exception e) {
            e.printStackTrace();
            return 0; // or handle the exception as needed
        }
    }

//    public List<Map<String, Object>> getClientListQuery(int page, int size) {
//        String sql = "SELECT CM.\"CLIENT_ID\", " +
//                "CM.\"COMPANY_STATUS\", " +
//                "CM.\"BUSINESS_NAME\", " +
//                "CM.\"COMPANY_BACKUP_PHONE_NUMBER\"," +
//                "CM.\"COMPANY_BACKUP_EMAIL\"," +
//                "CM.\"PASSWORD\"," +
//                "BTM.\"BUSINESS_TYPE_ENTITY\" " +
//                "FROM \"Bizfns\".\"COMPANY_MASTER\" CM " +
//                "JOIN \"Bizfns\".\"COMPANY_BUSINESS_TYPE_MAPPING\" CBTM " +
//                "ON CM.\"COMPANY_ID\" = CBTM.\"FK_COMPANY_ID\" " +
//                "JOIN \"Bizfns\".\"BUSINESS_TYPE_MASTER\" BTM " +
//                "ON BTM.\"PK_BUSINESS_TYPE_ID\" = CBTM.\"FK_BUSINESS_TYPE_ID\" " +
//                "LIMIT :size OFFSET :offset";
//
//        int offset = page * size;
//
//        Map<String, Object> paramMap = new HashMap<>();
//        paramMap.put("size", size);
//        paramMap.put("offset", offset);
//
//        return jdbcTemplate.queryForList(sql, paramMap);
//    }

    public List<Map<String, Object>> getClientListQuery(int page, int size) {
        String sql = "SELECT CM.\"CLIENT_ID\", " +
                "CM.\"COMPANY_STATUS\", " +
                "CM.\"BUSINESS_NAME\", " +
                "CM.\"COMPANY_BACKUP_PHONE_NUMBER\"," +
                "CM.\"COMPANY_BACKUP_EMAIL\"," +
                "CM.\"PASSWORD\"," +
                "TO_CHAR(CM.\"COMPANY_CREATED_AT\", 'YYYY-MM-DD HH24:MI:SS') AS \"COMPANY_CREATED_AT\",\n" +
                "BTM.\"BUSINESS_TYPE_ENTITY\", " +
                "AM.\"ADDRESS\" " +
                "FROM \"Bizfns\".\"COMPANY_MASTER\" CM " +
                "JOIN \"Bizfns\".\"COMPANY_BUSINESS_TYPE_MAPPING\" CBTM " +
                "ON CM.\"COMPANY_ID\" = CBTM.\"FK_COMPANY_ID\" " +
                "JOIN \"Bizfns\".\"BUSINESS_TYPE_MASTER\" BTM " +
                "ON BTM.\"PK_BUSINESS_TYPE_ID\" = CBTM.\"FK_BUSINESS_TYPE_ID\" " +
                "LEFT JOIN \"Bizfns\".\"ADDRESS_MASTER\" AM " +
                "ON AM.\"FK_COMPANY_ID\" = CM.\"COMPANY_ID\" " +
                "LIMIT ? OFFSET ?";

        int offset = page * size;

        return jdbcTemplate.queryForList(sql, size, offset);
    }


    public List<Map<String, Object>> getClientDetailByBusinessNameQuery(String businessName) {
        String sql = "SELECT CM.\"COMPANY_BACKUP_PHONE_NUMBER\", " +
                "CM.\"COMPANY_BACKUP_EMAIL\" " +
                "FROM \"Bizfns\".\"COMPANY_MASTER\" CM " +
                "WHERE CM.\"BUSINESS_NAME\" = ?";

        return jdbcTemplate.queryForList(sql, businessName);
    }

    public List<Map<String, Object>> getClientBusinessNameListQuery() {

        String sql = "SELECT DISTINCT CM.\"BUSINESS_NAME\" " +
                "FROM \"Bizfns\".\"COMPANY_MASTER\" CM ";

        return jdbcTemplate.queryForList(sql);
    }

    public String getBusinessEmailQuery(String tenantId, String userId) {

        String sql = "SELECT \"COMPANY_BACKUP_EMAIL\" " +
                "FROM \"Bizfns\".\"COMPANY_MASTER\" " +
                "WHERE \"SCHEMA_ID\" = ? AND \"COMPANY_BACKUP_PHONE_NUMBER\" = ?";

        try{
            String email = jdbcTemplate.queryForObject(sql, String.class, tenantId, userId);
            if (email !=null){
                return email;
            }else {
                return "Failure";
            }

        }catch (Exception e){
          return "failure" ;
        }
      }

    public int saveMasterProfileInUserMaster(String tenantId, String userId, String businessEmail, String businessName) {
        // SQL statement to update the USER_MASTER table
        String sql = "UPDATE \"Bizfns\".\"USER_MASTER\" " +
                "SET \"EMAIL_ID\" = ?, " +  // Update email ID
                "\"BUSINESS_NAME\" = ? " +  // Update business name
                "WHERE \"MOBILE_NUMBER\" = ? " +  // Condition: mobile number
                "AND \"SCHEMA_NAME\" = ?";  // Condition: schema name

        // Execute the update statement
        int rowsUpdated = jdbcTemplate.update(sql, businessEmail, businessName, userId, tenantId);

        return rowsUpdated;  // Return the number of rows updated
    }

    public int updateMobilenumberInUserMaster(String userIdOrOldMobileNumber, String newMobileNumber, String tenantId) {

        String sql1 = "UPDATE \"Bizfns\".\"USER_MASTER\" " +
                "SET \"MOBILE_NUMBER\" = ? " +
                "WHERE \"MOBILE_NUMBER\" = ? " +
                "AND \"SCHEMA_NAME\" = ? ";
        int updated = jdbcTemplate.update(sql1, newMobileNumber, userIdOrOldMobileNumber, tenantId);

        return updated;

    }

    public List<String> getBusinessName(String newMobileNumber) {
        String sql = "SELECT \"BUSINESS_NAME\" " +
                "FROM \"Bizfns\".\"COMPANY_MASTER\" " +
                "WHERE \"COMPANY_BACKUP_PHONE_NUMBER\" = ?";

        List<String> businessNames = jdbcTemplate.queryForList(sql, String.class, newMobileNumber);

        // Check if the list is empty
        if (businessNames.isEmpty()) {
            // Return an empty list
            return Collections.emptyList();
        } else {
            // Return the list of business names
            return businessNames;
        }
    }

    public List<String> getBusinessNameOldNumber(String newMobileNumber) {
        String sql = "SELECT \"BUSINESS_NAME\" " +
                "FROM \"Bizfns\".\"COMPANY_MASTER\" " +
                "WHERE \"COMPANY_BACKUP_PHONE_NUMBER\" = ?";

        List<String> businessNames = jdbcTemplate.queryForList(sql, String.class, newMobileNumber);
        return businessNames;
    }

    public int deleteToken(String tenantId) {
        // Query to check if a row exists with the specified tenantId
        String checkSql = "SELECT COUNT(*) FROM \"Bizfns\".\"TOKEN_MASTER\" WHERE \"SCHEMA_ID\" = ?";

        // Execute the query to check if a row exists
        int rowCount = jdbcTemplate.queryForObject(checkSql, Integer.class, tenantId);

        // If a row exists, proceed with the delete operation
        if (rowCount > 0) {
            String deleteSql = "DELETE FROM \"Bizfns\".\"TOKEN_MASTER\" WHERE \"SCHEMA_ID\" = ?";

            // Execute the delete operation
            int rowsAffected = jdbcTemplate.update(deleteSql, tenantId);

            // Return the number of rows affected
            return rowsAffected;
        } else {
            // If no row exists, return 0 indicating no deletion performed
            return 0;
        }
    }


    public String fetchBusinessName(int companyId) {
       String sql = "SELECT \"BUSINESS_NAME\" FROM \"Bizfns\".\"COMPANY_MASTER\" WHERE \"COMPANY_ID\" = ? ";


        String businessName = jdbcTemplate.queryForObject(sql, String.class, companyId);

        return businessName;
    }

    public Map<String, Object> getStaffProfile(String userId, String tenantId) {

        String sql = "SELECT \"EMAIL_ID\", \"MOBILE_NUMBER\", \"BUSINESS_NAME\", \"SCHEMA_NAME\", TO_CHAR(\"CREATED_BY\", 'YYYY-MM-DD HH24:MI:SS') AS \"CREATED_BY\" FROM\"Bizfns\".\"USER_MASTER\" " +
                "WHERE \"MOBILE_NUMBER\" = ? AND \"SCHEMA_NAME\" = ?";

        try {
            Map<String, Object> result = jdbcTemplate.queryForMap(sql, userId, tenantId);

            return result;

        }catch(Exception e){
            return new HashMap<>();
        }

    }

    public Map<String, Object> getCompanyIdFromCompanyMaster(String tenantId) {
        String sql = "SELECT \"COMPANY_BACKUP_PHONE_NUMBER\", \"COMPANY_ID\" FROM \"Bizfns\".\"COMPANY_MASTER\" " +
                "WHERE \"SCHEMA_ID\" = ?";

        try {
            Map<String, Object> result = jdbcTemplate.queryForMap(sql, tenantId);
            return result;
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyMap(); // Return empty map if no data found
        }
    }

//    public String checkAccessToken(String userId, String tenantId) {
//        String sql = "SELECT \"COMPANY_BACKUP_PHONE_NUMBER\" FROM \"Bizfns\".\"COMPANY_MASTER\" " +
//                "WHERE (\"COMPANY_BACKUP_PHONE_NUMBER\" = ? OR \"COMPANY_BACKUP_EMAIL\" = ? ) AND \"SCHEMA_ID\" = ?";
//
//        // Execute the query and retrieve the result
//        String companyBackupPhone = jdbcTemplate.queryForObject(
//                sql,
//                new Object[]{userId, userId, tenantId},
//                String.class
//        );
//
//        return companyBackupPhone;
//    }

    public String checkAccessToken(String userId, String tenantId) {
        String companyBackupPhone = null;

        // Query to check in the COMPANY_MASTER table
        String companySql = "SELECT \"COMPANY_BACKUP_PHONE_NUMBER\" FROM \"Bizfns\".\"COMPANY_MASTER\" " +
                "WHERE (\"COMPANY_BACKUP_PHONE_NUMBER\" = ? OR \"COMPANY_BACKUP_EMAIL\" = ?) AND \"SCHEMA_ID\" = ?";

        try {
            // Try to execute the query on the COMPANY_MASTER table
            companyBackupPhone = jdbcTemplate.queryForObject(
                    companySql,
                    new Object[]{userId, userId, tenantId},
                    String.class
            );
        } catch (EmptyResultDataAccessException e) {
            // If no result found in COMPANY_MASTER table, query USER_MASTER table
            String userSql = "SELECT \"MOBILE_NUMBER\" FROM \"Bizfns\".\"USER_MASTER\" " +
                    "WHERE (\"MOBILE_NUMBER\" = ? OR \"EMAIL_ID\" = ?) AND \"SCHEMA_NAME\" = ?";

            try {
                // Execute the query on the USER_MASTER table
                companyBackupPhone = jdbcTemplate.queryForObject(
                        userSql,
                        new Object[]{userId, userId, tenantId},
                        String.class
                );
            } catch (EmptyResultDataAccessException ex) {
                // Handle the case when no result found in USER_MASTER table as well
                // You may return null or throw an exception depending on your requirement
                return userId; // or throw new RuntimeException("No backup phone found for the user");
            }
        }

        return companyBackupPhone;
    }




}

