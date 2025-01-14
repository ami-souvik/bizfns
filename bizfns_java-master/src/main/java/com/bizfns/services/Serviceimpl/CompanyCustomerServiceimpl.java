package com.bizfns.services.Serviceimpl;
import com.bizfns.services.GlobalDto.GlobalResponseDTO;
import com.bizfns.services.Query.CustQuery;
import com.bizfns.services.Repository.CompanyCustomerRepository;
import com.bizfns.services.Repository.ProfileRepository;
import com.bizfns.services.Service.CompanyCustomerService;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.*;

@Service
public class CompanyCustomerServiceimpl implements CompanyCustomerService {

    private final CompanyCustomerRepository customerRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private CompanyCustomerRepository companyCustomerRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private CustQuery custQuery;

    public CompanyCustomerServiceimpl(CompanyCustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }


    @Override
    public ResponseEntity<GlobalResponseDTO> fetchCustomerList(Map<String, String> request, Principal principal) {

        String deviceId = request.get("deviceId");
        String deviceType = request.get("deviceType");
        String appVersion = request.get("appVersion");
        String userId = request.get("userId");
        String tenantId = request.get("tenantId");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            String authorityName = authority.getAuthority();
            List<String> priviledgeChk = custQuery.priviledgeChkForCustomer(tenantId);
            boolean hasEditPrivilege = false;
            for (String privilege : priviledgeChk) {
                if (privilege.equalsIgnoreCase("VIEW")) {
                    hasEditPrivilege = true;
                    break;
                }
            }
            if (authorityName.equalsIgnoreCase("Staff") && !hasEditPrivilege) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new GlobalResponseDTO(false, "A Staff user dont have the priviledge to fetch the customer list.", null));
            }
        }
        if(checkUserMatch(userId,tenantId, principal.getName())){
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorised user, we could not access the APIs from others token "));
        }

        List<JSONObject> userWithTenantValidation = companyCustomerRepository.userWithTenantValidation(tenantId, userId);


        if (userWithTenantValidation.isEmpty()){

            Map<String, Object> userIdAndComId = profileRepository.getCompanyIdFromCompanyMaster(tenantId);

            if (userIdAndComId.isEmpty()){

                return ResponseEntity.ok().body(new GlobalResponseDTO(false, "User Not Found for this staff : " +userId));
            }
            String userIdAssociated = (String) userIdAndComId.get("COMPANY_BACKUP_PHONE_NUMBER");
            int companyId = (Integer) userIdAndComId.get("COMPANY_ID");

            //Map<String, Object> staffProfile = profileRepository.getStaffProfile(userId, tenantId);


            //System.err.println("Staff profile");
            return fetchCustomerListBoth(deviceId,deviceType,appVersion,userIdAssociated, tenantId);
            //return   ResponseEntity.ok().body(new GlobalResponseDTO(true, "staff profile", staffProfile));
        } else {
            return fetchCustomerListBoth(deviceId,deviceType,appVersion,userId, tenantId);

        }
    }

        public ResponseEntity<GlobalResponseDTO> fetchCustomerListBoth(String deviceId, String deviceType, String appVersion , String userId , String tenantId) {

        List<JSONObject> userWithTenantValidation = companyCustomerRepository.userWithTenantValidation(tenantId, userId);


        if (userWithTenantValidation.isEmpty()) {
            return ResponseEntity.accepted().body(new GlobalResponseDTO(false, "Please enter correct Business Id ", null));
        }

        String invoiceId = "";
        String invoiceNo = "";
        String invoiceAmount = "";
        String invoiceStatus = "";
        String lifetimeAmount = "";

        String tenantFirstEightTLetters = null;
        if (tenantId != null && tenantId.length() >= 8) {
            tenantFirstEightTLetters = tenantId.substring(0, 8);

        }

        List<Map<String, Object>> recordForCustList = custQuery.recordForCustList(tenantFirstEightTLetters);

        List<Map<String, Object>> responseList = new ArrayList<>();

        for (Map<String, Object> customerRecord : recordForCustList) {
            List<Map<String, Object>> unpaidInvoices = new ArrayList<>();


            Map<String, Object> unpaidInvoice = new HashMap<>();
            unpaidInvoice.put("invoiceId", invoiceId);
            unpaidInvoice.put("invoiceNo", invoiceNo);
            unpaidInvoice.put("invoiceAmount", invoiceAmount);
            unpaidInvoice.put("invoiceStatus", invoiceStatus);
            unpaidInvoices.add(unpaidInvoice);

            Map<String, Object> customerData = new HashMap<>();
            customerData.put("customerId", customerRecord.get("PK_CUSTOMER_ID"));
            customerData.put("customerFirstName", customerRecord.get("CUSTOMER_FIRST_NAME"));
            customerData.put("customerLastName", customerRecord.get("CUSTOMER_LAST_NAME"));
            customerData.put("customerEmail", customerRecord.get("CUSTOMER_EMAIL"));
            customerData.put("customerPhoneNo", customerRecord.get("CUSTOMER_PHONE_NUMBER"));
            customerData.put("customerAddress", "");
            customerData.put("activeStatus", customerRecord.get("CUSTOMER_STATUS"));
            // You can add address data if available
            customerData.put("unpaidInvoice", unpaidInvoices);
            customerData.put("lifetimeAmount", lifetimeAmount);
            customerData.put("customerCreatedAt", customerRecord.get("COMPANY_CREATED_AT"));

            responseList.add(customerData);
        }


        return ResponseEntity.accepted()
                .body(new GlobalResponseDTO(true, "Success", responseList));

    }

    @Override
    public ResponseEntity<GlobalResponseDTO> customerHistoryRecordList(Map<String, String> request, Principal principal) {

        String deviceId = request.get("deviceId");
        String deviceType = request.get("deviceType");
        String appVersion = request.get("appVersion");
        String userId = request.get("userId");
        String tenantId = request.get("tenantId");

        if(checkUserMatch(userId,tenantId, principal.getName())){
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorised user, we could not access the APIs from others token "));
        }

        String fromDate = request.get("fromDate");
        String toDate = request.get("toDate");
        String customerId = request.get("customerId");

//        List<Map<String, Object>> recordForCustJobHistList = custQuery.recordForCustJobHistList(tenantId, customerId, formDate, toDate);
//
//
//        Map<String, Object> recordForCustDetails = custQuery.recordForCustDetails(tenantId, customerId);
//
//
//        Map<String, Object> recordForServiceDetails = custQuery.recordForServiceDetails(tenantId, customerId);
//
//
//


//
//
//        List<Map<String, Object>> recordForCustJobHistList = custQuery.recordForCustJobHistList(tenantId, customerId, formDate, toDate);
//        Map<String, Object> recordForCustDetails = custQuery.recordForCustDetails(tenantId, customerId);
//        Map<String, Object> recordForServiceDetails = custQuery.recordForServiceDetails(tenantId, customerId);
//
//        List<HashMap<String, Object>> responseList = new ArrayList<>();
//
//        for (Map<String, Object> jobHist : recordForCustJobHistList) {
//            HashMap<String, Object> responseItem = new HashMap<>();
//            responseItem.put("id", jobHist.get("PK_JOB_ID")); // Replace with actual column name
//            responseItem.put("startDate", jobHist.get("JOB_CREATED_AT")); // Replace with actual column name
//            responseItem.put("endDate", jobHist.get("JOB_STOP_ON")); // Replace with actual column name
//
//            HashMap<String, Object> customerDetails = new HashMap<>();
//            customerDetails.put("id", recordForCustDetails.get("PK_CUSTOMER_ID")); // Replace with actual column name
//            customerDetails.put("name", recordForCustDetails.get("CUSTOMER_FIRST_NAME") + " " + recordForCustDetails.get("CUSTOMER_LAST_NAME")); // Replace with actual column names
//            responseItem.put("customer", customerDetails);
//
//            HashMap<String, Object> serviceDetails = new HashMap<>();
//            serviceDetails.put("id", recordForServiceDetails.get("SERVICE_ID")); // Replace with actual column name
//            serviceDetails.put("name", recordForServiceDetails.get("SERVICE_NAME")); // Replace with actual column name
//            responseItem.put("serviceEntity", serviceDetails);
//
//            responseList.add(responseItem);
//        }
//
//        HashMap<String, Object> responseData = new HashMap<>();
//
//        responseData.put("data", responseList);


        List<Map<String, Object>> recordForCustHistList = custQuery.recordForCustHistList(tenantId, customerId, fromDate, toDate);

       // System.out.println("recordForCustHistList.size()");
       // System.out.println(recordForCustHistList.size());
       // System.out.println(recordForCustHistList.size());


        List<HashMap<String, Object>> responseList = new ArrayList<>();

        for (Map<String, Object> jobHist : recordForCustHistList) { // Correct variable name here
            HashMap<String, Object> responseItem = new HashMap<>();
            responseItem.put("id", jobHist.get("PK_JOB_ID"));
            responseItem.put("startDate", jobHist.get("JOB_CREATED_AT"));
            responseItem.put("endDate", jobHist.get("JOB_STOP_ON"));

            HashMap<String, Object> customerDetails = new HashMap<>();
            customerDetails.put("id", jobHist.get("PK_CUSTOMER_ID"));
            customerDetails.put("name", jobHist.get("CUSTOMER_FIRST_NAME") + " " + jobHist.get("CUSTOMER_LAST_NAME"));
            responseItem.put("customer", customerDetails);

            HashMap<String, Object> serviceDetails = new HashMap<>();
            serviceDetails.put("id", jobHist.get("SERVICE_ID")); // Assuming "SERVICE_ID" exists in the result
            serviceDetails.put("name", jobHist.get("SERVICE_NAME"));
            responseItem.put("serviceEntity", serviceDetails);

            responseList.add(responseItem);
        }




        return ResponseEntity.accepted()
                .body(new GlobalResponseDTO(true, "Success", responseList));
    }


    public boolean checkUserMatch(String userId,String tenantId , String username){

        String[] part = username.split(",");

        //String userPhone = profileRepository.checkAccessToken(userId, tenantId);

        String tokenUserId = part[0];
        String tokenTenantId = part[1];
        if (!tenantId.equals(tokenTenantId)){
            return true;
        }

        return false;
    }

}









