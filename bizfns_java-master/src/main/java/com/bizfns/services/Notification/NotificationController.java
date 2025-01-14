package com.bizfns.services.Notification;

import com.bizfns.services.Controller.EndpointPropertyKey;
import com.bizfns.services.GlobalDto.GlobalResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/api/users")
public class NotificationController {

    @Autowired
    NotificationService notificationService;

    @GetMapping(EndpointPropertyKey.getNotificationMaster)
    public ResponseEntity<GlobalResponseDTO> getNotificationMaster(Principal principal) {
        return notificationService.getNotificationMaster(principal);
    }

    @PostMapping(EndpointPropertyKey.saveNotificationMaster)
    public ResponseEntity<GlobalResponseDTO> saveNotificationMaster(
            @RequestBody Map<String, Object> requestBody,
            Principal principal) {
        List<Map<String, Object>> reminders = (List<Map<String, Object>>) requestBody.get("reminder");
        return notificationService.saveNotificationMaster(reminders, principal);
    }

}
