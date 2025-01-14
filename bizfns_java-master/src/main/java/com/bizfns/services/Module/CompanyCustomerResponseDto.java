package com.bizfns.services.Module;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyCustomerResponseDto {


    private Integer customerId;
    private Integer companyId;
    private String customerFirstName;
    private String customerLastName;
    private String customerEmail;
    private Integer customerPhoneNumber;
    private String customerPassword;
    private Integer customerStatus;
    private Integer lastOtp;
    private Integer isOtpVerified;
}
