package com.bizfns.services.Serviceimpl;

import com.bizfns.services.GlobalDto.GlobalResponseDTO;
import com.bizfns.services.Query.StaffAuthQuery;
import com.bizfns.services.Query.StaffQuery;
import com.bizfns.services.Repository.*;
import com.bizfns.services.Service.ErrorLogService;
import com.bizfns.services.Service.RegistrationService;
import com.bizfns.services.Utility.JWTUtility;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.security.Principal;
import java.security.SecureRandom;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

@Service
public class RegistrationServiceImpl implements RegistrationService {
    @Autowired
    private RegistrationRepository registrationRepository;


    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private JWTUtility jwtUtility;

    @Autowired
    private EmailSenderService emailSenderService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CompanyUserRepository companyUserRepository;

    @Autowired
    private CompanyMasterRepository companyMasterRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private DataSource dataSource;

    @Autowired
    private StaffAuthQuery staffAuthQuery;

    @Autowired
    private StaffQuery staffQuery;

    @Autowired
    private ErrorLogServiceImpl errorLogService;

    @Autowired
    private HttpServletRequest httpServletRequest;


    /*
     * AMIT KUMAR SINGH
     * This method handles the registration process for a new company.
     * It extracts information such as business name, type, phone number, email, password,
     * plan ID, and business category from the request to register the company.
     * Generates a unique tenant ID and client ID for the company.
     * Checks if the provided phone number is already associated with any existing business.
     * If not, it inserts the registration data into the database, including encryption of sensitive information.
     * Sends a registration notification and creates a schema for the new user.
     * @PARAM request A map containing registration details such as business name, type, phone number, email, password, plan ID, and business category
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> companyRegistration(Map<String, String> request) {

        // Extract information from the request

        String businessName = request.get("businessName");
        String businessType = request.get("businessType");
        String phoneNumber = request.get("phoneNumber");
        String businessEmail = request.get("businessEmail");
        String password = request.get("password");
        String planId = request.get("planId");
        String businessCategory = request.get("businessCategory");

        // Remove leading spaces from the business name
        //  String trimBusinessName = businessName.trim().replaceAll("\\s+", "").toLowerCase();
        String trimBusinessName = businessName.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();


        // Generate a unique tenant ID based on the business name and a random number
        String firstFourDigitsBusinessName = trimBusinessName.substring(0, 4);
        Random random = new Random();
        int randomNumber = random.nextInt(9000) + 1000;
        String randomNumberString = String.valueOf(randomNumber);
        String tenantId = firstFourDigitsBusinessName + randomNumberString;

        // Create a unique client ID based on business name, business type, and current timestamp
        String clientId = businessName.trim() + businessType + String.valueOf(System.currentTimeMillis());

        // Encrypt the client ID for security
        AES obj = new AES();
        String encryptedClientId = obj.encrypt(clientId);

        //Check if the email or phone number already exists in the database
        //List<String> fetchUserIdByEmail = registrationRepository.fetchUserIdByEmail(businessEmail);
       // List<String> fetchUserIdByPhNo = registrationRepository.fetchUserIdByPhNo(phoneNumber);

        // Handle cases where email or phone number already exist
//        if (!fetchUserIdByEmail.isEmpty()) {
//            return ResponseEntity.accepted().body(new GlobalResponseDTO(false, "EmailId exist already", null));
//
//        } else if (!fetchUserIdByPhNo.isEmpty()) {
//
//            return ResponseEntity.accepted().body(new GlobalResponseDTO(false, "Phone number already exists", null));
//
//        }

        List<String> fetchPhNumberOfBusiness = registrationRepository.fetchPhoneNumberOfBusiness(businessName);
        if(fetchPhNumberOfBusiness!=null && fetchPhNumberOfBusiness.contains(phoneNumber)){
            return ResponseEntity.accepted().body(new GlobalResponseDTO(false, "The Phone Number is already registered with busniess " + businessName + "", tenantId));
        }else{
            Integer autoGeneratedCompId = registrationRepository.inserCompanyRegistrationData(businessName, phoneNumber,
                    businessEmail, obj.encrypt(password), tenantId, encryptedClientId);
            Integer autoGeneratedBusinessTypeIdId = registrationRepository.inserCompanyRegistrationBusinessTypeData(Integer.valueOf(businessType), autoGeneratedCompId);
            registrationRepository.insertCompanyRegistrationPlanData(Integer.valueOf(planId), autoGeneratedBusinessTypeIdId);
            String userType = "Company";
            registrationRepository.insertBusinessIdData(phoneNumber, businessEmail, businessName, tenantId, userType);
            notificationRepository.registrationMessageQuery(phoneNumber, tenantId,"Successfully Registered","Company registration");
            createSchemaForUser(tenantId);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            String formattedTime = now.format(formatter);
            Timestamp timestamp = Timestamp.valueOf(now);

            String sql = "INSERT INTO \"" + tenantId + "\".\"reminder_event_details\" " +
                    "(\"REMINDER_ID\", \"EVENT_ID\", \"SMS\", \"PUSH\", \"FEEDBACK_REQUEST\", \"CREATED\", \"UPDATED\") " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            jdbcTemplate.update(sql,
                    1, 1, true, true, false, timestamp, timestamp);
            jdbcTemplate.update(sql,
                    2, 1, true, true, false, timestamp, timestamp);
            jdbcTemplate.update(sql,
                    3, 1, false, true, false, timestamp, timestamp);
            jdbcTemplate.update(sql,
                    4, 1, true, false, true, timestamp, timestamp);
            jdbcTemplate.update(sql,
                    5, 2, true, true, false, timestamp, timestamp);
            jdbcTemplate.update(sql,
                    6, 3, true, true, false, timestamp, timestamp);
            jdbcTemplate.update(sql,
                    7, 4, true, true, false, timestamp, timestamp);
            jdbcTemplate.update(sql,
                    8, 4, true, true, false, timestamp, timestamp);
            jdbcTemplate.update(sql,
                    9, 4, true, true, false, timestamp, timestamp);
            return ResponseEntity.accepted().body(new GlobalResponseDTO(true, "Successfully Registered", tenantId));
        }
    }


    /*
     * AMIT KUMAR SINGH
     * This method checks whether a phone number is already associated with any business registration.
     * It queries the database to fetch previous associations of the provided phone number.
     * Returns a response indicating whether the phone number is already registered or not.
     * @PARAM request A map containing the phone number to be checked
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> phoneNoRegCheck(Map<String, String> request) {
        String phoneNumber = request.get("phoneNumber");
        List<String> fetchPrevAssoBussiness = registrationRepository.fetchPrevAssoBuss(phoneNumber);
        if (fetchPrevAssoBussiness.contains(phoneNumber)) {
            return ResponseEntity.accepted().body(new GlobalResponseDTO(false, "The Phone Number: " + phoneNumber + " is already registered with a business."));
        }
        return ResponseEntity.accepted().body(new GlobalResponseDTO(true, "The Phone Number: " + phoneNumber + " is not registered with any business."));
    }

    /*
     * Retrieves security questions and their answers for a specified user.
     * If the user has previously set security questions, it fetches the questions and their answers.
     * If no security questions are set, it fetches available security questions for the user to choose from.
     *
     * @param request A map containing userId and tenantId for identifying the user
     * @param principal The Principal object containing the current authenticated user's information
     * @return ResponseEntity containing security question data or an error message if unauthorized or no data found
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> getSecurityQuestion(Map<String, String> request, Principal principal) {

        // Initialize a variable to hold the response data
        Object response = null;

        // Extract user ID and tenant ID from the request
        String userId = request.get("userId");
        String tenantId = request.get("tenantId");

        if(checkUserMatch(userId,tenantId, principal.getName())){
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorised user, we could not access the APIs from others token "));
        }

        // Retrieve user data for validation from the company master repository
        //List<JSONObject> dataForUserIdValidationFromCompanyMaster = companyUserRepository.dataForUserIdValidationFromCompanyMaster(userId);
        List<JSONObject> dataForUserIdValidationFromCompanyMaster =companyUserRepository.dataUserIdValidationFromCompanyMaster(userId,tenantId);
        // Check if user data exists in the company master repository
        if (!dataForUserIdValidationFromCompanyMaster.isEmpty()) {

            // Check if the user has previously set security questions
            List<JSONObject> checkSecurityQuestionByUserId = companyUserRepository.checkSecurityQuestionByUserId(userId,tenantId);

            // If no security questions are set, retrieve a list of available security questions
            if (checkSecurityQuestionByUserId.isEmpty()) {
                List<JSONObject> fetchSecurityQuestionByUserId = companyUserRepository.fetchSecurityQuestionByUserId();
                response = fetchSecurityQuestionByUserId;
            } else {
                // If security questions are already set, retrieve the user's security question answers
                List<JSONObject> getSecurityQuestionAnsByUserId = companyUserRepository.getSecurityQuestionAnsByUserId(userId,tenantId);
                response = getSecurityQuestionAnsByUserId;

            }

        }

        // Return the response containing security question data or an appropriate message
        return ResponseEntity.accepted().body(new GlobalResponseDTO(true, "Success", response));
    }

    /*
     * Saves security questions and their answers for a specified user.
     * Checks if the user is authorized and verifies user information before saving.
     * If the user has not already submitted security question answers, it saves them.
     * If answers were already submitted, it returns a message indicating that they were already saved.
     *
     * @param userId, tenantId.
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> saveSecurityQuestion(Map<String, Object> request, Principal principal) {

        // Extract device information from the request
        String deviceId = (String) request.get("deviceId");
        String deviceType = (String) request.get("deviceType");
        String appVersion = (String) request.get("appVersion");

        // Extract user information from the request
        String userId = (String) request.get("userId");
        String tenantId = (String) request.get("tenantId");

        if(checkUserMatch(userId,tenantId, principal.getName())){
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorised user, we could not access the APIs from others token "));
        }

        // Extract security questions and answers from the request
        List<Map<String, Object>> securityQuestions = (List<Map<String, Object>>) request.get("sequrityQuestions");


        // Initialize a variable to track the user type
        String user = null;

        // Retrieve user data for validation from the company master repository
        List<JSONObject> dataForUserIdValidationFromCompanyMaster = companyUserRepository.dataForUserIdValidationFromCompanyMaster(userId);

        // Check if user data exists in the company master repository
        if (!dataForUserIdValidationFromCompanyMaster.isEmpty()) {

            // User type is set as "Company"
            user = "Company";

            // Check if the user has already submitted security question answers
            String availableAns = companyUserRepository.availableAns(userId,tenantId);
            if (availableAns.equalsIgnoreCase("n")) {
                // If answers are not yet submitted, iterate through security questions and save answers
                for (Map<String, Object> question : securityQuestions) {

                    Integer questionId = (Integer) question.get("PK_QUESTION_ID");
                    String questionName = (String) question.get("QUESTION");
                    String answer = (String) question.get("answeer");

                    companyUserRepository.companyQuestionAnsSave(questionId, questionName, answer, userId, tenantId);
                }
                // Return a success response
                return ResponseEntity.accepted().body(new GlobalResponseDTO(true, "Submitted", null));

            } else {
                // Return a response indicating that security questions were already submitted
                return ResponseEntity.accepted().body(new GlobalResponseDTO(false, "Already Submitted", null));

            }
        }


        // Return a response for an invalid user
        return ResponseEntity.accepted().body(new GlobalResponseDTO(false, "", null));
    }


    /*
     * Huzefa
     * This endpoint handles the process of sending a one-time password (OTP) for password recovery.
     * It validates the user ID and tenant ID provided in the request.
     * If the user is not an admin, it sends the OTP to the user's registered email address.
     * If the tenant ID is not provided, it defaults to handling admin-related OTP functionality.
     *
     * @param request A map containing userId and tenantId for identifying the user, and userType to distinguish admin or non-admin
     * @param userType The type of user, where "4" indicates admin
     * @return ResponseEntity containing a success message with OTP details or an error message if user or tenant is invalid
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> forgotPassword(Map<String, String> request, String userType) {




        // Extract user ID and tenant ID from the request
        String userId = request.get("userId");
        String tenantId = request.get("tenantId");
        String otpTimeStamp = null;


        /*
        * @Author Huzefa
        * This method help the admin to change the password
        * */
        if (tenantId.isEmpty()){
            if (userType!=null && userType.equalsIgnoreCase("4")){

                Map<String, Object> response = new HashMap<>();

                // Convert a portion of the user ID to a hidden format
                String convertedUserId = userId.substring(0, 2) + userId.substring(2, userId.length() - 2).replaceAll(".", "*") + userId.substring(userId.length() - 2);
                String convertedUserIdMessage = "We have sent you verification code on" + " " + convertedUserId;


                String lastTwoTLetters = null;
                String tenantFirstEightTLetters = null;


                //  lastTwoTLetters = tenantId.substring(tenantId.length() - 2);

                /// Check tenantId length

                if (tenantId != null && tenantId.length() >= 2) {
                    lastTwoTLetters = tenantId.substring(tenantId.length() - 2);
                    tenantFirstEightTLetters = tenantId.substring(0, 8);

                }


                // Retrieve tenant data for validation from the company master repository
                List<JSONObject> dataForTenantValidationFromCompanyMaster = companyUserRepository.dataForTenantValidationFromCompanyMaster("adminOwn");

                // Check if tenant data exists in the company master repository
                if (dataForTenantValidationFromCompanyMaster.isEmpty()) {
                    // Return an error response for incorrect Business ID
                    return ResponseEntity.accepted().body(new GlobalResponseDTO(false, "Please enter correct Business Id ", null));
                }

                // Retrieve user data for validation from the company master repository
                List<JSONObject> dataForUserIdValidationFromCompanyMaster = companyUserRepository.dataForUserIdValidationFromCompanyMaster(userId);


                // Check if user data exists in the company master repository
                if (!dataForUserIdValidationFromCompanyMaster.isEmpty()) {

                    // Generate a random OTP
                    Random random = new Random();
                    int randomNumber = random.nextInt(900000) + 100000;


                    // Retrieve email information for the user from the company master repository
                    JSONObject getMailId = companyMasterRepository.getMailId(userId, tenantId);

                    if (getMailId != null) {
                        // The JSONObject is not null, so proceed with sending the email.
                        String otpMailSub = "Bizfns";
                        String otpMailBody = "Your one-time password is: " + randomNumber;

                        String companyBackupEmail = (String) getMailId.get("COMPANY_BACKUP_EMAIL");
                        if (companyBackupEmail != null) {
                            // Send the OTP email to the backup email address
                            //emailSenderService.sendSimpleEmail(companyBackupEmail, otpMailSub, otpMailBody);
                        } else {
                            // Handle the case when "COMPANY_BACKUP_EMAIL" is not found in the JSONObject
                        }
                    } else {
                        // Handle the case when getMailId is null
                    }


                    // Check if an OTP already exists for the user
                    List<JSONObject> checkOtpExistence = adminRepository.checkOtpExistence(userId);

                    if (checkOtpExistence.isEmpty()) {
                        // If no OTP exists, insert the OTP and its timestamp
                        otpTimeStamp = adminRepository.insertFirstLoginOtp(String.valueOf(randomNumber), userId, tenantId);
                    } else {

                        // If an OTP exists, update the OTP and its timestamp
                        otpTimeStamp = adminRepository.updateLoginOtp(String.valueOf(randomNumber), userId);
                    }

                    // Populate the response map with relevant data
                    response.put("otp", randomNumber);
                    //   response.put("token", token);
                    response.put("otpTimeStamp", otpTimeStamp);
                    response.put("otpMsg", convertedUserIdMessage);

                    // Return a success response with the response map
                    return ResponseEntity.accepted().body(new GlobalResponseDTO(true, "otpMsg", response));

                }


                // Return an error response for incorrect User ID
                return ResponseEntity.accepted().body(new GlobalResponseDTO(false, "Please enter the correct User Id", null));



                //System.err.println("Huzefa ansari");

            }else {

            }



        }

