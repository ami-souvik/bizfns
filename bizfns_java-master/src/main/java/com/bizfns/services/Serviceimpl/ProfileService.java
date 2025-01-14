package com.bizfns.services.Serviceimpl;

import com.bizfns.services.GlobalDto.GlobalResponseDTO;
import com.bizfns.services.Module.ProfileResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Map;

public interface ProfileService {
    public ResponseEntity<GlobalResponseDTO> getProfile(String tenantId, String userId, String businessEmail, Principal principal);

    ResponseEntity<GlobalResponseDTO> saveMasterProfile(Map<String , Object> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> saveChangesMobile(Map<String, Object> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> saveChangesSubscriptionPlan(Map<String, Object> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> verifyPassword(Map<String, Object> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> getOtpForMobileChanges(Map<String, Object> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> saveImagePath(MultipartFile[] file, String tenantId, String companyId, Principal principal) throws IOException;
}
