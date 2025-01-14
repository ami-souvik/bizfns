package com.bizfns.services.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Date;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "COMPANY_CUSTOMER")
public class CompanyCustomerEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column (name = "PK_CUSTOMER_ID")
    private Integer customerId;

    @Column(name = "FK_COMPANY_ID")
    private Integer companyId;

    @Column(name = "CUSTOMER_FIRST_NAME")
    private String customerFirstName;

    @Column(name = "CUSTOMER_LAST_NAME")
    private String customerLastName;

    @Column(name = "CUSTOMER_EMAIL")
    private String customerEmail;

    @Column(name = "CUSTOMER_PHONE_NUMBER")
    private Integer customerPhoneNumber;

    @Column(name = "CUSTOMER_PASSWORD")
    private String customerPassword;

    @Column(name = "CUSTOMER_STATUS")
    private Integer customerStatus;

    @Column(name = "LAST_OTP")
    private Integer lastOtp;

    @Column(name = "IS_OTP_VERIFIED")
    private Integer isOtpVerified;

    @Column(name = "COMPANY_CREATED_AT")
    private Date companyCreatedAt;

    @Column(name = "COMPANY_UPDATED_AT")
    private Date companyUpdatedAt;
}
