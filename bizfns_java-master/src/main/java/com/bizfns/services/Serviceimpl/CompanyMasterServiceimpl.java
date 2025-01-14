package com.bizfns.services.Serviceimpl;

import com.bizfns.services.GlobalDto.GlobalResponseDTO;
import com.bizfns.services.Entity.CompanyMasterEntity;
import com.bizfns.services.Query.StaffQuery;
import com.bizfns.services.Repository.CompanyMasterRepository;
import com.bizfns.services.Repository.ProfileRepository;
import com.bizfns.services.Service.CompanyMasterService;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Service
public class CompanyMasterServiceimpl implements CompanyMasterService {
    @Autowired
    private CompanyMasterRepository companyMasterRepository;
    @Autowired
    private ErrorLogServiceImpl errorLogService;

    @Autowired
    private StaffQuery staffQuery;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private ProfileRepository profileRepository;

//    @Override
//    public ResponseEntity<GlobalResponseDTO> forgotBusinessId(Map<String, String> request) {
//        // Extract device information from the request
//        String deviceId = request.get("deviceId");
//        String deviceType = request.get("deviceType");
//        String appVersion = request.get("appVersion");
//        String userId = request.get("userId");
//        String deviceInfo = "deviceId = " + deviceId + "\n" + "deviceType = " + deviceType + "\n" + "appVersion = " + appVersion;
//        String fullURL = httpServletRequest.getRequestURL().toString();
//        List<JSONObject> userType = companyMasterRepository.fetchUserType(userId);
//        List<JSONObject> tenantId = companyMasterRepository.fetchTenentId(userId);
//        if (!userType.isEmpty() && (userType.get(0).get("USER_TYPE").equals("staff") || userType.get(0).get("USER_TYPE").equals("Staff"))) {
//            String isPasswordChange = staffQuery.isPasswordChange(userId, (String) tenantId.get(0).get("SCHEMA_NAME"));
//            if(isPasswordChange.equals("N")){
//                List<JSONObject> fetchBusinessIdDetails = companyMasterRepository.fetchBusinessIdDetails(userId);
//                List<JSONObject> transformedData = new ArrayList<>();
//                for (JSONObject businessIdDetail : fetchBusinessIdDetails) {
//                    String schemaName = (String) businessIdDetail.get("SCHEMA_NAME");
//                    String businessName = (String) businessIdDetail.get("BUSINESS_NAME");
//                    JSONObject transformedItem = new JSONObject();
//                    transformedItem.put("SCHEMA_NAME", schemaName);
//                    transformedItem.put("BUSINESS_NAME", businessName);
//                    transformedData.add(transformedItem);
//                }
//                return ResponseEntity.ok(new GlobalResponseDTO(false, "Please set your password first", transformedData));
//            }else{
//                try {
//                    List<JSONObject> fetchUserIdValidation = companyMasterRepository.fetchUserIdValidation(userId);
//                    if (fetchUserIdValidation.isEmpty()) {
//                        return ResponseEntity.accepted()
//                                .body(new GlobalResponseDTO(false, "Please enter a valid user Id", null));
//                    }
//                    List<JSONObject> fetchBusinessIdDetails = companyMasterRepository.fetchBusinessIdDetails(userId);
//                    List<JSONObject> transformedData = new ArrayList<>();
//                    for (JSONObject businessIdDetail : fetchBusinessIdDetails) {
//                        String schemaName = (String) businessIdDetail.get("SCHEMA_NAME");
//                        String businessName = (String) businessIdDetail.get("BUSINESS_NAME");
//                        JSONObject transformedItem = new JSONObject();
//                        transformedItem.put("SCHEMA_NAME", schemaName);
//                        transformedItem.put("BUSINESS_NAME", businessName);
//                        transformedData.add(transformedItem);
//                    }
//                    return ResponseEntity.accepted()
//                            .body(new GlobalResponseDTO(true, "Success", transformedData));
//                } catch (Exception e) {
//                    errorLogService.errorLog(request, e.getMessage(), fullURL, userId, deviceInfo);
//                    return ResponseEntity.ok(new GlobalResponseDTO(false, "Success", null));
//                }
//            }
//        } else {
//            try {
//                List<JSONObject> fetchUserIdValidation = companyMasterRepository.fetchUserIdValidation(userId);
//                if (fetchUserIdValidation.isEmpty()) {
//                    return ResponseEntity.accepted()
//                            .body(new GlobalResponseDTO(false, "The number is not associated with any business.", null));
//                }
//                List<JSONObject> fetchBusinessIdDetails = companyMasterRepository.fetchBusinessIdDetails(userId);
//                List<JSONObject> transformedData = new ArrayList<>();
//                for (JSONObject businessIdDetail : fetchBusinessIdDetails) {
//                    String schemaName = (String) businessIdDetail.get("SCHEMA_NAME");
//                    String businessName = (String) businessIdDetail.get("BUSINESS_NAME");
//                    JSONObject transformedItem = new JSONObject();
//                    transformedItem.put("SCHEMA_NAME", schemaName);
//                    transformedItem.put("BUSINESS_NAME", businessName);
//                    transformedData.add(transformedItem);
//                }
//                return ResponseEntity.accepted()
//                        .body(new GlobalResponseDTO(true, "Success", transformedData));
//            } catch (Exception e) {
//                errorLogService.errorLog(request, e.getMessage(), fullURL, userId, deviceInfo);
//                return ResponseEntity.ok(new GlobalResponseDTO(false, "Success", null));
//            }
//        }
//    }

