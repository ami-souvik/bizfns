package com.bizfns.services.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class CustQuery {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> recordForCustList(String tenantId){

        String strQueryCustomerList = "select \"PK_CUSTOMER_ID\",\n" +
                "\"CUSTOMER_FIRST_NAME\",\n" +
                "\"CUSTOMER_LAST_NAME\",\n" +
                "\"CUSTOMER_EMAIL\",\n" +
                "TO_CHAR(\"COMPANY_CREATED_AT\", 'YYYY-MM-DD HH24:MI:SS') AS \"COMPANY_CREATED_AT\",\n" +
                "\"CUSTOMER_STATUS\",\n" +
                "\"CUSTOMER_PHONE_NUMBER\" from \"" + tenantId + "\".\"company_customer\" "+
                "ORDER BY \"PK_CUSTOMER_ID\" DESC";

        List<Map<String, Object>> dataForCustList= jdbcTemplate.queryForList(strQueryCustomerList);

        return dataForCustList;
    }

    public List<Map<String, Object>> recordForCustJobHistList(String tenantId, String customerId, String formDate, String toDate) {
        String strRecordForCustJobHistList = "select \"PK_JOB_ID\", \"JOB_STOP_ON\",\n" +
                "\"JOB_CREATED_AT\"\n" +
                "FROM      \"" + tenantId + "\".\"job_master\" \n" +
                "WHERE \"PK_JOB_ID\" IN (\n" +
                "    SELECT \"FK_JOB_ID\"\n" +
                "    FROM  \"" + tenantId + "\".\"assigned_job\" \n" +
                "    WHERE \"FK_CUSTOMER_ID\" = '1'\n" +
                ") \n" +
                "AND \"JOB_CREATED_AT\" >= DATE '"+formDate+"'\n" +
                "AND \"JOB_STOP_ON\" <= DATE '"+toDate+"' ";
       // System.out.println(strRecordForCustJobHistList);
        List<Map<String, Object>> dataForCustJobHistList= jdbcTemplate.queryForList(strRecordForCustJobHistList);

        return dataForCustJobHistList;

    }

    public Map<String, Object> recordForCustDetails(String tenantId, String customerId) {


        String strRecordForCustJobHistList = "select \"PK_CUSTOMER_ID\",\n" +
                "\"CUSTOMER_FIRST_NAME\",\n" +
                "\"CUSTOMER_LAST_NAME\" from   \"" + tenantId + "\".\"company_customer\"  where \"PK_CUSTOMER_ID\" = '"+customerId+"' ";
       // System.out.println(strRecordForCustJobHistList);
        Map<String, Object> dataForCustJobHistList= jdbcTemplate.queryForMap(strRecordForCustJobHistList);

        return dataForCustJobHistList;
    }

    public Map<String, Object> recordForServiceDetails(String tenantId, String customerId) {

        String strRecordForServiceDetails = "SELECT \"SERVICE_NAME\"\n" +
                "FROM "+tenantId+".service_master gg\n" +
                "WHERE \"SERVICE_ID\" = (\n" +
                "    SELECT \"SERVICE_ID\"\n" +
                "    FROM  \"" + tenantId + "\".\"job_wise_service_mapping\" \n" +
                "    WHERE \"JOB_ID\" in (\n" +
                "    SELECT CAST(\"FK_JOB_ID\" AS VARCHAR)  \n" +
                "    FROM \"" + tenantId + "\".\"assigned_job\" \n" +
                "    WHERE \"FK_CUSTOMER_ID\" = "+customerId+"\n" +
                ") \n" +
                ")::integer";
       // System.out.println(strRecordForServiceDetails);
        Map<String, Object> dataRecordForServiceDetails= jdbcTemplate.queryForMap(strRecordForServiceDetails);

        return dataRecordForServiceDetails;
    }

    public String addCustomerQuery(String tenantId, String customerId,String custFName,
                                                String custLName,String custEmail,String encryptedPassword,String lastOtp,
    String custPhNo,String custStatus, String isOtpVerified, String custCompanyName, String custAddress) {

        String queryForCustomerInsertion = "INSERT INTO \"" + tenantId + "\".\"company_customer\"   \n" +
                "(\"PK_CUSTOMER_ID\", \"FK_COMPANY_ID\", \"CUSTOMER_FIRST_NAME\", \"CUSTOMER_LAST_NAME\", \"CUSTOMER_EMAIL\", \"CUSTOMER_PASSWORD\", \n" +
                "\"LAST_OTP\", \"COMPANY_CREATED_AT\", \"COMPANY_UPDATED_AT\", \"CUSTOMER_PHONE_NUMBER\", \"CUSTOMER_STATUS\", \"IS_OTP_VERIFIED\", \"customer_company_name\", \"customer_address\")\n" +
                "VALUES(\n" +
                "(SELECT COALESCE((SELECT MAX(\"PK_CUSTOMER_ID\") FROM    \"" + tenantId + "\".\"company_customer\"  ), 0   ) + 1),\n" +
                "?, ?, ?, ?, ?, ?, current_timestamp, current_timestamp, ?, ?, ?, ?, ?) RETURNING \"PK_CUSTOMER_ID\"";

// Prepare parameter values
        Object[] params = new Object[] {
                Integer.parseInt(customerId),
                custFName,
                custLName,
                custEmail,
                encryptedPassword,
                lastOtp,
                custPhNo,
                custStatus,
                isOtpVerified,
                custCompanyName,
                custAddress
        };

// Execute the parameterized query
        Long insertedCustomerId = jdbcTemplate.queryForObject(queryForCustomerInsertion, params, Long.class);



        return insertedCustomerId.toString();
    }

    public List<Map<String, Object>> recordForCustHistList(String tenantId, String customerId, String fromDate, String toDate) {
        String strRecordForCustHistList = " SELECT \n" +
                "    jm.\"PK_JOB_ID\", \n" +
                "    jm.\"JOB_STOP_ON\", \n" +
                "    jm.\"JOB_CREATED_AT\",\n" +
                "    cc.\"PK_CUSTOMER_ID\",\n" +
                "    cc.\"CUSTOMER_FIRST_NAME\",\n" +
                "    cc.\"CUSTOMER_LAST_NAME\",\n" +
                "    sm.\"SERVICE_NAME\"\n" +
                "FROM  \"" + tenantId + "\".\"job_master\" jm\n" +
                "JOIN \"" + tenantId + "\".\"company_customer\" cc ON cc.\"PK_CUSTOMER_ID\" = '"+customerId+"'\n" +
                "JOIN   \"" + tenantId + "\".\"service_master\"  sm ON sm.\"SERVICE_ID\" = (\n" +
                "    SELECT cast(\"SERVICE_ID\" as Integer)\n" +
                "    FROM  \"" + tenantId + "\".\"job_wise_service_mapping\" \n" +
                "    WHERE \"JOB_ID\" IN (\n" +
                "        SELECT CAST(\"FK_JOB_ID\" AS VARCHAR)  \n" +
                "        FROM \"" + tenantId + "\".\"assigned_job\"  \n" +
                "        WHERE \"FK_CUSTOMER_ID\" = '"+customerId+"'\n" +
                "    )\n" +
                ")\n" +
                "WHERE jm.\"PK_JOB_ID\" IN (\n" +
                "    SELECT \"FK_JOB_ID\"\n" +
                "    FROM    \"" + tenantId + "\".\"assigned_job\"  \n" +
                "    WHERE \"FK_CUSTOMER_ID\" = '"+customerId+"'\n" +
                ") \n" +
                "AND jm.\"JOB_CREATED_AT\" >= DATE '"+fromDate+"'\n" +
                "AND jm.\"JOB_STOP_ON\" <= DATE '"+toDate+"' ";
       // System.out.println(strRecordForCustHistList);
        List<Map<String, Object>> dataRecordForCustHistList= jdbcTemplate.queryForList(strRecordForCustHistList);

        return dataRecordForCustHistList;
    }

    public List<String> priviledgeChkForCustomer(String tenantId) {
        try {
            String sql = "SELECT p.\"PREVILEDGE\" FROM \"Bizfns\".\"PRIVILEGE\" p, \"Bizfns\".\"PRIVILEGE_DTLS\" pd " +
                    "WHERE p.\"PK_PREVILEDGE_ID\" = pd.\"FK_PREVILEDGE_ID\" AND " +
                    "p.\"PREVILEDGE_TYPE\" = 'CUSTOMER' AND pd.\"TENANT_ID\" = ?";
            List<String> privileges = jdbcTemplate.queryForList(sql, new Object[]{tenantId}, String.class);
            return privileges;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> fetchCustomerDetailsFromDB(String tenantId, Integer customerId) {
        try {
            String strQueryCustomerList = "SELECT \"PK_CUSTOMER_ID\",\n" +
                    "\"CUSTOMER_FIRST_NAME\",\n" +
                    "\"CUSTOMER_LAST_NAME\",\n" +
                    "\"CUSTOMER_EMAIL\",\n" +
                    "\"customer_address\",\n" +
                    "\"customer_company_name\",\n" +
                    "\"CUSTOMER_STATUS\",\n" +
                    "\"CUSTOMER_PHONE_NUMBER\" FROM \"" + tenantId + "\".\"company_customer\" \n" +
                    "WHERE \"PK_CUSTOMER_ID\" = ? \n";
            List<Map<String, Object>> dataForCustList = jdbcTemplate.queryForList(strQueryCustomerList, customerId);
            if (dataForCustList.isEmpty()) {
                return null;
            }
            return dataForCustList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean updateCustomerData(String tenantId, Map<String, Object> request) {
        try {
            String customerId = (String) request.get("customerId");
            String firstName = (String) request.get("firstName");
            String lastName = (String) request.get("lastName");
            String companyName = (String) request.get("companyName");
            String address = (String) request.get("address");
            String email = (String) request.get("email");
            String phoneNumber = (String) request.get("customerPhone");

            String updateQuery = "UPDATE \"" + tenantId + "\".\"company_customer\" SET " +
                    "\"CUSTOMER_FIRST_NAME\" = ?, " +
                    "\"CUSTOMER_LAST_NAME\" = ?, " +
                    "\"CUSTOMER_EMAIL\" = ?, " +
                    "\"CUSTOMER_PHONE_NUMBER\" = ?, " +
                    "\"customer_company_name\" = ?, " +
                    "\"customer_address\" = ? " +
                    "WHERE \"PK_CUSTOMER_ID\" = ?";

            int rowsUpdated = jdbcTemplate.update(updateQuery, firstName, lastName, email, phoneNumber,companyName,address, Integer.parseInt(customerId));

            return rowsUpdated > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean deleteCustomerByCustomerId(String tenantId, Integer customerId) {
        try {
            String deleteCustomerServiceQuery = "DELETE FROM \"" + tenantId + "\".\"customer_wise_service_entity\" WHERE \"FK_CUSTOMER_ID\" = ?";
            String deleteCompanyCustomerQuery = "DELETE FROM \"" + tenantId + "\".\"company_customer\" WHERE \"PK_CUSTOMER_ID\" = ?";
            int rowsAffectedInService = jdbcTemplate.update(deleteCustomerServiceQuery, customerId);
            int rowsAffectedInCustomer = jdbcTemplate.update(deleteCompanyCustomerQuery, customerId);
            return (rowsAffectedInService > 0 || rowsAffectedInCustomer > 0);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
