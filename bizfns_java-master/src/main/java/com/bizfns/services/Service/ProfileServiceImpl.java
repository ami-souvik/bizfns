package com.bizfns.services.Service;

import com.bizfns.services.GlobalDto.GlobalResponseDTO;
import com.bizfns.services.Module.ProfileResponseDto;
import com.bizfns.services.Repository.CompanyMasterRepository;
import com.bizfns.services.Repository.ProfileRepository;
import com.bizfns.services.Serviceimpl.AES;
import com.bizfns.services.Serviceimpl.ProfileService;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Principal;
import java.sql.Timestamp;
import java.util.*;

@Service
public class ProfileServiceImpl implements ProfileService {

    @Value("${project.image}")
    private  String path;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private CompanyMasterRepository companyMasterRepository;

    public ProfileServiceImpl() {
    }


    /**
     * This method retrieves profile information for a user identified by userId within a specified tenantId.
     *
     * @param tenantId     The ID of the tenant.
     * @param userId       The ID of the user.
     * @param businessEmail The business email associated with the user.
     * @param principal    Principal object containing the current user's authentication details.
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> getProfile(String tenantId, String userId,String businessEmail, Principal principal) {

        if(checkUserMatch(userId,tenantId, principal.getName())){
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorised user, we could not access the APIs from others token "));
        }

        //System.out.println("userName : "+principal.getName());
        if (isNullOrEmpty(tenantId) || isNullOrEmpty(userId)){
            return ResponseEntity.ok().body(new GlobalResponseDTO(false, "Mandatory Fields should not be null"));

        }

        Map<String, Object> profileQuery1 = profileRepository.getProfileQuery1(tenantId, userId);

        if (profileQuery1.isEmpty()){

            Map<String, Object> userIdAndComId = profileRepository.getCompanyIdFromCompanyMaster(tenantId);

            if (userIdAndComId.isEmpty()){

                return ResponseEntity.ok().body(new GlobalResponseDTO(false, "User Not Found for this staff : " +userId));
            }
            String userIdAssociated = (String) userIdAndComId.get("COMPANY_BACKUP_PHONE_NUMBER");
            int companyId = (Integer) userIdAndComId.get("COMPANY_ID");

            //Map<String, Object> staffProfile = profileRepository.getStaffProfile(userId, tenantId);


            //System.err.println("Staff profile");
            return getProfileForAll(tenantId,userIdAssociated,businessEmail);
            //return   ResponseEntity.ok().body(new GlobalResponseDTO(true, "staff profile", staffProfile));
        } else {
            return getProfileForAll(tenantId,userId,businessEmail);

        }
        //return null;


    }

    public ResponseEntity<GlobalResponseDTO> getProfileForAll(String tenantId, String userId,String businessEmail) {

       // System.err.println(Principal.class.getName());

        //String businessEmailQuery = profileRepository.getBusinessEmailQuery(tenantId, userId);

//        if (!businessEmailQuery.equalsIgnoreCase("Failure")) {
//            if (!businessEmailQuery.equalsIgnoreCase(businessEmail)) {
//                return ResponseEntity.ok().body(new GlobalResponseDTO(false, "Business Email Not Matched"));
//            }
//        }



        ProfileResponseDto response = new ProfileResponseDto();

        Map<String, Object> profileQuery1 = profileRepository.getProfileQuery1(tenantId, userId);


            Map<String, Object> profileQuery2 = profileRepository.getProfileQuery2((Integer) profileQuery1.get("COMPANY_ID"));
            List<Map<String, Object>> questionQuery = profileRepository.getProfileQuery3((Integer) profileQuery1.get("COMPANY_ID"));

            Map<String, Object> marketingQuery = profileRepository.getMarketingQuery((Integer) profileQuery1.get("COMPANY_ID"));

            String addressQuery = profileRepository.getAddressQuery((Integer) profileQuery1.get("COMPANY_ID"));

            Map<String, Object> businessTypeId = profileRepository.getProfileQuery4((Integer) profileQuery1.get("COMPANY_ID"));

            Map<String, Object> addressMap = new HashMap<>();
           // addressMap.put("firstName", addressQuery.get("FIRST_NAME"));
            //addressMap.put("lastName", addressQuery.get("LAST_NAME"));
             //addressQuery.get("ADDRESS");

            response.setFullAddress(addressQuery != null ? addressQuery : "");

            response.setBusinessType((String) businessTypeId.get("BUSINESS_TYPE_ENTITY"));
            // Extracting values from profileQuery1
            String businessName = (String) profileQuery1.get("BUSINESS_NAME");
            Date registrationDate = (Date) profileQuery1.get("COMPANY_CREATED_AT");
            String backupEmail = (String) profileQuery1.get("COMPANY_BACKUP_EMAIL");
            String backupPhoneNumber = (String) profileQuery1.get("COMPANY_BACKUP_PHONE_NUMBER");
            String companyLogo = (String) profileQuery1.get("COMPANY_LOGO");
            String businessContactPerson = (String) profileQuery1.get("BUSINESS_CONTACT_PERSON");
            String secondaryNumber = (String) profileQuery1.get("TRUSTED_BACKUP_PHONE_NUMBER");
            String secondaryEmail = (String) profileQuery1.get("TRUSTED_BACKUP_EMAIL");
            String companyStatus = (String) profileQuery1.get("COMPANY_STATUS");

            Map<String, Object> businessMap = new HashMap<>();
            businessMap.put("businessName", businessName);
            businessMap.put("businessLogo", companyLogo != null ? companyLogo : "");

            Map<String, Object> marketingMap = new HashMap<>();

            String addLocation = (String) marketingQuery.get("add_location");

            if (addLocation==null ||addLocation.isEmpty()) {
                marketingMap.put("addLocation", new ArrayList<>());

            }

        else{
                String[] split = addLocation.split(",");

                ArrayList addMarket = new ArrayList();
                for (String addLocation1:split){
                    addMarket.add(addLocation1.trim());
                }
                //addMarket.add(split);
                marketingMap.put("addLocation", addMarket);
            }
            marketingMap.put("marketingDescription", marketingQuery.get("marketing_description") != null ? marketingQuery.get("marketing_description") : "");
            String subscriptionStartDate = String.valueOf((Date) profileQuery2.get("COMPANY_SUBSCRIPTION_START_DATE"));
            String subscriptionEndDate = String.valueOf((Date) profileQuery2.get("COMPANY_SUBSCRIPTION_END_DATE"));

            // Convert Timestamps to formatted date strings
           //String formattedStartDate = formatDate(subscriptionStartDate);
            //String formattedEndDate = formatDate(subscriptionEndDate);
            Map<String, Object> subscriptionMap = new HashMap<>();
            subscriptionMap.put("subscriptionStartDate", subscriptionStartDate);
            subscriptionMap.put("subscriptionEndDate", subscriptionEndDate);
            subscriptionMap.put("subscriptionCategoryDesc", profileQuery2.get("COMPANY_SUBSCRIPTION_CATEGORY_DESCRIPTION") != null ? profileQuery2.get("COMPANY_SUBSCRIPTION_CATEGORY_DESCRIPTION") : "");
            subscriptionMap.put("subscriptionPlanId", profileQuery2.get("FK_SUBSCRIPTION_PLAN_ID"));

            response.setTrustedBackupEmail(secondaryEmail != null ? secondaryEmail : "");
            response.setTrustedBackupMobileNumber(secondaryNumber != null ? secondaryNumber : "");
            response.setMarketing(marketingMap);
            response.setBusinessNameAndLogo(businessMap);
            response.setCompanyStatus(companyStatus);
            // Setting values in response object
            // response.setBusinessName(businessName);
            response.setRegistrationDate(String.valueOf(registrationDate));
            response.setPrimaryBusinessEmail(backupEmail != null ? backupEmail : "");
            response.setPrimaryMobileNumber(backupPhoneNumber);
            response.setBusinessContactPerson(businessContactPerson != null ? businessContactPerson : "");
            // response.setBusinessLogo(companyLogo);
            response.setSubscriptionPlan(subscriptionMap);

            response.setSecurityQuestion(questionQuery);

            return ResponseEntity.ok().body(new GlobalResponseDTO(true, "success",response ));

    }

    /**
     * This method saves master profile information for a user identified by userId within a specified tenantId.
     *
     * @param request   Map containing request parameters including tenantId, userId, and profile details.
     * @param principal Principal object containing the current user's authentication details.
     * @return ResponseEntity containing a GlobalResponseDTO with success status and message.
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> saveMasterProfile(Map<String, Object> request, Principal principal) {
        String tenantId = (String) request.get("tenantId");
        String userId = (String) request.get("userId");

        if(checkUserMatch(userId,tenantId, principal.getName())){
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorised user, we could not access the APIs from others token "));
        }

        String businessContactPerson = (String) request.get("businessContactPerson");
        String trustedBackupMobileNumber = (String) request.get("trustedBackupMobileNumber");
        String trustedBackupEmail = (String) request.get("trustedBackupEmail");
        String businessEmail = (String) request.get("businessEmail");


        Map<String, Object> businessNameAndLogo = (Map<String, Object>) request.get("businessNameAndLogo");
        String businessName = (String) businessNameAndLogo.get("businessName");
        String businessLogo = (String) businessNameAndLogo.get("businessLogo");


        Map<String, Object> marketingInfo = (Map<String, Object>) request.get("marketing");
        String marketingDescription = (String) marketingInfo.get("marketingDescription");
        List<String> addLocationList = (List<String>) marketingInfo.get("addLocation");


       // Map<String, Object> addressMap = (Map<String, Object>) request.get("fullAddress");
        //String firstName = (String) addressMap.get("firstName");
        //String lastName = (String) addressMap.get("lastName");
        String address = (String) request.get("address");


        String companyId = profileRepository.getCompanyId(tenantId, userId);

        if (companyId.equalsIgnoreCase("failure")){
            return ResponseEntity.ok().body(new GlobalResponseDTO(false, "UserId/TenantId invalid"));

        }

        //System.out.println("companyId :  " + companyId);

        int i = profileRepository.saveMasterProfile(tenantId, userId, businessContactPerson, trustedBackupMobileNumber,
                trustedBackupEmail, businessEmail, businessName, businessLogo, marketingDescription, addLocationList,
                address, Integer.parseInt(companyId));

        //int i1 = profileRepository.saveMasterProfileInUserMaster(tenantId, userId, businessEmail, businessName);


        if (i >= 1) {
            return ResponseEntity.ok().body(new GlobalResponseDTO(true, "All the details are Saved Successfully"));
        }
        return null;
    }

    @Override
    public ResponseEntity<GlobalResponseDTO> saveChangesMobile(Map<String, Object> request, Principal principal) {

        String userIdOrOldMobileNumber = (String) request.get("oldMobileNumber");
        String newMobileNumber = (String) request.get("newMobileNumber");
        String otp = (String) request.get("otp");
        String otpTimeStamp = (String) request.get("otpTimeStamp");
        String tenantId = (String) request.get("tenantId");

        if(checkUserMatch(userIdOrOldMobileNumber,tenantId, principal.getName())){
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorised user, we could not access the APIs from others token "));
        }
        String companyId = (String) request.get("companyId");

        if (newMobileNumber.isEmpty()||newMobileNumber==null){
            return ResponseEntity.ok().body(new GlobalResponseDTO(false, "New Mobile Number field should not be null"));

        }

        List<String> businessName = profileRepository.getBusinessName(newMobileNumber);

        if (!businessName.isEmpty()){
            if(!isNewBusinessNameDifferent(newMobileNumber, userIdOrOldMobileNumber)){
                return ResponseEntity.accepted()
                        .body(new GlobalResponseDTO(false, "Already same business present", null));

            }
        }

        //List<String> businessNameOldNumber = profileRepository.getBusinessNameOldNumber(userIdOrOldMobileNumber);
        //String deviceInfo = "deviceId = " + deviceId + "\n" + "deviceType = " + deviceType + "\n" + "appVersion = " + appVersion;

        //String fullURL = httpServletRequest.getRequestURL().toString();

        try {
            String fetchOtp = companyMasterRepository.getDbOtp(Integer.valueOf(companyId));
            String isOtpExpire = companyMasterRepository.isOtpExpireForEmail(Integer.valueOf(companyId));

            //System.err.println("otp ::" + fetchOtp);

           // System.err.println("otp ::" + isOtpExpire);

            if (otp.equals(fetchOtp)) {
                //System.err.println("otp verified");
                if (isOtpExpire.equalsIgnoreCase("y")) {

                    Date date = new Date();

                    // Convert the Date to a Timestamp
                    Timestamp timestamp = new Timestamp(date.getTime());
                    int message1 = profileRepository.updateMobileNumberQuery(userIdOrOldMobileNumber, newMobileNumber, tenantId);
                    if (message1 == 1) {
                        //System.err.println("integer 1 ");


                        String businessName1 = profileRepository.fetchBusinessName(Integer.parseInt(companyId));
                        profileRepository.deleteToken(tenantId);
                        int i = profileRepository.saveNotificationMessage(Integer.parseInt(companyId), "mobile", "mobile number changeD successfully", timestamp,businessName1, tenantId);

                       // profileRepository.updateMobilenumberInUserMaster(userIdOrOldMobileNumber, newMobileNumber, tenantId);
                        return ResponseEntity.ok().body(new GlobalResponseDTO(true, "Mobile number Updated successfully"));
                    } else {
                        return ResponseEntity.ok().body(new GlobalResponseDTO(false, "Failed to Update mobile number"));
                    }


                } else {
                    return ResponseEntity.accepted()
                            .body(new GlobalResponseDTO(false, "otp expired", null));
                }
            } else {
                return ResponseEntity.accepted()
                        .body(new GlobalResponseDTO(false, "otp not match", null));
            }

        } catch (Exception e) {
            // errorLogService.errorLog(request, e.getMessage(), fullURL, userId, deviceInfo);
            return ResponseEntity.accepted().body(new GlobalResponseDTO(false, "Failed to Update Mobile Number", null));

        }

    }

    private boolean isNewBusinessNameDifferent(String newMobileNumber, String userIdOrOldMobileNumber) {
        List<String> businessNameNewNumber = profileRepository.getBusinessName(newMobileNumber);
        List<String> businessNameOldNumber = profileRepository.getBusinessNameOldNumber(userIdOrOldMobileNumber);

        // Check if any business name from the new mobile number is the same as any business name from the old mobile number
        for (String newBusinessName : businessNameNewNumber) {
            for (String oldBusinessName : businessNameOldNumber) {
                if (newBusinessName.equals(oldBusinessName)) {
                    // Found a matching business name, indicating that the new business name is the same as the old one
                    return false; // Not different
                }
            }
        }
        // No matching business names found, indicating that the new business name is different
        return true; // Different
    }

    @Override
    public ResponseEntity<GlobalResponseDTO> saveChangesSubscriptionPlan(Map<String, Object> request, Principal principal) {


        return null;
    }

    /**
     * This method verifies the password during login for a given userId and tenantId from database.
     *
     * @param request   Map containing request parameters including userId, tenantId, and verifyPassword.
     * @param principal Principal object containing the current user's authentication details.
     * @return ResponseEntity containing a GlobalResponseDTO indicating whether the password verification was successful or not.
     */

