package com.bizfns.services.Serviceimpl;

import com.bizfns.services.GlobalDto.GlobalResponseDTO;
import com.bizfns.services.Query.AdminQuery;
import com.bizfns.services.Service.AdminService;
import com.bizfns.services.Utility.AccessTokenValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AccessTokenValidation token;

    @Autowired
    private AdminQuery adminQuery;

    /**
     * It retrieves all the registered company names based on the provided tenantId.
     *
     * @param request   The request containing parameters, particularly the tenantId.
     * @param principal The Principal object containing the authenticated user's information.
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> getAllCompanyRegistered(Map<String, String> request, Principal principal) {
        String tanentId=request.get("tanentId");

        if(token.checkUserMatch(tanentId, principal.getName())){
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorised user, we could not access the APIs from others token "));
        }

        List<Map<String, Object>> allDta= adminQuery.getAllRegisteredCompanyName(tanentId);
        return ResponseEntity.accepted()
                .body(new GlobalResponseDTO(true, "Successfully Fetched all the Company Name", allDta));

    }

    /**
     * This method saves the user privileges for a user based on userType(Staff) in the database.
     *
     * @param request   A map containing tenantId, userId, userType, and privilegeIds.
     * @param principal Principal object representing the authenticated user.
     * @return ResponseEntity containing a GlobalResponseDTO indicating success or failure of saving user privileges.
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> saveUserPrivileges(Map<String, String> request, Principal principal) {
        String tenantId = request.get("tenantId");
        String userIdString = request.get("userId");
        String userType = request.get("userType");
        String privilegeIds = request.get("priviledgeAssigned");
        String phoneNumber =  request.get("phoneNumber");
        try {
            if (token.checkUserMatch(tenantId, principal.getName())) {
                return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorized user, we could not access the APIs from other tokens"));
            }
            if(phoneNumber.isEmpty()){
                String[] privilegeIdArray = privilegeIds.split(",");
                List<Map<String, Object>> allResults = new ArrayList<>();
                for (String privilegeId : privilegeIdArray) {
                    String privilegeInfo = adminQuery.findPriviledgeInfo(privilegeId);
                    if (privilegeInfo == null) {
                        return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Privilege not found for privilege ID: " + privilegeId));
                    }
                    String[] parts = privilegeInfo.split(",");
                    String privilegeName = parts[0].trim();
                    String privilegeType = parts[1].trim();
                    if (privilegeName.isEmpty()) {
                        return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Privilege name is empty for privilege ID: " + privilegeId));
                    }
                    List<Map<String, Object>> result = adminQuery.assignUserPrivilege(userType, privilegeId, privilegeType, tenantId,phoneNumber);
                    if (result == null) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GlobalResponseDTO(false, "Error assigning privilege ID: " + privilegeId));
                    }
                    allResults.addAll(result);
                }
                return ResponseEntity.accepted().body(new GlobalResponseDTO(true, "Privileges assigned by the user " + userIdString + ":-", allResults));
            }else{
                String[] privilegeIdArray = privilegeIds.split(",");
                List<Map<String, Object>> allResults = new ArrayList<>();
                for (String privilegeId : privilegeIdArray) {
                    String privilegeInfo = adminQuery.findPriviledgeInfo(privilegeId);
                    if (privilegeInfo == null) {
                        return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Privilege not found for privilege ID: " + privilegeId));
                    }
                    String[] parts = privilegeInfo.split(",");
                    String privilegeName = parts[0].trim();
                    String privilegeType = parts[1].trim();
                    if (privilegeName.isEmpty()) {
                        return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Privilege name is empty for privilege ID: " + privilegeId));
                    }
                    List<Map<String, Object>> result = adminQuery.assignUserPrivilege(userType, privilegeId, privilegeType, tenantId, phoneNumber);
                    if (result == null) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GlobalResponseDTO(false, "Error assigning privilege ID: " + privilegeId));
                    }
                    allResults.addAll(result);
                }
                return ResponseEntity.accepted().body(new GlobalResponseDTO(true, "Privileges assigned by the user " + userIdString + ":-", allResults));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GlobalResponseDTO(false, "An error occurred while processing the request: " + e.getMessage()));
        }
    }

    /**
     * This method retrieves privileges assigned to a specific tenant based on the provided tenantId.
     *
     * @param request   A map containing the tenantId.
     * @param principal Principal object representing the authenticated user.
     * @return ResponseEntity containing a GlobalResponseDTO indicating success or failure of retrieving assigned privileges.
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> getAssignedPriviledges(Map<String, String> request, Principal principal) {
        try {
            String tenantId = request.get("tenantId");
            String phoneNumber = request.get("phoneNumber");
            if (tenantId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing tenantId in the request");
            }
            if (token.checkUserMatch(tenantId, principal.getName())) {
                return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorized user, we could not access the APIs from other tokens"));
            }
            List<Map<String, Object>> result = adminQuery.getAllAssignedPrivileges(tenantId,phoneNumber);
            return ResponseEntity.accepted().body(new GlobalResponseDTO(true, "Privilege assigned to the tenant " + tenantId + " :-", result));
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatus()).body(new GlobalResponseDTO(false, ex.getReason()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GlobalResponseDTO(false, "An unexpected error occurred: " + ex.getMessage()));
        }
    }
}
