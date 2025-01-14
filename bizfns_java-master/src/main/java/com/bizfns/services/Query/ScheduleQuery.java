package com.bizfns.services.Query;

import com.bizfns.services.Exceptions.CustomException;
import com.bizfns.services.Exceptions.RecordNotFoundException;
import com.bizfns.services.GlobalDto.GlobalResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Principal;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ScheduleQuery {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    public Integer insertAssignJobData(Integer pkJobId, Integer customerId, Integer staffId, String startDate, String tenantId, String EndDate_recurr, String StartTime_recurr, String EndTime_recurr) {

        String strInsertAssignJobData = "INSERT INTO \"" + tenantId + "\".\"assigned_job\" " +
                "(\"PK_ASSIGNED_JOB_ID\",\"FK_JOB_ID\", \"FK_USER_ID\", \"FK_CUSTOMER_ID\", \"ASSIGNED_JOB_DATE\"," +
                "\"ASSIGNED_JOB_PAYMENT_STATUS\",\"ASSIGNED_JOB_STATUS\",\"assigned_job_end_date\",\"assigned_job_start_time\",\"assigned_job_end_time\") " +
                "VALUES (" +
                "(SELECT COALESCE((SELECT MAX(\"PK_ASSIGNED_JOB_ID\") FROM  \"" + tenantId + "\".\"assigned_job\"  ), 0 ) + 1), " +
                pkJobId + "," + staffId + "," + customerId + ",'" + startDate + "'::DATE, 0, 0, '" + EndDate_recurr + "'::DATE, '" + StartTime_recurr + "'::TIME, '" + EndTime_recurr + "'::TIME) " +
                "RETURNING \"PK_ASSIGNED_JOB_ID\"";

        System.out.println(strInsertAssignJobData);

        //  jdbcTemplate.update(strInsertAssignJobData);

        Integer assignJobId = jdbcTemplate.queryForObject(strInsertAssignJobData, Integer.class);
        return assignJobId;

    }
//pranta changes below
    /*public Integer insertAssignJobData(Integer pkJobId, Integer customerId, Integer staffId, String startDate, String tenantId, String EndDate_recurr, String StartTime_recurr, String EndTime_recurr) {
        // Inserting into assigned_job table
        String strInsertAssignJobData = "INSERT INTO \"" + tenantId + "\".\"assigned_job\" " +
                "(\"PK_ASSIGNED_JOB_ID\",\"FK_JOB_ID\", \"FK_USER_ID\", \"FK_CUSTOMER_ID\", \"ASSIGNED_JOB_DATE\"," +
                "\"ASSIGNED_JOB_PAYMENT_STATUS\",\"ASSIGNED_JOB_STATUS\",\"assigned_job_end_date\",\"assigned_job_start_time\",\"assigned_job_end_time\") " +
                "VALUES (" +
                "(SELECT COALESCE((SELECT MAX(\"PK_ASSIGNED_JOB_ID\") FROM  \"" + tenantId + "\".\"assigned_job\"  ), 0 ) + 1), " +
                pkJobId + "," + staffId + "," + customerId + ",'" + startDate + "'::DATE, 0, 0, '" + EndDate_recurr + "'::DATE, '" + StartTime_recurr + "'::TIME, '" + EndTime_recurr + "'::TIME) " +
                "RETURNING \"PK_ASSIGNED_JOB_ID\"";

        Integer assignJobId = jdbcTemplate.queryForObject(strInsertAssignJobData, Integer.class);

        // Updating job_master table
        String strUpdateJobMaster = "UPDATE \"" + tenantId + "\".\"job_master\" " +
                "SET \"JOB_DATE\" = '" + startDate + "'::DATE, " +
                "\"JOB_START_TIME\" = '" + StartTime_recurr + "'::TIME, " +
                "\"JOB_END_TIME\" = '" + EndTime_recurr + "'::TIME " +
                "WHERE \"PK_JOB_ID\" = " + pkJobId; // Assuming PK_JOB_ID is the primary key

        jdbcTemplate.update(strUpdateJobMaster);

        // Updating assigned_job table with FK_JOB_ID
        String strUpdateAssignJob = "UPDATE \"" + tenantId + "\".\"assigned_job\" " +
                "SET \"FK_JOB_ID\" = " + pkJobId + ", " +
                "\"FK_USER_ID\" = " + staffId + ", " +
                "\"FK_CUSTOMER_ID\" = " + customerId + ", " +
                "\"ASSIGNED_JOB_DATE\" = '" + startDate + "'::DATE, " +
                "\"ASSIGNED_JOB_PAYMENT_STATUS\" = 0, " +
                "\"ASSIGNED_JOB_STATUS\" = 0, " +
                "\"assigned_job_end_date\" = '" + EndDate_recurr + "'::DATE, " +
                "\"assigned_job_start_time\" = '" + StartTime_recurr + "'::TIME, " +
                "\"assigned_job_end_time\" = '" + EndTime_recurr + "'::TIME " +
                "WHERE \"PK_ASSIGNED_JOB_ID\" = " + assignJobId;

        jdbcTemplate.update(strUpdateAssignJob);

        return assignJobId;
    }
*/


    public Integer insertCustomerandServiceEntityByJobId(Integer pkJobId, Integer customerId, String ServiceEntityId, String tenantId) {

        String strInsertAssignJobData = "INSERT INTO \"" + tenantId + "\".\"job_wise_customer_and_serviceentity_mapping\" " +
                "(\"PK_JOB_ID\",\"PK_CUSTOMER_ID\", \"PK_ENTITY_ID\") " +
                "VALUES (" +
                pkJobId + "," + customerId + ",'" + ServiceEntityId + "') " +
                "RETURNING \"PK_JOB_ID\"";


        System.out.println(strInsertAssignJobData);

        //  jdbcTemplate.update(strInsertAssignJobData);

        Integer assignJobId = jdbcTemplate.queryForObject(strInsertAssignJobData, Integer.class);
        return assignJobId;

    }

    public Integer insertMaterialData(String tenantId, Integer categoryId, String materialName, String materialType, String materialRate, Integer materialRateUnitId, Integer subcategoryId) {


        try {
            String materialActStatus="1";
            String strInsertMaterialData = "INSERT INTO \"" + tenantId + "\".\"material_master\" (" +
                    "\"PK_MATERIAL_ID\", \"MATERIAL_NAME\", \"FK_CATEGORY_ID\", \"MATERIAL_TYPE\", " +
                    "\"CREATED_AT\", \"RATE\", \"UPDATED_AT\", \"FK_MATERIAL_UNIT_ID\", \"FK_SUBCATEGORY_ID\", \"MATERIAL_STATUS\") " +
                    "VALUES (" +
                    "(SELECT COALESCE((SELECT MAX(\"PK_MATERIAL_ID\") FROM \"" + tenantId + "\".\"material_master\"), 0) + 1), " +
                    "?, ?, ?, current_timestamp, ?, current_timestamp, ?, ?,?) " +
                    "RETURNING \"PK_MATERIAL_ID\"";
            Object[] params = new Object[] {
                    materialName != null && !materialName.isEmpty() ? materialName : null,
                    categoryId,
                    materialType,
                    materialRate,
                    materialRateUnitId,
                    subcategoryId,
                    materialActStatus
            };
            System.out.println(strInsertMaterialData);
            Integer materialId = jdbcTemplate.queryForObject(strInsertMaterialData, params, Integer.class);
            return materialId;

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public List<Map<String, Object>> fetchMaterialList(String tenantId) {

        String strFetchMaterialList = " SELECT\n" +
                "    mm.\"PK_MATERIAL_ID\",\n" +
                "    mm.\"MATERIAL_NAME\",\n" +
                "    mm.\"FK_CATEGORY_ID\",\n" +
                "    mm.\"MATERIAL_TYPE\",\n" +
                "    mm.\"RATE\",\n" +
                "    mm.\"MATERIAL_STATUS\"\n" +
                "FROM\n" + tenantId +
                " .\"material_master\" mm\n ORDER BY mm.\"PK_MATERIAL_ID\" DESC";
        System.out.println(strFetchMaterialList);
        List<Map<String, Object>> dataForMaterialList = jdbcTemplate.queryForList(strFetchMaterialList);

        return dataForMaterialList;
    }

    public List<Map<String, Object>> fetchMaterialDataByMaterialId(String tenantId, Integer materialId) {
        String strFetchMaterialList = "SELECT" +
                "    mm.\"PK_MATERIAL_ID\"," +
                "    mm.\"FK_CATEGORY_ID\"," +
                "    mm.\"FK_SUBCATEGORY_ID\"," +
                "    mm.\"MATERIAL_NAME\"," +
                "    mm.\"RATE\"," +
                "    mm.\"MATERIAL_TYPE\"," +
                "    mm.\"FK_MATERIAL_UNIT_ID\"," +
                "    mm.\"MATERIAL_STATUS\"\n" +
                "FROM " + tenantId + ".\"material_master\" mm\n" +
                "WHERE mm.\"PK_MATERIAL_ID\" = ? ORDER BY mm.\"PK_MATERIAL_ID\" DESC";
        return jdbcTemplate.queryForList(strFetchMaterialList, materialId);
    }

    public List<Map<String, Object>> fetchMaterialCategoryData(String tenantId) {
        String strFetchMaterialCategoryData = "SELECT mcm.\"PK_CATEGORY_ID\", \n" +
                "       mcm.\"CATEGORY_NAME\", \n" +
                "       COALESCE(msm.\"pk_subcategory_id\", 0) AS \"pk_subcategory_id\", \n" +
                "       COALESCE(msm.\"pk_subcategory_name\", '') AS \"pk_subcategory_name\"\n" +
                "FROM " + tenantId + ".\"material_category_master\" mcm \n" +
                "LEFT JOIN " + tenantId + ".\"material_subcategory_master\" msm \n" +
                "ON mcm.\"PK_CATEGORY_ID\" = msm.\"pk_category_id\"\n";

        List<Map<String, Object>> dataForMaterialCategory = jdbcTemplate.queryForList(strFetchMaterialCategoryData);

        return dataForMaterialCategory;
    }

    public void insertQuestionDetails(String entityQuestionId, String assignJobId, String entityQuestionAnsOptions, String tenantId) {

        String strInsertQuestionDetails = "insert into   \"" + tenantId + "\".\"job_wise_service_entity_master\"  (\"ID\", \"JOB_ID\", \"QUESTION_ID\", \"ANSWER\") \n" +
                "values ((SELECT COALESCE((SELECT MAX(\"ID\") FROM  \"" + tenantId + "\".\"job_wise_service_entity_master\"  ), 0   ) + 1),\n" +
                "" + assignJobId + ", " + entityQuestionId + ", '" + entityQuestionAnsOptions + "')";
        System.out.println(strInsertQuestionDetails);

        jdbcTemplate.update(strInsertQuestionDetails);

    }

    public void insertServiceDetails(String serviceId, String pkJobId, String tenantId) {

        String strInsertServiceDetails = "insert into \"" + tenantId + "\".\"job_wise_service_mapping\" (\"ID\", \"JOB_ID\", \"SERVICE_ID\", \"CREATED_DATE\", \"UPDATED_DATE\" )\n" +
                "values((SELECT COALESCE((SELECT MAX(\"ID\") FROM  \"" + tenantId + "\".\"job_wise_service_mapping\" ), 0   ) + 1), '" + pkJobId + "' , '" + serviceId + "', current_timestamp, current_timestamp )";
        System.out.println(strInsertServiceDetails);

        jdbcTemplate.update(strInsertServiceDetails);
    }


    public void deleteSchedule(String tenantId, String jobId) {

        String strDeleteScheduleAJ = "delete from  \"" + tenantId + "\".\"assigned_job\"  where \"FK_JOB_ID\" = " + jobId + "  ";
        System.out.println(strDeleteScheduleAJ);
        jdbcTemplate.update(strDeleteScheduleAJ);

        String strDeleteScheduleJM = "delete from  \"" + tenantId + "\".\"job_master\"  where \"PK_JOB_ID\" = " + jobId + "  ";
        System.out.println(strDeleteScheduleJM);
        jdbcTemplate.update(strDeleteScheduleJM);

        String strDeleteScheduleJWSM = "delete from  \"" + tenantId + "\".\"job_wise_service_mapping\"  where \"JOB_ID\" = '" + jobId + "' ";
        System.out.println(strDeleteScheduleJWSM);
        jdbcTemplate.update(strDeleteScheduleJWSM);

        String jobWisecustomerandServie = "delete from  \"" + tenantId + "\".\"job_wise_customer_and_serviceentity_mapping\"  where \"PK_JOB_ID\" = '" + jobId + "' ";
        jdbcTemplate.update(jobWisecustomerandServie);

        String jobWiseMediaDataDeletion = "delete from  \"" + tenantId + "\".\"media\" m where m.\"JOB_ID\" = '" + jobId + "' ";
        jdbcTemplate.update(jobWiseMediaDataDeletion);
    }

    public boolean deleteMaterialData(String tenantId, Integer materialId) {
        try {
            String strDeleteMaterialData = "DELETE FROM \"" + tenantId + "\".\"material_master\" " +
                    "WHERE \"PK_MATERIAL_ID\" = ?";

            int rowsAffected = jdbcTemplate.update(strDeleteMaterialData, materialId);
            return rowsAffected > 0;

        } catch (DataAccessException dae) {
            dae.printStackTrace();
            System.err.println("Database access error: " + dae.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Error deleting material data: " + ex.getMessage());
        }
        return false;
    }


    public List<Map<String, Object>> jobDetails(String tenantId, String userId, String jobId) {

        String strJobDetails = "select \"PK_ASSIGNED_JOB_ID\", \"FK_JOB_ID\", \"FK_USER_ID\",\n" +
                "\"FK_CUSTOMER_ID\",\n" +
                "\"ASSIGNED_JOB_DATE\",\n" +
                "\"ASSIGNED_JOB_PAYMENT_STATUS\",\n" +
                "\"ASSIGNED_JOB_STATUS\",\n" +
                "\"ASSIGNED_JOB_CREATED_AT\",\n" +
                "\"ASSIGNED_JOB_UPDATED_AT\" from   \"" + tenantId + "\".\"assigned_job\"  " +
                "where \"FK_USER_ID\" = " + userId + " and \"FK_JOB_ID\" = " + jobId + " ";
        System.out.println(strJobDetails);
        List<Map<String, Object>> dataForJobDetails = jdbcTemplate.queryForList(strJobDetails);
        System.err.println(dataForJobDetails);
        return dataForJobDetails;
    }

    public List<Map<String, Object>> getStaffUserId(String tenantId, String userId, String jobId) {

        String staffUserId = "select \"FK_USER_ID\"" +
                "from" + tenantId + "\".\"assigned_job\"  " +
                "where \"FK_USER_ID\" = " + userId + " and \"FK_JOB_ID\" = " + jobId + " ";
        System.out.println(staffUserId);
        List<Map<String, Object>> dataForstaffUserId = jdbcTemplate.queryForList(staffUserId);
        System.err.println(dataForstaffUserId);
        return dataForstaffUserId;
    }

    public List<Map<String, Object>> jobData(String tenantId, String jobId) {

        String strJobDetails = "select \"PK_ASSIGNED_JOB_ID\", \"FK_JOB_ID\", \"FK_USER_ID\",\n" +
                "\"FK_CUSTOMER_ID\",\n" +
                "\"ASSIGNED_JOB_DATE\",\n" +
                "\"ASSIGNED_JOB_PAYMENT_STATUS\",\n" +
                "\"ASSIGNED_JOB_STATUS\",\n" +
                "\"ASSIGNED_JOB_CREATED_AT\",\n" +
                "\"ASSIGNED_JOB_UPDATED_AT\" from   \"" + tenantId + "\".\"assigned_job\"  " +
                "where \"FK_JOB_ID\" = " + jobId + " ";
        System.out.println(strJobDetails);
        List<Map<String, Object>> dataForJobDetails = jdbcTemplate.queryForList(strJobDetails);
        System.err.println(dataForJobDetails);
        return dataForJobDetails;
    }

    public List<Map<String, Object>> jobMasterDataChk(String tenantId, String jobId) {

        String getJob = "SELECT * FROM " + tenantId + ".job_master WHERE \"PK_JOB_ID\" =" + jobId + " ";
        System.out.println(getJob);
        List<Map<String, Object>> dataForJobDetails = jdbcTemplate.queryForList(getJob);
        System.err.println(dataForJobDetails);
        return dataForJobDetails;
    }
    public List<Map<String, Object>> currentImageIdList(String tenantId, String jobId) {

        String getJob = "SELECT * FROM " + tenantId + ".job_master WHERE \"PK_JOB_ID\" =" + jobId + " ";
        System.out.println(getJob);
        List<Map<String, Object>> dataForJobDetails = jdbcTemplate.queryForList(getJob);
        System.err.println(dataForJobDetails);
        return dataForJobDetails;
    }

    public List<Map<String, Object>> jobDataAvailabilityCheck(String tenantId, String staffId, String startDate, String startTime, String endTime) {

        int staffIdInt = Integer.parseInt(staffId);
        String[] startTimePart = startTime.split(":");
        String startTimeHour = startTimePart[0];
        String startTimeMinute = startTimePart[1];

        String[] endTimePart = endTime.split(":");
        String endTimeHour = endTimePart[0];
        String endTimeMinute = endTimePart[1];

        String strJobDataAvailabilityDetails = "SELECT * FROM " + tenantId + ".assigned_job aj " +
                "WHERE aj.\"FK_USER_ID\" = " + staffIdInt + " " +
                "AND aj.\"ASSIGNED_JOB_DATE\" = TO_DATE('" + startDate + "', 'yyyy-mm-dd') " +
                "AND EXTRACT(HOUR FROM aj.assigned_job_start_time) >= " + startTimeHour + " " +
                "AND EXTRACT(MINUTE FROM aj.assigned_job_start_time) >= " + startTimeMinute + " " +
                "AND EXTRACT(HOUR FROM aj.assigned_job_end_time) <= " + endTimeHour + " " +
                "AND EXTRACT(MINUTE FROM aj.assigned_job_end_time) <= " + endTimeMinute;


        System.out.println(strJobDataAvailabilityDetails);


            List<Map<String, Object>> dataForJobDetails = jdbcTemplate.queryForList(strJobDataAvailabilityDetails);
            System.err.println(dataForJobDetails);

            return dataForJobDetails;

    }

    public List<Map<String, Object>> fetchScheduleList(String tenantId, String fromDate) {


        String strFetchScheduleList = "SELECT\n distinct" +
                "    aj.\"FK_JOB_ID\",\n" +
                "    aj.\"FK_USER_ID\",\n" +
                "    aj.\"FK_CUSTOMER_ID\",\n" +
                "    aj.\"ASSIGNED_JOB_DATE\",\n" +
                "    cu.\"USER_FIRST_NAME\",\n" +
                "    cu.\"USER_LAST_NAME\",\n" +
                "    cc.\"CUSTOMER_FIRST_NAME\",\n" +
                "    cc.\"CUSTOMER_LAST_NAME\",\n" +
                "    jm.\"JOB_STOP_ON\" AS \"endTime\",\n" +
                "    jm.\"JOB_START_TIME\" AS \"startTime\",\n" +
                "    jm.\"JOB_MATERIAL\", \n" +
                "    jm.\"JOB_START_TIME\", \n" +
                "    jm.\"JOB_END_TIME\", \n" +
                "    jm.\"JOB_STATUS\", \n" +
                "    jm.\"JOB_STATUS\", \n" +
                "    jwsm.\"SERVICE_ID\"\n" +
                "FROM\n" +
                "    \"" + tenantId + "\".\"assigned_job\" aj\n" +
                "left JOIN\n" +
                "    \"" + tenantId + "\".\"company_user\" cu ON cu.\"PK_USER_ID\" = aj.\"FK_USER_ID\"\n" +
                "left JOIN\n" +
                "    \"" + tenantId + "\".\"company_customer\" cc ON cc.\"PK_CUSTOMER_ID\" = aj.\"FK_CUSTOMER_ID\"\n" +
                "left JOIN\n" +
                "    \"" + tenantId + "\".\"job_master\" jm ON jm.\"PK_JOB_ID\" = aj.\"FK_JOB_ID\"\n" +
                "left JOIN\n" +
                "    \"" + tenantId + "\".\"job_wise_service_mapping\" jwsm  ON CAST(jwsm.\"JOB_ID\" AS INTEGER) = jm.\"PK_JOB_ID\"  \n" +
                "WHERE\n" +
                "    aj.\"FK_JOB_ID\" IN (\n" +
                "        SELECT \"PK_JOB_ID\"\n" +
                "        FROM \"" + tenantId + "\".\"job_master\"\n" +
                "         WHERE CAST(\"JOB_DATE\" AS DATE) = to_date(?, 'yyyy-mm-dd')\n" +
                "    )    order by  aj.\"FK_JOB_ID\"  ";

        System.err.println(strFetchScheduleList );

        System.err.println(strFetchScheduleList);

        List<Map<String, Object>> dataForFetchScheduleList = jdbcTemplate.queryForList(strFetchScheduleList, fromDate);
        System.out.println(dataForFetchScheduleList);
        return dataForFetchScheduleList;


    }

    public List<Map<String, Object>> ftechScheduleDatabyDate(String tenantId, String fromDate) {
        String strFetchScheduleList = "select  distinct \n" +
                "    jm.\"JOB_STOP_ON\" AS \"endDate\",\n" +
                "    jm.\"JOB_DATE\" AS \"startDate\",\n" +
                "    jm.\"JOB_MATERIAL\",\n" +
                "    jm.\"JOB_START_TIME\",\n" +
                "    jm.\"JOB_END_TIME\",\n" +
                "    jm.\"JOB_STATUS\",\n" +
                "    jm.\"JOB_NOTES\",\n" +
                "    jm.\"PK_JOB_ID\",\n" +
                "    jm.\"JOB_LOCATION\",\n" +
                "    jm.\"STAFF_DETAILS\",\n" +
                "    jm.\"PAYMENT_DURATION\",\n" +
                "    jm.\"PAYMENT_DEPOSIT\",\n" +
                "    jm.\"IMAGE_AUDIT_ID\"\n" +
                "FROM\"" + tenantId +
                "\".job_master jm \n" +
                "WHERE\n" +
                "    CAST(\"JOB_DATE\" AS DATE) = to_date( ? , 'yyyy-mm-dd')\n" +
                "ORDER BY\n" +
                "    jm.\"PK_JOB_ID\";";
        System.err.println(strFetchScheduleList);
        List<Map<String, Object>> dataForFetchScheduleList = jdbcTemplate.queryForList(strFetchScheduleList, fromDate);
        System.out.println(dataForFetchScheduleList);
        return dataForFetchScheduleList;
    }

    public List<Map<String, Object>> fetchJobTimingAsPerJobDate(String tenantId, String fromDate) {
        String strJobTimingAsPerJobDate = "SELECT MIN(jm.\"JOB_START_TIME\") AS earliest_time, MAX(jm.\"JOB_END_TIME\") AS lastest_time\n" +
                "FROM \"" + tenantId + "\".job_master jm\n" +
                "WHERE CAST(\"JOB_DATE\" AS DATE) = to_date( ? , 'yyyy-mm-dd');";
        System.err.println(strJobTimingAsPerJobDate);
        List<Map<String, Object>> dataForJobTimeAsPerJobDate = jdbcTemplate.queryForList(strJobTimingAsPerJobDate, fromDate);
        System.out.println(dataForJobTimeAsPerJobDate);
        return dataForJobTimeAsPerJobDate;
    }

    public void removeStaffInfoFromJobMaster(String tenentId,String pkJobId,String staffId) {
         String removeStaffInfoFromJobMaster = "UPDATE " + tenentId + ".job_master \n" +
                "SET \"STAFF_DETAILS\" = NULL\n" +
                "WHERE \"STAFF_DETAILS\" = '" + staffId + "'" +
                 "AND \"PK_JOB_ID\" ="  + pkJobId + ";";
        System.err.println(removeStaffInfoFromJobMaster);
        jdbcTemplate.update(removeStaffInfoFromJobMaster);


    }

    public void updateMaterialData(String tenantId, Integer materialId, Map<String, Object> request) {
        StringBuilder updateQuery = new StringBuilder("UPDATE " + tenantId + ".\"material_master\" SET ");
        List<Object> params = new ArrayList<>();
        boolean isUpdateRequired = false;

        if (request.containsKey("categoryId") && request.get("categoryId") != null && !request.get("categoryId").toString().isEmpty()) {
            updateQuery.append("\"FK_CATEGORY_ID\" = ?, ");
            params.add(Integer.parseInt(request.get("categoryId").toString()));
            isUpdateRequired = true;
        }
        if (request.containsKey("subcategoryId") && request.get("subcategoryId") != null && !request.get("subcategoryId").toString().isEmpty()) {
            updateQuery.append("\"FK_SUBCATEGORY_ID\" = ?, ");
            params.add(Integer.parseInt(request.get("subcategoryId").toString()));
            isUpdateRequired = true;
        }
        if (request.containsKey("materialName") && request.get("materialName") != null && !request.get("materialName").toString().isEmpty()) {
            updateQuery.append("\"MATERIAL_NAME\" = ?, ");
            params.add(request.get("materialName"));
            isUpdateRequired = true;
        }
        if (request.containsKey("materialRate") && request.get("materialRate") != null && !request.get("materialRate").toString().isEmpty()) {
            updateQuery.append("\"RATE\" = ?, ");
            params.add(request.get("materialRate"));
            isUpdateRequired = true;
        }
        if (request.containsKey("materialType") && request.get("materialType") != null && !request.get("materialType").toString().isEmpty()) {
            updateQuery.append("\"MATERIAL_TYPE\" = ?, ");
            params.add(request.get("materialType"));
            isUpdateRequired = true;
        }
        if (request.containsKey("materialRateUnitId") && request.get("materialRateUnitId") != null && !request.get("materialRateUnitId").toString().isEmpty()) {
            updateQuery.append("\"FK_MATERIAL_UNIT_ID\" = ?, ");
            params.add(Integer.parseInt(request.get("materialRateUnitId").toString()));
            isUpdateRequired = true;
        }
        if (request.containsKey("materialActiveStatus") && request.get("materialActiveStatus") != null && !request.get("materialRateUnitId").toString().isEmpty()) {
            updateQuery.append("\"MATERIAL_STATUS\" = ?, ");
            params.add(Integer.parseInt(request.get("materialActiveStatus").toString()));
            isUpdateRequired = true;
        }

        if (isUpdateRequired) {
            updateQuery.append("\"UPDATED_AT\" = current_timestamp ");
            updateQuery.append("WHERE \"PK_MATERIAL_ID\" = ?");
            params.add(materialId);

            jdbcTemplate.update(updateQuery.toString(), params.toArray());
        } else {
            throw new IllegalArgumentException("No valid fields provided for update.");
        }
    }




    public List<Map<String, Object>> dataForJobAndCustDetails(String tenantId, String fromDate, String toDate) {


        String strDataForJobAndCustDetails = "SELECT\n" +
                "    jm.\"PK_JOB_ID\" AS jobId,\n" +
                "    aj.\"PK_ASSIGNED_JOB_ID\" AS assignJobId,\n" +
                "    jm.\"JOB_STOP_ON\" AS startDate,\n" +
                "    jm.\"JOB_CREATED_AT\" AS endDate,\n" +
                "    aj.\"FK_CUSTOMER_ID\" AS customerId,\n" +
                "    cc.\"CUSTOMER_FIRST_NAME\",\n" +
                "    cc.\"CUSTOMER_LAST_NAME\"\n" +
                "FROM\n" +
                "    ac5g5624.job_master jm\n" +
                "JOIN\n" +
                "    ac5g5624.assigned_job aj ON jm.\"PK_JOB_ID\" = aj.\"FK_JOB_ID\"\n" +
                "JOIN\n" +
                "    ac5g5624.company_customer cc ON aj.\"FK_CUSTOMER_ID\" = cc.\"PK_CUSTOMER_ID\"\n" +
                "WHERE\n" +
                "    jm.\"JOB_STOP_ON\" <= TIMESTAMP '2023-08-29 00:00:00'\n" +
                "    AND jm.\"JOB_CREATED_AT\" >= TIMESTAMP '2023-08-18 00:00:00';";

        System.out.println(strDataForJobAndCustDetails);
        List<Map<String, Object>> jobAndCustDetails = jdbcTemplate.queryForList(strDataForJobAndCustDetails);

        return jobAndCustDetails;
    }

    public List<Map<String, Object>> dataServiceDetails(String tenantId) {


        String strDataServiceDetails = "select\n" +
                "    sm.\"SERVICE_ID\" as serviceId,\n" +
                "    sm.\"SERVICE_NAME\" AS serviceName,\n" +
                "    jwsm.\"JOB_ID\" AS jobId,\n" +
                "    sm.\"RATE\" AS rate\n" +
                "FROM\n" +
                "    ac5g5624.service_master sm\n" +
                "JOIN\n" +
                "    ac5g5624.job_wise_service_mapping jwsm ON sm.\"SERVICE_ID\" = CAST(jwsm.\"SERVICE_ID\" AS INTEGER);";

        System.out.println(strDataServiceDetails);
        List<Map<String, Object>> serviceDetails = jdbcTemplate.queryForList(strDataServiceDetails);

        return serviceDetails;
    }

    public List<Map<String, Object>> questionEntityDetails(String tenantId) {


        String strQuestionEntityDetails = "select \n" +
                "jwsem.\"JOB_ID\",\n" +
                "jwsem.\"QUESTION_ID\",\n" +
                "jwsem.\"ANSWER\",\n" +
                "btfe.\"PK_FORM_KEY_ID\",\n" +
                "btfe.\"INPUT_KEY\",\n" +
                "btfe.\"ANSWER_TYPE\",\n" +
                "btfe.\"OPTIONS\"\n" +
                "from\n" +
                "ac5g5624.business_type_form_entities btfe\n" +
                "join\n" +
                "ac5g5624.job_wise_service_entity_master jwsem on btfe.\"PK_FORM_KEY_ID\" = CAST(jwsem.\"QUESTION_ID\" AS INTEGER)";

        System.out.println(strQuestionEntityDetails);
        List<Map<String, Object>> dataForQuestionEntityDetails = jdbcTemplate.queryForList(strQuestionEntityDetails);

        return dataForQuestionEntityDetails;
    }

    public void updateReScheduleJob(String userId, String tenantId, String jobId, String reScheduleDate, String starttime, String endtime) {
        // Update assigned_job table
        String updateAssignedJobQuery = "UPDATE \"" + tenantId + "\".\"assigned_job\" "
                + "SET \"assigned_job_start_time\" = '" + starttime + "'::TIME, "
                + "\"assigned_job_end_time\" = '" + endtime + "'::TIME "
                + "WHERE \"FK_JOB_ID\" = " + jobId;

        // Update job_master table
        String updateJobMasterQuery = "UPDATE \"" + tenantId + "\".\"job_master\" \n" +
                "SET \"JOB_START_TIME\" = '" + starttime + "'::time, \"JOB_END_TIME\" = '" + endtime + "'::time \n" +
                "WHERE \"PK_JOB_ID\" = " + jobId;
        try {
            // Update assigned_job table
            jdbcTemplate.update(updateAssignedJobQuery);
            // Update job_master table
            jdbcTemplate.update(updateJobMasterQuery);

            System.out.println("Job rescheduled successfully.");
        } catch (DataAccessException e) {
            System.out.println("Error occurred while rescheduling the job: " + e.getMessage());
        }
    }


    public List<Map<String, Object>> getMatUnit(String tenantId) {


        String strGetMatUnit = "select \"PK_MATERIAL_UNIT_ID\" as unit_id,\n" +
                "\"MATERIAL_UNIT_NAME\" as unit_name from   \"" + tenantId + "\".\"material_rate_master\"";
        System.out.println(strGetMatUnit);
        List<Map<String, Object>> dataForGetMatUnit = jdbcTemplate.queryForList(strGetMatUnit);

        return dataForGetMatUnit;

    }

    public void updateMat(String tenantId, String jobId, String materialsId) {
        String strUpdateMat = "update \n" +
                "  \"" + tenantId + "\".\"job_master\" \n" +
                "set \n" +
                "\"JOB_MATERIAL\"='" + materialsId + "'\n" +
                "where \"PK_JOB_ID\" = " + jobId + " \n";
        System.out.println(strUpdateMat);

        jdbcTemplate.update(strUpdateMat);
    }

    public void updatejobDetails(String tenantId, String jobId, String jobMaterial, String jobNote, String jobLocation, String staffDetails, String JOB_DATE, String JOB_START_TIME, String JOB_STOP_ON, String JOB_END_TIME,String paymentDuration,String paymentDeposit, String imageid) {

        String updateJob = "update \"" + tenantId + "\".job_master  set \"JOB_START_TIME\" = ? ::TIME ,\n" +
                "  \"JOB_END_TIME\"= ? ::TIME,\n" +
                "  \"JOB_DATE\"= ? ::DATE,\"JOB_MATERIAL\" = ? , " +
                "  \"JOB_NOTES\" = ? ,\"JOB_STOP_ON\"= ? ::DATE," +
                "  \"JOB_UPDATED_AT\"= current_timestamp ,\"JOB_LOCATION\" = ? ,\n" +
                "  \"STAFF_DETAILS\" = ?, \"PAYMENT_DURATION\" = ?, \"PAYMENT_DEPOSIT\" = ?, \"IMAGE_AUDIT_ID\"= ?  where \"PK_JOB_ID\" = ? \n" +
                "  ";

        int update = jdbcTemplate.update(updateJob, JOB_START_TIME, JOB_END_TIME, JOB_DATE,
                jobMaterial, jobNote, JOB_STOP_ON, jobLocation, staffDetails,paymentDuration,paymentDeposit, imageid, Integer.parseInt(jobId));
       // System.err.println(update);
    }

    public void updateService(String tenantId, String jobId, String serviceId) {
        String strUpdateService = "update \n" +
                " \"" + tenantId + "\".\"job_wise_service_mapping\"  \n" +
                "set \n" +
                "\"SERVICE_ID\"='" + serviceId + "'\n" +
                "where \"JOB_ID\" = '" + jobId + "'\n";
       // System.out.println(strUpdateService);

        jdbcTemplate.update(strUpdateService);
    }

    public void updateCust(String tenantId, String jobId, String customerId, String ServiceEntityId) {
            String deleteJobCustData = "delete from \"" + tenantId + "\".job_wise_customer_and_serviceentity_mapping where \"PK_JOB_ID\" = ? AND \"PK_CUSTOMER_ID\" = ?";
            int update = jdbcTemplate.update(deleteJobCustData, Integer.parseInt(jobId), Integer.parseInt(customerId));
            String strInsertAssignJobData = "INSERT INTO \"" + tenantId + "\".\"job_wise_customer_and_serviceentity_mapping\" " +
                    "(\"PK_JOB_ID\",\"PK_CUSTOMER_ID\", \"PK_ENTITY_ID\") " +
                    "VALUES (" +
                    jobId + "," + customerId + ",'" + ServiceEntityId + "') ";
            jdbcTemplate.update(strInsertAssignJobData);
    }

    public void updateCustEntity(String tenantId, String jobId) {

            String deleteJobCustData = "delete from \"" + tenantId + "\".job_wise_customer_and_serviceentity_mapping where \"PK_JOB_ID\" = ?";
            int update = jdbcTemplate.update(deleteJobCustData, Integer.parseInt(jobId));
    }

    public List<Map<String, Object>> getAssignJobData(String tenantId, String jobId) {

        String strGetAssignJobData = "select \n" +
                "\"FK_CUSTOMER_ID\",\n" +
                "\"ASSIGNED_JOB_DATE\",\n" +
                "\"ASSIGNED_JOB_PAYMENT_STATUS\",\n" +
                "\"ASSIGNED_JOB_STATUS\",\n" +
                "\"SCHEDULE_TIME\"\n" +
                "from   \"" + tenantId + "\".\"assigned_job\" where \"FK_JOB_ID\" = " + jobId + " ";

        //System.out.println(strGetAssignJobData);
        List<Map<String, Object>> dataStrGetAssignJobData = jdbcTemplate.queryForList(strGetAssignJobData);

        return dataStrGetAssignJobData;
    }

    public void deleteAssignJobData(String tenantId, String jobId, String staffId) {

        String strDeleteAssignJobData = " delete from  \"" + tenantId + "\".\"assigned_job\"  where \"FK_USER_ID\" = " + staffId + "";
        //System.out.println(strDeleteAssignJobData);

        jdbcTemplate.update(strDeleteAssignJobData);
    }

    public void deleteAllAssignJobData(String tenantId, String jobId) {

        String strDeleteAssignJobData = " delete from  \"" + tenantId + "\".\"assigned_job\"  where \"FK_JOB_ID\" = " + jobId + "";
        //System.out.println(strDeleteAssignJobData);
        jdbcTemplate.update(strDeleteAssignJobData);
    }

    public void updateStaffDetails(String tenantId, String jobId, String endTime, String startDate, String startTime, String endDate, String staffId) {


        String strUpdateStaffDetails = " insert into  \"" + tenantId + "\".\"assigned_job\"  (\n" +
                "\"PK_ASSIGNED_JOB_ID\",\n" +
                "\"FK_JOB_ID\",\n" +
                "\"FK_USER_ID\",\n" +
                "\"assigned_job_end_date\",\n" +
                "\"ASSIGNED_JOB_DATE\",\n" +
                "\"assigned_job_start_time\",\n" +
                "\"assigned_job_end_time\"\n" +
                ") values((SELECT COALESCE((SELECT MAX(\"PK_ASSIGNED_JOB_ID\") FROM   \"" + tenantId + "\".\"assigned_job\"  ), 0   ) + 1), " + jobId + ", " + staffId + ", '" + endDate + "'::DATE, '" + startDate + "'::DATE, '" + startTime + "'::TIME, '" + endTime + "'::TIME) ";


        int update = jdbcTemplate.update(strUpdateStaffDetails);
       // System.out.println(update);

    }

    public void addServiceEntityQuestionsList(String tenantId, Integer customerId, Integer questionId, String questionName, String answer, Integer answerTypeId, Integer pkServiceEntityId) {

        String sql = "INSERT INTO \"" + tenantId + "\".\"customer_wise_service_entity\" (\"PK_ID\", \"FK_CUSTOMER_ID\", \"FK_QUESTION_ID\", \"QUESTION\", \"ANSWER\", \"ANSWER_TYPE_ID\", \"FK_SERVICE_ENTITY\") " +
                "VALUES ((SELECT COALESCE((SELECT MAX(\"PK_ID\") FROM \"" + tenantId + "\".\"customer_wise_service_entity\"), 0) + 1), ?, ?, ?, ?, ?, ?)";
        Object[] params = {
                customerId,
                questionId != null ? questionId : 0,
                questionName != null ? questionName : "",
                answer != null ? answer : "",
                answerTypeId != null ? answerTypeId : 0,
                pkServiceEntityId
        };
        jdbcTemplate.update(sql, params);
    }

    public Integer insertCustWiseServiceEntity(String tenantId, String customerId, Integer pkJobId, String serviceEntityName) {

        /*String strInsertCustWiseServiceEntity = "INSERT INTO \"" + tenantId + "\".\"customer_service_entity_mapping\" " +
                "(\"PK_SERVICE_ENTITY\", \"FK_CUSTOMER_ID\", \"FK_JOB_ID\", \"SERVICE_ENTITY_NAME\") " +
                "VALUES " +
                "((SELECT COALESCE((SELECT MAX(\"PK_SERVICE_ENTITY\") FROM \"" + tenantId + "\".\"customer_service_entity_mapping\"), 0) + 1), " +
                customerId + ", " + pkJobId + ", '" + serviceEntityName + "') " +
                "RETURNING \"PK_SERVICE_ENTITY\"";

        Integer pkServiceEntityId = jdbcTemplate.queryForObject(strInsertCustWiseServiceEntity, Integer.class);
        return pkServiceEntityId;*/

        String sql = "INSERT INTO \"" + tenantId + "\".\"customer_service_entity_mapping\" " +
                "(\"PK_SERVICE_ENTITY\", \"FK_CUSTOMER_ID\", \"FK_JOB_ID\", \"SERVICE_ENTITY_NAME\") " +
                "VALUES " +
                "((SELECT COALESCE((SELECT MAX(\"PK_SERVICE_ENTITY\") FROM \"" + tenantId + "\".\"customer_service_entity_mapping\"), 0) + 1), ?, ?, ?) " +
                "RETURNING \"PK_SERVICE_ENTITY\"";
        Object[] params = {
                Integer.valueOf(customerId),
                pkJobId != null ? pkJobId : 0,
                serviceEntityName != null ? serviceEntityName : ""
        };
        try {
            Integer pkServiceEntityId = jdbcTemplate.queryForObject(sql, params, Integer.class);
            return pkServiceEntityId;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<Map<String, Object>> getCustWiseServiceEntity(String tenantId, Integer customerId) {

        String strGetCustWiseServiceEntity = " select \"FK_SERVICE_ENTITY\" as pkServiceEntityId, \"ANSWER\" as serviceEntityName from \n" +
                "    \"" + tenantId + "\".\"customer_wise_service_entity\"  where \"FK_CUSTOMER_ID\" = " + customerId + "  AND \"QUESTION\" = 'Name'";
        List<Map<String, Object>> dataForGetCustWiseServiceEntity = jdbcTemplate.queryForList(strGetCustWiseServiceEntity);
        return dataForGetCustWiseServiceEntity;
    }

    public ArrayList<HashMap<String, String>> getstaffWistJobdetails(LocalDateTime startDateAndTime, LocalDateTime endDateAndTime, String staffId, String tenantId) {

        LocalTime startTime = startDateAndTime.toLocalTime();
        LocalTime endTime = endDateAndTime.toLocalTime();

        String getAllStaffJobs = "SELECT * FROM " + tenantId + ".assigned_job aj " +
                "WHERE aj.\"ASSIGNED_JOB_DATE\" BETWEEN '" + startDateAndTime + "' AND '" + endDateAndTime + "' " +
                "AND aj.\"assigned_job_start_time\" = '" + startTime + "' AND aj.\"assigned_job_end_time\" = '" + endTime + "'" +
                " AND aj.\"FK_USER_ID\" = " + Integer.parseInt(staffId);

        //System.out.println(getAllStaffJobs);
        List<Map<String, Object>> dataForGetCustWiseServiceEntity = jdbcTemplate.queryForList(getAllStaffJobs);

        ArrayList<HashMap<String, String>> allStaffTime = new ArrayList<>();

        for (Map<String, Object> staffJoblist : dataForGetCustWiseServiceEntity) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            DateFormat timeFormat = new SimpleDateFormat("HH:mm");

            HashMap<String, String> allSearchDate = new HashMap<>();
            String startDateJob = dateFormat.format(staffJoblist.get("ASSIGNED_JOB_DATE"));
            String startTimeJob = timeFormat.format(staffJoblist.get("assigned_job_start_time"));
            String endDateJob = dateFormat.format(staffJoblist.get("assigned_job_end_date"));
            String endTimeJob = timeFormat.format(staffJoblist.get("assigned_job_end_time"));

            allSearchDate.put("startTime", startDateJob + " " + startTimeJob);
            allSearchDate.put("endTime", endDateJob + " " + endTimeJob);

            allStaffTime.add(allSearchDate);
        }

        //System.err.println(allStaffTime);
        return allStaffTime;
    }


//    public ArrayList<HashMap<String, String>> getstaffWistJobdetails(LocalDateTime startDateAndTime, LocalDateTime endDateAndTime, String staffId, String tenantId) {
//
//        LocalTime startTime = startDateAndTime.toLocalTime();
//
//        LocalTime endTime = endDateAndTime.toLocalTime();
//
//
//        String getAllstaffJobs = "SELECT * FROM " + tenantId + ".assigned_job aj " +
//                "WHERE aj.\"ASSIGNED_JOB_DATE\" BETWEEN '" + startDateAndTime + "' AND '" + endDateAndTime + "' " +
//                "AND aj.\"assigned_job_start_time\" '"+startTime+"' AND aj.\"assigned_job_end_time\" '"+endTime+"'" +
//                " aj.\"FK_USER_ID\" = " + Integer.parseInt(staffId);
//
//        System.out.println(getAllstaffJobs);
//        List<Map<String, Object>> dataForGetCustWiseServiceEntity = jdbcTemplate.queryForList(getAllstaffJobs);
//
//        ArrayList<HashMap<String, String>> all_staff_time = new ArrayList<>();
//
//        for (Map<String, Object> staffJoblist : dataForGetCustWiseServiceEntity) {
//            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//            DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
//
//            HashMap<String, String> allSearchDate = new HashMap<>();
//            String startDate_job = dateFormat.format(staffJoblist.get("ASSIGNED_JOB_DATE"));
//            String starttime_job = timeFormat.format(staffJoblist.get("assigned_job_start_time"));
//            String endDate_job = dateFormat.format(staffJoblist.get("assigned_job_end_date"));
//            String endtime_job = timeFormat.format(staffJoblist.get("assigned_job_end_time"));
//
//            allSearchDate.put("startTime", startDate_job + " " + starttime_job);
//            allSearchDate.put("endTime", endDate_job + " " + endtime_job);
//
//            all_staff_time.add(allSearchDate);
//        }
//
//        System.err.println(all_staff_time);
//        return all_staff_time;
//    }

    public ArrayList<HashMap<String, String>> getJobdetailsofCertainDateRange(String startDate, String endDate, String staffId, String tenantId) {


        String getJobdetails = "SELECT * " +
                "FROM " + tenantId + ".job_master aj " +
                "WHERE aj.\"JOB_DATE\" BETWEEN '" + startDate + "' AND '" + endDate + "' ";
        List<Map<String, Object>> dataForGetCustWiseServiceEntity = jdbcTemplate.queryForList(getJobdetails);

        ArrayList<HashMap<String, String>> all_staff_time = new ArrayList<>();

        for (Map<String, Object> staffJoblist : dataForGetCustWiseServiceEntity) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");


            HashMap<String, String> allSearchDate = new HashMap<>();
            String startDate_job = dateFormat.format(staffJoblist.get("JOB_DATE"));
            String starttime_job = timeFormat.format(staffJoblist.get("JOB_START_TIME"));
            String endDate_job = dateFormat.format(staffJoblist.get("JOB_STOP_ON"));
            String endtime_job = timeFormat.format(staffJoblist.get("JOB_END_TIME"));

            allSearchDate.put("startTime", startDate_job + " " + starttime_job);
            allSearchDate.put("endTime", endDate_job + " " + endtime_job);

            all_staff_time.add(allSearchDate);


        }

        //System.err.println(all_staff_time);
        return all_staff_time;
    }


    /*
     * @Author AGNIC BISWAS
     * THIS METHOD USED FOR Save the time interval data
     * @PARAM  TANENT ID,startTime,endDate,interval,userId
     * */


    public String saveCompanyTimeInterval(String tenantId, LocalDate startDate, String interval, String userId) {
        java.sql.Date sqlStartDate = java.sql.Date.valueOf(startDate);
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        Timestamp sqlNow = Timestamp.valueOf(now);
        String sql1 = "SELECT * FROM \"Bizfns\".\"COMPANY_TIME_INTERVAL\" " +
                "WHERE \"TENANT_ID\" = ? AND \"USER_ID\" = ? AND \"START_DATE\" = ? ";
        List<Map<String, Object>> allTimeIntervalData = jdbcTemplate.queryForList(sql1, tenantId, userId, sqlStartDate);

        if (allTimeIntervalData.isEmpty()) {
            String insertSql = "INSERT INTO \"Bizfns\".\"COMPANY_TIME_INTERVAL\" " +
                    "(\"TENANT_ID\", \"START_DATE\", \"INTERVAL_TIME\", \"USER_ID\", \"CREATED_TIME\") " +
                    "VALUES (?, ?, ?, ?, current_timestamp)";
            try {
                jdbcTemplate.update(insertSql, tenantId, sqlStartDate, interval, userId);
                return "Data inserted successfully";
            } catch (DataAccessException e) {
                return "Error inserting data: " + e.getMessage();
            }
        } else {
            String updateSql = "UPDATE \"Bizfns\".\"COMPANY_TIME_INTERVAL\" " +
                    "SET \"INTERVAL_TIME\" = ?, \"CREATED_TIME\" = current_timestamp " +
                    "WHERE \"TENANT_ID\" = ? AND \"USER_ID\" = ? AND \"START_DATE\" = ?";
            try {
                jdbcTemplate.update(updateSql, interval, tenantId, userId, sqlStartDate);
                return "Edit successfully";
            } catch (DataAccessException e) {
                return "Error updating data: " + e.getMessage();
            }
        }
    }

    public String intervalDuration(String tenantId, LocalDate startDate, String userId) throws ParseException {
        String sql = "SELECT \"START_TIME\", \"END_TIME\"" +
                "FROM \"Bizfns\".\"COMPANY_TIME_INTERVAL\" " +
                "WHERE \"TENANT_ID\" = ? AND \"USER_ID\" = ? ORDER BY \"START_DATE_WORKING_HOUR\" ASC FETCH FIRST ROW ONLY";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, tenantId, userId);
        int diff = 0;
        SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");
        if (!results.isEmpty()) {
            for (Map<String, Object> result : results) {
                String earliestTimeStr = (String) result.get("START_TIME");
                String latestTimeStr = (String) result.get("END_TIME");
                    Date earliestTime = timeFormatter.parse(earliestTimeStr);
                    Date latestTime = timeFormatter.parse(latestTimeStr);
                    int earliestHour = earliestTime.getHours();
                    int latestHour = latestTime.getHours();
                    diff = latestHour - earliestHour;
            }
        }
        return String.valueOf(diff);
    }



//        String resp;
//        String format = "yyyy-MM-dd HH:mm:ss";
//        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
//        String sql = "SELECT * FROM " + tenantId + ".company_wise_time_interval";
//        List<Map<String, Object>> allTimeIntervalData = jdbcTemplate.queryForList(sql);
//
//        if (allTimeIntervalData.size() == 0) {
//            String sql1 = "INSERT INTO " + tenantId + ".company_wise_time_interval (\"ID\",\"customer_id\",\"start_time\",\"end_time\",\"interval_time\") VALUES ((SELECT COALESCE((SELECT MAX(\"ID\") FROM \"" + tenantId + "\".\"company_wise_time_interval\"),0)+1),?,?,?,?)";
//
//            jdbcTemplate.update(sql1, (Integer.parseInt(userId)), (startTime), (endDate), (interval));
//            resp = "Save Successfully";
//        } else {
//            String EDITSQL = "UPDATE " + tenantId + ".company_wise_time_interval " +
//                    "SET \"start_time\" = ?, " +
//                    "    \"end_time\" = ?, " +
//                    "    \"interval_time\" = ? " +
//                    "WHERE \"customer_id\" = ?";
//
//            System.err.println(EDITSQL);
//            jdbcTemplate.update(EDITSQL, (startTime), (endDate), (interval), (Integer.parseInt(userId)));
//
//            resp = "edit Successfully";
//        }
//        return resp;

    /*
     * @Author AGNIC BISWAS
     * THIS METHOD WORK FOR Get all timeintervaldata
     * @PARAM  TANENT ID
     * */

    public List getCompanyTimeInterval(String tenantId) {
        String sql = "SELECT * FROM " + tenantId + ".company_wise_time_interval";
        List<Map<String, Object>> allTimeIntervalData = jdbcTemplate.queryForList(sql);
        return allTimeIntervalData;
    }

    /*
     * @Author AGNIC BISWAS
     * THIS METHOD USED FOR CALCULATE THE TOTAL WAGES
     * @PARAM JOB ID AND TANENT ID
     * */
    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty() || value == "null";
    }

    /**
     * Retrieves job schedule details including staff, materials, and pricing calculations based on job ID and tenant ID.
     *
     * @param jobid    The ID of the job for which details are requested.
     * @param tanentid The ID of the tenant to which the job belongs.
     * @return ResponseEntity containing job schedule details including pricing and items, or an error message if no data is found.
     * @throws CustomException if there's an issue with retrieving data or performing calculations.
     */
    public ResponseEntity<GlobalResponseDTO> getjobScheduleCalculation(String jobid, String tanentid) throws CustomException {
        Map<String, Object> responceJobprice = new HashMap<>();
        DecimalFormat df = new DecimalFormat("0.00");
        //Query for job details as per the job id
        String getJob = "SELECT * FROM " + tanentid + ".job_master WHERE \"PK_JOB_ID\" = ?";
        List<Map<String, Object>> allTimeIntervalData = jdbcTemplate.queryForList(getJob, Integer.parseInt(jobid));
        double time_wages = 10.00;
        double MeterialSum = 0.0;
        double totalHourwages = 0.0;
        double totalTax = 18.0;
        double extraMin = 0.0;
        if (allTimeIntervalData.isEmpty()) {
            return ResponseEntity.accepted()
                    .body(new GlobalResponseDTO(false, "No data found for this Criteria", null));
        } else {
            Map<String, Object> itemAtIndex2 = allTimeIntervalData.get(0);

            // starttime of a job
            String startDate = String.valueOf(itemAtIndex2.get("JOB_DATE"));
            // endtime of a job
            String endDate = String.valueOf(itemAtIndex2.get("JOB_STOP_ON"));
            // starttime of a job
            String startTime = String.valueOf(itemAtIndex2.get("JOB_START_TIME"));
            // endtime of a job
            String endTime = String.valueOf(itemAtIndex2.get("JOB_END_TIME"));
            String JOB_MATERIAL = String.valueOf(itemAtIndex2.get("JOB_MATERIAL"));
            String JOB_STAFF = String.valueOf(itemAtIndex2.get("STAFF_DETAILS"));
            String JOB_LOCATION = String.valueOf(itemAtIndex2.get("JOB_LOCATION"));
            String JOB_NOTES = String.valueOf(itemAtIndex2.get("JOB_NOTES"));

            if (isNullOrEmpty(JOB_STAFF)) {
                // throw new CustomException("First you havt to add Staff to proceed");
                return ResponseEntity.accepted()
                        .body(new GlobalResponseDTO(false, "First you havt to add Staff to proceed", null));
            }


            //for multiple Service and multiple customer


            String customerAndServiceQuery = "select * from \"" + tanentid + "\".job_wise_customer_and_serviceentity_mapping jwcasm where jwcasm.\"PK_JOB_ID\" = ?";
            List<Map<String, Object>> Job_customer = jdbcTemplate.queryForList(customerAndServiceQuery, Integer.parseInt(jobid));
            if (Job_customer.isEmpty()) {
                responceJobprice.put("customerList", new ArrayList<>());
            } else {
                ArrayList<Map<String, Object>> custList = new ArrayList<>();
                try {
                    for (Map<String, Object> customerMappedData : Job_customer) {
                        String customerId = String.valueOf(customerMappedData.get("PK_CUSTOMER_ID"));
                        String serviceEntity_arr = (String) customerMappedData.get("PK_ENTITY_ID");
                        String customerDetailsQuery = "select cc.\"CUSTOMER_FIRST_NAME\", cc.\"CUSTOMER_LAST_NAME\", cc.\"PK_CUSTOMER_ID\" " +
                                "from \"" + tanentid + "\".company_customer cc " +
                                "where cc.\"PK_CUSTOMER_ID\" = ?";
                        List<Map<String, Object>> customerDetails = jdbcTemplate.queryForList(customerDetailsQuery, Integer.parseInt(customerId));

                        Map<String, Object> customerList = customerDetails.get(0);

                        ArrayList<Integer> serviceEntityList = Arrays.stream(serviceEntity_arr.split(","))
                                .map(String::trim).map(Integer::parseInt) // Trim each element to remove leading/trailing whitespace
                                .collect(Collectors.toCollection(ArrayList::new));
                       // System.err.println("serviceEntityList:::" + serviceEntityList);
                        String serviceEntityDetailsQuerys = "select csem.\"SERVICE_ENTITY_NAME\",csem.\"PK_SERVICE_ENTITY\" " +
                                "from \"" + tanentid + "\".customer_service_entity_mapping csem " +
                                "where csem.\"PK_SERVICE_ENTITY\" IN (:serviceEntityIds)";
                        Map<String, List<Integer>> paramMap = Collections.singletonMap("serviceEntityIds", serviceEntityList);
                        List<Map<String, Object>> entityList = namedParameterJdbcTemplate.queryForList(serviceEntityDetailsQuerys, paramMap);
                        customerList.put("ServiceEntityList", entityList);
                        custList.add(customerList);
                        responceJobprice.put("CustomersList", custList);

                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
            //for staff
            ArrayList<Map<String, String>> StaffList = new ArrayList<>();
            StringBuilder staffBuilder = new StringBuilder();
            //if(!JOB_STAFF.isEmpty()){
            ArrayList<Integer> staffList = Arrays.stream(JOB_STAFF.split(","))
                    .map(Integer::parseInt)
                    .collect(Collectors.toCollection(ArrayList::new));
            Map<String, String> diff = diff_startDate_and_endDate(startTime, endTime);
            Optional<Integer> hours = Optional.of(Integer.parseInt(diff.get("hours")));
            Optional<Integer> minutes = Optional.of(Integer.parseInt(diff.get("minutes")));

            if (minutes.get() != 0 && minutes.isPresent()) {
                extraMin = 1.00;
            }
           // System.err.println(hours.get());

            for (Integer staffpart : staffList) {
                Map<String, String> collectStaffDetails = new HashMap<>();
                //Query for Material price  details as per each Material type
                String getMaterial_query = "SELECT * FROM " + tanentid + ".company_user WHERE \"PK_USER_ID\" = ?";
                // list of the material price
                List<Map<String, Object>> allStaffDetails = jdbcTemplate.queryForList(getMaterial_query, (staffpart));
                Map<String, Object> all_staffDetails = allStaffDetails.get(0);
                String First_staffName = (String) all_staffDetails.get("USER_FIRST_NAME");
                String last_staffName = (String) all_staffDetails.get("USER_LAST_NAME");
                String hourlyCharge = String.valueOf(all_staffDetails.get("USER_CHARGE_RATE"));
                collectStaffDetails.put("fullName", First_staffName + " " + last_staffName);
                collectStaffDetails.put("workingHours", (String.valueOf(hours.get() + extraMin)));
                collectStaffDetails.put("hourlyCharge", hourlyCharge);
                collectStaffDetails.put("totalwokingcharges", String.valueOf(Double.parseDouble(hourlyCharge) * hours.get()));
                totalHourwages = totalHourwages + Double.parseDouble(hourlyCharge) * (hours.get() + extraMin);
                staffBuilder.append(First_staffName).append(" ").append(last_staffName).append(",");
                StaffList.add(collectStaffDetails);


            }
            //System.err.println(staffBuilder.deleteCharAt(staffBuilder.length() - 1));

            //}

            // for Materials List

            ArrayList<Map<String, String>> MaterialList = new ArrayList<>();
            if (JOB_MATERIAL.isEmpty()) {
                MaterialList = new ArrayList<>();
            } else {
                String[] Material_parts = JOB_MATERIAL.split(",");
                ArrayList<Integer> intList = Arrays.stream(Material_parts)
                        .map(Integer::parseInt)
                        .collect(Collectors.toCollection(ArrayList::new));

                for (Integer part : intList) {
                    Map<String, String> MaterialNames = new HashMap<>();

                    //Query for Material price  details as per each Material type
                    String getMaterial_query = "SELECT * FROM " + tanentid + ".material_master WHERE \"PK_MATERIAL_ID\" = ?";
                    // list of the material price
                    List<Map<String, Object>> allMaterial_price = jdbcTemplate.queryForList(getMaterial_query, (part));
                    Map<String, Object> allMaterial_priceObj = allMaterial_price.get(0);
                    String MaterialPrice = String.valueOf(allMaterial_priceObj.get("RATE"));
                    String MaterialName = (String) allMaterial_priceObj.get("MATERIAL_NAME");
                    MaterialNames.put("MaterialName", MaterialName);
                    MaterialNames.put("MaterialPrice", MaterialPrice);
                    MaterialNames.put("MaterialQuantity", "1");
                    MaterialList.add(MaterialNames);
                    MeterialSum = MeterialSum + Double.parseDouble(MaterialPrice);

                }
            }


            //response Map contain sub_total,tax_sum,grand_total;

           // System.err.println(totalHourwages);
            double sub_total = (totalHourwages + MeterialSum);
            double tax_sum = (totalHourwages + MeterialSum) * (totalTax / 100);
            String grand_total = df.format(Math.round((sub_total + tax_sum)));
            responceJobprice.put("sub_total", df.format(Math.round(totalHourwages + MeterialSum)));
            responceJobprice.put("tax_sum", df.format(Math.round(((totalHourwages + MeterialSum) * (totalTax / 100)))));
            responceJobprice.put("grand_total", grand_total);
            responceJobprice.put("Material_details", MaterialList);
            responceJobprice.put("totalworkinghours", String.valueOf(hours.get()));
            responceJobprice.put("startDate", startDate);
            responceJobprice.put("endDate", endDate);
            responceJobprice.put("jobId", jobid);
            responceJobprice.put("Staffname", staffBuilder);
            responceJobprice.put("StaffList", StaffList);
            responceJobprice.put("tenantId", tanentid);
            responceJobprice.put("joblocation", JOB_LOCATION);
            responceJobprice.put("jobnote", JOB_NOTES);
        }


        //return responceJobprice;
        return ResponseEntity.accepted()
                .body(new GlobalResponseDTO(true, "Success", responceJobprice));
    }

    /*
     * AMIT KUMAR SINGH
     * This method is used to find the differnce between startDate and endDate
     * @PARAM  startTime,endTime
     * */
    public Map<String, String> diff_startDate_and_endDate(String startTime, String endTime) {
        // Parse the time strings into LocalTime objects
        LocalTime startTime_current = LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm:ss"));
        LocalTime endTime_current = LocalTime.parse(endTime, DateTimeFormatter.ofPattern("HH:mm:ss"));

        // Calculate the duration (difference) between start and end times
        Duration duration = Duration.between(startTime_current, endTime_current);

        // Convert the duration to hours, minutes, and seconds
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        Map<String, String> timeDiffObj = new HashMap<>();
        timeDiffObj.put("hours", String.valueOf(hours));
        timeDiffObj.put("minutes", String.valueOf(minutes));
        return timeDiffObj;
    }


    //Currently Not used
/*    public String saveMediaFile(MultipartFile[] file, String tenantId, String Pkjobid) throws IOException, CustomException {
        double totalSize = Arrays.stream(file)
                .mapToDouble(multifile -> ((double) multifile.getSize() / 1048576))
                .sum();
        System.err.println(totalSize);
        try {
            double CompanyConsumeData = 0.00;
            if (totalSize > 1) {
                throw new CustomException("File size exceeds the maximum allowed size of 1MB.");

            } else if ((100 - CompanyConsumeData) < totalSize) {
                throw new CustomException("Company has total consume data is exceeds");

            } else {
                Arrays.stream(file).forEach(multifile -> {
                    LocalDateTime currentTimestamp = LocalDateTime.now();

                    String sql1 = "INSERT INTO " + tenantId + ".media (\"PK_MEDIA_ID\",\"FILE_NAME\",\"MEDIA_CONTENT\",\"CONTENT_TYPE\",\"UPLOAD_DATE_TIME\",\"PK_JOB_ID\") VALUES ((SELECT COALESCE((SELECT MAX(\"PK_MEDIA_ID\") FROM \"" + tenantId + "\".\"media\"),0)+1),?,?,?,?,?)";
                    try {
                        jdbcTemplate.update(sql1, multifile.getOriginalFilename(), (multifile.getBytes()), multifile.getContentType(), currentTimestamp, Integer.parseInt(Pkjobid));
                    } catch (IOException e) {
                        throw new RuntimeException(e);

                    }
                });
            }
        } catch (CustomException e) {
            System.err.println("Caught a CustomException: " + e.getMessage());
            return e.getMessage();
            // Handle the exception as needed
        }


        return file.length + " Media File are Successfully saved";
    }

*/


    public String deleteMediaFile(String tenantId, String imageAuditID, String path) {

        /*String sql = "DELETE FROM " + tenantId + ".media WHERE \"IMAGE_AUDIT_ID\" = '" + imageAuditID + "' RETURNING \"FILE_NAME\"";
        String imageName = jdbcTemplate.queryForObject(sql, String.class);
        String filepath = path + File.separator + imageName;
        File f = new File(filepath);
        if (f.exists()) {
            f.delete();
        }*/
        return "Deleted";
    }

    public void deleteMedFile(String tenantId, String mediaId) {

        String sql = "DELETE FROM " + tenantId + ".media WHERE \"PK_MEDIA_ID\" = " + Integer.parseInt(mediaId) + " RETURNING \"FILE_NAME\"";
        String imageName = jdbcTemplate.queryForObject(sql, String.class);
    }

    /*
     * @Author AGNIC BISWAS
     * THIS METHOD USED FOR Multiple MediaFile
     * @PARAM  path,file,tenantId,pkJobId
     * */
    public ArrayList<Map<String, Object>> uploadFile(String path, MultipartFile[] file, String auditId) {
        double totalSize = 0.0;
        ArrayList<Map<String, Object>> ImageList = new ArrayList<>();
        for (MultipartFile multifile : file) {
            String auditNumber = "";
            Map<String, Object> imageMap = new HashMap<>();
            String name = multifile.getOriginalFilename();
            totalSize += (double) multifile.getSize() / 1048576;
            String filepath = path + File.separator + name;
            File f = new File(path);
            if (!f.exists()) {
                f.mkdir();
            }
            try {
                Files.copy(multifile.getInputStream(), Paths.get(filepath));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Random random = new Random(UUID.randomUUID().getMostSignificantBits());
            if (auditId == null || auditId.isEmpty() || auditId.equals("null")) {
                auditNumber = "image" + random.nextInt();
            } else {
                auditNumber = auditId;
            }
            imageMap.put("imageName", name);
            imageMap.put("contentType", multifile.getContentType());
            imageMap.put("AuditId",auditNumber);
            ImageList.add(imageMap);
        }
        try {
            double CompanyConsumeData = 0.00;
        } catch (Exception e) {
        }
        return ImageList;
    }

    public String saveMediaImage(String tenantId, String pkJobId, String path, MultipartFile[] file, String auditId) {
        ArrayList<Map<String, Object>> maps = uploadFile(path, file, auditId);
        Random random = new Random(UUID.randomUUID().getMostSignificantBits());

        StringBuilder auditNumbersBuilder = new StringBuilder();
        for (Map<String, Object> images : maps) {
            String auditNumber = "";
            String name = (String) images.get("imageName");
            String modeuleName = "Save Media";
            auditNumber = (String) images.get("AuditId");
            if (!auditNumbersBuilder.toString().isEmpty()) {
                auditNumbersBuilder.append(",");
            }
            auditNumbersBuilder.append(auditNumber);
            LocalDateTime currentTimestamp = LocalDateTime.now();
            String sql1 = "INSERT INTO " + tenantId + ".media (\"PK_MEDIA_ID\",\"MEDIA_MODULE_NAME\",\"FK_MODULE_PRIMARY_ID\",\"FILE_NAME\",\"UPLOAD_DATE_TIME\",\"JOB_ID\",\"IMAGE_AUDIT_ID\") VALUES ((SELECT COALESCE((SELECT MAX(\"PK_MEDIA_ID\") FROM \"" + tenantId + "\".\"media\"),0)+1),?,?,?,?,?,?)";
            jdbcTemplate.update(sql1, modeuleName, Integer.parseInt(pkJobId), name, currentTimestamp, Integer.parseInt(pkJobId), auditNumber);
        }
        return auditNumbersBuilder.toString();
    }

    public InputStream getMediaFile(String path, String fileName) throws FileNotFoundException {
        String fullpath = path + File.separator + fileName;
        InputStream is = new FileInputStream(fullpath);
        return is;
    }

    public InputStream getInvoice(String path, String fileName) throws FileNotFoundException {
        String fullpath = path + File.separator + fileName;
        InputStream is = new FileInputStream(fullpath);
        return is;

    }

    public List<Map<String, Object>> getServiceEntityFieldsHistory(String tenantId, String serviceEntityId) {

        String strGetServiceEntityFieldsHistory = " \tSELECT\n" +
                "    cwse.\"FK_QUESTION_ID\" as type,\n" +
                "    cwse.\"ANSWER\",\n" +
                "    btfe.\"ANSWER_TYPE\" as \"ANSWER_TYPE\",\n" +
                "    btfe.\"INPUT_KEY\"  as question,\n" +
                "    btfe.\"GROUP_BY\", \n" +
                "    btfe.\"OPTIONS\" as items \n" +
                "    FROM\n" + tenantId +
                ".customer_wise_service_entity cwse\n" +
                "JOIN\n" +
                "    \"Bizfns\".\"BUSINESS_TYPE_FORM_ENTITIES\" btfe\n" +
                "ON\n" +
                "    cwse.\"FK_QUESTION_ID\" = btfe.\"PK_FORM_KEY_ID\"\n" +
                "WHERE\n" +
                "    cwse.\"FK_SERVICE_ENTITY\" = ? ORDER BY cwse.\"FK_QUESTION_ID\" ASC";
        List<Map<String, Object>> dataForGetCustWiseServiceEntity = jdbcTemplate.queryForList(strGetServiceEntityFieldsHistory, Integer.parseInt(serviceEntityId));
        return dataForGetCustWiseServiceEntity;
    }


    public List<Map<String, Object>> getServiceEntityFieldsHistoryBycustomerId(String tenantId, String serviceEntityId, String pkJobid) {

        String strGetServiceEntityFieldsHistory =
                "SELECT " +
                        "    cwse.\"FK_QUESTION_ID\" as type, " +
                        "    cwse.\"ANSWER\", " +
                        "    btfe.\"ANSWER_TYPE\" as \"ANSWER_TYPE\", " +
                        "    btfe.\"INPUT_KEY\"  as question, " +
                        "    btfe.\"GROUP_BY\", " +
                        "    btfe.\"OPTIONS\" as items " +
                        "FROM " + tenantId + ".customer_wise_service_entity cwse " +
                        "JOIN " +
                        "    \"Bizfns\".\"BUSINESS_TYPE_FORM_ENTITIES\" btfe " +
                        "ON " +
                        "    cwse.\"FK_QUESTION_ID\" = btfe.\"PK_FORM_KEY_ID\" " +
                        "WHERE " +
                        "    cwse.\"FK_SERVICE_ENTITY\" = ( " +
                        "        SELECT csem.\"PK_SERVICE_ENTITY\" " +
                        "        FROM " + tenantId + ".customer_service_entity_mapping csem " +
                        "        WHERE csem.\"FK_CUSTOMER_ID\" = ? AND csem.\"FK_JOB_ID\" = ? " +
                        "    )";

       // System.out.println(strGetServiceEntityFieldsHistory);

        List<Map<String, Object>> dataForGetCustWiseServiceEntity =
                jdbcTemplate.queryForList(strGetServiceEntityFieldsHistory, Integer.parseInt(serviceEntityId), Integer.parseInt(pkJobid));

        return dataForGetCustWiseServiceEntity;
    }


//    public List<Map<String, Object>> getMediaNameList(String imageId, String tanentId) {
//        String mediaSql = "select m.\"FILE_NAME\"  from \"" + tanentId + "\".media m where m.\"IMAGE_AUDIT_ID\" = ? ";
//        List<Map<String, Object>> mediaFileName = jdbcTemplate.queryForList(mediaSql, imageId);

    public List<Map<String,Object>> getMediaNameList(String imageId, String tanentId){
        String mediaSql="select m.\"FILE_NAME\",m.\"PK_MEDIA_ID\"  from \""+tanentId+"\".media m where m.\"IMAGE_AUDIT_ID\" = ? ";
        List<Map<String, Object>> mediaFileName = jdbcTemplate.queryForList(mediaSql,imageId);

        return mediaFileName;

    }

    public List<Map<String,Object>> getMediaNameList(String imageId, String tanentId, Integer pk_job_id){
        String mediaSql="select m.\"FILE_NAME\",m.\"PK_MEDIA_ID\"  from \""+tanentId+"\".media m where m.\"IMAGE_AUDIT_ID\" = ? and m.\"JOB_ID\" = ?";
        List<Map<String, Object>> mediaFileName = jdbcTemplate.queryForList(mediaSql,imageId,pk_job_id);

        return mediaFileName;
    }

    public List<Map<String,Object>> getMediaNameList(String tanentId, Integer pk_job_id){
        String mediaSql="select m.\"FILE_NAME\",m.\"PK_MEDIA_ID\",m.\"IMAGE_AUDIT_ID\" AS \"IMAGE_ID\"  from \""+tanentId+"\".media m where m.\"JOB_ID\" = ?";
        List<Map<String, Object>> mediaFileName = jdbcTemplate.queryForList(mediaSql,pk_job_id);
        return mediaFileName;
    }

    public List<Map<String,Object>> getMediaNameListAsPerImageId(String tanentId, String  imageAuditID){
        String mediaSql="select m.\"FILE_NAME\",m.\"PK_MEDIA_ID\",m.\"IMAGE_AUDIT_ID\" AS \"IMAGE_ID\"  from \""+tanentId+"\".media m where m.\"IMAGE_AUDIT_ID\" = ?";
        List<Map<String, Object>> mediaFileName = jdbcTemplate.queryForList(mediaSql,imageAuditID);
        return mediaFileName;
    }

    public List<Map<String,Object>> getImageAuditID(String tanentId, Integer pk_job_id){
        String mediaSql="select m.\"IMAGE_AUDIT_ID\",m.\"PK_MEDIA_ID\"  from \""+tanentId+"\".media m where m.\"JOB_ID\" = ?";
        List<Map<String, Object>> imageAuditID = jdbcTemplate.queryForList(mediaSql,pk_job_id);
        return imageAuditID;
    }

    public void updateMedia(String imageId, String tanentId, Integer pk_job_id) {
        String strupdateMedia = "update \n" +
                " \"" + tanentId + "\".\"media\"  \n" +
                "set \n" +
                "\"IMAGE_AUDIT_ID\"='" + imageId + "'\n" +
                "where \"JOB_ID\" = '" + pk_job_id + "'\n";
        System.out.println(strupdateMedia);

        jdbcTemplate.update(strupdateMedia);
    }

    public void updateFileName(String fileNameStr, String tanentId, Integer pk_job_id) {
        String strupdateMedia = "update \n" +
                " \"" + tanentId + "\".\"media\"  \n" +
                "set \n" +
                "\"FILE_NAME\"='" + fileNameStr + "'\n" +
                "where \"JOB_ID\" = '" + pk_job_id + "'\n";
        System.out.println(strupdateMedia);

        jdbcTemplate.update(strupdateMedia);
    }

    public List<Map<String, Object>> getJobHistoryAsPerCustomerId() {
        String historyQuery = "SELECT DISTINCT \n" +
                "    cc.\"PK_CUSTOMER_ID\",\n" +
                "    cc.\"CUSTOMER_FIRST_NAME\",\n" +
                "    cc.\"CUSTOMER_LAST_NAME\",\n" +
                "    m.\"FILE_NAME\",\n" +
                "    jm.\"JOB_DATE\",\n" +
                "    jm.\"JOB_MATERIAL\",\n" +
                "    jm.\"JOB_NOTES\",\n" +
                "    jm.\"JOB_LOCATION\",\n" +
                "    jm.\"JOB_START_TIME\",\n" +
                "    jm.\"JOB_END_TIME\",\n" +
                "    btwsm.\"SERVICE_NAME\" \n" +
                "FROM \n" +
                "    agni2803.job_master jm \n" +
                "JOIN\n" +
                "    agni2803.job_wise_customer_and_serviceentity_mapping jwcasm ON jwcasm.\"PK_JOB_ID\" = jm.\"PK_JOB_ID\" \n" +
                "JOIN\n" +
                "    agni2803.company_customer cc ON cc.\"PK_CUSTOMER_ID\" IN (1,2)\n" +
                "JOIN \n" +
                "    agni2803.media m ON jm.\"PK_JOB_ID\" = m.\"JOB_ID\" \n" +
                "JOIN  \n" +
                "agni2803.business_type_wise_service_master btwsm ON btwsm.\"ID\" IN (SELECT CAST(jwsem.\"SERVICE_ID\" as INTEGER) FROM agni2803.job_wise_service_mapping jwsem WHERE CAST(jwsem.\"JOB_ID\" AS INTEGER) = jm.\"PK_JOB_ID\")\n" +
                "WHERE \n" +
                "    jm.\"PK_JOB_ID\" IN (\n" +
                "        SELECT \n" +
                "            jwcasm2.\"PK_JOB_ID\"  \n" +
                "        FROM \n" +
                "            agni2803.job_wise_customer_and_serviceentity_mapping jwcasm2  \n" +
                "        WHERE \n" +
                "            jwcasm2.\"PK_CUSTOMER_ID\" IN (1,2)\n" +
                "    )";

        List<Map<String, Object>> maps = jdbcTemplate.queryForList(historyQuery);

        List<Map<String, Object>> collect = maps.stream().collect(Collectors.groupingBy(m -> m.get("PK_CUSTOMER_ID"), Collectors
                .collectingAndThen(Collectors.toList(), list -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("CUSTOMER_FIRST_NAME", list.get(0).get("CUSTOMER_FIRST_NAME"));
                    map.put("CUSTOMER_LAST_NAME", list.get(0).get("CUSTOMER_LAST_NAME"));
                    //jobList
                    map.put("jobList", list.stream()
                            .map(subcategory -> {
                                Map<String, Object> subMap = new HashMap<>();
                                subMap.put("JOB_DATE", subcategory.get("JOB_DATE"));
                                subMap.put("JOB_MATERIAL", subcategory.get("JOB_MATERIAL"));
                                subMap.put("JOB_NOTES", subcategory.get("JOB_NOTES"));
                                subMap.put("JOB_LOCATION", subcategory.get("JOB_LOCATION"));
                                subMap.put("JOB_START_TIME", subcategory.get("JOB_START_TIME"));
                                subMap.put("JOB_END_TIME", subcategory.get("JOB_END_TIME"));
                                return subMap;
                            }).distinct()
                            .collect(Collectors.toList()));
                    //for ImageList
                    map.put("ImageList", list.stream()
                            .map(subcategory -> {
                                Map<String, Object> ImageList = new HashMap<>();
                                ImageList.put("ImageName", subcategory.get("FILE_NAME"));
                                return ImageList;
                            }).distinct()
                            .collect(Collectors.toList()));
                    //forService
                    map.put("ServiceList", list.stream()
                            .map(subcategory -> {
                                Map<String, Object> ImageList = new HashMap<>();
                                ImageList.put("SERVICE_NAME", subcategory.get("SERVICE_NAME"));
                                return ImageList;
                            }).distinct()

                            .collect(Collectors.toList()));
                    return map;
                }))).values().stream().collect(Collectors.toList());


        return collect;

    }

    /*
     * @Author AMIT KUMAR SINGH
     * THIS METHOD USED TO GET THE HISTORY OF CUSTOMERS THAT HAVE PREVIOUSLY TAKEN THE SERVICE
     * */
    public List<Map<String, Object>> getCustomerServiceHistory(String tenantId, Integer customerId) {

        String historyQuery = "SELECT \n" +
                "    cc.\"PK_CUSTOMER_ID\",\n" +
                "    JM.\"PK_JOB_ID\",\n" +
                "    cc.\"CUSTOMER_FIRST_NAME\",\n" +
                "    cc.\"CUSTOMER_LAST_NAME\",\n" +
                "    TO_CHAR(jm.\"JOB_DATE\", 'YYYY-MM-DD') AS \"JOB_DATE\",\n" +
                "    jm.\"JOB_NOTES\",\n" +
                "    m.\"FILE_NAME\",\n" +
                "    btwsm.\"SERVICE_NAME\"\n" +
                "FROM  \n" +
                tenantId +".company_customer cc\n" +
                "JOIN \n" +
                tenantId +".job_wise_customer_and_serviceentity_mapping jwcasm ON cc.\"PK_CUSTOMER_ID\" = jwcasm.\"PK_CUSTOMER_ID\"\n" +
                "JOIN \n" +
                tenantId +".job_master jm ON jwcasm.\"PK_JOB_ID\" = jm.\"PK_JOB_ID\"\n" +
                "LEFT JOIN \n" +
                tenantId +".media m ON jm.\"PK_JOB_ID\" = m.\"JOB_ID\"\n" +
                "JOIN \n" +
                tenantId +".business_type_wise_service_master btwsm ON btwsm.\"ID\" IN (\n" +
                "        SELECT \n" +
                "            btwsm.\"ID\"  \n" +
                "        FROM \n" +
                tenantId +".business_type_wise_service_master btwsm \n" +
                "        WHERE \n" +
                "            btwsm.\"ID\" IN (\n" +
                "                SELECT \n" +
                "                    CAST(value AS INTEGER) AS SERVICE_ID\n" +
                "                FROM \n" +
                tenantId +".job_wise_service_mapping jwsm,\n" +
                "                    UNNEST(STRING_TO_ARRAY(jwsm.\"SERVICE_ID\", ',')) AS value\n" +
                "                WHERE \n" +
                "                    cast(jwsm.\"JOB_ID\" as integer) = jm.\"PK_JOB_ID\"\n" +
                "            )\n" +
                ")\n" +
                "WHERE \n" +
                "    cc.\"PK_CUSTOMER_ID\" = ?  \n" +
                "    AND LENGTH(JM.\"JOB_NOTES\") != 0;";

        List<Map<String, Object>> maps = jdbcTemplate.queryForList(historyQuery,customerId);

        List<Map<String, Object>> collect = new ArrayList<>();
        Map<Object, Map<String, Object>> customerMap = new LinkedHashMap<>();

        for (Map<String, Object> m : maps) {
            Object custId = m.get("PK_CUSTOMER_ID");
            if (!customerMap.containsKey(custId)) {
                customerMap.put(custId, new HashMap<>());
                customerMap.get(custId).put("jobs", new ArrayList<>());
            }

            Object jobId = m.get("PK_JOB_ID");
            boolean jobExists = false;
            for (Map<String, Object> existingJob : (List<Map<String, Object>>) customerMap.get(custId).get("jobs")) {
                if (existingJob.get("jobId").equals(jobId)) {
                    jobExists = true;
                    break;
                }
            }
            if (!jobExists) {
                Map<String, Object> job = new HashMap<>();
                job.put("customerId", m.get("PK_CUSTOMER_ID"));
                job.put("customerName", m.get("CUSTOMER_FIRST_NAME") + " " + m.get("CUSTOMER_LAST_NAME"));
                job.put("jobId", jobId);
                job.put("date", m.get("JOB_DATE"));

                List<Object> services = new ArrayList<>();
                List<Object> images = new ArrayList<>();
                List<Object> notes = new ArrayList<>();
                for (Map<String, Object> map1 : maps) {
                    if (map1.get("PK_JOB_ID").equals(jobId)) {
                        services.add(map1.get("SERVICE_NAME"));
                        if (map1.get("FILE_NAME") != null) {
                            images.add(map1.get("FILE_NAME"));
                        }
                    }
                }
                job.put("services", services.stream().distinct().collect(Collectors.toList()));
                job.put("images", images.stream().distinct().collect(Collectors.toList()));
                job.put("notes", m.get("JOB_NOTES"));
                ((List<Map<String, Object>>) customerMap.get(custId).get("jobs")).add(job);
            }
        }
        collect.addAll(customerMap.values());
        return collect;
    }

    public Map<String, Object> getTimeInterval(String userId, String tenantId, String formDate) {
        String sql = "SELECT \"INTERVAL_TIME\" " +
                "FROM \"Bizfns\".\"COMPANY_TIME_INTERVAL\" " +
                "WHERE \"TENANT_ID\" = ? AND \"USER_ID\" = ? " +
                "AND \"START_DATE\" <= TO_DATE(?, 'YYYY-MM-DD') " +
                "ORDER BY \"START_DATE\" DESC " +
                "LIMIT 1";
        try {
            Map<String, Object> result = jdbcTemplate.queryForMap(sql, tenantId, userId, formDate);
            return result;
        } catch (EmptyResultDataAccessException e) {
            String sqlInsert = "INSERT INTO \"Bizfns\".\"COMPANY_TIME_INTERVAL\" " +
                    "(\"INTERVAL_TIME\", \"USER_ID\", \"TENANT_ID\", \"START_DATE\") " +
                    "VALUES (?, ?, ?, TO_DATE(?, 'YYYY-MM-DD'))";
            String defaultIntervalTime = "01:00";
            int update = jdbcTemplate.update(sqlInsert, defaultIntervalTime, userId, tenantId, formDate);

            if (update == 1) {
                Map<String, Object> defaultMap = new HashMap<>();
                defaultMap.put("INTERVAL_TIME", defaultIntervalTime);
                return defaultMap;
            }
        }
        return null;
    }

    public String saveWorkingHours(String startTime, LocalDate localDate, String endTime, String tenantId, String userId) {
        Date date = java.sql.Date.valueOf(localDate);
        String sqlCheck = "SELECT \"START_TIME\",\n" +
                "                \"END_TIME\"\n" +
                "                FROM \"Bizfns\".\"COMPANY_TIME_INTERVAL\"  \n" +
                "                WHERE \"TENANT_ID\" = ? AND \"USER_ID\" = ? AND \"START_DATE_WORKING_HOUR\" = ?";


        List<Map<String, Object>> stringObjectMap = jdbcTemplate.queryForList(sqlCheck, tenantId, userId, date);
        if (stringObjectMap.size() == 0) {
            String sql = "INSERT INTO \"Bizfns\".\"COMPANY_TIME_INTERVAL\" " +
                    "(\"TENANT_ID\", \"START_DATE_WORKING_HOUR\", \"START_TIME\", \"END_TIME\", \"USER_ID\") " +
                    "VALUES (?, ?, ?, ?, ?)";
            try {
                jdbcTemplate.update(sql, tenantId, date, startTime, endTime, userId);
                return " Data inserted successfully";
            } catch (DataAccessException e) {
                return "Error inserting data: " + e.getMessage();
            }
        } else {
            String EDITSQL = "UPDATE \"Bizfns\".\"COMPANY_TIME_INTERVAL\" " +
                    "SET \"START_TIME\" = ?, " +
                    "    \"END_TIME\" = ? " +
                    "WHERE \"TENANT_ID\" = ? AND \"USER_ID\" = ? AND \"START_DATE_WORKING_HOUR\" = ?";
            jdbcTemplate.update(EDITSQL, startTime, endTime, tenantId, userId,date);
            return " Edit Successfully";
        }
    }


    /*public Map<String, Object> getWorkingHours(String formDate, String userId, String tenantId) {
        String sql = "SELECT DISTINCT" +
                "    \"START_TIME\",\n" +
                "    \"END_TIME\"\n" +
                "FROM \"Bizfns\".\"COMPANY_TIME_INTERVAL\"\n" +
                "WHERE \"TENANT_ID\" = ? AND \"USER_ID\" = ? AND CAST(\"START_DATE_WORKING_HOUR\" AS DATE) = to_date( ? , 'yyyy-mm-dd')";
        try {
            Map<String, Object> stringObjectMap = jdbcTemplate.queryForMap(sql, tenantId, userId, formDate);
            return stringObjectMap;
        } catch (EmptyResultDataAccessException e) {
            String sqlInsert = "INSERT INTO \"Bizfns\".\"COMPANY_TIME_INTERVAL\" " +
                    "(\"START_TIME\", \"END_TIME\", \"USER_ID\", \"TENANT_ID\", \"START_DATE_WORKING_HOUR\") " +
                    "VALUES (?, ?, ?, ?, to_date(?, 'yyyy-mm-dd'))";
            int defaultStartTime = 7;
            int defaultEndTime = 20;
            int update = jdbcTemplate.update(sqlInsert, defaultStartTime, defaultEndTime, userId, tenantId, formDate);
            if (update == 1) {
                Map<String, Object> defaultMap = new HashMap<>();
                defaultMap.put("START_TIME", defaultStartTime);
                defaultMap.put("END_TIME", defaultEndTime);
                return defaultMap;
            }
            return null;
        }
    }*/

    /**
     * AMIT KUMAR SINGH
     * It retrieves the working hours for a specific user within the schedule on a given date.
     * If no working hours are found for the user on the given date,
     * default working hours are inserted and returned.
     * @param formDate The date for which working hours are requested (formatted as "yyyy-MM-dd").
     * @param userId   The ID of the user for whom working hours are requested.
     * @param tenantId The ID of the tenant to which the user belongs.
     */
    public Map<String, Object> getWorkingHours(String formDate, String userId, String tenantId) {
        String sql = "SELECT \"START_TIME\", \"END_TIME\", CAST(\"START_DATE_WORKING_HOUR\" AS DATE) AS \"START_DATE_WORKING_HOUR\" " +
                "FROM \"Bizfns\".\"COMPANY_TIME_INTERVAL\" " +
                "WHERE \"TENANT_ID\" = ? AND \"USER_ID\" = ? AND \"START_DATE_WORKING_HOUR\" IS NOT NULL " +
                "ORDER BY \"START_DATE_WORKING_HOUR\" ASC";

        try {
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, tenantId, userId);
            String pattern = "yyyy-MM-dd";
            SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
            Date parsedDate = dateFormat.parse(formDate);
            Date formDateObj = new Date(parsedDate.getTime());
            if (results.isEmpty()) {
                return insertDefaultWorkingHours(tenantId, userId, formDate);
            } else {
                TreeMap<Date, Map<String, Object>> dateToTimeMap = new TreeMap<>();
                for (Map<String, Object> result : results) {
                    Date startDateWorkingHour = (Date) result.get("START_DATE_WORKING_HOUR");
                    Map<String, Object> timeMap = new HashMap<>();
                    timeMap.put("START_TIME", result.get("START_TIME"));
                    timeMap.put("END_TIME", result.get("END_TIME"));
                    dateToTimeMap.put(startDateWorkingHour, timeMap);
                }
                Map.Entry<Date, Map<String, Object>> relevantEntry = dateToTimeMap.floorEntry(formDateObj);
                if (relevantEntry != null) {
                    return relevantEntry.getValue();
                } else {
                    return insertDefaultWorkingHours(tenantId, userId, formDate);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            throw new RuntimeException("Error parsing formDate: " + formDate, e);
        } catch (DataAccessException e) {
            e.printStackTrace();
            throw new RuntimeException("Database error occurred", e);
        }
    }

    private Map<String, Object> insertDefaultWorkingHours(String tenantId, String userId, String formDate) {
        String sqlInsert = "INSERT INTO \"Bizfns\".\"COMPANY_TIME_INTERVAL\" " +
                "(\"START_TIME\", \"END_TIME\", \"USER_ID\", \"TENANT_ID\", \"START_DATE_WORKING_HOUR\") " +
                "VALUES (?, ?, ?, ?, to_date(?, 'yyyy-mm-dd'))";
        String defaultStartTime = "07:00:00";
        String defaultEndTime = "20:00:00";
        int update = jdbcTemplate.update(sqlInsert, defaultStartTime, defaultEndTime, userId, tenantId, formDate);
        if (update == 1) {
            Map<String, Object> defaultMap = new HashMap<>();
            defaultMap.put("START_TIME", defaultStartTime);
            defaultMap.put("END_TIME", defaultEndTime);
            return defaultMap;
        }
        return null;
    }

    public String saveMaxJobTask(String maxJobTask) {
        String sql = "UPDATE \"Bizfns\".\"COMPANY_TIME_INTERVAL\"\n" +
                "SET \"MAX_JOB_TASK\" = ?";
        int update = jdbcTemplate.update(sql, maxJobTask);
        return "success";
    }

    public String getMaxJobTask() {
        String sql = "SELECT \"MAX_JOB_TASK\" FROM \"Bizfns\".\"COMPANY_TIME_INTERVAL\"";
        try {
            List<Map<String, Object>> maps = jdbcTemplate.queryForList(sql);
            if (!maps.isEmpty() && maps.get(0).get("MAX_JOB_TASK") != null) {
                Map<String, Object> stringObjectMap = maps.get(0);
                String maxJobTask = (String) stringObjectMap.get("MAX_JOB_TASK");
                return maxJobTask;
            } else {
                return "5";
            }
        } catch (DataAccessException dae) {
            System.err.println("DataAccessException occurred: " + dae.getMessage());
        } catch (Exception e) {
            System.err.println("Exception occurred: " + e.getMessage());
        }
        return "5";
    }



    public boolean existJobId(int jobId, String tenantId) {
        String sql = "SELECT COUNT(*) FROM \"" + tenantId + "\".\"job_master\" WHERE \"PK_JOB_ID\" = ?";
        try {
            int count = jdbcTemplate.queryForObject(sql, Integer.class, jobId);
            return count > 0;
        } catch (EmptyResultDataAccessException e) {
            // Handle the case when no rows are returned by the query
            return false;

        }
    }

    public boolean pastJobDataChk(int jobId, String tenantId) {
        String sql = "SELECT \"JOB_DATE\" FROM \"" + tenantId + "\".\"job_master\" WHERE \"PK_JOB_ID\" = ?";
        try {
            LocalDate jobDate = jdbcTemplate.queryForObject(sql, LocalDate.class, jobId);
            LocalDate currentDate = LocalDate.now();
            if (jobDate != null && jobDate.isBefore(currentDate)) {
                return true;
            } else {
                return false;
            }
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    public String existUserId(String tenantId){

        String sql = "SELECT \"COMPANY_BACKUP_PHONE_NUMBER\" FROM \"Bizfns\".\"COMPANY_MASTER\" " +
                " WHERE \"SCHEMA_ID\" = ? ";

        // Execute SQL query
        String phoneNumber = jdbcTemplate.queryForObject(
                sql, new Object[]{tenantId}, String.class);

        return phoneNumber;
    }

    public List<String> priviledgeChkForSchedule(String tenantId) {
        try {
            String sql = "SELECT p.\"PREVILEDGE\" FROM \"Bizfns\".\"PRIVILEGE\" p, \"Bizfns\".\"PRIVILEGE_DTLS\" pd " +
                    "WHERE p.\"PK_PREVILEDGE_ID\" = pd.\"FK_PREVILEDGE_ID\" AND " +
                    "p.\"PREVILEDGE_TYPE\" = 'SCHEDULE' AND pd.\"TENANT_ID\" = ?";
            List<String> privileges = jdbcTemplate.queryForList(sql, new Object[]{tenantId}, String.class);
            return privileges;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<String> priviledgeChkForMaterial(String tenantId) {
        try {
            String sql = "SELECT p.\"PREVILEDGE\" FROM \"Bizfns\".\"PRIVILEGE\" p, \"Bizfns\".\"PRIVILEGE_DTLS\" pd " +
                    "WHERE p.\"PK_PREVILEDGE_ID\" = pd.\"FK_PREVILEDGE_ID\" AND " +
                    "p.\"PREVILEDGE_TYPE\" = 'MATERIAL' AND pd.\"TENANT_ID\" = ?";
            List<String> privileges = jdbcTemplate.queryForList(sql, new Object[]{tenantId}, String.class);
            return privileges;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<String> priviledgeChkForScheduleAsPerPhNo(String tenantId, String staffPhoneNumber) {
        try {
            String sql = "SELECT p.\"PREVILEDGE\" FROM \"Bizfns\".\"PRIVILEGE\" p, \"Bizfns\".\"PRIVILEGE_DTLS\" pd " +
                    "WHERE p.\"PK_PREVILEDGE_ID\" = pd.\"FK_PREVILEDGE_ID\" AND " +
                    "p.\"PREVILEDGE_TYPE\" = 'SCHEDULE' AND pd.\"TENANT_ID\" = ? AND pd.\"STAFF_PHONE_NO\" = ?";
            List<String> privileges = jdbcTemplate.queryForList(sql, new Object[]{tenantId, staffPhoneNumber}, String.class);
            return privileges;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<String> phoneNoPriviledgeDataChk(String tenantId, String staffPhoneNumber) {
        try {
            String sql = "SELECT p.\"PREVILEDGE\" FROM \"Bizfns\".\"PRIVILEGE\" p, \"Bizfns\".\"PRIVILEGE_DTLS\" pd " +
                    "WHERE p.\"PK_PREVILEDGE_ID\" = pd.\"FK_PREVILEDGE_ID\" AND " +
                    "p.\"PREVILEDGE_TYPE\" = 'SCHEDULE' AND pd.\"TENANT_ID\" = ? AND pd.\"STAFF_PHONE_NO\" = ?";
            List<String> privileges = jdbcTemplate.queryForList(sql, new Object[]{tenantId, staffPhoneNumber}, String.class);
            return privileges;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public void deleteServiceObjectFromDB(String tenantId, Integer serviceEntityId) {
        String strDeleteAssignJobData = " delete from  \"" + tenantId + "\".\"customer_wise_service_entity\" cwse  where cwse.\"FK_SERVICE_ENTITY\" = " + serviceEntityId + "";
        jdbcTemplate.update(strDeleteAssignJobData);
    }

    public Integer addMaterialCategoryInDB(String tenantId, String categoryName) {


        try {
            String strInsertMaterialData = "INSERT INTO \"" + tenantId + "\".\"material_category_master\" \n" +
                    "(\"PK_CATEGORY_ID\", \"CATEGORY_NAME\", \"PARENT_CATEGORY_ID\", \"CREATED_AT\", \"UPDATED_AT\") \n" +
                    "VALUES (\n" +
                    "    (SELECT COALESCE(MAX(\"PK_CATEGORY_ID\"), 0) + 1 FROM \"" + tenantId + "\".\"material_category_master\"), \n" +
                    "    ?, \n" +
                    "    (SELECT COALESCE(MAX(\"PARENT_CATEGORY_ID\"), 0) + 1 FROM \"" + tenantId + "\".\"material_category_master\"), \n" +
                    "    current_timestamp, \n" +
                    "    current_timestamp\n" +
                    ") \n" +
                    "RETURNING \"PK_CATEGORY_ID\"";
            Object[] params = new Object[] {
                    categoryName != null && !categoryName.isEmpty() ? categoryName : null
            };
            System.out.println(strInsertMaterialData);
            Integer materialId = jdbcTemplate.queryForObject(strInsertMaterialData, params, Integer.class);
            return materialId;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public Integer addMaterialSubCategoryInDB(String tenantId, Integer categoryId, String subcategoryName) {
        try {
            String strInsertSubCategoryData = "INSERT INTO \"" + tenantId + "\".\"material_subcategory_master\" \n" +
                    "(\"pk_category_id\", \"pk_subcategory_id\", \"pk_subcategory_name\", \"created_at\", \"updated_at\") \n" +
                    "VALUES (\n" +
                    "    ?, \n" +
                    "    (SELECT COALESCE(MAX(\"pk_subcategory_id\"), 0) + 1 FROM \"" + tenantId + "\".\"material_subcategory_master\"), \n" +
                    "    ?, \n" +
                    "    current_timestamp, \n" +
                    "    current_timestamp\n" +
                    ") \n" +
                    "RETURNING \"pk_subcategory_id\"";
            Object[] params = new Object[] {
                    categoryId,
                    subcategoryName != null && !subcategoryName.isEmpty() ? subcategoryName : null
            };
            Integer subcategoryId = jdbcTemplate.queryForObject(strInsertSubCategoryData, params, Integer.class);
            return subcategoryId;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public int deleteSubcategoryFromDB(String tenantId, Integer categoryId, Integer subcategoryId) {
        try {
            if (subcategoryId == null) {
                String deleteCategoryData = "DELETE FROM \"" + tenantId + "\".\"material_category_master\" " +
                        "WHERE \"PK_CATEGORY_ID\" = ?";
                String deleteSubcategoryData = "DELETE FROM \"" + tenantId + "\".\"material_subcategory_master\" " +
                        "WHERE \"pk_category_id\" = ?";
                int deletedSubcategories = jdbcTemplate.update(deleteSubcategoryData, categoryId);
                int deletedCategories = jdbcTemplate.update(deleteCategoryData, categoryId);
                return deletedSubcategories + deletedCategories;
            } else {
                String deleteSubcategoryData = "DELETE FROM \"" + tenantId + "\".\"material_subcategory_master\" " + "WHERE \"pk_category_id\" = ? AND \"pk_subcategory_id\" = ?";
                return jdbcTemplate.update(deleteSubcategoryData, categoryId, subcategoryId);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
    }


    public void updateServiceEntityQuestionsList(String tenantId, Integer customerId, Integer questionId, Integer serviceEntityId, String answer, Integer answerTypeId) {
        String checkSql = "SELECT COUNT(*) FROM \"" + tenantId + "\".\"customer_wise_service_entity\" " +
                "WHERE \"FK_CUSTOMER_ID\" = ? AND \"FK_QUESTION_ID\" = ? AND \"FK_SERVICE_ENTITY\" = ?";
        Object[] checkParams = {
                customerId,
                questionId != null ? questionId : 0,
                serviceEntityId
        };
        Integer count = jdbcTemplate.queryForObject(checkSql, checkParams, Integer.class);
        if (count == null || count == 0) {
            throw new RecordNotFoundException("Update is made to the record that does not exist in database");
        }
        String updateSql = "UPDATE \"" + tenantId + "\".\"customer_wise_service_entity\" " +
                "SET \"ANSWER\" = ? " +
                "WHERE \"FK_CUSTOMER_ID\" = ? AND \"FK_QUESTION_ID\" = ? AND \"FK_SERVICE_ENTITY\" = ?";
        Object[] updateParams = {
                answer != null ? answer : "",
                customerId,
                questionId != null ? questionId : 0,
                serviceEntityId
        };
        jdbcTemplate.update(updateSql, updateParams);
    }

    public Map<String, Object> getInvoiceData(String tenantId, Integer customerId, String jobId) {
        try {
            String fetchInvoiceSql = "SELECT * FROM \"" + tenantId + "\".\"invoice\" WHERE \"CUSTOMER_ID\" = ? AND \"JOB_ID\" = ?";
            Map<String, Object> invoice = jdbcTemplate.queryForMap(fetchInvoiceSql, customerId, Integer.parseInt(jobId));
            return invoice;
        } catch (Exception e) {
            return null;
        }
    }

    public List<Map<String, Object>> getScheduledJobs(String schema) {
        String sql = "SELECT * FROM \"" + schema + "\".\"job_master\"";
        List<Map<String, Object>> scheduledJobs = jdbcTemplate.queryForList(sql);
        //System.out.println(schema);
        return scheduledJobs;
    }

    public boolean materialUnitExists(String tenantId, Integer unitId) {
        String sql = "SELECT COUNT(*) FROM \"" + tenantId + "\".\"material_rate_master\" WHERE \"PK_MATERIAL_UNIT_ID\" = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{unitId}, Integer.class);
        return count != null && count > 0;
    }

    public void saveMaterialUnit(String tenantId,String unitName) {
        String fetchUnitIdSql = "SELECT COALESCE(MAX(\"PK_MATERIAL_UNIT_ID\"), 0) + 1 FROM \"" + tenantId + "\".\"material_rate_master\"";
        Integer nextUnitId = jdbcTemplate.queryForObject(fetchUnitIdSql, Integer.class);
        String fetchMaxIdSql = "SELECT COALESCE(MAX(\"FK_MATERIAL_CATEGORY_ID\"), 0) + 1 FROM \"" + tenantId + "\".\"material_rate_master\"";
        Integer nextCategoryId = jdbcTemplate.queryForObject(fetchMaxIdSql, Integer.class);
        String insertSql = "INSERT INTO \"" + tenantId + "\".\"material_rate_master\" (\"PK_MATERIAL_UNIT_ID\", \"MATERIAL_UNIT_NAME\", \"FK_MATERIAL_CATEGORY_ID\") VALUES (?, ?, ?)";
        jdbcTemplate.update(insertSql,nextUnitId,unitName,nextCategoryId);
    }

    public void updateMaterialUnit(String tenantId, Integer unitId, String unitName) {
        String sql = "UPDATE \"" + tenantId + "\".\"material_rate_master\" SET \"MATERIAL_UNIT_NAME\" = ? WHERE \"PK_MATERIAL_UNIT_ID\" = ?";
        jdbcTemplate.update(sql, unitName, unitId);
    }

    public Map<String, String> getTimeIntervalFromDb(Principal principal) {
        String userInfo = principal.getName();
        String[] parts = userInfo.split(",");
        String mobileNumber = parts[0];
        String tenentId = parts[1];
        String sqlCheck = "SELECT \"INTERVAL_TIME\",\"START_DATE\" " +
                "FROM \"Bizfns\".\"COMPANY_TIME_INTERVAL\" " +
                "WHERE \"TENANT_ID\" = ? AND \"USER_ID\" = ? AND \"START_DATE\" IS NOT NULL AND \"CREATED_TIME\" IS NOT NULL " +
                "ORDER BY \"CREATED_TIME\" DESC LIMIT 1";
        List<Map<String, Object>> intervalTime = jdbcTemplate.queryForList(sqlCheck, tenentId, mobileNumber);
        Map<String, String> intervalTimeResponse = new HashMap<>();
        if (intervalTime.size() > 0 && intervalTime.get(0).get("INTERVAL_TIME") != null) {
            Map<String, Object> latestIntervalTime = intervalTime.get(0);
            intervalTimeResponse.put("fromDate", String.valueOf(latestIntervalTime.get("START_DATE")));
            intervalTimeResponse.put("interval", String.valueOf(latestIntervalTime.get("INTERVAL_TIME")));
        } else {
            intervalTimeResponse.put("interval", "01:00");

        }
        return intervalTimeResponse;
    }
}