package com.bizfns.services.Entity;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
@With
public class AssigendJobEntity implements Serializable {

    @Column(name = "PK_ASSIGNED_JOB_ID")
    private Integer assignedJobId;

    @Column(name = "FK_JOB_ID")
    private Integer jobId;

    @Column(name = "FK_USER_ID")
    private Integer userId;

    @Column(name = "FK_CUSTOMER_ID")
    private Integer customerId;

    @Column(name = "ASSIGNED_JOB_PAYMENT_STATUS")
    private Integer assignedJobPaymentStatus;

    @Column(name = "ASSIGNED_JOB_STATUS")
    private Integer assignedJobStatus;

//
//    @ManyToOne(fetch =  FetchType.LAZY)
//    @JoinColumn(name = "FK_USER_ID", referencedColumnName = "FK_USER_ID" ,insertable = false, updatable = false)
//    private AddScheduleEntity addScheduleEntity;





}
