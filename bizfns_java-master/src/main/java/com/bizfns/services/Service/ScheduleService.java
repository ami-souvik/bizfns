package com.bizfns.services.Service;

import com.bizfns.services.Exceptions.CustomException;
import com.bizfns.services.GlobalDto.GlobalResponseDTO;
import com.bizfns.services.GlobalDto.GlobalResponseListObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.Principal;
import java.text.ParseException;
import java.util.Map;

public interface ScheduleService {


    ResponseEntity<GlobalResponseDTO> scheduleList(Map<String, String> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> testErrorLog(Map<String, String> request);

    ResponseEntity<GlobalResponseDTO> scheduleHistory(Map<String, String> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> reScheduleJob(Map<String, String> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> getMaterialUnit(Map<String, String> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> addScheduleNew(Map<String, Object> request);

    ResponseEntity<GlobalResponseDTO> editSchedule(Map<String, Object> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> addWorkingHours(Map<String, Object> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> serviceEntityField(Map<String, String> request);

    ResponseEntity<GlobalResponseDTO> getServiceEntityDetails(Map<String, String> request);


    ResponseEntity<GlobalResponseDTO> getServiceEntityDetailsbyCustomerId(Map<String, String> request);



    ResponseEntity<GlobalResponseDTO> saveTimeInterval(Map<String, String> request, Principal principal) throws ParseException;


    ResponseEntity<GlobalResponseDTO> getTimeIntervalFromDb(Principal principal);

    ResponseEntity<GlobalResponseDTO> getTimeInterval(Map<String, String> request);

    ResponseEntity<GlobalResponseDTO> calCulateJobPrice(Map<String, String> request, Principal principal) throws CustomException;

    ResponseEntity<GlobalResponseDTO> saveMediaFile( MultipartFile[] file,String tenantId,String Pkjobid,String auditId, Principal principal) throws IOException, CustomException;

    ResponseEntity<GlobalResponseDTO> getMediaFile(Map<String, String> request);

    ResponseEntity<GlobalResponseDTO> deleteMediaFile(Map<String, String> request);

    void downloadImage(String imageName, HttpServletResponse response) throws IOException;


    ResponseEntity<GlobalResponseDTO> custWiseServiceEntity(Map<String, String> request);

    ResponseEntity<GlobalResponseDTO> generateInvoice(Map<String,Object> request, Principal principal);

    ResponseEntity<GlobalResponseListObject> getCustomerHistory(Map<String,Object> request);

    void downloadInvoice(String imageName, HttpServletResponse response) throws IOException;

    ResponseEntity<GlobalResponseListObject> getCustomerServiceHistory(Map<String,Object> request);


    ResponseEntity<GlobalResponseDTO> saveMaxJobTask(Map<String, Object> request);

    ResponseEntity<GlobalResponseDTO> staffUserLogin(Map<String,Object> request);

    ResponseEntity<GlobalResponseDTO> saveJobStatus(Map<String,Object> request);


    ResponseEntity<GlobalResponseDTO> getJobStatus(Map<String, Object> request);

    ResponseEntity<GlobalResponseDTO> getMaxJobTask();

    ResponseEntity<GlobalResponseDTO> deleteServiceObject(Map<String, String> request);

    ResponseEntity<GlobalResponseDTO> SaveEditInvoiceValuesByJobIdAndCustomerIds(Map<String, Object> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> updateEditInvoiceValues(Map<String, Object> request, Principal principal) throws IOException;

    ResponseEntity<Map<String, Object>> getEditInvoiceValuesByJobIdAndCustomerId(Map<String, Object> request);

    ResponseEntity<Map<String, Object>> createInvoicePdfByCustomers(Map<String, Object> request) throws IOException;

    ResponseEntity<GlobalResponseDTO> saveTimeSheet(Map<String, Object> request,Principal principal);

    ResponseEntity<GlobalResponseDTO> updateTimeSheet(Map<String, Object> request);

    ResponseEntity<GlobalResponseDTO> getTimeSheetList(Map<String, Object> request);

    ResponseEntity<GlobalResponseDTO> getTimeSheetByBillNoAndStaffId(Map<String, Object> request);

    ResponseEntity<GlobalResponseDTO> getInvoiceListsByJobId(Map<String, Object> request, Principal principal);

    ResponseEntity<GlobalResponseDTO> saveMaterialUnit(Map<String, Object> request, Principal principal);

    Map<String, String> getWorkingHours(Principal principal);
}
