package com.bizfns.services.Service;

import com.bizfns.services.GlobalDto.GlobalResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Map;

public interface AdminService {

    ResponseEntity<GlobalResponseDTO> getAllCompanyRegistered(Map<String, String> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> saveUserPrivileges(Map<String, String> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> getAssignedPriviledges(Map<String, String> request, Principal principal);
}
