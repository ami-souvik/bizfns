package com.bizfns.services.Service;

import com.bizfns.services.GlobalDto.GlobalResponseDTO;
import com.bizfns.services.Module.CompanyUserRequestDto;
import org.springframework.http.ResponseEntity;

import java.net.MalformedURLException;
import java.security.Principal;
import java.util.Map;

public interface CompanyUserService {

    ResponseEntity<GlobalResponseDTO> otpVerification(Map<String, String> request);

    ResponseEntity<GlobalResponseDTO> addCustomer(Map<String, Object> request);

    ResponseEntity<GlobalResponseDTO> fetchPreRegistration();


    ResponseEntity<GlobalResponseDTO> preCreationInfo(Map<String, String> request);

    ResponseEntity<GlobalResponseDTO> changePasswordSendOtp(Map<String, String> request);

    ResponseEntity<GlobalResponseDTO> validateOtpAndChangePassword(Map<String, String> request, String userType);

    ResponseEntity<GlobalResponseDTO> fetchPreStaffCreationDetails(Map<String, String> request);

    ResponseEntity<GlobalResponseDTO> fetchStaffList(Map<String, String> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> preNewScheduleData(Map<String, String> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> fetchServiceList(Map<String, String> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> addService(Map<String, String> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> fetchServiceRateUnit(Map<String, String> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> addCustomerAndService(Map<String, Object> request);

    ResponseEntity<GlobalResponseDTO> getCustomerDetails(Map<String, Object> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> updateCustomerDetails(Map<String, Object> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> deleteCustFromDB(Map<String, Object> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> updateServiceObjectDetails(Map<String, Object> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> getActiveInactiveStatusForCustomer(Map<String, Object> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> UpdateActiveInactiveStatusForCustomer(Map<String, Object> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> getServiceDetails(Map<String, String> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> updateServiceDetails(Map<String, String> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> deleteServiceDetails(Map<String, String> request, Principal principal);
}
