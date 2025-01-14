package com.bizfns.services.Serviceimpl;

import com.bizfns.services.Service.ClientSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ClientSearchServiceImpl implements ClientSearchService {

    @Autowired
    private JdbcTemplate jdbcTemplate;



    public Map<String, Object> fetchAllClients() {
        String countSql = "SELECT COUNT(*) " +
                "FROM \"Bizfns\".\"COMPANY_MASTER\" CM " +
                "JOIN \"Bizfns\".\"COMPANY_BUSINESS_TYPE_MAPPING\" CBTM ON CM.\"COMPANY_ID\" = CBTM.\"FK_COMPANY_ID\" " +
                "JOIN \"Bizfns\".\"BUSINESS_TYPE_MASTER\" BTM ON BTM.\"PK_BUSINESS_TYPE_ID\" = CBTM.\"FK_BUSINESS_TYPE_ID\" " +
                "LEFT JOIN \"Bizfns\".\"ADDRESS_MASTER\" AM ON AM.\"FK_COMPANY_ID\" = CM.\"COMPANY_ID\"";

      int   totalCount = jdbcTemplate.queryForObject(countSql, Integer.class);

        String dataSql = "SELECT CM.\"SCHEMA_ID\", CM.\"COMPANY_STATUS\", CM.\"BUSINESS_NAME\", CM.\"COMPANY_BACKUP_PHONE_NUMBER\", CM.\"COMPANY_BACKUP_EMAIL\", CM.\"PASSWORD\", TO_CHAR( CM.\"COMPANY_CREATED_AT\", 'YYYY-MM-DD HH24:MI:SS') AS \"COMPANY_CREATED_AT\", BTM.\"BUSINESS_TYPE_ENTITY\", AM.\"ADDRESS\", " +
                " TO_CHAR( CS.\"COMPANY_SUBSCRIPTION_START_DATE\", 'YYYY-MM-DD HH24:MI:SS') AS \"COMPANY_SUBSCRIPTION_START_DATE\", TO_CHAR( CS.\"COMPANY_SUBSCRIPTION_END_DATE\", 'YYYY-MM-DD HH24:MI:SS') AS \"COMPANY_SUBSCRIPTION_END_DATE\", SCP.\"SUBSCRIPTION_ENTITY\" " +
                "   FROM \"Bizfns\".\"COMPANY_MASTER\" CM " +
                " JOIN \"Bizfns\".\"COMPANY_BUSINESS_TYPE_MAPPING\" CBTM ON CM.\"COMPANY_ID\" = CBTM.\"FK_COMPANY_ID\" " +
                " JOIN \"Bizfns\".\"BUSINESS_TYPE_MASTER\" BTM ON BTM.\"PK_BUSINESS_TYPE_ID\" = CBTM.\"FK_BUSINESS_TYPE_ID\" " +
                " LEFT JOIN \"Bizfns\".\"ADDRESS_MASTER\" AM ON AM.\"FK_COMPANY_ID\" = CM.\"COMPANY_ID\" " +
                " JOIN \"Bizfns\".\"COMPANY_SUBSCRIPTION\" CS ON CBTM.\"PK_COMPANY_BUSINESS_MAPPING_ID\" = CS.\"FK_COMPANY_BUSINESS_MAPPING_ID\" " +
                " JOIN \"Bizfns\".\"SUBSCRIPTION_PLAN_MASTER\" SCP ON CS.\"FK_SUBSCRIPTION_PLAN_ID\" = SCP.\"PK_SUBSCRIPTION_PLAN_ID\" "+
                " ORDER BY CM.\"COMPANY_CREATED_AT\" DESC ";

        List<Map<String, Object>> data = jdbcTemplate.queryForList(dataSql);

        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", totalCount);
        result.put("data", data);
        return result;
    }







}
