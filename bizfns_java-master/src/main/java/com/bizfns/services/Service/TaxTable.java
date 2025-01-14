package com.bizfns.services.Service;

import com.bizfns.services.GlobalDto.GlobalResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Map;



public interface TaxTable {

    public ResponseEntity<GlobalResponseDTO> addTaxTable(Map<String, Object> request, Principal principal);
    ResponseEntity<GlobalResponseDTO> getTaxTable(String tenantId, String userId);

    public ResponseEntity<GlobalResponseDTO> updateTaxTable(Map<String, Object> request, Principal principal);

    public ResponseEntity<GlobalResponseDTO> deleteTaxTable(Map<String, Object> request, Principal principal);


}
