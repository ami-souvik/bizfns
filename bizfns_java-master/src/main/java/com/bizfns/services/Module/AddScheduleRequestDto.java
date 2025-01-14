package com.bizfns.services.Module;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddScheduleRequestDto implements Serializable {

    private Integer jobId;

    private  Integer companySubscriptionId;

    private Timestamp jobMasterStartTime;

    private Timestamp JobMasterEndTime;

    private String jobMasterMaterial;

    private String jobMasterNotes;

    private String  jobMasterDaysType;

    private Timestamp jobMasterStopOn;
}
