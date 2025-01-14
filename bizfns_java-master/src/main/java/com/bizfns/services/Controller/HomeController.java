package com.bizfns.services.Controller;

//import com.dailycodebuffer.jwt.model.JwtRequest;
//import com.dailycodebuffer.jwt.model.JwtResponse;
//import com.dailycodebuffer.jwt.service.UserService;
//import com.dailycodebuffer.jwt.utility.JWTUtility;
// ... (import statements)

import com.bizfns.services.GlobalDto.GlobalResponseDTO;
import com.bizfns.services.Query.StaffAuthQuery;
import com.bizfns.services.Query.StaffQuery;
import com.bizfns.services.Repository.AdminRepository;
import com.bizfns.services.Repository.CompanyMasterRepository;
import com.bizfns.services.Repository.CompanyUserRepository;
import com.bizfns.services.Repository.RegistrationRepository;
import com.bizfns.services.Serviceimpl.AES;
import com.bizfns.services.Serviceimpl.EmailSenderService;
import com.bizfns.services.Serviceimpl.UserService;
import com.bizfns.services.Utility.JWTUtility;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController

public class HomeController {
    // Autowired fields


    @Autowired
    private EmailSenderService emailSenderService;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private JWTUtility jwtUtility;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;
    @Autowired
    private RegistrationRepository registrationRepository;
    @Autowired
    private CompanyUserRepository companyUserRepository;
    @Autowired
    private StaffAuthQuery staffAuthQuery;
    @Autowired
    private CompanyMasterRepository companyMasterRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private StaffQuery staffQuery;

    // Method to check if a string is a valid email address
    public static boolean isEmail(String userId) {
        String emailPattern = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";

        return userId.matches(emailPattern);
    }

    // Method to check if a string is a valid phone number
    public static boolean isPhoneNumber(String userId) {
        String phonePattern = "\\d{10}";

        return userId.matches(phonePattern);
    }


