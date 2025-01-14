package com.bizfns.services.Controller;

import com.bizfns.services.GlobalDto.GlobalResponseDTO;
import com.bizfns.services.Query.StaffQuery;
import com.bizfns.services.Repository.RegistrationRepository;
import com.bizfns.services.Service.RegistrationService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Map;


@RestController
@Slf4j
@RequestMapping("/api/users")
public class RegistrationController {
    @Autowired
    private RegistrationRepository registrationRepository;
    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private StaffQuery staffQuery;

    @PostMapping(EndpointPropertyKey.COMPANY_REGISTRATION)
    public ResponseEntity<GlobalResponseDTO> companyRegistration(
            @RequestBody Map<String, String> request) {

        return registrationService.companyRegistration(request);
    }

    @PostMapping(EndpointPropertyKey.PHONE_NUMBER_REGISTRATION_CHECK)
    public ResponseEntity<GlobalResponseDTO> phoneNoRegCheck(
            @RequestBody Map<String, String> request) {

        return registrationService.phoneNoRegCheck(request);
    }

    /*
     * AMIT KUMAR SINGH
     * This endpoint redirects users to the appropriate app store link based on their device type (Android or iOS).
     * If the user-agent header indicates an Android device, it redirects to the Google Play Store link for Candy Crush Saga.
     * If the user-agent header indicates an iOS device, it redirects to the Apple App Store link for a specific app story.
     * For unrecognized platforms or missing user-agent headers, it redirects to a Stack Overflow question link.
     * @PARAM requestType The HTTPServletRequest object to retrieve the User-Agent header
     */
    @GetMapping(EndpointPropertyKey.APP_LINK)
    public ResponseEntity<GlobalResponseDTO> appLink(HttpServletRequest requestType) {
        String userAgent = requestType.getHeader("User-Agent");


        if (userAgent != null && userAgent.contains("Android")) {
            // The API is hit from an Android platform
            return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                    .header("Location", "https://play.google.com/store/apps/details?id=com.king.candycrushsaga&hl=en-IN")
                    .body(null);
        } else if (userAgent != null && userAgent.contains("iOS")) {
            // The API is hit from an iOS platform
            return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                    .header("Location", "https://apps.apple.com/in/story/id1678317523?itscg=10000&itsct=")
                    .body(null);
        } else {
            // The platform is not identified or the user-agent header is missing
            return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                    .header("Location", "https://stackoverflow.com/questions/68272297/how-to-extract-json-clob-to-java")
                    .body(null);
        }


    }


    @PostMapping(EndpointPropertyKey.GET_SECURITY_QUESTION)
    public ResponseEntity<GlobalResponseDTO> getSecurityQuestion(
            @RequestBody Map<String, String> request, Principal principal) {

        return registrationService.getSecurityQuestion(request, principal);
    }

    @PostMapping(EndpointPropertyKey.SAVE_SECURITY_QUESTION)
    public ResponseEntity<GlobalResponseDTO> saveSecurityQuestion(
            @RequestBody Map<String, Object> request, Principal principal) {

        return registrationService.saveSecurityQuestion(request, principal);
    }

    @PostMapping(EndpointPropertyKey.FORGOT_PASSWORD)
    public ResponseEntity<GlobalResponseDTO> forgotPassword(
            @RequestBody Map<String, String> request, @RequestParam(required = false) String userType) {

        return registrationService.forgotPassword(request,userType);
    }

    @PostMapping(EndpointPropertyKey.VALIDATE_FORGOT_PASSWORD_OTP)
    public ResponseEntity<GlobalResponseDTO> validateForgotPasswordOtp(
            @RequestBody Map<String, String> request, @RequestParam(required = false) String userType) {

        return registrationService.validateForgotPasswordOtp(request, userType);
    }

    @PostMapping(EndpointPropertyKey.RESET_PASSWORD)
    public ResponseEntity<GlobalResponseDTO> resetPassword(
            @RequestBody Map<String, String> request, @RequestParam(required = false) String userType) {

        return registrationService.resetPassword(request, userType);
    }

    @PostMapping(EndpointPropertyKey.TEST_SCHEMA)
    public ResponseEntity<GlobalResponseDTO> testSchema(@RequestHeader("Authorization") String token,
                                                        @RequestBody Map<String, String> request) {

        return registrationService.testSchema(token, request);
    }

    @PostMapping(EndpointPropertyKey.VERIFY_SECURITY_QUESTION)
    public ResponseEntity<GlobalResponseDTO> verifySecurityQuestion(
            @RequestBody Map<String, Object> request, Principal principal) {

        return registrationService.verifySecurityQuestion(request, principal);
    }

