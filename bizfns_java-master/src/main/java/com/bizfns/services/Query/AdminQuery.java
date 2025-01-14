package com.bizfns.services.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class AdminQuery {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> getAllRegisteredCompanyName(String tenantId) {
        String strQueryCustomerList = "SELECT *" +
                " FROM \"" + tenantId + "\".\"COMPANY_MASTER\"";
        List<Map<String, Object>> All_CompanyRegisteredData = jdbcTemplate.queryForList(strQueryCustomerList);

        return All_CompanyRegisteredData;
    }



    public String findPriviledgeInfo(String privilegeId) {
        String sql = "SELECT \"PREVILEDGE_TYPE\",\"PREVILEDGE\" FROM \"Bizfns\".\"PRIVILEGE\"" +
                " WHERE \"PK_PREVILEDGE_ID\" = ?";
        try {
            Map<String, Object> privilegeInfo = jdbcTemplate.queryForMap(sql, Integer.parseInt(privilegeId));
            String privilegeType = (String) privilegeInfo.get("PREVILEDGE_TYPE");
            String privilege = (String) privilegeInfo.get("PREVILEDGE");
            return privilegeType + "," + privilege;
        } catch (DataAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Map<String, Object>> assignUserPrivilege(String userType, String privilegeId, String privilegeType, String tenantId,String phoneNumber) {
        String insertSql = "INSERT INTO \"Bizfns\".\"PRIVILEGE_DTLS\"" +
                " (\"FK_PREVILEDGE_ID\", \"PRIVILEDGE\",\"USER_TYPE\", \"TENANT_ID\",\"PREVILEDGE_VALUE\") " +
                " VALUES (?, ?, ?,?,true)" +
                " RETURNING \"FK_PREVILEDGE_ID\", \"PRIVILEDGE\",\"USER_TYPE\", \"TENANT_ID\"";

        String insertSql1 = "INSERT INTO \"Bizfns\".\"PRIVILEGE_DTLS\"" +
                " (\"FK_PREVILEDGE_ID\", \"PRIVILEDGE\",\"USER_TYPE\", \"TENANT_ID\",\"STAFF_PHONE_NO\",\"PREVILEDGE_VALUE\")" +
                " VALUES (?, ?, ?,?,?,true)" +
                " RETURNING \"FK_PREVILEDGE_ID\", \"PRIVILEDGE\",\"USER_TYPE\", \"TENANT_ID\", \"STAFF_PHONE_NO\"";
        List<Map<String, Object>> allPrivileges = new ArrayList<>();
        try {
            try {
                if(phoneNumber.isEmpty()){
                    Map<String, Object> insertedValues = jdbcTemplate.queryForMap(insertSql, Integer.parseInt(privilegeId), privilegeType, userType, tenantId);
                    allPrivileges.add(insertedValues);
                }else{
                    Map<String, Object> insertedValues = jdbcTemplate.queryForMap(insertSql1, Integer.parseInt(privilegeId), privilegeType, userType, tenantId,phoneNumber);
                    allPrivileges.add(insertedValues);
                }
                return allPrivileges;
            } catch (DataAccessException ex) {
                ex.printStackTrace();
                return null;
            }
        } catch (DataAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void deleteUserPrivileges(String tenantId) {
        String deleteSql = "DELETE FROM \"Bizfns\".\"PRIVILEGE_DTLS\" WHERE \"TENANT_ID\" = ?";
        try {
            jdbcTemplate.update(deleteSql, tenantId);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
    }
    public void deleteUserPrivilegesAsPerPhoneNumber(String tenantId, String phoneNumber) {
        String deleteSql = "DELETE FROM \"Bizfns\".\"PRIVILEGE_DTLS\" WHERE \"TENANT_ID\" = ? AND \"STAFF_PHONE_NO\" = ?";
        try {
            jdbcTemplate.update(deleteSql, tenantId ,phoneNumber);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
    }

    public List<Map<String, Object>> getAllAssignedPrivileges(String tenantId, String phoneNumber) {
        String sqlPrivileges = "SELECT p.\"PK_PREVILEDGE_ID\", p.\"PREVILEDGE_TYPE\", p.\"PREVILEDGE\" " +
                "FROM \"Bizfns\".\"PRIVILEGE\" p";
        String sqlAssignedPrivilegesPhoneNumber = "SELECT pd.\"FK_PREVILEDGE_ID\" " +
                "FROM \"Bizfns\".\"PRIVILEGE_DTLS\" pd " +
                "WHERE pd.\"TENANT_ID\" = ? AND pd.\"STAFF_PHONE_NO\" = ?";
        String sqlAssignedPrivilegesTenant = "SELECT pd.\"FK_PREVILEDGE_ID\" " +
                "FROM \"Bizfns\".\"PRIVILEGE_DTLS\" pd " +
                "WHERE pd.\"TENANT_ID\" = ? AND pd.\"STAFF_PHONE_NO\" IS NULL";
        try {
            List<Map<String, Object>> allPrivileges = jdbcTemplate.queryForList(sqlPrivileges);
            List<Map<String, Object>> assignedPrivileges;
            if (phoneNumber.isEmpty()) {
                assignedPrivileges = jdbcTemplate.queryForList(sqlAssignedPrivilegesTenant, tenantId);
            } else {
                assignedPrivileges = jdbcTemplate.queryForList(sqlAssignedPrivilegesPhoneNumber, tenantId, phoneNumber);
            }
            Set<Integer> assignedPrivilegeIds = new HashSet<>();
            for (Map<String, Object> row : assignedPrivileges) {
                Integer privilegeId = (Integer) row.get("FK_PREVILEDGE_ID");
                assignedPrivilegeIds.add(privilegeId);
            }
            Map<String, List<Map<String, Object>>> groupedPrivilegesType1 = new HashMap<>();
            Map<String, List<Map<String, Object>>> groupedPrivilegesType2 = new HashMap<>();
            for (Map<String, Object> row : allPrivileges) {
                Integer privilegeId = (Integer) row.get("PK_PREVILEDGE_ID");
                String privilegeType = (String) row.get("PREVILEDGE_TYPE");
                String privilegeLabel = (String) row.get("PREVILEDGE");
                Boolean isAssigned = assignedPrivilegeIds.contains(privilegeId);
                Map<String, Object> privilege = new HashMap<>();
                privilege.put("id", privilegeId);
                privilege.put("type", privilegeLabel);
                privilege.put("value", isAssigned);
                if (privilegeType.equalsIgnoreCase("Customer") ||
                        privilegeType.equalsIgnoreCase("Material") ||
                        privilegeType.equalsIgnoreCase("Staff") ||
                        privilegeType.equalsIgnoreCase("Service")) {
                    groupedPrivilegesType2
                            .computeIfAbsent(privilegeType, k -> new ArrayList<>())
                            .add(privilege);
                } else {
                    groupedPrivilegesType1
                            .computeIfAbsent(privilegeType, k -> new ArrayList<>())
                            .add(privilege);
                }
            }

            // Prepare result
            List<Map<String, Object>> groupedResult = new ArrayList<>();
            if (!groupedPrivilegesType1.isEmpty()) {
                Map<String, Object> type1Entry = new HashMap<>();
                type1Entry.put("type", 1);
                List<Map<String, Object>> type1List = new ArrayList<>();
                for (String title : groupedPrivilegesType1.keySet()) {
                    Map<String, Object> listEntry = new HashMap<>();
                    listEntry.put("title", title);
                    listEntry.put("privilege", groupedPrivilegesType1.get(title));
                    type1List.add(listEntry);
                }
                type1Entry.put("list", type1List);
                groupedResult.add(type1Entry);
            }
            if (!groupedPrivilegesType2.isEmpty()) {
                Map<String, Object> type2Entry = new HashMap<>();
                type2Entry.put("type", 2);
                List<Map<String, Object>> type2List = new ArrayList<>();
                for (String title : groupedPrivilegesType2.keySet()) {
                    Map<String, Object> listEntry = new HashMap<>();
                    listEntry.put("title", title);
                    listEntry.put("privilege", groupedPrivilegesType2.get(title));
                    type2List.add(listEntry);
                }
                type2Entry.put("list", type2List);
                groupedResult.add(type2Entry);
            }

            return groupedResult;
        } catch (DataAccessException ex) {
            System.err.println("Database access error: " + ex.getMessage());
            return Collections.emptyList();
        } catch (Exception ex) {
            System.err.println("Unexpected error: " + ex.getMessage());
            return Collections.emptyList();
        }
    }


}
