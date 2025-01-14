package com.bizfns.services.Controller;

import com.bizfns.services.Service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import com.bizfns.services.GlobalDto.GlobalResponseDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.security.Principal;
import java.util.Map;

@Controller
public class AdminController {
    @Autowired
    private AdminService adminService;
    @PostMapping(EndpointPropertyKey.Get_All_Schemas)
    public ResponseEntity<GlobalResponseDTO> getAllRegisteredCompany(
            @RequestBody Map<String, String> request, Principal principal)  {
        return adminService.getAllCompanyRegistered(request, principal);
    }

    @PostMapping(EndpointPropertyKey.save_user_priviledges)
    public ResponseEntity<GlobalResponseDTO> saveUserPriviledges(
            @RequestBody Map<String, String> request, Principal principal)  {
        return adminService.saveUserPrivileges(request, principal);
    }

    @PostMapping(EndpointPropertyKey.get_assigned_priviledges)
    public ResponseEntity<GlobalResponseDTO> getAssignedPriviledges(
            @RequestBody Map<String, String> request, Principal principal)  {
        return adminService.getAssignedPriviledges(request, principal);
    }

}
