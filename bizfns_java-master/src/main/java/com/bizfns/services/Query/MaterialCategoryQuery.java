package com.bizfns.services.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class MaterialCategoryQuery {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Integer insertMaterialCategoryData(String tenantId, String categoryName, int parentCategoryIdInt, String companyMasterId) {
        try {
            String strInsertMaterialCategoryData = "INSERT INTO \"" + tenantId + "\".\"material_category_master\" (" +
                    "\"PK_CATEGORY_ID\", \"CATEGORY_NAME\", \"PARENT_CATEGORY_ID\", \"UPDATED_AT\", \"CREATED_AT\") " +
                    "VALUES (" +
                    "(SELECT COALESCE((SELECT MAX(\"PK_CATEGORY_ID\") FROM \"" + tenantId + "\".\"material_category_master\"), 0) + 1), " +
                    "?, ?, current_timestamp, current_timestamp) " +
                    "RETURNING \"PK_CATEGORY_ID\"";

            Object[] params = new Object[] {
                    categoryName,
                    parentCategoryIdInt
            };

            System.out.println("SQL Query: " + strInsertMaterialCategoryData);

            // Execute the query and retrieve the generated PK_CATEGORY_ID
            Integer categoryId = jdbcTemplate.queryForObject(strInsertMaterialCategoryData, params, Integer.class);

            return categoryId;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }


    }


        public Integer insertMaterialSubCategoryData(String tenantId, int categoryIdInt, String subcategoryName) {
            try {
                String strInsertMaterialSubCategoryData = "INSERT INTO \"" + tenantId + "\".\"material_subcategory_master\" (" +
                        "\"pk_subcategory_id\", \"pk_category_id\", \"pk_subcategory_name\", \"updated_at\", \"created_at\") " +
                        "VALUES (" +
                        "(SELECT COALESCE((SELECT MAX(\"pk_subcategory_id\") FROM \"" + tenantId + "\".\"material_subcategory_master\"), 0) + 1), " +
                        "?, ?, current_timestamp, current_timestamp) " +
                        "RETURNING \"pk_subcategory_id\"";

                Object[] params = new Object[]{
                        categoryIdInt,
                        subcategoryName
                };

                System.out.println("SQL Query: " + strInsertMaterialSubCategoryData);

                // Execute the query and retrieve the generated PK_SUBCATEGORY_ID
                Integer subcategoryId = jdbcTemplate.queryForObject(strInsertMaterialSubCategoryData, params, Integer.class);

                return subcategoryId;
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }




    // Method to delete a subcategory by categoryId and subcategoryId
    public boolean deleteSubcategory( String tenantId, int categoryId, int subcategoryId) {
        String sql = "DELETE FROM \"" + tenantId + "\".\"material_subcategory_master\" " +
                "WHERE \"pk_category_id\" = ? AND \"pk_subcategory_id\" = ?";
        int rowsAffected = jdbcTemplate.update(sql, categoryId, subcategoryId);
        return rowsAffected > 0;
    }

    // Method to delete category and all subcategories by categoryId
    public boolean deleteCategoryAndSubcategories( String tenantId, int categoryId) {
        String sqlDeleteSubcategories = "DELETE FROM \"" + tenantId + "\".\"material_subcategory_master\" " +
                "WHERE \"pk_category_id\" = ?";
        String sqlDeleteCategory = "DELETE FROM \"" + tenantId + "\".\"material_category_master\" " +
                "WHERE \"PK_CATEGORY_ID\" = ?";

        jdbcTemplate.update(sqlDeleteSubcategories, categoryId); // Delete subcategories first
        int rowsAffected = jdbcTemplate.update(sqlDeleteCategory, categoryId); // Then delete the category itself
        return rowsAffected > 0;
    }
}




