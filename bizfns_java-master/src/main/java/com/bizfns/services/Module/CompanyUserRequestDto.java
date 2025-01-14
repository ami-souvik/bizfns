package com.bizfns.services.Module;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyUserRequestDto implements Serializable {

    private Long id;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private double chargeRate;
    private String chargeFrequency;
    private String status;
    private LocalDate joiningDate;


    public CompanyUserRequestDto(String email, String password) {
    }
}

