package com.bizfns.services.Serviceimpl;

import com.bizfns.services.GlobalDto.GlobalResponseDTO;
import com.bizfns.services.Query.*;
import com.bizfns.services.Repository.*;
import com.bizfns.services.Service.CompanyUserService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.*;


@Service
public class CompanyUserServiceimpl implements CompanyUserService {
    @Autowired
    private CompanyMasterRepository companyMasterRepository;
    @Autowired
    private CompanyUserRepository companyUserRepository;
    @Autowired
    private ErrorLogServiceImpl errorLogServiceImpl;
    @Autowired
    private AddScheduleRepository addScheduleRepository;

    @Autowired
    private ScheduleQuery scheduleQuery;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CompanyCustomerRepository companyCustomerRepository;

    @Autowired
    private EmailSenderService emailSenderService;

    @Autowired
    private StaffQuery staffQuery;

    @Autowired
    private CustQuery custQuery;

    @Autowired
    private ServiceQuery serviceQuery;

    @Autowired
    private StaffAuthQuery staffAuthQuery;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private ErrorLogServiceImpl errorLogService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private ProfileRepository profileRepository;

    private static final Logger logger = LoggerFactory.getLogger(Controller.class);

    public static boolean isEmail(String userId) {
        String emailPattern = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";

        return userId.matches(emailPattern);
    }

    public static boolean isPhoneNumber(String userId) {
        String phonePattern = "\\d{10}";

        return userId.matches(phonePattern);
    }