    /*AMIT KUMAR SINGH
     * Authenticates the user based on the provided userId, password, and optional tenantId.
     * Handles authentication for both admin and staff users, generating OTP for login verification,
     * and issuing JWT tokens upon successful authentication.
     *
     * @param request A map containing the request parameters including userId, password, and optionally tenantId.
     */
    @PostMapping(EndpointPropertyKey.USER_LOGIN)
    public ResponseEntity<GlobalResponseDTO> authenticate(@RequestBody Map<String, String> request) {
        // Create an instance of AES
        try {
            AES obj = new AES();
            String userId = request.get("userId");
            String password = request.get("password");
            String tenantId = request.get("tenantId");
            String fcmId = request.get("fcmId");
            if (fcmId != null && !fcmId.isEmpty()) {
                String checkQuery = "SELECT COUNT(*) FROM \"Bizfns\".\"FCM_TOKEN\" WHERE \"USER_ID\" = ?";
                Integer count = jdbcTemplate.queryForObject(checkQuery, Integer.class, userId);
                if (count != null && count > 0) {
                    String updateQuery = "UPDATE \"Bizfns\".\"FCM_TOKEN\" SET \"FCM_TOKEN\" = ?, \"UPDATED_AT\" = CURRENT_TIMESTAMP WHERE \"USER_ID\" = ?";
                    jdbcTemplate.update(updateQuery, fcmId, userId);
                } else {
                    String insertQuery = "INSERT INTO \"Bizfns\".\"FCM_TOKEN\" (\"USER_ID\", \"DEVICE_ID\", \"FCM_TOKEN\", \"CREATED_AT\", \"UPDATED_AT\", \"TENANTID\") VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?)";
                    jdbcTemplate.update(insertQuery, userId, "1.0.17", fcmId, tenantId);
                }
            }
            String tokenData = null;
            String tokenDataForStaff = null;
            String executeQuery = null;
            String otpTimeStamp = null;

            String decryptedDbPassword = null;

            // Check for blank or null userId

            if (userId == null || userId.isBlank()) {
                return ResponseEntity.accepted()
                        .body(new GlobalResponseDTO(false, "User Id is blank or null", null));
            }

            // Check for blank or null password

            if (password == null || password.isBlank()) {
                return ResponseEntity.accepted()
                        .body(new GlobalResponseDTO(false, "password is blank or null", null));
            }
            // String decryptedUserPassword = obj.decrypt(password);



            /*
             * @Author Md Huzefa
             * This validation is used for admin login
             *
             * */
            if (tenantId == null || tenantId.isEmpty()) {

                if (!adminRepository.isUserTypeAdmin(userId)) {
                    return ResponseEntity.accepted().body(new GlobalResponseDTO(false, "Please enter the business Id"));

                }

                String tenantId1 = "adminOwn";
                // String dbCompBusinessId = companyUserRepository.dbCompBusinessId(userId);

                String lastTwoTLetters = null;
                String tenantFirstEightTLetters = null;

                if (tenantId1 != null && tenantId1.length() >= 2) {
                    lastTwoTLetters = tenantId1.substring(tenantId1.length() - 2);
                    tenantFirstEightTLetters = tenantId1.substring(0, 8);

                }

                String convertedUserId = userId.substring(0, 2) + userId.substring(2, userId.length() - 2).replaceAll(".", "*") + userId.substring(userId.length() - 2);
                String convertedUserIdMessage = "We have sent you verification code on" + " " + convertedUserId;

                String concatUserIdandTenantId = userId + "," + tenantId1;

                List<JSONObject> dataForTenantValidationFromCompanyMaster = companyUserRepository.dataForTenantValidationFromCompanyMaster(tenantFirstEightTLetters);

                if (dataForTenantValidationFromCompanyMaster.isEmpty()) {
                    return ResponseEntity.accepted().body(new GlobalResponseDTO(false, "Please enter correct Business Id ", null));
                }

                // Check if last two characters of tenantId are "st"

                String dbCompBusinessId = companyUserRepository.dbCompBusinessId(userId, tenantId1);
                if (dbCompBusinessId != null) {

                    if (!dbCompBusinessId.equalsIgnoreCase(tenantId1)) {
                        return ResponseEntity.accepted().body(new GlobalResponseDTO(false, "Please enter correct User Id", null));

                    }
                }

                // Proceed with your code using dbCompBusinessId
                else {
                }
                String dataForPasswordValidationFromCompanyMaster = companyUserRepository.dataForPasswordValidationFromCompanyMaster(userId, tenantId1);
                //String dataForPasswordValidationFromCompanyuser = staffAuthQuery.staffDbPassword(userId, tenantId1);
                if (dataForPasswordValidationFromCompanyMaster != null) {
//                decryptedDbPassword = obj.decrypt(dataForPasswordValidationFromCompanyuser);
//                executeQuery = "companyUser";
//             }
//            else

                    decryptedDbPassword = obj.decrypt(dataForPasswordValidationFromCompanyMaster);
                    executeQuery = "companyMaster";
                }
                if (password.equals(decryptedDbPassword)) {

                    Random random = new Random();
                    int randomNumber = random.nextInt(900000) + 100000;


                    JSONObject getMailId = companyMasterRepository.getMailId(userId, tenantId1);
                    String otpMailSub = "";
                    String otpMailBody = "";
                    String companyBackupEmail = "";
                    if (getMailId != null) {
                        // The JSONObject is not null, so proceed with sending the email.
                        otpMailSub = "Bizfns";
                        otpMailBody = "Your one-time password is: " + randomNumber;
                        companyBackupEmail = (String) getMailId.get("COMPANY_BACKUP_EMAIL");
                        if (companyBackupEmail != null) {
                            //emailSenderService.sendSimpleEmail(companyBackupEmail, otpMailSub, otpMailBody);
                        } else {
                            //companyBackupEmail = (String) getMailId.get("COMPANY_BACKUP_EMAIL");
                            // Handle the case when "COMPANY_BACKUP_EMAIL" is not found in the JSONObject
                        }
                    } else {
                        otpMailSub = tenantId1;
                        otpMailBody = "Your one-time password is: " + randomNumber;
                    }


                    if (executeQuery.equalsIgnoreCase("companyMaster")) {
                        List<JSONObject> checkOtpExistence = adminRepository.checkOtpExistence(userId);
                        if (checkOtpExistence.isEmpty()) {

                            otpTimeStamp = adminRepository.insertFirstLoginOtp(String.valueOf(randomNumber), userId, tenantId1);

                        } else {
                            otpTimeStamp = adminRepository.updateLoginOtp(String.valueOf(randomNumber), userId);
                        }
                    }
               /*
                else {
                    String userType = "staff";
                    String checkOtpExistence = staffQuery.checkOtpExistenceForStaff(userId);
                    if (checkOtpExistence == null || checkOtpExistence.isEmpty()) {
                        otpTimeStamp = companyMasterRepository.insertFirstLoginOtpForStaff(String.valueOf(randomNumber), userId, tenantId1, userType);

                    } else {
                        otpTimeStamp = companyMasterRepository.updateLoginOtpForStaff(String.valueOf(randomNumber), userId, userType);
                    }

                    try {
                        authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                        userId,
                                        password
                                )
                        );

                    } catch (Exception e) {
                        //   throw new Exception("INVALID_CREDENTIALS", e);
                    }
                    final UserDetails userDetails
                            = userService.loadUserByUsername(concatUserIdandTenantId);
                    final String token =
                            jwtUtility.generateToken(userDetails);
                    Map<String, Object> response = new HashMap<>();
                    response.put("otp", randomNumber);
                    response.put("otpTimeStamp", otpTimeStamp);
                    response.put("otp_message", convertedUserIdMessage);
                    tokenDataForStaff = companyMasterRepository.tokenDataStaff(userId);
                    if (tokenDataForStaff.equalsIgnoreCase("n")) {
                        companyMasterRepository.isertCompanyTokenForStaff(userId, tenantId, token);
                    } else {
                        String checkTokenValidation = companyMasterRepository.checkTokenValidationForStaff(userId);
                        if (checkTokenValidation.equalsIgnoreCase("y")) {
                            companyMasterRepository.updateCompanyTokenForStaff(userId, token);
                        }
                    }
                    return ResponseEntity.accepted().body(new GlobalResponseDTO(true, "Success", response));
                }

                */
                    try {
                        authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                        concatUserIdandTenantId,
                                        password
                                )
                        );

                    } catch (Exception e) {

                    }
                    final UserDetails userDetails
                            = userService.loadUserByUsername(concatUserIdandTenantId);
                    final String token =
                            jwtUtility.generateToken(userDetails);
                    Map<String, Object> response = new HashMap<>();
                    response.put("otp", randomNumber);
                    response.put("otpTimeStamp", otpTimeStamp);
                    response.put("otp_message", convertedUserIdMessage);
                    tokenData = companyMasterRepository.tokenData(userId, tenantId1);
                    if (tokenData.equalsIgnoreCase("n")) {
                        companyMasterRepository.isertCompanyToken(userId, tenantId1, token);
                    } else {
                        String checkTokenValidation = companyMasterRepository.checkTokenValidation(userId, tenantId1);
                        if (checkTokenValidation.equalsIgnoreCase("y")) {
                            companyMasterRepository.updateCompanyToken(userId, token, tenantId1);
                        } else {
                        }
                    }
                    return ResponseEntity.accepted().body(new GlobalResponseDTO(true, "Success", response));
                } else {
                    return ResponseEntity.accepted().body(new GlobalResponseDTO(false, "PLease Enter Correct Password"));

                }
            }


