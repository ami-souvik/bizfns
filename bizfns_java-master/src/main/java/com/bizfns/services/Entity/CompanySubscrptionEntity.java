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
@Table(name = "COMPANY_SUBSCRIPTION")
public class CompanySubscrptionEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_COMPANY_SUBSCRIPTION_ID")
    private Integer companySubscriptionId;

    @Column(name = "FK_COMPANY_BUSINESS_MAPPING_ID")
    private Integer companyBusinessMappingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_SUBSCRIPTION_PLAN_ID", referencedColumnName = "PK_SUBSCRIPTION_PLAN_ID", insertable = false, updatable = false)
    private SubscrptionPlanMasterEntity subscrptionPlanMasterEntity;

    @Column(name = "COMPANY_SUBSCRIPTION_CATEGORY_DESCRIPTION")
    private String companySubscrptionCategoryDescription;

    @Column(name = "COMPANY_SUBSCRIPTION_START_DATE")
    private Date companySubscriptionStartDate;

    @Column(name = "COMPANY_SUBSCRIPTION_END_DATE")
    private Date companySubscriptionEndDate;

    @Column(name = "COMPANY_SUBSCRIPTION_STATUS")
    private Integer companySubscriptionStatus;

    @Column(name = "COMPANY_CREATED_AT")
    private Date companyCreatedAt;

    @Column(name = "COMPANY_UPDATED_AT")
    private Date companyUpdatedAt;


}
