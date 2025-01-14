package com.bizfns.services.Module;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.sql.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyMasterRequestDto {
    @NotEmpty(message = "companyId required")
    private Integer companyId;
    private String businessName;
    private String companyBackupEmail;
    private String companyBackupPhoneNumber;
    private Integer companyStatus;
    private Date companyCreatedAt;
    private Date companyUpdatedAt;
}
