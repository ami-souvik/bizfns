package com.bizfns.services.Service;

import com.bizfns.services.GlobalDto.GlobalResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.security.Principal;
import java.util.Map;

public interface RegistrationService {


    ResponseEntity<GlobalResponseDTO> companyRegistration(Map<String, String> request);

    ResponseEntity<GlobalResponseDTO> phoneNoRegCheck(Map<String, String> request);

    ResponseEntity<GlobalResponseDTO> getSecurityQuestion(Map<String, String> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> saveSecurityQuestion(Map<String, Object> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> forgotPassword(Map<String, String> request,String userType);

    ResponseEntity<GlobalResponseDTO> validateForgotPasswordOtp(Map<String, String> request, String userType);

    ResponseEntity<GlobalResponseDTO> resetPassword(Map<String, String> request, String userType);

    ResponseEntity<GlobalResponseDTO> testSchema(String token, Map<String, String> request);

    ResponseEntity<GlobalResponseDTO> verifySecurityQuestion(Map<String, Object> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> updateBusinessMobileNo(Map<String, String> request);

    ResponseEntity<GlobalResponseDTO> addStaff(Map<String, String> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> preregistrationSendOtp(Map<String, String> request);

    ResponseEntity<GlobalResponseDTO> preregistrationOtpVerification(Map<String, String> request);

    ResponseEntity<GlobalResponseDTO> testFor(Map<String, String> request);

    ResponseEntity<GlobalResponseDTO> otpUpdateBusinessEmail(Map<String, String> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> validateOtpUpdateBusinessEmail(Map<String, String> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> getActiveStatusForStaff(Map<String, String> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> updateActiveInactiveStatusForStaff(Map<String, String> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> deleteStaff(Map<String, String> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> getStaffDetails(Map<String, String> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> updateStaffDetails(Map<String, Object> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> getUserTypeAndUserInfo(Principal principal);
}
