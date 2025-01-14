package com.bizfns.services.Controller;

import com.bizfns.services.Exceptions.CustomException;
import com.bizfns.services.GlobalDto.GlobalResponseDTO;
import com.bizfns.services.GlobalDto.GlobalResponseListObject;
import com.bizfns.services.Notification.PushNotificationServiceForNotf;
import com.bizfns.services.Query.ScheduleQuery;
import com.bizfns.services.Service.RegistrationService;
import com.bizfns.services.Service.ScheduleService;
import com.bizfns.services.SmsService.Scheduler;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.text.ParseException;
import java.util.*;

        /*scheduleList
        testErrorLog
        scheduleHistory
        reScheduleJob
        getMaterialUnit
        addScheduleNew
        editSchedule
        addWorkingHours
        serviceEntityField
        ScheduleTimeIntervalSave
        getScheduleTimeInterval
        GET_Job_price
        save_Media_file
        get_Media_file
        delete_Media_file
        download_Media_file
        custWiseServiceEntity
        getServiceEntityDetails
        getServiceEntityDetailsBycustomerids
        createInvoice
        DownloadInvoice
        getCustomerHistory
        getCustomerServiceHistory
        saveMaxJobTask
        getMAxJobTask
        getstaffUserLogin
        saveJobStatus
        getJobStatus*/