    @Override
    public ResponseEntity<GlobalResponseDTO> verifyPassword(Map<String, Object> request, Principal principal) {
        String userId = (String) request.get("userId");
        String tenantId = (String) request.get("tenantId");
        String verifyPassword = (String) request.get("verifyPassword");

        if(checkUserMatch(userId,tenantId, principal.getName())){
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorised user, we could not access the APIs from others token "));
        }


        String passwordByUserId = profileRepository.getPasswordByUserId(userId, tenantId);
        if (passwordByUserId.isEmpty() ) {
            return ResponseEntity.ok(new GlobalResponseDTO(false, "user not found"));
        }
        String decryptPassword = new AES().decrypt(passwordByUserId);


        //System.out.println("password : " + decryptPassword);


        if (verifyPassword.equals(decryptPassword)) {

            //System.out.println("password : " + passwordByUserId);
            return ResponseEntity.ok(new GlobalResponseDTO(true,
                    "Password Verified Successfully"));
        } else {
            return ResponseEntity.ok(new GlobalResponseDTO(false, "Password not matched"));

        }

    }

    /**
     * This method generates and sends OTP for mobile number change verification.
     *
     * @param request   Map containing request parameters including oldMobileNumber, newMobileNumber, tenantId, and companyId.
     * @param principal Principal object containing the current user's authentication details.
     * @return ResponseEntity containing a GlobalResponseDTO with OTP generation status and message.
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> getOtpForMobileChanges(Map<String, Object> request, Principal principal) {
        String userId = (String) request.get("oldMobileNumber");
        String newMobileNumber = (String) request.get("newMobileNumber");

        String tenantId = (String) request.get("tenantId");
        String companyId = (String) request.get("companyId");
        String otpTimeStamp = null;

        if(checkUserMatch(userId,tenantId, principal.getName())){
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorised user, we could not access the APIs from others token "));
        }

        //checkMailExistence = null;
        String checkMobileExistence = profileRepository.checkMobileExistence(Integer.valueOf(companyId));

        if (checkMobileExistence.equalsIgnoreCase(userId)) {


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


        } else {
            return ResponseEntity.accepted()
                    .body(new GlobalResponseDTO(false, "Failure", "Current Mobile Number not match"));

        }

    }

    /**
     * This method is used to save business logo media (MultipartFiles) associated with a job for a specific tenant.
     *
     * @param file      Array of MultipartFiles representing the media files to be saved.
     * @param tenantId  The ID of the tenant to which the media files belong.the authenticated user's information.
     * @return ResponseEntity containing a GlobalResponseDTO with a success status and the upload message,
     * or a bad request with an error message if the user is unauthorized.
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> saveImagePath(MultipartFile[] file, String tenantId, String userId, Principal principal) throws IOException {


        if(checkUserMatch(userId,tenantId, principal.getName())){
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorised user, we could not access the APIs from others token "));
        }
        ArrayList<Map<String, Object>> maps = uploadFile(path, file,tenantId, userId);

        return ResponseEntity.ok().body(new GlobalResponseDTO(true, "success",maps));
    }

    public ArrayList<Map<String,Object>> uploadFile(String path, MultipartFile[] file, String tenantId, String companyId) throws IOException {

        long count = Arrays.stream(file).count();
        //System.err.println("count of image : " +count);
        double totalSize = Arrays.stream(file)
                .mapToDouble(multifile -> ((double) multifile.getSize() / 1048576))
                .sum();
        ArrayList<Map<String,Object>> ImageList=new ArrayList<>();

        try {
            double CompanyConsumeData = 0.00;
          /*  if (totalSize > 5) {
              //  throw new CustomException("File size exceeds the maximum allowed size of 1MB.");

            } else if ((100 - CompanyConsumeData) < totalSize) {
               // throw new CustomException("Company has total consume data is exceeds");

            } else {*/