    /*
     * AMIT KUMAR SINGH
     * This method is used to handle OTP (One-Time Password) authentication, verifying the provided OTP
     * and returning the result of the verification process.
     * @PARAM request A map containing the OTP and other relevant information
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> otpVerification(Map<String, String> request) {
        String otp = request.get("otp");
        String userId = request.get("userId");
        String otpTimeStamp = request.get("otpTimeStamp");
        String tenantId = request.get("tenantId");


        String responseUserId = request.get("userId");
        String responseUserPhNo = null;
        String responseUserEmail = null;
        String responseUserIdType = null;
        String responseUserName = null;
        List<Map<String, Object>> responseList = null;
        // Object responseList;
        String isOtpExpire = null;
        String tokenText = null;

          List<JSONObject> dataForUserIdValidationFromCompanyMaster = companyUserRepository.dataForUserIdValidationFromCompanyMaster(userId);
        String executeQuery = null;
        String fetchTimeStamp = null;
        String fetchOtp = null;
        String userType = null;
        String userStaffType = null;


        String lastTwoTLetters = null;
        String tenantFirstEightTLetters = null;

        if (tenantId == null || tenantId.isEmpty()) {

            String tenantId1 = "adminOwn";

            if (tenantId1 != null && tenantId1.length() >= 8) {
                lastTwoTLetters = tenantId1.substring(tenantId1.length() - 2);
                tenantFirstEightTLetters = tenantId1.substring(0, 8);

            }

            fetchOtp = adminRepository.fetchOtp(userId);
            userType = companyMasterRepository.fetchUserTypeId();
            isOtpExpire = adminRepository.isOtpExpire(userId);
            tokenText = companyMasterRepository.tokenText(userId, tenantId1);
            String userTypeId = companyMasterRepository.fetchUserTypeId();
            //String securityQuestionData = companyMasterRepository.securityQuestionDate(userId);
            //String logoData = companyMasterRepository.logoData(userId);
            List<JSONObject> fetchUserDetails = companyMasterRepository.fetchUserDetails(userId, tenantId1);
            responseList = new ArrayList<>();
            Map<String, Object> userMap = new HashMap<>();

            for (JSONObject userDetails : fetchUserDetails) {

                userMap.put("COMPANY_ID", userDetails.get("COMPANY_ID"));
                userMap.put("BUSINESS_NAME", userDetails.get("BUSINESS_NAME"));
                userMap.put("COMPANY_BACKUP_EMAIL", userDetails.get("COMPANY_BACKUP_EMAIL"));
                userMap.put("COMPANY_BACKUP_PHONE_NUMBER", userDetails.get("COMPANY_BACKUP_PHONE_NUMBER"));
                //userMap.put("tenantId", userDetails.get("SCHEMA_ID"));
                userMap.put("logoAddress", userDetails.get("COMPANY_LOGO"));

                //userMap.put("isSequrityQuestionAnswered", securityQuestionData);
                userMap.put("userType", 4);
                //userMap.put("userTypeId", userTypeId);
                //userMap.put("businessLogo", logoData);
                userMap.put("token", tokenText);


                responseList.add(userMap);
            }


            if (otp.equals(fetchOtp)) {
                if (isOtpExpire.equalsIgnoreCase("y")) {


                    return ResponseEntity.accepted()
                            .body(new GlobalResponseDTO(true, "success", responseList));

                } else {
                    return ResponseEntity.accepted()
                            .body(new GlobalResponseDTO(false, "otp expire", null));
                }
            } else {
                return ResponseEntity.accepted()
                        .body(new GlobalResponseDTO(false, "otp not match", null));
            }


        }

        if (tenantId != null && tenantId.length() >= 8) {
            lastTwoTLetters = tenantId.substring(tenantId.length() - 2);
            tenantFirstEightTLetters = tenantId.substring(0, 8);

        }

        userType = companyMasterRepository.fetchUserTypeId();
        userStaffType = String.valueOf(staffQuery.userTypeStaff(userId, tenantId));

        //userStaffType = companyMasterRepository.fetchStaffTypeId();

        if (lastTwoTLetters.contains("st")) {

            fetchOtp = staffAuthQuery.fetctStaffOtp(tenantFirstEightTLetters, userId);
            isOtpExpire = staffAuthQuery.fetchStaffOtpExpire(tenantFirstEightTLetters, userId);

            tokenText = staffAuthQuery.fetchStaffToken(tenantFirstEightTLetters, userId);

            userType = companyMasterRepository.fetchStaffTypeId();
            String userTypeId = companyMasterRepository.fetchStaffTypeId();

            String logoData = companyMasterRepository.staffLogoData(tenantFirstEightTLetters);

            List<JSONObject> staffCompData = companyMasterRepository.staffCompData(tenantFirstEightTLetters);

            responseList = new ArrayList<>();
            Map<String, Object> staffMap = new HashMap<>();
            Integer COMPANY_ID = null;
            String BUSINESS_NAME = null;
            String logoAddress = null;

            for (JSONObject staffCompDetails : staffCompData) {


                COMPANY_ID = (Integer) staffCompDetails.get("COMPANY_ID");
                BUSINESS_NAME = (String) staffCompDetails.get("BUSINESS_NAME");
                logoAddress = (String) staffCompDetails.get("COMPANY_LOGO");


            }

            List<Map<String, Object>> staffData = staffAuthQuery.staffData(tenantFirstEightTLetters, userId);

            for (Map<String, Object> row : staffData) {
                staffMap.put("COMPANY_BACKUP_EMAIL", (String) row.get("USER_EMAIL"));
                staffMap.put("COMPANY_BACKUP_PHONE_NUMBER", (String) row.get("USER_PHONE_NUMBER"));
                staffMap.put("token", tokenText);
                staffMap.put("tenantId", tenantId);
                staffMap.put("businessLogo", logoData);
                staffMap.put("isSequrityQuestionAnswered", (String) row.get("PASSWORD_CHANGE"));
                staffMap.put("userType", userType);
                staffMap.put("userTypeId", userTypeId);


                staffMap.put("COMPANY_ID", COMPANY_ID);
                staffMap.put("BUSINESS_NAME", BUSINESS_NAME);
                staffMap.put("logoAddress", logoAddress);


                responseList.add(staffMap);

            }


            if (otp.equals(fetchOtp)) {
                if (isOtpExpire.equalsIgnoreCase("y")) {


                    return ResponseEntity.accepted()
                            .body(new GlobalResponseDTO(true, "success", responseList));

                } else {
                    return ResponseEntity.accepted()
                            .body(new GlobalResponseDTO(false, "otp expire", null));
                }
            } else {
                return ResponseEntity.accepted()
                        .body(new GlobalResponseDTO(false, "otp not match", null));
            }

        } else if (userStaffType != null && userStaffType.equals("2")) {
            fetchOtp = staffQuery.fetchOtpForStaff(userId, tenantId);
            isOtpExpire = staffQuery.isOtpExpireForStaff(userId, Integer.parseInt(userStaffType), tenantId);
            tokenText = staffQuery.tokenTextForStaff(userId, tenantId);
            Map<String, Object> firstRow = staffQuery.fetchStaffUserData(userId, tenantId);

            int companyId = (Integer) firstRow.get("COMPANY_ID");
            String businessName = (String) firstRow.get("BUSINESS_NAME");
            String COMPANY_BACKUP_EMAIL = (String) firstRow.get("COMPANY_BACKUP_EMAIL");
            String COMPANY_BACKUP_PHONE_NUMBER = (String) firstRow.get("COMPANY_BACKUP_PHONE_NUMBER");
            String SCHEMA_ID = (String) firstRow.get("SCHEMA_ID");
            String COMPANY_LOGO = (String) firstRow.get("COMPANY_LOGO");

            List<Map<String, Object>> staffData = staffAuthQuery.staffData(tenantFirstEightTLetters, userId);

           String userType1 = companyMasterRepository.fetchStaffTypeId();
            String userTypeId = companyMasterRepository.fetchStaffTypeId();
            List<Map<String, Object>> responseList1 = new ArrayList<>();

            for (Map<String, Object> row : staffData) {
                Map<String, Object> staffMap = new HashMap<>();
                staffMap.put("COMPANY_BACKUP_EMAIL", (String) row.get("USER_EMAIL"));
                staffMap.put("STAFF_BACKUP_PHONE_NUMBER", (String) row.get("USER_PHONE_NUMBER"));
                staffMap.put("token", tokenText);
                staffMap.put("tenantId", tenantId);
                staffMap.put("businessLogo", "Y");
                staffMap.put("isSequrityQuestionAnswered", (String) row.get("PASSWORD_CHANGE"));
                staffMap.put("userType", userType1);
                staffMap.put("COMPANY_BACKUP_PHONE_NUMBER",COMPANY_BACKUP_PHONE_NUMBER);
                staffMap.put("userTypeId",userTypeId);
                staffMap.put("logoAddress",COMPANY_LOGO);
                staffMap.put("COMPANY_ID", companyId);
                staffMap.put("BUSINESS_NAME", businessName);
                responseList1.add(staffMap);
            }

            if (otp.equals(fetchOtp)) {
                if (isOtpExpire.equalsIgnoreCase("y")) {
                    return ResponseEntity.accepted()
                            .body(new GlobalResponseDTO(true, "success", responseList1));
                } else {
                    return ResponseEntity.accepted()
                            .body(new GlobalResponseDTO(false, "otp expire", null));
                }
            } else {
                return ResponseEntity.accepted()
                        .body(new GlobalResponseDTO(false, "otp not match", null));
            }


        } else {
            fetchOtp = companyUserRepository.fetchOtp(userId, tenantId);
            userType = companyMasterRepository.fetchUserTypeId();
            isOtpExpire = companyUserRepository.isOtpExpire(userId, tenantId);
            tokenText = companyMasterRepository.tokenText(userId, tenantId);
            String userTypeId = companyMasterRepository.fetchUserTypeId();
            String securityQuestionData = companyMasterRepository.securityQuestionDate(userId, tenantId);
            String logoData = companyMasterRepository.logoData(userId, tenantId);
            List<JSONObject> fetchUserDetails = companyMasterRepository.fetchUserDetails(userId, tenantId);
            responseList = new ArrayList<>();
            Map<String, Object> userMap = new HashMap<>();

            for (JSONObject userDetails : fetchUserDetails) {

                userMap.put("COMPANY_ID", userDetails.get("COMPANY_ID"));
                userMap.put("BUSINESS_NAME", userDetails.get("BUSINESS_NAME"));
                userMap.put("COMPANY_BACKUP_EMAIL", userDetails.get("COMPANY_BACKUP_EMAIL"));
                userMap.put("COMPANY_BACKUP_PHONE_NUMBER", userDetails.get("COMPANY_BACKUP_PHONE_NUMBER"));
                userMap.put("tenantId", userDetails.get("SCHEMA_ID"));
                userMap.put("logoAddress", userDetails.get("COMPANY_LOGO"));

                userMap.put("isSequrityQuestionAnswered", securityQuestionData);
                userMap.put("userType", userType);
                userMap.put("userTypeId", userTypeId);
                userMap.put("businessLogo", logoData);
                userMap.put("token", tokenText);


                responseList.add(userMap);
            }


            if (otp.equals(fetchOtp)) {
                if (isOtpExpire.equalsIgnoreCase("y")) {


                    return ResponseEntity.accepted()
                            .body(new GlobalResponseDTO(true, "success", responseList));

                } else {
                    return ResponseEntity.accepted()
                            .body(new GlobalResponseDTO(false, "otp expire", null));
                }
            } else {
                return ResponseEntity.accepted()
                        .body(new GlobalResponseDTO(false, "otp not match", null));
            }


        }


    }

    /*
     * AMIT KUMAR SINGH
     * This method is used to add a new customer to the system. It handles extracting
     * customer details from the request, checking for existing records to prevent
     * duplicates, encrypting the password, and inserting the customer data into the
     * database. Additionally, it manages the insertion of associated service entity
     * questions and prepares the response containing the customer details.
     * @PARAM request A map containing customer details and related information
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> addCustomer(Map<String, Object> request) {


        String tenantId = (String) request.get("tenantId");
        String companyId = (String) request.get("companyId");
        String custFName = (String) request.get("custFName");
        String custLName = (String) request.get("custLName");
        String custEmail = (String) request.get("custEmail");
        String custPhNo = (String) request.get("custPhNo");
        String custCompanyName = (String) request.get("custCompanyName");
        String custAddress = (String) request.get("custAddress");
        List<Map<String, Object>> questionData = (List<Map<String, Object>>) request.get("questionData");
        String custStatus = "1";
        String lastOtp = "1234";
        String isOtpVerified = "1";
        String password = "123456";
        String responseTenantId = tenantId + "cu";
        AES obj = new AES();

        String encryptedPassword = obj.encrypt(password);

        CheckSchema schemaService = new CheckSchema(jdbcTemplate);

        boolean schemaExists = schemaService.doesSchemaExist(tenantId);
        if (!schemaExists) {
            return ResponseEntity.accepted()
                    .body(new GlobalResponseDTO(false, "invalid Business Id ", null));

        }

        String queryCustomerEmail = "SELECT \"CUSTOMER_EMAIL\" FROM \"" + tenantId + "\".\"company_customer\" WHERE \"CUSTOMER_EMAIL\" = ?";
        List<Map<String, Object>> recordForEmail = jdbcTemplate.queryForList(queryCustomerEmail, custEmail);

        String queryCustomerPhNo = "SELECT \"CUSTOMER_PHONE_NUMBER\" FROM \"" + tenantId + "\".\"company_customer\" WHERE \"CUSTOMER_PHONE_NUMBER\" = ?";
        List<Map<String, Object>> recordForPhNo = jdbcTemplate.queryForList(queryCustomerPhNo, custPhNo);


//        if (!recordForEmail.isEmpty()) {
//            return ResponseEntity.accepted()
//                    .body(new GlobalResponseDTO(false, "Email already exist", null));
//        }
       // else
            if (!recordForPhNo.isEmpty()) {
            return ResponseEntity.accepted()
                    .body(new GlobalResponseDTO(false, "Ph No already exist", null));
        }


        String custId = custQuery.addCustomerQuery(tenantId, companyId, custFName,
                custLName, custEmail, encryptedPassword, lastOtp,
                custPhNo, custStatus, isOtpVerified,custCompanyName, custAddress);

    /*    String queryForCustomerInsertion = "INSERT INTO \"" + tenantId + "\".\"company_customer\"   \n" +
                "(\"PK_CUSTOMER_ID\", \"FK_COMPANY_ID\", \"CUSTOMER_FIRST_NAME\", \"CUSTOMER_LAST_NAME\", \"CUSTOMER_EMAIL\", \"CUSTOMER_PASSWORD\", \n" +
                "\"LAST_OTP\", \"COMPANY_CREATED_AT\", \"COMPANY_UPDATED_AT\", \"CUSTOMER_PHONE_NUMBER\", \"CUSTOMER_STATUS\", \"IS_OTP_VERIFIED\")\n" +
                "VALUES(\n" +
                "(SELECT COALESCE((SELECT MAX(\"PK_CUSTOMER_ID\") FROM    \"" + tenantId + "\".\"company_customer\"  ), 0   ) + 1),\n" +
                "" + companyId + ",\n" +
                "'" + custFName + "', \n" +
                "'" + custLName + "',\n" +
                "'" + custEmail + "', \n" +
                "'" + encryptedPassword + "', \n" +
                "'" + lastOtp + "', \n" +
                "current_timestamp, \n" +
                "current_timestamp,\n" +
                "'" + custPhNo + "', \n" +
                "'" + custStatus + "', \n" +
                "'" + isOtpVerified + "') RETURNING \"PK_CUSTOMER_ID\"";

        Long insertedCustomerId = jdbcTemplate.queryForObject(queryForCustomerInsertion, Long.class);*/

