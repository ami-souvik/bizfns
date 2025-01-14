package com.bizfns.services.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table (name = "JOB_MASTER")
public class AddScheduleEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_JOB_ID")
    private Integer jobId;

    @Column(name = "FK_COMPANY_SUBSCRIPTION_ID")
    private Integer companySubscriptionId;

    @Column(name = "JOB_MASTER_START_TIME")
    private Timestamp jobMasterStartTime;

    @Column(name = "JOB_MASTER_END_TIME")
    private Timestamp jobMasterEndTime;


    @Column(name = "JOB_MASTER_DATE")
    private Date jobMasterDate;

    @Column(name = "JOB_MASTER_MATERIAL")
    private String jobMasterMaterial;

    @Column(name = "JOB_MASTER_NOTES")
    private String jobMasterNotes;

    @Column(name = "JOB_MASTER_DAYS_TYPE")
    private String jobMasterDaysType;

    @Column(name = "JOB_MASTER_STOP_ON")
    private Timestamp jobMasterStopOn;

    @Column(name = "JOB_MASTER_STATUS")
    private Integer jobMasterStatus;

    @Column(name = "JOB_LOCATION")
    private String jobLocation;

}