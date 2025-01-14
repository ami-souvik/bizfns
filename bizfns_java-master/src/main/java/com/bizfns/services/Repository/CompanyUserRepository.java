package com.bizfns.services.Repository;

import com.bizfns.services.Entity.CompanyUserEntity;
import org.json.simple.JSONObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface


CompanyUserRepository extends JpaRepository<CompanyUserEntity, Long> {


  @Query(value = "SELECT \"OTP\"\n" +
          "          FROM \"Bizfns\".\"USER_OTP\"\n" +
          "          WHERE \"FK_USER_ID\" = (\n" +
          "              SELECT \"COMPANY_ID\" \n" +
          "              FROM \"Bizfns\".\"COMPANY_MASTER\"\n" +
          "              WHERE (\"COMPANY_BACKUP_PHONE_NUMBER\" = :userId OR \"COMPANY_BACKUP_EMAIL\" = :userId\n)" +
          "              AND \"SCHEMA_ID\" = :tenantId\n" +
          "          ) \n" +
          "          AND \"USER_TYPE\" = (SELECT CAST(\"PK_USER_TYPE_ID\" AS VARCHAR) AS \"PK_USER_TYPE_ID_STRING\"\n" +
          "FROM \"Bizfns\".\"USER_TYPE_MASTER\"\n" +
          "WHERE LOWER(\"USER_TYPE_MASTER_ENTITY\") = 'company')\n" +
          "          AND DATE_TRUNC('day', \"OTP_CREATED_AT\") = DATE_TRUNC('day', CURRENT_DATE)", nativeQuery = true)
  String checkOtpExistence(String userId,String tenantId);


  @Query(value = "SELECT  \"PASSWORD\"\n" +
          "          FROM \"Bizfns\".\"COMPANY_MASTER\"\n" +
          "          WHERE (\"COMPANY_BACKUP_PHONE_NUMBER\" = :userId or \"COMPANY_BACKUP_EMAIL\" = :userId) AND \"SCHEMA_ID\" = :tenantId", nativeQuery = true)
  String checkOldPasswordCompMast(String userId,String tenantId);

  @Query(value = "select \"USER_PHONE_NUMBER\" from \"Bizfns\".\"COMPANY_USER\"  where \"USER_PHONE_NUMBER\" = :userId or \"USER_EMAIL\" = :userId\n", nativeQuery = true)
  List<JSONObject> dataForUserIdValidationFromCompanyUser(String userId);
  @Query(value = "select \"COMPANY_BACKUP_PHONE_NUMBER\", \"BUSINESS_NAME\" from \"Bizfns\".\"COMPANY_MASTER\" where \"COMPANY_BACKUP_PHONE_NUMBER\" = :userId or \"COMPANY_BACKUP_EMAIL\" = :userId ", nativeQuery = true)
  List<JSONObject> dataForUserIdValidationFromCompanyMaster(String userId);

  @Query(value = "select \"COMPANY_BACKUP_PHONE_NUMBER\" from \"Bizfns\".\"COMPANY_MASTER\" where (\"COMPANY_BACKUP_PHONE_NUMBER\" = :userId OR \"COMPANY_BACKUP_EMAIL\" = :userId) AND \"SCHEMA_ID\" = :tenantId", nativeQuery = true)
  List<JSONObject> dataUserIdValidationFromCompanyMaster(String userId, String tenantId);
  @Query(value = "select \"CUSTOMER_PHONE_NUMBER\" from \"Bizfns\".\"COMPANY_CUSTOMER\"   where \"CUSTOMER_PHONE_NUMBER\" = :userId or \"CUSTOMER_EMAIL\" = :userId", nativeQuery = true)
  List<JSONObject> dataForUserIdValidationFromCompanyCust(String userId);
  @Query(value = "SELECT \"PASSWORD\" FROM \"Bizfns\".\"COMPANY_MASTER\" WHERE (\"COMPANY_BACKUP_PHONE_NUMBER\" = :userId OR \"COMPANY_BACKUP_EMAIL\" = :userId) AND \"SCHEMA_ID\" = :tenantId", nativeQuery = true)
  String dataForPasswordValidationFromCompanyMaster(String userId, String tenantId);


  @Query(value = "select \"USER_PASSWORD\" from \"agni2803\".\"COMPANY_USER\"  where \"USER_PHONE_NUMBER\" = :userId or \"USER_EMAIL\" = :userId\n", nativeQuery = true)
  List<JSONObject> dataForPasswordValidationFromCompanyUser(String userId);



  @Query(value = "select \n" +
          " \"OTP\" from  \"Bizfns\".\"USER_OTP\"  WHERE \"FK_USER_ID\" = (\n" +
          "    SELECT \"COMPANY_ID\" \n" +
          "    FROM \"Bizfns\".\"COMPANY_MASTER\"\n" +
          "    WHERE (\"COMPANY_BACKUP_PHONE_NUMBER\" = :userId or \"COMPANY_BACKUP_EMAIL\" = :userId) AND \"SCHEMA_ID\" = :tenantId\n" +
          ") and \"USER_TYPE\" = (SELECT CAST(\"PK_USER_TYPE_ID\" AS VARCHAR) AS \"PK_USER_TYPE_ID_STRING\"\n" +
          "FROM \"Bizfns\".\"USER_TYPE_MASTER\"\n" +
          "WHERE LOWER(\"USER_TYPE_MASTER_ENTITY\") = 'company') and DATE_TRUNC('day', \"OTP_CREATED_AT\") = DATE_TRUNC('day', CURRENT_DATE)", nativeQuery = true)
  String fetchOtp(String userId, String tenantId);

  @Query(value = "SELECT CASE\n" +
          "    WHEN EXISTS (\n" +
          "        SELECT 1\n" +
          "        FROM \"agni2803\".\"user_otp\"\n" +
          "        WHERE \"FK_USER_ID\" = (\n" +
          "            SELECT \"FK_COMPANY_ID\" \n" +
          "            FROM \"agni2803\".\"company_user\"\n" +
          "            WHERE \"USER_PHONE_NUMBER\" = :userId OR \"USER_EMAIL\" = :userId\n" +
          "        ) \n" +
          "        AND \"USER_TYPE\" = :userStaffType \n" +
          "        AND DATE_TRUNC('day', \"OTP_CREATED_AT\") = DATE_TRUNC('day', CURRENT_DATE)\n" +
          "        AND \"OTP_UPDATED_AT\" >= NOW() - INTERVAL '2 minutes'\n" +
          "    )\n" +
          "    THEN 'y'\n" +
          "    ELSE 'n'\n" +
          "END", nativeQuery = true)
  String isOtpExpireForStaff(String userId,String userStaffType);

  @Query(value = "SELECT CASE\n" +
          "    WHEN EXISTS (\n" +
          "        SELECT 1\n" +
          "        FROM \"Bizfns\".\"USER_OTP\"\n" +
          "        WHERE \"FK_USER_ID\" = (\n" +
          "            SELECT \"COMPANY_ID\" \n" +
          "            FROM \"Bizfns\".\"COMPANY_MASTER\"\n" +
          "            WHERE (\"COMPANY_BACKUP_PHONE_NUMBER\" = :userId OR \"COMPANY_BACKUP_EMAIL\" = :userId)\n" +
          "      AND \"SCHEMA_ID\" = :tenantId) \n" +
          "        AND \"USER_TYPE\" = (SELECT CAST(\"PK_USER_TYPE_ID\" AS VARCHAR) AS \"PK_USER_TYPE_ID_STRING\"\n" +
          "FROM \"Bizfns\".\"USER_TYPE_MASTER\"\n" +
          "WHERE LOWER(\"USER_TYPE_MASTER_ENTITY\") = 'company') \n" +
          "        AND DATE_TRUNC('day', \"OTP_CREATED_AT\") = DATE_TRUNC('day', CURRENT_DATE)\n" +
          "        AND \"OTP_UPDATED_AT\" >= NOW() - INTERVAL '2 minutes'\n" +
          "    )\n" +
          "    THEN 'y'\n" +
          "    ELSE 'n'\n" +
          "END", nativeQuery = true)
  String isOtpExpire(String userId, String tenantId);


  @Query(value = "SELECT *\n" +
          "    FROM \"Bizfns\".\"COMPANY_SECURITY_QUESTION_ANSWER\"\n" +
          "    WHERE \"FK_COMPANY_MASTER_ID\" IN (\n" +
          "        SELECT \"COMPANY_ID\"\n" +
          "        FROM \"Bizfns\".\"COMPANY_MASTER\"\n" +
          "        WHERE (\"COMPANY_BACKUP_PHONE_NUMBER\" = :userId OR \"COMPANY_BACKUP_EMAIL\" = :userId \n)" +
          "        AND \"SCHEMA_ID\" = :tenantId \n"+
          "    )", nativeQuery = true)
  List<JSONObject> checkSecurityQuestionByUserId(String userId, String tenantId);


  @Query(value = "SELECT \"PK_QUESTION_ID\", \"QUESTION\", '' AS answeer\n" +
          "FROM \"Bizfns\".\"SECURITY_QUESTION_MASTER\"", nativeQuery = true)
  List<JSONObject> fetchSecurityQuestionByUserId();


  @Query(value = "select  \n" +
          "\"COMPANY_SECURITY_QUESTION\" AS \"QUESTION\",\n" +
          "\"COMPANY_SECURITY_ANSWER\" AS answeer,\n" +
          "\"FK_QUESTION_ID\" AS \"PK_QUESTION_ID\" from\n" +
          "\"Bizfns\".\"COMPANY_SECURITY_QUESTION_ANSWER\" where \"FK_COMPANY_MASTER_ID\" IN (\n" +
          "        SELECT \"COMPANY_ID\"\n" +
          "        FROM \"Bizfns\".\"COMPANY_MASTER\"\n" +
          "        WHERE (\"COMPANY_BACKUP_PHONE_NUMBER\" = :userId OR \"COMPANY_BACKUP_EMAIL\" = :userId\n)" +
          "        AND \"SCHEMA_ID\" = :tenantId \n"+
          "    )", nativeQuery = true)
  List<JSONObject> getSecurityQuestionAnsByUserId(String userId,String tenantId);

  @Modifying
@Transactional
  @Query(value = "insert into \"Bizfns\".\"COMPANY_SECURITY_QUESTION_ANSWER\"\n" +
          "(\"FK_COMPANY_MASTER_ID\",\n" +
          "\"COMPANY_SECURITY_QUESTION\",\n" +
          "\"COMPANY_SECURITY_ANSWER\",\n" +
          "\"FK_QUESTION_ID\") values(( \n" +
          "                SELECT \"COMPANY_ID\" \n" +
          "                FROM \"Bizfns\".\"COMPANY_MASTER\" \n" +
          "                WHERE  (\"COMPANY_BACKUP_PHONE_NUMBER\" = :userId or \"COMPANY_BACKUP_EMAIL\" = :userId  \n)" +
          "                AND \"SCHEMA_ID\" = :tenantId \n"+
          "            ), :questionName, :answer,:questionId\n" +
          ")", nativeQuery = true)
  void companyQuestionAnsSave(Integer questionId, String questionName, String answer, String userId, String tenantId);


  @Query(value = "SELECT CASE\n" +
          "    WHEN EXISTS (\n" +
          "        SELECT 1\n" +
          "        FROM \"Bizfns\".\"COMPANY_SECURITY_QUESTION_ANSWER\"\n" +
          "        WHERE \"FK_COMPANY_MASTER_ID\" IN (\n" +
          "            SELECT \"COMPANY_ID\"\n" +
          "            FROM \"Bizfns\".\"COMPANY_MASTER\"\n" +
          "            WHERE (\"COMPANY_BACKUP_PHONE_NUMBER\" = :userId OR \"COMPANY_BACKUP_EMAIL\" = :userId\n)" +
          "            AND \"SCHEMA_ID\" = :tenantId\n" +
          "        )\n" +
          "    )\n" +
          "    THEN 'y'\n" +
          "    ELSE 'n'\n" +
          "END ", nativeQuery = true)
  String availableAns(String userId,String tenantId);

  @Query(value = "SELECT \"SCHEMA_ID\" FROM \"Bizfns\".\"COMPANY_MASTER\" WHERE (\"COMPANY_BACKUP_EMAIL\" = :userId OR \"COMPANY_BACKUP_PHONE_NUMBER\" = :userId) AND \"SCHEMA_ID\" = :tenantId", nativeQuery = true)
  String dbCompBusinessId(String userId, String tenantId);


//  @Query(value = "select \"SCHEMA_ID\" from \"Bizfns\".\"COMPANY_MASTER\" where \"COMPANY_BACKUP_EMAIL\" = :userId or \n" +
//          "\"COMPANY_BACKUP_PHONE_NUMBER\" = :userId", nativeQuery = true)
//  String dbCompBusinessId(String userId, String tenantId);

  @Query(value = "select \"SCHEMA_ID\" from \"Bizfns\".\"COMPANY_MASTER\" where \"SCHEMA_ID\" = :tenantId ", nativeQuery = true)
    List<JSONObject> dataForTenantValidationFromCompanyMaster(String tenantId);

  @Query(value = "select \"ID\", \"RATE_UNIT_NAME\", \"STATUS\" from \"Bizfns\".\"SERVICE_RATE_UNIT_MASTER\"", nativeQuery = true)
  List<JSONObject> getServiceRateUnitList();

  @Query(value = "select \"PASSWORD\"  from  \"Bizfns\".\"ADMIN_MASTER\"  where \"USER_ID\" = :userId\n" +
          "or \"ADMIN_EMAIL\"  = :userId", nativeQuery = true)
  String dataForPasswordValidationFromAdminMaster(String userId);


 // String dataForPasswordValidationFromMaster(String userId);

}