        // Initialize a map to hold the response data
        Map<String, Object> response = new HashMap<>();

        // Convert a portion of the user ID to a hidden format
        String convertedUserId = userId.substring(0, 2) + userId.substring(2, userId.length() - 2).replaceAll(".", "*") + userId.substring(userId.length() - 2);
        String convertedUserIdMessage = "We have sent you verification code on" + " " + convertedUserId;


        String lastTwoTLetters = null;
        String tenantFirstEightTLetters = null;


        //  lastTwoTLetters = tenantId.substring(tenantId.length() - 2);

        /// Check tenantId length

        if (tenantId != null && tenantId.length() >= 2) {
            lastTwoTLetters = tenantId.substring(tenantId.length() - 2);
            tenantFirstEightTLetters = tenantId.substring(0, 8);

        }


        // Retrieve tenant data for validation from the company master repository
        List<JSONObject> dataForTenantValidationFromCompanyMaster = companyUserRepository.dataForTenantValidationFromCompanyMaster(tenantFirstEightTLetters);

        // Check if tenant data exists in the company master repository
        if (dataForTenantValidationFromCompanyMaster.isEmpty()) {
            // Return an error response for incorrect Business ID
            return ResponseEntity.accepted().body(new GlobalResponseDTO(false, "Please enter correct Business Id ", null));
        }


