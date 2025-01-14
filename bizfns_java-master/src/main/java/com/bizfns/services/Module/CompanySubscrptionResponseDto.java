package com.bizfns.services.Module;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanySubscrptionResponseDto implements Serializable {

    private Integer companySubscriptionId;
    private Integer companyBusinessMappingId;
    private Integer subscrptionPlanId;
    private String companySubscrptionCategoryDescription;
    private Date companySubscriptionStartDate;
    private Date companySubscriptionEndDate;
    private Integer comapnySubscriptionStatus;
    private Date companyCreatedAt;
    private Date companyUpdatedAt;

}
