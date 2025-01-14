package com.bizfns.services.Controller;

import com.bizfns.services.GlobalDto.GlobalResponseDTO;
import com.bizfns.services.Query.ScheduleQuery;
import com.bizfns.services.Service.AddScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController
public class ServiceController {
    @Autowired
    private AddScheduleService addScheduleService;

    @Autowired
    private ScheduleQuery scheduleQuery;
    @PostMapping(EndpointPropertyKey.JOB_DETAILS)
    public ResponseEntity<GlobalResponseDTO> jobDetails(
            @RequestBody Map<String, String> request, Principal principal)  {

        return addScheduleService.jobDetails(request, principal);
    }

    @PostMapping(EndpointPropertyKey.getjobnumberbydate)
    public ResponseEntity<GlobalResponseDTO> getJobNumberByDate(
            @RequestBody Map<String, String> request, Principal principal)  {

        return addScheduleService.getJobNumberByDate(request, principal);
    }

    @PostMapping(EndpointPropertyKey.DELETE_SCHEDULE)
    public ResponseEntity<GlobalResponseDTO> deleteSchedule(
            @RequestBody Map<String, String> request, Principal principal)  {
        String tenantId = (String) request.get("tenantId");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            String authorityName = authority.getAuthority();
            List<String> priviledgeChk = scheduleQuery.priviledgeChkForSchedule(tenantId);
            //boolean hasEditPrivilege = false;
            boolean hasEditPrivilege = true;
            for (String privilege : priviledgeChk) {
                if (privilege.equalsIgnoreCase("DELETE")) {
                    hasEditPrivilege = true;
                    break;
                }
            }
            if (authorityName.equalsIgnoreCase("Staff") && !hasEditPrivilege) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new GlobalResponseDTO(false, "A Staff user dont have the priviledge to DELETE the schedule.", null));
            }
        }

        return addScheduleService.deleteSchedule(request,principal);
    }

    @PostMapping(EndpointPropertyKey.get_Active_Inactive_Status_For_Service)
    public ResponseEntity<GlobalResponseDTO> getActiveInactiveStatusForService(
            @RequestBody Map<String, String> request, Principal principal)  {
        return addScheduleService.getActiveInactiveStatusForService(request, principal);
    }

    @PostMapping(EndpointPropertyKey.update_Active_Inactive_Status_For_Service)
    public ResponseEntity<GlobalResponseDTO> UpdateActiveInactiveStatusForService(
            @RequestBody Map<String, String> request, Principal principal)  {
        return addScheduleService.UpdateActiveInactiveStatusForService(request, principal);
    }



}
