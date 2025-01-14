package com.bizfns.services.Service;

import com.bizfns.services.GlobalDto.GlobalResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;


public interface CompanyMasterService {

    ResponseEntity<GlobalResponseDTO> forgotBusinessId(Map<String, String> request);

    //ResponseEntity<GlobalResponseDTO> getClientList(int page , int size);

    ResponseEntity<GlobalResponseDTO> getClientListByCompanyName(String businessName);
}
//