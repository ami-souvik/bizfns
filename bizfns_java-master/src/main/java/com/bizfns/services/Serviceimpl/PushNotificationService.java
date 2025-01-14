package com.bizfns.services.Serviceimpl;

import com.bizfns.services.GlobalDto.GlobalResponseDTO;
import com.bizfns.services.Module.PushNotificationRequest;
import com.bizfns.services.Repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PushNotificationService {


    @Autowired
    private NotificationRepository notificationRepository;




    public ResponseEntity<List<Map<String, Object>>>  getNotification(int page, int size) {

        //String companyId = (String) request.get("companyId");

        int offset = page * size;
        List<Map<String, Object>> notificationQuery = notificationRepository.getNotificationQuery(offset, size);

        Map<String , Object> response = new HashMap<>();
        response.put("status",true );
        response.put("message", "success");
        response.put("data", notificationQuery);

        return ResponseEntity.ok().body(notificationQuery);
    }

    public ResponseEntity<List<Map<String, Object>>>  updateNotification(int notificationID) {

        Map<String, Object> result = new HashMap<>();
        HttpStatus status = HttpStatus.OK;
        try {
            notificationRepository.updateNotificationRead(notificationID);
            result.put("status", "success");
            result.put("message", "The notification is read successfully.");
        } catch (Exception e) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            result.put("status", "error");
            result.put("message", "An error occurred while updating the notification: " + e.getMessage());
        }
        return ResponseEntity.status(status).body(Collections.singletonList(result));
    }
}