    /*
     * Retrieves business IDs associated with the provided userId, checking if the user is staff or not.
     * If the user is staff and has not yet set their password for any tenant, prompts them to do so and
     * returns the list of tenants requiring password setup. Otherwise, retrieves and returns the list of
     * business IDs associated with the userId.
     *
     * @param request USERID
     */
@Override
    public ResponseEntity<GlobalResponseDTO> forgotBusinessId(Map<String, String> request) {
// Extract device information from the request
        String deviceId = request.get("deviceId");
        String deviceType = request.get("deviceType");
        String appVersion = request.get("appVersion");
        String userId = request.get("userId");
        String deviceInfo = "deviceId = " + deviceId + "\n" + "deviceType = " + deviceType + "\n" + "appVersion = " + appVersion;
        String fullURL = httpServletRequest.getRequestURL().toString();
        List<JSONObject> userType = companyMasterRepository.fetchUserType(userId);
        List<JSONObject> tenantId = companyMasterRepository.fetchTenentId(userId);
        try {
            boolean isStaff = userType.stream()
                    .map(obj -> (String) obj.get("USER_TYPE"))
                    .anyMatch(userType1 -> "staff".equalsIgnoreCase(userType1));

           // ArrayList<String> tenantList = new ArrayList<>();
            if (isStaff) {

                ArrayList<String> tenantList = new ArrayList<>();
                List<JSONObject> transformedData = new ArrayList<>();

                for (JSONObject tenant : tenantId) {
                    String isPasswordChange = staffQuery.isPasswordChange(userId, (String) tenant.get("SCHEMA_NAME"));
                    if ("N".equals(isPasswordChange)) {

                        tenantList.add(String.valueOf(tenant.get("SCHEMA_NAME")));
                        List<JSONObject> fetchBusinessIdDetails = companyMasterRepository.fetchBusinessIdDetails(userId);

                        // Clear transformedData to avoid duplicate entries
                        transformedData.clear();

                        for (JSONObject businessIdDetail : fetchBusinessIdDetails) {
                            String schemaName = (String) businessIdDetail.get("SCHEMA_NAME");
                            String businessName = (String) businessIdDetail.get("BUSINESS_NAME");
                            JSONObject transformedItem = new JSONObject();
                            transformedItem.put("SCHEMA_NAME", schemaName);
                            transformedItem.put("BUSINESS_NAME", businessName);
                            transformedData.add(transformedItem);
                        }
                    }

                }
                if (!tenantList.isEmpty()) {
                    return ResponseEntity.ok(new GlobalResponseDTO(true, "Please set your password first : "  + Arrays.toString(tenantList.toArray()), transformedData));

                }
            }

            List<JSONObject> fetchUserIdValidation = companyMasterRepository.fetchUserIdValidation(userId);
            if (fetchUserIdValidation.isEmpty()) {
                return ResponseEntity.accepted().body(new GlobalResponseDTO(false, "Please enter a valid user Id", null));
            }

            List<JSONObject> fetchBusinessIdDetails = companyMasterRepository.fetchBusinessIdDetails(userId);
            List<JSONObject> transformedData = new ArrayList<>();
            for (JSONObject businessIdDetail : fetchBusinessIdDetails) {
                String schemaName = (String) businessIdDetail.get("SCHEMA_NAME");
                String businessName = (String) businessIdDetail.get("BUSINESS_NAME");
                JSONObject transformedItem = new JSONObject();
                transformedItem.put("SCHEMA_NAME", schemaName);
                transformedItem.put("BUSINESS_NAME", businessName);
                transformedData.add(transformedItem);
            }
            return ResponseEntity.accepted().body(new GlobalResponseDTO(true, "Success", transformedData));

        } catch (Exception e) {
            errorLogService.errorLog(request, e.getMessage(), fullURL, userId, deviceInfo);
            return ResponseEntity.ok(new GlobalResponseDTO(false, "Something went wrong", null));
        }
    }

//    @Override
//    public ResponseEntity<GlobalResponseDTO> getClientList(int page, int size) {
//
//        //Pageable pageable = PageRequest.of(page, size);
//       // Page<Map<String, Object>> clientPage = profileRepository.findAll(pageable);
//        List<Map<String, Object>> clientListQuery = profileRepository.getClientListQuery(page, size);
//
//        return ResponseEntity.ok(new GlobalResponseDTO(true, "success",clientListQuery));
//
//    }

