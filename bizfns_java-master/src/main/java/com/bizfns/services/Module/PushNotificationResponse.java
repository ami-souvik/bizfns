package com.bizfns.services.Module;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PushNotificationResponse {

    private int status ;
    private String message;

}
