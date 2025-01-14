package com.bizfns.services.Serviceimpl;


import com.bizfns.services.GlobalDto.GlobalResponseDTO;

import com.bizfns.services.Notification.PushNotificationServiceForNotf;
import com.bizfns.services.Query.ScheduleQuery;
import com.bizfns.services.Repository.AddScheduleRepository;
import com.bizfns.services.Repository.CompanyUserRepository;
import com.bizfns.services.Repository.ProfileRepository;
import com.bizfns.services.Service.AddScheduleService;
import com.bizfns.services.SmsService.Scheduler;
import com.bizfns.services.Utility.AccessTokenValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AddScheduleServiceimpl implements AddScheduleService {

    @Autowired
    private AccessTokenValidation token;

    @Autowired
    private Scheduler scheduler;

    @Autowired
    PushNotificationServiceForNotf pushNotificationServiceForNotf;

    @Autowired
    private AddScheduleRepository addScheduleRepository;

    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private ScheduleQuery scheduleQuery;

    @Autowired
    private CompanyUserRepository companyUserRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /*
     * AMIT KUMAR SINGH
     * This method is used to Create new job schedule as per the start date end date taking into account
     * durationofrecurr and number of recur on how many recurring want for that particular job schedule
     * @PARAM JOB ID AND TANENT ID
     * */
    @Override
    @CacheEvict(value = "scheduleList", allEntries = true)
    public ResponseEntity<GlobalResponseDTO> addNewSchedule(Map<String, Object> request,HttpServletRequest httpRequest, Principal principal) {

        String deviceId = (String) request.get("deviceId");
        String deviceType = (String) request.get("deviceType");
        String appVersion = (String) request.get("appVersion");
        String userId = (String) request.get("userId");
        String tenantId = (String) request.get("tenantId");
        Integer pkServiceEntityId=null;

        if(checkUserMatch(userId,tenantId, principal.getName())){
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorised user, we could not access the APIs from others token "));
        }

        Map<String, Object> jobDetails = (Map<String, Object>) request.get("jobDetails");


//        if (!scheduleQuery.existUserId(tenantId).equals(userId)){
//            return ResponseEntity.accepted()
//                                .body(new GlobalResponseDTO(false, "The userId not matched with this tenantId or User Not Found", null));
//  }
        String startDate = (String) jobDetails.get("startDate");
        String startTime = (String) jobDetails.get("startTime");
        String endDate = (String) jobDetails.get("endDate");
        String endTime = (String) jobDetails.get("endTime");
        String jobStropOn = (String) jobDetails.get("jobstopdate");

        String materialsId = (String) jobDetails.get("materials");
        String DurationOfrecurr = (String) jobDetails.get("DurationOfrecurr");
        String Numberofrecurr = (String) jobDetails.get("Numberofrecurr");
        String recurrType = (String) jobDetails.get("recurrType");
        String jobStatus=(String) jobDetails.get("jobstatus");
        String jobLocation=(String) jobDetails.get("joblocation");
        String paymentDuration=(String) jobDetails.get("paymentDuration");
        String deposit=(String) jobDetails.get("deposit");
        String jobNotes= jobDetails.get("note").toString();
        String ImageId= jobDetails.get("imageId").toString();
        int weekNumber =  Integer.parseInt((String)jobDetails.get("weekNumber"));

        int year= Integer.parseInt(startDate.split("-")[0]);
        int Month= Integer.parseInt(startDate.split("-")[1]);
        int day= Integer.parseInt(startDate.split("-")[2]);

        int hour=Integer.parseInt(startTime.split(":")[0]);
        int minutes=Integer.parseInt(startTime.split(":")[1]);

        LocalDate initialDateTime = LocalDate.of(year, Month, day);

        int year_end= Integer.parseInt(endDate.split("-")[0]);
        int Month_end= Integer.parseInt(endDate.split("-")[1]);
        int day_end= Integer.parseInt(endDate.split("-")[2]);

        int hour_end=Integer.parseInt(endTime.split(":")[0]);
        int minutes_end=Integer.parseInt(endTime.split(":")[1]);

        LocalDate endinitialDateTime = LocalDate.of(year_end, Month_end, day_end);
        Duration recurrenceDuration;

        LocalDate currentStartTime = initialDateTime;
        LocalDate currentStartTimeCheck = initialDateTime;

        LocalDate currentEndTime = endinitialDateTime;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String StartTime_recurr=hour+":"+minutes;
        String EndTime_recurr=hour_end+":"+minutes_end;
        LocalDate StartDate_recurr_present =(LocalDate.of(currentStartTime.getYear(), currentStartTime.getMonthValue(), currentStartTime.getDayOfMonth())) ;

        LocalDate EndDate_recurr_present = (LocalDate.of(currentEndTime.getYear(), currentEndTime.getMonthValue(), currentEndTime.getDayOfMonth()));
        int numberofRecurr;
        if(!jobStropOn.isEmpty()){
            LocalDate stopJob_recurr = LocalDate.parse(jobStropOn);
             numberofRecurr = countOccurrences(initialDateTime, stopJob_recurr, recurrType);
        }else {
            numberofRecurr =Integer.parseInt(Numberofrecurr);
        }



//        for(int i=0; i<numberofRecurr;i++) {
//            currentStartTimeCheck = currentStartTimeCheck.plusDays(Integer.parseInt(DurationOfrecurr));
//            //StartDate_recurr_present=currentStartTimeCheck;
//
//           // currentEndTime = currentEndTime.plusDays(Integer.parseInt(DurationOfrecurr));
//            //EndDate_recurr_present=currentEndTime;
//            List<Map<String, Object>> staffList1 = (List<Map<String, Object>>) jobDetails.get("staffList");
//            if (!staffList1.isEmpty()) {
//                for (Map<String, Object> staff : staffList1) {
//                    String staffId = (String) staff.get("staffId");
//                    List<Map<String, Object>> getJobAvailabilityData = scheduleQuery.jobDataAvailabilityCheck(tenantId, staffId, currentStartTime.toString(), startTime, endTime);
//
//                    if (!getJobAvailabilityData.isEmpty()){
//                        return ResponseEntity.accepted()
//                                .body(new GlobalResponseDTO(false, "The staff is already occupied. Please look for other staff", null));
//
//                    }
//                }
//            }
//
//        }

        for(int i=0; i<numberofRecurr;i++) {
            //  recurrenceDuration = Duration.ofDays(Integer.parseInt(DurationOfrecurr));
            List<Map<String, Object>> staffList = (List<Map<String, Object>>) jobDetails.get("staffList");
            StringBuilder jobstaff = new StringBuilder();
            if (staffList.isEmpty()) {
                jobstaff.append("0");
            } else {
                for (Map<String, Object> staffName : staffList) {
                    String staffIds = (String) staffName.get("staffId");
                    jobstaff.append(staffIds).append(",");

                }
            }

            Integer assignJobId = null;
            if (!staffList.isEmpty()) {
                for (Map<String, Object> staff : staffList) {
                    String staffId = (String) staff.get("staffId");
                    List<Map<String, Object>> getJobAvailabilityData = scheduleQuery.jobDataAvailabilityCheck(tenantId, staffId, currentStartTimeCheck.toString(), startTime, endTime);
                   // if (!getJobAvailabilityData.isEmpty()) {

                           //   return ResponseEntity.accepted()
                            //          .body(new GlobalResponseDTO(false, "The staff is already occupied. Please look for other staff", null));

                        //  assignJobId = scheduleQuery.insertAssignJobData(pkJobId, Integer.valueOf(0), Integer.valueOf(staffId), StartDate_recurr, tenantId, EndDate_recurr, StartTime_recurr, EndTime_recurr);
                  //  }
                    // else{
                    //     scheduleQuery.deleteAllAssignJobData(tenantId, String.valueOf(pkJobId));
                    // scheduleQuery.removeStaffInfoFromJobMaster(tenantId,String.valueOf(pkJobId),staffId);
                    //      return ResponseEntity.accepted()
                    //              .body(new GlobalResponseDTO(false, "The staff is already occupied. Please look for other staff", null));
                    //  }
                }
            }

            currentStartTimeCheck = currentStartTimeCheck.plusDays(Integer.parseInt(DurationOfrecurr));

        }
        //String previousImageId = ImageId;
        if((recurrType.equalsIgnoreCase("Day") || recurrType.equalsIgnoreCase("Week")) /*&& reucrrSlotValidation(startDate, startTime,  endDate, endTime, DurationOfrecurr, Numberofrecurr, recurrType, weekNumber, tenantId)*/){
            {

            }



                    for(int i=0; i<numberofRecurr;i++){
              //  recurrenceDuration = Duration.ofDays(Integer.parseInt(DurationOfrecurr));

                List<Map<String, Object>> staffList = (List<Map<String, Object>>) jobDetails.get("staffList");
                StringBuilder jobstaff = new StringBuilder();
                if(staffList.isEmpty()){
                    jobstaff.append("0");
                 }else {
                    for(Map<String, Object> staffName :staffList){
                        String staffIds = (String) staffName.get("staffId");
                        jobstaff.append(staffIds).append(",");

                    }
                }

                        List<Map<String, Object>> materialList = (List<Map<String, Object>>) jobDetails.get("materialList");
                        StringBuilder jobMaterial = new StringBuilder();
                        if (materialList.isEmpty()) {
                            jobMaterial.append("0");
                        } else {
                            for (int k = 0; k < materialList.size(); k++) {
                                Map<String, Object> materialName = materialList.get(k);
                                String materialIds = (String) materialName.get("materialId");
                                jobMaterial.append(materialIds);
                                if (k < materialList.size() - 1) {
                                    jobMaterial.append(",");
                                }
                            }
                        }
                DateTimeFormatter formatterr = DateTimeFormatter.ofPattern("yyMMdd");
                String jobDate = LocalDate.parse(startDate).format(formatterr);
                String jobSequenceNumberQuery = "SELECT COUNT(\"PK_JOB_ID\") FROM \"" + tenantId + "\".\"job_master\" " +
                                "WHERE \"JOB_DATE\" = ?";
                int jobSequenceNumber = jdbcTemplate.queryForObject(jobSequenceNumberQuery, Integer.class, LocalDate.parse(startDate));
                System.err.println("staffdata"+jobstaff.deleteCharAt(jobstaff.length()-1));
                String StartDate_recurr = StartDate_recurr_present.format(formatter);
                String EndDate_recurr = EndDate_recurr_present.format(formatter);

                        String strInsertJobDetails = "INSERT INTO \"" + tenantId + "\".\"job_master\" (" +
                                "\"PK_JOB_ID\", \"FK_COMPANY_SUBSCRIPTION_ID\", \"JOB_START_TIME\", " +
                                "\"JOB_END_TIME\", \"JOB_DATE\", \"JOB_STOP_ON\", \"JOB_MATERIAL\", " +
                                "\"JOB_CREATED_AT\", \"STAFF_DETAILS\", \"JOB_STATUS\", \"JOB_NOTES\", \"JOB_LOCATION\", \"PAYMENT_DURATION\", \"PAYMENT_DEPOSIT\", \"IMAGE_AUDIT_ID\") " +
                                "VALUES (" +
                                "(SELECT COALESCE((SELECT MAX(\"PK_JOB_ID\") FROM \"" + tenantId + "\".\"job_master\"), 0) + 1), " +
                                "(SELECT \"FK_SUBSCRIPTION_PLAN_ID\" FROM \"Bizfns\".\"COMPANY_SUBSCRIPTION\" WHERE \"FK_COMPANY_BUSINESS_MAPPING_ID\" =" +
                                "(SELECT \"PK_COMPANY_BUSINESS_MAPPING_ID\" FROM \"Bizfns\".\"COMPANY_BUSINESS_TYPE_MAPPING\" WHERE \"FK_COMPANY_ID\" =" +
                                "(SELECT \"COMPANY_ID\" FROM \"Bizfns\".\"COMPANY_MASTER\" WHERE \"SCHEMA_ID\" = ?)" +
                                ")" +
                                "), " +
                                "?::TIME, " +
                                "?::TIME, " +
                                "?::DATE, " +
                                "?::DATE, " +
                                "?, " +
                                "?::DATE, " +
                                "?, " +
                                "?, " +
                                "?, " +
                                "?, " +
                                "?, " +
                                "?, " +
                                "? " +
                                ") RETURNING \"PK_JOB_ID\"";
                        Object[] params = new Object[] {
                                tenantId,
                                StartTime_recurr,
                                EndTime_recurr,
                                LocalDate.parse(StartDate_recurr),
                                LocalDate.parse(EndDate_recurr),
                                jobMaterial,
                                LocalDate.parse(startDate),
                                jobstaff,
                                jobStatus,
                                jobNotes != null && !jobNotes.isEmpty() ? jobNotes : "",
                                jobLocation,
                                paymentDuration != null && !paymentDuration.isEmpty() ? paymentDuration : null,
                                deposit != null && !deposit.isEmpty() ? deposit : null,
                                ImageId
                        };
                        Integer pkJobId = jdbcTemplate.queryForObject(strInsertJobDetails, params, Integer.class);
                String scheduleId = String.format("%s%03d", jobDate, jobSequenceNumber + 1);
                String updateScheduleIdQuery = "UPDATE \"" + tenantId + "\".\"job_master\" " +
                                "SET \"SCHEDULE_ID\" = ? " +
                                "WHERE \"PK_JOB_ID\" = ?";
                Object[] updateParams = new Object[]{ scheduleId, pkJobId };
                int rowsAffected = jdbcTemplate.update(updateScheduleIdQuery, updateParams);
                currentStartTime = currentStartTime.plusDays(Integer.parseInt(DurationOfrecurr));
                StartDate_recurr_present=currentStartTime;
                currentEndTime = currentEndTime.plusDays(Integer.parseInt(DurationOfrecurr));
                EndDate_recurr_present=currentEndTime;
                Integer assignJobId = null;
                 if(!staffList.isEmpty()){
                     for (Map<String, Object> staff : staffList) {
                         String staffId = (String) staff.get("staffId");
                         List<Map<String, Object>> getJobAvailabilityData = scheduleQuery.jobDataAvailabilityCheck(tenantId,staffId,currentStartTime.toString(),startTime,endTime);
                         if(getJobAvailabilityData.isEmpty()){
                             assignJobId = scheduleQuery.insertAssignJobData(pkJobId, Integer.valueOf(0), Integer.valueOf(staffId), StartDate_recurr, tenantId,EndDate_recurr,StartTime_recurr,EndTime_recurr);
                         }
                        // else{
                        //     scheduleQuery.deleteAllAssignJobData(tenantId, String.valueOf(pkJobId));
                            // scheduleQuery.removeStaffInfoFromJobMaster(tenantId,String.valueOf(pkJobId),staffId);
                       //      return ResponseEntity.accepted()
                       //              .body(new GlobalResponseDTO(false, "The staff is already occupied. Please look for other staff", null));
                       //  }
                     }
                 }

                List<Map<String, Object>> customer = (List<Map<String, Object>>) jobDetails.get("customer");
                 if(!customer.isEmpty()){
                     for (Map<String, Object> customerList : customer){
                         String customerId =(String)customerList.get("customerId");
                         ArrayList<String> serviceEntityId = (ArrayList<String>)customerList.get("serviceEntityId");
                         String mappedServiceEntityId = convertStringArrayToString(serviceEntityId, ",").toString();
                         scheduleQuery.insertCustomerandServiceEntityByJobId(pkJobId,Integer.parseInt(customerId),mappedServiceEntityId,tenantId);

                     }
                 }
                 /*String[] previousImageIdsArray = previousImageId.split(",");
                 if(previousImageId != null && !previousImageId.isEmpty()){
                     for (String previImageId : previousImageIdsArray) {
                         List<Map<String, Object>> fileNameAsPerAuditID = scheduleQuery.getMediaNameList(previImageId.trim(), tenantId);
                         Map<String, Object> firstRecord = fileNameAsPerAuditID.get(0);
                         Object name = firstRecord.get("FILE_NAME");
                         String fileName = (String) name;
                         String modeuleName = "Add new schedule";
                         LocalDateTime currentTimestamp = LocalDateTime.now();
                         String sql1 = "INSERT INTO " + tenantId + ".media (\"PK_MEDIA_ID\",\"MEDIA_MODULE_NAME\",\"FK_MODULE_PRIMARY_ID\",\"FILE_NAME\",\"UPLOAD_DATE_TIME\",\"JOB_ID\",\"IMAGE_AUDIT_ID\") VALUES ((SELECT COALESCE((SELECT MAX(\"PK_MEDIA_ID\") FROM \"" + tenantId + "\".\"media\"),0)+1),?,?,?,?,?,?)";
                         jdbcTemplate.update(sql1, modeuleName, pkJobId, fileName, currentTimestamp, pkJobId, previImageId);
                         *//*long timestamp = Instant.now().toEpochMilli();*//*
                     }
                 }*/

                        /*Map<String, Object> service = (Map<String, Object>) jobDetails.get("service");
                        String serviceId = (String) service.get("serviceId");
                        scheduleQuery.insertServiceDetails(serviceId, String.valueOf(pkJobId), tenantId);*/

                        Map<String, Object> service = (Map<String, Object>) jobDetails.get("service");
                        if (service == null || service.isEmpty()) {
                            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Error: Please add service to add the schedule."));
                        } else {
                            String serviceId = (String) service.get("serviceId");

                            if (serviceId == null || serviceId.isEmpty()) {
                                return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Error: Please add service to add the schedule."));
                            } else {
                                scheduleQuery.insertServiceDetails(serviceId, String.valueOf(pkJobId), tenantId);
                            }
                        }
                        //String phoneNo = "+919113780416";
                        //String message = "Your job (ID: " + pkJobId + ") got created";
                        //String token = "c27KE31xRf6lyREYQfyQL3:APA91bEb49XpfXmkmrwpcQFj0ZS1uzMNzcwtbywe0bZCw8r3__3wb1KymuI2Scq4b1M6zLBLgqrJdFXJunICPWXQq-PD2mIfc2h7VPTg5EnMvVPwoEMia5nxz_uay-wT0EI4-P-r3e30";
                        //scheduler.throwMessageBasedOnScheduleStatus(phoneNo,message);
                        //pushNotificationServiceForNotf.sendNotification(token, "Schedule Created", message);
                        String message = "Your job (ID: " + pkJobId + ") got created";
                        for (Map<String, Object> staff : staffList) {
                            String staffId = (String) staff.get("staffId");
                            String staffPhoneQuery = "SELECT cu.\"USER_PHONE_NUMBER\" " +
                                    "FROM \"" + tenantId + "\".\"company_user\" cu " +
                                    "WHERE cu.\"PK_USER_ID\" = ?";
                            List<String> staffPhoneNumbers = jdbcTemplate.query(staffPhoneQuery, new Object[]{Integer.parseInt(staffId)},
                                    (rs, rowNum) -> rs.getString("USER_PHONE_NUMBER"));
                            if (!staffPhoneNumbers.isEmpty()) {
                                String staffPhoneNumber = staffPhoneNumbers.get(0);
                                String fcmTokenQuery = "SELECT fcm.\"FCM_TOKEN\" " +
                                        "FROM \"Bizfns\".\"FCM_TOKEN\" fcm " +
                                        "WHERE fcm.\"USER_ID\" = ?";
                                List<String> staffFcmTokens = jdbcTemplate.query(fcmTokenQuery, new Object[]{staffId},
                                        (rs, rowNum) -> rs.getString("FCM_TOKEN"));
                                /*if (!staffFcmTokens.isEmpty()) {
                                    String staffFcmToken = staffFcmTokens.get(0);  // Get the FCM token
                                    if (staffPhoneNumber != null && staffFcmToken != null) {
                                        pushNotificationServiceForNotf.sendNotification(staffFcmToken, "Schedule Created", message);
                                    }
                                }*/
                            }
                        }
                        String userTokenQuery = "SELECT \"FCM_TOKEN\" FROM \"Bizfns\".\"FCM_TOKEN\" WHERE \"USER_ID\" = ?";
                        List<String> userTokens = jdbcTemplate.query(userTokenQuery, new Object[]{userId}, (rs, rowNum) -> rs.getString("FCM_TOKEN"));
                        /*if (!userTokens.isEmpty()) {
                            String userFcmToken = userTokens.get(0);
                            pushNotificationServiceForNotf.sendNotification(userFcmToken, "Schedule Created", message);
                        }*/
                    }
        } else if ((recurrType.equalsIgnoreCase("Month") || recurrType.equalsIgnoreCase("year")) /*&& reucrrSlotValidation(startDate, startTime,  endDate, endTime, DurationOfrecurr, Numberofrecurr, recurrType, weekNumber, tenantId)*/) {

             for(int i=0; i<numberofRecurr;i++){

                List<Map<String, Object>> staffList = (List<Map<String, Object>>) jobDetails.get("staffList");
                StringBuilder jobstaff = new StringBuilder();

                if(staffList.isEmpty()){
                    jobstaff.append("0");
                }else {
                    for(Map<String, Object> staffName :staffList){
                        String staffIds = (String) staffName.get("staffId");


                        jobstaff.append(staffIds).append(",");

                    }
                }
                 List<Map<String, Object>> materialList = (List<Map<String, Object>>) jobDetails.get("materialList");
                 StringBuilder jobMaterial = new StringBuilder();
                 if (materialList.isEmpty()) {
                     jobMaterial.append("0");
                 } else {
                     for (int k = 0; k < materialList.size(); k++) {
                         Map<String, Object> materialName = materialList.get(k);
                         String materialIds = (String) materialName.get("materialId");
                         jobMaterial.append(materialIds);
                         if (k < materialList.size() - 1) {
                             jobMaterial.append(",");
                         }
                     }
                 }
                String StartDate_recurr = StartDate_recurr_present.format(formatter);
                String EndDate_recurr = EndDate_recurr_present.format(formatter);

                 String strInsertJobDetails = "INSERT INTO \"" + tenantId + "\".\"job_master\" (" +
                         "\"PK_JOB_ID\", \"FK_COMPANY_SUBSCRIPTION_ID\", \"JOB_START_TIME\", " +
                         "\"JOB_END_TIME\", \"JOB_DATE\", \"JOB_STOP_ON\", \"JOB_MATERIAL\", " +
                         "\"JOB_CREATED_AT\", \"STAFF_DETAILS\", \"JOB_STATUS\", \"JOB_NOTES\", \"JOB_LOCATION\", \"PAYMENT_DURATION\", \"PAYMENT_DEPOSIT\", \"IMAGE_AUDIT_ID\") " +
                         "VALUES (" +
                         "(SELECT COALESCE((SELECT MAX(\"PK_JOB_ID\") FROM \"" + tenantId + "\".\"job_master\"), 0) + 1), " +
                         "(SELECT \"FK_SUBSCRIPTION_PLAN_ID\" FROM \"Bizfns\".\"COMPANY_SUBSCRIPTION\" WHERE \"FK_COMPANY_BUSINESS_MAPPING_ID\" =" +
                         "(SELECT \"PK_COMPANY_BUSINESS_MAPPING_ID\" FROM \"Bizfns\".\"COMPANY_BUSINESS_TYPE_MAPPING\" WHERE \"FK_COMPANY_ID\" =" +
                         "(SELECT \"COMPANY_ID\" FROM \"Bizfns\".\"COMPANY_MASTER\" WHERE \"SCHEMA_ID\" = ?)" +
                         ")" +
                         "), " +
                         "?::TIME, " +
                         "?::TIME, " +
                         "?::DATE, " +
                         "?::DATE, " +
                         "?, " +
                         "?::DATE, " +
                         "?, " +
                         "?, " +
                         "?, " +
                         "?, " +
                         "?, " +
                         "?, " +
                         "? " +
                         ") RETURNING \"PK_JOB_ID\"";
                 Object[] params = new Object[] {
                         tenantId,
                         StartTime_recurr,
                         EndTime_recurr,
                         LocalDate.parse(StartDate_recurr),
                         LocalDate.parse(EndDate_recurr),
                         jobMaterial,
                         LocalDate.parse(startDate),
                         jobstaff,
                         jobStatus,
                         jobNotes != null && !jobNotes.isEmpty() ? jobNotes : null,
                         jobLocation,
                         paymentDuration != null && !paymentDuration.isEmpty() ? paymentDuration : null,
                         deposit != null && !deposit.isEmpty() ? deposit : null,
                         ImageId
                 };
                 Integer pkJobId = jdbcTemplate.queryForObject(strInsertJobDetails, Integer.class);
                 StartDate_recurr_present =get_recurr_StartDate(LocalDate.of(currentStartTime.getYear(), currentStartTime.getMonthValue(), currentStartTime.getDayOfMonth()),recurrType,weekNumber) ;
                 currentStartTime=StartDate_recurr_present;
                 EndDate_recurr_present = get_recurr_endDate(LocalDate.of(currentEndTime.getYear(), currentEndTime.getMonthValue(), currentEndTime.getDayOfMonth()),recurrType,weekNumber);
                 currentEndTime=StartDate_recurr_present;
                  Integer assignJobId = null;
                 if(!staffList.isEmpty()){
                     for (Map<String, Object> staff : staffList) {
                         String staffId = (String) staff.get("staffId");
                         assignJobId =scheduleQuery.insertAssignJobData(pkJobId, Integer.valueOf(0), Integer.valueOf(staffId), StartDate_recurr, tenantId,EndDate_recurr,StartTime_recurr,EndTime_recurr);
                     }
                 }
                 List<Map<String, Object>> customer = (List<Map<String, Object>>) jobDetails.get("customer");
                 for (Map<String, Object> customerList : customer){
                     String customerId =(String)customerList.get("customerId");
                     ArrayList<String> serviceEntityId = (ArrayList<String>)customerList.get("serviceEntityId");
                     String mappedServiceEntityId = convertStringArrayToString(serviceEntityId, ",").toString();
                     scheduleQuery.insertCustomerandServiceEntityByJobId(pkJobId,Integer.parseInt(customerId),mappedServiceEntityId,tenantId);
                 }
                Map<String, Object> service = (Map<String, Object>) jobDetails.get("service");
                String serviceId = (String) service.get("serviceId");
                String serviceName = (String) service.get("serviceName");
                String serviceRate = (String) service.get("serviceRate");
                scheduleQuery.insertServiceDetails(serviceId, String.valueOf(pkJobId), tenantId);
                 String phoneNo = "+919113780416";
                 String message = "Your job (ID: " + pkJobId + ") got created";
                        /*String selectQuery = "SELECT \"reminder_messages\" FROM hoxs3359.reminder WHERE \"id\" = ?";
                        String reminderEvent = jdbcTemplate.queryForObject(selectQuery, new Object[]{1}, String.class);*/
                 //scheduler.throwMessageBasedOnScheduleStatus(phoneNo,message);
            }
        }
        String paymentModeId = (String) jobDetails.get("paymentModeId");
        String paymentTypeId = (String) jobDetails.get("paymentTypeId");
        String note = (String) jobDetails.get("note");
        List<String> attachments = (List<String>) jobDetails.get("attachments");
        if (attachments != null && !attachments.isEmpty()) {
            for (String attachment : attachments) {
            }
        } else {
        }
        return ResponseEntity.accepted()
                .body(new GlobalResponseDTO(true, "Successfully added", null));
    }

    private String extractBearerToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7); // Remove "Bearer " prefix
        }
        return null;
    }
    private static String convertStringArrayToString(ArrayList<String> strArr, String delimiter) {
        StringBuilder sb = new StringBuilder();
        for (String str : strArr)
            sb.append(str).append(delimiter);
        return sb.substring(0, sb.length() - 1);
    }
    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }


    public static int countOccurrences(LocalDate startDate, LocalDate jobStopon,String recurrType) {
        long daysBetween = ChronoUnit.DAYS.between(startDate, jobStopon)+1;
        if(recurrType.equalsIgnoreCase("day")){
                 return (int)daysBetween;
        } else if (recurrType.equalsIgnoreCase("week")) {
            return (int) Math.floorDiv(daysBetween,7)+1;
        } else if (recurrType.equalsIgnoreCase("month")) {
            return (int)Math.floorDiv(daysBetween,30)+1;
        } else if (recurrType.equalsIgnoreCase("year")) {
            return (int) Math.floorDiv(daysBetween,365)+1;

        }

        return 1;
    }

    /**
     * This method is used to add a new material with details like material
     * name,price,rate,category etc to the database.
     * @param request   A map containing the request parameters including deviceId, deviceType,
     *                  appVersion, userId, tenantId, and materialData which contains details
     *                  about the material to be added.
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> addMaterial(Map<String, Object> request, Principal principal) {
        // Extract request parameters

        String deviceId = (String) request.get("deviceId");
        String deviceType = (String) request.get("deviceType");
        String appVersion = (String) request.get("appVersion");
        String userId = (String) request.get("userId");
        String tenantId = (String) request.get("tenantId");



        if(checkUserMatch(userId,tenantId, principal.getName())){
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorised user, we could not access the APIs from others token "));
        }

        Map<String, Object> materialData = (Map<String, Object>) request.get("materialData");
        String categoryId = (String) materialData.get("categoryId");
        String materialName = (String) materialData.get("materialName");
        String materialRate = (String) materialData.get("materialRate");
        String materialType = (String) materialData.get("materialType");
        String materialRateUnitId = (String) materialData.get("materialRateUnitId");
        String subcategoryId = (String) materialData.get("subcategoryId");
        Integer categoryIdVal = 7;
        Integer subcategoryIdVal = 7;
        if (categoryId != null && !categoryId.isEmpty()) {
            try {
                categoryIdVal = Integer.valueOf(categoryId);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        if (subcategoryId != null && !subcategoryId.isEmpty()) {
            try {
                subcategoryIdVal = Integer.valueOf(subcategoryId);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        String dbCompBusinessId = companyUserRepository.dbCompBusinessId(userId,tenantId);
        if (dbCompBusinessId == null) {
            return ResponseEntity.accepted().body(new GlobalResponseDTO(false, "Please enter correct User Id", null));
        }
        Integer materialId = null;
        Map<String, Object> responseMaterialData = new HashMap<>();
        materialId = scheduleQuery.insertMaterialData(tenantId, categoryIdVal, materialName, materialType,
                materialRate, Integer.valueOf(materialRateUnitId), subcategoryIdVal);
        if (materialId != null) {
            responseMaterialData.put("materialId", materialId);
            responseMaterialData.put("materialName", materialName);
            return ResponseEntity.accepted()
                    .body(new GlobalResponseDTO(true, "Successfully added", responseMaterialData));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GlobalResponseDTO(false, "Failed to add material. No ID returned.", null));
        }
    }


    /**
     * This method retrieves the list of materials associated with the specified tenantId from the database.
     * @param request   A map containing the request parameters including deviceId, deviceType,
     *                  appVersion, userId, and tenantId.
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> materialList(Map<String, String> request, Principal principal) {
        String userId = request.get("userId");
        String tenantId = request.get("tenantId");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            String authorityName = authority.getAuthority();
            List<String> priviledgeChk = scheduleQuery.priviledgeChkForMaterial(tenantId);
            boolean hasEditPrivilege = false;
            for (String privilege : priviledgeChk) {
                if (privilege.equalsIgnoreCase("VIEW")) {
                    hasEditPrivilege = true;
                    break;
                }
            }
            if (authorityName.equalsIgnoreCase("Staff") && !hasEditPrivilege) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new GlobalResponseDTO(false, "A Staff user dont have the priviledge to ADD the MATERIAL.", null));
            }
        }
        if(checkUserMatch(userId,tenantId, principal.getName())){
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorised user, we could not access the APIs from others token "));
        }
        List<Map<String, Object>> fetchMaterialList = scheduleQuery.fetchMaterialList(tenantId);
        List<Map<String, Object>> responseData = new ArrayList<>();
        for (Map<String, Object> material : fetchMaterialList) {
            Map<String, Object> materialData = new HashMap<>();
            materialData.put("materialId", material.get("PK_MATERIAL_ID"));
            materialData.put("materialName", material.get("MATERIAL_NAME"));
            materialData.put("materialCategory", material.get("FK_CATEGORY_ID"));
            materialData.put("materialType", material.get("MATERIAL_TYPE"));
            materialData.put("materialRate", material.get("RATE"));
            materialData.put("activeStatus", material.get("MATERIAL_STATUS"));
            responseData.add(materialData);
        }

        // Return a ResponseEntity with the constructed response

        return ResponseEntity.accepted()
                .body(new GlobalResponseDTO(true, "Success", responseData));
    }


    /**
     * AMIT KUMAR SINGH
     * Thsi method retrieves material category data associated with the specified tenantId from the database.
     * @param request   A map containing the request parameters including deviceId, deviceType,
     *                  appVersion, userId, and tenantId.\
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> materialCategoryData(Map<String, String> request, Principal principal) {
        // Extract request parameters

        String deviceId = request.get("deviceId");
        String deviceType = request.get("deviceType");
        String appVersion = request.get("appVersion");
        String userId = request.get("userId");
        String tenantId = request.get("tenantId");


        if(checkUserMatch(userId,tenantId, principal.getName())){
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorised user, we could not access the APIs from others token "));
        }

        // Fetch material category data from the database

        List<Map<String, Object>> fetchMaterialCategoryData = scheduleQuery.fetchMaterialCategoryData(tenantId);


        // Creating a response data list

        /*List<Map<String, Object>> responseData = new ArrayList<>();
        for (Map<String, Object> materialCategory : fetchMaterialCategoryData) {
            // Create a map to hold material category data

            Map<String, Object> materialCategoryDate = new HashMap<>();
            materialCategoryDate.put("categoryId", materialCategory.get("PK_CATEGORY_ID"));
            materialCategoryDate.put("categoryName", materialCategory.get("CATEGORY_NAME"));
            materialCategoryDate.put("categoryParentId", materialCategory.get("PARENT_CATEGORY_ID"));
            responseData.add(materialCategoryDate);
        }*/

        // Return a ResponseEntity with the constructed response
        List<Map<String, Object>> result = fetchMaterialCategoryData.stream()
                .collect(Collectors.groupingBy(
                        m -> m.get("PK_CATEGORY_ID"),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("PK_CATEGORY_ID", list.get(0).get("PK_CATEGORY_ID"));
                                    map.put("CATEGORY_NAME", list.get(0).get("CATEGORY_NAME"));
                                    map.put("SubCategory", list.stream()
                                            .map(subcategory -> {
                                                Map<String, Object> subMap = new HashMap<>();
                                                subMap.put("pk_subcategory_name", subcategory.get("pk_subcategory_name"));
                                                subMap.put("pk_subcategory_id", subcategory.get("pk_subcategory_id"));
                                                return subMap;
                                            })
                                            .collect(Collectors.toList()));
                                    return map;
                                }
                        )
                ))
                .values().stream()
                .collect(Collectors.toList());

        return ResponseEntity.accepted()
                .body(new GlobalResponseDTO(true, "Success", result));
    }

    /**
     * AMIT KUMAR SINGH
     * This method retrieves details of a specific job identified by jobId associated with the specified userId and tenantId.
     *
     * @param request jobId.
     */

    @Override
    public ResponseEntity<GlobalResponseDTO> jobDetails(Map<String, String> request, Principal principal) {

        String deviceId = request.get("deviceId");
        String deviceType = request.get("deviceType");
        String appVersion = request.get("appVersion");
        String userId = request.get("userId");
        String tenantId = request.get("tenantId");
        String jobId = request.get("jobId");

        if(checkUserMatch(userId,tenantId, principal.getName())){
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorised user, we could not access the APIs from others token "));
        }

        List<Map<String, Object>> jobDetails = scheduleQuery.jobDetails(tenantId, userId, jobId);


        return ResponseEntity.accepted()
                .body(new GlobalResponseDTO(true, "Success", jobDetails));
    }

    /*
     * AMIT KUMAR SINGH
     * This method is used to delete the schedule and all the related schedule data based on JOB_ID.
     * */
    @Override
    @CacheEvict(value = "scheduleList", allEntries = true)
    public ResponseEntity<GlobalResponseDTO> deleteSchedule(Map<String, String> request, Principal principal) {

        String deviceId = request.get("deviceId");
        String deviceType = request.get("deviceType");
        String appVersion = request.get("appVersion");
        String userId = request.get("userId");
        String tenantId = request.get("tenantId");
        String jobId = request.get("jobId");

        if(checkUserMatch(userId,tenantId, principal.getName())){
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorised user, we could not access the APIs from others token "));
        }


        List<Map<String, Object>> getJobData = scheduleQuery.jobData(tenantId,jobId);
        //Amit Kumar Singh
        // Making it applicable for all the scenaries as the case of schedule with no staff being created creates issue in schedule deletion.
        /*if(getJobData.isEmpty()){
            return ResponseEntity.accepted()
                    .body(new GlobalResponseDTO(false, "Failed", null));
        }else {
            scheduleQuery.deleteSchedule(tenantId, jobId);
            return ResponseEntity.accepted()
                    .body(new GlobalResponseDTO(true, "Success", null));
        }*/
        boolean smsEnabled = isSmsEnabled(6,tenantId);
        if (smsEnabled) {
            String message = getReminderMessage(6);
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
                        /*if (!fcmTokens.isEmpty()) {
                            String staffFcmToken = fcmTokens.get(0);
                            if (staffPhoneNumber != null && staffFcmToken != null) {
                                pushNotificationServiceForNotf.sendNotification(staffFcmToken, "Schedule Creation", message);
                            }
                        }*/
                    }
                }

                String userQuery = "SELECT um.\"MOBILE_NUMBER\", fcm.\"FCM_TOKEN\" " +
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
                    /*if (userPhoneNumber != null && userFcmToken != null) {
                        pushNotificationServiceForNotf.sendNotification(userFcmToken, "Schedule Creation", message);
                        System.out.println("Notification sent to user phone number: " + userPhoneNumber);
                    }*/
                }
            } else {
                System.err.println("No message found for reminder ID: " + 6);
            }
        }
        scheduleQuery.deleteSchedule(tenantId, jobId);
        return ResponseEntity.accepted()
                .body(new GlobalResponseDTO(true, "Success", null));
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


    public LocalDate get_recurr_StartDate(LocalDate startDate,String reucrrType,int weekNumber){

      // int weekNumbers = startDate.get(WeekFields.of(Locale.getDefault()).weekOfMonth())-1;
        //System.err.println("test week number:::::::::::::::::::::::::::::"+weekNumbers);
       // Calculate the date of the second Monday in the next month

        if(reucrrType.equalsIgnoreCase("Month")){
            LocalDate   nextMonth = startDate.plusMonths(1);
            LocalDate nextDate = nextMonth.withDayOfMonth(1);

            while (nextDate.getDayOfWeek() != startDate.getDayOfWeek()) {
                nextDate = nextDate.plusDays(1);
            }
             nextDate = nextDate.plusWeeks(weekNumber);

           // System.out.println("start time of  the next month: " + nextDate);

            return nextDate;

        } else if (reucrrType.equalsIgnoreCase("year")) {
            // int weekNumbers = startDate.get(WeekFields.of(Locale.getDefault()).weekOfMonth())-1;
            LocalDate nextMonth = startDate.plusYears(1);
            LocalDate  nextDate = nextMonth.withDayOfMonth(1);

            while (nextDate.getDayOfWeek() != startDate.getDayOfWeek()) {
                nextDate = nextDate.plusDays(1);
            }


            nextDate = nextDate.plusWeeks(weekNumber);

            //System.out.println("start time of  the next month: " + nextDate);

            return nextDate;

        }

            return null;

    }
    public LocalDate get_recurr_endDate(LocalDate endDate,String reucrrType,int weekNumber) {


        // Calculate the date of the day in the next month

        if (reucrrType.equalsIgnoreCase("Month")) {
            LocalDate nextMonth = endDate.plusMonths(1);
            LocalDate nextDate = nextMonth.withDayOfMonth(1);

            while (nextDate.getDayOfWeek() != endDate.getDayOfWeek()) {
                nextDate = nextDate.plusDays(1);
            }

            // Find the second Monday
            nextDate = nextDate.plusWeeks(weekNumber);

            //System.out.println("end time of next month: " + nextDate);

            return nextDate;

        } else if (reucrrType.equalsIgnoreCase("year")) {
            LocalDate nextMonth = endDate.plusYears(1);
            LocalDate nextDate = nextMonth.withDayOfMonth(1);

            while (nextDate.getDayOfWeek() != endDate.getDayOfWeek()) {
                nextDate = nextDate.plusDays(1);
            }

            // Find the second Monday
            nextDate = nextDate.plusWeeks(weekNumber);

            //System.out.println("end time of next month: " + nextDate);

            return nextDate;

        }


        // Find the first day

        return null;
    }
    /*
     * @Author AGNIC BISWAS
     * THIS METHOD USED FOR get All startDate and endDate of current Input
     * @PARAM  startDate,endDate,tenantId,staffId,data for all the current recurr startdate enddate data
     * */

    @Override
    public ResponseEntity<GlobalResponseDTO> getAllrecurrDate(Map<String, String> request,Principal principal) {

        // Extracting fields from the JSON payload
      String startDate = (String) request.get("startDate");
        String startTime = (String) request.get("startTime");
        String endDate = (String) request.get("endDate");
        String endTime = (String) request.get("endTime");
        String tanentId=   (String) request.get("tanentId");
         String jobStropOn= request.get("jobstopdate");
        String DurationOfrecurr = (String) request.get("DurationOfrecurr");
        String Numberofrecurr = (request.get("Numberofrecurr")) ;
        String recurrType = (String) request.get("recurrType");
        int weekNumber =  Integer.parseInt((String)request.get("weekNumber"));
        //startDate and Time Of a job


        if(token.checkUserMatch(tanentId, principal.getName())){
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorised user, we could not access the APIs from others token "));
        }


        int year= Integer.parseInt(startDate.split("-")[0]);
        int Month= Integer.parseInt(startDate.split("-")[1]);
        int day= Integer.parseInt(startDate.split("-")[2]);

        String hour= String.valueOf(startTime.split(":")[0]);
        String minutes= String.valueOf(startTime.split(":")[1]);
        // Specify the initial date and time
        LocalDate initialDateTime = LocalDate.of(year, Month, day);


        //endtime and Time Of a job
        int year_end= Integer.parseInt(endDate.split("-")[0]);
        int Month_end= Integer.parseInt(endDate.split("-")[1]);
        int day_end= Integer.parseInt(endDate.split("-")[2]);

        String hour_end= String.valueOf(endTime.split(":")[0]);
        String minutes_end= String.valueOf(endTime.split(":")[1]);
        // Specify the initial date and time
        LocalDate endinitialDateTime = LocalDate.of(year_end, Month_end, day_end);
        Duration recurrenceDuration;

        // Create a variable to keep track of the current start time and end time
        LocalDate currentStartTime = initialDateTime;
        LocalDate currentEndTime = endinitialDateTime;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String StartTime_recurr=hour+":"+minutes;
        String EndTime_recurr=hour_end+":"+minutes_end;
        LocalDate StartDate_recurr_present =(LocalDate.of(currentStartTime.getYear(), currentStartTime.getMonthValue(), currentStartTime.getDayOfMonth())) ;

        LocalDate EndDate_recurr_present = (LocalDate.of(currentEndTime.getYear(), currentEndTime.getMonthValue(), currentEndTime.getDayOfMonth()));
         ArrayList<Map<String,String>> responceList=new ArrayList<>();
         int numberofrecurring =0;
        if(!jobStropOn.isEmpty()){
            LocalDate stopJob_recurr = LocalDate.parse(jobStropOn);
            numberofrecurring= countOccurrences(initialDateTime, stopJob_recurr, recurrType);
            //System.err.println(numberofrecurring);

        }else {
            numberofrecurring =Integer.parseInt(Numberofrecurr);
        }
        if(recurrType.equalsIgnoreCase("Day") || recurrType.equalsIgnoreCase("Week")){


            for(int i=0; i<numberofrecurring;i++){
                HashMap<String,String> responceDate= new HashMap<>();
                recurrenceDuration = Duration.ofDays(Integer.parseInt(DurationOfrecurr));
                  // Format the day as a two-digit day with leading zeros
                String StartDate_recurr = StartDate_recurr_present.format(formatter);
                String EndDate_recurr = EndDate_recurr_present.format(formatter);

                String slotValidationSql="SELECT * " +
                        "FROM " + tanentId + ".job_master jm " +
                        "WHERE (jm.\"JOB_DATE\" = '" + StartDate_recurr + "'::DATE " +
                        " AND jm.\"JOB_START_TIME\" >= '" + StartTime_recurr + "'::TIME " +
                        " AND jm.\"JOB_START_TIME\" <= '" + EndTime_recurr + "'::TIME) " +
                        " OR (jm.\"JOB_STOP_ON\" = '" + EndDate_recurr + "'::DATE " +
                        " AND jm.\"JOB_END_TIME\" >= '" + StartTime_recurr + "'::TIME " +
                        " AND jm.\"JOB_END_TIME\" <= '" + EndTime_recurr + "'::TIME) " +
                        " OR (jm.\"JOB_DATE\" < '" + StartDate_recurr + "'::DATE " +
                        " AND jm.\"JOB_STOP_ON\" > '" + EndDate_recurr + "'::DATE)";
                List<Map<String, Object>> staffnameQuery = jdbcTemplate.queryForList(slotValidationSql);

                if(staffnameQuery.size()>=4){
                    responceDate.put("status","0");// 0 means not available

                }else {

                    responceDate.put("status","1");//1 means available
                }


                responceDate.put("startTime",StartDate_recurr+" "+StartTime_recurr);
                responceDate.put("endTime",EndDate_recurr+" "+EndTime_recurr);
                responceList.add(responceDate);
                currentStartTime = currentStartTime.plusDays(Integer.parseInt(DurationOfrecurr));
                StartDate_recurr_present=currentStartTime;

                currentEndTime = currentEndTime.plusDays(Integer.parseInt(DurationOfrecurr));
                EndDate_recurr_present=currentEndTime;




            }
        } else if (recurrType.equalsIgnoreCase("Month") || recurrType.equalsIgnoreCase("year")) {

            for(int i=0; i<numberofrecurring;i++){
                HashMap<String,String> YearMap=new HashMap<>();

                  // Format the day as a two-digit day with leading zeros
                String StartDate_recurr = StartDate_recurr_present.format(formatter);
                String EndDate_recurr = EndDate_recurr_present.format(formatter);


                String slotValidationSql="SELECT * " +
                        "FROM " + tanentId + ".job_master jm " +
                        "WHERE (jm.\"JOB_DATE\" = '" + StartDate_recurr + "'::DATE " +
                        " AND jm.\"JOB_START_TIME\" >= '" + StartTime_recurr + "'::TIME " +
                        " AND jm.\"JOB_START_TIME\" <= '" + EndTime_recurr + "'::TIME) " +
                        " OR (jm.\"JOB_STOP_ON\" = '" + EndDate_recurr + "'::DATE " +
                        " AND jm.\"JOB_END_TIME\" >= '" + StartTime_recurr + "'::TIME " +
                        " AND jm.\"JOB_END_TIME\" <= '" + EndTime_recurr + "'::TIME) " +
                        " OR (jm.\"JOB_DATE\" < '" + StartDate_recurr + "'::DATE " +
                        " AND jm.\"JOB_STOP_ON\" > '" + EndDate_recurr + "'::DATE)";
                List<Map<String, Object>> staffnameQuery = jdbcTemplate.queryForList(slotValidationSql);

                if(staffnameQuery.size()>=4){
                    YearMap.put("status","0");// 0 means not available

                }else {

                    YearMap.put("status","1");//1 means available
                }


                YearMap.put("startTime",StartDate_recurr+" "+StartTime_recurr);
                YearMap.put("endTime",EndDate_recurr+" "+EndTime_recurr);
                responceList.add(YearMap);


                StartDate_recurr_present =get_recurr_StartDate(LocalDate.of(currentStartTime.getYear(), currentStartTime.getMonthValue(), currentStartTime.getDayOfMonth()),recurrType,weekNumber) ;
                currentStartTime=StartDate_recurr_present;

                EndDate_recurr_present = get_recurr_endDate(LocalDate.of(currentEndTime.getYear(), currentEndTime.getMonthValue(), currentEndTime.getDayOfMonth()),recurrType,weekNumber);

                currentEndTime=StartDate_recurr_present;

            }

        }

      return ResponseEntity.accepted()
                .body(new GlobalResponseDTO(true, "Success", responceList));


    }
    /*
     * @Author AGNIC BISWAS
     * THIS METHOD USED FOR validation staff is avialable or not
     * @PARAM  startDate,endDate,tenantId,staffId,data for all the current recurr startdate enddate data
     * */
   /* @Override
    public ResponseEntity<GlobalResponseDTO> recurrValidation(Map<String, Object> request) {

        String startDate = (String) request.get("startDate");
        String endDate = (String) request.get("endDate");
        ArrayList<String> staffIdList = (ArrayList<String>) request.get("staffId");
        String tanentId   =(String)request.get("tanendId");
       ArrayList<Map<String, Object>> staffAvailableList=new ArrayList<>();
       for(String staffId :staffIdList){
           ArrayList<HashMap<String,String>>  recurrDates= (ArrayList<HashMap<String,String>>)request.get("currentRecurrDate");

           ArrayList<HashMap<String,String>>   get_allstaffJob=  scheduleQuery.getstaffWistJobdetails(startDate,endDate,staffId,tanentId);

           if (isSlotAvailable(recurrDates,get_allstaffJob)) {

           String staffNameQuery=    "select \"USER_FIRST_NAME\",\n" +
                       "\"USER_LAST_NAME\" FROM \"" +
                   tanentId + "\".\"company_user\"  where \"PK_USER_ID\" ="+staffId;

               List<Map<String, Object>> staffName = jdbcTemplate.queryForList(staffNameQuery);

               Map<String, Object> staffNames= staffName.get(0);
               System.err.println(staffNames);
  // staffAvailableList.add(staffNames.get("USER_FIRST_NAME") +" "+staffNames.get("USER_LAST_NAME")+" All slot Available");
           } else {

               return ResponseEntity.accepted()
                       .body(new GlobalResponseDTO(false, "Slots are not available for this staff : " +staffId));


//               HashMap<String,String> staffOBJ=new HashMap<>();
//               String staffNameQueryNotAvialable=    "select \"USER_FIRST_NAME\",\n" +
//                       "\"USER_LAST_NAME\" FROM \"" +
//                       tanentId + "\".\"company_user\"  where \"PK_USER_ID\" ="+staffId;
//
//               List<Map<String, Object>> staffnameQuery = jdbcTemplate.queryForList(staffNameQueryNotAvialable);
//                  Map<String, Object> staffNames= staffnameQuery.get(0);
//                  staffNames.put("status","0");
//
//               staffAvailableList.add(staffNames);

           }
       }
     return ResponseEntity.accepted()
                .body(new GlobalResponseDTO(true, "Success", staffAvailableList));
    }*/

