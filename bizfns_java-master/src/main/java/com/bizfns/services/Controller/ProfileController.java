package com.bizfns.services.Controller;

import com.bizfns.services.GlobalDto.GlobalResponseDTO;
import com.bizfns.services.Module.ProfileRequest;
import com.bizfns.services.Module.ProfileResponseDto;
import com.bizfns.services.Serviceimpl.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.Multipart;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Map;

@RestController
@RequestMapping("api/profile")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @PostMapping(EndpointPropertyKey.get_profile)
    public ResponseEntity<GlobalResponseDTO> getProfile(@RequestBody ProfileRequest profileRequest, Principal principal) {
        ResponseEntity<GlobalResponseDTO> responseEntity = profileService.getProfile(profileRequest.getTenantId(), profileRequest.getUserId(),profileRequest.getBusinessEmail(),principal);
        return responseEntity;
    }

    @PostMapping(EndpointPropertyKey.save_master_profile)
    public ResponseEntity<GlobalResponseDTO> saveMasterProfile(@RequestBody Map<String, Object> request, Principal principal) {
        return  profileService.saveMasterProfile(request,  principal);
        // You might want to return the responseEntity directly or perform additional logic here

    }

    @PostMapping("/saveChangesMobile")
    public ResponseEntity<GlobalResponseDTO> saveChangesMobile(@RequestBody Map<String, Object> request, Principal principal) {

        return profileService.saveChangesMobile(request,  principal);
        // You might want to return the responseEntity directly or perform additional logic here

    }

    @PostMapping(EndpointPropertyKey.get_otp_for_mobile_changes)
    public ResponseEntity<GlobalResponseDTO> getOtpForMobileChanges(@RequestBody Map<String, Object> request, Principal principal) {

        return profileService.getOtpForMobileChanges(request, principal);
        // You might want to return the responseEntity directly or perform additional logic here

    }

    @PostMapping(EndpointPropertyKey.verify_password)
    public ResponseEntity<GlobalResponseDTO> verifyPassword(@RequestBody Map<String, Object> request, Principal principal) {


        return  profileService.verifyPassword(request,  principal);
        // You might want to return the responseEntity directly or perform additional logic here

    }

    @PostMapping("/saveChangesSubscriptionPlan")
    public ResponseEntity<GlobalResponseDTO> saveChangesSubscriptionPlan(@RequestBody Map<String, Object> request, Principal principal) {

        profileService.saveChangesSubscriptionPlan(request,  principal);
        // You might want to return the responseEntity directly or perform additional logic here
        return null;
    }

    @PostMapping(value = EndpointPropertyKey.upload_business_logo)
    public ResponseEntity<GlobalResponseDTO> uploadBusinessLogo(
             MultipartFile[] businessLogo,
             String tenantId,
             String userId , Principal principal) throws IOException {

//        if (file.isEmpty()) {
//            // Handle the case when no file is provided
//            return ResponseEntity.badRequest().body(new GlobalResponseDTO("No file provided for upload"));
//        }

       return profileService.saveImagePath(businessLogo, tenantId, userId,  principal);

        //return ResponseEntity.ok(new GlobalResponseDTO("File uploaded successfully"));
    }





}
