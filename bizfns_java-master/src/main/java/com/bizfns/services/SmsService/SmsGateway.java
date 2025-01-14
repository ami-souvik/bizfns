package com.bizfns.services.SmsService;


import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.messaging.v1.service.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsGateway {

    private final String twilioPhoneNumber;

    public SmsGateway(@Value("${twilio.phone.number}") String twilioPhoneNumber) {
        this.twilioPhoneNumber = twilioPhoneNumber;
    }

    public void sendSms(String to, String message) {
        Message.creator(
                new com.twilio.type.PhoneNumber(to),
                new com.twilio.type.PhoneNumber(twilioPhoneNumber),
                message
        ).create();
    }
}
