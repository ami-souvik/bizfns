package com.bizfns.services.Repository;

import com.bizfns.services.Entity.CompanyMasterEntity;

import org.json.simple.JSONObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import javax.transaction.Transactional;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface CompanyMasterRepository extends JpaRepository<CompanyMasterEntity, Long> {



    @Query(value = "select \n" +
            "\"PK_SUBSCRIPTION_PLAN_ID\",\n" +
            "\"SUBSCRIPTION_USER_LIMIT\",\n" +
            "\"SUBSCRIPTION_ENTITY\",\n" +
            "\"SUBSCRIPTION_DURATION\",\n" +
            "\"DESCRIPTION\",\"SUBSCRIPTION_ENTITY_PRICE\" from \"Bizfns\".\"SUBSCRIPTION_PLAN_MASTER\"", nativeQuery = true)
    List<JSONObject> fetchPreRegistrationDataForCompanySubscription();


    @Query(value = "SELECT \"PK_BUSINESS_TYPE_ID\", \"BUSINESS_TYPE_ENTITY\"\n" +
            "FROM \"Bizfns\".\"BUSINESS_TYPE_MASTER\"", nativeQuery = true)
    List<JSONObject> fetchPreRegistrationDataForBusinessTypeMaster();

    @Query(value = "SELECT \"TNC_DESCRIPTION\"\n" +
            "FROM \"Bizfns\".\"TERMS_AND_CONDITION\"\n", nativeQuery = true)
    String fetchPreRegistrationDataForTAndC();

    @Query(value = "SELECT \"USER_TYPE\"\n" +
            "FROM \"Bizfns\".\"USER_OTP\"\n" +
            "WHERE \"FK_USER_ID\" = (\n" +
            "    SELECT \"COMPANY_ID\" \n" +
            "    FROM \"Bizfns\".\"COMPANY_MASTER\"\n" +
            "    WHERE (\"COMPANY_BACKUP_PHONE_NUMBER\" = :userId OR \"COMPANY_BACKUP_EMAIL\" = :userId) \n" +
            " AND \"SCHEMA_ID\" = :tenantId) \n" +
            "AND \"USER_TYPE\" = (SELECT CAST(\"PK_USER_TYPE_ID\" AS VARCHAR) AS \"PK_USER_TYPE_ID_STRING\"\n" +
            "FROM \"Bizfns\".\"USER_TYPE_MASTER\"\n" +
            "WHERE LOWER(\"USER_TYPE_MASTER_ENTITY\") = 'company') \n" +
            "AND DATE_TRUNC('day', \"OTP_CREATED_AT\") = DATE_TRUNC('day', CURRENT_DATE)", nativeQuery = true)
    List<JSONObject> checkOtpExistence(String userId, String tenantId);

    @Query(value = "SELECT \"USER_TYPE\"\n" +
            "FROM \"agni2803\".\"USER_OTP\"\n" +
            "WHERE \"FK_USER_ID\" = (\n" +
            "    SELECT \"FK_COMPANY_ID\" \n" +
            "    FROM \"agni2803\".\"COMPANY_USER\"\n" +
            "    WHERE \"USER_PHONE_NUMBER\" = :userId OR \"USER_EMAIL\" = :userId \n" +
            ") \n" +
            "AND \"USER_TYPE\" = :userType \n" +
            "AND DATE_TRUNC('day', \"OTP_CREATED_AT\") = DATE_TRUNC('day', CURRENT_DATE)", nativeQuery = true)
    List<JSONObject> checkOtpExistenceForStaff(@Param("userId") String userId, @Param("userType") String userType);


    @Modifying
    @Transactional
    @Query(value = " UPDATE \"Bizfns\".\"COMPANY_MASTER\"\n" +
            "SET \"PASSWORD\" = :newPassword \n" +
            "WHERE  (\"COMPANY_BACKUP_PHONE_NUMBER\" = :userId or \"COMPANY_BACKUP_EMAIL\" = :userId)" +
            "AND \"SCHEMA_ID\" = :tenantId \n", nativeQuery = true)
    void updatePassword(String userId, String newPassword, String tenantId);

    @Transactional
    @Query(value = "INSERT INTO \"Bizfns\".\"USER_OTP\" (\"FK_USER_ID\", \"USER_TYPE\", \"OTP\", \"OTP_CREATED_AT\", \"OTP_COUNT\",\n" +
            "\"SCHEMA_ID\") \n" +
            "            VALUES (\n" +
            "                (\n" +
            "                    SELECT \"COMPANY_ID\" \n" +
            "                    FROM \"Bizfns\".\"COMPANY_MASTER\" \n" +
            "                    WHERE (\"COMPANY_BACKUP_PHONE_NUMBER\" =:userId or \"COMPANY_BACKUP_EMAIL\" = :userId)\n" +
            "               AND \"SCHEMA_ID\" = :tenantId), \n" +
            "                (SELECT CAST(\"PK_USER_TYPE_ID\" AS VARCHAR) AS \"PK_USER_TYPE_ID_STRING\"\n" +
            "FROM \"Bizfns\".\"USER_TYPE_MASTER\"\n" +
            "WHERE LOWER(\"USER_TYPE_MASTER_ENTITY\") = 'company'), \n" +
            "                :randomNumber, \n" +
            "                current_timestamp,\n" +
            "                '0',\n" +
            "                :tenantId \n" +
            "                \n" +
            "            ) \n" +
            "            RETURNING EXTRACT(EPOCH FROM \"OTP_UPDATED_AT\") * 1000 AS \"OTP_UPDATED_AT_MS\"", nativeQuery = true)
    String insertFirstLoginOtp(String randomNumber, String userId, String tenantId);

    @Transactional
    @Query(value = "INSERT INTO \"agni2803\".\"user_otp\" (\"FK_USER_ID\", \"USER_TYPE\", \"OTP\", \"OTP_CREATED_AT\", \"OTP_COUNT\",\n" +
            "\"SCHEMA_ID\") \n" +
            "            VALUES (\n" +
            "                (\n" +
            "                    SELECT \"FK_COMPANY_ID\" \n" +
            "                    FROM \"agni2803\".\"company_user\" \n" +
            "                    WHERE \"USER_PHONE_NUMBER\" =:userId or \"USER_EMAIL\" = :userId\n" +
            "                ), \n" +
            "                :userType, \n" +
            "                :randomNumber, \n" +
            "                current_timestamp,\n" +
            "                '0',\n" +
            "                :tenantId \n" +
            "                \n" +
            "            ) \n" +
            "            RETURNING EXTRACT(EPOCH FROM \"OTP_UPDATED_AT\") * 1000 AS \"OTP_UPDATED_AT_MS\"", nativeQuery = true)
    String insertFirstLoginOtpForStaff(String randomNumber, String userId, String tenantId, String userType);





    @Transactional
    @Query(value = "UPDATE \"Bizfns\".\"USER_OTP\" \n" +
            "SET \"OTP\" = :randomNumber, \n" +
            "    \"OTP_UPDATED_AT\" = current_timestamp,\n" +
            "    \"OTP_COUNT\" = \"OTP_COUNT\" + 1\n" +
            "WHERE \"FK_USER_ID\" = ( \n" +
            "    SELECT \"COMPANY_ID\" \n" +
            "    FROM \"Bizfns\".\"COMPANY_MASTER\" \n" +
            "    WHERE  (\"COMPANY_BACKUP_PHONE_NUMBER\" = :userId or \"COMPANY_BACKUP_EMAIL\" = :userId)\n" +
            "AND \"SCHEMA_ID\" = :tenantId)\n" +
            "AND \"USER_TYPE\" = (SELECT CAST(\"PK_USER_TYPE_ID\" AS VARCHAR) AS \"PK_USER_TYPE_ID_STRING\"\n" +
            "FROM \"Bizfns\".\"USER_TYPE_MASTER\"\n" +
            "WHERE LOWER(\"USER_TYPE_MASTER_ENTITY\") = 'company') AND DATE_TRUNC('day', \"OTP_CREATED_AT\") = DATE_TRUNC('day', CURRENT_DATE) " +
            "            RETURNING EXTRACT(EPOCH FROM \"OTP_UPDATED_AT\") * 1000 AS \"OTP_UPDATED_AT_MS\"\n", nativeQuery = true)
    String updateLoginOtp(String randomNumber, String userId, String tenantId);

//    @Query(value = "UPDATE \"agni2803\".\"user_otp\" \n" +
//            "SET \"OTP\" = :randomNumber, \n" +
//            "    \"OTP_UPDATED_AT\" = current_timestamp,\n" +
//            "    \"OTP_COUNT\" = \"OTP_COUNT\" + 1\n" +
//            "WHERE \"FK_USER_ID\" = ( \n" +
//            "    SELECT \"FK_COMPANY_ID\" \n" +
//            "    FROM \"agni2803\".\"company_user\" \n" +
//            "    WHERE  \"USER_PHONE_NUMBER\" = :userId or \"USER_EMAIL\" = :userId\n" +
//            ")\n" +
//            "AND \"USER_TYPE\" = :userType AND DATE_TRUNC('day', \"OTP_CREATED_AT\") = DATE_TRUNC('day', CURRENT_DATE) " +
//            "            RETURNING EXTRACT(EPOCH FROM \"OTP_UPDATED_AT\") * 1000 AS \"OTP_UPDATED_AT_MS\"\n", nativeQuery = true)
//    String updateLoginOtpForStaff(String randomNumber, String userId,String userType);



    @Query(value = "SELECT \"COMPANY_ID\",\n" +
            "\"BUSINESS_NAME\",\n" +
            "\"COMPANY_BACKUP_EMAIL\",\n" +
            "\"COMPANY_LOGO\",\n" +
            "\"SCHEMA_ID\",\n" +
            "\"COMPANY_BACKUP_PHONE_NUMBER\"\n" +
            " from \"Bizfns\".\"COMPANY_MASTER\" where (\"COMPANY_BACKUP_EMAIL\" = :userId or \n" +
            "\"COMPANY_BACKUP_PHONE_NUMBER\" = :userId) AND \"SCHEMA_ID\" = :tenantId", nativeQuery = true)
    List<JSONObject> fetchUserDetails(String userId, String tenantId);


    @Query(value = "SELECT \n" +
            "    CASE\n" +
            "        WHEN EXISTS (\n" +
            "            SELECT 1\n" +
            "            FROM \"Bizfns\".\"TOKEN_MASTER\"\n" +
            "            WHERE \"USER_ID\" = (\n" +
            "                SELECT \"COMPANY_ID\"\n" +
            "                FROM \"Bizfns\".\"COMPANY_MASTER\"\n" +
            "                WHERE (\"COMPANY_BACKUP_PHONE_NUMBER\" = :userId OR \"COMPANY_BACKUP_EMAIL\" = :userId)\n" +
            "                AND \"SCHEMA_ID\" = :tenantId\n" +
            "            )\n" +
            "        )\n" +
            "        THEN 'y'\n" +
            "        ELSE 'n'\n" +
            "    END;", nativeQuery = true)
    String tokenData(String userId,String tenantId);

    @Query(value = "SELECT CASE\n" +
            "    WHEN EXISTS (\n" +
            "        SELECT 1\n" +
            "        FROM \"Bizfns\".\"TOKEN_MASTER\"\n" +
            "        WHERE \"USER_ID\" = (\n" +
            "            SELECT \"FK_COMPANY_ID\"\n" +
            "            FROM \"agni2803\".\"company_user\"\n" +
            "            WHERE \"USER_PHONE_NUMBER\" = :userId OR \"USER_EMAIL\" = :userId\n" +
            "        )\n" +
            "    )\n" +
            "    THEN 'y'\n" +
            "    ELSE 'n'\n" +
            "END", nativeQuery = true)
    String tokenDataStaff(String userId);

    @Query(value = "SELECT CASE\n" +
            "    WHEN EXISTS (\n" +
            "        SELECT 1\n" +
            "        FROM \"Bizfns\".\"TOKEN_MASTER\"\n" +
            "        WHERE \"USER_ID\" = (\n" +
            "            SELECT \"COMPANY_ID\"\n" +
            "            FROM \"agni\".\"COMPANY_MASTER\"\n" +
            "            WHERE \"COMPANY_BACKUP_PHONE_NUMBER\" = :userId OR \"COMPANY_BACKUP_EMAIL\" = :userId\n" +
            "        )\n" +
            "    )\n" +
            "    THEN 'y'\n" +
            "    ELSE 'n'\n" +
            "END", nativeQuery = true)
    String tokenDataForStaff(String userId);
@Modifying
    @Transactional
    @Query(value = "INSERT INTO \"Bizfns\".\"TOKEN_MASTER\"\n" +
            "    (\"USER_ID\", \"SCHEMA_ID\", \"TOKEN\", \"CREATED_AT\", \"UPDATED_AT\")\n" +
            "VALUES\n" +
            "    (( \n" +
            "    SELECT \"COMPANY_ID\" \n" +
            "    FROM \"Bizfns\".\"COMPANY_MASTER\" \n" +
            "    WHERE  (\"COMPANY_BACKUP_PHONE_NUMBER\" = :userId or \"COMPANY_BACKUP_EMAIL\" = :userId)\n" +
            " AND \"SCHEMA_ID\" = :tenantId ), :tenantId, :token, current_timestamp, current_timestamp)", nativeQuery = true)
    void isertCompanyToken(String userId, String tenantId, String token);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO \"Bizfns\".\"TOKEN_MASTER\"\n" +
            "    (\"USER_ID\", \"SCHEMA_ID\", \"TOKEN\", \"CREATED_AT\", \"UPDATED_AT\")\n" +
            "VALUES\n" +
            "    (( \n" +
            "    SELECT \"COMPANY_ID\" \n" +
            "    FROM \"agni2803\".\"company_user\" \n" +
            "    WHERE  \"COMPANY_BACKUP_PHONE_NUMBER\" = :userId or \"COMPANY_BACKUP_EMAIL\" = :userId\n" +
            "), :tenantId, :token, current_timestamp, current_timestamp)", nativeQuery = true)
    void isertCompanyTokenForStaff(String userId, String tenantId, String token);

    @Modifying
    @Transactional
    @Query(value = "UPDATE \"Bizfns\".\"TOKEN_MASTER\"\n" +
            "SET \"TOKEN\" = :token,\n" +
            "    \"UPDATED_AT\" = current_timestamp\n" +
            "WHERE \"USER_ID\" = ( \n" +
            "    SELECT \"COMPANY_ID\" \n" +
            "    FROM \"Bizfns\".\"COMPANY_MASTER\" \n" +
            "    WHERE  (\"COMPANY_BACKUP_PHONE_NUMBER\" = :userId or \"COMPANY_BACKUP_EMAIL\" = :userId)\n" +
            " AND \"SCHEMA_ID\" = :tenantId )", nativeQuery = true)
    void updateCompanyToken(String userId,  String token, String tenantId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE \"Bizfns\".\"TOKEN_MASTER\"\n" +
            "SET \"TOKEN\" = :token,\n" +
            "    \"UPDATED_AT\" = current_timestamp\n" +
            "WHERE \"USER_ID\" = ( \n" +
            "    SELECT \"FK_COMPANY_ID\" \n" +
            "    FROM \"agni2803\".\"company_user\" \n" +
            "    WHERE  \"USER_PHONE_NUMBER\" = :userId or \"USER_EMAIL\" = :userId\n" +
            ")", nativeQuery = true)
    void updateCompanyTokenForStaff(String userId,  String token);


    @Query(value = "select \"TOKEN\" from   \"Bizfns\".\"TOKEN_MASTER\" where  \"USER_ID\" = ( \n" +
            "    SELECT \"COMPANY_ID\" \n" +
            "    FROM \"Bizfns\".\"COMPANY_MASTER\" \n" +
            "    WHERE  (\"COMPANY_BACKUP_PHONE_NUMBER\" = :userId or \"COMPANY_BACKUP_EMAIL\" = :userId)\n" +
            " AND \"SCHEMA_ID\" = :tenantId ) \n", nativeQuery = true)
    String tokenText(String userId , String tenantId);

    @Query(value = "select \"TOKEN\" from   \"Bizfns\".\"TOKEN_MASTER\" where  \"USER_ID\" = ( \n" +
            "    SELECT \"FK_COMPANY_ID\" \n" +
            "    FROM \"agni2803\".\"company_user\" \n" +
            "    WHERE  \"USER_PHONE_NUMBER\" = :userId or \"USER_EMAIL\" = :userId\n" +
            ") \n", nativeQuery = true)
    String tokenTextForStaff(String userId );

    @Query(value = "SELECT CASE\n" +
            "    WHEN EXISTS (\n" +
            "        SELECT 1\n" +
            "        FROM \"Bizfns\".\"COMPANY_SECURITY_QUESTION_ANSWER\"\n" +
            "        WHERE \"FK_COMPANY_MASTER_ID\" = (\n" +
            "            SELECT \"COMPANY_ID\"\n" +
            "            FROM \"Bizfns\".\"COMPANY_MASTER\"\n" +
            "            WHERE (\"COMPANY_BACKUP_PHONE_NUMBER\" = :userId OR \"COMPANY_BACKUP_EMAIL\" = :userId)\n" +
            "      AND \"SCHEMA_ID\" = :tenantId  )\n" +
            "    )\n" +
            "    THEN 'Y'\n" +
            "    ELSE 'N'\n" +
            "END", nativeQuery = true)
    String securityQuestionDate(String userId, String tenantId);


    @Query(value = "SELECT CASE\n" +
            "    WHEN \"COMPANY_LOGO\" IS NOT NULL AND \"COMPANY_LOGO\" <> '' THEN 'Y'\n" +
            "    ELSE 'N'\n" +
            "END AS \"ValueExists\"\n" +
            "FROM \"Bizfns\".\"COMPANY_MASTER\"\n" +
            "WHERE (\"COMPANY_BACKUP_PHONE_NUMBER\" = :userId OR \"COMPANY_BACKUP_EMAIL\" = :userId) AND \"SCHEMA_ID\" = :tenantId", nativeQuery = true)
    String logoData(String userId, String tenantId);


    @Query(value = "select a.\"PK_CATEGORY_ID\", a.\"CATEGORY_DESCRIPTION\" ,\n" +
            "            a.\"CATEGORY_NAME\", b.\"PK_BUSINESS_TYPE_ID\",\n" +
            "            b.\"BUSINESS_TYPE_ENTITY\" from \"Bizfns\".\"CATEGORY_MASTER\" a\n" +
            "            Join \n" +
            "            \"Bizfns\".\"BUSINESS_TYPE_MASTER\" b on a.\"PK_CATEGORY_ID\"= b.\"FK_CATEGORY_ID\" order by  b.\"PK_BUSINESS_TYPE_ID\"", nativeQuery = true)
    List<JSONObject> businessCategoryAndType();

    @Modifying
    @Transactional
    @Query(value = "update \"Bizfns\".\"COMPANY_MASTER\" set \"PASSWORD\" = :newPassword where (\"COMPANY_BACKUP_EMAIL\" = :userId or\n" +
            "\"COMPANY_BACKUP_PHONE_NUMBER\" = :userId) AND \"SCHEMA_ID\" = :tenantId", nativeQuery = true)
    void saveChangePassWord(String userId,String tenantId, String newPassword);


    @Modifying
    @Transactional
    @Query(value = "update \"Bizfns\".\"COMPANY_MASTER\" set \"PASSWORD\" = :newPassword where (\"COMPANY_BACKUP_EMAIL\" = :userId or\n" +
            "\"COMPANY_BACKUP_PHONE_NUMBER\" = :userId) AND \"SCHEMA_ID\" = 'adminOwn'", nativeQuery = true)
    void saveChangePassWordCompanyMaster(String userId, String newPassword);

    @Query(value = "SELECT\n" +
            "  CASE\n" +
            "    WHEN LOWER(\"COMPANY_SECURITY_ANSWER\") = LOWER(:answer) THEN true\n" +
            "    ELSE false\n" +
            "  END AS result\n" +
            "FROM\n" +
            "  \"Bizfns\".\"COMPANY_SECURITY_QUESTION_ANSWER\"\n" +
            "WHERE\n" +
            "  \"FK_COMPANY_MASTER_ID\" =  (SELECT \"COMPANY_ID\"\n" +
            "FROM \"Bizfns\".\"COMPANY_MASTER\"\n" +
            "WHERE (\"COMPANY_BACKUP_PHONE_NUMBER\" = :userId OR \"COMPANY_BACKUP_EMAIL\" = :userId\n)" +
            "AND \"SCHEMA_ID\" = :tenantId" +
            ") AND \"FK_QUESTION_ID\" = :questionId", nativeQuery = true)
    boolean checkAns(String userId, Integer questionId, String answer, String tenantId);


    @Query(value = "select \"COMPANY_BACKUP_PHONE_NUMBER\" FROM \"Bizfns\".\"COMPANY_MASTER\" where \"COMPANY_BACKUP_PHONE_NUMBER\" = :userId OR \"COMPANY_BACKUP_EMAIL\" = :userId ", nativeQuery = true)
    String fetchMobileNoForCompany(String userId);


    @Modifying
    @Transactional
    @Query(value = "update \"Bizfns\".\"COMPANY_MASTER\" set \"COMPANY_BACKUP_PHONE_NUMBER\" = :newMobileNo where  \"COMPANY_BACKUP_PHONE_NUMBER\" = :currentMobileNo", nativeQuery = true)
    void changeMobileNo(String currentMobileNo, String newMobileNo);

    @Query(value = "(SELECT CAST(\"PK_USER_TYPE_ID\" AS VARCHAR) AS \"PK_USER_TYPE_ID_STRING\"\n" +
            "FROM \"Bizfns\".\"USER_TYPE_MASTER\"\n" +
            "WHERE LOWER(\"USER_TYPE_MASTER_ENTITY\") = 'company')", nativeQuery = true)
    String fetchUserTypeId();


    @Query(value = "(SELECT CAST(\"PK_USER_TYPE_ID\" AS VARCHAR) AS \"PK_USER_TYPE_ID_STRING\"\n" +
            "FROM \"Bizfns\".\"USER_TYPE_MASTER\"\n" +
            "WHERE LOWER(\"USER_TYPE_MASTER_ENTITY\") = 'staff')", nativeQuery = true)
    String fetchStaffTypeId();

    @Query(value = "SELECT\n" +
            "                CASE\n" +
            "                    WHEN \"UPDATED_AT\" <= NOW() - INTERVAL '11 HOURS' THEN 'y'\n" +
            "                    ELSE 'n'\n" +
            "                END AS y\n" +
            "            FROM \"Bizfns\".\"TOKEN_MASTER\" where \"USER_ID\" = \t(SELECT \"COMPANY_ID\"\n" +
            "            FROM \"Bizfns\".\"COMPANY_MASTER\"\n" +
            "            WHERE (\"COMPANY_BACKUP_PHONE_NUMBER\" = :userId OR \"COMPANY_BACKUP_EMAIL\" = :userId) AND \"SCHEMA_ID\" = :tenantId\n" +
            "            )", nativeQuery = true)
    String checkTokenValidation(String userId, String tenantId);

    @Query(value = "SELECT\n" +
            "                CASE\n" +
            "                    WHEN \"UPDATED_AT\" <= NOW() - INTERVAL '11 HOURS' THEN 'y'\n" +
            "                    ELSE 'n'\n" +
            "                END AS y\n" +
            "            FROM \"Bizfns\".\"TOKEN_MASTER\" where \"USER_ID\" = \t(SELECT \"FK_COMPANY_ID\"\n" +
            "            FROM \"agni2803\".\"company_user\"\n" +
            "            WHERE \"USER_PHONE_NUMBER\" = :userId OR \"USER_EMAIL\" = :userId\n" +
            "            )", nativeQuery = true)
    String checkTokenValidationForStaff(String userId);

    @Query(value = "select \"BUSINESS_NAME\" from \"Bizfns\".\"COMPANY_MASTER\" where \"COMPANY_ID\" = :companyId", nativeQuery = true)
    String fetchCompanyName(Integer companyId);

    @Query(value = "select \"SCHEMA_ID\" from \"Bizfns\".\"COMPANY_MASTER\" where \"COMPANY_BACKUP_PHONE_NUMBER\" = :staffMobile", nativeQuery = true)
    List<String> fetchCompanyNameByPhNo(String staffMobile);

    @Query(value = "select \"COMPANY_BACKUP_PHONE_NUMBER\" from \"Bizfns\".\"COMPANY_MASTER\"   where \"COMPANY_BACKUP_PHONE_NUMBER\" = :newMobileNo", nativeQuery = true)
    List<org.json.simple.JSONObject> getMobileNoExistence(String newMobileNo);

    @Query(value = "select \"COMPANY_BACKUP_PHONE_NUMBER\" FROM \"Bizfns\".\"COMPANY_MASTER\"   WHERE \"BUSINESS_NAME\" = :companyName", nativeQuery = true)
    List<JSONObject> getPhoneNumberRegisteredWithBusiness(String companyName);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM \"Bizfns\".\"TOKEN_MASTER\"\n" +
            "WHERE \"USER_ID\" = (\n" +
            "    SELECT \"COMPANY_ID\"\n" +
            "    FROM \"Bizfns\".\"COMPANY_MASTER\"\n" +
            "    WHERE \"COMPANY_BACKUP_PHONE_NUMBER\" = :userId OR \"COMPANY_BACKUP_EMAIL\" = :userId\n" +
            ")\n" +
            "AND \"SCHEMA_ID\" = :tenantId", nativeQuery = true)
    void refreshToken(String userId, String tenantId);


    @Query(value = "select \"COMPANY_BACKUP_PHONE_NUMBER\" from  \"Bizfns\".\"COMPANY_MASTER\" where \"COMPANY_BACKUP_PHONE_NUMBER\" = :userId ", nativeQuery = true)
    List<JSONObject> fetchUserMobileNoValidation(String userId);


    @Query(value = "select \"MOBILE_NO\" from  \"Bizfns\".\"PRE_REGISTRATION_OTP\" where \"MOBILE_NO\" = :userId", nativeQuery = true)
    List<JSONObject> fetchRegOtpExistence(String userId);


    @Modifying
    @Transactional
    @Query(value = "insert into  \"Bizfns\".\"PRE_REGISTRATION_OTP\"(\"OTP\",\n" +
            "\"MOBILE_NO\",\n" +
            "\"CREATED_TIME\",\n" +
            "\"UPDATED_TIME\") values(:strRandomNumber, :userId, current_timestamp, current_timestamp)", nativeQuery = true)
    void insertRegOtp(String strRandomNumber, String userId);


    @Modifying
    @Transactional
    @Query(value = "update \"Bizfns\".\"PRE_REGISTRATION_OTP\" set \"OTP\" = :strRandomNumber, \"UPDATED_TIME\" = current_timestamp  where \"MOBILE_NO\" = :userId", nativeQuery = true)
    void updateRegOtp(String strRandomNumber, String userId);


    @Query(value = "select \"OTP\" from  \"Bizfns\".\"PRE_REGISTRATION_OTP\" where \"MOBILE_NO\" = :userId", nativeQuery = true)
    String otpValidation(String userId);



    @Query(value = "SELECT \n" +
            "    CASE \n" +
            "        WHEN CURRENT_TIMESTAMP > \"Bizfns\".\"PRE_REGISTRATION_OTP\".\"UPDATED_TIME\" + INTERVAL '2' MINUTE\n" +
            "        THEN 'n'\n" +
            "        ELSE 'y'\n" +
            "    END AS result\n" +
            "FROM \"Bizfns\".\"PRE_REGISTRATION_OTP\" where \"MOBILE_NO\" = :userId", nativeQuery = true)
    String otpExpiration(String userId);



    @Query(value = "select \"COMPANY_BACKUP_EMAIL\" from  \"Bizfns\".\"COMPANY_MASTER\" where (\"COMPANY_BACKUP_EMAIL\"= :userId or \"COMPANY_BACKUP_PHONE_NUMBER\" = :userId) AND \"SCHEMA_ID\" = :tenantId", nativeQuery = true)
    JSONObject getMailId(String userId, String tenantId);



    @Query(value = "select \"COMPANY_BACKUP_EMAIL\" from  \"Bizfns\".\"COMPANY_MASTER\" where \"COMPANY_BACKUP_EMAIL\" = :emailId ", nativeQuery = true)
    List<JSONObject> fetchUserEmailIdValidation(String emailId);

    @Query(value = "SELECT CASE WHEN \"COMPANY_LOGO\" IS NOT NULL AND \"COMPANY_LOGO\" <> '' THEN 'Y' ELSE 'N' END AS \"ValueExists\"  \n" +
            "FROM \"Bizfns\".\"COMPANY_MASTER\" WHERE \"SCHEMA_ID\" = :tenantFirstEightTLetters ", nativeQuery = true)
    String staffLogoData(String tenantFirstEightTLetters);

    @Query(value = "select \"COMPANY_ID\", \"BUSINESS_NAME\", \"COMPANY_LOGO\" from \"Bizfns\".\"COMPANY_MASTER\" where \"SCHEMA_ID\" = :tenantFirstEightTLetters ", nativeQuery = true)
    List<JSONObject> staffCompData(String tenantFirstEightTLetters);



    @Query(value = "SELECT\n" +
            "    \"SCHEMA_NAME\",\n" +
            "    \"BUSINESS_NAME\"\n" +
            "FROM\n" +
            "    \"Bizfns\".\"USER_MASTER\"\n" +
            "WHERE\n" +
            "    \"EMAIL_ID\" = :userId OR \"MOBILE_NUMBER\" = :userId ", nativeQuery = true)
    List<JSONObject> fetchBusinessIdDetails(String userId);

    @Query(value = "SELECT\n" +
            "    \"SCHEMA_NAME\",\n" +
            "    \"BUSINESS_NAME\"\n" +
            "FROM\n" +
            "    \"Bizfns\".\"USER_MASTER\"\n" +
            "WHERE\n" +
            "(\"EMAIL_ID\" = :userId OR \"MOBILE_NUMBER\" = :userId ) AND \"SCHEMA_NAME\" = :tenentID", nativeQuery = true)
    List<JSONObject> fetchBusinessIdDetail(String userId, String tenentID);

    @Query(value = " SELECT\n" +
            "   \"USER_TYPE\"\n" +
            "FROM\n" +
            "    \"Bizfns\".\"USER_MASTER\"\n" +
            "WHERE\n" +
            "    \"EMAIL_ID\" = :userId OR \"MOBILE_NUMBER\" = :userId ", nativeQuery = true)
    List<JSONObject> fetchUserType(String userId);

    @Query(value = " SELECT\n" +
            "   \"MOBILE_NUMBER\"\n" +
            "FROM\n" +
            "    \"Bizfns\".\"USER_MASTER\"\n" +
            "WHERE\n" +
            "    \"EMAIL_ID\" = :userId OR \"MOBILE_NUMBER\" = :userId", nativeQuery = true)
    List<JSONObject> fetchUserIdValidation(String userId);

    @Query(value = " select um.\"SCHEMA_NAME\"  from \"Bizfns\".\"USER_MASTER\" um where um.\"MOBILE_NUMBER\" = :userId OR \"EMAIL_ID\" = :userId ;\n", nativeQuery = true)
    List<JSONObject> fetchTenentId(String userId);

    @Modifying
    @Transactional
    @Query(value = "update \"Bizfns\".\"USER_MASTER\" set \"MOBILE_NUMBER\" = :newMobileNo where \"MOBILE_NUMBER\" = :currentMobileNo ", nativeQuery = true)
    void updateUserMobileNumber(String currentMobileNo, String newMobileNo);


    @Query(value = "SELECT\n" +
            "  COALESCE(\"COMPANY_BACKUP_EMAIL\", 'N')\n" +
            "FROM\n" +
            "  \"Bizfns\".\"COMPANY_MASTER\"\n" +
            "WHERE\n" +
            "  \"COMPANY_ID\" = :companyId ", nativeQuery = true)
    List<JSONObject> checkMailExistence( Integer companyId);


    @Query(value = " select  \"OTP\" from  \"Bizfns\".\"USER_OTP\"  WHERE \"FK_USER_ID\" = :companyId  and \"USER_TYPE\" = (SELECT CAST(\"PK_USER_TYPE_ID\" AS VARCHAR) AS \"PK_USER_TYPE_ID_STRING\"\n" +
            "          FROM \"Bizfns\".\"USER_TYPE_MASTER\"\n" +
            "          WHERE LOWER(\"USER_TYPE_MASTER_ENTITY\") = 'company') and DATE_TRUNC('day', \"OTP_CREATED_AT\") = DATE_TRUNC('day', CURRENT_DATE) ", nativeQuery = true)
    String getDbOtp(Integer companyId);


    @Modifying
    @Transactional
    @Query(value = "DELETE FROM \"Bizfns\".\"TOKEN_MASTER\"\n" +
            "            WHERE \"USER_ID\" = :userId \n" +
            "            AND \"SCHEMA_ID\" = :tenantId ", nativeQuery = true)
    void refreshTokenForEmail(Integer userId, String tenantId);



    @Modifying
    @Transactional
    @Query(value = "update \"Bizfns\".\"USER_MASTER\" set \"EMAIL_ID\" = :newEmail where \"MOBILE_NUMBER\" = (\n" +
            "select \"COMPANY_BACKUP_PHONE_NUMBER\" from \"Bizfns\".\"COMPANY_MASTER\" where \"COMPANY_ID\" = :companyId ) and \"SCHEMA_NAME\" = :tenantId ", nativeQuery = true)
    void changeUserEmail(Integer companyId, String tenantId, String newEmail);


    @Modifying
    @Transactional
    @Query(value = "update  \"Bizfns\".\"COMPANY_MASTER\" set  \"COMPANY_BACKUP_EMAIL\" = :newEmail where \"CLIENT_ID\"= :companyId ", nativeQuery = true)
    void changeMasterEmail(Integer companyId, String newEmail);


    @Query(value = " SELECT CASE\n" +
            "              WHEN EXISTS (\n" +
            "                  SELECT 1\n" +
            "                  FROM \"Bizfns\".\"USER_OTP\"\n" +
            "                  WHERE \"FK_USER_ID\" = :companyId \n" +
            "                  AND \"USER_TYPE\" = (SELECT CAST(\"PK_USER_TYPE_ID\" AS VARCHAR) AS \"PK_USER_TYPE_ID_STRING\"\n" +
            "          FROM \"Bizfns\".\"USER_TYPE_MASTER\"\n" +
            "          WHERE LOWER(\"USER_TYPE_MASTER_ENTITY\") = 'company') \n" +
            "                  AND DATE_TRUNC('day', \"OTP_CREATED_AT\") = DATE_TRUNC('day', CURRENT_DATE)\n" +
            "                  AND \"OTP_UPDATED_AT\" >= NOW() - INTERVAL '2 minutes'\n" +
            "              )\n" +
            "              THEN 'y'\n" +
            "              ELSE 'n'\n" +
            "          END ", nativeQuery = true)
    String isOtpExpireForEmail(Integer companyId);


    @Query(value = " select \"COMPANY_ID\", \"BUSINESS_NAME\",\n" +
            "\"COMPANY_BACKUP_EMAIL\",\n" +
            "\"SCHEMA_ID\",\n" +
            "\"COMPANY_BACKUP_PHONE_NUMBER\" from \"Bizfns\".\"COMPANY_MASTER\" where \"COMPANY_ID\" = :companyId ", nativeQuery = true)
    List<JSONObject> fetchCompDetails(Integer companyId);