//    @Override
//    public ResponseEntity<GlobalResponseDTO> recurrValidation(Map<String, Object> request) {
//        String startDate = (String) request.get("startDate");
//        String endDate = (String) request.get("endDate");
//        ArrayList<String> staffIdList = (ArrayList<String>) request.get("staffId");
//        String tenantId = (String) request.get("tenentId");
//        ArrayList<Map<String, Object>> staffAvailableList = new ArrayList<>();
//
//        ArrayList<HashMap<String, String>> recurrDates = (ArrayList<HashMap<String, String>>) request.get("currentRecurrDate");
//
//        // If there are no recurring dates, use the end date from the request body
//        if (recurrDates.isEmpty()) {
//            // Proceed with validation using the provided end date
//            for (String staffId : staffIdList) {
//                ArrayList<HashMap<String, String>> allStaffJobs = scheduleQuery.getstaffWistJobdetails(startDate, endDate, staffId, tenantId);
//                if (isSlotAvailable(recurrDates, allStaffJobs)) {
//                    // Staff slot available
//                    String staffNameQuery = "select \"USER_FIRST_NAME\", \"USER_LAST_NAME\" FROM \"" +
//                            tenantId + "\".\"company_user\"  where \"PK_USER_ID\" =" + staffId;
//                    List<Map<String, Object>> staffName = jdbcTemplate.queryForList(staffNameQuery);
//                    Map<String, Object> staffNames = staffName.get(0);
//                    staffAvailableList.add(staffNames);
//                } else {
//                    // Staff slot not available
//                    return ResponseEntity.accepted()
//                            .body(new GlobalResponseDTO(false, "Slots are not available for this staff: " + staffId));
//                }
//            }
//        } else {
//            // Update the end date of the last element in the currentRecurrDate list
//            String lastEndDateStr = recurrDates.get(recurrDates.size() - 1).get("endTime");
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
//            LocalDateTime lastEndDate = LocalDateTime.parse(lastEndDateStr, formatter);
//
//
//            // lastRecurrDate.put("endTime", endDate); // Separate date part from the end date
//
//            endDate= String.valueOf(lastEndDate);
//
//            // Proceed with validation using the updated recurring dates
//            for (String staffId : staffIdList) {
//                ArrayList<HashMap<String, String>> allStaffJobs = scheduleQuery.getstaffWistJobdetails(startDate, endDate, staffId, tenantId);
//                if (isSlotAvailable(recurrDates, allStaffJobs)) {
//                    // Staff slot available
//                    String staffNameQuery = "select \"USER_FIRST_NAME\", \"USER_LAST_NAME\" FROM \"" +
//                            tenantId + "\".\"company_user\"  where \"PK_USER_ID\" =" + staffId;
//                    List<Map<String, Object>> staffName = jdbcTemplate.queryForList(staffNameQuery);
//                    Map<String, Object> staffNames = staffName.get(0);
//                    staffAvailableList.add(staffNames);
//                } else {
//                    // Staff slot not available
//                    return ResponseEntity.accepted()
//                            .body(new GlobalResponseDTO(false, "Slots are not available for this staff: " + staffId));
//                }
//            }
//        }
//
//        return ResponseEntity.accepted()
//                .body(new GlobalResponseDTO(true, "Success", staffAvailableList));
//    }

    /* 13.04.2024 @Override
    public ResponseEntity<GlobalResponseDTO> recurrValidation(Map<String, Object> request) {
        String startDate = (String) request.get("startDate");
        String endDate = (String) request.get("endDate");
        ArrayList<String> staffIdList = (ArrayList<String>) request.get("staffId");
        String tenantId = (String) request.get("tenantId");
        ArrayList<Map<String, Object>> staffAvailableList = new ArrayList<>();
        ArrayList<HashMap<String, String>> recurrDates = (ArrayList<HashMap<String, String>>) request.get("currentRecurrDate");
        ArrayList<Map<String, Object>> responseData = new ArrayList<>();
        boolean isSlotsAvailable = true;

        // Update the end date of the last element in the currentRecurrDate list
        String lastEndDateStr = recurrDates.get(recurrDates.size() - 1).get("endTime");

// Assuming the date string is in the format 'yyyy-MM-dd'
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        try {
            LocalDateTime lastEndDate = LocalDateTime.parse(lastEndDateStr, formatter);
            endDate = String.valueOf(lastEndDate);
        } catch (DateTimeParseException e) {
            // Handle parsing exception
            System.err.println("Error parsing end date: " + e.getMessage());
            // Optionally, you can return an error response or throw an exception
        }


        for (String staffId : staffIdList) {
            ArrayList<HashMap<String, String>> allStaffJobs = scheduleQuery.getstaffWistJobdetails(startDate, endDate, staffId, tenantId);
            boolean isSlotAvailable = isSlotAvailable(recurrDates, allStaffJobs);

            String staffNameQuery = "SELECT \"USER_FIRST_NAME\", \"USER_LAST_NAME\" FROM \"" +
                    tenantId + "\".\"company_user\" WHERE \"PK_USER_ID\" =" + staffId;
            List<Map<String, Object>> staffName = jdbcTemplate.queryForList(staffNameQuery);
            Map<String, Object> staffData = staffName.get(0);

            Map<String, Object> staffResponseData = new HashMap<>();
            staffResponseData.put("User ID", staffId);
            staffResponseData.put("USER_FIRST_NAME", staffData.get("USER_FIRST_NAME"));
            staffResponseData.put("USER_LAST_NAME", staffData.get("USER_LAST_NAME"));
            staffResponseData.put("Availability", isSlotAvailable ? 1 : 0);
            responseData.add(staffResponseData);

            if (!isSlotAvailable) {
                isSlotsAvailable = false;
            }
        }

        String message = isSlotsAvailable ? "All slots are available for the provided staff." : "Slots are not available for some staff.";

        return ResponseEntity.accepted().body(new GlobalResponseDTO(!isSlotsAvailable, message, responseData));
    }






    public boolean reucrrSlotValidation(String startDate,String startTime, String endDate,String endTime,String DurationOfrecurr,String Numberofrecurr,String recurrType,int weekNumber,String tanentId) {
         ArrayList<Map<String,String>> allrecurringDates=getAllrecurrDates(startDate, startTime,endDate,endTime, DurationOfrecurr, Numberofrecurr,recurrType, weekNumber);


        for(Map<String,String> dates:allrecurringDates){
            String startDate_recurr=dates.get("startDate");
            String endDate_recurr=dates.get("endDate");
            String startTime_recurr=dates.get("startTime");
            String endTime_recurr=dates.get("endTime");
            String slotValidationSql="SELECT * " +
                    "FROM " + tanentId + ".job_master jm " +
                    "WHERE (jm.\"JOB_DATE\" = '" + startDate_recurr + "'::DATE " +
                    " AND jm.\"JOB_START_TIME\" >= '" + startTime_recurr + "'::TIME " +
                    " AND jm.\"JOB_START_TIME\" <= '" + endTime_recurr + "'::TIME) " +
                    " OR (jm.\"JOB_STOP_ON\" = '" + endDate_recurr + "'::DATE " +
                    " AND jm.\"JOB_END_TIME\" >= '" + startTime_recurr + "'::TIME " +
                    " AND jm.\"JOB_END_TIME\" <= '" + endTime_recurr + "'::TIME) " +
                    " OR (jm.\"JOB_DATE\" < '" + startDate_recurr + "'::DATE " +
                    " AND jm.\"JOB_STOP_ON\" > '" + endDate_recurr + "'::DATE)";
            List<Map<String, Object>> staffnameQuery = jdbcTemplate.queryForList(slotValidationSql);

            if(staffnameQuery.size()>=4){
                return false;

            }
            System.err.println(staffnameQuery.size());
        }

        return true;
    }


    private boolean isSlotAvailable(ArrayList<HashMap<String,String>>   recurrDates,ArrayList<HashMap<String,String>>   get_allstaffJob) {

        for(HashMap<String,String> recurrDate:recurrDates) {
            //loop for recurr current dates
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime recurr_startDate_recurr = LocalDateTime.parse(String.valueOf(recurrDate.get("startTime")), formatter);
            System.err.println(recurr_startDate_recurr);
            LocalDateTime recurr_endDate_recurr = LocalDateTime.parse(recurrDate.get("endTime"), formatter);
            for (HashMap<String, String> bookedSlot : get_allstaffJob) {
                
                DateTimeFormatter formatter_satff= DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime staff_startDate_recurr = LocalDateTime.parse(bookedSlot.get("startTime"), formatter_satff);
                LocalDateTime staff_endDate_recurr = LocalDateTime.parse(bookedSlot.get("endTime"), formatter_satff);

                if (overlaps(staff_startDate_recurr, staff_endDate_recurr, recurr_startDate_recurr, recurr_endDate_recurr)) {
                    return false; // Slot is not available
                }
            }
        } pranta

        return true; // Slot is available
    }


    private boolean overlaps( LocalDateTime staff_startDate_recurr,  LocalDateTime staff_endDate_recurr, LocalDateTime startDateTime,  LocalDateTime endDateTime) {
        return startDateTime.isBefore(staff_endDate_recurr) &&
                staff_startDate_recurr.isBefore(endDateTime);


    }
13.04.2024 */


    /*
     * @AMIT KUMAR SINGH
     * This method is used for staff validation and to check if the staff is avialable or not between the startTime
     * and endTime on the day of creation of schedule.
     * @PARAM  startDate,endDate,tenantId,staffId,data for all the current recurr startdate enddate data
     * */
    @Override
    public ResponseEntity<GlobalResponseDTO> recurrValidation(Map<String, Object> request, Principal principal) {
        String startDate = (String) request.get("startDate");
        String endDate = (String) request.get("endDate");
        ArrayList<String> staffIdList = (ArrayList<String>) request.get("staffId");
        String tenantId = (String) request.get("tenantId");
        String jobId = (String) request.get("jobId");
        ArrayList<HashMap<String, String>> recurrDates = (ArrayList<HashMap<String, String>>) request.get("currentRecurrDate");
        ArrayList<Map<String, Object>> responseData = new ArrayList<>();
        boolean isSlotsAvailable = true;
        if(token.checkUserMatch(tenantId, principal.getName())){
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorised user, we could not access the APIs from others token "));
        }
        LocalDateTime startDateTime;
        DateTimeFormatter formatter1;
        String startDateAndTime = recurrDates.get(0).get("startTime");
       try{
           formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
           startDateTime = LocalDateTime.parse(startDateAndTime, formatter1);
           }
           catch(DateTimeParseException E){
               formatter1 = DateTimeFormatter.ofPattern("yyyy-M-dd HH:mm");
               startDateTime = LocalDateTime.parse(startDateAndTime, formatter1);
       }
        String lastEndDateAndTime = recurrDates.get(recurrDates.size() - 1).get("endTime");
        LocalDateTime lastEndDateAndTime1 = LocalDateTime.parse(lastEndDateAndTime,formatter1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        try {
            LocalDateTime lastEndDate = LocalDateTime.parse(lastEndDateAndTime, formatter);
            endDate = String.valueOf(lastEndDate);
        } catch (DateTimeParseException e) {
        }

        for (String staffId : staffIdList) {
            ArrayList<HashMap<String, String>> allStaffJobs = scheduleQuery.getstaffWistJobdetails(startDateTime, lastEndDateAndTime1, staffId, tenantId);
            boolean isSlotAvailable = isSlotAvailable(startDate,recurrDates, allStaffJobs, jobId, tenantId,staffId); // Added jobId and tenantId
            String staffNameQuery = "SELECT \"USER_FIRST_NAME\", \"USER_LAST_NAME\" FROM \"" +
                    tenantId + "\".\"company_user\" WHERE \"PK_USER_ID\" =" + staffId;
            List<Map<String, Object>> staffName = jdbcTemplate.queryForList(staffNameQuery);
            Map<String, Object> staffData = staffName.get(0);
            Map<String, Object> staffResponseData = new HashMap<>();
            staffResponseData.put("User ID", staffId);
            staffResponseData.put("USER_FIRST_NAME", staffData.get("USER_FIRST_NAME"));
            staffResponseData.put("USER_LAST_NAME", staffData.get("USER_LAST_NAME"));
            staffResponseData.put("Availability", isSlotAvailable ? 1 : 0);
            responseData.add(staffResponseData);
            if (!isSlotAvailable) {
                isSlotsAvailable = false;
            }
        }

        String message = isSlotsAvailable ? "All slots are available for the provided staff." : "Slots are   available for some staff.";
        return ResponseEntity.accepted().body(new GlobalResponseDTO(!isSlotsAvailable, message, responseData));
    }

    public ResponseEntity<GlobalResponseDTO> deleteMaterial(Map<String, Object> request, Principal principal) {
        String tenantId = (String) request.get("tenantId");
        String userId = principal.getName();
        String materialId = (String) request.get("materialId");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            String authorityName = authority.getAuthority();
            List<String> priviledgeChk = scheduleQuery.priviledgeChkForMaterial(tenantId);
            boolean hasEditPrivilege = false;
            for (String privilege : priviledgeChk) {
                if (privilege.equalsIgnoreCase("DELETE")) {
                    hasEditPrivilege = true;
                    break;
                }
            }
            if (authorityName.equalsIgnoreCase("Staff") && !hasEditPrivilege) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new GlobalResponseDTO(false, "A Staff user dont have the priviledge to delete the material.", null));
            }
        }
        try {
            boolean isDeleted = scheduleQuery.deleteMaterialData(tenantId, Integer.parseInt(materialId));

            if (isDeleted) {
                return ResponseEntity.ok().body(new GlobalResponseDTO(true, "Material has been deleted successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new GlobalResponseDTO(false, "Material ID is invalid or material not found"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GlobalResponseDTO(false, "An error occurred while deleting material data"));
        }
    }

    @Override
    public ResponseEntity<GlobalResponseDTO> getMaterialDetails(Map<String, Object> request, Principal principal) {
        try {
            String tenantId = (String) request.get("tenantId");
            String userId = principal.getName();
            String materialId = (String) request.get("materialId");
            if (checkUserMatch(userId, tenantId, principal.getName())) {
                return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorized user, we could not access the APIs from others token "));
            }
            List<Map<String, Object>> fetchMaterialList = scheduleQuery.fetchMaterialDataByMaterialId(tenantId, Integer.valueOf(materialId));

            if (fetchMaterialList.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new GlobalResponseDTO(false, "Material data for the ID " + materialId + " not found"));
            }
            Map<String, Object> materialData = fetchMaterialList.get(0);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("materialId", materialData.get("PK_MATERIAL_ID"));
            responseData.put("categoryId", materialData.get("FK_CATEGORY_ID"));
            responseData.put("subcategoryId", materialData.get("FK_SUBCATEGORY_ID"));
            responseData.put("materialName", materialData.get("MATERIAL_NAME"));
            responseData.put("materialRate", materialData.get("RATE"));
            responseData.put("materialType", materialData.get("MATERIAL_TYPE"));
            responseData.put("materialRateUnitId", materialData.get("FK_MATERIAL_UNIT_ID"));
            responseData.put("activeStatus", materialData.get("MATERIAL_STATUS"));

            return ResponseEntity.accepted()
                    .body(new GlobalResponseDTO(true, "Material details retrieved successfully", responseData));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GlobalResponseDTO(false, "Failed to retrieve material details: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<GlobalResponseDTO> updateMaterialDetails(Map<String, Object> request, Principal principal) {
        try {
            String tenantId = (String) request.get("tenantId");
            String userId = (String) request.get("userId");
            String materialId = (String) request.get("materialId");
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            for (GrantedAuthority authority : authorities) {
                String authorityName = authority.getAuthority();
                List<String> priviledgeChk = scheduleQuery.priviledgeChkForMaterial(tenantId);
                boolean hasEditPrivilege = false;
                for (String privilege : priviledgeChk) {
                    if (privilege.equalsIgnoreCase("EDIT")) {
                        hasEditPrivilege = true;
                        break;
                    }
                }
                if (authorityName.equalsIgnoreCase("Staff") && !hasEditPrivilege) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new GlobalResponseDTO(false, "A Staff user dont have the priviledge to edit the material.", null));
                }
            }
            if (checkUserMatch(userId, tenantId, principal.getName())) {
                return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorized user, we could not access the APIs from others token"));
            }
            scheduleQuery.updateMaterialData(tenantId, Integer.valueOf(materialId), request);
            return ResponseEntity.accepted().body(new GlobalResponseDTO(true, "Material details updated successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GlobalResponseDTO(false, "Failed to update material details: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<GlobalResponseDTO> addMaterialCategory(Map<String, Object> request, Principal principal) {
        String tenantId = (String) request.get("tenantId");
        String userId = (String) request.get("userId");
        String categoryName = (String) request.get("categoryName");
        String parentCategoryId = (String) request.get("categoryName");
        if(checkUserMatch(userId,tenantId, principal.getName())){
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorised user, we could not access the APIs from others token "));
        }
        String dbCompBusinessId = companyUserRepository.dbCompBusinessId(userId,tenantId);
        if (dbCompBusinessId == null) {
            return ResponseEntity.accepted().body(new GlobalResponseDTO(false, "Please enter correct User Id", null));
        }
        String checkCategoryExistsQuery = "SELECT COUNT(*) FROM \"" + tenantId + "\".\"material_category_master\" " +
                "WHERE \"CATEGORY_NAME\" = ?";
        Integer count = jdbcTemplate.queryForObject(checkCategoryExistsQuery, new Object[]{categoryName}, Integer.class);
        if (count != null && count > 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new GlobalResponseDTO(false, "Category name already exists: " + categoryName, null));
        }
        Map<String, Object> responseCategoryData = new HashMap<>();
        Integer categoryId = scheduleQuery.addMaterialCategoryInDB(tenantId, categoryName);
        if (categoryId != null) {
            responseCategoryData.put("tenantId",tenantId);
            responseCategoryData.put("userId", userId);
            responseCategoryData.put("categoryId", categoryId);
            responseCategoryData.put("categoryName", categoryName);
            return ResponseEntity.accepted()
                    .body(new GlobalResponseDTO(true, "Successfully added", responseCategoryData));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GlobalResponseDTO(false, "Failed to add category. No ID returned.", null));
        }
    }

    @Override
    public ResponseEntity<GlobalResponseDTO> addMaterialSubCategory(Map<String, Object> request, Principal principal) {
        String tenantId = (String) request.get("tenantId");
        String userId = (String) request.get("userId");
        String categoryId = (String) request.get("categoryId");
        String subcategoryName = (String) request.get("subcategoryName");

        if(checkUserMatch(userId, tenantId, principal.getName())){
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorized user, we could not access the APIs from others token"));
        }

        String dbCompBusinessId = companyUserRepository.dbCompBusinessId(userId, tenantId);
        if (dbCompBusinessId == null) {
            return ResponseEntity.accepted().body(new GlobalResponseDTO(false, "Please enter correct User Id", null));
        }

        String checkSubCategoryExistsQuery = "SELECT COUNT(*) FROM \"" + tenantId + "\".\"material_subcategory_master\" " +
                "WHERE \"pk_subcategory_name\" = ?";
        Integer count = jdbcTemplate.queryForObject(checkSubCategoryExistsQuery,
                new Object[]{subcategoryName}, Integer.class);
        if (count != null && count > 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new GlobalResponseDTO(false, "Subcategory name already exists : '" + subcategoryName + "' for category ID: " + categoryId, null));
        }

        Map<String, Object> responseSubCategoryData = new HashMap<>();
        Integer subcategoryId = scheduleQuery.addMaterialSubCategoryInDB(tenantId, Integer.parseInt(categoryId), subcategoryName);
        if (subcategoryId != null) {
            responseSubCategoryData.put("subcategoryName", subcategoryName);
            responseSubCategoryData.put("subcategoryId", subcategoryId);
            return ResponseEntity.accepted()
                    .body(new GlobalResponseDTO(true, "Subcategory added successfully.", responseSubCategoryData));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GlobalResponseDTO(false, "Failed to add subcategory. No ID returned.", null));
        }
    }

    @Override
    public ResponseEntity<GlobalResponseDTO> deleteCategoryAndSubcategory(Map<String, String> request, Principal principal) {
        String tenantId = request.get("tenantId");
        String userId = request.get("userId");
        Integer categoryId = Integer.parseInt(request.get("categoryId"));
        String subcategoryIdStr = request.get("subcategoryId");
        Integer subcategoryId = null;
        if (subcategoryIdStr != null && !subcategoryIdStr.trim().isEmpty()) {
            subcategoryId = Integer.parseInt(subcategoryIdStr);
        }
        if (checkUserMatch(userId, tenantId, principal.getName())) {
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorized user, we could not access the APIs from others token"));
        }

        String dbCompBusinessId = companyUserRepository.dbCompBusinessId(userId, tenantId);
        if (dbCompBusinessId == null) {
            return ResponseEntity.accepted().body(new GlobalResponseDTO(false, "Please enter correct User Id", null));
        }

        try {
            int rowsAffected = scheduleQuery.deleteSubcategoryFromDB(tenantId, categoryId, subcategoryId);
            if (rowsAffected == 2) {
                return ResponseEntity.ok().body(new GlobalResponseDTO(true, "Category deleted successfully", null));
            }else if (rowsAffected == 1) {
                return ResponseEntity.ok().body(new GlobalResponseDTO(true, "Subcategory deleted successfully", null));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GlobalResponseDTO(false, "Failed to delete subcategory. No rows affected.", null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GlobalResponseDTO(false, "An unexpected error occurred", e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<GlobalResponseDTO> getActiveInactiveStatusForService(Map<String, String> request, Principal principal) {
        String tenantId = request.get("tenantId");
        String serviceId = request.get("serviceId");

        if (tenantId == null || serviceId == null) {
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Tenant ID or service ID is missing"));
        }

        try {
            String query = "SELECT \"ID\", \"STATUS\" FROM \"" + tenantId + "\".\"business_type_wise_service_master\" WHERE \"ID\" = ?";
            Map<String, Object> serviceStatus = jdbcTemplate.queryForMap(query,Integer.parseInt(serviceId));
            String status = (String) serviceStatus.get("STATUS");
            Integer serviceIdFromDb = (Integer) serviceStatus.get("ID");
            Map<String, Object> response = new HashMap<>();
            response.put("serviceId", serviceIdFromDb);
            response.put("activeStatus :- ", status);
            return ResponseEntity.ok(new GlobalResponseDTO(true, "Success", response));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new GlobalResponseDTO(false, "Failed to retrieve service status: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<GlobalResponseDTO> UpdateActiveInactiveStatusForService(Map<String, String> request, Principal principal) {
        String tenantId = request.get("tenantId");
        String serviceId = request.get("serviceId");
        String status = request.get("status");
        if (tenantId == null || serviceId == null || status == null) {
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Tenant ID, service ID or status is missing"));
        }
        try {
            String updateStatusQuery = "UPDATE \"" + tenantId + "\".\"business_type_wise_service_master\" " +
                    "SET \"STATUS\" = ? " +
                    "WHERE \"ID\" = ?";
            int rowsAffected = jdbcTemplate.update(updateStatusQuery, status, Integer.parseInt(serviceId));
            if (rowsAffected > 0) {
                String statusMessage = status.equals("1") ? "activated" : "deactivated";
                return ResponseEntity.ok(new GlobalResponseDTO(true, "Service Status has been " + statusMessage + " successfully"));
            } else {
                return ResponseEntity.status(404).body(new GlobalResponseDTO(false, "Service update failed"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new GlobalResponseDTO(false, "Error occurred while updating service status: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<GlobalResponseDTO> getActiveInactiveStatusForMaterial(Map<String, String> request, Principal principal) {
        String tenantId = request.get("tenantId");
        String materialId = request.get("materialId");
        if (tenantId == null || materialId == null) {
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Tenant ID or material ID is missing"));
        }
        try {
            String query = "SELECT \"PK_MATERIAL_ID\", \"MATERIAL_STATUS\" FROM \"" + tenantId + "\".\"material_master\" WHERE \"PK_MATERIAL_ID\" = ?";
            Map<String, Object> materialStatus = jdbcTemplate.queryForMap(query, Integer.parseInt(materialId));
            String status = (String) materialStatus.get("MATERIAL_STATUS");
            Map<String, Object> response = new HashMap<>();
            response.put("materialId", materialStatus.get("PK_MATERIAL_ID"));
            response.put("activeStatus :-", status);
            return ResponseEntity.ok(new GlobalResponseDTO(true, "Success", response));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new GlobalResponseDTO(false, "Failed to retrieve material status: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<GlobalResponseDTO> UpdateActiveInactiveStatusForMaterial(Map<String, String> request, Principal principal) {
        String tenantId = request.get("tenantId");
        String materialId = request.get("materialId");
        String status = request.get("status");

        if (tenantId == null || materialId == null || status == null) {
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Tenant ID, material ID or status is missing"));
        }

        try {
            String updateStatusQuery = "UPDATE \"" + tenantId + "\".\"material_master\" " +
                    "SET \"MATERIAL_STATUS\" = ? " +
                    "WHERE \"PK_MATERIAL_ID\" = ?";
            int rowsAffected = jdbcTemplate.update(updateStatusQuery, status, Integer.parseInt(materialId));

            if (rowsAffected > 0) {
                String statusMessage = status.equals("1") ? "activated" : "deactivated";
                return ResponseEntity.ok(new GlobalResponseDTO(true, "Material has been " + statusMessage + " successfully"));
            } else {
                return ResponseEntity.status(404).body(new GlobalResponseDTO(false, "Material update failed"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new GlobalResponseDTO(false, "Error occurred while updating material status: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<GlobalResponseDTO> getJobNumberByDate(Map<String, String> request, Principal principal) {
        String tenantId = request.get("tenantId");
        String date = request.get("date");
        String query = "SELECT \"SCHEDULE_ID\" AS jobNumber, \"PK_JOB_ID\" AS jobId FROM \"" + tenantId + "\".\"job_master\" WHERE \"JOB_DATE\" = TO_DATE(?, 'YYYY-MM-DD')";

        try {
            List<Map<String, Object>> jobDetailsList = jdbcTemplate.queryForList(query, new Object[]{date});
            List<Map<String, Object>> jobDetails = new ArrayList<>();
            for (Map<String, Object> row : jobDetailsList) {
                Map<String, Object> jobDetail = new HashMap<>();
                jobDetail.put("jobNumber", row.get("jobNumber"));
                jobDetail.put("jobId", row.get("jobId"));
                jobDetails.add(jobDetail);
            }
            GlobalResponseDTO responseDTO = new GlobalResponseDTO();
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("jobDetails", jobDetails);
            responseDTO.setData(responseBody);
            responseDTO.setMessage("Job details fetched successfully");
            responseDTO.setSuccess(true);
            return ResponseEntity.ok(responseDTO);
        } catch (DataAccessException ex) {
            ex.printStackTrace();
            GlobalResponseDTO errorResponse = new GlobalResponseDTO();
            errorResponse.setMessage("Error fetching job details");
            errorResponse.setSuccess(false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    /*    private boolean isSlotAvailable(String startDate1, ArrayList<HashMap<String,String>> recurrDates, ArrayList<HashMap<String,String>> get_allstaffJob, String jobId, String tanentId, String staffId) {
            if (jobId != null && !jobId.isEmpty()) {
                int JobAvailability=0;
                int overlap = 0;
                for (HashMap<String,String> recurrDate : recurrDates) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    LocalDateTime recurrStartDate = LocalDateTime.parse(recurrDate.get("startTime"), formatter);
                    LocalDateTime recurrEndDate = LocalDateTime.parse(recurrDate.get("endTime"), formatter);
                    for (HashMap<String,String> bookedSlot : get_allstaffJob) {

                        DateTimeFormatter formatter_staff = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                        LocalDateTime staffStartDate = LocalDateTime.parse(bookedSlot.get("startTime"), formatter_staff);
                        LocalDateTime staffEndDate = LocalDateTime.parse(bookedSlot.get("endTime"), formatter_staff);
                        if (overlaps(staffStartDate, staffEndDate, recurrStartDate, recurrEndDate)) {
                            overlap++;
                            LocalDateTime firstUnavailableStartTime = LocalDateTime.parse(recurrDates.get(0).get("startTime"), formatter);
                            LocalDateTime firstUnavailableEndTime = LocalDateTime.parse(recurrDates.get(0).get("endTime"), formatter);
                            LocalDate startDate = firstUnavailableStartTime.toLocalDate();
                            LocalTime startTime = firstUnavailableStartTime.toLocalTime();
                            LocalTime endTime = firstUnavailableEndTime.toLocalTime();
                            String alreadyBookedJobIdQuery ="SELECT \"FK_JOB_ID\" FROM \"" + tanentId + "\".\"assigned_job\" WHERE \"assigned_job_start_time\" = '" + startTime + "' AND \"assigned_job_end_time\" = '" + endTime + "' AND \"ASSIGNED_JOB_DATE\" = '" + startDate + "' AND \"FK_USER_ID\" = '" + staffId + "'";
                            List<Map<String, Object>> jobIdResult = jdbcTemplate.queryForList(alreadyBookedJobIdQuery);
                            String recurrStartDateWithoutTime = String.valueOf(recurrStartDate.toLocalDate());
                            if(startDate1.equalsIgnoreCase(recurrStartDateWithoutTime)){
                                if (!jobIdResult.isEmpty()) {
                                    Map<String, Object> firstResultMap = jobIdResult.get(0);
                                    Object jobIdObject = firstResultMap.get("FK_JOB_ID");
                                    if (jobIdObject != null) {
                                        String fetchedJobIdStr = jobIdObject.toString();
                                        try {
                                            int fetchedJobId = Integer.parseInt(fetchedJobIdStr);
                                            int jobIdInt = Integer.parseInt(jobId);
                                            if (fetchedJobId == jobIdInt) {
                                                JobAvailability = 1;
                                            }
                                        } catch (NumberFormatException e) {
                                        }
                                    }
                                }
                                if (JobAvailability ==1){
                                    return true;
                                }
                            }
                            return false;
                        }
                        if (overlap>1){
                            return false;
                        }
                    }
                }
                return true;
            } else {
                for (HashMap<String,String> recurrDate : recurrDates) {
                    DateTimeFormatter formatter_recurr = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    LocalDateTime recurrStartDate = LocalDateTime.parse(recurrDate.get("startTime"), formatter_recurr);
                    LocalDateTime recurrEndDate = LocalDateTime.parse(recurrDate.get("endTime"), formatter_recurr);
                    for (HashMap<String,String> bookedSlot : get_allstaffJob) {
                        DateTimeFormatter formatter_staff = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                        LocalDateTime staffStartDate = LocalDateTime.parse(bookedSlot.get("startTime"), formatter_staff);
                        LocalDateTime staffEndDate = LocalDateTime.parse(bookedSlot.get("endTime"), formatter_staff);
                        if (overlaps(staffStartDate, staffEndDate, recurrStartDate, recurrEndDate)) {
                            return false;
                        }
                    }
                }
                return true;
            }

        }*/
    private boolean isSlotAvailable(String startDate1, ArrayList<HashMap<String, String>> recurrDates,
                                    ArrayList<HashMap<String, String>> get_allstaffJob,
                                    String jobId, String tenantId, String staffId) {
        if (jobId == null || jobId.isEmpty()) {
            return checkForOverlapWithoutJob(recurrDates, get_allstaffJob);
        } else {
            return checkForOverlapWithJob(startDate1, recurrDates, get_allstaffJob, jobId, tenantId, Collections.singletonList(staffId));
        }
    }

    private boolean checkForOverlapWithoutJob(ArrayList<HashMap<String, String>> recurrDates,
                                              ArrayList<HashMap<String, String>> get_allstaffJob) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (HashMap<String, String> recurrDate : recurrDates) {
            LocalDateTime recurrStartDate = LocalDateTime.parse(recurrDate.get("startTime"), formatter);
            LocalDateTime recurrEndDate = LocalDateTime.parse(recurrDate.get("endTime"), formatter);
            for (HashMap<String, String> bookedSlot : get_allstaffJob) {
                LocalDateTime staffStartDate = LocalDateTime.parse(bookedSlot.get("startTime"), formatter);
                LocalDateTime staffEndDate = LocalDateTime.parse(bookedSlot.get("endTime"), formatter);
                if (overlaps(staffStartDate, staffEndDate, recurrStartDate, recurrEndDate)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkForOverlapWithJob(String startDate1, ArrayList<HashMap<String, String>> recurrDates,
                                           ArrayList<HashMap<String, String>> get_allstaffJob,
                                           String jobId, String tenantId, List<String> staffIds) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (String staffId : staffIds) {
            int overlapCount = 0;

            for (HashMap<String, String> recurrDate : recurrDates) {
                LocalDateTime recurrStartDate = LocalDateTime.parse(recurrDate.get("startTime"), formatter);
                LocalDateTime recurrEndDate = LocalDateTime.parse(recurrDate.get("endTime"), formatter);

                for (HashMap<String, String> bookedSlot : get_allstaffJob) {
                    LocalDateTime staffStartDate = LocalDateTime.parse(bookedSlot.get("startTime"), formatter);
                    LocalDateTime staffEndDate = LocalDateTime.parse(bookedSlot.get("endTime"), formatter);

                    if (overlaps(staffStartDate, staffEndDate, recurrStartDate, recurrEndDate)) {
                        overlapCount++;
                        if (overlapCount > 1 || !isSameJob(startDate1, recurrStartDate, recurrDates, tenantId, staffId, jobId)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean isSameJob(String startDate1, LocalDateTime recurrStartDate,
                              ArrayList<HashMap<String, String>> recurrDates,
                              String tenantId, String staffId, String jobId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime firstUnavailableStartTime = LocalDateTime.parse(recurrDates.get(0).get("startTime"), formatter);
        LocalDateTime firstUnavailableEndTime = LocalDateTime.parse(recurrDates.get(0).get("endTime"), formatter);
        LocalDate startDate = firstUnavailableStartTime.toLocalDate();
        LocalTime startTime = firstUnavailableStartTime.toLocalTime();
        LocalTime endTime = firstUnavailableEndTime.toLocalTime();
        String query = String.format("SELECT \"FK_JOB_ID\" FROM \"%s\".\"assigned_job\" WHERE \"assigned_job_start_time\" = '%s' AND \"assigned_job_end_time\" = '%s' AND \"ASSIGNED_JOB_DATE\" = '%s' AND \"FK_USER_ID\" = '%s'",
                tenantId, startTime, endTime, startDate, staffId);
        List<Map<String, Object>> jobIdResult = jdbcTemplate.queryForList(query);
        String recurrStartDateWithoutTime = recurrStartDate.toLocalDate().toString();

        if (startDate1.equalsIgnoreCase(recurrStartDateWithoutTime) && !jobIdResult.isEmpty()) {
            Map<String, Object> firstResultMap = jobIdResult.get(0);
            Object jobIdObject = firstResultMap.get("FK_JOB_ID");
            if (jobIdObject != null) {
                try {
                    int fetchedJobId = Integer.parseInt(jobIdObject.toString());
                    int jobIdInt = Integer.parseInt(jobId);

                    if (fetchedJobId == jobIdInt) {
                        return true;
                    }
                } catch (NumberFormatException e) {
                }
            }
        }
        return false;
    }
    private boolean overlaps(LocalDateTime staff_startDate_recurr, LocalDateTime staff_endDate_recurr, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return (startDateTime.isBefore(staff_endDate_recurr) || startDateTime.isEqual(staff_endDate_recurr)) &&
                (staff_startDate_recurr.isBefore(endDateTime) || staff_startDate_recurr.isEqual(endDateTime));
    }



    public ArrayList<Map<String,String>> getAllrecurrDates(String startDate,String startTime, String endDate,String endTime,String DurationOfrecurr,String Numberofrecurr,String recurrType,int weekNumber) {


        //startDate and Time Of a job
        int year= Integer.parseInt(startDate.split("-")[0]);
        int Month= Integer.parseInt(startDate.split("-")[1]);
        int day= Integer.parseInt(startDate.split("-")[2]);

        int hour=Integer.parseInt(startTime.split(":")[0]);
        int minutes=Integer.parseInt(startTime.split(":")[1]);
        // Specify the initial date and time
        LocalDate initialDateTime = LocalDate.of(year, Month, day);


        //endtime and Time Of a job
        int year_end= Integer.parseInt(endDate.split("-")[0]);
        int Month_end= Integer.parseInt(endDate.split("-")[1]);
        int day_end= Integer.parseInt(endDate.split("-")[2]);

        int hour_end=Integer.parseInt(endTime.split(":")[0]);
        int minutes_end=Integer.parseInt(endTime.split(":")[1]);
        // Specify the initial date and time
        LocalDate endinitialDateTime = LocalDate.of(year_end, Month_end, day_end);

        Duration recurrenceDuration;

        // Create a variable to keep track of the current start time and end time
        LocalDate currentStartTime = initialDateTime;
        LocalDate currentEndTime = endinitialDateTime;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String StartTime_recurr=hour+":"+minutes;
        String EndTime_recurr=hour_end+":"+minutes_end;
        LocalDate StartDate_recurr_present =(LocalDate.of(currentStartTime.getYear(), currentStartTime.getMonthValue(), currentStartTime.getDayOfMonth())) ;

        LocalDate EndDate_recurr_present = (LocalDate.of(currentEndTime.getYear(), currentEndTime.getMonthValue(), currentEndTime.getDayOfMonth()));
        ArrayList<Map<String,String>> responceList=new ArrayList<>();
        if(recurrType.equalsIgnoreCase("Day") || recurrType.equalsIgnoreCase("Week")){


            for(int i=0; i<Integer.parseInt(Numberofrecurr);i++){
                HashMap<String,String> responceDate= new HashMap<>();
                recurrenceDuration = Duration.ofDays(Integer.parseInt(DurationOfrecurr));
                // Format the day as a two-digit day with leading zeros
                String StartDate_recurr = StartDate_recurr_present.format(formatter);
                String EndDate_recurr = EndDate_recurr_present.format(formatter);

                responceDate.put("startDate",StartDate_recurr);
                responceDate.put("endDate",EndDate_recurr);
                responceDate.put("startTime",StartTime_recurr);
                responceDate.put("endTime",EndTime_recurr);
                responceList.add(responceDate);
                currentStartTime = currentStartTime.plusDays(Integer.parseInt(DurationOfrecurr));
                StartDate_recurr_present=currentStartTime;

                currentEndTime = currentEndTime.plusDays(Integer.parseInt(DurationOfrecurr));
                EndDate_recurr_present=currentEndTime;




            }
        } else if (recurrType.equalsIgnoreCase("Month") || recurrType.equalsIgnoreCase("year")) {

            for(int i=0; i<Integer.parseInt(Numberofrecurr);i++){
                HashMap<String,String> YearMap=new HashMap<>();

                // Format the day as a two-digit day with leading zeros
                String StartDate_recurr = StartDate_recurr_present.format(formatter);
                String EndDate_recurr = EndDate_recurr_present.format(formatter);
                YearMap.put("startDate",StartDate_recurr);
                YearMap.put("endDate",EndDate_recurr);
                YearMap.put("startTime",StartTime_recurr);
                YearMap.put("endTime",EndTime_recurr);

                responceList.add(YearMap);


                StartDate_recurr_present =get_recurr_StartDate(LocalDate.of(currentStartTime.getYear(), currentStartTime.getMonthValue(), currentStartTime.getDayOfMonth()),recurrType,weekNumber) ;
                currentStartTime=StartDate_recurr_present;

                EndDate_recurr_present = get_recurr_endDate(LocalDate.of(currentEndTime.getYear(), currentEndTime.getMonthValue(), currentEndTime.getDayOfMonth()),recurrType,weekNumber);

                currentEndTime=StartDate_recurr_present;

            }

        }

        return responceList;
    }


    public boolean checkUserMatch(String userId,String tenantId , String username){

        String[] part = username.split(",");

        //String userPhone = profileRepository.checkAccessToken(userId, tenantId);

        String tokenUserId = part[0];
        String tokenTenantId = part[1];
        if (!tenantId.equals(tokenTenantId)){
            return true;
        }

        return false;
    }

}



