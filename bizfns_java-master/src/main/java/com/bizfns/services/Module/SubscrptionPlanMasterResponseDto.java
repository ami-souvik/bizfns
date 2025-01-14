package com.bizfns.services.Module;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscrptionPlanMasterResponseDto implements Serializable {

    private Integer subscrptionPlanId;
    private String subscrptionEntityPrice;
    private Float subscrptionDuration;
    private Integer subscrptionUserLimit;
    private Integer subscrptionPriceFrequency;
    private Integer subscrptionPlanStatus;
    private Date companyCreatedAt;
    private Date companyUpdatedAt;

}
