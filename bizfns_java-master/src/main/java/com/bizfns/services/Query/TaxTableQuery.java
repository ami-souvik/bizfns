package com.bizfns.services.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class TaxTableQuery {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Integer insertTaxMasterData(String tenantId, String taxMasterName, Double taxMasterRate, int userId) {


        try {
            String strInsertTaxMasterData = "INSERT INTO \"" + tenantId + "\".\"tax_master\" (" +
                    "\"PK_TAX_MASTER_ID\", \"TAX_MASTER_NAME\", \"TAX_MASTER_RATE\",  " +
                    "\"FK_COMPANY_MASTER_ID\", \"TAX_MASTER_STATUS\", \"TAX_MASTER_CREATED_AT\", \"TAX_MASTER_UPDATED_AT\") " +
                    "VALUES (" +
                    "(SELECT COALESCE((SELECT MAX(\"PK_TAX_MASTER_ID\") FROM \"" + tenantId + "\".\"tax_master\"), 0) + 1), " +
                    "?, ?, ?, 'ACTIVE', current_timestamp, current_timestamp) " +
                    "RETURNING \"PK_TAX_MASTER_ID\"";

            Object[] params = new Object[]{
                    taxMasterName,
                    taxMasterRate,
                    userId
            };

            System.out.println("SQL Query: " + strInsertTaxMasterData);

            // Execute the query and retrieve the generated PK_TAX_MASTER_ID
            Integer taxMasterId = jdbcTemplate.queryForObject(strInsertTaxMasterData, params, Integer.class);

            return taxMasterId;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }


    public List<Map<String, Object>> fetchTaxTableData(String tenantId) {
        try {
            String sql = "SELECT \"PK_TAX_MASTER_ID\" as TaxTypeId, " +
                    "\"TAX_MASTER_NAME\" as TaxTypeName, " +
                    "\"TAX_MASTER_RATE\" as TaxRate " +
                    "FROM \"" + tenantId + "\".\"tax_master\" " +
                    "WHERE \"TAX_MASTER_STATUS\" = 'ACTIVE'";

            System.out.println("SQL Query: " + sql);

            // Execute query and fetch results
            List<Map<String, Object>> taxTableData = jdbcTemplate.queryForList(sql);

            return taxTableData;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

    }


    public boolean updateTaxMasterData(String tenantId, List<Map<String, Object>> taxUpdates) {
        try {
            for (Map<String, Object> taxUpdate : taxUpdates) {
                int taxTypeId = Integer.valueOf(taxUpdate.get("TaxTypeId").toString());
                Double taxRate = Double.valueOf(taxUpdate.get("TaxRate").toString());

                String strUpdateTaxMasterData = "UPDATE \"" + tenantId + "\".\"tax_master\" SET " +
                        "\"TAX_MASTER_RATE\" = ?, " +
                        "\"TAX_MASTER_UPDATED_AT\" = current_timestamp " +
                        "WHERE \"PK_TAX_MASTER_ID\" = ?";

                Object[] params = new Object[]{
                        taxRate,
                        taxTypeId
                };

                jdbcTemplate.update(strUpdateTaxMasterData, params);
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean deleteTaxMasterData(String tenantId, String taxTypeId) {

        int taxTypeIdInt = Integer.parseInt(taxTypeId);
        try {
            String strDeleteTaxMasterData = "DELETE FROM \"" + tenantId + "\".\"tax_master\" " +
                    "WHERE \"PK_TAX_MASTER_ID\" = ?";

            Object[] params = new Object[]{
                    taxTypeIdInt
            };

            jdbcTemplate.update(strDeleteTaxMasterData, params);

            return true;


        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

    }
}