        //  List<Map<String, Object>> questionData =(List<Map<String, Object>>)request.get("questionData");
        String NameServiceEntity = null;
        for (Map<String, Object> staff : questionData) {


            String question_name = (String) staff.get("question_name");
            String answer = (String) staff.get("answer");

            if (question_name.equalsIgnoreCase("name")) {
                NameServiceEntity = answer;

            }

            //  scheduleQuery.addServiceEntityQuestionsList(tenantId, Integer.valueOf(custId), Integer.valueOf(questionId), question_name, answer, Integer.valueOf(answer_type_id), pkServiceEntityId);

        }
        Integer pkServiceEntityId = scheduleQuery.insertCustWiseServiceEntity(tenantId, custId, 0, NameServiceEntity);


        for (Map<String, Object> staff : questionData) {

            String questionId = (String) staff.get("question_id");
            String question_name = (String) staff.get("question_name");
            String answer = (String) staff.get("answer");
            String answer_type_id = (String) staff.get("answer_type_id");
            /*if ("Phone Number".equalsIgnoreCase(question_name)) {
                String mobileNumber = (String) staff.get("answer");

                if (!mobileNumber.isEmpty() && mobileNumber.length() < 10) {
                    return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Mobile number must be at least 10 digits long.", null));
                }
            }*/
            scheduleQuery.addServiceEntityQuestionsList(tenantId, Integer.valueOf(custId), Integer.valueOf(questionId), question_name, answer, Integer.valueOf(answer_type_id), pkServiceEntityId);


        }


        String mailSubject = "Successfully register in Bizfns";
        String mailBody = "You have successfully registered in Bizfns,\n" +
                "your tenant id is " + responseTenantId + "\n"
                + "app download link is: http://182.156.196.67:8085/api/users/appLink"
                + "\nUserId - " + custPhNo;

        Map<String, Object> response = new HashMap<>();
        response.put("tenantId", tenantId);
        response.put("customerFirstName", custFName);
        response.put("customerLastName", custLName);
        response.put("customerEmail", custEmail);
        response.put("customerMobile", custPhNo);
        response.put("companyId", companyId);
        response.put("customerId", custId);
        response.put("serviceEntityId", String.valueOf(pkServiceEntityId));
        response.put("address", custAddress);