    /*public ResponseEntity<GlobalResponseDTO> getClientList(int page, int size) {

//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
//         System.err.println("authorityttt : :" +authorities);
//        for (GrantedAuthority authority : authorities){
//           String authority1 = authority.getAuthority();
//           System.err.println("suthorirty : : :" +authority1);
//       }

        List<Map<String, Object>> clientListQuery = profileRepository.getClientListQuery(page, size);
        return ResponseEntity.ok().body(new GlobalResponseDTO(true,"success",clientListQuery));
    }*/


    /**
     * AMIT KUMAR SINGH
     * This method retrieves client details by business name or returns a list of all client business names if no business name is provided.
     *
     * @param businessName The business name to search for client details.
     * @return ResponseEntity containing a GlobalResponseDTO indicating success or failure with the retrieved client details.
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> getClientListByCompanyName(String businessName) {

        if (!isNullOrEmpty(businessName)){
            List<Map<String, Object>> clientDetail= profileRepository.getClientDetailByBusinessNameQuery(businessName);


        //List<Map<String, Object>> clientDetailQuery = profileRepository.getClientDetailByBusinessNameQuery(businessName);

        System.err.println("clientList :  :  " +clientDetail);
        return ResponseEntity.ok().body(new GlobalResponseDTO(true, "success",clientDetail));
    }
        else {
            List<Map<String, Object>> clientBusinessNameListQuery = profileRepository.getClientBusinessNameListQuery();
            ArrayList<String> businessNameList= new ArrayList<>();
            for (Map<String,Object> businessNameMap:clientBusinessNameListQuery){
                String businessName1 = (String) businessNameMap.get("BUSINESS_NAME");
                businessNameList.add(businessName1);
            }

            Map<String, Object> businessNameMap= new HashMap<>();
            businessNameMap.put("companyBusinessName", businessNameList);

            return ResponseEntity.ok().body(new GlobalResponseDTO(true, "success",businessNameMap));

        }

    }

    public boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }


}

