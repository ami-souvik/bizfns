package com.bizfns.services.Query;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class StaffAuthQuery {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public String staffDbPassword(String userId, String tenantId) {

//        try {
//
//            String strStaffPassword= "select \"USER_PASSWORD\" from \"" + tenantId + "\".\"company_user\" where \"USER_EMAIL\" = ? or \"USER_PHONE_NUMBER\" = ?";
//            System.out.println("SQL Query: " + strStaffPassword);
//
//            List<Map<String, Object>> result = jdbcTemplate.queryForList(strStaffPassword, userId, userId);
//
//            String staffPassword = null;
//
//            if (!result.isEmpty()) {
//                Map<String, Object> firstRow = result.get(0);
//                staffPassword = (String) firstRow.get("USER_PASSWORD");
//            }
//
//            System.out.println("Password: " + staffPassword);
//            return staffPassword;
//        } catch (Exception e) {
//            // Properly handle and log exceptions here
//            e.printStackTrace();
//            return null; // or return an error response
//        }
        return userId;
    }

    public String staffDbPasswordForStaff(String userId, String tenantId) {

        String strStaffPassword= "select \"USER_PASSWORD\" from \"" + tenantId + "\".\"company_user\" where \"USER_EMAIL\" = ? or \"USER_PHONE_NUMBER\" = ?";
           //System.out.println("SQL Query: " + strStaffPassword);

            List<Map<String, Object>> result = jdbcTemplate.queryForList(strStaffPassword, userId, userId);

        String staffPassword = null;

            if (!result.isEmpty()) {
                Map<String, Object> firstRow = result.get(0);
               staffPassword = (String) firstRow.get("USER_PASSWORD");
           }

           // System.out.println("Password: " + staffPassword);
            return staffPassword;
    }






    public String fetctStaffOtp(String tenantFirstEightTLetters, String userId) {

        String strfetctStaffOtp = "select \"OTP\" from \"" + tenantFirstEightTLetters + "\".\"customer_otp\" where \"FK_CUSTOMER_ID\" = (select \"PK_USER_ID\" from  \"" + tenantFirstEightTLetters + "\".\"company_user\"  where \n" +
                "                \"USER_EMAIL\" = '"+userId+"' or\n" +
                "                \"USER_PHONE_NUMBER\" = '"+userId+"') and  DATE_TRUNC('day', \"OTP_CREATED_AT\") = DATE_TRUNC('day', CURRENT_DATE) and \"USER_TYPE\" = (SELECT \"PK_USER_TYPE_ID\" FROM \"Bizfns\".\"USER_TYPE_MASTER\" WHERE LOWER(\"USER_TYPE_MASTER_ENTITY\") = 'staff')\n";
       // System.out.println("SQL Query: " + strfetctStaffOtp);

        List<Map<String, Object>> result = jdbcTemplate.queryForList(strfetctStaffOtp);

        String strOtp = null;

        if (!result.isEmpty()) {
            Map<String, Object> firstRow = result.get(0);
            strOtp = (String) firstRow.get("OTP");
        }
        return strOtp;
    }

    public String fetchStaffOtpExpire(String tenantFirstEightTLetters, String userId) {


        String strFetchStaffOtpExpire = " SELECT \n" +
                "    CASE \n" +
                "        WHEN \"OTP_UPDATED_AT\" >= (CURRENT_TIMESTAMP - INTERVAL '2 minutes') \n" +
                "        THEN 'y' \n" +
                "        ELSE 'n' \n" +
                "    END AS \"RESULT\"\n" +
                "FROM       \"" + tenantFirstEightTLetters + "\".\"customer_otp\"     \n" +
                "WHERE \"FK_CUSTOMER_ID\" = (\n" +
                "    SELECT \"PK_USER_ID\" \n" +
                "    FROM     \"" + tenantFirstEightTLetters + "\".\"company_user\"   \n" +
                "    WHERE \"USER_EMAIL\" = '"+userId+"' OR \"USER_PHONE_NUMBER\" = '"+userId+"'\n" +
                ")\n" +
                "AND DATE_TRUNC('day', \"OTP_CREATED_AT\") = DATE_TRUNC('day', CURRENT_DATE)\n" +
                "AND \"USER_TYPE\" = (\n" +
                "    SELECT \"PK_USER_TYPE_ID\" \n" +
                "    FROM \"Bizfns\".\"USER_TYPE_MASTER\" \n" +
                "    WHERE LOWER(\"USER_TYPE_MASTER_ENTITY\") = 'staff'\n" +
                ")";
       // System.out.println("SQL Query: " + strFetchStaffOtpExpire);

        List<Map<String, Object>> result = jdbcTemplate.queryForList(strFetchStaffOtpExpire);
        String strOtpExpire = null;

        if (!result.isEmpty()) {
            Map<String, Object> firstRow = result.get(0);
            strOtpExpire = (String) firstRow.get("RESULT");
        }
        return strOtpExpire;
    }

    public String fetchStaffToken(String tenantFirstEightTLetters, String userId) {


        String strFetchStaffToken = "select \"USER_TOKEN\" from    \"" + tenantFirstEightTLetters + "\".\"company_user\"  where  \"USER_EMAIL\" = '"+userId+"' or\n" +
                "\"USER_PHONE_NUMBER\" = '"+userId+"'";
        //System.out.println("SQL Query: " + strFetchStaffToken);

        List<Map<String, Object>> result = jdbcTemplate.queryForList(strFetchStaffToken);
        String strStaffToken = null;

        if (!result.isEmpty()) {
            Map<String, Object> firstRow = result.get(0);
            strStaffToken = (String) firstRow.get("USER_TOKEN");
        }
        return strStaffToken;
    }


    public List<Map<String, Object>> staffData(String tenantFirstEightTLetters, String userId) {

        String strFetchStaffToken = "select \"USER_EMAIL\", \"USER_PHONE_NUMBER\",  \"PASSWORD_CHANGE\"   from    \"" + tenantFirstEightTLetters + "\".\"company_user\" where \"USER_EMAIL\" = '"+userId+"' or\n" +
                "\"USER_PHONE_NUMBER\" = '"+userId+"' ";
        //System.out.println("SQL Query: " + strFetchStaffToken);

        List<Map<String, Object>> result = jdbcTemplate.queryForList(strFetchStaffToken);

        return result;

    }

    public void changeSataffPassord(String tenantFirstEightTLetters, String encryptPassword, String userId) {


        String strUpdateStaffOtp = "update \"" + tenantFirstEightTLetters + "\".\"company_user\"  set \"USER_PASSWORD\"= '"+encryptPassword+"', \"PASSWORD_CHANGE\" ='Y' where  \"USER_EMAIL\" = '"+userId+"' or\n" +
                "                \"USER_PHONE_NUMBER\" =  '"+userId+"' ";
       // System.out.println(strUpdateStaffOtp);

        jdbcTemplate.update(strUpdateStaffOtp);
    }



    public void changeStaffPassword(String tenantFirstEightTLetters, String encryptPassword, String userId) {


        String strUpdateStaffOtp = "update \"" + tenantFirstEightTLetters + "\".\"company_user\"  set \"USER_PASSWORD\"= '"+encryptPassword+"', \"PASSWORD_CHANGE\" ='Y' where  \"USER_EMAIL\" = '"+userId+"' or\n" +
                "                \"USER_PHONE_NUMBER\" =  '"+userId+"' ";
        //System.out.println(strUpdateStaffOtp);

        jdbcTemplate.update(strUpdateStaffOtp);
    }
}
