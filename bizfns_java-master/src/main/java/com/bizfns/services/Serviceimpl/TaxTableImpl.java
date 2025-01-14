package com.bizfns.services.Serviceimpl;

import com.bizfns.services.GlobalDto.GlobalResponseDTO;
import com.bizfns.services.Query.TaxTableQuery;
import com.bizfns.services.Repository.CompanyUserRepository;
import com.bizfns.services.Service.TaxTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TaxTableImpl implements TaxTable {

    @Autowired
    private CompanyUserRepository companyUserRepository;

    @Autowired
    private TaxTableQuery taxTableQuery;

    @Override
    public ResponseEntity<GlobalResponseDTO> addTaxTable(Map<String, Object> request, Principal principal) {
        // Extract request parameters
        String tenantId = (String) request.get("tenantId");
        String userId = (String) request.get("userId");
        String taxMasterName = (String) request.get("taxMasterName");
        Double taxMasterRate = Double.valueOf(request.get("taxMasterRate").toString());

        int userIdInt = Integer.parseInt(userId);


        // Validate user and tenant ID
       String dbCompBusinessId = (companyUserRepository.dbCompBusinessId( userId, tenantId));

        if (dbCompBusinessId == null) {
            return ResponseEntity.accepted().body(new GlobalResponseDTO(false, "Invalid user ID or tenant ID."));
        }

        // Insert tax master data
        Integer taxMasterId = taxTableQuery.insertTaxMasterData(tenantId, taxMasterName, taxMasterRate, userIdInt);

        if (taxMasterId != null) {
            Map<String, Object> responseTaxData = new HashMap<>();
            responseTaxData.put("taxMasterId", taxMasterId);
            responseTaxData.put("taxMasterName", taxMasterName);
            return ResponseEntity.accepted()
                    .body(new GlobalResponseDTO(true, "Tax added successfully.", responseTaxData));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GlobalResponseDTO(false, "Failed to add tax."));
        }
    }

    // Method to check if user matches
    private boolean checkUserMatch(String userId, String tenantId, String username) {
        // Implement your logic to check if the user is authorized
        return true; // Example implementation, replace with actual logic
    }




    @Override
    public ResponseEntity<GlobalResponseDTO> getTaxTable(String tenantId, String userId) {


        // Validate user and tenant ID
        String dbCompBusinessId = companyUserRepository.dbCompBusinessId(userId, tenantId);
        if (dbCompBusinessId == null) {
            return ResponseEntity.accepted().body(new GlobalResponseDTO(false, "Invalid user ID or tenant ID."));
        }
        List<Map<String, Object>> taxTableData = taxTableQuery.fetchTaxTableData(tenantId);

        if (taxTableData != null && !taxTableData.isEmpty()) {
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("TaxTable", taxTableData);
            return ResponseEntity.ok().body(new GlobalResponseDTO(true, "Tax table values retrieved successfully", responseData));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new GlobalResponseDTO(true,""));
        }
    }




    @Override
    public ResponseEntity<GlobalResponseDTO> updateTaxTable(Map<String, Object> request, Principal principal) {
        String tenantId = (String) request.get("tenantId");
        String userId = (String) request.get("userId");
        List<Map<String, Object>> taxUpdates = (List<Map<String, Object>>) request.get("taxUpdates");



        // Validate user and tenant ID
        String dbCompBusinessId = (companyUserRepository.dbCompBusinessId(userId, tenantId));

        if (dbCompBusinessId == null) {
            return ResponseEntity.accepted().body(new GlobalResponseDTO(false, "Invalid user ID or tenant ID."));
        }

        // Update tax master data
        boolean updateSuccess = taxTableQuery.updateTaxMasterData(tenantId, taxUpdates);

        if (updateSuccess) {
            return ResponseEntity.accepted().body(new GlobalResponseDTO(true, "Tax updated successfully."));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GlobalResponseDTO(false, "Failed to update tax."));
        }
    }

    @Override
    public ResponseEntity<GlobalResponseDTO> deleteTaxTable(Map<String, Object> request, Principal principal) {
        String tenantId = (String) request.get("tenantId");
        String userId = (String) request.get("userId");
        String taxTypeId = (String) request.get("taxTypeId");

        // Check if user is authorized
        if (!checkUserMatch(userId, tenantId, principal.getName())) {
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorized user."));
        }

        // Validate user and tenant ID
        String dbCompBusinessId = (companyUserRepository.dbCompBusinessId(userId, tenantId));

        if (dbCompBusinessId == null) {
            return ResponseEntity.accepted().body(new GlobalResponseDTO(false, "Invalid user ID or tenant ID."));
        }

        // Delete tax master data
        boolean deleteSuccess = taxTableQuery.deleteTaxMasterData(tenantId, taxTypeId);

        if (deleteSuccess) {
            return ResponseEntity.accepted().body(new GlobalResponseDTO(true, "Tax deleted successfully."));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GlobalResponseDTO(false, "Failed to delete tax."));
        }
    }



}