    @PostMapping(EndpointPropertyKey.UPDATE_BUSINESS_MOBILE_NO)
    public ResponseEntity<GlobalResponseDTO> updateBusinessMobileNo(
            @RequestBody Map<String, String> request) {

        return registrationService.updateBusinessMobileNo(request);
    }

    @PostMapping(EndpointPropertyKey.ADD_STAFF)
    public ResponseEntity<GlobalResponseDTO> addStaff(
            @RequestBody Map<String, String> request, Principal principal) {
        String tenantId = (String) request.get("tenantId");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            String authorityName = authority.getAuthority();
            List<String> priviledgeChk = staffQuery.priviledgeChkForStaff(tenantId);
            boolean hasEditPrivilege = false;
            for (String privilege : priviledgeChk) {
                if (privilege.equalsIgnoreCase("ADD")) {
                    hasEditPrivilege = true;
                    break;
                }
            }
            if (authorityName.equalsIgnoreCase("Staff") && !hasEditPrivilege) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new GlobalResponseDTO(false, "A Staff user dont have the priviledge to ADD the staff.", null));
            }
        }
        return registrationService.addStaff(request, principal);
    }

    @PostMapping(EndpointPropertyKey.PRE_REGISTRATION_SEND_OTP)
    public ResponseEntity<GlobalResponseDTO> preregistrationSendOtp(
            @RequestBody Map<String, String> request) {

        return registrationService.preregistrationSendOtp(request);
    }

    @PostMapping(EndpointPropertyKey.PRE_REGISTRATION_OTP_VERIFICATION)
    public ResponseEntity<GlobalResponseDTO> preregistrationOtpVerification(
            @RequestBody Map<String, String> request) {

        return registrationService.preregistrationOtpVerification(request);
    }

    @PostMapping(EndpointPropertyKey.TEST_FOT)
    public ResponseEntity<GlobalResponseDTO> testFor(
            @RequestBody Map<String, String> request) {

        return registrationService.testFor(request);
    }

    @PostMapping(EndpointPropertyKey.OTP_UPDATE_BUSINESS_EMAIL)
    public ResponseEntity<GlobalResponseDTO> otpUpdateBusinessEmail(
            @RequestBody Map<String, String> request, Principal principal) {

        return registrationService.otpUpdateBusinessEmail(request, principal);
    }

    @PostMapping(EndpointPropertyKey.VALIDATE_OTP_UPDATE_BUSINESS_EMAIL)
    public ResponseEntity<GlobalResponseDTO> validateOtpUpdateBusinessEmail(
            @RequestBody Map<String, String> request, Principal principal) {

        return registrationService.validateOtpUpdateBusinessEmail(request, principal);
    }

    @PostMapping(EndpointPropertyKey.Get_All_Schemas)
    public ResponseEntity<GlobalResponseDTO> AllSchemasForBusiness(
            @RequestBody Map<String, String> request, Principal principal) {

        return registrationService.validateOtpUpdateBusinessEmail(request, principal);
    }

    @PostMapping(EndpointPropertyKey.get_active_status_for_staff)
    public ResponseEntity<GlobalResponseDTO> getActiveStatusForStaff(
            @RequestBody Map<String, String> request, Principal principal) {

        return registrationService.getActiveStatusForStaff(request, principal);
    }

    @PostMapping(EndpointPropertyKey.update_active_inactive_status_for_staff)
    public ResponseEntity<GlobalResponseDTO> updateActiveInactiveStatusForStaff(
            @RequestBody Map<String, String> request, Principal principal) {

        return registrationService.updateActiveInactiveStatusForStaff(request, principal);
    }

    @PostMapping(EndpointPropertyKey.delete_staff)
    public ResponseEntity<GlobalResponseDTO> deleteStaff(
            @RequestBody Map<String, String> request, Principal principal) {

        return registrationService.deleteStaff(request, principal);
    }

    @PostMapping(EndpointPropertyKey.get_staff_details)
    public ResponseEntity<GlobalResponseDTO> getStaffDetails(
            @RequestBody Map<String, String> request, Principal principal) {

        return registrationService.getStaffDetails(request, principal);
    }

    @PostMapping(EndpointPropertyKey.update_staff_details)
    public ResponseEntity<GlobalResponseDTO> updateStaffDetails(
            @RequestBody Map<String, Object> request,Principal principal) {
        return registrationService.updateStaffDetails(request,principal);
    }

    @GetMapping(EndpointPropertyKey.get_userType_And_UserInfo)
    public ResponseEntity<GlobalResponseDTO> getUserTypeAndUserInfo(Principal principal) {
        return registrationService.getUserTypeAndUserInfo(principal);
    }

}