//for dynamic purpose
 /*   @Query(value = " select \"PK_FORM_KEY_ID\" as type,\"ANSWER_TYPE\", \"FK_BUSINESS_TYPE_ID\", \"INPUT_KEY\" as question, \"OPTIONS\" as items, \"GROUP_BY\"\n" +
            "from \"Bizfns\".\"BUSINESS_TYPE_FORM_ENTITIES\" where \"FK_BUSINESS_TYPE_ID\" = ( select \"FK_BUSINESS_TYPE_ID\"\n" +
            "from \"Bizfns\".\"COMPANY_BUSINESS_TYPE_MAPPING\" where \"FK_COMPANY_ID\" = :compid ) \n ", nativeQuery = true)
*/
//for now its hardcoded
    @Query(value = " select \"PK_FORM_KEY_ID\" as type,\"ANSWER_TYPE\", \"FK_BUSINESS_TYPE_ID\", \"INPUT_KEY\" as question, \"OPTIONS\" as items, \"GROUP_BY\"\n" +
            "from \"Bizfns\".\"BUSINESS_TYPE_FORM_ENTITIES\" where \"FK_BUSINESS_TYPE_ID\" = 4 ", nativeQuery = true)

    List<JSONObject> getServiceEntityFields(Integer compid);


    @Query(value = " select  \"PK_FORM_KEY_ID\" as type,\"ANSWER_TYPE\", \"INPUT_KEY\" as question, \"OPTIONS\" as items\n" +
            "from \"Bizfns\".\"BUSINESS_TYPE_FORM_ENTITIES\" where \"GROUP_BY\" = 1", nativeQuery = true)
    List<JSONObject> getRowItems();

    @Query(value = " ( select \"FK_BUSINESS_TYPE_ID\"\n" +
            "from \"Bizfns\".\"COMPANY_BUSINESS_TYPE_MAPPING\" where \"FK_COMPANY_ID\" = :compId )\n" +
            "    ", nativeQuery = true)
    Integer compTypeId( Integer compId);


    @Query(value = "select \"ADMIN_EMAIL\" from  \"Bizfns\".\"ADMIN_MASTER\" where \"ADMIN_EMAIL\"= :userId or \"USER_ID\" = :userId", nativeQuery = true)
    JSONObject getAdminMailId(String userId);
}


