package com.bizfns.services.Controller;

import com.bizfns.services.GlobalDto.GlobalResponseDTO;
import com.bizfns.services.Query.ScheduleQuery;
import com.bizfns.services.Service.AddScheduleService;
import com.bizfns.services.Utility.JWTUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/*addNewSchedule
addMaterial
materialList
materialCategoryData
getAllRecurrDate
getcheckedRecurrDate*/

@RestController
@RequestMapping("/api/users")
public class AddScheduleController {

    private static final Logger logger = LoggerFactory.getLogger(AddScheduleController.class);
    @Autowired
    private AddScheduleService addScheduleService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private ScheduleQuery scheduleQuery;
    

    @PostMapping(EndpointPropertyKey.ADD_NEW_SCHEDULE)
    public ResponseEntity<GlobalResponseDTO> addNewSchedule(
            @RequestBody Map<String, Object> request,HttpServletRequest httpRequest, Principal principal) {
        String tenantId = (String) request.get("tenantId");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            String authorityName = authority.getAuthority();
            List<String> priviledgeChk = scheduleQuery.priviledgeChkForSchedule(tenantId);
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
                        .body(new GlobalResponseDTO(false, "A Staff user dont have the priviledge to ADD the schedule.", null));
            }
        }

        try {
            ResponseEntity<GlobalResponseDTO> response = addScheduleService.addNewSchedule(request,httpRequest, principal);
            boolean RES = response.getBody().getSuccess();
            if (!RES) {
                logger.error("Error occurred in API call {} (Status Code: {}): {}", EndpointPropertyKey.ADD_NEW_SCHEDULE,response.getStatusCodeValue(),response.getBody().getMessage());
            }
            return response;
        } catch (Exception e) {
            logger.error("An error occurred in the Add Schedule method: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GlobalResponseDTO(false, "An error occurred while processing the request", null));
        }
    }
    @PostMapping(EndpointPropertyKey.ADD_MATERIAL)
    public ResponseEntity<GlobalResponseDTO> addMaterial(
            @RequestBody Map<String, Object> request, Principal principal)  {
        String tenantId = (String) request.get("tenantId");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            String authorityName = authority.getAuthority();
            List<String> priviledgeChk = scheduleQuery.priviledgeChkForMaterial(tenantId);
            boolean hasEditPrivilege = false;
            for (String privilege : priviledgeChk) {
                if (privilege.equalsIgnoreCase("ADD")) {
                    hasEditPrivilege = true;
                    break;
                }
            }
            if (authorityName.equalsIgnoreCase("Staff") && !hasEditPrivilege) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new GlobalResponseDTO(false, "A Staff user dont have the priviledge to add the material.", null));
            }
        }
        return addScheduleService.addMaterial(request,principal);
    }


    @PostMapping(EndpointPropertyKey.MATERIAL_LIST)
    public ResponseEntity<GlobalResponseDTO> materialList(
            @RequestBody Map<String, String> request, Principal principal)  {
        return addScheduleService.materialList(request, principal);
    }

    @PostMapping(EndpointPropertyKey.MATERIAL_CATEGORY_DATA)
    public ResponseEntity<GlobalResponseDTO> materialCategoryData(
            @RequestBody Map<String, String> request,Principal principal)  {
        return addScheduleService.materialCategoryData(request, principal);
    }



    @PostMapping(EndpointPropertyKey.Get_All_recurrDate)
    public ResponseEntity<GlobalResponseDTO> getAllRecurrDate(@RequestBody Map<String, String> request,Principal principal) {
        return addScheduleService.getAllrecurrDate(request, principal);
    }

    @PostMapping(EndpointPropertyKey.Get_staff_checked_recurrDate)
    public ResponseEntity<GlobalResponseDTO> getcheckedRecurrDate(@RequestBody Map<String, Object> request, Principal principal) {
        return addScheduleService.recurrValidation(request, principal);
    }

    @PostMapping(EndpointPropertyKey.delete_material)
    public ResponseEntity<GlobalResponseDTO> deleteMaterial(@RequestBody Map<String, Object> request, Principal principal) {
        return addScheduleService.deleteMaterial(request, principal);
    }

    @PostMapping(EndpointPropertyKey.get_Material_Details)
    public ResponseEntity<GlobalResponseDTO> getMaterialDetails(@RequestBody Map<String, Object> request, Principal principal) {
        return addScheduleService.getMaterialDetails(request, principal);
    }
    @PostMapping(EndpointPropertyKey.update_Material_Details)
    public ResponseEntity<GlobalResponseDTO> updateMaterialDetails(@RequestBody Map<String, Object> request, Principal principal) {
        return addScheduleService.updateMaterialDetails(request, principal);
    }
    @PostMapping(EndpointPropertyKey.add_Material_Category)
    public ResponseEntity<GlobalResponseDTO> addMaterialCategory(@RequestBody Map<String, Object> request, Principal principal) {
        return addScheduleService.addMaterialCategory(request, principal);
    }

    @PostMapping(EndpointPropertyKey.add_Material_Sub_Category)
    public ResponseEntity<GlobalResponseDTO> addMaterialSubCategory(@RequestBody Map<String, Object> request, Principal principal) {
        return addScheduleService.addMaterialSubCategory(request, principal);
    }

    @PostMapping(EndpointPropertyKey.delete_category_And_Sub_Category)
    public ResponseEntity<GlobalResponseDTO> deleteCategoryAndSubcategory(@RequestBody Map<String, String> request, Principal principal) {
        return addScheduleService.deleteCategoryAndSubcategory(request, principal);
    }

    @PostMapping(EndpointPropertyKey.get_Active_Inactive_Status_For_Material)
    public ResponseEntity<GlobalResponseDTO> getActiveInactiveStatusForMaterial(@RequestBody Map<String, String> request, Principal principal) {
        return addScheduleService.getActiveInactiveStatusForMaterial(request, principal);
    }

    @PostMapping(EndpointPropertyKey.update_Active_Inactive_Status_For_Material)
    public ResponseEntity<GlobalResponseDTO> UpdateActiveInactiveStatusForMaterial(@RequestBody Map<String, String> request, Principal principal) {
        return addScheduleService.UpdateActiveInactiveStatusForMaterial(request, principal);
    }

  /*  @PostMapping(EndpointPropertyKey.recurrDate_slot_Validation)
    public ArrayList<String> recurrDate_slot_Validation(@RequestBody Map<String, Object> request) {

        return addScheduleService.reucrrSlotValidation(request);
    }*/
}