            // Check tenantId length
            if (tenantId != null && tenantId.length() < 8) {
                return ResponseEntity.accepted().body(new GlobalResponseDTO(false, "enter valid Business Id", null));
            }

            String lastTwoTLetters = null;
            String tenantFirstEightTLetters = null;


            //  lastTwoTLetters = tenantId.substring(tenantId.length() - 2);

            /// Check tenantId length

            if (tenantId != null && tenantId.length() >= 2) {
                lastTwoTLetters = tenantId.substring(tenantId.length() - 2);
                tenantFirstEightTLetters = tenantId.substring(0, 8);

            }


            String convertedUserId = userId.substring(0, 2) + userId.substring(2, userId.length() - 2).replaceAll(".", "*") + userId.substring(userId.length() - 2);
            String convertedUserIdMessage = "We have sent you verification code on" + " " + convertedUserId;

            String concatUserIdandTenantId = userId + "," + tenantId;

            List<JSONObject> dataForTenantValidationFromCompanyMaster = companyUserRepository.dataForTenantValidationFromCompanyMaster(tenantFirstEightTLetters);

            if (dataForTenantValidationFromCompanyMaster.isEmpty()) {
                return ResponseEntity.accepted().body(new GlobalResponseDTO(false, "Please enter correct Business Id ", null));
            }

