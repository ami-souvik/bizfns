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
@Table(name = "COMPANY_MASTER")
public class CompanyMasterEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "COMPANY_ID", length = 50)
    private Integer companyId;

    @Column(name = "BUSINESS_NAME", length = 300)
    private String businessName;

    @Column(name = "COMPANY_BACKUP_EMAIL", length = 300)
    private String companyBackupEmail;

    @Column(name = "COMPANY_BACKUP_PHONE_NUMBER", length = 12)
    private String companyBackupPhoneNumber;

    @Column(name = "COMPANY_STATUS", length = 30)
    private Integer companyStatus;

    @Column(name = "COMPANY_CREATED_AT")
    private Date companyCreatedAt;

    @Column(name = "COMPANY_UPDATED_AT")
    private Date companyUpdatedAt;
}