        if (lastTwoTLetters.equalsIgnoreCase("st")) {

            Random random = new Random();
            int randomNumber = random.nextInt(900000) + 100000;
            String strRandomNo = String.valueOf(randomNumber);


            String getStaffMailId = staffQuery.getStaffMailId(userId, tenantFirstEightTLetters);

            if (getStaffMailId != null) {

                String otpMailSub = "Bizfns";
                String otpMailBody = "Your one-time password is: " + randomNumber;

                //emailSenderService.sendSimpleEmail(getStaffMailId, otpMailSub, otpMailBody);

            }


            // Check if staff OTP exists in the database
            String staffOtpExistence = staffQuery.staffOtpExistence(tenantFirstEightTLetters, userId);
            if (StringUtils.isEmpty(staffOtpExistence)) {
                // Insert or update staff OTP in the database

                staffQuery.insertFirstStaffOtp(tenantFirstEightTLetters, userId, strRandomNo);

            } else {
                staffQuery.updateStaffOtp(tenantFirstEightTLetters, userId, strRandomNo);

            }


            // Populate the response map with relevant data
            response.put("otp", randomNumber);
            //   response.put("token", token);
            response.put("otpTimeStamp", otpTimeStamp);
            response.put("otpMsg", convertedUserIdMessage);

            // Return a success response with the response map
            return ResponseEntity.accepted().body(new GlobalResponseDTO(true, "otpMsg", response));

        } else {


            // Retrieve user data for validation from the company master repository
            List<JSONObject> dataForUserIdValidationFromCompanyMaster = companyUserRepository.dataForUserIdValidationFromCompanyMaster(userId);

            // Check if user data exists in the company master repository
            if (!dataForUserIdValidationFromCompanyMaster.isEmpty()) {

                // Generate a random OTP
                Random random = new Random();
                int randomNumber = random.nextInt(900000) + 100000;


                // Retrieve email information for the user from the company master repository
                JSONObject getMailId = companyMasterRepository.getMailId(userId, tenantId);

                if (getMailId != null) {
                    // The JSONObject is not null, so proceed with sending the email.
                    String otpMailSub = "Bizfns";
                    String otpMailBody = "Your one-time password is: " + randomNumber;

                    String companyBackupEmail = (String) getMailId.get("COMPANY_BACKUP_EMAIL");
                    if (companyBackupEmail != null) {
                        // Send the OTP email to the backup email address
                        //emailSenderService.sendSimpleEmail(companyBackupEmail, otpMailSub, otpMailBody);
                    } else {
                        // Handle the case when "COMPANY_BACKUP_EMAIL" is not found in the JSONObject
                    }
                } else {
                    // Handle the case when getMailId is null
                }


                // Check if an OTP already exists for the user
                List<JSONObject> checkOtpExistence = companyMasterRepository.checkOtpExistence(userId, tenantId);

                if (checkOtpExistence.isEmpty()) {
                    // If no OTP exists, insert the OTP and its timestamp
                    otpTimeStamp = companyMasterRepository.insertFirstLoginOtp(String.valueOf(randomNumber), userId, tenantId);
                } else {

                    // If an OTP exists, update the OTP and its timestamp
                    otpTimeStamp = companyMasterRepository.updateLoginOtp(String.valueOf(randomNumber), userId,tenantId);
                }
                response.put("otp", randomNumber);
                response.put("otpTimeStamp", otpTimeStamp);
                response.put("otpMsg", convertedUserIdMessage);
                return ResponseEntity.accepted().body(new GlobalResponseDTO(true, "otpMsg", response));

            }
            return ResponseEntity.accepted().body(new GlobalResponseDTO(false, "User Id Not Found.Please Enter Correct User Id", null));
        }
    }


    /*
     * HAMIT KUMAR SINGH
     * This endpoint validates the OTP entered during the password reset process.
     * It verifies the OTP against the stored OTP for the user and tenant ID provided.
     * If the user is identified as an admin (userType = 4), it checks admin-related OTP handling.
     * If the tenant ID is not provided, it defaults to handling admin-related OTP functionality.
     *
     * @param request A map containing userId, tenantId, and otp for validating the OTP entered by the user
     * @param userType The type of user, where "4" indicates admin
     * @return ResponseEntity containing a success message if OTP is valid and not expired, or an error message otherwise
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> validateForgotPasswordOtp(Map<String, String> request, String userType) {

        String userId = request.get("userId");
        String tenantId = request.get("tenantId");
        String otp = request.get("otp");

        String otpTimeStamp = null;
        String isOtpExpire = null;
        String executeQuery = null;
        String fetchTimeStamp = null;
        String fetchOtp = null;

        if (tenantId.isEmpty()) {
            if (userType != null && userType.equalsIgnoreCase("4")) {

                Map<String, Object> response = new HashMap<>();

                String convertedUserId = userId.substring(0, 2) + userId.substring(2, userId.length() - 2).replaceAll(".", "*") + userId.substring(userId.length() - 2);
                String convertedUserIdMessage = "We have sent you verification code on" + " " + convertedUserId;

                String lastTwoTLetters = null;
                String tenantFirstEightTLetters = null;


                //  lastTwoTLetters = tenantId.substring(tenantId.length() - 2);

                /// Check tenantId length

                String tenantId1 = "adminOwn";

                if (tenantId1 != null && tenantId1.length() >= 2) {
                    lastTwoTLetters = tenantId1.substring(tenantId1.length() - 2);
                    tenantFirstEightTLetters = tenantId1.substring(0, 8);

                }

                List<JSONObject> dataForUserIdValidationFromCompanyMaster = companyUserRepository.dataForUserIdValidationFromCompanyMaster(userId);

                if (!dataForUserIdValidationFromCompanyMaster.isEmpty()) {
                    fetchOtp = adminRepository.checkOtpExistenceForPassword(userId);
                    //  fetchTimeStamp = companyUserRepository.fetchTimeStamp(userId);
                    isOtpExpire = adminRepository.isOtpExpire(userId);
                    if (otp.equals(fetchOtp)) {
                        if (isOtpExpire.equalsIgnoreCase("y")) {


                            return ResponseEntity.accepted()
                                    .body(new GlobalResponseDTO(true, "success", null));

                        } else {
                            return ResponseEntity.accepted()
                                    .body(new GlobalResponseDTO(false, "otp expire", null));
                        }


                    }

                }
                return ResponseEntity.accepted()
                        .body(new GlobalResponseDTO(false, "otp not match", null));


            }
        }




                Map<String, Object> response = new HashMap<>();

        String convertedUserId = userId.substring(0, 2) + userId.substring(2, userId.length() - 2).replaceAll(".", "*") + userId.substring(userId.length() - 2);
        String convertedUserIdMessage = "We have sent you verification code on" + " " + convertedUserId;

        String lastTwoTLetters = null;
        String tenantFirstEightTLetters = null;


        //  lastTwoTLetters = tenantId.substring(tenantId.length() - 2);

        /// Check tenantId length

        if (tenantId != null && tenantId.length() >= 2) {
            lastTwoTLetters = tenantId.substring(tenantId.length() - 2);
            tenantFirstEightTLetters = tenantId.substring(0, 8);

        }


        if (lastTwoTLetters.equalsIgnoreCase("st")) {
            fetchOtp = staffAuthQuery.fetctStaffOtp(tenantFirstEightTLetters, userId);
            isOtpExpire = staffAuthQuery.fetchStaffOtpExpire(tenantFirstEightTLetters, userId);
            if (otp.equals(fetchOtp)) {
                if (isOtpExpire.equalsIgnoreCase("y")) {


                    return ResponseEntity.accepted()
                            .body(new GlobalResponseDTO(true, "success", null));

                } else {
                    return ResponseEntity.accepted()
                            .body(new GlobalResponseDTO(false, "otp expire", null));
                }


            }
            return ResponseEntity.accepted()
                    .body(new GlobalResponseDTO(false, "otp not match", null));
        } else {


            List<JSONObject> dataForUserIdValidationFromCompanyMaster = companyUserRepository.dataForUserIdValidationFromCompanyMaster(userId);

            if (!dataForUserIdValidationFromCompanyMaster.isEmpty()) {
                fetchOtp = companyUserRepository.fetchOtp(userId,tenantId);
                //  fetchTimeStamp = companyUserRepository.fetchTimeStamp(userId);
                isOtpExpire = companyUserRepository.isOtpExpire(userId,tenantId);
                if (otp.equals(fetchOtp)) {
                    if (isOtpExpire.equalsIgnoreCase("y")) {


                        return ResponseEntity.accepted()
                                .body(new GlobalResponseDTO(true, "success", null));

                    } else {
                        return ResponseEntity.accepted()
                                .body(new GlobalResponseDTO(false, "otp expire", null));
                    }


                }

            }
            return ResponseEntity.accepted()
                    .body(new GlobalResponseDTO(false, "otp not match", null));
        }
    }


    /*
     * Huzefa
     * This method allows users to reset their password based on user type (admin or regular user).
     * It encrypts the new password using AES encryption before storing it.
     * If the tenantId is not provided, it defaults to handling admin-related password reset functionality.
     *
     * @param request A map containing userId, tenantId, and newPassword for resetting the password
     * @param userType The type of user, where "4" indicates admin
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> resetPassword(Map<String, String> request, String userType) {
        String userId = request.get("userId");
        String tenantId = request.get("tenantId");
        String newPassword = request.get("newPassword");


        AES obj = new AES();

        if (tenantId.isEmpty()) {
            if (userType != null && userType.equalsIgnoreCase("4")) {

                String tenantId1 = "adminOwn";
                String lastTwoTLetters = null;
                String tenantFirstEightTLetters = null;


                if (tenantId1 != null && tenantId1.length() >= 8) {
                    lastTwoTLetters = tenantId1.substring(tenantId1.length() - 2);
                    tenantFirstEightTLetters = tenantId1.substring(0, 8);

                }
                List<JSONObject> dataForUserIdValidationFromCompanyMaster = companyUserRepository.dataForUserIdValidationFromCompanyMaster(userId);


                if (!dataForUserIdValidationFromCompanyMaster.isEmpty()) {

                    companyMasterRepository.saveChangePassWord(userId,tenantId1, obj.encrypt(newPassword));
                   // companyMasterRepository.updatePassword(userId, obj.encrypt(newPassword));
                    adminRepository.updatePasswordAdmin(userId, obj.encrypt(newPassword));

                    return ResponseEntity.accepted()
                            .body(new GlobalResponseDTO(true, "password change successfully", null));

                }


                return ResponseEntity.accepted()
                        .body(new GlobalResponseDTO(true, "No user found", null));



            }

        }

        String lastTwoTLetters = null;
        String tenantFirstEightTLetters = null;


        if (tenantId != null && tenantId.length() >= 8) {
            lastTwoTLetters = tenantId.substring(tenantId.length() - 2);
            tenantFirstEightTLetters = tenantId.substring(0, 8);

        }

        if (lastTwoTLetters.equalsIgnoreCase("st")) {


            staffAuthQuery.changeSataffPassord(tenantFirstEightTLetters, obj.encrypt(newPassword), userId);


            return ResponseEntity.accepted()
                    .body(new GlobalResponseDTO(true, "Successfully Updated", null));

        } else {
            List<JSONObject> dataForUserIdValidationFromCompanyMaster = companyUserRepository.dataForUserIdValidationFromCompanyMaster(userId);


            if (!dataForUserIdValidationFromCompanyMaster.isEmpty()) {

                companyMasterRepository.saveChangePassWord(userId,tenantId, obj.encrypt(newPassword));
                return ResponseEntity.accepted()
                        .body(new GlobalResponseDTO(true, "password change successfully", null));

            }


            return ResponseEntity.accepted()
                    .body(new GlobalResponseDTO(true, "No user found", null));
        }
    }


    @Override
    public ResponseEntity<GlobalResponseDTO> testSchema(String token, Map<String, String> request) {
        String userId = request.get("userId");


//        System.out.println(token);
//        String userName = jwtUtility.extractUsernameFromToken(token);
//        System.out.println(userName);
//
//
        //    String schemaName = "axis45";

//        String query = "select * from  \"" + schemaName + "\".\"assigned_job\"";
//        List<Map<String, Object>> result = jdbcTemplate.queryForList(query);


        //   String insertStaffQuery = "select * from  \"" + schemaName + "\".\"assigned_job\"";


        // String tableName = "COMPANY_MASTER";

        // String query = "SELECT * FROM \"" + schemaName + "\".\""+tableName+"\"";


//        PKCS7Padding p = new PKCS7Padding();
//        p.sayan();

//        CheckSchema schemaService = new CheckSchema(jdbcTemplate);
//
//        boolean schemaExists = schemaService.doesSchemaExist(schemaName);
//        System.out.println("Schema " + schemaName + " exists in the database: " + schemaExists);


//        RegistrationServiceImpl ab = new RegistrationServiceImpl();
//        ab.createSchemaForUser(userId);


        return ResponseEntity.accepted()
                .body(new GlobalResponseDTO(true, "password change successfully", "result"));
    }

    /*
     * AMIT KUMAR SINGH
     * This method verifies security questions provided by the user.
     * It checks if the answers provided match the stored answers in the database for the given user.
     * If all answers match, it returns a success response; otherwise, it returns an error response.
     *
     * @param request A map containing deviceId, deviceType, appVersion, userId, tenantId, and securityQuestions
     * @param principal Principal object containing user authentication details
     * @return ResponseEntity containing a success message if all security answers are correct, or an error message otherwise
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> verifySecurityQuestion(Map<String, Object> request, Principal principal) {

        String deviceId = (String) request.get("deviceId");
        String deviceType = (String) request.get("deviceType");
        String appVersion = (String) request.get("appVersion");
        String userId = (String) request.get("userId");
        String tenantId = (String) request.get("tenantId");
        List<Map<String, Object>> securityQuestions = (List<Map<String, Object>>) request.get("sequrityQuestions");

        if(checkUserMatch(userId,tenantId, principal.getName())){
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorised user, we could not access the APIs from others token "));
        }


        if (securityQuestions == null || securityQuestions.isEmpty()) {
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "No security questions provided.", null));
        }

        List<org.json.simple.JSONObject> dataForUserIdValidationFromCompanyMaster = companyUserRepository.dataUserIdValidationFromCompanyMaster(userId,tenantId);

        if (!dataForUserIdValidationFromCompanyMaster.isEmpty()) {


            boolean isAllAnsTrue = true;
            for (Map<String, Object> question : securityQuestions) {

                Integer questionId = (Integer) question.get("PK_QUESTION_ID");
                String questionName = (String) question.get("QUESTION");
                String answer = (String) question.get("answeer");


                boolean checkAns = companyMasterRepository.checkAns(userId, questionId, answer,tenantId);


                if (!checkAns) {
                    isAllAnsTrue = false;
                    break;
                }

            }

            if (isAllAnsTrue) {
                return ResponseEntity.accepted()
                        .body(new GlobalResponseDTO(true, "all correct", null));
            } else {
                return ResponseEntity.accepted()
                        .body(new GlobalResponseDTO(false, "Wrong answer", null));
            }
        }

        return ResponseEntity.accepted()
                .body(new GlobalResponseDTO(false, "no user found", null));
    }


    /*
     * AMIT KUMAR SINGH
     * This method updates the mobile number for a business user.
     * It verifies the current mobile number, checks if the new mobile number already exists,
     * and then updates the mobile number in the database if all conditions are met.
     *
     * @param request A map containing userId, tenantId, currentMobileNo, and newMobileNo
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> updateBusinessMobileNo(Map<String, String> request) {

        String userId = request.get("userId");
        String tenantId = request.get("tenantId");
        String currentMobileNo = request.get("currentMobileNo");
        String newMobileNo = request.get("newMobileNo");


        List<org.json.simple.JSONObject> dataForUserIdValidationFromCompanyMaster = companyUserRepository.dataForUserIdValidationFromCompanyMaster(userId);

        if (!dataForUserIdValidationFromCompanyMaster.isEmpty()) {


            String mobileNo = companyMasterRepository.fetchMobileNoForCompany(userId);


            if (mobileNo.equalsIgnoreCase(currentMobileNo)) {


                List<org.json.simple.JSONObject> getMobileNoExistence = companyMasterRepository.getMobileNoExistence(newMobileNo);

                if (!getMobileNoExistence.isEmpty()) {


                    return ResponseEntity.accepted()
                            .body(new GlobalResponseDTO(false, "Mobile number already exists", null));

                }


                companyMasterRepository.refreshToken(userId, tenantId);

                companyMasterRepository.updateUserMobileNumber(currentMobileNo, newMobileNo);
                companyMasterRepository.changeMobileNo(currentMobileNo, newMobileNo);


                return ResponseEntity.accepted()
                        .body(new GlobalResponseDTO(true, "success", null));


            } else {
                return ResponseEntity.accepted()
                        .body(new GlobalResponseDTO(false, "current mobile number not match", null));

            }
        }

        return ResponseEntity.accepted()
                .body(new GlobalResponseDTO(false, "no user found", null));
    }


    public String generateRandomPassword() {
        Random random = new Random();
        StringBuilder password = new StringBuilder();

        // Generate the first three characters (uppercase and lowercase letters)
        StringBuilder alphabets = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            if (i == 0) {
                char firstChar = (char) ('A' + random.nextInt(26)); // Generates uppercase letters
                alphabets.append(firstChar);
            } else {
                char nextChar = (char) ('a' + random.nextInt(26)); // Generates lowercase letters
                alphabets.append(nextChar);
            }
        }

        // Shuffle alphabet characters
        for (int i = 0; i < alphabets.length(); i++) {
            int index = random.nextInt(alphabets.length());
            char temp = alphabets.charAt(i);
            alphabets.setCharAt(i, alphabets.charAt(index));
            alphabets.setCharAt(index, temp);
        }

        password.append(alphabets);

        // Append a special character
        char specialChar = "@#$%&*(){}[]".charAt(random.nextInt("@#$%&*(){}[]".length()));
        password.append(specialChar);

        // Generate the next three characters (integers)
        StringBuilder integers = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            int digit = random.nextInt(10); // Generates a random digit (0-9)
            integers.append(digit);
        }

        // Shuffle integer characters
        for (int i = 0; i < integers.length(); i++) {
            int index = random.nextInt(integers.length());
            char temp = integers.charAt(i);
            integers.setCharAt(i, integers.charAt(index));
            integers.setCharAt(index, temp);
        }

        password.append(integers);

        // Shuffle the characters in the password
//        for (int i = 0; i < password.length(); i++) {
//            int index = random.nextInt(password.length());
//            char temp = password.charAt(i);
//            password.setCharAt(i, password.charAt(index));
//            password.setCharAt(index, temp);
//        }

        return password.toString();
    }


    /*
     * AMIT KUMAR SINGH
     * This method adds a staff member to a company with validation checks.
     * It verifies the user's credentials, checks for existing staff with the same phone number or email,
     * and inserts the new staff member into the database if all validations pass.
     *
     * @param request A map containing deviceId, deviceType, appVersion, userId, tenantId, staffFirstName,
     *                staffLastName, staffEmail, staffMobile, staffType, companyId, chargeRate, and chargeFrequency
     */

    @Override
    public ResponseEntity<GlobalResponseDTO> addStaff(Map<String, String> request, Principal principal) {

        String deviceId = request.get("deviceId");
        String deviceType = request.get("deviceType");
        String appVersion = request.get("appVersion");
        String userId = request.get("userId");
        String tenantId = request.get("tenantId");
        if(checkUserMatch(userId,tenantId, principal.getName())){
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorised user, we could not access the APIs from others token "));
        }
        String staffFirstName = request.get("staffFirstName");
        String staffLastName = request.get("staffLastName");
        String staffEmail = request.get("staffEmail");
        String staffMobile = request.get("staffMobile");
        String staffType = request.get("staffType");
        String companyId = request.get("companyId");
        String chargeRate = request.get("chargeRate");
        String chargeFrequency = request.get("chargeFrequency");
        String staffStatus = "1";
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.accepted()
                    .body(new GlobalResponseDTO(false, "User Id is blank or null", null));
        }
        String formattedStaff = staffMobile.replaceAll("[\\s()-]", "");
        List<String> fetchCompanyNameByPhNo = companyMasterRepository.fetchCompanyNameByPhNo(formattedStaff);
        if (!fetchCompanyNameByPhNo.isEmpty()) {
            for (String company : fetchCompanyNameByPhNo) {
                if (company.trim().equalsIgnoreCase(tenantId)) {
                    return ResponseEntity.accepted()
                            .body(new GlobalResponseDTO(false, "You cannot be added as a staff in your own company", null));
                }
            }
        }
        if (companyId == null || companyId.isBlank()) {
            return ResponseEntity.accepted()
                    .body(new GlobalResponseDTO(false, "Company Id is blank or null", null));
        }
        if (tenantId == null || tenantId.isEmpty()) {
                return ResponseEntity.accepted().body(new GlobalResponseDTO(false, "Please enter the business Id"));
        }
        String responseTenantId = tenantId + "st";
        String password = "Abcd@123";
        AES obj = new AES();
        String encryptedPassword = obj.encrypt(password);
        CheckSchema schemaService = new CheckSchema(jdbcTemplate);
        boolean schemaExists = schemaService.doesSchemaExist(tenantId);
        if (!schemaExists) {
            return ResponseEntity.accepted()
                    .body(new GlobalResponseDTO(false, "invalid Business Id ", null));

        }
        String userCount = staffQuery.userCount(tenantId, companyId);
        String strQueryForValidateStaffEmail = "SELECT \"USER_EMAIL\" FROM \"" + tenantId + "\".\"company_user\" WHERE \"USER_EMAIL\" = ?";
        List<Map<String, Object>> rowsForValidateStaffEmail = jdbcTemplate.queryForList(strQueryForValidateStaffEmail, staffEmail);
        String strQueryForValidateStaffPhNo = "SELECT \"USER_EMAIL\" FROM \"" + tenantId + "\".\"company_user\" WHERE  \"USER_PHONE_NUMBER\" = ?";
        List<Map<String, Object>> rowsForValidateStaffPhNo = jdbcTemplate.queryForList(strQueryForValidateStaffPhNo, formattedStaff);
        String strQueryToValidateStaffExistence = "SELECT \"USER_FIRST_NAME\" FROM \"" + tenantId + "\".\"company_user\" WHERE  \"USER_PHONE_NUMBER\" = ?";
        List<Map<String, Object>> rowsToValidateStaffExistence = jdbcTemplate.queryForList(strQueryToValidateStaffExistence, formattedStaff);
        if (!rowsToValidateStaffExistence.isEmpty()) {
            return ResponseEntity.accepted()
                    .body(new GlobalResponseDTO(false, "A staff is already added with this phone number.", null));

        }
        if (!rowsForValidateStaffEmail.isEmpty() && !staffEmail.isEmpty()) {
            return ResponseEntity.accepted()
                    .body(new GlobalResponseDTO(false, "EmailId Already Exists", null));

        }
        if (!rowsForValidateStaffPhNo.isEmpty()) {
            return ResponseEntity.accepted()
                    .body(new GlobalResponseDTO(false, "Phone number Already Exists", null));

        }
        String fetchCompanyName = companyMasterRepository.fetchCompanyName(Integer.valueOf(companyId));
        List<JSONObject> phoneNumberRegisteredWithBusiness = companyMasterRepository.getPhoneNumberRegisteredWithBusiness(fetchCompanyName);
        for(JSONObject businessPhoneNumber : phoneNumberRegisteredWithBusiness){
            if(staffMobile.equals(businessPhoneNumber.get("COMPANY_BACKUP_PHONE_NUMBER"))){
                return ResponseEntity.accepted()
                        .body(new GlobalResponseDTO(false, "The mobile number " + staffMobile + " is already registed with company " + fetchCompanyName + "", null));
            }
        }
        staffMobile=staffMobile.replaceAll("[^\\d]", "");
        int companyIdInt = Integer.parseInt(companyId);
        int staffTypeInd = Integer.parseInt(staffType);
        String staffInsertQuery = "INSERT INTO \"" + tenantId + "\".\"company_user\" (\n" +
                "    \"PK_USER_ID\",\n" +
                "    \"FK_COMPANY_ID\",\n" +
                "    \"USER_FIRST_NAME\",\n" +
                "    \"USER_LAST_NAME\",\n" +
                "    \"USER_EMAIL\",\n" +
                "    \"FK_USER_TYPE_ID\",\n" +
                "    \"USER_PASSWORD\",\n" +
                "    \"USER_JOINING_DATE\",\n" +
                "    \"USER_CHARGE_RATE\",\n" +
                "    \"USER_CHARGE_FREQUENCY\",\n" +
                "    \"USER_PHONE_NUMBER\",\n" +
                "    \"USER_CREATED_AT\",\n" +
                "    \"USER_UPDATED_AT\",\n" +
                "    \"PASSWORD_CHANGE\",\n" +
                "    \"USER_STATUS\"\n" +
                ")\n" +
                "VALUES (\n" +
                "    COALESCE((SELECT MAX(\"PK_USER_ID\") FROM \"" + tenantId + "\".\"company_user\"), 0) + 1,\n" +
                "    ?,\n" +
                "    ?,\n" +
                "    ?,\n" +
                "    ?,\n" +
                "    ?,\n" +
                "    ?,\n" +
                "    current_timestamp,\n" +
                "    ?,\n" +
                "    ?,\n" +
                "    ?,\n" +
                "    current_timestamp,\n" +
                "    current_timestamp,\n" +
                "    'N',\n" +
                "    ?\n" +
                ") RETURNING \"PK_USER_ID\"";
        Object[] params = new Object[] {
                Integer.parseInt(companyId),
                staffFirstName,
                staffLastName,
                staffEmail,
                Integer.parseInt(staffType),
                encryptedPassword,
                Double.parseDouble(chargeRate),
                Integer.parseInt(chargeFrequency),
                staffMobile,
                staffStatus
        };
        Long insertStaffId = jdbcTemplate.queryForObject(staffInsertQuery, params, Long.class);
        String userType = "Staff";
        registrationRepository.insertBusinessIdData(staffMobile, staffEmail, fetchCompanyName, tenantId, userType);
        Map<String, Object> response = new HashMap<>();
        response.put("staffId", insertStaffId);
        response.put("staffEmail", staffEmail);
        response.put("staffMobile", staffMobile);
        response.put("staffType", staffType);
        response.put("companyId", companyId);
        response.put("staffFirstName", staffFirstName);
        response.put("staffLastName", staffLastName);
        response.put("chargeRate", chargeRate);
        response.put("chargeFrequency", chargeFrequency);
        response.put("tenantId", tenantId);
        return ResponseEntity.accepted()
                .body(new GlobalResponseDTO(true, "Your temporary password is " + password + "", response));
    }

    /*
     * AMIT KUMAR SINGH
     * This method sends an OTP for pre-registration verification based on userId and emailId provided.
     * It checks if the mobile number and email are already registered. If not, it generates an OTP,
     * sends it via email, and updates the OTP in the database.
     *
     * @param request A map containing userId and emailId for OTP generation
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> preregistrationSendOtp(Map<String, String> request) {
        String userId = request.get("userId");
        String emailId = request.get("emailId");


        List<JSONObject> fetchUserMobileNoValidation = companyMasterRepository.fetchUserMobileNoValidation(userId);

        if (!fetchUserMobileNoValidation.isEmpty()) {
            return ResponseEntity.accepted()
                    .body(new GlobalResponseDTO(false, "Mobile Number already exists", null));

        }


        List<JSONObject> fetchUserEmailIdValidation = companyMasterRepository.fetchUserEmailIdValidation(emailId);

        if (!fetchUserEmailIdValidation.isEmpty()) {
            return ResponseEntity.accepted()
                    .body(new GlobalResponseDTO(false, "Email Id already exists", null));

        }


        String convertedUserId = userId.substring(0, 2) + userId.substring(2, userId.length() - 2).replaceAll(".", "*") + userId.substring(userId.length() - 2);
        String convertedUserIdMessage = "We have sent you verification code on" + " " + convertedUserId;


        Random random = new Random();
        int randomNumber = random.nextInt(900000) + 100000;
        String strRandomNumber = String.valueOf(randomNumber);


        String otpMailSub = "Bizfns";
        String otpMailBody = "Your one time password is : " + randomNumber;


/*
        if (emailId != null) {
            emailSenderService.sendSimpleEmail(emailId, otpMailSub, otpMailBody);
        }
*/


        List<JSONObject> fetchRegOtpExistence = companyMasterRepository.fetchRegOtpExistence(userId);


        if (fetchRegOtpExistence.isEmpty()) {
            companyMasterRepository.insertRegOtp(strRandomNumber, userId);

        } else {
            companyMasterRepository.updateRegOtp(strRandomNumber, userId);


        }

        Map<String, Object> response = new HashMap<>();
        response.put("otp", strRandomNumber);
        response.put("otp_message", convertedUserIdMessage);


        return ResponseEntity.accepted()
                .body(new GlobalResponseDTO(true, "Success", response));
    }

    /*
     * AMIT KUMAR SINGH
     * This endpoint verifies the OTP provided during pre-registration.
     * It validates the OTP against the userId and checks its expiration status.
     *
     * @param request A map containing userId and otp for OTP verification
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> preregistrationOtpVerification(Map<String, String> request) {
        String userId = request.get("userId");
        String otp = request.get("otp");

        String otpValidation = companyMasterRepository.otpValidation(userId);


        String otpExpiration = companyMasterRepository.otpExpiration(userId);
        if (otpValidation.equals(otp)) {

            if (otpExpiration.equalsIgnoreCase("y")) {

                return ResponseEntity.accepted()
                        .body(new GlobalResponseDTO(true, "success", null));

            } else {

                return ResponseEntity.accepted()
                        .body(new GlobalResponseDTO(false, "Otp expire", null));
            }

        } else {
            return ResponseEntity.accepted()
                    .body(new GlobalResponseDTO(false, "Otp not match", null));
        }


    }


    /**
     * AMIT KUMAR SINGH
     * Method to test dynamic schema and table creation based on OTP and random number.
     *
     * @param request A map containing otp for schema name generation
     * @return ResponseEntity containing success message and generated schema name
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> testFor(Map<String, String> request) {
        String otp = request.get("otp");

        Random random = new Random();
        int randomNumber = random.nextInt(900000) + 100000;
        String strRandomNo = String.valueOf(randomNumber);

        String b = otp + strRandomNo;

        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();

            statement.executeUpdate("CREATE SCHEMA IF NOT EXISTS " + b);


            String combinedQuery =
                    "CREATE TABLE " + b + ".ASSIGNED_JOB (LIKE \"Bizfns\".\"ASSIGNED_JOB\" INCLUDING CONSTRAINTS);" +
                            "ALTER TABLE " + b + ".ASSIGNED_JOB ADD CONSTRAINT \"PK_ASSIGNED_JOB_ID\" PRIMARY KEY (\"PK_ASSIGNED_JOB_ID\")";

            statement.executeUpdate(combinedQuery);


            statement.close();
        } catch (SQLException e) {
        }

        return ResponseEntity.accepted()
                .body(new GlobalResponseDTO(true, "test", b));
    }


    /**
     * This method is designed to update business email after OTP verification.
     *
     * @param request   A map containing userId, companyId, currentEmail, newEmail, tenantId
     * @param principal Principal object containing authenticated user details
     * @return ResponseEntity containing success or failure response
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> otpUpdateBusinessEmail(Map<String, String> request, Principal principal) {

        String deviceId = request.get("deviceId");
        String deviceType = request.get("deviceType");
        String appVersion = request.get("appVersion");
        String userId = request.get("userId");
        String companyId = request.get("companyId");
        String currentEmail = request.get("currentEmail");
        String newEmail = request.get("newEmail");
        String tenantId = request.get("tenantId");
        String otpTimeStamp = null;

        if(checkUserMatch(userId,tenantId, principal.getName())){
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorised user, we could not access the APIs from others token "));
        }


        String deviceInfo = "deviceId = " + deviceId + "\n" + "deviceType = " + deviceType + "\n" + "appVersion = " + appVersion;

        String fullURL = httpServletRequest.getRequestURL().toString();

        try {

            List<JSONObject> checkMailExistence = null;
            checkMailExistence = companyMasterRepository.checkMailExistence(Integer.valueOf(companyId));


            String email = null;
            if (!checkMailExistence.isEmpty()) {
                for (JSONObject jsonObject : checkMailExistence) {
                    email = (String) jsonObject.get("coalesce");

                }
            }

            if (email.equalsIgnoreCase(currentEmail) || email.equalsIgnoreCase("n")) {


                Random random = new Random();
                int randomNumber = random.nextInt(900000) + 100000;


                String convertedUserId = userId.substring(0, 2) + userId.substring(2, userId.length() - 2).replaceAll(".", "*") + userId.substring(userId.length() - 2);
                String convertedUserIdMessage = "We have sent you verification code on" + " " + convertedUserId;


                List<JSONObject> checkOtpExistence = companyMasterRepository.checkOtpExistence(userId, tenantId);
                if (checkOtpExistence.isEmpty()) {

                    otpTimeStamp = companyMasterRepository.insertFirstLoginOtp(String.valueOf(randomNumber), userId, tenantId);

                } else {
                    otpTimeStamp = companyMasterRepository.updateLoginOtp(String.valueOf(randomNumber), userId,tenantId);
                }

                Map<String, String> otpData = new HashMap<>();
                otpData.put("OTP", String.valueOf(randomNumber));
                otpData.put("otpTimeStamp", otpTimeStamp);
                otpData.put("message", convertedUserIdMessage);

                return ResponseEntity.accepted()
                        .body(new GlobalResponseDTO(true, "OTP", otpData));


            }

        } catch (Exception e) {
            errorLogService.errorLog(request, e.getMessage(), fullURL, userId, deviceInfo);

            return ResponseEntity.accepted().body(new GlobalResponseDTO(false, "Success", null));

        }

        return ResponseEntity.accepted()
                .body(new GlobalResponseDTO(false, "Failure", "Current Email not match"));


    }
    /*
     * AMIT KUMAR SINGH
     * This method verifies the OTP provided against the OTP stored in the database,
     * and updates the user's email if the OTP is valid and not expired.
     *
     * @param request A map containing the request parameters including deviceId, deviceType, appVersion,
     *                userId, companyId, currentEmail, newEmail, tenantId, and otp.
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> validateOtpUpdateBusinessEmail(Map<String, String> request, Principal principal) {

        String deviceId = request.get("deviceId");
        String deviceType = request.get("deviceType");
        String appVersion = request.get("appVersion");
        String userId = request.get("userId");
        String companyId = request.get("companyId");
        String currentEmail = request.get("currentEmail");
        String newEmail = request.get("newEmail");
        String tenantId = request.get("tenantId");
        String otp = request.get("otp");
        //    String otpTimeStamp = null;

        if(checkUserMatch(userId,tenantId, principal.getName())){
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorised user, we could not access the APIs from others token "));
        }

        String deviceInfo = "deviceId = " + deviceId + "\n" + "deviceType = " + deviceType + "\n" + "appVersion = " + appVersion;

        String fullURL = httpServletRequest.getRequestURL().toString();

        try {
            String fetchOtp = companyMasterRepository.getDbOtp(Integer.valueOf(companyId));
            String isOtpExpire = companyMasterRepository.isOtpExpireForEmail(Integer.valueOf(companyId));


            if (otp.equals(fetchOtp)) {
                if (isOtpExpire.equalsIgnoreCase("y")) {

                    companyMasterRepository.refreshTokenForEmail(Integer.valueOf(userId), tenantId);
                    companyMasterRepository.changeUserEmail(Integer.valueOf(companyId), tenantId, newEmail);
                    companyMasterRepository.changeMasterEmail(Integer.valueOf(companyId), newEmail);


                    List<JSONObject> companyDetails = companyMasterRepository.fetchCompDetails(Integer.valueOf(companyId));

                    JSONObject companyInfo = new JSONObject();
                    JSONObject contactPersonDetails = new JSONObject();

                    // Assuming that the query returns only one row, as companyId should be unique
                    JSONObject company = companyDetails.get(0);

                    // Populate the JSON object with the fetched data
                    companyInfo.put("companyId", company.get("COMPANY_ID"));
                    companyInfo.put("companyName", company.get("BUSINESS_NAME"));
                    companyInfo.put("companyMobileNo", company.get("COMPANY_BACKUP_PHONE_NUMBER"));
                    companyInfo.put("companyEmail", company.get("COMPANY_BACKUP_EMAIL"));

                    contactPersonDetails.put("contactPersonEmail", "");  // Set contact person email as needed
                    contactPersonDetails.put("contactPersonPhoneNo", ""); // Set contact person phone number as needed

                    companyInfo.put("companyContactPersonDetails", contactPersonDetails);


                    return ResponseEntity.accepted()
                            .body(new GlobalResponseDTO(true, "success", companyInfo));

                } else {
                    return ResponseEntity.accepted()
                            .body(new GlobalResponseDTO(false, "otp expire", null));
                }
            } else {
                return ResponseEntity.accepted()
                        .body(new GlobalResponseDTO(false, "otp not match", null));
            }

        }

        catch (Exception e) {
            errorLogService.errorLog(request, e.getMessage(), fullURL, userId, deviceInfo);

            return ResponseEntity.accepted().body(new GlobalResponseDTO(false, "Success", null));

        }
    }

    @Override
    public ResponseEntity<GlobalResponseDTO> getActiveStatusForStaff(Map<String, String> request, Principal principal) {
        String tenantId = request.get("tenantId");
        String userId = request.get("userId");
        String staffPhoneNumber = request.get("staffPhoneNumber");
        if (checkUserMatch(userId, tenantId, principal.getName())) {
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorised user, we could not access the APIs from others token "));
        }
        try {
            Map<String, Object> getActiveStatusData = staffQuery.getActiveStatusForStaff(tenantId,staffPhoneNumber );
            return ResponseEntity.accepted().body(new GlobalResponseDTO(true, "Success", getActiveStatusData));
        } catch (Exception e) {
            return ResponseEntity.accepted().body(new GlobalResponseDTO(false, "Failed", e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<GlobalResponseDTO> updateActiveInactiveStatusForStaff(Map<String, String> request, Principal principal) {
        try {
            String tenantId = request.get("tenantId");
            String userId = request.get("userId");
            String staffPhoneNumber = request.get("staffPhoneNumber");
            String staffActiveInactiveStatus = request.get("staffActiveInactiveStatus");
            if (checkUserMatch(userId, tenantId, principal.getName())) {
                return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorized user, we could not access the APIs from others token "));
            }
            String result = staffQuery.updateActiveInactiveStatusForStaff(tenantId, staffPhoneNumber, staffActiveInactiveStatus);
            if (result.contains("successfully")) {
                return ResponseEntity.accepted().body(new GlobalResponseDTO(true, "Success", result));
            } else {
                return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Failed to update status: " + result));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GlobalResponseDTO(false, "Error occurred while updating staff status"));
        }
    }

    @Override
    public ResponseEntity<GlobalResponseDTO> deleteStaff(Map<String, String> request, Principal principal) {
        try {
            String tenantId = request.get("tenantId");
            String userId = request.get("userId");
            String staffPhoneNumber = request.get("staffPhoneNumber");
            if (checkUserMatch(userId, tenantId, principal.getName())) {
                return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorized user, we could not access the APIs from others token "));
            }
            staffQuery.deletestaffFromDB(tenantId, staffPhoneNumber);
            return ResponseEntity.accepted().body(new GlobalResponseDTO(true, "Success", "Staff Name has been deleted successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GlobalResponseDTO(false, "Failed to delete staff: " + e.getMessage()));
        }
    }

    public ResponseEntity<GlobalResponseDTO> getStaffDetails(Map<String, String> request, Principal principal) {
        try {
            String tenantId = request.get("tenantId");
            String userId = request.get("userId");
            String staffPhoneNumber = request.get("staffPhoneNumber");
            if (checkUserMatch(userId, tenantId, principal.getName())) {
                return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorized user, we could not access the APIs from others token"));
            }
            if (staffPhoneNumber == null || staffPhoneNumber.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Please enter staff phone number", null));
            }
            Map<String, Object> getStaffDtlsFromDB = staffQuery.getStaffDtlsFromDB(tenantId, staffPhoneNumber);
            if (getStaffDtlsFromDB != null && !getStaffDtlsFromDB.isEmpty() /*&&
                    getStaffDtlsFromDB.values().stream().noneMatch(value -> value == null)*/) {
                return ResponseEntity.accepted().body(new GlobalResponseDTO(true, "Staff details retrieved successfully", getStaffDtlsFromDB));
            }
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Staff with phone number " + staffPhoneNumber + " does not exist.", getStaffDtlsFromDB));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GlobalResponseDTO(false, "Failed to retrieve staff details: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<GlobalResponseDTO> updateStaffDetails(Map<String, Object> request, Principal principal) {
        try {
            String tenantId = (String) request.get("tenantId");
            String userId = (String) request.get("userId");
            String staffId = (String) request.get("staffId");
            if (checkUserMatch(userId, tenantId, principal.getName())) {
                return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorized user, we could not access the APIs from others token "));
            }
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            for (GrantedAuthority authority : authorities) {
                String authorityName = authority.getAuthority();
                List<String> priviledgeChk = staffQuery.priviledgeChkForStaff(tenantId);
                boolean hasEditPrivilege = false;
                for (String privilege : priviledgeChk) {
                    if (privilege.equalsIgnoreCase("EDIT")) {
                        hasEditPrivilege = true;
                        break;
                    }
                }
                if (authorityName.equalsIgnoreCase("Staff") && !hasEditPrivilege) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new GlobalResponseDTO(false, "A Staff user dont have the priviledge to edit the staff details.", null));
                }
            }
            boolean isUpdated = staffQuery.updateStaffDetailsInDB(tenantId, request);
            if (isUpdated) {
                return ResponseEntity.ok(new GlobalResponseDTO(true, "Staff details updated successfully"));
            } else {
                return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Failed to update staff details"));
            }
        } catch (RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new GlobalResponseDTO(false, "Failed to update staff details: " + e.getMessage()));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new GlobalResponseDTO(false, "Failed to update staff details: " + e.getMessage()));
    }
    }

    @Override
    public ResponseEntity<GlobalResponseDTO> getUserTypeAndUserInfo(Principal principal) {
        String userInfo = principal.getName();
        String[] parts = userInfo.split(",");
        String tenantId = parts[1];
        List<Map<String, Object>> userTypeAndUsersList = new ArrayList<>();
        List<Map<String, Object>> userMasterDataList = getUserTypeAndMobileByTenant(tenantId);
        if (userMasterDataList == null || userMasterDataList.isEmpty()) {
            return ResponseEntity.ok(new GlobalResponseDTO(true, "No users found for the tenant", null));
        }
        Map<String, List<Map<String, Object>>> usersByType = new HashMap<>();
        for (Map<String, Object> userMasterData : userMasterDataList) {
            String userType = (String) userMasterData.get("USER_TYPE");
            String mobileNumber = (String) userMasterData.get("MOBILE_NUMBER");
            if ("Company".equalsIgnoreCase(userType)) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("user_id", userMasterData.get("ID"));
                userMap.put("user_name", userMasterData.get("BUSINESS_NAME"));
                userMap.put("phoneNumber", mobileNumber);
                usersByType.computeIfAbsent(userType, k -> new ArrayList<>()).add(userMap);
            } else if ("Staff".equalsIgnoreCase(userType)) {
                List<Map<String, Object>> staffDetailsList = getStaffDetailsByMobileNumber(tenantId, mobileNumber);
                for (Map<String, Object> staffDetails : staffDetailsList) {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("user_id", staffDetails.get("PK_USER_ID"));
                    userMap.put("phoneNumber", staffDetails.get("USER_PHONE_NUMBER"));
                    userMap.put("user_name", staffDetails.get("USER_FIRST_NAME") + " " + staffDetails.get("USER_LAST_NAME"));
                    usersByType.computeIfAbsent(userType, k -> new ArrayList<>()).add(userMap);
                }
            }
        }
        for (Map.Entry<String, List<Map<String, Object>>> entry : usersByType.entrySet()) {
            Map<String, Object> userTypeAndUsers = new HashMap<>();
            userTypeAndUsers.put("user_type", entry.getKey());
            userTypeAndUsers.put("users", entry.getValue());
            userTypeAndUsersList.add(userTypeAndUsers);
        }
        GlobalResponseDTO response = new GlobalResponseDTO(true, "User info fetched successfully", userTypeAndUsersList);
        return ResponseEntity.ok(response);
    }


    public List<Map<String, Object>> getUserTypeAndMobileByTenant(String tenantId){
        String query = "SELECT \"USER_TYPE\", \"MOBILE_NUMBER\", \"ID\", \"BUSINESS_NAME\" FROM \"Bizfns\".\"USER_MASTER\" WHERE \"SCHEMA_NAME\" = ?";
        return jdbcTemplate.queryForList(query, tenantId);
    }
    public List<Map<String, Object>> getStaffDetailsByMobileNumber(String tenantId,String mobileNumber) {
        String query = "SELECT \"PK_USER_ID\", \"USER_FIRST_NAME\", \"USER_LAST_NAME\", \"USER_PHONE_NUMBER\" FROM \"" + tenantId + "\".company_user WHERE \"USER_PHONE_NUMBER\" = ?";
        return jdbcTemplate.queryForList(query, mobileNumber);
    }
    public void triggerMail(String toMail, String mailSubject, String mailBody) throws MessagingException {
        emailSenderService.sendSimpleEmail(toMail,
                mailSubject,
                mailBody);

    }

    private void createSchemaForUser(String username) {
        String[] tableCreationQueries = {
                "CREATE SCHEMA IF NOT EXISTS " + username,
                "CREATE TABLE " + username + ".ASSIGNED_JOB (LIKE \"Bizfns\".\"ASSIGNED_JOB\" INCLUDING CONSTRAINTS)",
                "CREATE TABLE " + username + ".COMPANY_CUSTOMER (LIKE \"Bizfns\".\"COMPANY_CUSTOMER\" INCLUDING CONSTRAINTS)",
                "CREATE TABLE " + username + ".COMPANY_SECURITY_QUESTION_ANSWER (LIKE \"Bizfns\".\"COMPANY_SECURITY_QUESTION_ANSWER\" INCLUDING CONSTRAINTS)",
                "CREATE TABLE " + username + ".COMPANY_USER_SHIFT (LIKE \"Bizfns\".\"COMPANY_USER_SHIFT\" INCLUDING CONSTRAINTS)",
                "CREATE TABLE " + username + ".COMPANY_USER (LIKE \"Bizfns\".\"COMPANY_USER\" INCLUDING CONSTRAINTS)",
                "CREATE TABLE " + username + ".CUSTOMER_NOTIFICATION_HISTORY (LIKE \"Bizfns\".\"CUSTOMER_NOTIFICATION_HISTORY\" INCLUDING CONSTRAINTS)",
                "CREATE TABLE " + username + ".CUSTOMER_OTP (LIKE \"Bizfns\".\"CUSTOMER_OTP\" INCLUDING CONSTRAINTS)",
                "CREATE TABLE " + username + ".COMPANY_TNC (LIKE \"Bizfns\".\"COMPANY_TNC\" INCLUDING CONSTRAINTS)",
                "CREATE TABLE " + username + ".INVOICE_MASTER (LIKE \"Bizfns\".\"INVOICE_MASTER\" INCLUDING CONSTRAINTS)",
                "CREATE TABLE " + username + ".JOB_MASTER (LIKE \"Bizfns\".\"JOB_MASTER\" INCLUDING CONSTRAINTS)",
                "CREATE TABLE " + username + ".PASSWORD_HISTORY (LIKE \"Bizfns\".\"PASSWORD_HISTORY\" INCLUDING CONSTRAINTS)",
                "CREATE TABLE " + username + ".MEDIA (LIKE \"Bizfns\".\"MEDIA\" INCLUDING CONSTRAINTS)",
                "CREATE TABLE " + username + ".COMPANY_BUSINESS_TYPE_FORM_DETAILS (LIKE \"Bizfns\".\"COMPANY_BUSINESS_TYPE_FORM_DETAILS\" INCLUDING CONSTRAINTS)",
                "CREATE TABLE " + username + ".COMPANY_BUSINESS_TYPE_MAPPING (LIKE \"Bizfns\".\"COMPANY_BUSINESS_TYPE_MAPPING\" INCLUDING CONSTRAINTS)",
                "CREATE TABLE " + username + ".PAYMENT_TRANSACTION (LIKE \"Bizfns\".\"PAYMENT_TRANSACTION\" INCLUDING CONSTRAINTS)",
                "CREATE TABLE " + username + ".PRIVILEGE_MAPPING (LIKE \"Bizfns\".\"PRIVILEGE_MAPPING\" INCLUDING CONSTRAINTS)",
                "CREATE TABLE " + username + ".PRIVILEGE (LIKE \"Bizfns\".\"PRIVILEGE\" INCLUDING CONSTRAINTS)",
                "CREATE TABLE " + username + ".TAX_MASTER (LIKE \"Bizfns\".\"TAX_MASTER\" INCLUDING CONSTRAINTS)",
                "CREATE TABLE " + username + ".TERMS_AND_CONDITION (LIKE \"Bizfns\".\"TERMS_AND_CONDITION\" INCLUDING CONSTRAINTS)",
                "CREATE TABLE " + username + ".USER_OTP (LIKE \"Bizfns\".\"USER_OTP\" INCLUDING CONSTRAINTS)",
                "CREATE TABLE " + username + ".INVOICE (LIKE \"Bizfns\".\"INVOICE\" INCLUDING CONSTRAINTS)",
                "CREATE TABLE " + username + ".INVOICE_ITEM (LIKE \"Bizfns\".\"INVOICE_ITEM\" INCLUDING CONSTRAINTS)",
                "CREATE TABLE " + username + ".INVOICE_MASTER (LIKE \"Bizfns\".\"INVOICE_MASTER\" INCLUDING CONSTRAINTS)",
                "CREATE TABLE " + username + ".INVOICE_TAXES (LIKE \"Bizfns\".\"INVOICE_TAXES\" INCLUDING CONSTRAINTS)",
                "CREATE TABLE " + username + ".reminder (LIKE \"Bizfns\".\"reminder\" INCLUDING CONSTRAINTS)",
                "CREATE TABLE " + username + ".BUSINESS_TYPE_WISE_SERVICE_MASTER (LIKE \"Bizfns\".\"BUSINESS_TYPE_WISE_SERVICE_MASTER\" INCLUDING CONSTRAINTS)",
                "CREATE TABLE " + username + ".SERVICE_ENTITY_ANSWER_TYPE_MASTER (LIKE \"Bizfns\".\"SERVICE_ENTITY_ANSWER_TYPE_MASTER\" INCLUDING CONSTRAINTS)",
                "CREATE TABLE " + username + ".SERVICE_MASTER (LIKE \"Bizfns\".\"SERVICE_MASTER\" INCLUDING CONSTRAINTS)",
                "CREATE TABLE " + username + ".MATERIAL_CATEGORY_MASTER (LIKE \"Bizfns\".\"MATERIAL_CATEGORY_MASTER\" INCLUDING CONSTRAINTS)",
                "CREATE TABLE " + username + ".MATERIAL_SUBCATEGORY_MASTER (LIKE \"Bizfns\".\"MATERIAL_SUBCATEGORY_MASTER\" INCLUDING CONSTRAINTS)",
                "CREATE TABLE " + username + ".MATERIAL_RATE_MASTER (LIKE \"Bizfns\".\"MATERIAL_RATE_MASTER\" INCLUDING CONSTRAINTS)",
                "CREATE TABLE " + username + ".JOB_WISE_SERVICE_ENTITY_MASTER (LIKE \"Bizfns\".\"JOB_WISE_SERVICE_ENTITY_MASTER\" INCLUDING CONSTRAINTS)",
                "CREATE TABLE " + username + ".JOB_WISE_SERVICE_MAPPING (LIKE \"Bizfns\".\"JOB_WISE_SERVICE_MAPPING\" INCLUDING CONSTRAINTS)",
                "CREATE TABLE " + username + ".MATERIAL_MASTER (LIKE \"Bizfns\".\"MATERIAL_MASTER\" INCLUDING CONSTRAINTS)",
                "CREATE TABLE " + username + ".BUSINESS_TYPE_FORM_ENTITIES AS SELECT * FROM \"Bizfns\".\"BUSINESS_TYPE_FORM_ENTITIES\"",
                "CREATE TABLE " + username + ".CUSTOMER_SERVICE_ENTITY_MAPPING (LIKE \"Bizfns\".\"CUSTOMER_SERVICE_ENTITY_MAPPING\" INCLUDING CONSTRAINTS)",
                "CREATE TABLE " + username + ".CUSTOMER_WISE_SERVICE_ENTITY (LIKE \"Bizfns\".\"CUSTOMER_WISE_SERVICE_ENTITY\" INCLUDING CONSTRAINTS)",
                "CREATE TABLE " + username + ".material_subcategory_master (LIKE \"Bizfns\".\"material_subcategory_master\" INCLUDING CONSTRAINTS)",
                "CREATE TABLE " + username + ".JOB_WISE_CUSTOMER_AND_SERVICEENTITY_MAPPING (LIKE \"Bizfns\".\"JOB_WISE_CUSTOMER_AND_SERVICEENTITY_MAPPING\" INCLUDING CONSTRAINTS)",
                "CREATE TABLE " + username + ".REMINDER_EVENT_DETAILS (LIKE \"Bizfns\".\"REMINDER_EVENT_DETAILS\" INCLUDING CONSTRAINTS)",
                "CREATE TABLE " + username + ".TIMESHEET (LIKE \"Bizfns\".\"TIMESHEET\" INCLUDING CONSTRAINTS)"
        };

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            for (String query : tableCreationQueries) {
                try {
                    statement.executeUpdate(query);
                } catch (SQLException e) {
                    System.err.println("Error executing query: " + query);
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static String generateRandomPassword(int length) {
        final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            password.append(CHARACTERS.charAt(randomIndex));
        }
        return password.toString();
    }

    public boolean checkUserMatch(String userId,String tenantId , String username){

        String[] part = username.split(",");

        //String userPhone = profileRepository.checkAccessToken(userId, tenantId);


        String tokenUserId = part[0];
        String tokenTenantId = part[1];
        if ( !tenantId.equals(tokenTenantId)){
            return true;
        }

        return false;
    }


}
