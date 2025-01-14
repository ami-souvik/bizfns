package com.bizfns.services.Module;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import java.sql.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyMasterResponseDto {
    private Integer companyId;

    private String businessName;

    private Integer companyBackupPhoneNumber;

    private Integer companyStatus;

    private Date companyCreatedAt;

    private Date companyUpdatedAt;

}
