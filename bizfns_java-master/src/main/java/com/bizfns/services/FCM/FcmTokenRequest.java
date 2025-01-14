package com.bizfns.services.FCM;

import lombok.Data;

@Data
public class FcmTokenRequest {
    private int userId;
    private String deviceId;
    private String fcmToken;
}