        return ResponseEntity.accepted()
                .body(new GlobalResponseDTO(true, "inserted Succseefully", response));

    }


    /*
     * AMIT KUMAR SINGH
     * This method is used to add a new customer service entity and associated questions.
     * It processes the request to extract customer and service entity details, inserts
     * the service entity, and saves the related questions.
     * @PARAM request A map containing customer ID, tenant ID, and question data
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> addCustomerAndService(Map<String, Object> request) {

        String tenantId = (String) request.get("tenantId");
        String custId = (String) request.get("customer_id");

        List<Map<String, Object>> questionData = (List<Map<String, Object>>) request.get("questionData");
        String NameServiceEntity = null;
        for (Map<String, Object> staff : questionData) {


            String question_name = (String) staff.get("question_name");
            String answer = (String) staff.get("answer");

            if (question_name.equalsIgnoreCase("name")) {
                NameServiceEntity = answer;
            }
        }
        Integer pkServiceEntityId = scheduleQuery.insertCustWiseServiceEntity(tenantId, custId, 0, NameServiceEntity);


        for (Map<String, Object> staff : questionData) {

            String questionId = (String) staff.get("question_id");
            String question_name = (String) staff.get("question_name");
            String answer = (String) staff.get("answer");
            String answer_type_id = (String) staff.get("answer_type_id");

            scheduleQuery.addServiceEntityQuestionsList(tenantId, Integer.valueOf(custId), Integer.valueOf(questionId), question_name, answer, Integer.valueOf(answer_type_id), pkServiceEntityId);
        }
        return ResponseEntity.accepted().body(new GlobalResponseDTO(true, "Successfully added", null));
    }

    @Override
    public ResponseEntity<GlobalResponseDTO> getCustomerDetails(Map<String, Object> request, Principal principal) {
        try {
            String tenantId = (String) request.get("tenantId");
            String userId = (String) request.get("userId");
            String customerId = (String) request.get("customerId");
            if (checkUserMatch(userId, tenantId, principal.getName())) {
                return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorized user, we could not access the APIs from others token "));
            }
            List<Map<String, Object>> fetchCustomerDetails = custQuery.fetchCustomerDetailsFromDB(tenantId, Integer.valueOf(customerId));
            if (fetchCustomerDetails == null || fetchCustomerDetails.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new GlobalResponseDTO(false, "Customer data for customer Id :- " + customerId + " not found"));
            }
            Map<String, Object> customerData = fetchCustomerDetails.get(0);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("customerId", customerData.get("PK_CUSTOMER_ID").toString());
            responseData.put("FirstName", customerData.get("CUSTOMER_FIRST_NAME"));
            responseData.put("LastName", customerData.get("CUSTOMER_LAST_NAME"));
            responseData.put("Email", customerData.get("CUSTOMER_EMAIL"));
            responseData.put("customerPhone", customerData.get("CUSTOMER_PHONE_NUMBER"));
            responseData.put("address", customerData.get("customer_address"));
            responseData.put("companyName", customerData.get("customer_company_name"));
            responseData.put("activeStatus", customerData.get("CUSTOMER_STATUS"));

            return ResponseEntity.ok()
                    .body(new GlobalResponseDTO(true, "Customer details retrieved successfully", responseData));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GlobalResponseDTO(false, "Failed to retrieve customer details: " + e.getMessage()));
        }
    }

    public ResponseEntity<GlobalResponseDTO> updateCustomerDetails(Map<String, Object> request, Principal principal) {
        try {
            String tenantId = (String) request.get("tenantId");
            String userId = (String) request.get("userId");
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            for (GrantedAuthority authority : authorities) {
                String authorityName = authority.getAuthority();
                List<String> priviledgeChk = custQuery.priviledgeChkForCustomer(tenantId);
                boolean hasEditPrivilege = false;
                for (String privilege : priviledgeChk) {
                    if (privilege.equalsIgnoreCase("EDIT")) {
                        hasEditPrivilege = true;
                        break;
                    }
                }
                if (authorityName.equalsIgnoreCase("Staff") && !hasEditPrivilege) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new GlobalResponseDTO(false, "A Staff user dont have the priviledge to edit the customer details.", null));
                }
            }
            if (checkUserMatch(userId, tenantId, principal.getName())) {
                return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorized user, we could not access the APIs from others token"));
            }
            boolean isUpdated = custQuery.updateCustomerData(tenantId, request);
            if (isUpdated) {
                return ResponseEntity.accepted().body(new GlobalResponseDTO(true, "Customer details updated successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GlobalResponseDTO(false, "Failed to update customer details"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GlobalResponseDTO(false, "Failed to update customer details: " + e.getMessage()));
        }
    }

    public ResponseEntity<GlobalResponseDTO> deleteCustFromDB(Map<String, Object> request, Principal principal) {
        try {
            String tenantId = (String) request.get("tenantId");
            String userId = (String) request.get("userId");
            String customerId = (String) request.get("customerId");
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            for (GrantedAuthority authority : authorities) {
                String authorityName = authority.getAuthority();
                List<String> priviledgeChk = custQuery.priviledgeChkForCustomer(tenantId);
                boolean hasEditPrivilege = false;
                for (String privilege : priviledgeChk) {
                    if (privilege.equalsIgnoreCase("DELETE")) {
                        hasEditPrivilege = true;
                        break;
                    }
                }
                if (authorityName.equalsIgnoreCase("Staff") && !hasEditPrivilege) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new GlobalResponseDTO(false, "A Staff user dont have the priviledge to delete the customer data.", null));
                }
            }
            if (checkUserMatch(userId, tenantId, principal.getName())) {
                return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorized user, we could not access the APIs from others token"));
            }
            boolean isDeleted = custQuery.deleteCustomerByCustomerId(tenantId, Integer.parseInt(customerId));

            if (isDeleted) {
                return ResponseEntity.ok(new GlobalResponseDTO(true, "Customer has been deleted successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GlobalResponseDTO(false, "Failed to delete customer"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GlobalResponseDTO(false, "Failed to delete customer: " + e.getMessage()));
        }
    }

    public ResponseEntity<GlobalResponseDTO> updateServiceObjectDetails(Map<String, Object> request, Principal principal) {
        String tenantId = (String) request.get("tenantId");
        String custId = (String) request.get("customer_id");
        String serviceEntityId = (String) request.get("service_Entity_Id");
        List<Map<String, Object>> questionData = (List<Map<String, Object>>) request.get("service_entity_items");
        try {
            for (Map<String, Object> staff : questionData) {
                String questionId = (String) staff.get("question_id");
                String answer = (String) staff.get("answer");
                String answerTypeId = (String) staff.get("answer_type_id");
                scheduleQuery.updateServiceEntityQuestionsList(
                        tenantId,
                        Integer.valueOf(custId),
                        Integer.valueOf(questionId),
                        Integer.valueOf(serviceEntityId),
                        answer,
                        Integer.valueOf(answerTypeId)
                );
            }
            return ResponseEntity.accepted().body(new GlobalResponseDTO(true, "Service object details updated successfully", null));
        } catch (Exception e) {
            System.err.println("Exception occurred while updating service object details: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GlobalResponseDTO(false, "Failed to update service object details", e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<GlobalResponseDTO> getActiveInactiveStatusForCustomer(Map<String, Object> request, Principal principal) {
        String tenantId = (String) request.get("tenantId");
        String customerId = (String) request.get("customerId");
        if (tenantId == null || customerId == null) {
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Tenant ID or customer ID is missing"));
        }
        try {
            String query = "SELECT \"PK_CUSTOMER_ID\", \"CUSTOMER_STATUS\" FROM \"" + tenantId + "\".\"company_customer\" WHERE \"PK_CUSTOMER_ID\" = ?";
            Map<String, Object> customerStatus = jdbcTemplate.queryForMap(query, Integer.parseInt(customerId));
            String status = (String) customerStatus.get("CUSTOMER_STATUS");
            Map<String, Object> response = new HashMap<>();
            response.put("customerId", customerStatus.get("PK_CUSTOMER_ID"));
            response.put("activeStatus :- ", status);
            return ResponseEntity.ok(new GlobalResponseDTO(true, "Success", response));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new GlobalResponseDTO(false, "Failed to retrieve customer status: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<GlobalResponseDTO> UpdateActiveInactiveStatusForCustomer(Map<String, Object> request, Principal principal) {
        String tenantId = (String) request.get("tenantId");
        String customerId = (String) request.get("customerId");
        String status = (String) request.get("status");
        if (tenantId == null || customerId == null || status == null) {
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Tenant ID, customer ID, or status is missing"));
        }
        try {
            String updateStatusQuery = "UPDATE \"" + tenantId + "\".\"company_customer\" " +
                    "SET \"CUSTOMER_STATUS\" = ? " +
                    "WHERE \"PK_CUSTOMER_ID\" = ?";
            int rowsAffected = jdbcTemplate.update(updateStatusQuery, status, Integer.parseInt(customerId));
            if (rowsAffected > 0) {
                String statusMessage = status.equals("1") ? "activated" : "deactivated";
                return ResponseEntity.ok(new GlobalResponseDTO(true, "Customer has been " + statusMessage + " successfully"));
            } else {
                return ResponseEntity.status(404).body(new GlobalResponseDTO(false, "Customer update failed"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new GlobalResponseDTO(false, "Error occurred while updating customer status: " + e.getMessage()));
        }
    }


    /*
     * AMIT KUMAR SINGH
     * This method is used to fetch pre-registration data including subscription plans,
     * business types, and terms and conditions. It gathers the necessary data from
     * various repository methods, formats it into JSON objects, and returns the
     * structured response.
     * @PARAM None
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> fetchPreRegistration() {
        List<JSONObject> fetchPreRegistrationDataForCompanySubscription = companyMasterRepository.fetchPreRegistrationDataForCompanySubscription();

        List<JSONObject> fetchPreRegistrationDataForBusinessTypeMaster = companyMasterRepository.fetchPreRegistrationDataForBusinessTypeMaster();
        String fetchPreRegistrationDataForTAndC = companyMasterRepository.fetchPreRegistrationDataForTAndC();

        JSONObject responseData = new JSONObject();
        JSONArray planList = new JSONArray();
        JSONArray businessCategory = new JSONArray();

        for (JSONObject result : fetchPreRegistrationDataForCompanySubscription) {
            JSONObject plan = new JSONObject();

            plan.put("planId", result.get("PK_SUBSCRIPTION_PLAN_ID"));
            plan.put("planName", result.get("SUBSCRIPTION_ENTITY"));
            plan.put("userCount", result.get("SUBSCRIPTION_USER_LIMIT"));
            plan.put("description", result.get("DESCRIPTION"));
            plan.put("price", result.get("SUBSCRIPTION_ENTITY_PRICE"));


            planList.add(plan);
        }

        JSONObject businessType = new JSONObject();
        JSONArray businessTypeList = new JSONArray();


        businessType.put("business_type", businessTypeList);

        businessCategory.add(businessType);


        List<JSONObject> businessCategoryAndType = companyMasterRepository.businessCategoryAndType();

        responseData.put("planList", planList);
        responseData.put("businessCategoryAndType", businessCategoryAndType);
        responseData.put("termsAndCondition", fetchPreRegistrationDataForTAndC);


        return ResponseEntity.accepted()
                .body(new GlobalResponseDTO(true, "Success", responseData));
    }


    public boolean checkFileExists(String filePath) {
        Path path = Paths.get(filePath);
        return Files.exists(path);
    }


    @Override
    public ResponseEntity<GlobalResponseDTO> preCreationInfo(Map<String, String> request) {

        String deviceId = request.get("deviceId");
        String deviceType = request.get("deviceType");
        String appVersion = request.get("appVersion");


        return ResponseEntity.accepted()
                .body(new GlobalResponseDTO(true, "", "password"));
    }


    /*
     * AMIT KUMAR SINGH
     * This method is used to handle the process of changing a user's password by sending an OTP for verification.
     * It verifies the old password, generates a new OTP, and sends the OTP to the user if the password is correct.
     * @PARAM request A map containing user ID, old password, and tenant ID
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> changePasswordSendOtp(Map<String, String> request) {

        String userId = request.get("userId");
        String oldPassword = request.get("oldPassword");
        String tenantId = request.get("tenantId");

        AES obj = new AES();

        String convertedUserId = userId.substring(0, 2) + userId.substring(2, userId.length() - 2).replaceAll(".", "*") + userId.substring(userId.length() - 2);
        String convertedUserIdMessage = "We have sent you verification code on" + " " + convertedUserId;

        String otpTimeStamp = null;

        String decryptedDbPassword = null;

        String executeQuery = null;
        String fetchTimeStamp = null;
        String fetchOtp = null;


        String lastTwoTLetters = null;
        String tenantFirstEightTLetters = null;


        if (tenantId != null && tenantId.length() >= 8) {
            lastTwoTLetters = tenantId.substring(tenantId.length() - 2);
            tenantFirstEightTLetters = tenantId.substring(0, 8);

        }

        if (lastTwoTLetters.equalsIgnoreCase("st")) {

            String staffPassword = staffAuthQuery.staffDbPassword(userId, tenantFirstEightTLetters);


            decryptedDbPassword = obj.decrypt(staffPassword);

            if (decryptedDbPassword.equals(oldPassword)) {
                Random random = new Random();
                int randomNumber = random.nextInt(900000) + 100000;

                String strRandomNo = String.valueOf(randomNumber);


                String staffOtpExistence = staffQuery.staffOtpExistence(tenantFirstEightTLetters, userId);
                if (StringUtils.isEmpty(staffOtpExistence)) {

                    staffQuery.insertFirstStaffOtp(tenantFirstEightTLetters, userId, strRandomNo);

                } else {
                    staffQuery.updateStaffOtp(tenantFirstEightTLetters, userId, strRandomNo);

                }


                Map<String, String> otpData = new HashMap<>();
                otpData.put("OTP", String.valueOf(randomNumber));
                otpData.put("otpTimeStamp", otpTimeStamp);
                otpData.put("message", convertedUserIdMessage);

                return ResponseEntity.accepted()
                        .body(new GlobalResponseDTO(true, "OTP", otpData));

            }


            return ResponseEntity.accepted()
                    .body(new GlobalResponseDTO(false, "You have entered a wrong password", null));


        } else {
            List<JSONObject> dataForUserIdValidationFromCompanyMaster = companyUserRepository.dataForUserIdValidationFromCompanyMaster(userId);

            if (dataForUserIdValidationFromCompanyMaster.isEmpty()) {
                return ResponseEntity.accepted()
                        .body(new GlobalResponseDTO(false, "no user found", null));
            }

            if (!dataForUserIdValidationFromCompanyMaster.isEmpty()) {

                String checkOldPassword = companyUserRepository.checkOldPasswordCompMast(userId,tenantId);

                decryptedDbPassword = obj.decrypt(checkOldPassword);
                executeQuery = "companyMaster";


            }


            if (decryptedDbPassword.equals(oldPassword)) {
                Random random = new Random();
                int randomNumber = random.nextInt(900000) + 100000;


                JSONObject getMailId = companyMasterRepository.getMailId(userId, tenantId);

                if (getMailId != null) {
                    // The JSONObject is not null, so proceed with sending the email.
                    String otpMailSub = "Bizfns";
                    String otpMailBody = "Your one-time password is: " + randomNumber;

                    String companyBackupEmail = (String) getMailId.get("COMPANY_BACKUP_EMAIL");
                    if (companyBackupEmail != null) {
                        // emailSenderService.sendSimpleEmail(companyBackupEmail, otpMailSub, otpMailBody);
                    } else {
                        // Handle the case when "COMPANY_BACKUP_EMAIL" is not found in the JSONObject
                    }
                } else {
                    // Handle the case when getMailId is null
                }


                if (executeQuery.equalsIgnoreCase("companyMaster")) {


                    List<JSONObject> checkOtpExistence = companyMasterRepository.checkOtpExistence(userId, tenantId);
                    if (checkOtpExistence.isEmpty()) {
                        otpTimeStamp = companyMasterRepository.insertFirstLoginOtp(String.valueOf(randomNumber), userId, tenantId);
                    } else {
                        otpTimeStamp = companyMasterRepository.updateLoginOtp(String.valueOf(randomNumber), userId,tenantId);
                    }

                }

                Map<String, String> otpData = new HashMap<>();
                otpData.put("OTP", String.valueOf(randomNumber));
                otpData.put("otpTimeStamp", otpTimeStamp);
                otpData.put("message", convertedUserIdMessage);

                return ResponseEntity.accepted()
                        .body(new GlobalResponseDTO(true, "OTP", otpData));

            }
            return ResponseEntity.accepted()
                    .body(new GlobalResponseDTO(false, "You have entered a wrong password", null));


        }


    }


    /*
     * AMIT KUMAR SINGH
     * This method is used to validate the OTP and change the user's password.
     * It checks the OTP's validity, expiry, and matches it before updating the password.
     * @PARAM request A map containing user ID, OTP, new password, and tenant ID
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> validateOtpAndChangePassword(Map<String, String> request, String userType) {
        String userId = request.get("userId");
        String otp = request.get("otp");
        String newPassword = request.get("newPassword");
        String tenantId = request.get("tenantId");
        String isOtpExpire = null;


        AES obj = new AES();


        String checkOtpExistence = null;
        String extractTimeStamp = null;
        String executeQuery = null;


        String lastTwoTLetters = null;
        String tenantFirstEightTLetters = null;




        if (tenantId.isEmpty()){
            if (userType!=null && userType.equalsIgnoreCase("4")){

                if (tenantId != null && tenantId.length() >= 8) {
                    lastTwoTLetters = tenantId.substring(tenantId.length() - 2);
                    tenantFirstEightTLetters = tenantId.substring(0, 8);

                }

                String otpTimeStamp = request.get("otpTimeStamp");

                List<JSONObject> dataForUserIdValidationFromCompanyMaster = companyUserRepository.dataForUserIdValidationFromCompanyMaster(userId);
                if (dataForUserIdValidationFromCompanyMaster.isEmpty()) {
                    return ResponseEntity.accepted()
                            .body(new GlobalResponseDTO(false, "no user found", null));
                }

                if (!dataForUserIdValidationFromCompanyMaster.isEmpty()) {
                    checkOtpExistence = adminRepository.checkOtpExistenceForPassword(userId);
                    isOtpExpire = adminRepository.isOtpExpire(userId);
                    executeQuery = "companyMaster";

                }

                if (isOtpExpire.equalsIgnoreCase("y")) {

                    if (checkOtpExistence.equals(otp)) {
                        if (executeQuery.equalsIgnoreCase("companyMaster")) {

                            companyMasterRepository.updatePassword(userId, obj.encrypt(newPassword),tenantId);
                            adminRepository.updatePasswordAdmin(userId, obj.encrypt(newPassword));

                        }
                        return ResponseEntity.accepted()
                                .body(new GlobalResponseDTO(true, "Successfully Updated", null));
                    } else {
                        return ResponseEntity.accepted()
                                .body(new GlobalResponseDTO(false, "OTP not Matched", null));

                    }

                } else {
                    return ResponseEntity.accepted()
                            .body(new GlobalResponseDTO(false, "OTP Expire", null));
                }


            }

        }

        if (tenantId != null && tenantId.length() >= 8) {
            lastTwoTLetters = tenantId.substring(tenantId.length() - 2);
            tenantFirstEightTLetters = tenantId.substring(0, 8);

        }

        if (lastTwoTLetters.equalsIgnoreCase("st")) {

            checkOtpExistence = staffAuthQuery.fetctStaffOtp(tenantFirstEightTLetters, userId);

            isOtpExpire = staffAuthQuery.fetchStaffOtpExpire(tenantFirstEightTLetters, userId);

            if (isOtpExpire.equalsIgnoreCase("y")) {

                if (checkOtpExistence.equals(otp)) {

                    staffAuthQuery.changeSataffPassord(tenantFirstEightTLetters, obj.encrypt(newPassword), userId);

                }

                return ResponseEntity.accepted()
                        .body(new GlobalResponseDTO(true, "Successfully Updated", null));
            }

            return ResponseEntity.accepted()
                    .body(new GlobalResponseDTO(false, "OTP Expire", null));
        } else {
            String otpTimeStamp = request.get("otpTimeStamp");

            List<JSONObject> dataForUserIdValidationFromCompanyMaster = companyUserRepository.dataUserIdValidationFromCompanyMaster(userId,tenantId);
            if (dataForUserIdValidationFromCompanyMaster.isEmpty()) {
                return ResponseEntity.accepted()
                        .body(new GlobalResponseDTO(false, "no user found", null));
            }

            if (!dataForUserIdValidationFromCompanyMaster.isEmpty()) {
                checkOtpExistence = companyUserRepository.checkOtpExistence(userId,tenantId);
                isOtpExpire = companyUserRepository.isOtpExpire(userId,tenantId);
                executeQuery = "companyMaster";

            }

            if (isOtpExpire.equalsIgnoreCase("y")) {

                if (checkOtpExistence.equals(otp)) {
                    if (executeQuery.equalsIgnoreCase("companyMaster")) {

                        companyMasterRepository.updatePassword(userId, obj.encrypt(newPassword),tenantId);

                    }
                    return ResponseEntity.accepted()
                            .body(new GlobalResponseDTO(true, "Successfully Updated", null));
                } else {
                    return ResponseEntity.accepted()
                            .body(new GlobalResponseDTO(false, "OTP not Matched", null));

                }

            } else {
                return ResponseEntity.accepted()
                        .body(new GlobalResponseDTO(false, "OTP Expire", null));
            }
        }
    }

    /*
     * AMIT KUMAR SINGH
     * This method is used to fetch details required for pre-staff creation, including
     * staff type details and wage frequency details, based on the specified tenant ID.
     * It verifies the existence of the tenant ID in the database and returns the
     * appropriate response.
     * @PARAM request A map containing the tenant ID
     */

    @Override
    public ResponseEntity<GlobalResponseDTO> fetchPreStaffCreationDetails(Map<String, String> request) {
        String tenantId = "Bizfns";
        String query = "SELECT EXISTS(SELECT 1 FROM information_schema.schemata WHERE schema_name = ?)";
        Boolean tenantIdExist = jdbcTemplate.queryForObject(query, Boolean.class, tenantId);


        if (tenantIdExist) {
            String queryFetchingStaffTypeDetails = "SELECT \"PK_TYPE_ID\", \"TYPE_NAME\" FROM \"" + tenantId + "\".\"STAFF_TYPE_MASTER\"";
            String queryFetchingWageFrequencyDetails = " SELECT \"PK_FREQUENCY_ID\", \"FREQUENCY_TYPE\" FROM \"" + tenantId + "\".\"FREQUENCY_MASTER\"";

            List<Map<String, Object>> staffTypeDetailsList = jdbcTemplate.queryForList(queryFetchingStaffTypeDetails);

            List<Map<String, Object>> wageFrequencyDetailsList = jdbcTemplate.queryForList(queryFetchingWageFrequencyDetails);

            Map<String, Object> responseMap = new HashMap<>();

            responseMap.put("staffTypeDetailsList", staffTypeDetailsList);

            responseMap.put("wageFrequencyDetailsList", wageFrequencyDetailsList);

            return ResponseEntity.accepted()
                    .body(new GlobalResponseDTO(true, "Success", responseMap));
        } else {

            return ResponseEntity.accepted()
                    .body(new GlobalResponseDTO(true, "The given tenantId does not exist..", null));
        }


    }

    /*
     * AMIT KUMAR SINGH
     * This method fetches a list of staff members based on the provided parameters.
     * It validates the user against the specified tenant ID and user ID, ensuring
     * authorized access to the API. If the user is authorized, it retrieves the staff
     * list; otherwise, it checks for associated user details in the database and
     * retrieves the staff list accordingly.
     * @PARAM request A map containing device ID, device type, app version, user ID, and tenant ID
     * @PARAM principal The principal object representing the current logged-in user
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> fetchStaffList(Map<String, String> request, Principal principal) {

        String deviceId = request.get("deviceId");
        String deviceType = request.get("deviceType");
        String appVersion = request.get("appVersion");
        String userId = request.get("userId");
        String tenantId = request.get("tenantId");

        if(checkUserMatch(userId,tenantId, principal.getName())){
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorised user, we could not access the APIs from others token "));
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            String authorityName = authority.getAuthority();
            List<String> priviledgeChk = staffQuery.priviledgeChkForStaff(tenantId);
            boolean hasEditPrivilege = false;
            for (String privilege : priviledgeChk) {
                if (privilege.equalsIgnoreCase("VIEW")) {
                    hasEditPrivilege = true;
                    break;
                }
            }
            if (authorityName.equalsIgnoreCase("Staff") && !hasEditPrivilege) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new GlobalResponseDTO(false, "A Staff user dont have the priviledge to fetch the staff details.", null));
            }
        }

//        if(checkUserMatch(userId,tenantId, principal.getName())){
//            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorised user, we could not access the APIs from others token "));
//        }
//
//        System.out.println("userName : "+principal.getName());
//        if (isNullOrEmpty(tenantId) || isNullOrEmpty(userId)){
//            return ResponseEntity.ok().body(new GlobalResponseDTO(false, "Mandatory Fields should not be null"));
//
//        }

        List<JSONObject> userWithTenantValidation = companyCustomerRepository.userWithTenantValidation(tenantId, userId);

        if (userWithTenantValidation.isEmpty()){

            Map<String, Object> userIdAndComId = profileRepository.getCompanyIdFromCompanyMaster(tenantId);

            if (userIdAndComId.isEmpty()){

                return ResponseEntity.ok().body(new GlobalResponseDTO(false, "User Not Found for this staff : " +userId));
            }
            String userIdAssociated = (String) userIdAndComId.get("COMPANY_BACKUP_PHONE_NUMBER");
            int companyId = (Integer) userIdAndComId.get("COMPANY_ID");
            return fetchStaffListBoth(deviceId,deviceType,appVersion,userIdAssociated, tenantId);
        } else {
            return fetchStaffListBoth(deviceId,deviceType,appVersion,userId, tenantId);

        }
    }

    /*
     * AMIT KUMAR SINGH
     * This method fetches a list of staff members based on the provided parameters.
     * It validates the user against the specified tenant ID and user ID, ensuring
     * authorized access to the API. If the user is authorized, it retrieves the staff
     * list; otherwise, it checks for associated user details in the database and
     * retrieves the staff list accordingly.
     * @PARAM request A map containing device ID, device type, app version, user ID, and tenant ID
     * @PARAM principal The principal object representing the current logged-in user
     */
    public ResponseEntity<GlobalResponseDTO> fetchStaffListBoth(String deviceId, String deviceType,String appVersion,
                                                                String userId, String tenantId) {

//        String deviceId = request.get("deviceId");
//        String deviceType = request.get("deviceType");
//        String appVersion = request.get("appVersion");
//        String userId = request.get("userId");
//        String tenantId = request.get("tenantId");

        List<JSONObject> userWithTenantValidation = companyCustomerRepository.userWithTenantValidation(tenantId, userId);

        if (userWithTenantValidation.isEmpty()) {
            return ResponseEntity.accepted().body(new GlobalResponseDTO(false, "Please enter correct Business Id ", null));
        }

        String tenantFirstEightTLetters = null;
        if (tenantId != null && tenantId.length() >= 8) {
            tenantFirstEightTLetters = tenantId.substring(0, 8);

        }


        List<Map<String, Object>> recordForStaff = staffQuery.recordForStaffList(tenantFirstEightTLetters);

        List<Map<String, Object>> staffDataList = new ArrayList<>();

        for (Map<String, Object> staffData : recordForStaff) {
            String staffId = String.valueOf(staffData.get("PK_USER_ID"));

            String staffFName = String.valueOf(staffData.get("USER_FIRST_NAME"));
            String staffLFName = String.valueOf(staffData.get("USER_LAST_NAME"));

            String staffEmail = String.valueOf(staffData.get("USER_EMAIL"));
            String staffPhoneNo = String.valueOf(staffData.get("USER_PHONE_NUMBER"));
            String staffStatus = String.valueOf(staffData.get("USER_STATUS"));
            String staffCreatedAt = String.valueOf(staffData.get("USER_JOINING_DATE"));
            String staffAddress = String.valueOf(staffData.get("USER_ADDRESS"));

            List<String> jobList = new ArrayList<>();

            Map<String, Object> staffResponse = new HashMap<>();
            staffResponse.put("staffId", staffId);
            staffResponse.put("staffFirstName", staffFName);
            staffResponse.put("staffLastName", staffLFName);

            staffResponse.put("staffEmail", staffEmail);
            staffResponse.put("staffPhoneNo", staffPhoneNo);
            staffResponse.put("staffAddress", staffAddress);
            staffResponse.put("activeStatus", staffStatus);
            staffResponse.put("staffCreatedAt", staffCreatedAt);
            staffResponse.put("jobList", jobList);

            staffDataList.add(staffResponse);
        }


        return ResponseEntity.accepted().body(new GlobalResponseDTO(true, "Success ", staffDataList));

    }



    @Override
    public ResponseEntity<GlobalResponseDTO> preNewScheduleData(Map<String, String> request, Principal principal) {

        String deviceId = request.get("deviceId");
        String deviceType = request.get("deviceType");
        String appVersion = request.get("appVersion");
        String userId = request.get("userId");
        String tenantId = request.get("tenantId");


        if(checkUserMatch(userId,tenantId, principal.getName())){
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorised user, we could not access the APIs from others token "));
        }

        String tenantFirstEightTLetters = null;
        String deviceInfo = "deviceId = " + deviceId + "\n" + "deviceType = " + deviceType + "\n" + "appVersion = " + appVersion;

        String fullURL = httpServletRequest.getRequestURL().toString();

        try {
            if (tenantId != null && tenantId.length() >= 8) {
                tenantFirstEightTLetters = tenantId.substring(0, 8);
            }

            List<Map<String, Object>> customerList = new ArrayList<>();
            List<Map<String, Object>> staffList = new ArrayList<>();
            List<Map<String, Object>> serviceEntityQuestions = new ArrayList<>();
            List<Map<String, Object>> serviceList = new ArrayList<>();


            List<Map<String, Object>> recordForStaff = staffQuery.recordForStaffList(tenantFirstEightTLetters);
            for (Map<String, Object> staffRecord : recordForStaff) {
                Map<String, Object> staffData = new HashMap<>();
                staffData.put("staffId", staffRecord.get("PK_USER_ID"));
                staffData.put("staffName", staffRecord.get("USER_FIRST_NAME"));
                staffData.put("staffLastName", staffRecord.get("USER_LAST_NAME"));
                staffList.add(staffData);
            }

            List<Map<String, Object>> recordForCustList = custQuery.recordForCustList(tenantFirstEightTLetters);
            for (Map<String, Object> customerRecord : recordForCustList) {
                Map<String, Object> customerData = new HashMap<>();
                customerData.put("customerId", customerRecord.get("PK_CUSTOMER_ID"));
                customerData.put("customerFirstName", customerRecord.get("CUSTOMER_FIRST_NAME"));
                customerData.put("customerLastName", customerRecord.get("CUSTOMER_LAST_NAME"));
                customerList.add(customerData);
            }

            List<Map<String, Object>> recordForServiceList = serviceQuery.recordForServiceList(tenantFirstEightTLetters, userId);
            for (Map<String, Object> serviceEntityRecord : recordForServiceList) {
                Map<String, Object> serviceEntityData = new HashMap<>();
                serviceEntityData.put("entityQuestionId", serviceEntityRecord.get("PK_FORM_KEY_ID"));
                serviceEntityData.put("entityQuestion", serviceEntityRecord.get("INPUT_KEY"));
                serviceEntityData.put("entityQuestionAnsType", serviceEntityRecord.get("ANSWER_TYPE"));
                serviceEntityData.put("entityQuestionAnsOptions", serviceEntityRecord.get("OPTIONS"));
                serviceEntityQuestions.add(serviceEntityData);
            }


            List<Map<String, Object>> recordForServiceRateList = serviceQuery.recordForServiceRateList(tenantFirstEightTLetters, userId);
            for (Map<String, Object> serviceEntityRateRecord : recordForServiceRateList) {
                Map<String, Object> serviceListDate = new HashMap<>();
                serviceListDate.put("serviceId", serviceEntityRateRecord.get("ID"));
                serviceListDate.put("serviceName", serviceEntityRateRecord.get("SERVICE_NAME"));
                serviceListDate.put("serviceRate", serviceEntityRateRecord.get("RATE"));
                serviceList.add(serviceListDate);
            }


            Map<String, Object> responseData = new HashMap<>();
            responseData.put("customerList", customerList);
            responseData.put("staffList", staffList);
            responseData.put("serviceEntityQuestions", serviceEntityQuestions);
            responseData.put("serviceList", serviceList);


            return ResponseEntity.accepted().body(new GlobalResponseDTO(true, "Success", responseData));

        } catch (Exception e) {
            errorLogService.errorLog(request, e.getMessage(), fullURL, userId, deviceInfo);

            return ResponseEntity.ok(new GlobalResponseDTO(false, "Success", null));

        }

    }

    /*
     *AMIT KUMAR SINGH
     * This method retrieves the lists of services taken by the customer based on the provided
     * parameters including deviceId, deviceType,appVersion, userId, and tenantId. Checks user authorization before proceeding with fetching
     * service records.
     *
     * @param request A map containing the request parameters including deviceId, deviceType,
     *                appVersion, userId, and tenantId.
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> fetchServiceList(Map<String, String> request, Principal principal) {

        String deviceId = request.get("deviceId");
        String deviceType = request.get("deviceType");
        String appVersion = request.get("appVersion");
        String userId = request.get("userId");
        String tenantId = request.get("tenantId");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            String authorityName = authority.getAuthority();
            List<String> priviledgeChk = staffQuery.priviledgeChkForService(tenantId);
            boolean hasEditPrivilege = true;
            for (String privilege : priviledgeChk) {
                if (privilege.equalsIgnoreCase("VIEW")) {
                    hasEditPrivilege = true;
                    break;
                }
            }
            if (authorityName.equalsIgnoreCase("Staff") && !hasEditPrivilege) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new GlobalResponseDTO(false, "A Staff user dont have the priviledge to fetch the service.", null));
            }
        }
        if(checkUserMatch(userId,tenantId, principal.getName())){
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorised user, we could not access the APIs from others token "));
        }

        String tenantFirstEightTLetters = null;

        if (tenantId != null && tenantId.length() >= 8) {
            tenantFirstEightTLetters = tenantId.substring(0, 8);
        }
        List<Map<String, Object>> recordForServiceRateList = serviceQuery.recordForServiceRateList(tenantFirstEightTLetters, userId);


        if (recordForServiceRateList.isEmpty()){

            Map<String, Object> userIdAndComId = profileRepository.getCompanyIdFromCompanyMaster(tenantId);

            if (userIdAndComId.isEmpty()){

                return ResponseEntity.ok().body(new GlobalResponseDTO(false, "User Not Found for this staff : " +userId));
            }
            String userIdAssociated = (String) userIdAndComId.get("COMPANY_BACKUP_PHONE_NUMBER");
            int companyId = (Integer) userIdAndComId.get("COMPANY_ID");

            //Map<String, Object> staffProfile = profileRepository.getStaffProfile(userId, tenantId);


            //System.err.println("Staff profile");
            return fetchServiceListBoth(deviceId,deviceType,appVersion,userIdAssociated, tenantId, request);
            //return   ResponseEntity.ok().body(new GlobalResponseDTO(true, "staff profile", staffProfile));
        } else {
            return fetchServiceListBoth(deviceId,deviceType,appVersion,userId, tenantId, request);

        }
    }

        public ResponseEntity<GlobalResponseDTO> fetchServiceListBoth(String deviceId, String deviceType, String appVersion , String userId , String tenantId,Map<String, String> request) {




        String tenantFirstEightTLetters = null;

        String deviceInfo = "deviceId = " + deviceId + "\n" + "deviceType = " + deviceType + "\n" + "appVersion = " + appVersion;

        String fullURL = httpServletRequest.getRequestURL().toString();

        try {
            if (tenantId != null && tenantId.length() >= 8) {
                tenantFirstEightTLetters = tenantId.substring(0, 8);
            }
            List<Map<String, Object>> serviceList = new ArrayList<>();


            List<Map<String, Object>> recordForServiceRateList = serviceQuery.recordForServiceRateList(tenantFirstEightTLetters, userId);
            for (Map<String, Object> serviceEntityRateRecord : recordForServiceRateList) {
                Map<String, Object> serviceListDate = new HashMap<>();
                serviceListDate.put("serviceId", serviceEntityRateRecord.get("ID"));
                serviceListDate.put("serviceName", serviceEntityRateRecord.get("SERVICE_NAME"));
                serviceListDate.put("serviceRate", serviceEntityRateRecord.get("RATE"));
                serviceListDate.put("serviceActive/InactiveStatus", serviceEntityRateRecord.get("STATUS"));
                serviceList.add(serviceListDate);
            }

            return ResponseEntity.accepted().body(new GlobalResponseDTO(true, "Success", serviceList));
        } catch (Exception e) {
            errorLogService.errorLog(request, e.getMessage(), fullURL, userId, deviceInfo);

            return ResponseEntity.ok(new GlobalResponseDTO(false, "Success", null));

        }
    }


    /**
     * AMIT KUMAR SINGH
     * This method adds a new service with rate informations for a specific user and tenant .
     * @param request   A map containing the request parameters including deviceId, deviceType,
     *                  appVersion, userId, tenantId, serviceName, rate, and rateUnit.e.
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> addService(Map<String, String> request, Principal principal) {
        // Extract necessary data from the request
        String deviceId = request.get("deviceId");
        String deviceType = request.get("deviceType");
        String appVersion = request.get("appVersion");
        String userId = request.get("userId");
        String tenantId = request.get("tenantId");
        String serviceName = request.get("serviceName");
        String rate = request.get("rate");
        String rateUnit = request.get("rateUnit");

        if(checkUserMatch(userId,tenantId, principal.getName())){
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorised user, we could not access the APIs from others token "));
        }

        // Save the service rate data using the staffQuery's saveServiceRateData method

        Integer serviceId = staffQuery.saveServiceRateData(userId, tenantId, serviceName, Integer.valueOf(rate), Integer.valueOf(rateUnit));
        Map<String, Object> serviceDate = new HashMap<>();
        serviceDate.put("serviceId", serviceId);
        serviceDate.put("serviceName", serviceName);


        // Return a ResponseEntity with an "Accepted" status and a response body indicating success

        return ResponseEntity.accepted().body(new GlobalResponseDTO(true, "Successfully added", serviceDate));


    }


    /*
     *AMIT KUMAR SINGH
     * This method retrieve a lists of service rate units from the database.
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> fetchServiceRateUnit(Map<String, String> request, Principal principal) {

        // Retrieve a list of service rate units from the companyUserRepository
        List<JSONObject> serviceRateUnitList = companyUserRepository.getServiceRateUnitList();

        // Return a ResponseEntity with an "Accepted" status and a response body containing the fetched service rate units
        return ResponseEntity.accepted().body(new GlobalResponseDTO(true, "Success", serviceRateUnitList));
    }

    public boolean checkUserMatch(String userId,String tenantId , String username){

        String[] part = username.split(",");
        String tokenUserId = part[0];
        String tokenTenantId = part[1];
        if ( !tenantId.equals(tokenTenantId)){
            return true;
        }

        return false;
    }

    @Override
    public ResponseEntity<GlobalResponseDTO> getServiceDetails(Map<String, String> request, Principal principal) {
        String deviceId = request.get("deviceId");
        String deviceType = request.get("deviceType");
        String appVersion = request.get("appVersion");
        String userId = request.get("userId");
        String tenantId = request.get("tenantId");
        String serviceId = request.get("serviceId");
        if (checkUserMatch(userId, tenantId, principal.getName())) {
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorized user, we could not access the APIs from others token"));
        }
        String tenantFirstEightTLetters = null;
        if (tenantId != null && tenantId.length() >= 8) {
            tenantFirstEightTLetters = tenantId.substring(0, 8);
        }
        Map<String, Object> serviceDetails = serviceQuery.getServiceDetails(tenantFirstEightTLetters, userId, serviceId);
        if (serviceDetails.isEmpty()) {
            return ResponseEntity.ok().body(new GlobalResponseDTO(false, "No service details found for service ID: " + serviceId));
        } else {
            return ResponseEntity.ok().body(new GlobalResponseDTO(true, "Service details fetched successfully", serviceDetails));
        }
    }

    @Override
    public ResponseEntity<GlobalResponseDTO> updateServiceDetails(Map<String, String> request, Principal principal) {
        String tenantId = (String) request.get("tenantId");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            String authorityName = authority.getAuthority();
            List<String> priviledgeChk = staffQuery.priviledgeChkForService(tenantId);
            boolean hasEditPrivilege = true;
            for (String privilege : priviledgeChk) {
                if (privilege.equalsIgnoreCase("EDIT")) {
                    hasEditPrivilege = true;
                    break;
                }
            }
            if (authorityName.equalsIgnoreCase("Staff") && !hasEditPrivilege) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new GlobalResponseDTO(false, "A Staff user dont have the priviledge to edit the service.", null));
            }
        }
        String deviceId = request.get("deviceId");
        String deviceType = request.get("deviceType");
        String appVersion = request.get("appVersion");
        String userId = request.get("userId");
        String serviceId = request.get("serviceId");
        String serviceName = request.get("serviceName");
        String rate = request.get("rate");
        String rateUnit = request.get("rateUnit");
        String status = request.get("status");
        if (checkUserMatch(userId, tenantId, principal.getName())) {
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorized user, we could not access the APIs from others token"));
        }
        String tenantFirstEightTLetters = null;
        if (tenantId != null && tenantId.length() >= 8) {
            tenantFirstEightTLetters = tenantId.substring(0, 8);
        }
        boolean isUpdated = serviceQuery.updateServiceDetails(tenantFirstEightTLetters, userId, serviceId, serviceName, rate, rateUnit, status);
        if (isUpdated) {
            return ResponseEntity.ok().body(new GlobalResponseDTO(true, "Service details updated successfully"));
        } else {
            return ResponseEntity.status(404).body(new GlobalResponseDTO(false, "Service not found or update failed"));
        }
    }

    @Override
    public ResponseEntity<GlobalResponseDTO> deleteServiceDetails(Map<String, String> request, Principal principal) {
        String tenantId = request.get("tenantId");
        String userId = request.get("userId");
        String serviceId = request.get("serviceId");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            String authorityName = authority.getAuthority();
            List<String> priviledgeChk = staffQuery.priviledgeChkForService(tenantId);
            boolean hasEditPrivilege = false;
            for (String privilege : priviledgeChk) {
                if (privilege.equalsIgnoreCase("DELETE")) {
                    hasEditPrivilege = true;
                    break;
                }
            }
            if (authorityName.equalsIgnoreCase("Staff") && !hasEditPrivilege) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new GlobalResponseDTO(false, "A Staff user dont have the priviledge to delete the service.", null));
            }
        }
        if (checkUserMatch(userId, tenantId, principal.getName())) {
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorized user, we could not access the APIs from others' tokens"));
        }

        try {
            int rowsAffected = serviceQuery.deleteService(tenantId, userId, serviceId);

            if (rowsAffected > 0) {
                return ResponseEntity.ok().body(new GlobalResponseDTO(true, "Service with ID " + serviceId + " deleted successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new GlobalResponseDTO(false, "No service found with ID " + serviceId));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GlobalResponseDTO(false, "Error occurred while deleting service"));
        }
    }


}





