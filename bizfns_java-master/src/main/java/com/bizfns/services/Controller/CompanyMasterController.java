package com.bizfns.services.Controller;

import com.bizfns.services.GlobalDto.GlobalResponseDTO;
import com.bizfns.services.Service.CompanyMasterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@Slf4j
@RequestMapping("/api/users")
public class CompanyMasterController {

    @Autowired
    private  CompanyMasterService companyMasterService;

//    @Autowired
//    public CompanyMasterController(CompanyMasterService companyMasterService) {
//        this.companyMasterService = companyMasterService;
//    }

    @PostMapping(EndpointPropertyKey.FORGOT_BUSINESS_ID)
    public ResponseEntity<GlobalResponseDTO> forgotBusinessId(
            @RequestBody Map<String, String> request)  {

        return companyMasterService.forgotBusinessId(request);
    }


    /*@GetMapping(EndpointPropertyKey.CLIENT_LIST)
    public ResponseEntity<GlobalResponseDTO> getClientList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return companyMasterService.getClientList(page, size);

    }*/

    @PostMapping(EndpointPropertyKey.CLIENT_LIST_BY_COMPANY_BUSINESS_NAME)
    public ResponseEntity<GlobalResponseDTO> getClientListByClientName(@RequestParam(required = false) String businessName) {
       return companyMasterService.getClientListByCompanyName(businessName);


    }


}


