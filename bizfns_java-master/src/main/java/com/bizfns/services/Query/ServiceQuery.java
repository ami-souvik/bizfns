package com.bizfns.services.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.List;
import java.util.Map;

@Component
public class ServiceQuery {

    @Autowired
    private JdbcTemplate jdbcTemplate;


    public List<Map<String, Object>> recordForServiceList(String tenantId, String userId){

        String strQueryServiceList = "select \"PK_FORM_KEY_ID\",\n" +
                "\"INPUT_KEY\",\n" +
                "\"ANSWER_TYPE\",\n" +
                "\"OPTIONS\" from  \"" + tenantId + "\".\"business_type_form_entities\"  where \"FK_BUSINESS_TYPE_ID\" =(select \"FK_BUSINESS_TYPE_ID\" from \"Bizfns\".\"COMPANY_BUSINESS_TYPE_MAPPING\" where \"FK_COMPANY_ID\"= (select \"COMPANY_ID\" from  \"Bizfns\".\"COMPANY_MASTER\" where \"COMPANY_BACKUP_EMAIL\" = '"+userId+"' or\n" +
                "\"COMPANY_BACKUP_PHONE_NUMBER\" = '"+userId+"' ))";
        List<Map<String, Object>> dataForServiceList= jdbcTemplate.queryForList(strQueryServiceList);
 //System.out.println(strQueryServiceList);
        return dataForServiceList;
    }


    public List<Map<String, Object>> recordForServiceRateList(String tenantId, String userId){

        String strQueryServiceRateList = "select \"ID\",\n" +
                "\"SERVICE_NAME\",\n" +
                "\"RATE\",\n" +
                "\"STATUS\" from    \"" + tenantId + "\".\"business_type_wise_service_master\"  where \"BUSINESS_TYPE_ID\" = (select \"FK_BUSINESS_TYPE_ID\" from \"Bizfns\".\"COMPANY_BUSINESS_TYPE_MAPPING\" where \"FK_COMPANY_ID\"= (select \"COMPANY_ID\" from  \"Bizfns\".\"COMPANY_MASTER\" where (\"COMPANY_BACKUP_EMAIL\" =  '"+userId+"' or\n" +
                "\"COMPANY_BACKUP_PHONE_NUMBER\" = '"+userId+"') AND \"SCHEMA_ID\"='"+tenantId+"'))" +
                "ORDER BY \"ID\" DESC";
       // System.out.println(strQueryServiceRateList);
        List<Map<String, Object>> dataForServiceRateList= jdbcTemplate.queryForList(strQueryServiceRateList);
        //System.out.println(strQueryServiceRateList);
        return dataForServiceRateList;
    }


    public Map<String, Object> getServiceDetails(String tenantId, String userId, String serviceId) {
        String strQueryServiceDetails = "SELECT \"ID\", \"SERVICE_NAME\", \"RATE\", \"STATUS\", \"RATE_UNIT\" " +
                "FROM \"" + tenantId + "\".\"business_type_wise_service_master\" " +
                "WHERE \"ID\" = ? " +
                "AND \"BUSINESS_TYPE_ID\" = (SELECT \"FK_BUSINESS_TYPE_ID\" " +
                "FROM \"Bizfns\".\"COMPANY_BUSINESS_TYPE_MAPPING\" " +
                "WHERE \"FK_COMPANY_ID\" = (SELECT \"COMPANY_ID\" " +
                "FROM \"Bizfns\".\"COMPANY_MASTER\" " +
                "WHERE (\"COMPANY_BACKUP_EMAIL\" = ? OR \"COMPANY_BACKUP_PHONE_NUMBER\" = ?) " +
                "AND \"SCHEMA_ID\" = ?))";
        return jdbcTemplate.queryForMap(strQueryServiceDetails, Integer.parseInt(serviceId), userId, userId, tenantId);
    }

    public boolean updateServiceDetails(String tenantId, String userId, String serviceId, String serviceName, String rate, String rateUnit, String status) {
        String strUpdateServiceDetails = "UPDATE \"" + tenantId + "\".\"business_type_wise_service_master\" " +
                "SET \"SERVICE_NAME\" = ?, \"RATE\" = ?, \"RATE_UNIT\" = ?, \"STATUS\" = ? " +
                "WHERE \"ID\" = ? " +
                "AND \"BUSINESS_TYPE_ID\" = (SELECT \"FK_BUSINESS_TYPE_ID\" " +
                "FROM \"Bizfns\".\"COMPANY_BUSINESS_TYPE_MAPPING\" " +
                "WHERE \"FK_COMPANY_ID\" = (SELECT \"COMPANY_ID\" " +
                "FROM \"Bizfns\".\"COMPANY_MASTER\" " +
                "WHERE (\"COMPANY_BACKUP_EMAIL\" = ? OR \"COMPANY_BACKUP_PHONE_NUMBER\" = ?) " +
                "AND \"SCHEMA_ID\" = ?))";

        int rowsAffected = jdbcTemplate.update(strUpdateServiceDetails, serviceName, Integer.parseInt(rate), Integer.parseInt(rateUnit), status, Integer.parseInt(serviceId), userId, userId, tenantId);
        return rowsAffected > 0;
    }

    public int deleteService(String tenantId, String userId, String serviceId) {
        String strDeleteService = "DELETE FROM \"" + tenantId + "\".\"business_type_wise_service_master\" " +
                "WHERE \"ID\" = ? " +
                "AND \"BUSINESS_TYPE_ID\" = (SELECT \"FK_BUSINESS_TYPE_ID\" " +
                "FROM \"Bizfns\".\"COMPANY_BUSINESS_TYPE_MAPPING\" " +
                "WHERE \"FK_COMPANY_ID\" = (SELECT \"COMPANY_ID\" " +
                "FROM \"Bizfns\".\"COMPANY_MASTER\" " +
                "WHERE (\"COMPANY_BACKUP_EMAIL\" = ? OR \"COMPANY_BACKUP_PHONE_NUMBER\" = ?) " +
                "AND \"SCHEMA_ID\" = ?))";
        return jdbcTemplate.update(strDeleteService, Integer.parseInt(serviceId), userId, userId, tenantId);
    }
}
