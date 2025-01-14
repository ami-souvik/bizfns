package com.bizfns.services.Controller;

import com.bizfns.services.GlobalDto.GlobalResponseDTO;
import com.bizfns.services.Query.CustQuery;
import com.bizfns.services.Query.StaffQuery;
import com.bizfns.services.Service.CompanyUserService;
import com.bizfns.services.Service.ErrorLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping ("/api/users")
public class CompanyUserController {

    @Autowired private CompanyUserService companyUserService;
    @Autowired
    private ErrorLogService errorLogService;

    @Autowired
    private StaffQuery staffQuery;

    @Autowired
    private CustQuery custQuery;



    @PostMapping(EndpointPropertyKey.OTP_AUTHENTICATION)
    public ResponseEntity<GlobalResponseDTO> otpVerification(
            @RequestBody  Map<String, String> request) {

        return companyUserService.otpVerification(request);
    }

    @PostMapping(EndpointPropertyKey.CHANGE_PASSWORD_SEND_OTP)
    public ResponseEntity<GlobalResponseDTO> changePasswordSendOtp( @RequestHeader("Authorization") String token,
            @RequestBody  Map<String, String> request) {

        return companyUserService.changePasswordSendOtp(request);
    }

    @PostMapping(EndpointPropertyKey.VALIDATE_OTP_AND_CHANGE_PASSWORD)
    public ResponseEntity<GlobalResponseDTO> validateOtpAndChangePassword(@RequestParam(required = false) String userType,
            @RequestBody  Map<String, String> request) {

        return companyUserService.validateOtpAndChangePassword(request , userType);
    }



