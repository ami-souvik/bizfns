package com.bizfns.services.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table (name = "COMPANY_USER")
public class CompanyUserEntity implements Serializable {
    @Id
    @Column (name = "PK_USER_ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "FK_COMPANY_ID")
    private CompanyMasterEntity companyMasterEntity;

    @Column(name = "USER_FIRST_NAME")
    private String firstName;

    @Column(name = "USER_LAST_NAME")
    private String lastName;

    @Column(name = "USER_EMAIL")
    private String email;

    @Column(name = "USER_PHONE_NUMBER")
    private String phoneNumber;

//    @ManyToOne
//    @JoinColumn(name = "FK_USER_TYPE_ID")
//    private UserType userType;

    @Column(name = "USER_PASSWORD")
    private String password;

    @Column(name = "USER_CHARGE_RATE")
    private double chargeRate;

    @Column(name = "USER_CHARGE_FREQUENCY")
    private String chargeFrequency;

    @Column(name = "USER_STATUS")
    private String status;

    @Column(name = "USER_JOINING_DATE")
    private LocalDate joiningDate;


}
