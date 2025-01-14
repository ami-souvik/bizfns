package com.bizfns.services.Controller;

import com.bizfns.services.GlobalDto.GlobalResponseDTO;
import com.bizfns.services.Service.AddScheduleService;
import com.bizfns.services.Service.TaxTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;


@RestController
@RequestMapping("/api/users")
public class TaxController {

    @Autowired
    private TaxTable taxTableService;

    @PostMapping(EndpointPropertyKey.ADD_TAX_TABLE)
    public ResponseEntity<GlobalResponseDTO> addTaxTable(
            @RequestBody Map<String, Object> request, Principal principal) {
        return taxTableService.addTaxTable(request, principal);
    }



//
//    @GetMapping(EndpointPropertyKey.GET_TAX_TABLE)
//        public ResponseEntity<GlobalResponseDTO> getTaxTable(
//                @RequestParam("TenantId") String tenantId,
//                @RequestParam("UserId") String userId,
//                Principal principal) {
//            return taxTableService.getTaxTable(tenantId, userId, principal);
//    }

    @PostMapping(EndpointPropertyKey.GET_TAX_TABLE)
    public ResponseEntity<GlobalResponseDTO> getTaxTable(
            @RequestBody Map<String, String> requestBody,
            Principal principal) {
        String tenantId = requestBody.get("tenantId");
        String userId = requestBody.get("userId");
        return taxTableService.getTaxTable(tenantId, userId);
    }


    @PostMapping(EndpointPropertyKey.UPDATE_TAX_TABLE)
    public ResponseEntity<GlobalResponseDTO> updateTaxTable(
            @RequestBody Map<String, Object> request, Principal principal) {
        return taxTableService.updateTaxTable(request, principal);
    }

    @PostMapping(EndpointPropertyKey.DELETE_TAX_TABLE)
    public ResponseEntity<GlobalResponseDTO> deleteTaxTable(
            @RequestBody Map<String, Object> request, Principal principal) {
        return taxTableService.deleteTaxTable(request, principal);
    }

}