            // Check if last two characters of tenantId are "st"
            if (lastTwoTLetters.equalsIgnoreCase("st")) {


                String strQueryForStaffDecryptPassword = "SELECT \"USER_PASSWORD\" FROM \"" + tenantFirstEightTLetters + "\".\"company_user\" WHERE \"USER_EMAIL\" = '" + userId + "'" +
                        " or \"USER_PHONE_NUMBER\" = '" + userId + "' ";

                List<Map<String, Object>> rowsForValidateStaffPassword = jdbcTemplate.queryForList(strQueryForStaffDecryptPassword);


                String userPassword = null;
                if (!rowsForValidateStaffPassword.isEmpty()) {
                    Map<String, Object> extractDbPassword = rowsForValidateStaffPassword.get(0);
                    userPassword = (String) extractDbPassword.get("USER_PASSWORD");
                } else {

                }
                // Decrypt staff user password from the database
                String staffDbDecryptPassword = obj.decrypt(userPassword);

                //   if (staffDbDecryptPassword.equals(password)) {
                // Compare decrypted password with provided password
                if (staffDbDecryptPassword != null && staffDbDecryptPassword.equals(password)) {

                    /// Staff authentication and OTP generation
                    Random random = new Random();
                    int randomNumber = random.nextInt(900000) + 100000;
                    String strRandomNo = String.valueOf(randomNumber);

                    // Check if staff OTP exists in the database
                    String staffOtpExistence = staffQuery.staffOtpExistence(tenantFirstEightTLetters, userId);
                    if (StringUtils.isEmpty(staffOtpExistence)) {
                        // Insert or update staff OTP in the database

                        staffQuery.insertFirstStaffOtp(tenantFirstEightTLetters, userId, strRandomNo);

                    } else {
                        staffQuery.updateStaffOtp(tenantFirstEightTLetters, userId, strRandomNo);

                    }

                    try {
                        authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                        userId,
                                        password
                                )
                        );


                    } catch (Exception e) {
                        //   throw new Exception("INVALID_CREDENTIALS", e);
                    }

                    final UserDetails userDetails
                            = userService.loadUserByUsername(concatUserIdandTenantId);
                    final String token =
                            jwtUtility.generateToken(userDetails);

                    Map<String, Object> response = new HashMap<>();
                    response.put("otp", randomNumber);
                    response.put("otpTimeStamp", "1691653953660.561000");
                    response.put("otp_message", convertedUserIdMessage);


                    String tokenFieldStatus = staffQuery.checkStaffToken(tenantFirstEightTLetters, userId);

