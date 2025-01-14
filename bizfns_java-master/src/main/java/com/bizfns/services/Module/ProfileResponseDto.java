package com.bizfns.services.Module;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder
public class ProfileResponseDto {

    private Object businessNameAndLogo;
    //private String businessName;
   // private String businessLogo;
    private String businessType;
    private Object marketing;
    private String companyStatus;
    private String businessContactPerson;
    private String primaryMobileNumber;
    private String primaryBusinessEmail;
    private String trustedBackupEmail;
    private String trustedBackupMobileNumber;
    private String registrationDate;
    //private Object changePassword;
    private List<Map<String, Object>> securityQuestion;
    private Map<String, Object> subscriptionPlan;

    private String fullAddress;

}
