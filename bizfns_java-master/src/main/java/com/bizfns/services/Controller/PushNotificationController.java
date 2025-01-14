package com.bizfns.services.Controller;

import com.bizfns.services.GlobalDto.GlobalResponseDTO;
import com.bizfns.services.Module.PushNotificationRequest;
import com.bizfns.services.Module.PushNotificationResponse;
import com.bizfns.services.Serviceimpl.PushNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/users")
public class PushNotificationController {

    @Autowired
    private PushNotificationService pushNotificationService;



    @GetMapping("/getNotification")
    public ResponseEntity<List<Map<String, Object>>> getNotification(@RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "50") int size) {

        return pushNotificationService.getNotification(page, size);
        //return new ResponseEntity<>(new PushNotificationResponse(HttpStatus.OK.value(), "Notify successfully"), HttpStatus.OK);


    }

    @PostMapping("/readAllNotification")
    public ResponseEntity<GlobalResponseDTO> readAllNotification(@RequestBody Map<String, String> request) {

        String readNotificationIds = request.get("NOTIFICATION_IDS");
        List<String> notificationIdList = Arrays.asList(readNotificationIds.split(","));

        for (String notificationId : notificationIdList) {
            ResponseEntity<List<Map<String, Object>>> res = pushNotificationService.updateNotification(Integer.parseInt(notificationId.trim()));
            if(res.getBody().get(0).get("status").toString().equalsIgnoreCase("error")){
                String message = res.getBody().get(0).get("message").toString();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new GlobalResponseDTO(false, message, null));
            }
        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(new GlobalResponseDTO(true, "Notifications updated successfully", null));
    }
}