            Arrays.stream(file).forEach(multifile -> {
                Map<String,Object> imageMap=new HashMap<>();
                // LocalDateTime currentTimestamp = LocalDateTime.now();


                String name = multifile.getOriginalFilename();
                String filepath = path + File.separator + name;
                File f = new File(path);
                if (!f.exists()) {
                    f.mkdir();
                }
                try {
                    Files.copy(multifile.getInputStream(), Paths.get(filepath));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                profileRepository.saveImageName(name,tenantId, companyId);
                imageMap.put("imageName",name);
                imageMap.put("contentType",multifile.getContentType());
                ImageList.add(imageMap);

            });
            // }
        } catch (Exception e) {
           // System.err.println("Caught a CustomException: " + e.getMessage());

        }
        return ImageList;
    }
    
    
    public boolean isNullOrEmpty(String value){
        if (value.trim().isEmpty()||value==null ){
            return true;
        }
        return false;
    }


    /*
    * @Author Md Huzefa
    * This method is used to check the access token userId with APIs userId
    * if the userId mismatch then throw unauthorised exception
    * @Param userId from request body of APIs and TokenUserId from access token
    * */
    public boolean checkUserMatch(String userId,String tenantId , String username){

        //String userPhone = profileRepository.checkAccessToken(userId, tenantId);

        String[] part = username.split(",");

        String tokenUserId = part[0];
        String tokenTenantId = part[1];
        if (!tenantId.equals(tokenTenantId)){
            return true;
         }
        //else if (!email.equals(tokenUserId) || !tenantId.equals(tokenTenantId)) {
           // return true;
       // }

        return false;
    }

    
    
}














