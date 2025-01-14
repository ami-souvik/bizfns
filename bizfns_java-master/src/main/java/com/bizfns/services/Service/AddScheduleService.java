package com.bizfns.services.Service;

import com.bizfns.services.GlobalDto.GlobalResponseDTO;
import com.bizfns.services.Module.AddScheduleRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Map;

public interface AddScheduleService {
   // ResponseEntity<GlobalResponseDTO> saveAddSchedule(AddScheduleRequestDto addScheduleRequestDto);

    ResponseEntity<GlobalResponseDTO> addNewSchedule(Map<String, Object> request, HttpServletRequest httpRequest, Principal principal);

    ResponseEntity<GlobalResponseDTO> addMaterial(Map<String, Object> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> materialList(Map<String, String> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> materialCategoryData(Map<String, String> request,Principal principal);

    ResponseEntity<GlobalResponseDTO> jobDetails(Map<String, String> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> deleteSchedule(Map<String, String> request,Principal principal);

    ResponseEntity<GlobalResponseDTO> getAllrecurrDate(Map<String, String> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> recurrValidation(Map<String, Object> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> deleteMaterial(Map<String, Object> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> getMaterialDetails(Map<String, Object> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> updateMaterialDetails(Map<String, Object> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> addMaterialCategory(Map<String, Object> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> addMaterialSubCategory(Map<String, Object> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> deleteCategoryAndSubcategory(Map<String, String> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> getActiveInactiveStatusForService(Map<String, String> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> UpdateActiveInactiveStatusForService(Map<String, String> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> getActiveInactiveStatusForMaterial(Map<String, String> request, Principal principal);
    ResponseEntity<GlobalResponseDTO> UpdateActiveInactiveStatusForMaterial(Map<String, String> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> getJobNumberByDate(Map<String, String> request, Principal principal);
}
