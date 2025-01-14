package com.bizfns.services.Module;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PushNotificationRequest {

    private String token;
    private String title;
    private String message;
    private String topic;

    // Constructors, getters, and setters

}
