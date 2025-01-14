package com.bizfns.services.Controller;


import com.bizfns.services.Exceptions.CommonsXsdSchemaExceptionnException;
import com.bizfns.services.GlobalDto.GlobalResponseDTO;
import com.bizfns.services.Module.CompanyCustomerRequestDto;
import com.bizfns.services.Service.CompanyCustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping ("/api/users")
public class CompanyCustomerController {
       // private final CompanyCustomerService customerService;
        @Autowired
        private CompanyCustomerService companyCustomerService;

//        public CompanyCustomerController(CompanyCustomerService customerService) {
//            this.customerService = customerService;
//        }

    @PostMapping(EndpointPropertyKey.FETCH_CUSTOMER_LIST)
    public ResponseEntity<GlobalResponseDTO> fetchCustomerList(@RequestBody Map<String, String> request, Principal principal){

        return companyCustomerService.fetchCustomerList(request, principal);
    }

    @PostMapping(EndpointPropertyKey.CUSTOMER_HISTORY_RECORD_LIST)
    public ResponseEntity<GlobalResponseDTO> customerHistoryRecordList(@RequestBody Map<String, String> request, Principal principal
    ){

        return companyCustomerService.customerHistoryRecordList(request,principal);
    }





    }


