package com.bizfns.services.Service;

import com.bizfns.services.Exceptions.CommonsXsdSchemaExceptionnException;
import com.bizfns.services.GlobalDto.GlobalResponseDTO;
import com.bizfns.services.Module.CompanyCustomerRequestDto;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.Map;

public interface CompanyCustomerService {


    ResponseEntity<GlobalResponseDTO> fetchCustomerList(Map<String, String> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> customerHistoryRecordList(Map<String, String> request,Principal principal);
}


