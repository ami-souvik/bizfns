package com.bizfns.services.FCM;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;

@RestController
@RequestMapping("/api/users")
public class FcmController {
    @Autowired
    private FcmTokenRepository fcmTokenRepository;

    @PostMapping("save-token")
    public ResponseEntity<String> saveFcmToken(@RequestBody FcmTokenRequest request) {
        fcmTokenRepository.deleteByUserId(request.getUserId());
        FcmToken fcmToken = new FcmToken();
        fcmToken.setUserId(request.getUserId());
        fcmToken.setDeviceId(request.getDeviceId());
        fcmToken.setFcmToken(request.getFcmToken());
        fcmToken.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        fcmToken.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        fcmTokenRepository.saveFcmToken(fcmToken);
        return ResponseEntity.ok("FCM token saved successfully");
    }

    @GetMapping("/get-token/{userId}")
    public ResponseEntity<FcmToken> getFcmToken(@PathVariable int userId) {
        FcmToken fcmToken = fcmTokenRepository.findByUserId(userId);
        return ResponseEntity.ok(fcmToken);
    }
}

