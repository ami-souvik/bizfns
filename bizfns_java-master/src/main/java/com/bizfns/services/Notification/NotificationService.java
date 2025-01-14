package com.bizfns.services.Notification;

import com.bizfns.services.GlobalDto.GlobalResponseDTO;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.List;
import java.util.Map;

public interface NotificationService {
    ResponseEntity<GlobalResponseDTO> getNotificationMaster(Principal principal);

    ResponseEntity<GlobalResponseDTO> saveNotificationMaster(List<Map<String, Object>> reminders, Principal principal);
}