    @PostMapping(EndpointPropertyKey.ADD_CUSTOMER)
    public ResponseEntity<GlobalResponseDTO> addCustomer(
            @RequestBody  Map<String, Object> request) {
        String tenantId = (String) request.get("tenantId");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            String authorityName = authority.getAuthority();
            List<String> priviledgeChk = custQuery.priviledgeChkForCustomer(tenantId);
            //boolean hasEditPrivilege = false;
            boolean hasEditPrivilege = true;
            for (String privilege : priviledgeChk) {
                if (privilege.equalsIgnoreCase("ADD")) {
                    hasEditPrivilege = true;
                    break;
                }
            }
            if (authorityName.equalsIgnoreCase("Staff") && !hasEditPrivilege) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new GlobalResponseDTO(false, "A Staff user dont have the priviledge to add the customer.", null));
            }
        }
        return companyUserService.addCustomer(request);
    }




    @PostMapping(EndpointPropertyKey.FETCH_PRE_REGISTRATION)
    public ResponseEntity<GlobalResponseDTO> fetchPreRegistration(
          ) {

        return companyUserService.fetchPreRegistration();


    }


    @PostMapping(EndpointPropertyKey.PRE_CREATION_INFO)
    public ResponseEntity<GlobalResponseDTO> preCreationInfo(@RequestBody Map<String, String> request) {
        return companyUserService.preCreationInfo(request);
    }


    @PostMapping(EndpointPropertyKey.PRE_STAFF_CREATION_DETAILS)
    public ResponseEntity<GlobalResponseDTO> preStaffCreationDetails(@RequestBody Map<String, String> request){
        return companyUserService.fetchPreStaffCreationDetails(request);
    }

    @PostMapping(EndpointPropertyKey.FETCH_STAFF_LIST)
    public ResponseEntity<GlobalResponseDTO> fetchStaffList(@RequestBody Map<String, String> request, Principal principal){

        return companyUserService.fetchStaffList(request, principal);
    }

    @PostMapping(EndpointPropertyKey.PRE_NEW_SCHEDULE_DATA)
    public ResponseEntity<GlobalResponseDTO> preNewScheduleData(@RequestBody Map<String, String> request, Principal principal){

        return companyUserService.preNewScheduleData(request, principal);
    }

    @PostMapping(EndpointPropertyKey.SERVICELIST)
    public ResponseEntity<GlobalResponseDTO> fetchServiceList(@RequestBody Map<String, String> request, Principal principal){

        return companyUserService.fetchServiceList(request,principal );
    }

    @PostMapping(EndpointPropertyKey.TST)
    public ResponseEntity<GlobalResponseDTO> tst(@RequestBody Map<String, String> request){

       return null;
    }


    @PostMapping(EndpointPropertyKey.FETCH_SERVICE_RATE_UNIT)
    public ResponseEntity<GlobalResponseDTO> fetchServiceRateUnit(@RequestBody Map<String, String> request, Principal principal){

        return companyUserService.fetchServiceRateUnit(request, principal);


    }

    @PostMapping(EndpointPropertyKey.ADD_SERVICE)
    public ResponseEntity<GlobalResponseDTO> addService(@RequestBody Map<String, String> request,Principal principal){
        String tenantId = (String) request.get("tenantId");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            String authorityName = authority.getAuthority();
            List<String> priviledgeChk = staffQuery.priviledgeChkForService(tenantId);
            //boolean hasEditPrivilege = false;
            boolean hasEditPrivilege = true;
            for (String privilege : priviledgeChk) {
                if (privilege.equalsIgnoreCase("ADD")) {
                    hasEditPrivilege = true;
                    break;
                }
            }
            if (authorityName.equalsIgnoreCase("Staff") && !hasEditPrivilege) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new GlobalResponseDTO(false, "A Staff user dont have the priviledge to ADD the service.", null));
            }
        }
        return companyUserService.addService(request, principal);


    }

    @PostMapping(EndpointPropertyKey.addServiceEnity)
    public ResponseEntity<GlobalResponseDTO> addCustomerAndServiceEnity(@RequestBody Map<String, Object> request){

        return companyUserService.addCustomerAndService(request);

    }

    @PostMapping(EndpointPropertyKey.get_Customer_Details)
    public ResponseEntity<GlobalResponseDTO> getCustomerDetails(@RequestBody Map<String, Object> request, Principal principal){

        return companyUserService.getCustomerDetails(request,principal);

    }

    @PostMapping(EndpointPropertyKey.update_Customer_Details)
    public ResponseEntity<GlobalResponseDTO> updateCustomerDetails(@RequestBody Map<String, Object> request, Principal principal) {
        return companyUserService.updateCustomerDetails(request, principal);
    }

    @PostMapping(EndpointPropertyKey.delete_Customer)
    public ResponseEntity<GlobalResponseDTO> deleteCustFromDB(@RequestBody Map<String, Object> request, Principal principal) {
        return companyUserService.deleteCustFromDB(request, principal);
    }

    @PostMapping(EndpointPropertyKey.update_Service_Object_Details)
    public ResponseEntity<GlobalResponseDTO> updateServiceObjectDetails(@RequestBody Map<String, Object> request, Principal principal) {
        return companyUserService.updateServiceObjectDetails(request, principal);
    }

    @PostMapping(EndpointPropertyKey.get_Active_Inactive_Status_For_Customer)
    public ResponseEntity<GlobalResponseDTO> getActiveInactiveStatusForCustomer(@RequestBody Map<String, Object> request, Principal principal) {
        return companyUserService.getActiveInactiveStatusForCustomer(request, principal);
    }

    @PostMapping(EndpointPropertyKey.update_Active_Inactive_Status_For_Customer)
    public ResponseEntity<GlobalResponseDTO> UpdateActiveInactiveStatusForCustomer(@RequestBody Map<String, Object> request, Principal principal) {
        return companyUserService.UpdateActiveInactiveStatusForCustomer(request, principal);
    }

    @PostMapping(EndpointPropertyKey.GET_SERVICE_DETAILS)
    public ResponseEntity<GlobalResponseDTO> getServiceDetails(@RequestBody Map<String, String> request, Principal principal) {
        return companyUserService.getServiceDetails(request, principal);
    }

    @PostMapping(EndpointPropertyKey.UPDATE_SERVICE_DETAILS)
    public ResponseEntity<GlobalResponseDTO> updateServiceDetails(@RequestBody Map<String, String> request, Principal principal) {
        return companyUserService.updateServiceDetails(request, principal);
    }

    @PostMapping(EndpointPropertyKey.DELETE_SERVICE)
    public ResponseEntity<GlobalResponseDTO> deleteServiceDetails(@RequestBody Map<String, String> request, Principal principal) {
        return companyUserService.deleteServiceDetails(request, principal);
    }


}
