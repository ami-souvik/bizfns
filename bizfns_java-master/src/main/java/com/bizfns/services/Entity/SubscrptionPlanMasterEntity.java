package com.bizfns.services.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "SUBSCRIPTION_PLAN_MASTER")
public class SubscrptionPlanMasterEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column (name = "PK_SUBSCRIPTION_PLAN_ID")
    private Integer subscrptionPlanId;

    @Column (name = "SUBSCRIPTION_ENTITY_PRICE")
    private String subscrptionEntityPrice;

    @Column (name = "SUBSCRIPTION_DURATION")
    private Float subscrptionDuration;

    @Column (name = "SUBSCRIPTION_USER_LIMIT")
    private Integer subscrptionUserLimit;

    @Column (name = "SUBSCRIPTION_PRICE_FREQUENCY")
    private Integer subscrptionPriceFrequency;

    @Column (name = "SUBSCRIPTION_PLAN_STATUS")
    private Integer subscrptionPlanStatus;

    @Column(name = "COMPANY_CREATED_AT")
    private Date companyCreatedAt;

    @Column(name = "COMPANY_UPDATED_AT")
    private Date companyUpdatedAt;

    @OneToMany(mappedBy = "subscrptionPlanMasterEntity", fetch = FetchType.LAZY)
    private List<CompanySubscrptionEntity> companySubscrptionEntity;

}



