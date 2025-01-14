package com.bizfns.services.Controller;

// SubscriptionPlanController.java
import com.bizfns.services.Module.SubscrptionPlanMasterResponseDto;
import com.bizfns.services.Service.SubscrptionPlanMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SubscriptionPlanMasterController {
    @Autowired
    private  SubscrptionPlanMasterService subscriptionPlanMasterService;



}
