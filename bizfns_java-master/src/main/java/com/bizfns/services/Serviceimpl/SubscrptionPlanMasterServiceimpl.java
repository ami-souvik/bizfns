package com.bizfns.services.Serviceimpl;

import com.bizfns.services.Entity.SubscrptionPlanMasterEntity;
import com.bizfns.services.Module.CompanyMasterResponseDto;
import com.bizfns.services.Module.SubscrptionPlanMasterResponseDto;
import com.bizfns.services.Repository.SubscrptionPlanMasterRepository;
import com.bizfns.services.Service.SubscrptionPlanMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
public class SubscrptionPlanMasterServiceimpl implements SubscrptionPlanMasterService {

    @Autowired
    private SubscrptionPlanMasterRepository subscrptionPlanMasterRepository;

}