@RestController
@Slf4j
//@RequestMapping("/api/users")
public class ScheduleController {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleController.class);
    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private Scheduler scheduler;

    @Autowired
    PushNotificationServiceForNotf pushNotificationServiceForNotf;
    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private ScheduleQuery scheduleQuery;

    @Autowired
    private SpringTemplateEngine springTemplateEngine;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @PostMapping(EndpointPropertyKey.SCHEDULE_LIST)
    public ResponseEntity<GlobalResponseDTO> scheduleList(@RequestBody Map<String, String> request, Principal principal) {
        String tenantId = (String) request.get("tenantId");
        String staffPhoneNumber = (String) request.get("userId");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        List<String> priviledgeChk = new ArrayList<>();
        for (GrantedAuthority authority : authorities) {
            String authorityName = authority.getAuthority();
            List<String> phoneNoPrivilegeDataChk = scheduleQuery.phoneNoPriviledgeDataChk(tenantId,staffPhoneNumber);
            if(phoneNoPrivilegeDataChk.isEmpty()){
                priviledgeChk = scheduleQuery.priviledgeChkForSchedule(tenantId);

            }else{
                priviledgeChk = scheduleQuery.priviledgeChkForScheduleAsPerPhNo(tenantId,staffPhoneNumber);
            }
            //boolean hasEditPrivilege = false;--at the time of integration of priviledge check make it to false
            //making it true just for case test.
            boolean hasEditPrivilege = true;
            for (String privilege : priviledgeChk) {
                if (privilege.equalsIgnoreCase("VIEW")) {
                    hasEditPrivilege = true;
                    break;
                }else if(privilege.equalsIgnoreCase("VIEW OWN")) {
                    ResponseEntity<GlobalResponseDTO> res = scheduleService.scheduleList(request, principal);
                    GlobalResponseDTO response = res.getBody();
                    Object resObj = response.getData();
                    List<Map<String, Object>> filteredResponseList = new ArrayList<>();
                    if (resObj instanceof List<?>) {
                        List<?> responseList = (List<?>) resObj;
                        for (Object obj : responseList) {
                            if (obj instanceof Map<?, ?>) {
                                Map<String, Object> map = (Map<String, Object>) obj;
                                Object scheduleObj = map.get("schedule");
                                if (scheduleObj instanceof List<?>) {
                                    List<?> scheduleList = (List<?>) scheduleObj;
                                    List<Map<String, Object>> filteredScheduleList = new ArrayList<>();
                                    for (Object scheduleItem : scheduleList) {
                                        if (scheduleItem instanceof Map<?, ?>) {
                                            Map<String, Object> schedule = (Map<String, Object>) scheduleItem;
                                            Object staffListObj = schedule.get("StaffList");
                                            if (staffListObj instanceof List<?>) {
                                                List<?> staffList = (List<?>) staffListObj;
                                                boolean hasMatchingStaff = false;
                                                for (Object staffObj : staffList) {
                                                    if (staffObj instanceof Map<?, ?>) {
                                                        Map<String, Object> staff = (Map<String, Object>) staffObj;
                                                        Object pkUserId = staff.get("PK_USER_ID");
                                                        String pkUserIdStr = pkUserId.toString();
                                                        String staffQuery = "select cu.\"PK_USER_ID\" " +
                                                                "from \"" + tenantId + "\".company_user cu " +
                                                                "where cu.\"USER_PHONE_NUMBER\" = ?";
                                                        List<Map<String, Object>> staffIdMaps = jdbcTemplate.queryForList(staffQuery, staffPhoneNumber);
                                                        Set<String> staffIdSet = new HashSet<>();
                                                        for (Map<String, Object> staffIdMap : staffIdMaps) {
                                                            Object staffIdObj = staffIdMap.get("PK_USER_ID");
                                                            if (staffIdObj != null) {
                                                                String staffIdStr = staffIdObj.toString();
                                                                staffIdSet.add(staffIdStr);
                                                            }
                                                        }
                                                        boolean isMatched = staffIdSet.contains(pkUserIdStr);
                                                        if (isMatched) {
                                                            hasMatchingStaff = true;
                                                            break;
                                                        }
                                                    }
                                                    if (!hasMatchingStaff) {
                                                        hasMatchingStaff = false;
                                                    }
                                                }
                                                if (hasMatchingStaff) {
                                                    filteredScheduleList.add(schedule);
                                                }
                                            }
                                        }
                                    }
                                    map.put("schedule", filteredScheduleList);
                                    filteredResponseList.add(map);
                                }
                            }
                        }
                        response.setData(filteredResponseList);
                        return ResponseEntity.status(HttpStatus.OK).body(response);
                    }
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new GlobalResponseDTO(false, "A staff user doesn't have the privilege to VIEW other staff's schedule.", null));
                }
            }
            if (authorityName.equalsIgnoreCase("Staff") && !hasEditPrivilege) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new GlobalResponseDTO(false, "A Staff user dont have the priviledge to VIEW the schedule.", null));
            }
        }
        return scheduleService.scheduleList(request, principal);
    }

    @PostMapping(EndpointPropertyKey.TEST_ERROR_LOG)
    public ResponseEntity<GlobalResponseDTO> testErrorLog(@RequestBody Map<String, String> request) {


        return scheduleService.testErrorLog(request);
    }

    @PostMapping(EndpointPropertyKey.SCHEDULE_HISTORY)
    public ResponseEntity<GlobalResponseDTO> scheduleHistory(@RequestBody Map<String, String> request, Principal principal) {

        return scheduleService.scheduleHistory(request, principal);
    }

    @PostMapping(EndpointPropertyKey.RESCHEDULE_JOB)
    public ResponseEntity<GlobalResponseDTO> reScheduleJob(@RequestBody Map<String, String> request, Principal principal) {

        return scheduleService.reScheduleJob(request, principal);
    }

    @PostMapping(EndpointPropertyKey.GET_MATERIAL_UNIT)
    public ResponseEntity<GlobalResponseDTO> getMaterialUnit(@RequestBody Map<String, String> request, Principal principal) {

        return scheduleService.getMaterialUnit(request, principal);
    }

    @PostMapping(EndpointPropertyKey.SAVE_MATERIAL_UNIT)
    public ResponseEntity<GlobalResponseDTO> saveMaterialUnit(@RequestBody Map<String, Object> request, Principal principal) {
        return scheduleService.saveMaterialUnit(request, principal);
    }
    @PostMapping(EndpointPropertyKey.ADD_SCHEDULE_NEW)
    public ResponseEntity<GlobalResponseDTO> addScheduleNew(@RequestBody Map<String, Object> request) {

        return scheduleService.addScheduleNew(request);
    }
    @PostMapping(EndpointPropertyKey.EDIT_SCHEDULE)
    public ResponseEntity<GlobalResponseDTO> editSchedule(@RequestBody Map<String, Object> request, Principal principal) {
        String tenantId = (String) request.get("tenantId");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            String authorityName = authority.getAuthority();
            List<String> priviledgeChk = scheduleQuery.priviledgeChkForSchedule(tenantId);
            boolean hasEditPrivilege = false;
            for (String privilege : priviledgeChk) {
                if (privilege.equalsIgnoreCase("EDIT ALL")) {
                    hasEditPrivilege = true;
                    break;
                } else if (privilege.equalsIgnoreCase("EDIT OWN")) {
                    String jobId = (String) request.get("job_id");
                    String userId = (String) request.get("userId");
                    String selectQuery = "SELECT \"STAFF_DETAILS\" " +
                            "FROM \"" + tenantId + "\".job_master " +
                            "WHERE \"PK_JOB_ID\" = ?";
                    String staffDetails = jdbcTemplate.queryForObject(selectQuery, new Object[]{Integer.parseInt(jobId)}, String.class);
                    String[] staffIds = staffDetails.split(",");
                    String userPhoneQuery = "SELECT \"USER_PHONE_NUMBER\" " +
                            "FROM \"" + tenantId + "\".company_user " +
                            "WHERE \"PK_USER_ID\" = ?";
                    boolean isAuthorized = false;
                    for (String staffId : staffIds) {
                        String staffPhoneNumber = jdbcTemplate.queryForObject(userPhoneQuery, new Object[]{Integer.parseInt(staffId)}, String.class);
                        if (staffPhoneNumber != null && staffPhoneNumber.equals(userId)) {
                            isAuthorized = true;
                            break;
                        }
                    }
                    if (isAuthorized) {
                        ResponseEntity<GlobalResponseDTO> response = scheduleService.editSchedule(request, principal);
                        boolean RES = response.getBody().getSuccess();
                        String phoneNo = "+919113780416";
                        String message = "Your job (ID: " + jobId + ") got edited";
                        /*String selectQuery = "SELECT \"reminder_messages\" FROM hoxs3359.reminder WHERE \"id\" = ?";
                        String reminderEvent = jdbcTemplate.queryForObject(selectQuery, new Object[]{1}, String.class);*/
                        //scheduler.throwMessageBasedOnScheduleStatus(phoneNo,message);
                        return response;
                    } else {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new GlobalResponseDTO(false, "A Staff user dont have the priviledge to EDIT the schedule.", null));

                    }
                }
            }
            if (authorityName.equalsIgnoreCase("Staff") && hasEditPrivilege) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new GlobalResponseDTO(false, "A Staff user dont have the priviledge to EDIT the schedule.", null));
            }
        }
        try {
            ResponseEntity<GlobalResponseDTO> response = scheduleService.editSchedule(request, principal);
            boolean RES = response.getBody().getSuccess();
            String jobId = (String) request.get("job_id");
            String phoneNo = "+917587272727";
            String token = "c27KE31xRf6lyREYQfyQL3:APA91bEb49XpfXmkmrwpcQFj0ZS1uzMNzcwtbywe0bZCw8r3__3wb1KymuI2Scq4b1M6zLBLgqrJdFXJunICPWXQq-PD2mIfc2h7VPTg5EnMvVPwoEMia5nxz_uay-wT0EI4-P-r3e30";
            boolean smsEnabled = isSmsEnabled(5,tenantId);
            /*if (smsEnabled) {
                String message = getReminderMessage(5);
                if (message != null) {
                    //scheduler.throwMessageBasedOnScheduleStatus(phoneNo,message);
                    String staffQuery = "SELECT \"STAFF_DETAILS\" FROM \"" + tenantId + "\".\"job_master\" WHERE \"PK_JOB_ID\" = ?";
                    String staffDetails = jdbcTemplate.queryForObject(staffQuery, new Object[]{Integer.parseInt(jobId)}, String.class);
                    if (staffDetails != null && !staffDetails.isEmpty()) {
                        String[] staffIds = staffDetails.split(",");
                        String staffPhoneQuery = "SELECT \"USER_PHONE_NUMBER\", \"PK_USER_ID\" FROM \"" + tenantId + "\".\"company_user\" WHERE \"PK_USER_ID\" IN (" + String.join(",", staffIds) + ")";
                        List<Map<String, Object>> staffPhones = jdbcTemplate.query(staffPhoneQuery, (rs, rowNum) -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put("phoneNumber", rs.getString("USER_PHONE_NUMBER"));
                            map.put("userId", rs.getInt("PK_USER_ID"));
                            return map;
                        });
                        for (Map<String, Object> staff : staffPhones) {
                            String staffPhoneNumber = (String) staff.get("phoneNumber");
                            String tokenQuery = "SELECT \"FCM_TOKEN\" FROM \"Bizfns\".\"FCM_TOKEN\" WHERE \"USER_ID\" = ?";
                            List<String> fcmTokens = jdbcTemplate.query(tokenQuery, new Object[]{staffPhoneNumber}, (rs, rowNum) -> rs.getString("FCM_TOKEN"));
                            if (!fcmTokens.isEmpty()) {
                                String staffFcmToken = fcmTokens.get(0);
                                if (staffPhoneNumber != null && staffFcmToken != null) {
                                    pushNotificationServiceForNotf.sendNotification(staffFcmToken, "Schedule Creation", message);
                                }
                            }
                        }
                    }

                    String userQuery = "SELECT um.\"MOBILE_NUMBER\", fcm.\"FCM_TOKEN\"" +
                            "FROM \"Bizfns\".\"USER_MASTER\" um " +
                            "JOIN \"Bizfns\".\"FCM_TOKEN\" fcm ON um.\"MOBILE_NUMBER\" = fcm.\"USER_ID\"" +
                            "WHERE um.\"SCHEMA_NAME\" = ? AND um.\"USER_TYPE\" = 'Company'";
                    List<Map<String, Object>> userDetails = jdbcTemplate.query(userQuery, new Object[]{tenantId}, (rs, rowNum) -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("phoneNumber", rs.getString("MOBILE_NUMBER"));
                        map.put("fcmToken", rs.getString("FCM_TOKEN"));
                        return map;
                    });
                    if (!userDetails.isEmpty()) {
                        Map<String, Object> userDetail = userDetails.get(0);
                        String userPhoneNumber = (String) userDetail.get("phoneNumber");
                        String userFcmToken = (String) userDetail.get("fcmToken");
                        if (userPhoneNumber != null && userFcmToken != null) {
                            pushNotificationServiceForNotf.sendNotification(userFcmToken, "Schedule Creation", message);
                            System.out.println("Notification sent to user phone number: " + userPhoneNumber);
                        }
                    }
                }
            }*/
            if (!RES) {
                logger.error("Error occurred in API call {} (Status Code: {}): {}", EndpointPropertyKey.EDIT_SCHEDULE,response.getStatusCodeValue(),response.getBody().getMessage());
            }
            return response;
        } catch (Exception e) {
            logger.error("An error occurred in the editSchedule method: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GlobalResponseDTO(false, "An error occurred while processing the request", null));
        }
    }

    private boolean isSmsEnabled(int reminderId, String tenantId) {
        String sql = "SELECT \"SMS\" FROM \"" + tenantId + "\".\"reminder_event_details\" WHERE \"REMINDER_ID\" = ?";
        Boolean smsFlag = jdbcTemplate.queryForObject(sql, Boolean.class, reminderId);
        return smsFlag != null && smsFlag;
    }

    private String getReminderMessage(int reminderId) {
        String sql = "SELECT \"REMINDER_MESSAGE\" FROM \"Bizfns\".\"REMINDER_EVENT_MASTER\" WHERE \"REMINDER_ID\" = ?";
        return jdbcTemplate.queryForObject(sql, String.class, reminderId);
    }
    @PostMapping(EndpointPropertyKey.ADD_WORKING_HOURS)
    public ResponseEntity<GlobalResponseDTO> addWorkingHours(@RequestBody Map<String, Object> request, Principal principal) {

        return scheduleService.addWorkingHours(request, principal);
    }

    @GetMapping(EndpointPropertyKey.GET_WORKING_HOURS)
    public ResponseEntity<GlobalResponseDTO> getWorkingHours(Principal principal) {
        Map<String, String> response = scheduleService.getWorkingHours(principal);
        GlobalResponseDTO globalResponseDTO = new GlobalResponseDTO(true, "success", response);
        return new ResponseEntity<>(globalResponseDTO, HttpStatus.OK);
    }
    @PostMapping(EndpointPropertyKey.SERVICE_ENTITY_FIELD)
    public ResponseEntity<GlobalResponseDTO> serviceEntityField(@RequestBody Map<String, String> request) {

        return scheduleService.serviceEntityField(request);
    }

    @PostMapping(EndpointPropertyKey.SCHEDULE_TIME_INTERVAL_SAVE)
    public ResponseEntity<GlobalResponseDTO> ScheduleTimeIntervalSave(@RequestBody Map<String, String> request, Principal principal) throws ParseException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            String authorityName = authority.getAuthority();
            System.err.println("rolesss :  " + authorityName);
        }
        return scheduleService.saveTimeInterval(request, principal);
    }

    @GetMapping(EndpointPropertyKey.GET_SCHEDULE_TIME_INTERVAL)
    public ResponseEntity<GlobalResponseDTO> getScheduleTimeInterval(Principal principal) {

        return scheduleService.getTimeIntervalFromDb(principal);
    }

    @PostMapping(EndpointPropertyKey.GET_Job_price)
    public ResponseEntity<GlobalResponseDTO> GET_Job_price(@RequestBody Map<String, String> request, Principal principal) throws CustomException {

        return scheduleService.calCulateJobPrice(request, principal);
    }
    @PostMapping(EndpointPropertyKey.save_Media_file)
    public ResponseEntity<GlobalResponseDTO> save_Media_file(MultipartFile[] file,String tenantId,String Pkjobid,String auditId, Principal principal) throws IOException, CustomException {

        return scheduleService.saveMediaFile(file,tenantId,Pkjobid,auditId, principal);
    }

    @PostMapping(EndpointPropertyKey.get_Media_file)
    public ResponseEntity<GlobalResponseDTO> get_Media_file(@RequestBody Map<String, String> request) {

        return scheduleService.getMediaFile(request);
    }
    @PostMapping(EndpointPropertyKey.delete_Media_file)
    public ResponseEntity<GlobalResponseDTO> delete_Media_file(@RequestBody Map<String, String> request) {

        return scheduleService.deleteMediaFile(request);
    }

    @GetMapping (value = EndpointPropertyKey.download_Media_file,produces = {MediaType.IMAGE_PNG_VALUE,MediaType.IMAGE_JPEG_VALUE,MediaType.IMAGE_GIF_VALUE})
    public void download_Media_file(@PathVariable("imageName") String imageName, HttpServletResponse response) throws IOException {

         scheduleService.downloadImage(imageName,response);
    }


    @PostMapping(EndpointPropertyKey.CUST_WISE_SERVICE_ENTITY)
    public ResponseEntity<GlobalResponseDTO> custWiseServiceEntity(@RequestBody Map<String, String> request) {

        return scheduleService.custWiseServiceEntity(request);
    }
    @PostMapping(EndpointPropertyKey.GET_SERVICE_ENTITY_DETAILS)
    public ResponseEntity<GlobalResponseDTO> getServiceEntityDetails(@RequestBody Map<String, String> request) {

        return scheduleService.getServiceEntityDetails(request);
    }

    @PostMapping(EndpointPropertyKey.GET_SERVICE_ENTITY_DETAILS_customerId)
    public ResponseEntity<GlobalResponseDTO> getServiceEntityDetailsBycustomerids(@RequestBody Map<String, String> request) {

        return scheduleService.getServiceEntityDetailsbyCustomerId(request);
    }

    /*
     * @Author AGNIC BISWAS
     * THIS METHOD USED To create a Invoice
     * @PARAM JOB ID AND TANENT ID
     * */
    @PostMapping (EndpointPropertyKey.CREATE_INVOICE)
    public  ResponseEntity<GlobalResponseDTO>  createInvoice(@RequestBody Map<String, Object> request, Principal principal) {

      return   scheduleService.generateInvoice(request, principal);

    }

    @PostMapping (EndpointPropertyKey.SaveEdit_InvoiceValues_By_JobIdAndCustomerIds)
    public  ResponseEntity<GlobalResponseDTO>  SaveEditInvoiceValuesByJobIdAndCustomerIds(@RequestBody Map<String, Object> request, Principal principal) {

        return   scheduleService.SaveEditInvoiceValuesByJobIdAndCustomerIds(request, principal);

    }

    @PostMapping (EndpointPropertyKey.UpdateEdit_InvoiceValues_By_CustomerId)
    public ResponseEntity<GlobalResponseDTO> updateEditInvoiceValues(@RequestBody Map<String, Object> request, Principal principal) throws IOException {
        return scheduleService.updateEditInvoiceValues(request, principal);
    }

    @PostMapping (EndpointPropertyKey.GetEdit_InvoiceValues_By_JobIdAndCustomerIds)
    public ResponseEntity<Map<String, Object>> getEditInvoiceValuesByJobIdAndCustomerId(@RequestBody Map<String, Object> request) {
        return scheduleService.getEditInvoiceValuesByJobIdAndCustomerId(request);
    }

    @PostMapping(EndpointPropertyKey.Create_Invoice_Pdf_By_Customers)
    public ResponseEntity<Map<String, Object>> createInvoicePdfByCustomers(@RequestBody Map<String, Object> request) throws IOException {
        return scheduleService.createInvoicePdfByCustomers(request);
    }


    @GetMapping("/api/users/invoices/{invoiceId}.pdf")
    public ResponseEntity<FileSystemResource> getInvoicePdf(
            @PathVariable("invoiceId") String invoiceId) {
        String filePath = "src/main/resources/static/invoices/"+ invoiceId + ".pdf";
        File pdfFile = new File(filePath);
        if (pdfFile.exists()) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.inline().filename(invoiceId + ".pdf").build());
            FileSystemResource fileSystemResource = new FileSystemResource(pdfFile);
            return new ResponseEntity<>(fileSystemResource, headers, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(EndpointPropertyKey.saveTimeSheet)
    public ResponseEntity<GlobalResponseDTO> saveTimeSheet(@RequestBody Map<String, Object> request,Principal principal) {
        return scheduleService.saveTimeSheet(request,principal);
    }

    @PostMapping(EndpointPropertyKey.updateTimeSheet)
    public ResponseEntity<GlobalResponseDTO> updateTimeSheet(@RequestBody Map<String, Object> request) {
        return scheduleService.updateTimeSheet(request);
    }

    @GetMapping(EndpointPropertyKey.getTimeSheetList)
    public ResponseEntity<GlobalResponseDTO> getTimeSheetList(@RequestBody Map<String, Object> request) {
        return scheduleService.getTimeSheetList(request);
    }

    @PostMapping(EndpointPropertyKey.getTimeSheetByBillNoAndStaffId)
    public ResponseEntity<GlobalResponseDTO> getTimeSheetByBillNoAndDate(@RequestBody Map<String, Object> request) {
        return scheduleService.getTimeSheetByBillNoAndStaffId(request);
    }

    @PostMapping(EndpointPropertyKey.GET_INVOICELISTS_BY_JOBID)
    public ResponseEntity<GlobalResponseDTO> getInvoiceListsByJobId(@RequestBody Map<String, Object> request, Principal principal) {
        return scheduleService.getInvoiceListsByJobId(request,principal);
    }


    /*
     * @Author AGNIC BISWAS
     * THIS METHOD USED Get Invoice as per the name
     * @PARAM JOB ID AND TANENT ID
     * */
    @GetMapping (value = EndpointPropertyKey.download_Invoice_file,produces = {MediaType.APPLICATION_PDF_VALUE})
    public void DownloadInvoice(@PathVariable("invoice") String invoice, HttpServletResponse response) throws IOException {

        scheduleService.downloadInvoice(invoice,response);
    }
    @PostMapping (EndpointPropertyKey.customerHistory)
    public  ResponseEntity<GlobalResponseListObject>  getCustomerHistory(@RequestBody Map<String, Object> request) {

        return  scheduleService.getCustomerHistory(request);

    }

    @PostMapping (EndpointPropertyKey.get_customer_service_history)
    public  ResponseEntity<GlobalResponseListObject>  getCustomerServiceHistory(@RequestBody Map<String, Object> request) {

        return  scheduleService.getCustomerServiceHistory(request);

    }

    @PostMapping(EndpointPropertyKey.save_Max_Job_Task)
    public  ResponseEntity<GlobalResponseDTO>  saveMaxJobTask(@RequestBody Map<String, Object> request) {

        return  scheduleService.saveMaxJobTask(request);

    }

    @GetMapping(EndpointPropertyKey.get_MAx_Job_Task)
    public  ResponseEntity<GlobalResponseDTO>  getMAxJobTask() {

        return  scheduleService.getMaxJobTask();

    }


    @PostMapping (EndpointPropertyKey.staff_change_password_using_temporary_password)
    public  ResponseEntity<GlobalResponseDTO>  getstaffUserLogin(@RequestBody Map<String, Object> request) {

        return  scheduleService.staffUserLogin(request);

    }

    @PostMapping (EndpointPropertyKey.save_job_status)
    public  ResponseEntity<GlobalResponseDTO>  saveJobStatus(@RequestBody Map<String, Object> request) {

        return  scheduleService.saveJobStatus(request);

    }

    @PostMapping (EndpointPropertyKey.get_job_status)
    public  ResponseEntity<GlobalResponseDTO>  getJobStatus(@RequestBody Map<String, Object> request) {

        return  scheduleService.getJobStatus(request);

    }

    @PostMapping(EndpointPropertyKey.delete_Service_Object)
    public ResponseEntity<GlobalResponseDTO> deleteServiceObject(@RequestBody Map<String, String> request) {

        return scheduleService.deleteServiceObject(request);
    }

}