                    if (tokenFieldStatus.equalsIgnoreCase("n")) {

                        staffQuery.updateStaffToken(tenantFirstEightTLetters, userId, token);
                    } else {

                        String tokenValidStatus = staffQuery.staffTokenValidation(tenantFirstEightTLetters, userId);

                        if (tokenValidStatus.equalsIgnoreCase("n")) {

                            staffQuery.updateStaffToken(tenantFirstEightTLetters, userId, token);

                        }
                    }
                    return ResponseEntity.accepted().body(new GlobalResponseDTO(true, "Success", response));


                }

            } else {
                String dbCompBusinessId = companyUserRepository.dbCompBusinessId(userId, tenantId);
                if (dbCompBusinessId != null) {

                    if (!dbCompBusinessId.equalsIgnoreCase(tenantId)) {
                        return ResponseEntity.accepted().body(new GlobalResponseDTO(false, "Please enter correct User Id", null));

                    }
                }
                // Proceed with your code using dbCompBusinessId
                else {
                }


                String dataForPasswordValidationFromCompanyMaster = companyUserRepository.dataForPasswordValidationFromCompanyMaster(userId, tenantId);
                if (dataForPasswordValidationFromCompanyMaster == null) {
                    String dataForPasswordValidationFromCompanyuser = staffAuthQuery.staffDbPasswordForStaff(userId, tenantFirstEightTLetters);


                    decryptedDbPassword = obj.decrypt(dataForPasswordValidationFromCompanyuser);
                    executeQuery = "companyUser";
                } else {
                    decryptedDbPassword = obj.decrypt(dataForPasswordValidationFromCompanyMaster);
                    executeQuery = "companyMaster";
                }
                if (password.equals(decryptedDbPassword)) {

                    Random random = new Random();
                    int randomNumber = random.nextInt(900000) + 100000;


                    JSONObject getMailId = companyMasterRepository.getMailId(userId, tenantId);
                    String otpMailSub = "";
                    String otpMailBody = "";
                    String companyBackupEmail = "";
                    if (getMailId != null) {
                        // The JSONObject is not null, so proceed with sending the email.
                        otpMailSub = "Bizfns";
                        otpMailBody = "Your one-time password is: " + randomNumber;
                        companyBackupEmail = (String) getMailId.get("COMPANY_BACKUP_EMAIL");
                        if (companyBackupEmail != null) {
                            //emailSenderService.sendSimpleEmail(companyBackupEmail, otpMailSub, otpMailBody);
                        } else {
                            //companyBackupEmail = (String) getMailId.get("COMPANY_BACKUP_EMAIL");
                            // Handle the case when "COMPANY_BACKUP_EMAIL" is not found in the JSONObject
                        }
                    } else {
                        otpMailSub = tenantId;
                        otpMailBody = "Your one-time password is: " + randomNumber;
                    }


                    if (executeQuery.equalsIgnoreCase("companyMaster")) {
                        List<JSONObject> checkOtpExistence = companyMasterRepository.checkOtpExistence(userId, tenantId);
                        if (checkOtpExistence.isEmpty()) {

                            otpTimeStamp = companyMasterRepository.insertFirstLoginOtp(String.valueOf(randomNumber), userId, tenantId);

                        } else {
                            otpTimeStamp = companyMasterRepository.updateLoginOtp(String.valueOf(randomNumber), userId, tenantId);
                        }
                    } else {
                        String userType = "staff";
                        List<Map<String, Object>> checkOtpExistence = staffQuery.checkOtpExistenceForStaff(userId, tenantId);

                        if (checkOtpExistence == null || checkOtpExistence.isEmpty()) {
                            otpTimeStamp = staffQuery.insertFirstLoginOtpForStaff(String.valueOf(randomNumber), userId, tenantId, 2);

                        } else {
                            otpTimeStamp = staffQuery.updateLoginOtpForStaff(String.valueOf(randomNumber), userId, userType, tenantId);
                        }

                        try {
                            authenticationManager.authenticate(
                                    new UsernamePasswordAuthenticationToken(
                                            userId,
                                            password
                                    )
                            );

                        } catch (Exception e) {
                            //   throw new Exception("INVALID_CREDENTIALS", e);
                        }
                        final UserDetails userDetails
                                = userService.loadUserByUsername(concatUserIdandTenantId);
                        final String token =
                                jwtUtility.generateToken(userDetails);
                        Map<String, Object> response = new HashMap<>();
                        response.put("otp", randomNumber);
                        response.put("otpTimeStamp", otpTimeStamp);
                        response.put("otp_message", convertedUserIdMessage);
                        tokenDataForStaff = staffQuery.tokenDataStaff(userId, tenantId);
                        if (tokenDataForStaff.equalsIgnoreCase("n")) {
                            staffQuery.isertCompanyTokenForStaff(userId, tenantId, token);
                        } else {
                            staffQuery.updateCompanyTokenForStaff(userId, token, tenantId);
                        /*String checkTokenValidation = staffQuery.checkTokenValidationForStaff(userId, tenantId);
                        if (checkTokenValidation.equalsIgnoreCase("y")) {
                            staffQuery.updateCompanyTokenForStaff(userId, token, tenantId);
                        }*/
                        }
                        return ResponseEntity.accepted().body(new GlobalResponseDTO(true, "Success", response));
                    }
                    try {
                        authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                        concatUserIdandTenantId,
                                        password
                                )
                        );

                    } catch (Exception e) {
                        //   throw new Exception("INVALID_CREDENTIALS", e);
                    }
                    final UserDetails userDetails
                            = userService.loadUserByUsername(concatUserIdandTenantId);
                    final String token =
                            jwtUtility.generateToken(userDetails);
                    Map<String, Object> response = new HashMap<>();
                    response.put("otp", randomNumber);
                    response.put("otpTimeStamp", otpTimeStamp);
                    response.put("otp_message", convertedUserIdMessage);
                    tokenData = companyMasterRepository.tokenData(userId, tenantId);
                    if (tokenData.equalsIgnoreCase("n")) {
                        companyMasterRepository.isertCompanyToken(userId, tenantId, token);
                    } else {
                        companyMasterRepository.updateCompanyToken(userId, token, tenantId);
                        String checkTokenValidation = companyMasterRepository.checkTokenValidation(userId, tenantId);
                        if (checkTokenValidation.equalsIgnoreCase("y")) {
                            companyMasterRepository.updateCompanyToken(userId, token, tenantId);
                        } else {
                        }
                    }
                    return ResponseEntity.accepted().body(new GlobalResponseDTO(true, "Success", response));
                }
            }
            return ResponseEntity.accepted().body(new GlobalResponseDTO(false, "UserId / password not match", null));
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(new GlobalResponseDTO(false, "Internal Server Error Encountered", null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

