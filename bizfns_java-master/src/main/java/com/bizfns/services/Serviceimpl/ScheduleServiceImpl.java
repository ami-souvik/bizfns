package com.bizfns.services.Serviceimpl;

import com.bizfns.services.Exceptions.CustomException;
import com.bizfns.services.GlobalDto.GlobalResponseDTO;
import com.bizfns.services.GlobalDto.GlobalResponseListObject;
import com.bizfns.services.Notification.PushNotificationServiceForNotf;
import com.bizfns.services.Query.ScheduleQuery;
import com.bizfns.services.Query.StaffAuthQuery;
import com.bizfns.services.Query.StaffQuery;
import com.bizfns.services.Repository.CompanyMasterRepository;
import com.bizfns.services.Repository.CompanyUserRepository;
import com.bizfns.services.Repository.ProfileRepository;
import com.bizfns.services.Repository.ScheduleRepository;
import com.bizfns.services.Service.ScheduleService;
import com.bizfns.services.SmsService.Scheduler;
import com.bizfns.services.Utility.AccessTokenValidation;
import com.bizfns.services.Utility.JWTUtility;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.Principal;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.time.ZonedDateTime;
import java.time.Instant;

@Service
public class ScheduleServiceImpl implements ScheduleService {
    @Autowired
    private CompanyMasterRepository companyMasterRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private AccessTokenValidation token;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ScheduleQuery scheduleQuery;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JWTUtility jwtUtility;

    @Autowired
    private StaffQuery staffQuery;

    @Autowired
    private ErrorLogServiceImpl errorLogService;

    @Autowired
    private StaffAuthQuery staffAuthQuery;

    @Autowired
    private CompanyUserRepository companyUserRepository;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private SpringTemplateEngine springTemplateEngine;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    PushNotificationServiceForNotf pushNotificationServiceForNotf;

    @Value("${project.image}")
    private String path;

    @Value("${project.invoice}")
    private String invoicepath;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    Scheduler scheduler;

    /*
     * AMIT KUMAR SINGH
     * This method is used to fetch all the schedule/job data related
     * to a created schedule/job based on schedule/job date.
     * @PARAM SCHEDULE_DATE
     * */
    @Override
    @Cacheable("scheduleList")
    public ResponseEntity<GlobalResponseDTO> scheduleList(Map<String, String> request, Principal principal) {
        String deviceId = request.get("deviceId");
        String deviceType = request.get("deviceType");
        String appVersion = request.get("appVersion");
        String userId = request.get("userId");
        String tenantId = request.get("tenantId");
        if (checkUserMatch(userId, tenantId, principal.getName())) {
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorised user, we could not access the APIs from others token "));
        }
        String fromDate = request.get("fromDate");
        String deviceInfo = "deviceId = " + deviceId + "\n" + "deviceType = " + deviceType + "\n" + "appVersion = " + appVersion;
        String fullURL = httpServletRequest.getRequestURL().toString();
        try {
            List<Map<String, Object>> fetchJobData = scheduleQuery.ftechScheduleDatabyDate(tenantId, fromDate);
            List<Map<String, Object>> fetchJobs = arrangeScheduleData(fetchJobData, tenantId, userId, fromDate);
            return ResponseEntity.accepted().body(new GlobalResponseDTO(true, "Success", fetchJobs));
        } catch (Exception e) {
            errorLogService.errorLog(request, e.getMessage(), fullURL, userId, deviceInfo);
            return ResponseEntity.accepted().body(new GlobalResponseDTO(false, "Success", e.getMessage()));
        }
    }

    private boolean isNullOrEmpty(String value) {

        return value == null || value.trim().isEmpty() || value == "null";
    }

    //updated schedule list to handle the case where if a schedule is scheduled before working hour
    //then the working hour should be modified accordingly - AMIT KUMAR SINGH
    private List<Map<String, Object>> arrangeScheduleData(List<Map<String, Object>> rawData, String tenantId, String userId, String formDate) {
        List<Map<String, Object>> transformedData = new ArrayList<>();
        Map<String, Object> workingHours = scheduleQuery.getWorkingHours(formDate, userId, tenantId);
        Map<String, Object> timeIntervalMap = scheduleQuery.getTimeInterval(userId, tenantId, formDate);
        String intervalTimeString = timeIntervalMap.get("INTERVAL_TIME").toString();
        String[] intervalParts = intervalTimeString.split(":");
        double intervalTime = Double.parseDouble(intervalParts[0] + "." + intervalParts[1]);

        //int i = (int) timeIntervalMap.get("INTERVAL_TIME");
        //int i = Integer.parseInt((String) timeIntervalMap.get("INTERVAL_TIME"));
        int hours=0;
        int minutes=0;
        Object value = timeIntervalMap.get("INTERVAL_TIME");
        if (value instanceof String) {
            String valueStr = (String) value;
            String[] parts = valueStr.split("\\:");
            hours=Integer.parseInt(parts[0]);
            minutes=Integer.parseInt(parts[1]);
        }
        String startTimeWorkingStr = (String) workingHours.get("START_TIME");
        String endTimeWorkingStr = (String) workingHours.get("END_TIME");
        int startTimeWorking = 0;
        int endTimeWorking = 0;
        if (startTimeWorkingStr.contains(":")) {
            String[] startTimeParts = startTimeWorkingStr.split(":");
            startTimeWorking = Integer.parseInt(startTimeParts[0]) * 60 + Integer.parseInt(startTimeParts[1]);
        } else {
            startTimeWorking = Integer.parseInt(startTimeWorkingStr) * 60;
        }
        if (endTimeWorkingStr.contains(":")) {
            String[] endTimeParts = endTimeWorkingStr.split(":");
            endTimeWorking = Integer.parseInt(endTimeParts[0]) * 60 + Integer.parseInt(endTimeParts[1]);
        } else {
            endTimeWorking = Integer.parseInt(endTimeWorkingStr) * 60;
        }
        /*List<Map<String, Object>> fetchJobData = scheduleQuery.ftechScheduleDatabyDate(tenantId, formDate);
        if (!fetchJobData.isEmpty()) {
            List<Map<String, Object>> fetchJobTimeData = scheduleQuery.fetchJobTimingAsPerJobDate(tenantId, formDate);
            SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");
            for (Map<String, Object> fetchIndvJobTimeData : fetchJobTimeData) {
                Time earliestTime = (Time) fetchIndvJobTimeData.get("earliest_time");
                Time latestTime = (Time) fetchIndvJobTimeData.get("lastest_time");
                if (earliestTime != null && latestTime != null) {
                    String startTimeOfAvailableJob = timeFormatter.format(earliestTime);
                    int startHourOfAvailableJob = Integer.parseInt(startTimeOfAvailableJob.substring(0, 2));
                    if (startHourOfAvailableJob < startTimeWorking) {
                        int difference = startTimeWorking - startHourOfAvailableJob;
                        startTimeWorking = startHourOfAvailableJob;
                        endTimeWorking = endTimeWorking - difference;
                    }
                }
            }
        }*/
        int intervalMinutes = (hours * 60) + (minutes * 1);
        int startTimeInMinutes = startTimeWorking;
        int endTimeInMinutes = endTimeWorking;
        for (int currentTimeInMinutes = startTimeInMinutes; currentTimeInMinutes <= endTimeInMinutes; currentTimeInMinutes += intervalMinutes) {
            int currentHour24 = currentTimeInMinutes / 60;
            int currentMinute = currentTimeInMinutes % 60;
            int currentHour12 = currentHour24 % 12;
            if (currentHour12 == 0) {
                currentHour12 = 12;
            }
            String period = (currentHour24 < 12 /*|| currentHour24 == 24*/) ? "AM" : "PM";
            String startTime = String.format("%02d:%02d:00 %s", currentHour12, currentMinute, period);
            Map<String, Object> timeSlot = new HashMap<>();
            timeSlot.put("time", startTime);
            timeSlot.put("schedule", new ArrayList<>());
            transformedData.add(timeSlot);
        }
        /*for (int hour = startTimeWorking; hour <= endTimeWorking; hour += i) {
            String startTime = String.format("%02d:00:00", hour);
            Map<String, Object> timeSlot = new HashMap<>();
            timeSlot.put("time", startTime);
            timeSlot.put("schedule", new ArrayList<>());
            transformedData.add(timeSlot);
        }*/
        if (rawData.isEmpty()) {
            return transformedData;
        } else {
            List<Map<String, Object>> collect = null;
            for (Map<String, Object> map : rawData) {
                System.err.println(map);
                Map<String, Object> scheduleData = new HashMap<>();
                String endDate = map.get("endDate").toString();
                String startDate = map.get("startDate").toString();
                String JOB_START_TIME = map.get("JOB_START_TIME").toString();
                String JOB_END_TIME = map.get("JOB_END_TIME").toString();
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a");
                LocalTime startTime = LocalTime.parse(JOB_START_TIME, inputFormatter);
                LocalTime endTime = LocalTime.parse(JOB_END_TIME, inputFormatter);
                String formattedStartTime = startTime.format(outputFormatter);
                String formattedEndTime = endTime.format(outputFormatter);
                String JOB_STATUS = String.valueOf(map.get("JOB_STATUS"));
                String JOB_NOTES = String.valueOf(map.get("JOB_NOTES"));
                String PK_JOB_ID = map.get("PK_JOB_ID").toString();
                String JOB_LOCATION = String.valueOf(map.get("JOB_LOCATION"));
                String JOB_Satff = String.valueOf(map.get("STAFF_DETAILS"));
                String JOB_Material = String.valueOf(map.get("JOB_MATERIAL"));
                String ImageId = (String) map.get("IMAGE_AUDIT_ID");
                String paymentDurationId = (String) map.get("PAYMENT_DURATION");
                String paymentDuration="";
                String paymentDeposit="";
                if(paymentDurationId != null && !paymentDurationId.isEmpty()) {
                    String paymentDurationQuery = "select pt.\"PAYMENT_DURATION\" " +
                            "from \"Bizfns\".\"PAYMENT_TERM\" pt " +
                            "where pt.\"PAYMENT_DURATION_ID\" = ?";
                    paymentDuration = jdbcTemplate.queryForObject(paymentDurationQuery, new Object[]{Integer.parseInt(paymentDurationId)}, String.class);
                }
                paymentDeposit = (String) map.get("PAYMENT_DEPOSIT");
                if(paymentDeposit == null){
                    paymentDeposit="";
                }
                try {
                    scheduleData.put("startDate", startDate);
                    scheduleData.put("endDate", endDate);
                    scheduleData.put("JOB_START_TIME", formattedStartTime.toUpperCase());
                    scheduleData.put("JOB_END_TIME", formattedEndTime.toUpperCase());
                    scheduleData.put("JOB_STATUS", JOB_STATUS);
                    scheduleData.put("JOB_NOTES", JOB_NOTES);
                    scheduleData.put("PK_JOB_ID", PK_JOB_ID);
                    scheduleData.put("JOB_LOCATION", JOB_LOCATION);
                    scheduleData.put("ImageId", ImageId);
                    scheduleData.put("paymentDuration", paymentDurationId);
                    scheduleData.put("paymentDeposit", paymentDeposit);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                if (isNullOrEmpty(JOB_Satff)) {
                    scheduleData.put("JOB_STAFF", new ArrayList<>());
                } else {
                    ArrayList<Integer> staffList = Arrays.stream(JOB_Satff.split(","))
                            .map(String::trim).map(Integer::parseInt) // Trim each element to remove leading/trailing whitespace
                            .collect(Collectors.toCollection(ArrayList::new));
                    try {
                        String staffQuery = "select cu.\"PK_USER_ID\", cu.\"USER_FIRST_NAME\", cu.\"USER_LAST_NAME\" " +
                                "from \"" + tenantId + "\".company_user cu " +
                                "where cu.\"PK_USER_ID\" in (:customerIds)";
                        Map<String, List<Integer>> paramMap = Collections.singletonMap("customerIds", staffList);
                        List<Map<String, Object>> staffLists = namedParameterJdbcTemplate.queryForList(staffQuery, paramMap);
                        scheduleData.put("StaffList", staffLists);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                if (isNullOrEmpty(JOB_Material)) {
                    scheduleData.put("JOB_MATERIAL", new ArrayList<>());
                } else {
                    ArrayList<Integer> materialList = Arrays.stream(JOB_Material.split(","))
                            .map(String::trim)
                            .map(Integer::parseInt)
                            .collect(Collectors.toCollection(ArrayList::new));
                    try {
                        String materialQuery = "SELECT mm.\"PK_MATERIAL_ID\", mm.\"MATERIAL_NAME\", mm.\"RATE\", mrm.\"MATERIAL_UNIT_NAME\" " +
                                "FROM \"" + tenantId + "\".material_master mm " +
                                "JOIN \"" + tenantId + "\".material_rate_master mrm ON mm.\"FK_MATERIAL_UNIT_ID\" = mrm.\"PK_MATERIAL_UNIT_ID\" " +
                                "WHERE mm.\"PK_MATERIAL_ID\" IN (:materialIds)";
                        Map<String, List<Integer>> paramMap = Collections.singletonMap("materialIds", materialList);
                        List<Map<String, Object>> materialDetails = namedParameterJdbcTemplate.queryForList(materialQuery, paramMap);
                        scheduleData.put("JOB_MATERIAL", materialDetails);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                String customerAndServiceQuery = "select * from \"" + tenantId + "\".job_wise_customer_and_serviceentity_mapping jwcasm where jwcasm.\"PK_JOB_ID\" = ?";
                List<Map<String, Object>> Job_customer = jdbcTemplate.queryForList(customerAndServiceQuery, Integer.parseInt(PK_JOB_ID));
                if (Job_customer.isEmpty()) {
                    scheduleData.put("customerList", new ArrayList<>());
                } else {
                    try {
                        ArrayList<Map<String, Object>> customers = new ArrayList<>();
                        for (Map<String, Object> customerMappedData : Job_customer) {
                            String customerId = String.valueOf(customerMappedData.get("PK_CUSTOMER_ID"));
                            String serviceEntity_arr = (String) customerMappedData.get("PK_ENTITY_ID");
                            String customerDetailsQuery = "select cc.\"CUSTOMER_FIRST_NAME\", cc.\"CUSTOMER_LAST_NAME\", cc.\"PK_CUSTOMER_ID\" " +
                                    "from \"" + tenantId + "\".company_customer cc " +
                                    "where cc.\"PK_CUSTOMER_ID\" = ?";
                            List<Map<String, Object>> customerDetails = jdbcTemplate.queryForList(customerDetailsQuery, Integer.parseInt(customerId));
                            Map<String, Object> customerList = customerDetails.get(0);
                            ArrayList<Integer> serviceEntityList = Arrays.stream(serviceEntity_arr.split(","))
                                    .map(String::trim).map(Integer::parseInt) // Trim each element to remove leading/trailing whitespace
                                    .collect(Collectors.toCollection(ArrayList::new));
                            String serviceEntityDetailsQuery = "select csem.\"SERVICE_ENTITY_NAME\",csem.\"PK_SERVICE_ENTITY\" " +
                                    "from \"" + tenantId + "\".customer_service_entity_mapping csem " +
                                    "where csem.\"PK_SERVICE_ENTITY\" IN (:serviceEntityIds)";
                            Map<String, List<Integer>> paramMap = Collections.singletonMap("serviceEntityIds", serviceEntityList);
                            List<Map<String, Object>> entityList = namedParameterJdbcTemplate.queryForList(serviceEntityDetailsQuery, paramMap);
                            customerList.put("ServiceEntityList", entityList);
                            customers.add(customerList);
                            System.err.println(customerList);
                            scheduleData.put("CustomersList", customers);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                //forfetchingService-Amit
                String serviceQuery = "SELECT btwsm.\"ID\", btwsm.\"SERVICE_NAME\", btwsm.\"RATE\", srum.\"RATE_UNIT_NAME\" " +
                        "FROM \"" + tenantId + "\".business_type_wise_service_master btwsm " +
                        "JOIN \"Bizfns\".\"SERVICE_RATE_UNIT_MASTER\" srum " +
                        "ON btwsm.\"RATE_UNIT\" = srum.\"ID\" " +
                        "WHERE btwsm.\"ID\" IN ( " +
                        "    SELECT CAST(value AS INTEGER) AS SERVICE_ID " +
                        "    FROM \"" + tenantId + "\".job_wise_service_mapping jwsm, " +
                        "         UNNEST(STRING_TO_ARRAY(jwsm.\"SERVICE_ID\", ',')) AS value " +
                        "    WHERE jwsm.\"JOB_ID\" = ? " +
                        ")";
                List<Map<String, Object>> serviceList = jdbcTemplate.queryForList(serviceQuery, PK_JOB_ID);
                scheduleData.put("serviceList", serviceList);
                String invoiceQuery = " select im.\"CUSTOMER_ID\" ,im.\"INVOICE_MASTER_NAME\",im.\"INVOICE_ID\" from\"" + tenantId + "\".invoice_master im where im.\"PK_JOB_ID\" = ? ";
                List<Map<String, Object>> invoiceList = jdbcTemplate.queryForList(invoiceQuery, Integer.parseInt(PK_JOB_ID));
                if (invoiceList.isEmpty()) {
                    scheduleData.put("invoiceList", new ArrayList<>());
                } else {
                    scheduleData.put("invoiceList", invoiceList);
                }

                String fetchCustomerIdsSql = "SELECT STRING_AGG(\"CUSTOMER_ID\"::TEXT, ',') AS customer_ids " +
                        "FROM \"" + tenantId + "\".\"invoice\" " +
                        "WHERE \"JOB_ID\" = ?";
                String customerInvCreatedJobIds = jdbcTemplate.queryForObject(fetchCustomerIdsSql, new Object[]{Integer.parseInt(PK_JOB_ID)}, String.class);
                scheduleData.put("custInvoiceCreatedIds",customerInvCreatedJobIds);
                /*List<Map<String, Object>> mediaNameList = scheduleQuery.getMediaNameList(tenantId, Integer.parseInt(PK_JOB_ID));
                if (mediaNameList.isEmpty()) {
                    scheduleData.put("imageList", new ArrayList<>());
                } else {
                    scheduleData.put("imageList", mediaNameList);
                }*/
                Set<Map<String, Object>> allMediaNameSet = new HashSet<>();
                String[] ids = ImageId.split(",");
                for (String id : ids) {
                    List<Map<String, Object>> mediaNameList = scheduleQuery.getMediaNameListAsPerImageId(tenantId, id);
                    allMediaNameSet.addAll(mediaNameList);
                }
                List<Map<String, Object>> allMediaNameList = new ArrayList<>(allMediaNameSet);
                scheduleData.put("imageList", allMediaNameList);
                collect = transformedData.stream().map(maps -> {
                    if (isWithinInterval((String) maps.get("time"), formattedStartTime.toUpperCase(), intervalTime)) {
                        ((List<Map<String, Object>>) maps.get("schedule")).add(scheduleData);
                    }
                    return maps;
                }).collect(Collectors.toList());
            }
            return collect;
        }
    }

    private boolean isWithinInterval(String mapTime1, String jobStartTime1, Double intervalHours) {
        DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm:ss a", Locale.ENGLISH);
        LocalTime mapTime = LocalTime.parse(mapTime1, TIME_FORMATTER);
        LocalTime jobStartTime = LocalTime.parse(jobStartTime1, TIME_FORMATTER);
        int hours = intervalHours.intValue();
        int minutes = (int) ((intervalHours - hours) * 60);
        LocalTime endTimeOfInterval = jobStartTime.plusHours(hours).plusMinutes(minutes);
        return !mapTime.isBefore(jobStartTime) && mapTime.isBefore(endTimeOfInterval);
        /*LocalTime mapTime = LocalTime.parse(mapTime1);
        LocalTime jobStartTime = LocalTime.parse(jobStartTime1);
        int hours = intervalHours.intValue();
        int minutes = (int) ((intervalHours - hours) * 60);
        LocalTime endTimeOfInterval = jobStartTime.plusHours(hours).plusMinutes(minutes);
        return !mapTime.isBefore(jobStartTime) && mapTime.isBefore(endTimeOfInterval);*/
    }

    /* private List<Map<String, Object>> transformData(List<Map<String, Object>> rawData,String tenantId) {
           List<Map<String, Object>> transformedData = new ArrayList<>();

           for (int hour = 7; hour <= 20; hour++) {
               String startTime = String.format("%02d:00:00", hour);

               Map<String, Object> timeSlot = new HashMap<>();
               timeSlot.put("time", startTime);
               timeSlot.put("schedule", new ArrayList<>()); // Initialize as an empty list
               transformedData.add(timeSlot);
           }

           // If rawData is empty, return the empty time slots
           if (rawData.isEmpty()) {
               Map<String, Object> response = new HashMap<>();
               response.put("date", ""); // Set the date as needed
               response.put("schedule_list", transformedData);
               return Collections.singletonList(response);
           }
           String customerFName;
           String customerLName;
           // Map the fetched data to the time slots and group by "job_id"
           Map<String, List<Map<String, Object>>> scheduleMap = new HashMap<>();
           for (Map<String, Object> rawEntry : rawData) {
               String startTime = rawEntry.get("startTime").toString();
               String jobId = rawEntry.get("FK_JOB_ID").toString();
               String customerId = rawEntry.get("FK_CUSTOMER_ID").toString();
               if(isNullOrEmpty((String)rawEntry.get("CUSTOMER_FIRST_NAME"))){
                   customerFName="";
               }else {
                   customerFName=(String)rawEntry.get("CUSTOMER_FIRST_NAME");
               }
               if(isNullOrEmpty((String)rawEntry.get("CUSTOMER_LAST_NAME"))){
                   customerLName="";
               }else {
                   customerLName=(String)rawEntry.get("CUSTOMER_LAST_NAME");
               }
            //   String customerLName = rawEntry.get("CUSTOMER_LAST_NAME").toString();
               String staffFirstName=rawEntry.get("USER_FIRST_NAME").toString();
               String stafflastName=rawEntry.get("USER_LAST_NAME").toString();
               String jobstatus = String.valueOf(rawEntry.get("JOB_STATUS"));
               String serviceIds = rawEntry.get("SERVICE_ID").toString();


               ArrayList<Integer> ServiceArrayList = Arrays.stream(serviceIds.split(","))
                       .map(Integer::parseInt)
                       .collect(Collectors.toCollection(ArrayList::new));

               String matId = rawEntry.get("JOB_MATERIAL").toString();
               String start_time = rawEntry.get("JOB_START_TIME").toString();
               String end_time = rawEntry.get("JOB_END_TIME").toString();

               if (!scheduleMap.containsKey(startTime)) {
                   scheduleMap.put(startTime, new ArrayList<>());
               }

               // Check if a schedule entry with the same "job_id" already exists
               boolean found = false;
       for (Map<String, Object> scheduleEntry : scheduleMap.get(startTime)) {
                   if (scheduleEntry.get("job_id").equals(jobId)) {
                       found = true;

                       // Check if the staff entry with the same "staff_id" already exists
                       List<Map<String, Object>> staffList = (List<Map<String, Object>>) scheduleEntry.get("staff");
                       boolean staffFound = false;
                       for (Map<String, Object> staff : staffList) {
                           if (staff.get("staff_id").equals(rawEntry.get("FK_USER_ID").toString())) {
                               staffFound = true;
                               break;
                           }
                       }

                       if (!staffFound) {
                           Map<String, Object> staff = new HashMap<>();
                           staff.put("staff_id", rawEntry.get("FK_USER_ID").toString());
                          staff.put("staff_name",staffFirstName +" "+stafflastName);

                           // Add staff details like staff_name if needed
                           staffList.add(staff);
                       }

                       // Check if the customer entry with the same "customer_id" already exists
                       List<Map<String, Object>> customerList = (List<Map<String, Object>>) scheduleEntry.get("customer");
                       boolean customerFound = false;
                       for (Map<String, Object> customer : customerList) {
                           if (customer.get("customer_id").equals(customerId)) {
                               customerFound = true;
                               break;
                           }
                       }

                       if (!customerFound) {
                           Map<String, Object> customer = new HashMap<>();
                           customer.put("customer_id", customerId);
                           customer.put("customer_Name", customerFName + " " + customerLName);
                           // customer.put("customer_L_name", customerLName);
                           // Add customer details like customer_name if needed
                           customerList.add(customer);
                       }

                       break;
                   }
               }



               if (!found) {
            ArrayList<Map<String,Object>> ServiceList=new ArrayList<>();
             for (Integer service: ServiceArrayList){
                   HashMap<String,String> serviceMap=new HashMap<>();
                   serviceMap.put("serviceId",service.toString());
                 String sql = "SELECT \"SERVICE_NAME\" FROM " + tenantId + ".\"business_type_wise_service_master\" WHERE \"ID\" = " + service;

                   List<Map<String, Object>> serviceName = jdbcTemplate.queryForList(sql);


                   if (serviceName.size() == 0){
                       ServiceList=null;
                   }else {
                       Map<String, Object> serviceMaps= serviceName.get(0);
                       serviceMaps.put("serviceId",service.toString());
                       ServiceList.add(serviceMaps);
                   }
             }
                                 //change for this line
                   String serviceEntityId = "select csem.\"PK_SERVICE_ENTITY\" from "+tenantId+".customer_service_entity_mapping csem where csem.\"FK_CUSTOMER_ID\"= ? ";

                   List<Map<String, Object>>  serviceName = jdbcTemplate.queryForList(serviceEntityId,Integer.parseInt(customerId));
                   Map<String, Object> servicemacustp=serviceName.get(0);


                   // Create a new schedule entry
                   Map<String, Object> scheduleEntry = new HashMap<>();
                   scheduleEntry.put("job_id", jobId);
                   scheduleEntry.put("service_entity_id",servicemacustp.get("PK_SERVICE_ENTITY").toString()); // Add the job_no if available
                   scheduleEntry.put("matId", matId); // Add the job_no if available
                   scheduleEntry.put("serviceId", ServiceList); // Add the job_no if available
                   scheduleEntry.put("start_time", start_time); // Add the job_no if available
                   scheduleEntry.put("end_time", end_time); // Add the job_no if available
                   scheduleEntry.put("job_status", jobstatus);
                   List<Map<String, Object>> staffList = new ArrayList<>();
                   Map<String, Object> staff = new HashMap<>();
                   staff.put("staff_id", rawEntry.get("FK_USER_ID").toString());
                   staff.put("staff_name",staffFirstName +" "+stafflastName);
                   // Add staff details like staff_name if needed
                   staffList.add(staff);
                   scheduleEntry.put("staff", staffList);
                   List<Map<String, Object>> customerList = new ArrayList<>();
                   Map<String, Object> customer = new HashMap<>();
                   customer.put("customer_id", customerId);
                   customer.put("customer_Name", customerFName + " " + customerLName);
                   //   customer.put("customer_L_name", customerLName);
                   // Add customer details like customer_name if needed
                   customerList.add(customer);
                   scheduleEntry.put("customer", customerList);

                   scheduleMap.get(startTime).add(scheduleEntry);
               }
           }

           // Replace the empty time slots with the corresponding schedule data
           for (Map<String, Object> timeSlot : transformedData) {
               String startTime = timeSlot.get("time").toString();
               if (scheduleMap.containsKey(startTime)) {
                   timeSlot.put("schedule", scheduleMap.get(startTime));
               }
           }

           Map<String, Object> response = new HashMap<>();
           response.put("date", ""); // Set the date as needed
           response.put("schedule_list", transformedData);

           return Collections.singletonList(response);
       }

   */
    @Override
    public ResponseEntity<GlobalResponseDTO> testErrorLog(Map<String, String> request) {
        String fullURL = httpServletRequest.getRequestURL().toString();


        String deviceId = request.get("deviceId");
        String deviceType = request.get("deviceType");
        String appVersion = request.get("appVersion");


//        String deviceInfo = "deviceId = " + deviceId + "\n" + "deviceType = " + deviceType + "\n" + "appVersion = " + appVersion;
//
//        // Now you can use 'currentMappingPath' and 'fullURL' as needed
//        System.out.println("Full URL: " + fullURL);
//        try {
//
//            String a = null;
//            if (a.equalsIgnoreCase("s")) {
//
//            }
//        } catch (Exception e) {
//            System.out.println("11");
//            errorLogService.errorLog(request, e.getMessage(), fullURL, "a", deviceInfo);
//
//        }

//        String businessName = request.get("businessName");
//
//        String trimBusinessName = businessName.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
//        // Generate a unique tenant ID based on the business name and a random number
//        String firstFourDigitsBusinessName = trimBusinessName.substring(0, 4);
//        Random random = new Random();
//        int randomNumber = random.nextInt(9000) + 1000;
//        String randomNumberString = String.valueOf(randomNumber);
//        String tenantId = firstFourDigitsBusinessName + randomNumberString;
//
//        System.out.println("======");
//        System.out.println(businessName);
//        System.out.println(trimBusinessName);
//        System.out.println(tenantId);
//
//        System.out.println("======");

        return null;
    }

    /**
     * AMIT KUMAR SINGH
     * This method fetches schedule history data i.e schedule/job details, service details, and question entity details associated with the given tenantId
     * and optionally filtered by fromDate and toDate.
     * @param request   A map containing the request parameters including deviceId, deviceType,
     *                  appVersion, userId, tenantId, fromDate, and toDate.
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> scheduleHistory(Map<String, String> request, Principal principal) {
        // Extract request parameters
        String deviceId = request.get("deviceId");
        String deviceType = request.get("deviceType");
        String appVersion = request.get("appVersion");
        String userId = request.get("userId");
        String tenantId = request.get("tenantId");

        if (checkUserMatch(userId, tenantId, principal.getName())) {
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorised user, we could not access the APIs from others token "));
        }

        String fromDate = request.get("fromDate");
        String toDate = request.get("toDate");

        String deviceInfo = "deviceId = " + deviceId + "\n" + "deviceType = " + deviceType + "\n" + "appVersion = " + appVersion;


        List<Map<String, Object>> dataForJobAndCustDetails = scheduleQuery.dataForJobAndCustDetails(tenantId, fromDate, toDate);
        List<Map<String, Object>> dataServiceDetails = scheduleQuery.dataServiceDetails(tenantId);
        List<Map<String, Object>> questionEntityDetails = scheduleQuery.questionEntityDetails(tenantId);

        System.out.println(dataForJobAndCustDetails);
        System.out.println(dataServiceDetails);
        System.out.println(questionEntityDetails);


        List<Map<String, Object>> serviceEntities = new ArrayList<>();
        List<Map<String, Object>> customerData = new ArrayList<>();


        for (Map<String, Object> jobAndCustDetail : dataForJobAndCustDetails) {
            System.out.println(" 1. Processing jobAndCustDetail with jobId: " + jobAndCustDetail.get("jobId"));

            Map<String, Object> serviceData = new HashMap<>();
            serviceData.put("jobId", jobAndCustDetail.get("jobId"));
            serviceData.put("assignJobId", jobAndCustDetail.get("assignJobId"));
            serviceData.put("startDate", jobAndCustDetail.get("startDate"));
            serviceData.put("endDate", jobAndCustDetail.get("endDate"));
            serviceData.put("customerId", jobAndCustDetail.get("customerId"));
            serviceData.put("CUSTOMER_FIRST_NAME", jobAndCustDetail.get("CUSTOMER_FIRST_NAME"));
            serviceData.put("CUSTOMER_LAST_NAME", jobAndCustDetail.get("CUSTOMER_LAST_NAME"));

            List<Map<String, Object>> serviceList = new ArrayList<>();
            for (Map<String, Object> serviceDetail : dataServiceDetails) {
                String jobIdFromService = (String) serviceDetail.get("jobId");
                Integer _jobIdFromService = Integer.valueOf(jobIdFromService);

                System.out.println("====jobIdFromService" + serviceDetail.get("jobId"));
                System.out.println(" 2. Comparing jobIdFromService: " + jobIdFromService + " with jobId: " + jobAndCustDetail.get("jobId"));
                Integer getJobId = (Integer) jobAndCustDetail.get("jobId");
                String strGetJobId = getJobId.toString();
                if (jobIdFromService.equals(getJobId.toString())) {

                    System.out.println(" 21. Comparing jobIdFromService: ");
                }


                if (jobIdFromService != null && jobIdFromService.equals(strGetJobId)) {

                    System.out.println("11111");
                    System.out.println(jobIdFromService);
                    System.out.println(jobAndCustDetail.get("jobId"));
                    System.out.println("11111");

                    //   if (jobIdFromService != null && "1".equals("1")) {
                    System.out.println("sayan");

                    Map<String, Object> service = new HashMap<>();
                    service.put("serviceId", serviceDetail.get("serviceId"));
                    service.put("serviceName", serviceDetail.get("serviceName"));
                    service.put("rate", serviceDetail.get("rate"));
                    serviceList.add(service);
                }
            }
            serviceData.put("serviceName", serviceList);

            List<Map<String, Object>> entityList = new ArrayList<>();
            for (Map<String, Object> questionEntityDetail : questionEntityDetails) {
                System.out.println("serviceEntity1");

                String jobIdFromEntity = (String) questionEntityDetail.get("JOB_ID");
                System.out.println(questionEntityDetail.get("JOB_ID"));

                System.out.println(" 3. Comparing jobIdFromEntity: " + jobIdFromEntity + " with jobId: " + jobAndCustDetail.get("jobId"));


                if (jobIdFromEntity.equals(jobAndCustDetail.get("jobId").toString())) {
                    System.out.println(" 31. Comparing jobIdFromEntity: ");
                }

                if (jobIdFromEntity != null && jobIdFromEntity.equals(jobAndCustDetail.get("jobId").toString())) {

                    System.out.println("22222222");
                    System.out.println(jobIdFromEntity);
                    System.out.println(jobAndCustDetail.get("jobId"));
                    System.out.println("2222222");

                    // if (jobIdFromEntity != null && "1".equals("1")) {
                    System.out.println("serviceEntity2");

                    Map<String, Object> entity = new HashMap<>();
                    entity.put("entityId", questionEntityDetail.get("PK_FORM_KEY_ID"));
                    entity.put("entityName", questionEntityDetail.get("INPUT_KEY"));
                    entity.put("answerType", questionEntityDetail.get("ANSWER_TYPE"));
                    entity.put("answer", questionEntityDetail.get("ANSWER"));
                    entity.put("options", questionEntityDetail.get("OPTIONS"));
                    entityList.add(entity);
                }
            }
            serviceData.put("serviceEntity", entityList);

            customerData.add(serviceData);
        }


        return ResponseEntity.accepted().body(new GlobalResponseDTO(true, "Success", customerData));
    }

    /*
     * AMIT KUMAR SINGH
     * This method is used to reschedule a job on the reschedule date identified by jobId for the given userId and tenantId.
     * It updates the job's date, start time, and end time based on the provided request parameters.
     * Verifies if the requesting user matches the userId and tenantId associated with the principal.
     * If not, returns a bad request response indicating unauthorized access.
     * @param request   A map containing the request parameters including deviceId, deviceType,
     *                  appVersion, userId, tenantId, jobId, reScheduleDate, startTime, and endTime.
     */
    @Override
    @CacheEvict(value = "scheduleList", allEntries = true)
    public ResponseEntity<GlobalResponseDTO> reScheduleJob(Map<String, String> request, Principal principal) {
        String deviceId = request.get("deviceId");
        String deviceType = request.get("deviceType");
        String appVersion = request.get("appVersion");
        String userId = request.get("userId");
        String tenantId = request.get("tenantId");
        String jobId = request.get("jobId");
        String reScheduleDate = request.get("reScheduleDate");
        String starttime = request.get("startTime");
        String endtime = request.get("endTime");
        String date = request.get("date");
        if (checkUserMatch(userId, tenantId, principal.getName())) {
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorised user, we could not access the APIs from others token "));
        }
        String deviceInfo = "deviceId = " + deviceId + "\n" + "deviceType = " + deviceType + "\n" + "appVersion = " + appVersion;
        String fullURL = httpServletRequest.getRequestURL().toString();
        try {
            scheduleQuery.updateReScheduleJob(userId, tenantId, jobId, reScheduleDate, starttime, endtime);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            errorLogService.errorLog(request, e.getMessage(), fullURL, userId, deviceInfo);
            return ResponseEntity.accepted().body(new GlobalResponseDTO(false, "Success", null));
        }
        return ResponseEntity.accepted().body(new GlobalResponseDTO(true, "Success", null));
    }

    /*
     * AMIT KUMAR SINGH
     * This method retrieves the list of material units based on the given tenantId and category_id.
     * Verifies if the requesting user matches the userId and tenantId associated with the principal.
     * If not, returns a bad request response indicating unauthorized access.
     * @param request userId.
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> getMaterialUnit(Map<String, String> request, Principal principal) {
        String tenantId = request.get("tenantId");
        String deviceId = request.get("deviceId");
        String deviceType = request.get("deviceType");
        String appVersion = request.get("appVersion");
        String userId = request.get("userId");
        if (checkUserMatch(userId, tenantId, principal.getName())) {
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorised user, we could not access the APIs from others token "));
        }
        String deviceInfo = "deviceId = " + deviceId + "\n" + "deviceType = " + deviceType + "\n" + "appVersion = " + appVersion;
        String fullURL = httpServletRequest.getRequestURL().toString();
        List<Map<String, Object>> getMatUnit;
        try {
            getMatUnit = scheduleQuery.getMatUnit(tenantId);
        } catch (Exception e) {
            errorLogService.errorLog(request, e.getMessage(), fullURL, userId, deviceInfo);
            return ResponseEntity.accepted().body(new GlobalResponseDTO(false, "Success", null));
        }
        return ResponseEntity.accepted().body(new GlobalResponseDTO(true, "Success", getMatUnit));
    }

    @Override
    public ResponseEntity<GlobalResponseDTO> addScheduleNew(Map<String, Object> request) {


        // Extract the values from the request
        String deviceId = (String) request.get("deviceId");
        String deviceType = (String) request.get("deviceType");
        String appVersion = (String) request.get("appVersion");
        String tenantId = (String) request.get("tenantId");
        Integer jobId = (Integer) request.get("job_id");
        String startDate = (String) request.get("start_date");
        String startTime = (String) request.get("start_time");
        String endDate = (String) request.get("end_date");
        String endTime = (String) request.get("end_time");
        List<Map<String, String>> customerList = (List<Map<String, String>>) request.get("customer_list");
        List<Map<String, String>> serviceList = (List<Map<String, String>>) request.get("service_list");
        List<Map<String, String>> materialList = (List<Map<String, String>>) request.get("material_list");
        List<Map<String, String>> staffList = (List<Map<String, String>>) request.get("staff_list");
        String note = (String) request.get("note");

        for (Map<String, String> customer : customerList) {
        }

        for (Map<String, String> service : serviceList) {
            System.out.println("  Service ID: " + service.get("service_id"));
        }

        for (Map<String, String> material : materialList) {
        }

        for (Map<String, String> staff : staffList) {
        }


        return ResponseEntity.accepted().body(new GlobalResponseDTO(true, "Success", request));
    }

    private static String convertStringArrayToString(ArrayList<String> strArr, String delimiter) {
        StringBuilder sb = new StringBuilder();
        for (String str : strArr)
            sb.append(str).append(delimiter);
        return sb.substring(0, sb.length() - 1);
    }

    /*
     * AMIT KUMAR SINGH
     * This method handles the editing of a job schedule based on the provided parameters and updates the job based on job_id.
     * Verifies if the requesting user matches the userId and tenantId associated with the principal.
     * If not, returns a bad request response indicating unauthorized access.
     * @param request   jobDetails (containing detailed job information).

     */
    @Override
    @CacheEvict(value = "scheduleList", allEntries = true)
    public ResponseEntity<GlobalResponseDTO> editSchedule(Map<String, Object> request, Principal principal) {

        String deviceId = (String) request.get("deviceId");
        String deviceType = (String) request.get("deviceType");
        String appVersion = (String) request.get("appVersion");
        String userId = (String) request.get("userId");
        String tenantId = (String) request.get("tenantId");

        if (checkUserMatch(userId, tenantId, principal.getName())) {
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorised user, we could not access the APIs from others token "));
        }

//        if (!scheduleQuery.existUserId(tenantId).equals(userId)){
//            return ResponseEntity.accepted()
//                    .body(new GlobalResponseDTO(false, "The userId not matched with this tenantId or User Not Found", null));
//
//        }

        String job_id = (String) request.get("job_id");
        System.out.println("jobId = " + job_id);
        Map<String, Object> jobDetails = (Map<String, Object>) request.get("jobDetails");
        String startDate = (String) jobDetails.get("startDate");
        String startTime = (String) jobDetails.get("startTime");
        String endDate = (String) jobDetails.get("endDate");
        String endTime = (String) jobDetails.get("endTime");
        String DurationOfrecurr = (String) jobDetails.get("DurationOfrecurr");
        String Numberofrecurr = (String) jobDetails.get("Numberofrecurr");
        String recurrType = (String) jobDetails.get("recurrType");
        String materialsId = (String) jobDetails.get("materials");
        String jobNote = (String) jobDetails.get("note");
        String jobLocation = (String) jobDetails.get("joblocation");
        String imageId = (String) jobDetails.get("imageId");
        String jobNotes = jobDetails.get("note").toString();
        String jobStropOn = (String) jobDetails.get("jobstopdate");
        String jobStatus = (String) jobDetails.get("jobstatus");
        String ImageId = jobDetails.get("imageId").toString();
        String uniqueImageIds = generateUniqueImageIds(ImageId);
        String paymentDuration = (String) jobDetails.get("paymentDuration");
        if (paymentDuration == null) {
            paymentDuration = "";
        }
        String paymentDeposit = (String) jobDetails.get("paymentDeposit");
        if (paymentDeposit == null) {
            paymentDeposit = "";
        }
        List<Map<String, Object>> staffList = (List<Map<String, Object>>) jobDetails.get("staffList");

        /*if(scheduleQuery.pastJobDataChk(Integer.parseInt(job_id),tenantId)){
            return ResponseEntity.badRequest()
                    .body(new GlobalResponseDTO(false, "You cannot edit the past dated jobs.", null));
        }*/

        String[] eligibleImageId = uniqueImageIds.split(",");
        StringBuilder imageListFinal = new StringBuilder();
        for (String id : eligibleImageId) {
            List<Map<String, Object>> fileNameAsPerAuditID = scheduleQuery.getMediaNameList(id, tenantId);
            if (!fileNameAsPerAuditID.isEmpty()) {
                imageListFinal.append(id).append(',');
            }
        }

        if (imageListFinal.length() > 0) {
            imageListFinal.deleteCharAt(imageListFinal.length() - 1);
        }

        String imgListFnl = imageListFinal.toString();


        if (!scheduleQuery.existJobId(Integer.parseInt(job_id), tenantId)) {
            return ResponseEntity.badRequest()
                    .body(new GlobalResponseDTO(false, "JOB_ID not found", null));

        }

        int year1 = Integer.parseInt(startDate.split("-")[0]);
        int Month1 = Integer.parseInt(startDate.split("-")[1]);
        int day1 = Integer.parseInt(startDate.split("-")[2]);
        int hour1 = Integer.parseInt(startTime.split(":")[0]);
        int minutes1 = Integer.parseInt(startTime.split(":")[1]);
        LocalDate initialDateTime1 = LocalDate.of(year1, Month1, day1);
        int year_end1 = Integer.parseInt(endDate.split("-")[0]);
        int Month_end1 = Integer.parseInt(endDate.split("-")[1]);
        int day_end1 = Integer.parseInt(endDate.split("-")[2]);
        int hour_end1 = Integer.parseInt(endTime.split(":")[0]);
        int minutes_end1 = Integer.parseInt(endTime.split(":")[1]);
        // LocalDate endinitialDateTime1 = LocalDate.of(year_end1, Month_end1, day_end1);
        LocalDate currentStartTimeCheck = initialDateTime1;
        //LocalDate currentEndTime1 = endinitialDateTime1;
        //DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        // String StartTime_recurr1 = hour1 + ":" + minutes1;
        //String EndTime_recurr1 = hour_end1 + ":" + minutes_end1;
        //LocalDate StartDate_recurr_present1 = (LocalDate.of(currentStartTime1.getYear(), currentStartTime1.getMonthValue(), currentStartTime1.getDayOfMonth()));
        //LocalDate EndDate_recurr_present1 = (LocalDate.of(currentEndTime1.getYear(), currentEndTime1.getMonthValue(), currentEndTime1.getDayOfMonth()));
        //int numberofRecurr1;
        int numberofRecurr1;
        if (!jobStropOn.isEmpty()) {
            LocalDate stopJob_recurr = LocalDate.parse(jobStropOn);
            numberofRecurr1 = countOccurrences(initialDateTime1, stopJob_recurr, recurrType);
            // System.err.println(numberofRecurr);

        } else {
            numberofRecurr1 = Integer.parseInt(Numberofrecurr);
        }


        for (int i = 1; i < numberofRecurr1; i++) {

            List<Map<String, Object>> staffList1 = (List<Map<String, Object>>) jobDetails.get("staffList");
            if (!staffList1.isEmpty()) {
                for (Map<String, Object> staff : staffList1) {
                    String staffId = (String) staff.get("staffId");
                    List<Map<String, Object>> getJobAvailabilityData = scheduleQuery.jobDataAvailabilityCheck(tenantId, staffId, currentStartTimeCheck.toString(), startTime, endTime);

//                    if (!getJobAvailabilityData.isEmpty()){
//                        return ResponseEntity.accepted()
//                                .body(new GlobalResponseDTO(false, "The staff is already occupied. Please look for other staff", null));
//
//                    }
                }
            }
            currentStartTimeCheck = currentStartTimeCheck.plusDays(Integer.parseInt(DurationOfrecurr));
        }

        List<Map<String, Object>> currentImageId = scheduleQuery.currentImageIdList(tenantId,job_id);

        StringBuilder staffBuilder = new StringBuilder();
        for (Map<String, Object> staff : staffList) {
            String staffId = (String) staff.get("staffId");
            staffBuilder.append(staffId).append(",");

        }
        if (!staffBuilder.isEmpty()) {
            staffBuilder.deleteCharAt(staffBuilder.length() - 1);
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
        Map<String, Object> service = (Map<String, Object>) jobDetails.get("service");
        String serviceId = (String) service.get("serviceId");
        if (serviceId == null || serviceId.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new GlobalResponseDTO(false, "Please provide service name", null));
        } else {
            scheduleQuery.updateService(tenantId, job_id, serviceId);
        }
        scheduleQuery.updatejobDetails(tenantId, job_id, String.valueOf(jobMaterial), jobNote, jobLocation, String.valueOf(staffBuilder), startDate, startTime, endDate, endTime,paymentDuration,paymentDeposit,imgListFnl);

        ArrayList<Map<String, Object>> customer = (ArrayList<Map<String, Object>>) jobDetails.get("customer");
        if (customer.size() != 0) {
            scheduleQuery.updateCustEntity(tenantId, job_id);
            for (Map<String, Object> customers : customer) {
                String customerId = (String) customers.get("customerId");
                ArrayList<String> serviceEntityId = (ArrayList<String>) customers.get("serviceEntityId");
                String entityId = convertStringArrayToString(serviceEntityId, ",");
                scheduleQuery.updateCust(tenantId, job_id, customerId, entityId);
            }
        } else {
            scheduleQuery.updateCustEntity(tenantId, job_id);
        }
        /*Map<String, Object> service = (Map<String, Object>) jobDetails.get("service");
        String serviceId = (String) service.get("serviceId");
        String serviceName = (String) service.get("serviceName");
        String serviceRate = (String) service.get("serviceRate");
        scheduleQuery.updateService(tenantId, job_id, serviceId);*/
        String paymentModeId = (String) jobDetails.get("paymentModeId");
        String paymentTypeId = (String) jobDetails.get("paymentTypeId");
        if (staffList.size() != 0) {
            scheduleQuery.deleteAllAssignJobData(tenantId, job_id);
            for (Map<String, Object> staff : staffList) {
                String staffId = (String) staff.get("staffId");
                scheduleQuery.updateStaffDetails(tenantId, job_id, endTime, startDate, startTime, endDate, staffId);
            }
        } else {
            scheduleQuery.deleteAllAssignJobData(tenantId, job_id);
        }
        int year = Integer.parseInt(startDate.split("-")[0]);
        int Month = Integer.parseInt(startDate.split("-")[1]);
        int day = Integer.parseInt(startDate.split("-")[2]);
        int hour = Integer.parseInt(startTime.split(":")[0]);
        int minutes = Integer.parseInt(startTime.split(":")[1]);
        LocalDate initialDateTime = LocalDate.of(year, Month, day);
        int year_end = Integer.parseInt(endDate.split("-")[0]);
        int Month_end = Integer.parseInt(endDate.split("-")[1]);
        int day_end = Integer.parseInt(endDate.split("-")[2]);
        int hour_end = Integer.parseInt(endTime.split(":")[0]);
        int minutes_end = Integer.parseInt(endTime.split(":")[1]);
        LocalDate endinitialDateTime = LocalDate.of(year_end, Month_end, day_end);
        LocalDate currentStartTime = initialDateTime;
        LocalDate currentEndTime = endinitialDateTime;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String StartTime_recurr = hour + ":" + minutes;
        String EndTime_recurr = hour_end + ":" + minutes_end;
        LocalDate StartDate_recurr_present = (LocalDate.of(currentStartTime.getYear(), currentStartTime.getMonthValue(), currentStartTime.getDayOfMonth()));
        LocalDate EndDate_recurr_present = (LocalDate.of(currentEndTime.getYear(), currentEndTime.getMonthValue(), currentEndTime.getDayOfMonth()));
        int numberofRecurr;
        if (!jobStropOn.isEmpty()) {
            LocalDate stopJob_recurr = LocalDate.parse(jobStropOn);
            numberofRecurr = countOccurrences(initialDateTime, stopJob_recurr, recurrType);
        } else {
            numberofRecurr = Integer.parseInt(Numberofrecurr);
        }
        /*List<Map<String, Object>> mediaInfo = scheduleQuery.getImageAuditID(tenantId, Integer.valueOf(job_id));
        String[] imageIds = imageId.split(",");
        int imageSize = imageIds.length;
        int mediaSize = mediaInfo.size();
        boolean isInserted = true;
        if(numberofRecurr == 0){
            int imageIdIndex = 0;
            for (Map<String, Object> record : mediaInfo) {
                Integer pkMedia = (Integer) record.get("PK_MEDIA_ID");
                scheduleQuery.deleteMedFile(tenantId, String.valueOf(pkMedia));
                if (imageIdIndex < imageIds.length) {
                    String id = imageIds[imageIdIndex].trim();
                    List<Map<String, Object>> fileNameAsPerAuditID = scheduleQuery.getMediaNameList(id, tenantId);
                    if (!fileNameAsPerAuditID.isEmpty() && !(mediaSize == 0 && imageSize > 0) && !(mediaSize == 1 && imageSize > 1) && !(mediaSize == 2 && imageSize > 2) && !(mediaSize == 3 && imageSize > 3)) {
                        Map<String, Object> fileNameRecord = fileNameAsPerAuditID.get(0);
                        String fileNameStr = (String) fileNameRecord.get("FILE_NAME");
                        LocalDateTime currentTimestamp = LocalDateTime.now();
                        String sql1 = "INSERT INTO " + tenantId + ".media (\"PK_MEDIA_ID\",\"MEDIA_MODULE_NAME\",\"FK_MODULE_PRIMARY_ID\",\"FILE_NAME\",\"UPLOAD_DATE_TIME\",\"JOB_ID\",\"IMAGE_AUDIT_ID\") VALUES (?,?,?,?,?,?,?)";
                        jdbcTemplate.update(sql1, pkMedia, "Edit Schedule", Integer.parseInt(job_id), fileNameStr, currentTimestamp, Integer.parseInt(job_id), id);
                    } else {
                        if (!fileNameAsPerAuditID.isEmpty() && isInserted) {
                            for (String indvImage : imageIds) {
                                List<Map<String, Object>> fileNameAsPerAdtID = scheduleQuery.getMediaNameList(indvImage, tenantId);
                                Map<String, Object> fileNameRecord = fileNameAsPerAdtID.get(0);
                                String fileNameStr = (String) fileNameRecord.get("FILE_NAME");
                                LocalDateTime currentTimestamp = LocalDateTime.now();
                                String sql1 = "INSERT INTO " + tenantId + ".media (\"PK_MEDIA_ID\",\"MEDIA_MODULE_NAME\",\"FK_MODULE_PRIMARY_ID\",\"FILE_NAME\",\"UPLOAD_DATE_TIME\",\"JOB_ID\",\"IMAGE_AUDIT_ID\") VALUES (?,?,?,?,?,?,?)";
                                jdbcTemplate.update(sql1, pkMedia, "Edit Schedule", Integer.parseInt(job_id), fileNameStr, currentTimestamp, Integer.parseInt(job_id), indvImage);
                                String getMaxPkMediaSql = "SELECT MAX(\"PK_MEDIA_ID\") FROM " + tenantId + ".media";
                                Integer maxPkMedia = jdbcTemplate.queryForObject(getMaxPkMediaSql, Integer.class);
                                if (maxPkMedia != null) {
                                    pkMedia = maxPkMedia + 1;
                                } else {
                                    pkMedia = 1;
                                }
                            }
                            isInserted = false;
                        }
                    }
                    imageIdIndex++;
                }
            }
            if(mediaInfo.isEmpty()){
                String getMaxPkMediaSql = "SELECT MAX(\"PK_MEDIA_ID\") FROM " + tenantId + ".media";
                Integer maxPkMedia = jdbcTemplate.queryForObject(getMaxPkMediaSql, Integer.class);
                for (String indvImage : imageIds) {
                    List<Map<String, Object>> fileNameAsPerAdtID = scheduleQuery.getMediaNameList(indvImage, tenantId);
                    Map<String, Object> fileNameRecord = fileNameAsPerAdtID.get(0);
                    String fileNameStr = (String) fileNameRecord.get("FILE_NAME");
                    LocalDateTime currentTimestamp = LocalDateTime.now();
                    String sql1 = "INSERT INTO " + tenantId + ".media (\"PK_MEDIA_ID\",\"MEDIA_MODULE_NAME\",\"FK_MODULE_PRIMARY_ID\",\"FILE_NAME\",\"UPLOAD_DATE_TIME\",\"JOB_ID\",\"IMAGE_AUDIT_ID\") VALUES (?,?,?,?,?,?,?)";
                    jdbcTemplate.update(sql1, maxPkMedia, "Edit Schedule", Integer.parseInt(job_id), fileNameStr, currentTimestamp, Integer.parseInt(job_id), indvImage);
                    if (maxPkMedia != null) {
                        maxPkMedia = maxPkMedia + 1;
                    } else {
                        maxPkMedia = 1;
                    }
                }
            }
        }*/

        if ((recurrType.equalsIgnoreCase("Day") || recurrType.equalsIgnoreCase("Week"))) {
            for (int i = 1; i < numberofRecurr; i++) {
                StringBuilder jobstaff = new StringBuilder();
                if (staffList.isEmpty()) {
                    jobstaff.append("0");
                } else {
                    for (Map<String, Object> staffName : staffList) {
                        String staffIds = (String) staffName.get("staffId");
                        jobstaff.append(staffIds).append(",");
                    }
                }
                currentStartTime = currentStartTime.plusDays(Integer.parseInt(DurationOfrecurr));
                StartDate_recurr_present = currentStartTime;
                currentEndTime = currentEndTime.plusDays(Integer.parseInt(DurationOfrecurr));
                EndDate_recurr_present = currentEndTime;
                System.err.println("staffdata" + jobstaff.deleteCharAt(jobstaff.length() - 1));
                String StartDate_recurr = StartDate_recurr_present.format(formatter);
                String EndDate_recurr = EndDate_recurr_present.format(formatter);
                String strInsertJobDetails = "INSERT INTO \"" + tenantId + "\".\"job_master\" (" +
                        "\"PK_JOB_ID\", \"FK_COMPANY_SUBSCRIPTION_ID\", \"JOB_START_TIME\", " +
                        "\"JOB_END_TIME\", \"JOB_DATE\", \"JOB_STOP_ON\", \"JOB_MATERIAL\", " +
                        "\"JOB_CREATED_AT\", \"STAFF_DETAILS\", \"JOB_STATUS\", \"JOB_NOTES\", \"JOB_LOCATION\", \"PAYMENT_DURATION\", \"IMAGE_AUDIT_ID\") " +
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
                        "? " +
                        ") RETURNING \"PK_JOB_ID\"";
                Object[] params = new Object[] {
                        tenantId,
                        StartTime_recurr,
                        EndTime_recurr,
                        LocalDate.parse(StartDate_recurr),
                        LocalDate.parse(EndDate_recurr),
                        materialsId,
                        LocalDate.parse(startDate),
                        jobstaff,
                        jobStatus,
                        jobNotes != null && !jobNotes.isEmpty() ? jobNotes : null,
                        jobLocation,
                        paymentDuration != null && !paymentDuration.isEmpty() ? paymentDuration : null,
                        imgListFnl
                };
                Integer pkJobId = jdbcTemplate.queryForObject(strInsertJobDetails, params, Integer.class);

                System.err.println(pkJobId);
                /*currentStartTime = currentStartTime.plusDays(Integer.parseInt(DurationOfrecurr));
                StartDate_recurr_present = currentStartTime;
                currentEndTime = currentEndTime.plusDays(Integer.parseInt(DurationOfrecurr));
                EndDate_recurr_present = currentEndTime;*/
                Integer assignJobId = null;
                if (!staffList.isEmpty()) {
                    for (Map<String, Object> staff : staffList) {
                        String staffId = (String) staff.get("staffId");
                        List<Map<String, Object>> getJobAvailabilityData = scheduleQuery.jobDataAvailabilityCheck(tenantId, staffId, currentStartTime.toString(), startTime, endTime);
//                        if (getJobAvailabilityData.isEmpty()) {
//                            assignJobId = scheduleQuery.insertAssignJobData(pkJobId, Integer.valueOf(0), Integer.valueOf(staffId), StartDate_recurr, tenantId, EndDate_recurr, StartTime_recurr, EndTime_recurr);
//                        } else {
//                            scheduleQuery.deleteAllAssignJobData(tenantId, String.valueOf(pkJobId));
//                            scheduleQuery.removeStaffInfoFromJobMaster(tenantId, String.valueOf(pkJobId), staffId);
//                            return ResponseEntity.accepted()
//                                    .body(new GlobalResponseDTO(false, "The staff is already occupied. Please look for other staff", null));
//                        }
                    }
                }
                if(!staffList.isEmpty()){
                    for (Map<String, Object> staff : staffList) {
                        String staffId = (String) staff.get("staffId");
                        List<Map<String, Object>> getJobAvailabilityData = scheduleQuery.jobDataAvailabilityCheck(tenantId,staffId,currentStartTime.toString(),startTime,endTime);
                        if(getJobAvailabilityData.isEmpty()){
                            assignJobId = scheduleQuery.insertAssignJobData(pkJobId, Integer.valueOf(0), Integer.valueOf(staffId), StartDate_recurr, tenantId,EndDate_recurr,StartTime_recurr,EndTime_recurr);
                        }
                    }
                }

                /*int imageIdInd = 0;
                for (Map<String, Object> record : mediaInfo) {
                    Integer pkMedia = 0;
                    String getMaxPkMediaSql = "SELECT MAX(\"PK_MEDIA_ID\") FROM " + tenantId + ".media";
                    Integer maxPkMedia = jdbcTemplate.queryForObject(getMaxPkMediaSql, Integer.class);
                    if (imageIdInd < imageIds.length) {
                        String id = imageIds[imageIdInd].trim();
                        List<Map<String, Object>> fileNameAsPerAuditID = scheduleQuery.getMediaNameList(id, tenantId);
                        if (!fileNameAsPerAuditID.isEmpty()) {
                            if (maxPkMedia != null) {
                                pkMedia = maxPkMedia + 1;
                            } else {
                                pkMedia = 1;
                            }
                            Map<String, Object> fileNameRecord = fileNameAsPerAuditID.get(0);
                            String fileNameStr = (String) fileNameRecord.get("FILE_NAME");
                            String sql1 = "INSERT INTO " + tenantId + ".media (\"PK_MEDIA_ID\",\"MEDIA_MODULE_NAME\",\"FK_MODULE_PRIMARY_ID\",\"FILE_NAME\",\"UPLOAD_DATE_TIME\",\"JOB_ID\",\"IMAGE_AUDIT_ID\") VALUES (?,?,?,?,?,?,?)";
                            jdbcTemplate.update(sql1, pkMedia, "Edit Schedule", pkJobId, fileNameStr, LocalDate.parse(StartDate_recurr), pkJobId, id);
                        }
                        imageIdInd++;
                    }
                }*/
                if (!customer.isEmpty()) {
                    for (Map<String, Object> customerList : customer) {
                        String customerId = (String) customerList.get("customerId");
                        ArrayList<String> serviceEntityId = (ArrayList<String>) customerList.get("serviceEntityId");
                        String mappedServiceEntityId = convertStringArrayToString(serviceEntityId, ",").toString();
                        scheduleQuery.insertCustomerandServiceEntityByJobId(pkJobId, Integer.parseInt(customerId), mappedServiceEntityId, tenantId);

                    }
                }
                scheduleQuery.insertServiceDetails(serviceId, String.valueOf(pkJobId), tenantId);

            }

        }
        return ResponseEntity.accepted()
                .body(new GlobalResponseDTO(true, "Successfully updated", null));
    }

    public static String generateUniqueImageIds(String ImageId) {
        Set<String> uniqueIdsSet = new LinkedHashSet<>();
        String[] ids = ImageId.split(",");
        for (String id : ids) {
            uniqueIdsSet.add(id.trim());
        }
        StringBuilder uniqueIdsBuilder = new StringBuilder();
        for (String uniqueId : uniqueIdsSet) {
            uniqueIdsBuilder.append(uniqueId).append(",");
        }
        if (uniqueIdsBuilder.length() > 0) {
            uniqueIdsBuilder.deleteCharAt(uniqueIdsBuilder.length() - 1);
        }

        return uniqueIdsBuilder.toString();
    }


    /*
     * AMIT KUMAR SINGH
     * This method provides a mechanism to manage and save working hours while considering existing
     * job schedules, ensuring data integrity and proper user authorization. Adjustments to start_time
     * and end_time are made dynamically based on existing job timings, providing a flexible and
     * responsive user experience.
     */
    @Override
    @CacheEvict(value = "scheduleList", allEntries = true)
    public ResponseEntity<GlobalResponseDTO> addWorkingHours(Map<String, Object> request, Principal principal) {

        String deviceId = (String) request.get("deviceId");
        String deviceType = (String) request.get("deviceType");
        String appVersion = (String) request.get("appVersion");
        String userId = (String) request.get("userId");
        String tenantId = (String) request.get("tenantId");
        String start_time = (String) request.get("start_time");
        String end_time = (String) request.get("end_time");
        if (checkUserMatch(userId, tenantId, principal.getName())) {
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorised user, we could not access the APIs from others token "));
        }
        String fromDate = (String) request.get("fromDate");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(fromDate, formatter);
        List<Map<String, Object>> fetchJobData = scheduleQuery.ftechScheduleDatabyDate(tenantId, fromDate);
        if (!fetchJobData.isEmpty()) {
            List<Map<String, Object>> fetchJobTimeData = scheduleQuery.fetchJobTimingAsPerJobDate(tenantId, fromDate);
            SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");
            String[] timeParts = start_time.split("\\:");
            int startTimeInt;
            if (timeParts[0].length() == 1) {
                startTimeInt = Integer.parseInt("0" + timeParts[0]);
            } else {
                startTimeInt = Integer.parseInt(timeParts[0]);
            }
            for (Map<String, Object> fetchIndvJobTimeData : fetchJobTimeData) {
                Time earliestTime = (Time) fetchIndvJobTimeData.get("earliest_time");
                Time latestTime = (Time) fetchIndvJobTimeData.get("lastest_time");
                if (earliestTime != null && latestTime != null) {
                    String startTimeWorking = timeFormatter.format(earliestTime);
                    String endTimeWorking = timeFormatter.format(latestTime);
                    String startHour = startTimeWorking.substring(0, 2);
                    String endHour = endTimeWorking.substring(0, 2);
                    if (Integer.parseInt(startHour) < startTimeInt) {
                        int difference = startTimeInt - Integer.parseInt(startHour);
                        int finalEndTime = Integer.parseInt(difference + endHour);
                        String message = scheduleQuery.saveWorkingHours(startHour, localDate, String.valueOf(finalEndTime), tenantId, userId);
                        Map<String, Object> finalResponse = new HashMap<>();
                        finalResponse.put("isScheduleBeforeTheWorkingHour", "Y");
                        return ResponseEntity.accepted()
                                .body(new GlobalResponseDTO(true,message,finalResponse));
                    }else{
                        String message = scheduleQuery.saveWorkingHours(start_time, localDate, end_time, tenantId, userId);
                        Map<String, Object> finalResponse = new HashMap<>();
                        finalResponse.put("isScheduleBeforeTheWorkingHour", "N");
                        return ResponseEntity.accepted()
                                .body(new GlobalResponseDTO(true,message,finalResponse));
                    }
                }
            }
        }
        String message = scheduleQuery.saveWorkingHours(start_time, localDate, end_time, tenantId, userId);
        Map<String, Object> finalResponse = new HashMap<>();
        finalResponse.put("isScheduleBeforeTheWorkingHour", "N");
        return ResponseEntity.accepted()
                .body(new GlobalResponseDTO(true,message,finalResponse));
    }


    /*
     *AMIT KUMAR SINGH
     * This method retrieves service entity fields based on the provided request parameters.
     * @param request 'compId'.
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> serviceEntityField(Map<String, String> request) {
        String deviceId = request.get("deviceId");
        String deviceType = request.get("deviceType");
        String appVersion = request.get("appVersion");
        String userId = request.get("userId");
        String tenantId = request.get("tenantId");
        String compId = request.get("compId");

        List<JSONObject> storeServiceEntityFields = companyMasterRepository.getServiceEntityFields(Integer.parseInt(compId));
        System.out.println(storeServiceEntityFields);

        List<Map<String, Object>> responseList = new ArrayList<>();
        List<Map<String, Object>> rowItems = new ArrayList<>();
        String prevGId = null;
        Map<String, Object> responseMap = null;
        Map<String, Object> responseMap2 = null;
        List<String> stringList;
        List<String> stringGenderList;
        String serviceType = null;
        for (JSONObject field : storeServiceEntityFields) {
            Object groupByValue = field.get("GROUP_BY");
            String gId = groupByValue != null ? groupByValue.toString() : null;
            if (prevGId != null && !prevGId.equals(gId) && gId != null) {
                responseMap.put("row_items", rowItems);
                responseList.add(responseMap);
                rowItems = new ArrayList<>();
            }
            prevGId = gId;
            if (groupByValue != null) {
                responseMap = new HashMap<>();
                responseMap.put("question_id", null);
                responseMap.put("type_id", 11);
                responseMap.put("question", "");
                responseMap.put("items", null);
                responseMap.put("answer", "");
                String itemsFileds = (String) field.get("items");
                Map<String, Object> rowItem1 = new HashMap<>();
                if (itemsFileds != null) {
                    stringList = Arrays.asList(itemsFileds.split(","))
                            .stream()
                            .map(String::trim)
                            .collect(Collectors.toCollection(ArrayList::new));
                    System.err.println(stringList);
                } else {
                    stringList = null;
                }
                rowItem1.put("row_question_id", field.get("type"));
                rowItem1.put("type_id", field.get("ANSWER_TYPE"));
                rowItem1.put("row_question", field.get("question"));
                rowItem1.put("items", stringList);
                rowItem1.put("row_answer", "");
                rowItems.add(rowItem1);
                responseMap.put("row_items", rowItems);
            } else {
                String genderList = (String) field.get("items");
                if (genderList != null) {
                    stringGenderList = Arrays.asList(genderList.split(","))
                            .stream()
                            .map(String::trim)
                            .collect(Collectors.toCollection(ArrayList::new));
                    System.err.println(stringGenderList);
                } else {
                    stringGenderList = null;
                }
                responseMap2 = new HashMap<>();
                responseMap2.put("question_id", field.get("type"));
                responseMap2.put("type_id", field.get("ANSWER_TYPE"));
                responseMap2.put("question", field.get("question"));
                responseMap2.put("items", stringGenderList);
                responseMap2.put("answer", "");
                responseMap2.put("row_items", null);
                responseList.add(responseMap2);
            }
            serviceType = null;
            String serviceTypeDb = field.get("FK_BUSINESS_TYPE_ID").toString();
            if ("4".equals(serviceTypeDb)) {
                serviceType = "Person";
            }
        }
        responseList.add(responseMap);
        Map<String, Object> finalResponse = new HashMap<>();
        finalResponse.put("service_entity_items", responseList);
        return ResponseEntity.accepted()
                .body(new GlobalResponseDTO(true, serviceType, finalResponse));
    }

    @Override
    public ResponseEntity<GlobalResponseDTO> custWiseServiceEntity(Map<String, String> request) {
        String tenantId = request.get("tenantId");
        String customer_id = request.get("customer_id");
        List<Map<String, Object>> getCustWiseServiceEntity = scheduleQuery.getCustWiseServiceEntity(tenantId, Integer.valueOf(customer_id));
        return ResponseEntity.accepted()
                .body(new GlobalResponseDTO(true, "Success", getCustWiseServiceEntity));
    }

    /**
     * Retrieves service entity details including questionnaires, answers, and items based on provided request parameters.
     *
     * @param request The request containing parameters like deviceId, deviceType, appVersion, userId, tenantId, service_entity_id, and CompanyId.
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> getServiceEntityDetails(Map<String, String> request) {
        String tenantId = request.get("tenantId");
        String service_entity_id = request.get("service_entity_id");
        String CompanyId = request.get("compId");
        List<Map<String, Object>> storeServiceEntityFields = scheduleQuery.getServiceEntityFieldsHistory(tenantId, service_entity_id);
        Integer compTypeId = companyMasterRepository.compTypeId(Integer.parseInt(CompanyId));
        String strCompTypeId = compTypeId.toString();
        List<Map<String, Object>> responseList = new ArrayList<>();
        List<Map<String, Object>> rowItems = new ArrayList<>();
        String prevGId = null;
        Map<String, Object> responseMap = null;
        Map<String, Object> responseMap2 = null;
        Map<String, Object> responseMap3 = null;
        String serviceType = null;
        for (Map<String, Object> field : storeServiceEntityFields) {
            Object groupByValue = field.get("GROUP_BY");
            String gId = groupByValue != null ? groupByValue.toString() : null;
            if (prevGId != null && !prevGId.equals(gId) /*&& gId != null*/) {
                responseList.add(responseMap);
                rowItems = new ArrayList<>();
            }
            prevGId = gId;
            if (groupByValue != null) {
                responseMap = new HashMap<>();
                responseMap.put("question_id", null);
                responseMap.put("type_id", 11);
                responseMap.put("question", "");
                responseMap.put("items", null);
                responseMap.put("answer", "");
                Map<String, Object> rowItem1 = new HashMap<>();
                rowItem1.put("row_question_id", field.get("type"));
                rowItem1.put("type_id", field.get("ANSWER_TYPE"));
                rowItem1.put("row_question", field.get("question"));
                if (field.get("items") == null) {
                    rowItem1.put("items", field.get("items"));
                } else {
                    ArrayList<String> stringList = Arrays.asList(String.valueOf(field.get("items")).split(","))
                            .stream()
                            .map(String::trim)
                            .collect(Collectors.toCollection(ArrayList::new));
                    rowItem1.put("items", stringList);
                }
                rowItem1.put("row_answer", field.get("ANSWER"));
                rowItems.add(rowItem1);
                responseMap.put("row_items", rowItems);
            } else {
                System.out.println("GROUP_BY is null");
                responseMap2 = new HashMap<>();
                responseMap2.put("question_id", field.get("type"));
                responseMap2.put("type_id", field.get("ANSWER_TYPE"));
                responseMap2.put("question", field.get("question"));
                if (field.get("items") == null) {
                    responseMap2.put("items", field.get("items"));
                } else {
                    ArrayList<String> stringList = Arrays.asList(String.valueOf(field.get("items")).split(","))
                            .stream()
                            .map(String::trim)
                            .collect(Collectors.toCollection(ArrayList::new));
                    responseMap2.put("items", stringList);
                }
                responseMap2.put("answer", field.get("ANSWER"));
                responseMap2.put("row_items", null);
                responseList.add(responseMap2);
                System.out.println("else... " + responseMap2);
            }
            serviceType = null;
            if ("4".equals(strCompTypeId)) {
                serviceType = "Person";
            }
        }
        System.err.println(responseMap);
        responseList.add(responseMap);
        Map<String, Object> finalResponse = new HashMap<>();
        finalResponse.put("service_entity_items", responseList);
        if(responseList.size()>1){
            return ResponseEntity.accepted()
                    .body(new GlobalResponseDTO(true, "Success", finalResponse));
        }else{
            return ResponseEntity.accepted()
                    .body(new GlobalResponseDTO(false, "Not found", finalResponse));
        }
    }


    /**
     * Retrieves service entity details including questionnaires, answers, and items based on a particular customer_ID.
     *
     * @param request The request containing parameters like deviceId, deviceType, appVersion, userId, tenantId, service_entity_id, and CompanyId.
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> getServiceEntityDetailsbyCustomerId(Map<String, String> request) {
        String deviceId = request.get("deviceId");
        String deviceType = request.get("deviceType");
        String appVersion = request.get("appVersion");
        String customer_id = request.get("customer_id");
        String tenantId = request.get("tenantId");
        String service_entity_id = request.get("service_entity_id");
        String CompanyId = request.get("compId");
        String pkjobid = request.get("pkjobid");


        List<Map<String, Object>> storeServiceEntityFields = scheduleQuery.getServiceEntityFieldsHistoryBycustomerId(tenantId, customer_id, pkjobid);

        Integer compTypeId = companyMasterRepository.compTypeId(Integer.parseInt(CompanyId));
        String strCompTypeId = compTypeId.toString();

        List<Map<String, Object>> responseList = new ArrayList<>();
        List<Map<String, Object>> rowItems = new ArrayList<>();
        String prevGId = null; // To track the previous gId
        Map<String, Object> responseMap = null; // Initialize the responseMap outside the loop
        Map<String, Object> responseMap2 = null; // Initialize the responseMap outside the loop
        Map<String, Object> responseMap3 = null; // Initialize the responseMap outside the loop

        String serviceType = null;
        for (Map<String, Object> field : storeServiceEntityFields) {
            // System.out.println("loop");
            // System.out.println(field.get("GROUP_BY"));

            Object groupByValue = field.get("GROUP_BY");
            String gId = groupByValue != null ? groupByValue.toString() : null;

            if (prevGId != null && !prevGId.equals(gId)) {
                // Add the previous row_items to the responseMap and reset rowItems
                // responseMap.put("row_items", rowItems);
                //   responseList.add(responseMap);

                // Create new rowItems for the new group
                responseList.add(responseMap);
                rowItems = new ArrayList<>();
            }

            prevGId = gId;


            if (groupByValue != null) {
                //System.out.println("it is not null");

                responseMap = new HashMap<>(); // Initialize a new map for each iteration

                responseMap.put("question_id", null);
                responseMap.put("type_id", 11);
                responseMap.put("question", "");
                responseMap.put("items", null);
                responseMap.put("answer", "");

                Map<String, Object> rowItem1 = new HashMap<>();

                rowItem1.put("question_id", field.get("type"));
                rowItem1.put("type_id", field.get("ANSWER_TYPE"));
                rowItem1.put("question", field.get("question"));
                if (field.get("items") == null) {
                    rowItem1.put("items", field.get("items"));
                } else {
                    ArrayList<String> stringList = Arrays.asList(String.valueOf(field.get("items")).split(","))
                            .stream()
                            .map(String::trim)
                            .collect(Collectors.toCollection(ArrayList::new));
                    rowItem1.put("items", stringList);
                }

                rowItem1.put("answer", field.get("ANSWER"));
                rowItems.add(rowItem1);

                responseMap.put("row_items", rowItems);


                // System.out.println("if... " + responseMap);

            } else {
                // Handle the case where "GROUP_BY" is null
                System.out.println("GROUP_BY is null");

                responseMap2 = new HashMap<>(); // Initialize a new map for each iteration

                responseMap2.put("question_id", field.get("type"));
                responseMap2.put("type_id", field.get("ANSWER_TYPE"));
                responseMap2.put("question", field.get("question"));
                // responseMap2.put("items", field.get("items"));

                if (field.get("items") == null) {
                    responseMap2.put("items", field.get("items"));
                } else {
                    ArrayList<String> stringList = Arrays.asList(String.valueOf(field.get("items")).split(","))
                            .stream()
                            .map(String::trim)
                            .collect(Collectors.toCollection(ArrayList::new));
                    responseMap2.put("items", stringList);
                }
                responseMap2.put("answer", field.get("ANSWER"));
                responseMap2.put("row_items", null);
                responseList.add(responseMap2);
                System.out.println("else... " + responseMap2);
            }

            serviceType = null;
            //  String serviceTypeDb = field.get("FK_BUSINESS_TYPE_ID").toString();
            //String serviceTypeDb = (field.get("FK_BUSINESS_TYPE_ID") != null) ? field.get("FK_BUSINESS_TYPE_ID").toString() : null;
            if ("4".equals(strCompTypeId)) {
                serviceType = "Human";
            }

        }
        System.err.println(responseMap);
        responseList.add(responseMap);

        // responseList.remove(responseList.size() - 1); // Remove the last element from the list

        Map<String, Object> finalResponse = new HashMap<>();
        System.out.println("merge test");
        finalResponse.put("service_entity_items", responseList);

        System.out.println("size " + responseList.size());

        return ResponseEntity.accepted()
                .body(new GlobalResponseDTO(true, serviceType, finalResponse));
    }


    public String createInvoice(String processedHtml, String tenantId, int invoiceId, String jobid, String customer_Id, String invoiceid) {
        String finalString = null;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {

            PdfWriter pdfwriter = new PdfWriter(byteArrayOutputStream);

            DefaultFontProvider defaultFont = new DefaultFontProvider(false, true, false);

            ConverterProperties converterProperties = new ConverterProperties();

            converterProperties.setFontProvider(defaultFont);

            HtmlConverter.convertToPdf(processedHtml, pdfwriter, converterProperties);
            UUID randomUUID = UUID.randomUUID();
            String name = "invoice2" + randomUUID + ".pdf";
            finalString = name;
            String filepath = invoicepath + File.separator + name;
            File f = new File(invoicepath);

            ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            if (!f.exists()) {
                f.mkdir();
            }
            Files.copy(inputStream, Paths.get(filepath));
            LocalDateTime currentTimestamp = LocalDateTime.now();

            //   InetAddress localHost = InetAddress.getLocalHost();
            // Get the host name
            // String hostName = localHost.getHostName();

            //    System.out.println("Host Name: " + hostName);

            String invoiceQuery = "INSERT INTO " + tenantId + ".invoice_master (\"PK_INVOICE_MASTER_ID\",\"INVOICE_MASTER_NAME\",\"FK_PAYMENT_TRANSACTION_ID\",\"INVOICE_MASTER_DATE\",\"PK_JOB_ID\",\"CUSTOMER_ID\",\"INVOICE_ID\") VALUES ((SELECT COALESCE((SELECT MAX(\"PK_INVOICE_MASTER_ID\") FROM \"" + tenantId + "\".\"invoice_master\"),0)+1),?,?,?,?,?,?)";
            System.err.println(invoiceQuery);
            jdbcTemplate.update(invoiceQuery, name, invoiceId, currentTimestamp, Integer.parseInt(jobid), Integer.parseInt(customer_Id), invoiceid);
            System.err.println("Invoice Created");

            /*String updateJobStatus = "UPDATE " + tenantId + ".job_master SET \"JOB_STATUS\" = ? WHERE \"PK_JOB_ID\" = ?";
            System.err.println(invoiceQuery);
            jdbcTemplate.update(updateJobStatus, "1", Integer.parseInt(jobid));
            System.err.println("job Updated");*/




            /*  HttpHeaders httpHeaders=new HttpHeaders();
            httpHeaders.add("Content-Disposition","inline;file=invoice.pdf");
*/

        } catch (Exception ex) {
            System.err.println(ex.getMessage());


        }


        return finalString;
    }


    @Override
    public void downloadInvoice(String imageName, HttpServletResponse response) throws IOException {
        InputStream resource = scheduleQuery.getInvoice(invoicepath, imageName);
        System.err.println(resource);

        if (imageName.toLowerCase().endsWith(".pdf")) {
            response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        } else if (imageName.toLowerCase().endsWith(".jpg")) {
            response.setContentType(MediaType.IMAGE_JPEG_VALUE);
        }

        StreamUtils.copy(resource, response.getOutputStream());

    }


    @Override
    public ResponseEntity<GlobalResponseDTO> generateInvoice(Map<String, Object> request, Principal principal) {
        ArrayList<Map<String, String>> invoiceNameList = new ArrayList<>();
        try {

//            String endDate = (String) request.get("endDate");
//            String sub_total = (String) request.get("sub_total");
//            String grand_total = (String) request.get("grand_total");
//           String startDate = (String) request.get("startDate");
//            String tanentId = (String) request.get("tenantId");
//            String StaffList = (String) request.get("Staffname");
//            String jobid = (String) request.get("jobId");
//           String total_Tax = (String) request.get("tax_sum");
//            String job_Location = (String) request.get("joblocation");
//            String jobNote = (String) request.get("jobnote");
//            String CustomerName=  (String) request.get("customerName");
//            ArrayList<Map<String, String>> serviceName = (ArrayList<Map<String, String>>) request.get("Material_details");
//            ArrayList<Map<String, String>> staffDetails = (ArrayList<Map<String, String>>) request.get("StaffList");
//           ArrayList<Map<String, Object>> customerList = (ArrayList<Map<String, Object>>) request.get("CustomersList");

            String endDate = (String) request.get("endDate");
            String sub_total = (String) request.get("sub_total");
            String grand_total = (String) request.get("grand_total");
            String startDate = (String) request.get("startDate");
            String tanentId = (String) request.get("tenantId");
            String StaffList = (String) request.get("Staffname");
            String jobid = (String) request.get("jobId");
            String total_Tax = (String) request.get("tax_sum");
            String job_Location = (String) request.get("joblocation");
            String jobNote = (String) request.get("jobnote");
            //  String CustomerName=  (String) request.get("customerName");
            ArrayList<Map<String, String>> materials = (ArrayList<Map<String, String>>) request.get("Material_details");
            ArrayList<Map<String, String>> staffDetails = (ArrayList<Map<String, String>>) request.get("StaffList");
            ArrayList<Map<String, Object>> customerList = (ArrayList<Map<String, Object>>) request.get("CustomersList");


            //for multiple Customer create multiple invoice
            if (customerList.isEmpty()) {
                return ResponseEntity.accepted()
                        .body(new GlobalResponseDTO(false, "customer is mandatory", null));
            }

            for (Map<String, Object> customers : customerList) {
                String customer_Id = String.valueOf(customers.get("PK_CUSTOMER_ID"));
                String finalName = customers.get("CUSTOMER_FIRST_NAME").toString() + " " + customers.get("CUSTOMER_LAST_NAME").toString();
                ArrayList<Map<String, Object>> serviceEntityList = (ArrayList<Map<String, Object>>) customers.get("ServiceEntityList");
                StringBuilder sp = new StringBuilder();
                serviceEntityList.stream()
                        .map(map -> (String) map.get("SERVICE_ENTITY_NAME")) // Extract SERVICE_ENTITY_NAME from each map
                        .forEach(serviceEntityName -> sp.append(serviceEntityName).append(",")); // Append each name to StringBuilder


//                String serviceEntity = sp.toString(); // Convert StringBuilder to String
//                if (serviceEntity.endsWith(",")) {
//                    serviceEntity.substring(0, serviceEntity.length() - 1); // Remove the trailing comma
//                }
//                System.err.println(serviceEntity);
//                String outputpDF;
//                Context context = new Context();
//                Random random = new Random();
//                long currentTimeMillis = System.currentTimeMillis();
//                final String invoiceid = "Bizfns" + currentTimeMillis;

                String serviceEntity = sp.toString(); // Convert StringBuilder to String
                if (serviceEntity.endsWith(",")) {
                    serviceEntity.substring(0, serviceEntity.length() - 1); // Remove the trailing comma
                }
                System.err.println(serviceEntity);
                String outputpDF;
                Context context = new Context();
                Random random = new Random();
                long currentTimeMillis = System.currentTimeMillis();
                final String invoiceid = "Bizfns" + currentTimeMillis;

                int invoiceId_pdfName = random.nextInt(Integer.MAX_VALUE);
                //set All data in html template
                LocalDateTime currentTimestamp = LocalDateTime.now();
                context.setVariable("CustomerName", finalName);
                context.setVariable("scheduleName", jobNote);
                context.setVariable("invoiceId", invoiceid);
                context.setVariable("address", job_Location);
                context.setVariable("endDate", currentTimestamp);
                context.setVariable("staffName", StaffList);
                context.setVariable("serviceName", materials);
                context.setVariable("sub_total", sub_total);
                context.setVariable("grand_total", grand_total);
                context.setVariable("startDate", startDate);
                context.setVariable("jobid", jobid);
                context.setVariable("staffDetails", staffDetails);
                context.setVariable("total_Tax", total_Tax);
                context.setVariable("serviceEntity", serviceEntity);
                String fileName = "";

                outputpDF = springTemplateEngine.process("InvoiceTemplate", context);
                System.err.println(outputpDF);

                String jobFileName = "select * from " + tanentId + ".invoice_master im where im.\"PK_JOB_ID\" = " + Integer.parseInt(jobid) + " AND im.\"CUSTOMER_ID\" = " + Integer.parseInt(customer_Id);
                List<Map<String, Object>> getData = jdbcTemplate.queryForList(jobFileName);
                Map<String, String> invocees = new HashMap<>();
                if (getData.isEmpty()) {
                    fileName = createInvoice(outputpDF, tanentId, invoiceId_pdfName, jobid, customer_Id, invoiceid);
                    invocees.put("CustomerName", finalName);
                    invocees.put("Invoice", fileName);
                    invoiceNameList.add(invocees);
                } else {
                    Map<String, Object> fileData = getData.get(0);
                    fileName = (String) fileData.get("INVOICE_MASTER_NAME");
                    invocees.put("CustomerName", finalName);
                    invocees.put("Invoice", fileName);
                    invoiceNameList.add(invocees);
                }


//                int invoiceId_pdfName = random.nextInt(Integer.MAX_VALUE);
//                //set All data in html template
//                LocalDateTime currentTimestamp = LocalDateTime.now();
//                context.setVariable("CustomerName", finalName);
//                context.setVariable("scheduleName", jobNote);
//                context.setVariable("invoiceId", invoiceid);
//                context.setVariable("address", job_Location);
//                context.setVariable("endDate", currentTimestamp);
//                context.setVariable("staffName", StaffList);
//                context.setVariable("serviceName", serviceName);
//                context.setVariable("sub_total", sub_total);
//                context.setVariable("grand_total", grand_total);
//                context.setVariable("startDate", startDate);
//                context.setVariable("jobid", jobid);
//                context.setVariable("staffDetails", staffDetails);
//                context.setVariable("total_Tax", total_Tax);
//                context.setVariable("serviceEntity", serviceEntity);
//                String fileName = "";
//
//                outputpDF = springTemplateEngine.process("InvoiceTemplate", context);
//                System.err.println(outputpDF);
//
//                String jobFileName = "select * from " + tanentId + ".invoice_master im where im.\"PK_JOB_ID\" = " + Integer.parseInt(jobid) + " AND im.\"CUSTOMER_ID\" = " + Integer.parseInt(customer_Id);
//                List<Map<String, Object>> getData = jdbcTemplate.queryForList(jobFileName);
//                Map<String, String> invocees = new HashMap<>();
//                if (getData.isEmpty()) {
//                    fileName = createInvoice(outputpDF, tanentId, invoiceId_pdfName, jobid, customer_Id, invoiceid);
//                    invocees.put("CustomerName", finalName);
//                    invocees.put("Invoice", fileName);
//                    invoiceNameList.add(invocees);
//                } else {
//                    Map<String, Object> fileData = getData.get(0);
//                    fileName = (String) fileData.get("INVOICE_MASTER_NAME");
//                    invocees.put("CustomerName", finalName);
//                    invocees.put("Invoice", fileName);
//                    invoiceNameList.add(invocees);
//                }


            }


        } catch (Exception ex) {

        }


        return ResponseEntity.accepted()
                .body(new GlobalResponseDTO(true, "Success", invoiceNameList));


    }

    @Override
    public ResponseEntity<GlobalResponseListObject> getCustomerHistory(Map<String, Object> request) {

        return ResponseEntity.accepted()
                .body(new GlobalResponseListObject(true, "Fetch Successfully", null));

    }
    @Override
    public ResponseEntity<GlobalResponseListObject> getCustomerServiceHistory(Map<String, Object> request) {
        String tenantId = (String) request.get("tenantId");
        List<Map<Integer, Object>> customerIdList = (List<Map<Integer, Object>>) request.get("customerIdList");
        List<List<Map<String, Object>>> jobHistoriesPerCustomerId = new ArrayList<>();
        for (Map<Integer, Object> customerIds : customerIdList) {
            Integer custId = (Integer) customerIds.get("customerId");
            List<Map<String, Object>> jobHistoryAsPerCustomerId = scheduleQuery.getCustomerServiceHistory(tenantId, custId);
            jobHistoriesPerCustomerId.add(jobHistoryAsPerCustomerId);
        }
        return ResponseEntity.accepted()
                .body(new GlobalResponseListObject(true, "Fetched Successfully", jobHistoriesPerCustomerId));
    }


    /*
     * AMIT KUMAR SINGH
     * This method is used to save the maximum task as per the userID
     * @Param userId , tenantId , maxJobTask
     **/
    @Override

    public ResponseEntity<GlobalResponseDTO> saveMaxJobTask(Map<String, Object> request) {
        String tenantId = (String) request.get("tenantId");
        String userId = (String) request.get("userId");
        String date = (String) request.get("fromDate");
        String maxJobTask = (String) request.get("maximumTask");

        String message = scheduleQuery.saveMaxJobTask(maxJobTask);

        return ResponseEntity.accepted()
                .body(new GlobalResponseDTO(true, message));
    }

    /**
     *AMIT KUMAR SINGH
     *
     * Handles staff user login and password change process.
     *
     * @param request A Map containing request parameters:
     *                - "userId": String representing the user ID of the staff member.
     *                - "temporaryPassword": String representing the temporary password provided by the user.
     *                - "newPassword": String representing the new password to be set by the user.
     *                - "tenantId": String representing the ID of the tenant.
     *                - "staffEmail": String representing the email of the staff member.
     */
    public ResponseEntity<GlobalResponseDTO> staffUserLogin(Map<String, Object> request) {
        String userId = (String) request.get("userId");
        String temporaryPassword = (String) request.get("temporaryPassword");
        String newPassword = (String) request.get("newPassword");
        String tenantId = (String) request.get("tenantId");
        String staffEmail = (String) request.get("staffEmail");
        AES obj = new AES();
        String tenantFirstEightTLetters = null;

        if (userId == null || userId.isBlank()) {
            return ResponseEntity.accepted()
                    .body(new GlobalResponseDTO(false, "User Id is blank or null", null));
        }
        if (newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.accepted()
                    .body(new GlobalResponseDTO(false, "password is blank or null", null));
        }
        if (tenantId != null && tenantId.length() >= 2) {
            tenantFirstEightTLetters = tenantId.substring(0, 8);
        }
        if (tenantId != null && tenantId.length() < 8) {
            return ResponseEntity.accepted().body(new GlobalResponseDTO(false, "enter valid Business Id", null));
        }

        String staffExistence = staffQuery.getStaffMailId(staffEmail, tenantFirstEightTLetters);
        String staffDBTempPassword = obj.decrypt(staffAuthQuery.staffDbPasswordForStaff(userId, tenantFirstEightTLetters));
        if (temporaryPassword.equals(staffDBTempPassword)) {
            staffAuthQuery.changeSataffPassord(tenantFirstEightTLetters, obj.encrypt(newPassword), userId);
            Map<String, Object> response = new HashMap<>();

            response.put("newPassword", newPassword);
            response.put("userID", userId);
            response.put("message", "The new password got set");
            return ResponseEntity.accepted().body(new GlobalResponseDTO(true, "Success", response));
        }
        Map<String, Object> response1 = new HashMap<>();
        response1.put("newPassword", newPassword);
        response1.put("userID", userId);
        return ResponseEntity.accepted().body(new GlobalResponseDTO(false,
                "The temp password does not match", response1));
    }


    /**
     * This method handles saving or updating job status based on the provided parameters.
     *
     * @param request A Map containing request parameters:
     *                - "tenantId": String representing the ID of the tenant.
     *                - "jobId": String representing the ID of the job.
     *                - "jobStatus": String representing the status of the job.
     *                - "fkSubscriptionId": String representing the ID of the company subscription related to the job.
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> saveJobStatus(Map<String, Object> request) {
        String tenantId = (String) request.get("tenantId");
        String jobId = (String) request.get("jobId");
        String jobStatus = (String) request.get("JobStatus");
        String fkSubscriptionId = (String) request.get("fkSubscriptionId");
        String selectQuery = "select DISTINCT\"PK_JOB_ID\" ,\"JOB_STATUS\" from \"" + tenantId + "\".job_master jm where JM.\"PK_JOB_ID\" = ? ";
        List<Map<String, Object>> dataForJobDetails = jdbcTemplate.queryForList(selectQuery, Integer.parseInt(jobId));

        if (dataForJobDetails.size() == 0) {
            String insertQuery = "insert into \"" + tenantId + "\".job_master(\"PK_JOB_ID\",\"JOB_STATUS\",\"FK_COMPANY_SUBSCRIPTION_ID\") \n" +
                    "values (?,?,?)";
            System.err.println(insertQuery);
            jdbcTemplate.update(insertQuery, Integer.parseInt(jobId), jobStatus, Integer.parseInt(fkSubscriptionId));

        } else {
            String updateQuery = "UPDATE \"" + tenantId + "\".job_master " +
                    "SET \"JOB_STATUS\" = ? " +
                    "WHERE \"PK_JOB_ID\" = ?";

            jdbcTemplate.update(updateQuery, jobStatus, Integer.parseInt(jobId));

        }
//        Map<String, Object> response = new HashMap<>();
//        Boolean ans=false;
//        for (Map<String, Object> data : dataForJobDetails) {
//            if(!data.get("JOB_STATUS").equals(jobStatus)){
//                ans=true;
//            }
//        }
        // if(ans) {
//            String insertQuery = "insert into \"" + tenantId + "\".job_master(\"PK_JOB_ID\",\"JOB_STATUS\",\"FK_COMPANY_SUBSCRIPTION_ID\") \n" +
//                    "values (?,?,?)";
//            System.err.println(insertQuery);
        // jdbcTemplate.update(insertQuery, Integer.parseInt(jobId), jobStatus, Integer.parseInt(fkSubscriptionId));
        Map<String, Object> response1 = new HashMap<>();
        String statusDescription;
        //for jobcode 0,1,2,3,4 and the status is open,completed,attempted,WIP,closed respectively.
        switch (Integer.parseInt(jobStatus)) {
            case 0:
                statusDescription = "Open";
                break;
            case 1:
                statusDescription = "Completed";
                break;
            case 2:
                statusDescription = "Attempted";
                break;
            case 3:
                statusDescription = "WIP";
                break;
            case 4:
                statusDescription = "Closed";
                break;
            default:
                statusDescription = "Unknown";
                break;
        }
        response1.put("jobId", jobId);
        response1.put("jobStatus", statusDescription);
        return ResponseEntity.accepted().body(new GlobalResponseDTO(true,
                "The job status got assigned to job", response1));
        // }
//        else {
//            response.put("jobId", jobId);
//            response.put("jobStatus", jobStatus);
//            return ResponseEntity.accepted().body(new GlobalResponseDTO(false,
//                    "The job status already exist",response));
//        }

    }


    /**
     * AMIT KUMAR SINGH
     * This method retrieves the status of a job based on the particular job_id.
     *
     * @param request A Map containing request parameters:
     *                - "tenantId": String representing the ID of the tenant.
     *                - "jobId": String representing the ID of the job.
     */

    @Override
    public ResponseEntity<GlobalResponseDTO> getJobStatus(Map<String, Object> request) {

        String tenantId = (String) request.get("tenantId");
        String jobId = (String) request.get("jobId");

        String selectQuery = "select \"PK_JOB_ID\" ,\"JOB_STATUS\" from \"" + tenantId + "\".job_master jm where JM.\"PK_JOB_ID\" = ? ";
        Map<String, Object> stringObjectMap = jdbcTemplate.queryForMap(selectQuery, Integer.parseInt(jobId));
        Map<String, Object> response = new HashMap<>();
        response.put("jobId", stringObjectMap.get("PK_JOB_ID"));
        //List<String> statuses = new ArrayList<>();

        //for (Map<String, Object> data : dataForJobDetails) {
        //   statuses.add((String) data.get("JOB_STATUS")); // Assuming "status" is the column name for status
        // }

        response.put("statuses", stringObjectMap.get("JOB_STATUS"));

        return ResponseEntity.accepted().body(new GlobalResponseDTO(true,
                "The fetched JOB Status", response));
    }

    /*
     * This method Saves the time interval for working hour for a given user and date
     * , after performing authorization and validation checks.
     * If conditions are met, saves the interval; otherwise, returns appropriate error messages.
     *
     * @param 'fromDate','interval'.
     */
    @Override
    @CacheEvict(value = "scheduleList", allEntries = true)
    public ResponseEntity<GlobalResponseDTO> saveTimeInterval(Map<String, String> request, Principal principal) throws ParseException {
        String userId = request.get("userId");
        String tenantId = request.get("tenantId");
        String fromDate = request.get("fromDate");
        /*String hh = request.get("hh");
        String mm = request.get("mm");*/
        /*String interval = hh + ":" + mm;*/
        String interval = request.get("interval");
        String[] parts = interval.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        double hoursAsDouble = hours + (minutes / 60.0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(fromDate, formatter);
        if (checkUserMatch(userId, tenantId, principal.getName())) {
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorised user, we could not access the APIs from others token"));
        }
        List<Map<String, Object>> fetchJobData = scheduleQuery.ftechScheduleDatabyDate(tenantId, fromDate);
        if (!fetchJobData.isEmpty()) {
            return ResponseEntity.ok().body(new GlobalResponseDTO(
                    false, "slot is booked already, can't proceed this date"));
        }
        String workingHourDuration = scheduleQuery.intervalDuration(tenantId,localDate,userId);
        if(hoursAsDouble <= Double.parseDouble(workingHourDuration)){
            String response = scheduleQuery.saveCompanyTimeInterval(tenantId, localDate, interval, userId);
            return ResponseEntity.accepted()
                    .body(new GlobalResponseDTO(true, response));
        }else{
            return ResponseEntity.badRequest()
                    .body(new GlobalResponseDTO(false, "Interval Outside The Working Hour"));
        }
    }

    @Override
    public ResponseEntity<GlobalResponseDTO> getTimeIntervalFromDb(Principal principal) {
        Map<String, String> workingHours = scheduleQuery.getTimeIntervalFromDb(principal);
        List<Map<String, String>> workingHoursList = new ArrayList<>();
        String formattedInterval = "00:00";
        if (workingHours.containsKey("fromDate") && workingHours.containsKey("interval")) {
            String interval = workingHours.get("interval");
            formattedInterval = interval.replace(":", ":");
            Map<String, String> formattedData = new HashMap<>();
            formattedData.put("fromDate", workingHours.get("fromDate"));
            formattedData.put("interval", formattedInterval);
            workingHoursList.add(formattedData);
        }
        return ResponseEntity.accepted()
                .body(new GlobalResponseDTO(true, "Fetch Successfully", workingHoursList));
    }

    @Override
    public ResponseEntity<GlobalResponseDTO> getTimeInterval(Map<String, String> request) {
        String tenantId = request.get("tenantId");
        String userId = request.get("userId");
        String fromDate = request.get("fromDate");
        Map<String, Object> workingHours = scheduleQuery.getWorkingHours(fromDate, userId, tenantId);
        List<Map.Entry<String, Object>> workingHoursList = new ArrayList<>(workingHours.entrySet());
        return ResponseEntity.accepted()
                .body(new GlobalResponseDTO(true, "Fetch Successfully", workingHoursList));

    }

    @Override
    public ResponseEntity<GlobalResponseDTO> calCulateJobPrice(Map<String, String> request, Principal principal) throws CustomException {

        String jobname = request.get("jobid");
        String tanentid = request.get("tenantId");

        if (token.checkUserMatch(tanentid, principal.getName())) {
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorised user, we could not access the APIs from others token "));
        }

        return scheduleQuery.getjobScheduleCalculation(jobname, tanentid);

    }

    /**
     * Saves media files (MultipartFiles) associated with a job for a specific tenant.
     *
     * @param file      Array of MultipartFiles representing the media files to be saved.
     * @param tenantId  The ID of the tenant to which the media files belong.
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> saveMediaFile(MultipartFile[] file, String tenantId, String Pkjobid, String auditId, Principal principal) {
        System.out.println(tenantId);
        String uploadMessage = scheduleQuery.saveMediaImage(tenantId, Pkjobid, path, file, auditId);
        if (token.checkUserMatch(tenantId, principal.getName())) {
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorised user, we could not access the APIs from others token "));
        }
        return ResponseEntity.accepted()
                .body(new GlobalResponseDTO(true, "Success", uploadMessage));
    }


    /**
     * Retrieves media file details based on the provided imageId and tenantId.
     *
     * @param 'imageId' and 'tenantId'
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> getMediaFile(Map<String, String> request) {
        String imageId = request.get("imageId");
        String tenantId = request.get("tenantId");
        List<Map<String, Object>> mediaNameList = scheduleQuery.getMediaNameList(imageId, tenantId);
        return ResponseEntity.accepted()
                .body(new GlobalResponseDTO(true, " Media File Fetch Successfully  ", mediaNameList));
    }

    /**
     * Deletes a media file identified by mediaId and associated with a specific tenantId.
     *
     * @param 'mediaId' and 'tenantId'.
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> deleteMediaFile(Map<String, String> request) {
        String imageAuditID = request.get("mediaId");
        String tenantId = request.get("tenantId");
        String deletedmessage = scheduleQuery.deleteMediaFile(tenantId, imageAuditID, path);
        return ResponseEntity.accepted()
                .body(new GlobalResponseDTO(true, deletedmessage, null));
        //InputStream resource= scheduleQuery.getMediaFile(path,imageName);
    }

    @Override
    public void downloadImage(String imageName, HttpServletResponse response) throws IOException {
        InputStream resource = scheduleQuery.getMediaFile(path, imageName);
        if (imageName.toLowerCase().endsWith(".png")) {
            response.setContentType(MediaType.IMAGE_PNG_VALUE);
        } else if (imageName.toLowerCase().endsWith(".jpg")) {
            response.setContentType(MediaType.IMAGE_JPEG_VALUE);
        }

        StreamUtils.copy(resource, response.getOutputStream());

    }


    /**
     * Retrieves the maximum job task data (i.e maximum job limit per day for a schedule/job) from the database.
     *
     * @return ResponseEntity containing a GlobalResponseDTO indicating success or failure with the retrieved data.
     */
    @Override
    public ResponseEntity<GlobalResponseDTO> getMaxJobTask() {
        String maxJobTask = scheduleQuery.getMaxJobTask();
        if (!maxJobTask.equalsIgnoreCase("failure")) {
            return ResponseEntity.ok().body(new GlobalResponseDTO(true, "success", maxJobTask));
        } else {
            return ResponseEntity.ok().body(new GlobalResponseDTO(false, "Max Job Task data is not found"));
        }
    }

    @Override
    public ResponseEntity<GlobalResponseDTO> deleteServiceObject(Map<String, String> request) {
        String tenantId = request.get("tenantId");
        String serviceEntityId = request.get("service_entity_id");

        try {
            scheduleQuery.deleteServiceObjectFromDB(tenantId, Integer.parseInt(serviceEntityId));
            return ResponseEntity.ok().body(new GlobalResponseDTO(true, "Service object deleted successfully", null));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Invalid service entity ID format", e.getMessage()));
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GlobalResponseDTO(false, "Database error occurred while deleting the service object", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GlobalResponseDTO(false, "An unexpected error occurred", e.getMessage()));
        }
    }

//    @Override
//    public ResponseEntity<GlobalResponseDTO> SaveEditInvoiceValuesByJobIdAndCustomerIds(Map<String, Object> request, Principal principal) {
//        String tenantId = (String) request.get("TenantId");
//        String userId = (String) request.get("UserId");
//        String jobId = (String) request.get("JobId");
//        List<Integer> customerIds = (List<Integer>) request.get("CustomerIds");
//        Map<String, Integer> services = (Map<String, Integer>) request.get("Services");
//        Map<String, Double> materials = (Map<String, Double>) request.get("Materials");
//        double laborCharge = (Double) request.get("LaborCharge");
//        double tripTravelCharge = (Double) request.get("TripTravelCharge");
//        Map<String, Double> specialCharges = (Map<String, Double>) request.get("SpecialCharges");
//        Map<String, Object> discount = (Map<String, Object>) request.get("Discount");
//        String paymentDurationId = (String) request.get("paymentTerm");
//        double deposit = (Double) request.get("Deposit");
//
//        double discountValByCal = fetchDiscountValue(tenantId,services, materials, laborCharge, tripTravelCharge, specialCharges, discount, deposit);
//        double totalAmount = calculateTotalAmount(tenantId,services, materials, laborCharge, tripTravelCharge, specialCharges, discount, deposit);
////        String paymentDurationQuery = "select jm.\"PAYMENT_DURATION\" " +
////                "from \"" + tenantId + "\".job_master jm " +
////                "where jm.\"PK_JOB_ID\" = ?";
//        //String paymentDuration = jdbcTemplate.queryForObject(paymentDurationQuery, new Object[]{Integer.parseInt(jobId)}, String.class);
//
//        String paymentDuration = paymentDurationId;
//
//        String insertInvoiceSql = "INSERT INTO \"" + tenantId + "\".\"invoice\" (\"CUSTOMER_ID\", \"JOB_ID\", \"INVOICE_NUMBER\", \"DATE\", \"PAYMENT_TERM\", \"STATUS\", \"TOTAL_AMOUNT\") VALUES (?, ?, ?, CURRENT_DATE, ?, ?, ?) RETURNING \"INVOICE_ID\"";
//        for (int customerId : customerIds) {
//            String invoiceNumber = generateInvoiceNumber(jobId, customerId,tenantId);
//            int invoiceId = jdbcTemplate.queryForObject(insertInvoiceSql, new Object[]{customerId, Integer.parseInt(jobId), invoiceNumber, paymentDuration, "Current", totalAmount}, Integer.class);
//            insertInvoiceItems(tenantId,invoiceId, services, materials,laborCharge,tripTravelCharge,specialCharges,discountValByCal,deposit,totalAmount,discount);
//        }
//
//        return ResponseEntity.ok().body(new GlobalResponseDTO(true, "Invoice Data Inserted Successfully"));
//    }

    @Override
    public ResponseEntity<GlobalResponseDTO> SaveEditInvoiceValuesByJobIdAndCustomerIds(Map<String, Object> request, Principal principal) {
        try {
            String tenantId = (String) request.get("TenantId");
            String userId = (String) request.get("UserId");
            String jobId = (String) request.get("JobId");
            List<Integer> customerIds = (List<Integer>) request.get("CustomerIds");
            Map<String, Integer> services = (Map<String, Integer>) request.get("Services");
            if(services.isEmpty()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GlobalResponseDTO(false, "Please enter atleast one service."));
            }
            Map<String, Double> materials = (Map<String, Double>) request.get("Materials");
            double laborCharge = (Double) request.get("LaborCharge");
            double tripTravelCharge = (Double) request.get("TripTravelCharge");
            Map<String, Double> specialCharges = (Map<String, Double>) request.get("SpecialCharges");
            Map<String, Object> discount = (Map<String, Object>) request.get("Discount");
            String paymentDurationId = (String) request.get("paymentTerm");
            double deposit = (Double) request.get("Deposit");
            double discountValByCal = fetchDiscountValue(tenantId, services, materials, laborCharge, tripTravelCharge, specialCharges, discount, deposit);
            double totalAmount = calculateTotalAmount(tenantId, services, materials, laborCharge, tripTravelCharge, specialCharges, discount, deposit);
            String paymentDuration = paymentDurationId;
            String insertInvoiceSql = "INSERT INTO \"" + tenantId + "\".\"invoice\" " +
                    "(\"INVOICE_ID\", \"CUSTOMER_ID\", \"JOB_ID\", \"INVOICE_NUMBER\", \"DATE\", \"PAYMENT_TERM\", \"STATUS\", \"TOTAL_AMOUNT\", \"DUE_DATE\") " +
                    "VALUES ((SELECT COALESCE(MAX(\"INVOICE_ID\"), 0) + 1 FROM \"" + tenantId + "\".\"invoice\"), ?, ?, ?, CURRENT_DATE, ?, ?, ?, ?) " +
                    "RETURNING \"INVOICE_ID\"";
            String paymentDurationFromDB = fetchPaymentDurationById(tenantId, paymentDurationId);
            Date dueDate = null;
            LocalDateTime currentTimestamp = LocalDateTime.now();
            ZonedDateTime zonedDateTime = currentTimestamp.atZone(ZoneId.systemDefault());
            Instant instant = zonedDateTime.toInstant();
            Date invdate = Date.from(instant);
            Calendar calendar = Calendar.getInstance();
            if (paymentDurationFromDB != null) {
                switch (paymentDurationFromDB) {
                    case "On Receipt":
                        dueDate = invdate;
                        break;
                    case "Next 30 days":
                        calendar.add(Calendar.DAY_OF_MONTH, 30);
                        dueDate = calendar.getTime();
                        break;
                    case "Next 60 days":
                        calendar.add(Calendar.DAY_OF_MONTH, 60);
                        dueDate = calendar.getTime();
                        break;
                    case "Next 90 days":
                        calendar.add(Calendar.DAY_OF_MONTH, 90);
                        dueDate = calendar.getTime();
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown payment duration: " + paymentDuration);
                }
            }
            for (int customerId : customerIds) {
                String invoiceNumber = generateInvoiceNumber(jobId, customerId, tenantId);
                String checkInvoiceSql = "SELECT COUNT(*) FROM \"" + tenantId + "\".\"invoice\" WHERE \"INVOICE_NUMBER\" = ?";
                Integer count = jdbcTemplate.queryForObject(checkInvoiceSql, new Object[]{invoiceNumber}, Integer.class);
                if (count != null && count > 0) {
                    return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Invoice for this customer is already generated : " + invoiceNumber));
                }
                int invoiceId = jdbcTemplate.queryForObject(insertInvoiceSql, new Object[]{customerId, Integer.parseInt(jobId), invoiceNumber, paymentDuration, "Current", totalAmount, dueDate}, Integer.class);
                insertInvoiceItems(tenantId, invoiceId, services, materials, laborCharge, tripTravelCharge, specialCharges, discountValByCal, deposit, totalAmount, discount);
            }
            return ResponseEntity.ok().body(new GlobalResponseDTO(true, "Invoice Data Inserted Successfully"));
        } catch (DuplicateKeyException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GlobalResponseDTO(false, "Duplicate invoice number. Please try again with a different number."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GlobalResponseDTO(false, "An unexpected error occurred. Please try again later."));
        }
    }

    // Other methods like fetchDiscountValue, calculateTotalAmount, generateInvoiceNumber, and insertInvoiceItems


    @Override
    public ResponseEntity<GlobalResponseDTO> updateEditInvoiceValues(Map<String, Object> request, Principal principal) throws IOException {
        String tenantId = (String) request.get("TenantId");
        String jobId = (String) request.get("JobId");
        List<Integer> customerIds = (List<Integer>) request.get("CustomerIds");
        Map<String, Integer> services = (Map<String, Integer>) request.get("Services");
        if(services.isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GlobalResponseDTO(false, "Please enter atleast one service."));
        }
        Map<String, Double> materials = (Map<String, Double>) request.get("Materials");
        double laborCharge = (Double) request.get("LaborCharge");
        double tripTravelCharge = (Double) request.get("TripTravelCharge");
        Map<String, Double> specialCharges = (Map<String, Double>) request.get("SpecialCharges");
        Map<String, Object> discount = (Map<String, Object>) request.get("Discount");
        double deposit = (Double) request.get("Deposit");
        String paymentDurationId = (String) request.get("paymentTerm");
        String paymentDuration = fetchPaymentDurationById(tenantId, paymentDurationId);
        Date dueDate = null;
        Calendar calendar = Calendar.getInstance();
        LocalDateTime currentTimestamp = LocalDateTime.now();
        ZonedDateTime zonedDateTime = currentTimestamp.atZone(ZoneId.systemDefault());
        Instant instant = zonedDateTime.toInstant();
        Date invdate = Date.from(instant);
        if (paymentDuration != null) {
            switch (paymentDuration) {
                case "On Receipt":
                    dueDate = invdate;
                    break;
                case "Next 30 days":
                    calendar.add(Calendar.DAY_OF_MONTH, 30);
                    dueDate = calendar.getTime();
                    break;
                case "Next 60 days":
                    calendar.add(Calendar.DAY_OF_MONTH, 60);
                    dueDate = calendar.getTime();
                    break;
                case "Next 90 days":
                    calendar.add(Calendar.DAY_OF_MONTH, 90);
                    dueDate = calendar.getTime();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown payment duration: " + paymentDuration);
            }
        }
        double totalAmount = calculateTotalAmount(tenantId, services, materials, laborCharge, tripTravelCharge, specialCharges, discount, deposit);
        for (int customerId : customerIds) {
            String fetchInvoiceIdSql = "SELECT \"INVOICE_ID\" FROM \"" + tenantId + "\".\"invoice\" WHERE \"CUSTOMER_ID\" = ? AND \"JOB_ID\" = ?";
            Integer invoiceId = jdbcTemplate.queryForObject(fetchInvoiceIdSql, new Object[]{customerId, Integer.parseInt(jobId)}, Integer.class);
            if (invoiceId == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new GlobalResponseDTO(false, "Invoice not found"));
            }
            String fetchInvoiceNumSql = "SELECT \"INVOICE_NUMBER\" FROM \"" + tenantId + "\".\"invoice\" WHERE \"CUSTOMER_ID\" = ? AND \"JOB_ID\" = ?";
            String invoiceNumber = jdbcTemplate.queryForObject(fetchInvoiceNumSql, new Object[]{customerId, Integer.parseInt(jobId)}, String.class);
            String fileDirectory = "src/main/resources/static/invoices";
            Path path = Paths.get(fileDirectory, invoiceNumber + ".pdf");
            if (Files.exists(path)) {
                Files.delete(path);
            }
            String updateInvoiceSql = "UPDATE \"" + tenantId + "\".\"invoice\" SET \"PAYMENT_TERM\" = ?,\"TOTAL_AMOUNT\" = ?,\"DUE_DATE\" = ?, \"DATE\" = CURRENT_DATE WHERE \"INVOICE_ID\" = ?";
            jdbcTemplate.update(updateInvoiceSql, paymentDurationId, totalAmount,dueDate,invoiceId);
            updateInvoiceItems(tenantId, invoiceId, services, materials, laborCharge, tripTravelCharge, specialCharges, discount, deposit, totalAmount);

        }
        return ResponseEntity.ok().body(new GlobalResponseDTO(true, "Invoice Data Updated Successfully"));
    }

//    private void updateInvoiceItems(String tenantId, int invoiceId, Map<String, Integer> services, Map<String, Double> materials, double laborCharge, double tripTravelCharge, Map<String, Double> specialCharges, Map<String, Object> discount, double deposit, double totalAmount) {
//        String deleteInvoiceItemsSql = "DELETE FROM \"" + tenantId + "\".\"invoice_item\" WHERE \"INVOICE_ID\" = ?";
//        jdbcTemplate.update(deleteInvoiceItemsSql, invoiceId);
//
//        String insertInvoiceItemSql = "INSERT INTO \"" + tenantId + "\".\"invoice_item\" (\"INVOICE_ID\", \"CATEGORY\", \"CATEGORY_ITEM_ID\", \"ITEM_NAME\", \"QUANTITY\", \"PRICE\", \"TOTAL\") VALUES (?, ?, ?, ?, ?, ?, ?)";
//        for (Map.Entry<String, Integer> entry : services.entrySet()) {
//            double servicePrice = getServicePrice(tenantId, entry.getKey());
//            double total = servicePrice * entry.getValue();
//            double quantity = entry.getValue();
//            String serviceName = getServiceName(tenantId,entry.getKey());
//            jdbcTemplate.update(insertInvoiceItemSql, invoiceId, "Service", Integer.parseInt(entry.getKey().replace("serviceid", "")), serviceName, quantity, servicePrice, total);
//        }
//        for (Map.Entry<String, Double> entry : materials.entrySet()) {
//            double materialPrice = getMaterialPrice(tenantId, entry.getKey());
//            double total = materialPrice * entry.getValue();
//            double quantity = entry.getValue();
//            String materialName = getMaterialName(tenantId,entry.getKey());
//            jdbcTemplate.update(insertInvoiceItemSql, invoiceId, "Material", Integer.parseInt(entry.getKey().replace("MaterialId", "")), materialName, quantity, materialPrice, total);
//        }
//        if (laborCharge > 0) {
//            jdbcTemplate.update(insertInvoiceItemSql, invoiceId, "Charge", null, "Labor Charge", 1, laborCharge, laborCharge);
//        }
//        if (tripTravelCharge > 0) {
//            jdbcTemplate.update(insertInvoiceItemSql, invoiceId, "Charge", null, "Trip Travel Charge", 1, tripTravelCharge, tripTravelCharge);
//        }
//        for (Map.Entry<String, Double> entry : specialCharges.entrySet()) {
//            jdbcTemplate.update(insertInvoiceItemSql, invoiceId, "Charge", null, entry.getKey(), 1, entry.getValue(), entry.getValue());
//        }
//        double discountValue = (Double) discount.get("DiscountValue");
//        String discountMethod = (String) discount.get("DiscountMethod");
//        if ("percentage".equalsIgnoreCase(discountMethod)) {
//            double discountAmount = totalAmount * (discountValue / 100);
//            jdbcTemplate.update(insertInvoiceItemSql, invoiceId, "Discount", null, "Discount", 1, -discountAmount, -discountAmount);
//        } else if ("FixedAmount".equalsIgnoreCase(discountMethod)) {
//            jdbcTemplate.update(insertInvoiceItemSql, invoiceId, "Discount", null, "Discount", 1, -discountValue, -discountValue);
//        }
//        jdbcTemplate.update(insertInvoiceItemSql, invoiceId, "Deposit", null, "Deposit", 1, -deposit, -deposit);
//    }
private void updateInvoiceItems(String tenantId, int invoiceId, Map<String, Integer> services, Map<String, Double> materials, double laborCharge, double tripTravelCharge, Map<String, Double> specialCharges, Map<String, Object> discountObj, double deposit, double totalAmount) {
    String deleteInvoiceItemsSql = "DELETE FROM \"" + tenantId + "\".\"invoice_item\" WHERE \"INVOICE_ID\" = ?";
    jdbcTemplate.update(deleteInvoiceItemsSql, invoiceId);

    String insertInvoiceItemSql = "INSERT INTO \"" + tenantId + "\".\"invoice_item\" " +
            "(\"INVOICE_ITEM_ID\", \"INVOICE_ID\", \"CATEGORY\", \"CATEGORY_ITEM_ID\", \"ITEM_NAME\", \"QUANTITY\", \"PRICE\", \"TOTAL\") " +
            "VALUES ((SELECT COALESCE(MAX(\"INVOICE_ITEM_ID\"), 0) + 1 FROM \"" + tenantId + "\".\"invoice_item\"), ?, ?, ?, ?, ?, ?, ?)";

    for (Map.Entry<String, Integer> entry : services.entrySet()) {
        double servicePrice = getServicePrice(tenantId, entry.getKey());
        double total = servicePrice * entry.getValue();
        double quantity = entry.getValue();
        String serviceName = getServiceName(tenantId, entry.getKey());
        jdbcTemplate.update(insertInvoiceItemSql, invoiceId, "Service", Integer.parseInt(entry.getKey().replace("serviceid", "")), serviceName, quantity, servicePrice, total);
    }

    if (materials.isEmpty()) {
        jdbcTemplate.update(insertInvoiceItemSql, invoiceId, "Material", null, "N/A", 0.0, 0.0, 0.0);
    } else {
        for (Map.Entry<String, Double> entry : materials.entrySet()) {
            double materialPrice = getMaterialPrice(tenantId, entry.getKey());
            double total = materialPrice * entry.getValue();
            double quantity = entry.getValue();
            String materialName = getMaterialName(tenantId, entry.getKey());
            jdbcTemplate.update(insertInvoiceItemSql, invoiceId, "Material", Integer.parseInt(entry.getKey().replace("MaterialId", "")), materialName, quantity, materialPrice, total);
        }
    }
    int laborQuantity = laborCharge == 0.0 ? 0 : 1;
    jdbcTemplate.update(insertInvoiceItemSql, invoiceId, "Charge", null, "Labor Charge", laborQuantity, laborCharge, laborCharge);
    int tripQuantity = tripTravelCharge == 0.0 ? 0 : 1;
    jdbcTemplate.update(insertInvoiceItemSql, invoiceId, "Charge", null, "Trip Travel Charge", tripQuantity, tripTravelCharge, tripTravelCharge);for (Map.Entry<String, Double> entry : specialCharges.entrySet()) {
        double chargeValue = entry.getValue();
        int quantity = chargeValue == 0.0 ? 0 : 1;
        jdbcTemplate.update(insertInvoiceItemSql, invoiceId, "Charge", null, entry.getKey(), quantity, chargeValue, chargeValue);
    }
    Double discountValue = discountObj.get("DiscountValue") != null ? (Double) discountObj.get("DiscountValue") : 0.0;
    String discountMethod = (String) discountObj.get("DiscountMethod");
    String discountTxt = "";
    if (discountMethod == null || discountMethod.isEmpty()) {
        discountValue=0.0;
    }
    if ("percentage".equalsIgnoreCase(discountMethod)) {
        discountTxt = "Discount (" + String.format("%.2f", discountValue) + "%)";
    } else if ("FixedAmount".equalsIgnoreCase(discountMethod)) {
        discountTxt = "Discount (₹" + String.format("%.2f", discountValue) + ")";
    }else{
        discountTxt = discountValue == 0.0 ? "0.0%" : discountValue + "%";
    }
    double discountValByCal = fetchDiscountValue(tenantId, services, materials, laborCharge, tripTravelCharge, specialCharges, discountObj, deposit);
    int discountQuantity = discountValue == 0.0 ? 0 : 1;
    jdbcTemplate.update(insertInvoiceItemSql, invoiceId, "Discount", null, discountTxt, discountQuantity, -discountValByCal, -discountValByCal);
    int depositQuantity = deposit == 0.0 ? 0 : 1;
    jdbcTemplate.update(insertInvoiceItemSql, invoiceId, "Deposit", null, "Deposit", depositQuantity, -deposit, -deposit);
}


    private double calculateTotalAmount(String tenantId, Map<String, Integer> services, Map<String, Double> materials, double laborCharge, double tripTravelCharge, Map<String, Double> specialCharges, Map<String, Object> discount, Double deposit) {
        double totalAmount = 0;
        double discountVal = 0;
        for (Map.Entry<String, Integer> entry : services.entrySet()) {
            double servicePrice = getServicePrice(tenantId, entry.getKey());
            totalAmount += servicePrice * entry.getValue();
        }
        for (Map.Entry<String, Double> entry : materials.entrySet()) {
            double materialPrice = getMaterialPrice(tenantId, entry.getKey());
            totalAmount += materialPrice * entry.getValue();
        }
        totalAmount += laborCharge;
        totalAmount += tripTravelCharge;
        for (double charge : specialCharges.values()) {
            totalAmount += charge;
        }
        Double discountValue = discount.get("DiscountValue") != null ? (Double) discount.get("DiscountValue") : 0.0;
        String discountMethod = (String) discount.get("DiscountMethod");
        if (discountMethod == null || discountMethod.isEmpty()) {
            return 0.0;
        }
        if ("percentage".equalsIgnoreCase(discountMethod)) {
            discountVal = totalAmount * (discountValue / 100);
        } else if ("FixedAmount".equalsIgnoreCase(discountMethod)) {
            discountVal = discountValue;
        }
        String fetchTaxesSql = "SELECT T.\"TAX_MASTER_NAME\", T.\"TAX_MASTER_RATE\" FROM \"" + tenantId + "\".\"tax_master\" T";
        List<Map<String, Object>> taxRates = jdbcTemplate.queryForList(fetchTaxesSql);
        double totalTaxAmount = 0;
        for (Map<String, Object> taxRate : taxRates) {
            double taxRateValue = ((Number) taxRate.get("TAX_MASTER_RATE")).doubleValue();
            totalTaxAmount += totalAmount * (taxRateValue / 100);
        }
        totalAmount += totalTaxAmount;
        totalAmount -= discountVal;
        totalAmount -= deposit;
        return totalAmount;
    }

    private double fetchDiscountValue(String tenantId, Map<String, Integer> services, Map<String, Double> materials, double laborCharge, double tripTravelCharge, Map<String, Double> specialCharges, Map<String, Object> discount, Double deposit) {
        double totalAmountTemp = 0;
        double discountVal = 0;
        for (Map.Entry<String, Integer> entry : services.entrySet()) {
            double servicePrice = getServicePrice(tenantId, entry.getKey());

            totalAmountTemp += servicePrice * entry.getValue();
        }
        for (Map.Entry<String, Double> entry : materials.entrySet()) {
            double materialPrice = getMaterialPrice(tenantId, entry.getKey());
            totalAmountTemp += materialPrice * entry.getValue();
        }
        totalAmountTemp += laborCharge;
        totalAmountTemp += tripTravelCharge;
        for (double charge : specialCharges.values()) {
            totalAmountTemp += charge;
        }
        Double discountValue = discount.get("DiscountValue") != null ? (Double) discount.get("DiscountValue") : 0.0;
        String discountMethod = (String) discount.get("DiscountMethod");
        if (discountMethod == null || discountMethod.isEmpty()) {
            return 0.0;
        }
        if ("percentage".equalsIgnoreCase(discountMethod)) {
            discountVal = totalAmountTemp * (discountValue / 100);
        } else if ("FixedAmount".equalsIgnoreCase(discountMethod)) {
            discountVal = discountValue;
        }
        return discountVal;

    }

    private void insertInvoiceItems(String tenantId,int invoiceId, Map<String, Integer> services, Map<String, Double> materials, double laborCharge, double tripTravelCharge, Map<String, Double> specialCharges, double discount, double deposit, double totalAmount,Map<String, Object> discountObj) {
        String insertInvoiceItemSql = "INSERT INTO \"" + tenantId + "\".\"invoice_item\" " +
                "(\"INVOICE_ITEM_ID\", \"INVOICE_ID\", \"CATEGORY\", \"CATEGORY_ITEM_ID\", \"ITEM_NAME\", \"QUANTITY\", \"PRICE\", \"TOTAL\") " +
                "VALUES ((SELECT COALESCE(MAX(\"INVOICE_ITEM_ID\"), 0) + 1 FROM \"" + tenantId + "\".\"invoice_item\"), ?, ?, ?, ?, ?, ?, ?)";
        for (Map.Entry<String, Integer> entry : services.entrySet()) {
            double servicePrice = getServicePrice(tenantId,entry.getKey());
            double total = servicePrice * entry.getValue();
            double quantity = entry.getValue();
            String serviceName = getServiceName(tenantId,entry.getKey());
            jdbcTemplate.update(insertInvoiceItemSql, invoiceId, "Service", Integer.parseInt(entry.getKey().replace("serviceid", "")), serviceName, quantity, servicePrice, total);
        }
        if (materials.isEmpty()) {
            jdbcTemplate.update(insertInvoiceItemSql, invoiceId, "Material", null, "N/A", 0.0, 0.0, 0.0);
        } else {
            for (Map.Entry<String, Double> entry : materials.entrySet()) {
                double materialPrice = getMaterialPrice(tenantId, entry.getKey());
                double total = materialPrice * entry.getValue();
                double quantity = entry.getValue();
                String materialName = getMaterialName(tenantId, entry.getKey());
                jdbcTemplate.update(insertInvoiceItemSql, invoiceId, "Material", Integer.parseInt(entry.getKey().replace("MaterialId", "")), materialName, quantity, materialPrice, total);
            }
        }
        int laborQuantity = laborCharge == 0.0 ? 0 : 1;
        jdbcTemplate.update(insertInvoiceItemSql, invoiceId, "Charge", null, "Labor Charge", laborQuantity, laborCharge, laborCharge);
        int tripQuantity = tripTravelCharge == 0.0 ? 0 : 1;
        jdbcTemplate.update(insertInvoiceItemSql, invoiceId, "Charge", null, "Trip Travel Charge", tripQuantity, tripTravelCharge, tripTravelCharge);for (Map.Entry<String, Double> entry : specialCharges.entrySet()) {
            double chargeValue = entry.getValue();
            int quantity = chargeValue == 0.0 ? 0 : 1;
            jdbcTemplate.update(insertInvoiceItemSql, invoiceId, "Charge", null, entry.getKey(), quantity, chargeValue, chargeValue);
        }

//        double discountValue = (Double) discountObj.get("DiscountValue");
//        String discountMethod = (String) discountObj.get("DiscountMethod");
//        String discountTxt="";
//        if ("percentage".equalsIgnoreCase(discountMethod)) {
//            discountTxt = "Discount (" + String.format("%.2f", discountValue) + "%)";
//        } else if ("FixedAmount".equalsIgnoreCase(discountMethod)) {
//            discountTxt = "Discount";
//        }

        Double discountValue = discountObj.get("DiscountValue") != null ? (Double) discountObj.get("DiscountValue") : 0.0;
        String discountMethod = (String) discountObj.get("DiscountMethod");
        String discountTxt = "";
        if (discountMethod == null || discountMethod.isEmpty()) {
            discountValue=0.0;
        }
        if ("percentage".equalsIgnoreCase(discountMethod)) {
            discountTxt = "Discount (" + String.format("%.2f", discountValue) + "%)";
        } else if ("FixedAmount".equalsIgnoreCase(discountMethod)) {
            discountTxt = "Discount (₹" + String.format("%.2f", discountValue) + ")";
        }else{
            discountTxt = discount == 0.0 ? "0.0%" : discount + "%";
        }
        int discountQuantity = discount == 0.0 ? 0 : 1;
        jdbcTemplate.update(insertInvoiceItemSql, invoiceId, "Discount", null, discountTxt, discountQuantity, -discount, -discount);
        int depositQuantity = deposit == 0.0 ? 0 : 1;
        jdbcTemplate.update(insertInvoiceItemSql, invoiceId, "Deposit", null, "Deposit", depositQuantity, -deposit, -deposit);
    }
    private String getServiceName(String tenantId,String serviceId) {
        String sql = "SELECT b.\"SERVICE_NAME\"  FROM \"" + tenantId + "\".business_type_wise_service_master b WHERE b.\"ID\"  = ?";
        try {
            int serviceIdInt = Integer.parseInt(serviceId.replace("serviceid", ""));
            return jdbcTemplate.queryForObject(sql, String.class, serviceIdInt);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid serviceId format: " + serviceId);
        }
    }

    private String getMaterialName(String tenantId,String materialId) {
        String sql = "SELECT m.\"MATERIAL_NAME\"  FROM \"" + tenantId + "\".material_master m WHERE m.\"PK_MATERIAL_ID\"  = ?";
        try {
            int materialIdInt = Integer.parseInt(materialId.replace("MaterialId", ""));
            return jdbcTemplate.queryForObject(sql, String.class, materialIdInt);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid materialId format: " + materialId);
        }
    }

    private void calculateAndInsertTaxes(int invoiceId, double totalAmount) {
        double countryTax = totalAmount * 0.02;
        double townTax = totalAmount * 0.0125;
        double otherTax = totalAmount * 0.03;
        String insertInvoiceTaxSql = "INSERT INTO hoxs3359.\"invoice_taxes\" (\"INVOICE_ID\", \"TAX_ID\", \"AMOUNT\") VALUES (?, ?, ?)";
        jdbcTemplate.update(insertInvoiceTaxSql, invoiceId, 1, countryTax);
        jdbcTemplate.update(insertInvoiceTaxSql, invoiceId, 2, townTax);
        jdbcTemplate.update(insertInvoiceTaxSql, invoiceId, 3, otherTax);
    }

    private double getServicePrice(String tenantId,String serviceId) {
        String sql = "SELECT b.\"RATE\"  FROM \"" + tenantId + "\".business_type_wise_service_master b WHERE b.\"ID\"  = ?";
        try {
            int serviceIdInt = Integer.parseInt(serviceId.replace("serviceid", ""));
            return jdbcTemplate.queryForObject(sql, Double.class, serviceIdInt);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid serviceId format: " + serviceId);
        }
    }

    private double getMaterialPrice(String tenantId,String materialId) {
        String sql = "SELECT m.\"RATE\"  FROM \"" + tenantId + "\".material_master m WHERE m.\"PK_MATERIAL_ID\"  = ?";
        try {
            int materialIdInt = Integer.parseInt(materialId.replace("materialId", ""));
            return jdbcTemplate.queryForObject(sql, Double.class, materialIdInt);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid serviceId format: " + materialId);
        }
    }

    private String generateInvoiceNumber(String jobId, int customerNumber, String tenantId) {
        String retrieveScheduleIdQuery = "SELECT \"SCHEDULE_ID\" FROM \"" + tenantId + "\".\"job_master\" WHERE \"PK_JOB_ID\" = ?";
        String scheduleId = jdbcTemplate.queryForObject(retrieveScheduleIdQuery, String.class, Integer.parseInt(jobId));
        String paddedCustomerNumber = String.format("%04d", customerNumber);
        return scheduleId + "-" + paddedCustomerNumber;
    }

    public static int countOccurrences(LocalDate startDate, LocalDate jobStopon, String recurrType) {
        long daysBetween = ChronoUnit.DAYS.between(startDate, jobStopon) + 1;
        if (recurrType.equalsIgnoreCase("day")) {
            return (int) daysBetween;
        } else if (recurrType.equalsIgnoreCase("week")) {
            return (int) Math.floorDiv(daysBetween, 7) + 1;
        } else if (recurrType.equalsIgnoreCase("month")) {
            return (int) Math.floorDiv(daysBetween, 30) + 1;
        } else if (recurrType.equalsIgnoreCase("year")) {
            return (int) Math.floorDiv(daysBetween, 365) + 1;

        }

        return 1;
    }


    public boolean checkUserMatch(String userId, String tenantId, String username) {

        String[] part = username.split(",");

        //String userPhone = profileRepository.checkAccessToken(userId, tenantId);


        String tokenUserId = part[0];
        String tokenTenantId = part[1];
        if (!tenantId.equals(tokenTenantId)) {
            return true;
        }

        return false;
    }

//    @Override
//    public ResponseEntity<Map<String, Object>> getEditInvoiceValuesByJobIdAndCustomerId(Map<String, Object> request) {
//        String tenantId = (String) request.get("TenantId");
//        String jobId = (String) request.get("JobId");
//        List<Integer> customerIds = (List<Integer>) request.get("CustomerIds");
//        Map<String, Object> response = new HashMap<>();
//        response.put("JobId", jobId);
//        response.put("CustomerIds", customerIds);
//        boolean isDataMatched = true;
//        Map<String, Integer> services = null;
//        Map<String, Double> materials = null;
//        double laborCharge = 0;
//        double tripTravelCharge = 0;
//        Map<String, Double> specialCharges = null;
//        Map<String, Object> discount = null;
//        double deposit = 0;
//        String paymentDuration="";
//
//        for (int customerId : customerIds) {
//            Map<String, Object> invoice = scheduleQuery.getInvoiceData(tenantId, customerId,jobId);
//            if (invoice == null) {
//                isDataMatched = false;
//                break;
//            }
//            Integer invoiceId = (Integer) invoice.get("INVOICE_ID");
//            String paymentTerm = (String) invoice.get("PAYMENT_TERM");
//            String fetchInvoiceItemsSql = "SELECT * FROM \"" + tenantId + "\".\"invoice_item\" WHERE \"INVOICE_ID\" = ?";
//            List<Map<String, Object>> invoiceItems = jdbcTemplate.queryForList(fetchInvoiceItemsSql, invoiceId);
//            Map<String, Integer> currentServices = new HashMap<>();
//            Map<String, Double> currentMaterials = new HashMap<>();
//            double currentLaborCharge = 0;
//            double currentTripTravelCharge = 0;
//            Map<String, Double> currentSpecialCharges = new HashMap<>();
//            Map<String, Object> currentDiscount = new HashMap<>();
//            double currentDeposit = 0;
//
//            for (Map<String, Object> item : invoiceItems) {
//                String category = (String) item.get("CATEGORY");
//                String itemName = (String) item.get("ITEM_NAME");
//                BigDecimal quantity = (BigDecimal) item.get("QUANTITY");
//                BigDecimal price = (BigDecimal) item.get("PRICE");
//
//                switch (category) {
//                    case "Service":
//                        currentServices.put(itemName, quantity.intValue());
//                        break;
//                    case "Material":
//                        currentMaterials.put(itemName, quantity.doubleValue());
//                        break;
//                    case "Charge":
//                        if ("Labor Charge".equals(itemName)) {
//                            currentLaborCharge = price.doubleValue();
//                        } else if ("Trip Travel Charge".equals(itemName)) {
//                            currentTripTravelCharge = price.doubleValue();
//                        } else {
//                            currentSpecialCharges.put(itemName, price.doubleValue());
//                        }
//                        break;
//                    case "Discount":
////                        currentDiscount.put("DiscountValue", price.negate().doubleValue());
////                        currentDiscount.put("DiscountMethod", "FixedAmount");
//                        currentDiscount.put("DiscountItemName", itemName);
//                        break;
//                    case "Deposit":
//                        currentDeposit = price.negate().doubleValue();
//                        break;
//                }
//            }
//
//            if (services == null) {
//                services = currentServices;
//                materials = currentMaterials;
//                laborCharge = currentLaborCharge;
//                tripTravelCharge = currentTripTravelCharge;
//                specialCharges = currentSpecialCharges;
//                discount = currentDiscount;
//                deposit = currentDeposit;
//                paymentDuration = paymentTerm;
//            } /*else {
//                if (!services.equals(currentServices) && !materials.equals(currentMaterials)) {
//                    isDataMatched = false;
//                    break;
//                }
//            }*/
//        }
//
//        if (!isDataMatched) {
//            response.put("CustomersDataMatched", 0);
//            response.put("message", "Data mismatch for this customer list. Please edit individually, find the correct combination, or create anew.");
//            return ResponseEntity.ok(response);
//        }
//
//        response.put("CustomersDataMatched", 1);
//        response.put("Services", services);
//        response.put("Materials", materials);
//        response.put("LaborCharge", laborCharge);
//        response.put("TripTravelCharge", tripTravelCharge);
//        response.put("SpecialCharges", specialCharges);
//        response.put("Discount", discount);
//        response.put("Deposit", deposit);
//        response.put("paymentDuration", paymentDuration);
//
//        return ResponseEntity.ok(response);
//    }

//    @Override
//    public ResponseEntity<Map<String, Object>> getEditInvoiceValuesByJobIdAndCustomerId(Map<String, Object> request) {
//        String tenantId = (String) request.get("TenantId");
//        String jobId = (String) request.get("JobId");
//        List<Integer> customerIds = (List<Integer>) request.get("CustomerIds");
//        Map<String, Object> response = new HashMap<>();
//        response.put("JobId", jobId);
//        response.put("CustomerIds", customerIds);
//        boolean isDataMatched = true;
//
//        // Initialize variables to aggregate data
//        Map<String, Integer> aggregatedServices = new HashMap<>();
//        Map<String, Double> aggregatedMaterials = new HashMap<>();
//        double aggregatedLaborCharge = 0;
//        double aggregatedTripTravelCharge = 0;
//        Map<String, Double> aggregatedSpecialCharges = new HashMap<>();
//        Map<String, Object> aggregatedDiscount = new HashMap<>();
//        double aggregatedDeposit = 0;
//        String paymentDuration = "";
//
//        for (int customerId : customerIds) {
//            Map<String, Object> invoice = scheduleQuery.getInvoiceData(tenantId, customerId, jobId);
//            if (invoice == null) {
//                response.put("message", "Invoice not created");
//                break;
//            }
//            Integer invoiceId = (Integer) invoice.get("INVOICE_ID");
//            paymentDuration = (String) invoice.get("PAYMENT_TERM");
//            String fetchInvoiceItemsSql = "SELECT * FROM \"" + tenantId + "\".\"invoice_item\" WHERE \"INVOICE_ID\" = ?";
//            List<Map<String, Object>> invoiceItems = jdbcTemplate.queryForList(fetchInvoiceItemsSql, invoiceId);
//
//            Map<String, Integer> currentServices = new HashMap<>();
//            Map<String, Double> currentMaterials = new HashMap<>();
//            double currentLaborCharge = 0;
//            double currentTripTravelCharge = 0;
//            Map<String, Double> currentSpecialCharges = new HashMap<>();
//            Map<String, Object> currentDiscount = new HashMap<>();
//            double currentDeposit = 0;
//
//            for (Map<String, Object> item : invoiceItems) {
//                String category = (String) item.get("CATEGORY");
//                String itemName = (String) item.get("ITEM_NAME");
//                BigDecimal quantity = (BigDecimal) item.get("QUANTITY");
//                BigDecimal price = (BigDecimal) item.get("PRICE");
//
//                switch (category) {
//                    case "Service":
//                        currentServices.put(itemName, quantity.intValue());
//                        break;
//                    case "Material":
//                        currentMaterials.put(itemName, quantity.doubleValue());
//                        break;
//                    case "Charge":
//                        if ("Labor Charge".equals(itemName)) {
//                            currentLaborCharge = price.doubleValue();
//                        } else if ("Trip Travel Charge".equals(itemName)) {
//                            currentTripTravelCharge = price.doubleValue();
//                        } else {
//                            currentSpecialCharges.put(itemName, price.doubleValue());
//                        }
//                        break;
//                    case "Discount":
//                        if (itemName.contains("%")) {
//                            String discountValueStr = itemName.substring(itemName.indexOf("(") + 1, itemName.indexOf("%"));
//                            currentDiscount.put("DiscountValue", Double.parseDouble(discountValueStr));
//                            currentDiscount.put("DiscountMethod", "percentage");
//                        } else if (itemName.contains("$")) {
//                            String discountValueStr = itemName.substring(itemName.indexOf("$") + 1, itemName.indexOf(")"));
//                            currentDiscount.put("DiscountValue", Double.parseDouble(discountValueStr));
//                            currentDiscount.put("DiscountMethod", "FixedAmount");
//                        }
//                        break;
//                    case "Deposit":
//                        currentDeposit = price.negate().doubleValue();
//                        break;
//                }
//            }
//
//            // Aggregating data if it's the first customer or matching data
//            if (aggregatedServices.isEmpty()) {
//                aggregatedServices = currentServices;
//                aggregatedMaterials = currentMaterials;
//                aggregatedLaborCharge = currentLaborCharge;
//                aggregatedTripTravelCharge = currentTripTravelCharge;
//                aggregatedSpecialCharges = currentSpecialCharges;
//                aggregatedDiscount = currentDiscount;
//                aggregatedDeposit = currentDeposit;
//                paymentDuration = paymentDuration;
//            } else {
//                if (!aggregatedServices.equals(currentServices) || !aggregatedMaterials.equals(currentMaterials)) {
//                    isDataMatched = false;
//                    break;
//                }
//            }
//        }
//
//        if (!isDataMatched) {
//            response.put("CustomersDataMatched", 0);
//            response.put("message", "Data mismatch for this customer list. Please edit individually, find the correct combination, or create anew.");
//        } else {
//            // Ensure the discount information is included even if no discount is present
//            if (!aggregatedDiscount.containsKey("DiscountValue")) {
//                aggregatedDiscount.put("DiscountValue", 0.0);
//                aggregatedDiscount.put("DiscountMethod", "None");
//            }
//
//            response.put("CustomersDataMatched", 1);
//            response.put("Services", aggregatedServices);
//            response.put("Materials", aggregatedMaterials);
//            response.put("LaborCharge", aggregatedLaborCharge);
//            response.put("TripTravelCharge", aggregatedTripTravelCharge);
//            response.put("SpecialCharges", aggregatedSpecialCharges);
//            response.put("Discount", aggregatedDiscount);
//            response.put("Deposit", aggregatedDeposit);
//            response.put("paymentDuration", paymentDuration);
//        }
//
//        return ResponseEntity.ok(response);
//    }

    @Override
    public ResponseEntity<Map<String, Object>> getEditInvoiceValuesByJobIdAndCustomerId(Map<String, Object> request) {
        String tenantId = (String) request.get("TenantId");
        String jobId = (String) request.get("JobId");
        List<Integer> customerIds = (List<Integer>) request.get("CustomerIds");
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("JobId", jobId);
        response.put("CustomerIds", customerIds);
        boolean isDataMatched = true;
        boolean invoiceFound = false;
        Map<String, Integer> aggregatedServices = new HashMap<>();
        Map<String, Double> aggregatedMaterials = new HashMap<>();
        double aggregatedLaborCharge = 0;
        double aggregatedTripTravelCharge = 0;
        Map<String, Double> aggregatedSpecialCharges = new HashMap<>();
        Map<String, Object> aggregatedDiscount = new HashMap<>();
        double aggregatedDeposit = 0;
        String paymentDuration = "";
        Date invDate = null;
        Date dueDate = null;
        for (int customerId : customerIds) {
            Map<String, Object> invoice = scheduleQuery.getInvoiceData(tenantId, customerId, jobId);
            if (invoice == null) {
                isDataMatched = false;
                continue;
            }
            invoiceFound = true;
            Integer invoiceId = (Integer) invoice.get("INVOICE_ID");
            paymentDuration = (String) invoice.get("PAYMENT_TERM");
            invDate = (Date) invoice.get("DATE");
            dueDate = (Date) invoice.get("DUE_DATE");
            String fetchInvoiceItemsSql = "SELECT * FROM \"" + tenantId + "\".\"invoice_item\" WHERE \"INVOICE_ID\" = ?";
            List<Map<String, Object>> invoiceItems = jdbcTemplate.queryForList(fetchInvoiceItemsSql, invoiceId);
            Map<String, Integer> currentServices = new HashMap<>();
            Map<String, Double> currentMaterials = new HashMap<>();
            double currentLaborCharge = 0;
            double currentTripTravelCharge = 0;
            Map<String, Double> currentSpecialCharges = new HashMap<>();
            Map<String, Object> currentDiscount = new HashMap<>();
            double currentDeposit = 0;

            for (Map<String, Object> item : invoiceItems) {
                String category = (String) item.get("CATEGORY");
                String itemName = (String) item.get("ITEM_NAME");
                BigDecimal quantity = (BigDecimal) item.get("QUANTITY");
                BigDecimal price = (BigDecimal) item.get("PRICE");

                switch (category) {
                    case "Service":
                        currentServices.put(itemName, quantity.intValue());
                        break;
                    case "Material":
                        currentMaterials.put(itemName, quantity.doubleValue());
                        break;
                    case "Charge":
                        if ("Labor Charge".equals(itemName)) {
                            currentLaborCharge = price.doubleValue();
                        } else if ("Trip Travel Charge".equals(itemName)) {
                            currentTripTravelCharge = price.doubleValue();
                        } else {
                            currentSpecialCharges.put(itemName, price.doubleValue());
                        }
                        break;
                    case "Discount":
                        if (itemName.contains("%")) {
                            String discountValueStr = itemName.substring(itemName.indexOf("(") + 1, itemName.indexOf("%"));
                            currentDiscount.put("DiscountValue", Double.parseDouble(discountValueStr));
                            currentDiscount.put("DiscountMethod", "percentage");
                        } else if (itemName.contains("$")) {
                            String discountValueStr = itemName.substring(itemName.indexOf("$") + 1, itemName.indexOf(")"));
                            currentDiscount.put("DiscountValue", Double.parseDouble(discountValueStr));
                            currentDiscount.put("DiscountMethod", "FixedAmount");
                        }
                        break;
                    case "Deposit":
                        currentDeposit = price.negate().doubleValue();
                        break;
                }
            }
            if (aggregatedServices.isEmpty()) {
                aggregatedServices.putAll(currentServices);
                aggregatedMaterials.putAll(currentMaterials);
                aggregatedLaborCharge = currentLaborCharge;
                aggregatedTripTravelCharge = currentTripTravelCharge;
                aggregatedSpecialCharges.putAll(currentSpecialCharges);
                aggregatedDiscount.putAll(currentDiscount);
                aggregatedDeposit = currentDeposit;
                paymentDuration = paymentDuration;
            } else {
                if (!aggregatedServices.equals(currentServices) || !aggregatedMaterials.equals(currentMaterials)) {
                    isDataMatched = false;
                    break;
                }
            }
        }
        if (!invoiceFound) {
            response.put("CustomersDataMatched", 0);
            response.put("message", "Invoice not created.");
        } else if (!isDataMatched) {
            response.put("CustomersDataMatched", 0);
            response.put("message", "Data mismatch for this customer list. Please edit individually, find the correct combination, or create anew.");
        } else {
            if (!aggregatedDiscount.containsKey("DiscountValue")) {
                aggregatedDiscount.put("DiscountValue", 0.0);
                aggregatedDiscount.put("DiscountMethod", "None");
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String invDateStr = (invDate != null) ? sdf.format(invDate) : null;
            String dueDateStr = (dueDate != null) ? sdf.format(dueDate) : null;
            response.put("CustomersDataMatched", 1);
            response.put("Services", aggregatedServices);
            response.put("Materials", aggregatedMaterials);
            response.put("LaborCharge", aggregatedLaborCharge);
            response.put("TripTravelCharge", aggregatedTripTravelCharge);
            response.put("SpecialCharges", aggregatedSpecialCharges);
            response.put("Discount", aggregatedDiscount);
            response.put("Deposit", aggregatedDeposit);
            response.put("paymentDuration", paymentDuration);
            response.put("invoiceDate",invDateStr);
            response.put("dueDate",dueDateStr);
        }
        return ResponseEntity.ok(response);
    }




    @Override
    public ResponseEntity<Map<String, Object>> createInvoicePdfByCustomers(Map<String, Object> request) throws IOException {
        String tenantId = (String) request.get("TenantId");
        String jobId = (String) request.get("JobId");
        List<Integer> customerIds = (List<Integer>) request.get("CustomerIds");
        List<Map<String, Object>> customerInvoices = new ArrayList<>();
        String invoiceNumber="";
        String fetchBusinessNameSql = "SELECT cm.\"BUSINESS_NAME\" " +
                "FROM \"Bizfns\".\"COMPANY_MASTER\" cm " +
                "WHERE cm.\"SCHEMA_ID\" = ?";
        Map<String, Object> businessName = jdbcTemplate.queryForMap(fetchBusinessNameSql, tenantId);
        for (Integer customerId : customerIds) {
            Map<String, Object> customerInvoice = new HashMap<>();
            String fetchCustomerSql = "SELECT cc.\"PK_CUSTOMER_ID\", cc.\"CUSTOMER_FIRST_NAME\", cc.\"CUSTOMER_LAST_NAME\", cc.\"customer_company_name\" FROM \"" + tenantId + "\".\"company_customer\" cc WHERE cc.\"PK_CUSTOMER_ID\" = ?";
            Map<String, Object> customerDetails = jdbcTemplate.queryForMap(fetchCustomerSql, customerId);
            customerInvoice.put("customerId", customerDetails.get("PK_CUSTOMER_ID"));
            customerInvoice.put("businessName", businessName.get("BUSINESS_NAME"));
            customerInvoice.put("customerName", customerDetails.get("CUSTOMER_FIRST_NAME") + " " + customerDetails.get("CUSTOMER_LAST_NAME"));
            String fetchServiceObjectNamesSql = "SELECT cwse.\"ANSWER\" as \"serviceObjectNames\" FROM \"" + tenantId + "\".\"customer_wise_service_entity\" cwse WHERE cwse.\"FK_CUSTOMER_ID\" = ? AND cwse.\"QUESTION\" = 'Name'";
            List<String> serviceObjectNames = jdbcTemplate.queryForList(fetchServiceObjectNamesSql, String.class, customerId);
            customerInvoice.put("ServiceObjectNames", serviceObjectNames);
            String fetchInvoiceSql = "SELECT * FROM \"" + tenantId + "\".\"invoice\" WHERE \"CUSTOMER_ID\" = ? AND \"JOB_ID\" = ?";
            List<Map<String, Object>> invoices = jdbcTemplate.queryForList(fetchInvoiceSql, customerId, Integer.parseInt(jobId));
            List<Map<String, Object>> invoiceData = new ArrayList<>();
            SimpleDateFormat timeFormatter = new SimpleDateFormat("dd-MM-yyyy");
            for (Map<String, Object> invoice : invoices) {
                Map<String, Object> invoiceDetails = new HashMap<>();
                Integer invoiceId = (Integer) invoice.get("INVOICE_ID");
                invoiceNumber = (String) invoice.get("INVOICE_NUMBER");
                invoiceDetails.put("invoiceId", invoiceNumber);
                Date invoiceDate = (Date) invoice.get("DATE");
                String paymentDurationId = (String) invoice.get("PAYMENT_TERM");
                String paymentDuration = fetchPaymentDurationById(tenantId, paymentDurationId);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(invoiceDate);
                Date dueDate = null;
                if (paymentDuration != null) {
                    switch (paymentDuration) {
                        case "On Receipt":
                            dueDate = invoiceDate;
                            break;
                        case "Next 30 days":
                            calendar.add(Calendar.DAY_OF_MONTH, 30);
                            dueDate = calendar.getTime();
                            break;
                        case "Next 60 days":
                            calendar.add(Calendar.DAY_OF_MONTH, 60);
                            dueDate = calendar.getTime();
                            break;
                        case "Next 90 days":
                            calendar.add(Calendar.DAY_OF_MONTH, 90);
                            dueDate = calendar.getTime();
                            break;
                        default:
                            throw new IllegalArgumentException("Unknown payment duration: " + paymentDuration);
                    }
                }
                String formattedInvoiceDate = timeFormatter.format(invoiceDate);
                invoiceDetails.put("invoiceDate", formattedInvoiceDate);
                String formattedDueDate = timeFormatter.format(dueDate);
                invoiceDetails.put("dueDate", formattedDueDate);
                invoiceDetails.put("invoiceDate", timeFormatter.format(invoice.get("DATE")));
                String fetchInvoiceItemsSql = "SELECT * FROM \"" + tenantId + "\".\"invoice_item\" WHERE \"INVOICE_ID\" = ?";
                List<Map<String, Object>> invoiceItems = jdbcTemplate.queryForList(fetchInvoiceItemsSql, invoiceId);
                List<Map<String, Object>> serviceItems = new ArrayList<>();
                List<Map<String, Object>> materialItems = new ArrayList<>();
                List<Map<String, Object>> discountItems = new ArrayList<>();
                List<Map<String, Object>> chargeItems = new ArrayList<>();
                double totalServiceCost = 0;
                double totalMaterialCost = 0;
                double discountAmount = 0;
                double depositAmount = 0;
                double chargeAmount = 0;
                for (Map<String, Object> item : invoiceItems) {
                    String category = (String) item.get("CATEGORY");
                    String itemName = (String) item.get("ITEM_NAME");
                    double quantity = ((Number) item.get("QUANTITY")).doubleValue();
                    double price = ((Number) item.get("PRICE")).doubleValue();
                    Map<String, Object> itemDetails = new HashMap<>();
                    itemDetails.put("ServiceRateUnit", "Hr");
                    switch (category) {
                        case "Service":
                            itemDetails.put("ServiceId", item.get("CATEGORY_ITEM_ID"));
                            itemDetails.put("ServiceName", itemName);
                            itemDetails.put("ServiceCount", quantity);
                            itemDetails.put("ServiceRate", price);
                            serviceItems.add(itemDetails);
                            totalServiceCost += price * quantity;
                            break;
                        case "Material":
                            itemDetails.put("MaterialId", item.get("CATEGORY_ITEM_ID"));
                            itemDetails.put("MaterialName", itemName);
                            itemDetails.put("MaterialCount", quantity);
                            itemDetails.put("MaterialRate", price);
                            materialItems.add(itemDetails);
                            totalMaterialCost += price * quantity;
                            break;
                        case "Discount":
                            itemDetails.put("DiscountName", itemName);
                            itemDetails.put("DiscountQuantity", quantity);
                            discountItems.add(itemDetails);
                            discountAmount += price;
                            break;
                        case "Deposit":
                            depositAmount += price;
                            break;
                        case "Charge":
                            itemDetails.put("ChargeId", item.get("CATEGORY_ITEM_ID"));
                            itemDetails.put("ChargeName", itemName);
                            itemDetails.put("ChargeCount", quantity);
                            itemDetails.put("ChargeAmount", price);
                            chargeItems.add(itemDetails);
                            chargeAmount += price * quantity;
                            break;
                    }
                }
                invoiceDetails.put("Service Names&Charges", serviceItems);
                invoiceDetails.put("Material Names&Charges", materialItems);
                invoiceDetails.put("Charges", chargeItems);
                invoiceDetails.put("Discount", discountItems);
                /*String fetchStaffDetailsSql = "SELECT cu.\"PK_USER_ID\", cu.\"USER_FIRST_NAME\", cu.\"USER_LAST_NAME\", cu.\"USER_CHARGE_RATE\" as \"StaffRate\", cu.\"USER_CHARGE_FREQUENCY\" as \"StaffWorkingUnitCount\" " +
                        "FROM \"" + tenantId + "\".\"company_user\" cu " +
                        "WHERE cu.\"PK_USER_ID\" = ANY (SELECT unnest(string_to_array(jm.\"STAFF_DETAILS\", ','))::int FROM \"" + tenantId + "\".\"job_master\" jm WHERE jm.\"PK_JOB_ID\" = ?)";
                List<Map<String, Object>> staffDetails = jdbcTemplate.queryForList(fetchStaffDetailsSql, Integer.parseInt(jobId));
                List<Map<String, Object>> staffItems = new ArrayList<>();
                double totalStaffCost = 0;
                for (Map<String, Object> staff : staffDetails) {
                    Map<String, Object> staffItem = new HashMap<>();
                    staffItem.put("StaffId", staff.get("PK_USER_ID"));
                    staffItem.put("StaffName", staff.get("USER_FIRST_NAME") + " " + staff.get("USER_LAST_NAME"));
                    double staffRate = ((Number) staff.get("StaffRate")).doubleValue();
                    staffItem.put("StaffRate", staffRate);
                    staffItem.put("StaffRateUnit", "Hr");
                    staffItem.put("StaffWorkingUnitCount", 1);  // Default count to 1
                    staffItems.add(staffItem);
                    totalStaffCost += staffRate;
                }
                invoiceDetails.put("Staff Names&Charges", staffItems);*/
                String fetchTaxesSql = "SELECT T.\"TAX_MASTER_NAME\", T.\"TAX_MASTER_RATE\", T.\"PK_TAX_MASTER_ID\" FROM \"" + tenantId + "\".\"tax_master\" T";
                List<Map<String, Object>> taxRates = jdbcTemplate.queryForList(fetchTaxesSql);
                double totalTaxCost = 0;
                String formattedTaxCost="";
                String formattedIndvTaxCost="";
                List<Map<String, Object>> taxItems = new ArrayList<>();
                double totalCost = totalServiceCost + totalMaterialCost + chargeAmount;
                String deleteInvoiceTaxesSql = "DELETE FROM \"" + tenantId + "\".\"invoice_taxes\" WHERE \"INVOICE_ID\" = ?";
                jdbcTemplate.update(deleteInvoiceTaxesSql, invoiceId);
                for (Map<String, Object> taxRate : taxRates) {
                    String taxName = (String) taxRate.get("TAX_MASTER_NAME");
                    double taxRateValue = ((Number) taxRate.get("TAX_MASTER_RATE")).doubleValue();
                    int taxId = (Integer) taxRate.get("PK_TAX_MASTER_ID");
                    double taxCost = totalCost * (taxRateValue/100);
                    Map<String, Object> taxItem = new HashMap<>();
                    taxItem.put("TaxName", taxName);
                    taxItem.put("TaxRate", taxRateValue);
                    formattedIndvTaxCost = String.format("%.2f", taxCost);
                    taxItem.put("TaxCost", formattedIndvTaxCost);
                    taxItems.add(taxItem);
                    totalTaxCost += taxCost;
                    formattedTaxCost = String.format("%.2f", totalTaxCost);
                    String maxIdSql = "SELECT COALESCE(MAX(\"INVOICE_TAX_ID\"), 0) + 1 FROM \"" + tenantId + "\".\"invoice_taxes\"";
                    Integer maxId = jdbcTemplate.queryForObject(maxIdSql, Integer.class);
                    Integer newInvoiceTaxId = maxId + 1;
                    String insertInvoiceTaxSql = "INSERT INTO \"" + tenantId + "\".\"invoice_taxes\" (\"INVOICE_TAX_ID\", \"INVOICE_ID\", \"TAX_ID\", \"AMOUNT\") VALUES (?, ?, ?, ?)";
                    jdbcTemplate.update(insertInvoiceTaxSql, newInvoiceTaxId, invoiceId, taxId, taxCost);
                }
                
                invoiceDetails.put("Taxes", taxItems);
                double totalPayableAmount = totalCost + totalTaxCost + discountAmount + depositAmount;
                BigDecimal bd = new BigDecimal(totalPayableAmount).setScale(2, RoundingMode.HALF_UP);
                double roundedTotalPayableAmount = bd.doubleValue();
                invoiceDetails.put("TotalServiceTime", serviceItems.stream().mapToDouble(item -> (Double) item.get("ServiceCount")).sum());
                invoiceDetails.put("TotalServiceCost", totalServiceCost);
                invoiceDetails.put("paymentTerm", paymentDuration);
                invoiceDetails.put("TotalDiscountAmount", discountAmount);
                invoiceDetails.put("TotalDepositAmount", depositAmount);
                invoiceDetails.put("TotalMaterialCount", materialItems.size());
                invoiceDetails.put("TotalMaterialCost", totalMaterialCost);
                invoiceDetails.put("TotalApplicableTaxCost", formattedTaxCost);
                invoiceDetails.put("TotalPayableAmount", roundedTotalPayableAmount);
                invoiceDetails.put("status", invoice.get("STATUS"));
                invoiceData.add(invoiceDetails);
            }
            customerInvoice.put("JobId", jobId);
            customerInvoice.put("invoices", invoiceData);
            customerInvoices.add(customerInvoice);
        }
        /*byte[] pdfBytes = generateInvoicePdf(customerInvoices);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=invoice.pdf");
        headers.add("Content-Type", "application/pdf");
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);*/

        byte[] pdfBytes = generateInvoicePdf(customerInvoices);
        String fileName = invoiceNumber + ".pdf";
        String fileDirectory = "src/main/resources/static/invoices";
        Path path = Paths.get(fileDirectory, fileName);

        try {
            Files.createDirectories(path.getParent());
            Files.write(path, pdfBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            String fileDownloadUri = "/users/invoices/" + fileName;
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            HttpHeaders headersres = new HttpHeaders();
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("status", "true");
            Map<String, String> da = new HashMap<>();
            da.put("url", fileDownloadUri);
            responseBody.put("data", da);
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonResponse = objectMapper.writeValueAsString(responseBody);
            boolean smsEnabled = isSmsEnabled(5,tenantId);
            /*String token = "c27KE31xRf6lyREYQfyQL3:APA91bEb49XpfXmkmrwpcQFj0ZS1uzMNzcwtbywe0bZCw8r3__3wb1KymuI2Scq4b1M6zLBLgqrJdFXJunICPWXQq-PD2mIfc2h7VPTg5EnMvVPwoEMia5nxz_uay-wT0EI4-P-r3e30";
            String phoneNo = "+919113780416";
            if (smsEnabled) {
                String message = getReminderMessage(7);
                if (message != null) {
                    //scheduler.throwMessageBasedOnScheduleStatus(phoneNo,message);
                    pushNotificationServiceForNotf.sendNotification(token, "Invoice PDF Created", message);

                } else {
                    System.err.println("No message found for reminder ID: " + 7);
                }
            }*/
            return new ResponseEntity<>(responseBody, headers, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
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

    private String fetchPaymentDurationById(String tenantId, String paymentDurationId) {
        String paymentDurationQuery = "select pt.\"PAYMENT_DURATION\" " +
                "from \"Bizfns\".\"PAYMENT_TERM\" pt " +
                "where pt.\"PAYMENT_DURATION_ID\" = ?";
        return jdbcTemplate.queryForObject(paymentDurationQuery, new Object[]{Integer.parseInt(paymentDurationId)}, String.class);
    }

    @Override
    public ResponseEntity<GlobalResponseDTO> saveTimeSheet(Map<String, Object> request,Principal principal) {
        try {
            String userInfo = principal.getName();
            String[] parts = userInfo.split(",");
            String tenantId = parts[1];
            String timesheetBillNo = (String) request.get("timesheetBillNo");
            int weekNumber = (Integer) request.get("weekNumber");
            String weekStartDateStr = (String) request.get("weekStartDate");
            String weekEndDateStr = (String) request.get("weekEndDate");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date weekStartDate = dateFormat.parse(weekStartDateStr);
            Date weekEndDate = dateFormat.parse(weekEndDateStr);
            int staffId = (Integer) request.get("staffId");
            boolean isExempt = (Boolean) request.get("isExempt");
            List<Map<String, Object>> entries = (List<Map<String, Object>>) request.get("entries");
            double totalRegularHour = (Double) request.get("totalRegularHour");
            double totalOvertimeHour = (Double) request.get("totalOvertimeHour");
            double totalRegularCost = (Double) request.get("totalRegularCost");
            double totalOvertimeCost = (Double) request.get("totalOvertimeCost");
            double totalPayableCost = (Double) request.get("totalPayableCost");
            String maxIdQuery = "SELECT COALESCE(MAX(\"Timesheet_Id\"), 0) FROM hoxs3359.timesheet";
            String insertTimeSheetSql = "INSERT INTO \"" + tenantId + "\".timesheet (" +
                    "\"Timesheet_Id\", \"Time_sheet_Bill_NO\", \"Week_Number\", \"Week_start_date\", \"Week_end_date\", " +
                    "\"Staff_id\", \"isExempt\", \"Day_of_week\", \"Date_of_week\", \"Job_events\", \"Regular_hour\", " +
                    "\"Overtime_hour\", \"Total_regular_hour\", \"Total_overtime_hour\", \"Total_regular_cost\", " +
                    "\"Total_overtime_cost\", \"Total_payable_cost\") " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)";
            for (Map<String, Object> entry : entries) {
                String dayOfWeek = (String) entry.get("dayOfWeek");
                String dateOfWeekStr = (String) entry.get("dateOfWeek");
                Date dateOfWeek = dateFormat.parse(dateOfWeekStr);
                String jobEvents = (String) entry.get("jobEvents");
                double regularHour = (Double) entry.get("regularHour");
                double overtimeHour = (Double) entry.get("overtimeHour");
                String jobEventsJson = objectMapper.writeValueAsString(jobEvents);
                Integer maxId = jdbcTemplate.queryForObject(maxIdQuery, Integer.class);
                int newTimesheetId = maxId + 1;
                jdbcTemplate.update(insertTimeSheetSql,
                        newTimesheetId, timesheetBillNo, weekNumber, weekStartDate, weekEndDate,
                        staffId, isExempt, dayOfWeek, dateOfWeek, jobEvents, regularHour, overtimeHour,
                        totalRegularHour, totalOvertimeHour, totalRegularCost, totalOvertimeCost, totalPayableCost);

            }
            GlobalResponseDTO response = new GlobalResponseDTO(true, "Timesheet saved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            GlobalResponseDTO response = new GlobalResponseDTO(false, "Failed to save timesheet: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @Autowired
    private TemplateEngine templateEngine;

    /*public byte[] generateInvoicePdf(List<Map<String, Object>> data) {
        Context context = new Context();
        context.setVariable("customerInvoices", data);
        String html = templateEngine.process("invoice", context);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, baos);
            document.open();
            com.itextpdf.text.html.simpleparser.HTMLWorker htmlWorker = new com.itextpdf.text.html.simpleparser.HTMLWorker(document);
            htmlWorker.parse(new StringReader(html));
            document.close();
            return baos.toByteArray();
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }*/

    public byte[] generateInvoicePdf(List<Map<String, Object>> data) {
        Context context = new Context();
        context.setVariable("customerInvoices", data);
        String html = templateEngine.process("invoice", context);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            com.itextpdf.text.pdf.PdfWriter writer = com.itextpdf.text.pdf.PdfWriter.getInstance(document, baos);
            document.open();
            XMLWorkerHelper.getInstance().parseXHtml(writer, document, new StringReader(html));
            document.close();
            return baos.toByteArray();
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ResponseEntity<GlobalResponseDTO> updateTimeSheet(Map<String, Object> request) {
        try {
            int timesheetId = (Integer) request.get("timesheetId");
            String dayOfWeek = (String) request.get("dayOfWeek");
            String dateOfWeekStr = (String) request.get("dateOfWeek");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date dateOfWeek = dateFormat.parse(dateOfWeekStr);
            String weekStartDateStr = (String) request.get("weekStartDate");
            String weekEndDateStr = (String) request.get("weekEndDate");
            Date weekStartDate = dateFormat.parse(weekStartDateStr);
            Date weekEndDate = dateFormat.parse(weekEndDateStr);
            List<String> jobEvents = (List<String>) request.get("jobEvents");
            double regularHour = (Double) request.get("regularHour");
            double overtimeHour = (Double) request.get("overtimeHour");
            String jobEventsJson = objectMapper.writeValueAsString(jobEvents);
            String deleteTimeSheetEntrySql = "DELETE FROM hoxs3359.timesheet WHERE \"Timesheet_Id\" = ?";
            String insertTimeSheetSql = "INSERT INTO hoxs3359.timesheet (" +
                    "\"Timesheet_Id\", \"Time_sheet_Bill_NO\", \"Week_Number\", \"Week_start_date\", \"Week_end_date\", " +
                    "\"Staff_id\", \"isExempt\", \"Day_of_week\", \"Date_of_week\", \"Job_events\", \"Regular_hour\", " +
                    "\"Overtime_hour\", \"Total_regular_hour\", \"Total_overtime_hour\", \"Total_regular_cost\", " +
                    "\"Total_overtime_cost\", \"Total_payable_cost\") " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(deleteTimeSheetEntrySql, timesheetId);
            jdbcTemplate.update(insertTimeSheetSql,
                    timesheetId, request.get("timesheetBillNo"), request.get("weekNumber"), weekStartDate,
                    weekEndDate, request.get("staffId"), request.get("isExempt"), dayOfWeek,
                    dateOfWeek, jobEventsJson, regularHour, overtimeHour,
                    request.get("totalRegularHour"), request.get("totalOvertimeHour"),
                    request.get("totalRegularCost"), request.get("totalOvertimeCost"),
                    request.get("totalPayableCost"));
            GlobalResponseDTO response = new GlobalResponseDTO(true, "Timesheet entry updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            GlobalResponseDTO response = new GlobalResponseDTO(false, "Failed to update timesheet entry: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @Override
    public ResponseEntity<GlobalResponseDTO> getTimeSheetList(Map<String, Object> request) {
        String tenantId = (String) request.get("tenantId");
        String phoneNumber = (String) request.get("phoneNumber");
        String fromDateStr = (String) request.get("fromDate");
        String toDateStr = (String) request.get("toDate");
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd"); // Adjust format as needed
        Date fromDate;
        Date toDate;
        try {
            fromDate = inputFormat.parse(fromDateStr);
            toDate = inputFormat.parse(toDateStr);
        } catch (ParseException e) {
            return ResponseEntity.status(400).body(new GlobalResponseDTO(false, "Invalid date format: " + e.getMessage()));
        }
        try {
            String userTypeQuery = "SELECT \"USER_TYPE\" FROM \"Bizfns\".\"USER_MASTER\" WHERE \"MOBILE_NUMBER\" = ?";
            List<String> userTypeResult = jdbcTemplate.queryForList(userTypeQuery, String.class, phoneNumber);
            if (userTypeResult.isEmpty()) {
                return ResponseEntity.status(404).body(new GlobalResponseDTO(false, "User not found"));
            }
            String userType = userTypeResult.get(0);
            String timesheetQuery;
            List<Map<String, Object>> timesheetData;
            if ("Company".equalsIgnoreCase(userType)) {
                timesheetQuery = "SELECT t.\"Timesheet_Id\" AS timesheetId, " +
                        "t.\"Time_sheet_Bill_NO\" AS timesheetBillNo, " +
                        "t.\"Week_Number\" AS weekNumber, " +
                        "t.\"Week_start_date\" AS weekStartDate, " +
                        "t.\"Week_end_date\" AS weekEndDate, " +
                        "t.\"Staff_id\" AS staffId, " +
                        "t.\"Date_of_week\" AS dateOfWeek,"+
                        "cu.\"USER_FIRST_NAME\" || ' ' || cu.\"USER_LAST_NAME\" AS staffName, " +
                        "cu.\"USER_PHONE_NUMBER\" AS staffPhoneNo " +
                        "FROM \"" + tenantId + "\".\"timesheet\" t " +
                        "JOIN \"" + tenantId + "\".\"company_user\" cu ON t.\"Staff_id\" = cu.\"PK_USER_ID\" " +
                        "WHERE t.\"Date_of_week\" >= ? AND t.\"Date_of_week\" <= ? " +
                        "ORDER BY t.\"Staff_id\", t.\"Date_of_week\"";
                timesheetData = jdbcTemplate.queryForList(timesheetQuery, fromDate, toDate);
                Map<Integer, List<Map<String, Object>>> groupedByStaff = new HashMap<>();
                for (Map<String, Object> row : timesheetData) {
                    Integer staffId = (Integer) row.get("staffId");
                    if (!groupedByStaff.containsKey(staffId)) {
                        groupedByStaff.put(staffId, new ArrayList<>());
                    }
                    groupedByStaff.get(staffId).add(row);
                }
                List<Map<String, Object>> responseData = new ArrayList<>();
                for (Integer staffId : groupedByStaff.keySet()) {
                    Map<String, Object> staffData = new HashMap<>();
                    List<Map<String, Object>> staffTimesheets = groupedByStaff.get(staffId);
                    if (!staffTimesheets.isEmpty()) {
                        Map<String, Object> firstEntry = staffTimesheets.get(0);
                        staffData.put("staffId", staffId);
                        staffData.put("staffName", firstEntry.get("staffName"));
                        staffData.put("staffPhoneNo", firstEntry.get("staffPhoneNo"));
                    }
                    for (Map<String, Object> timesheet : staffTimesheets) {
                        timesheet.put("weekStartDate", outputFormat.format((Date) timesheet.get("weekStartDate")));
                        timesheet.put("weekEndDate", outputFormat.format((Date) timesheet.get("weekEndDate")));
                        timesheet.put("dateOfWeek", outputFormat.format((Date) timesheet.get("dateOfWeek")));
                    }
                    staffData.put("timesheets", staffTimesheets);
                    responseData.add(staffData);
                }
                return ResponseEntity.ok(new GlobalResponseDTO(true, "Timesheets retrieved successfully", responseData));
            } else if ("Staff".equalsIgnoreCase(userType)) {
                String fetchStaffDetailsSql = "SELECT \"PK_USER_ID\" " +
                        "FROM \"" + tenantId + "\".\"company_user\" " +
                        "WHERE \"USER_PHONE_NUMBER\" = ?";
                List<Map<String, Object>> staffDetails = jdbcTemplate.queryForList(fetchStaffDetailsSql, phoneNumber);
                if (staffDetails.isEmpty()) {
                    return ResponseEntity.status(404).body(new GlobalResponseDTO(false, "Staff not found"));
                }
                int staffId = (Integer) staffDetails.get(0).get("PK_USER_ID");
                timesheetQuery = "SELECT t.\"Timesheet_Id\" AS timesheetId, " +
                        "t.\"Time_sheet_Bill_NO\" AS timesheetBillNo, " +
                        "t.\"Week_Number\" AS weekNumber, " +
                        "t.\"Week_start_date\" AS weekStartDate, " +
                        "t.\"Week_end_date\" AS weekEndDate, " +
                        "t.\"Staff_id\" AS staffId, " +
                        "t.\"Date_of_week\" AS dateOfWeek,"+
                        "cu.\"USER_FIRST_NAME\" || ' ' || cu.\"USER_LAST_NAME\" AS staffName, " +
                        "cu.\"USER_PHONE_NUMBER\" AS staffPhoneNo " +
                        "FROM \"" + tenantId + "\".\"timesheet\" t " +
                        "JOIN \"" + tenantId + "\".\"company_user\" cu ON t.\"Staff_id\" = cu.\"PK_USER_ID\" " +
                        "WHERE t.\"Staff_id\" = ? " +
                        "AND t.\"Date_of_week\" >= ? AND t.\"Date_of_week\" <= ? " +
                        "ORDER BY t.\"Date_of_week\"";
                timesheetData = jdbcTemplate.queryForList(timesheetQuery, staffId, fromDate, toDate);
                for (Map<String, Object> timesheet : timesheetData) {
                    timesheet.put("weekStartDate", outputFormat.format((Date) timesheet.get("weekStartDate")));
                    timesheet.put("weekEndDate", outputFormat.format((Date) timesheet.get("weekEndDate")));
                    timesheet.put("dateOfWeek", outputFormat.format((Date) timesheet.get("dateOfWeek")));
                }
                return ResponseEntity.ok(new GlobalResponseDTO(true, "Timesheets retrieved successfully", timesheetData));
            } else {
                return ResponseEntity.status(403).body(new GlobalResponseDTO(false, "Access denied"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new GlobalResponseDTO(false, "Failed to retrieve timesheets: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<GlobalResponseDTO> getTimeSheetByBillNoAndStaffId(Map<String, Object> request) {
        String tenantId = (String) request.get("TenantId");
        String billNo = (String) request.get("billNo");
        String staffId = (String) request.get("staffId");
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
        if (staffId == null || billNo == null) {
            return ResponseEntity.status(400).body(new GlobalResponseDTO(false, "Bill number or staff ID is missing"));
        }

        try {
            String timesheetQuery = "SELECT t.\"Timesheet_Id\" AS timesheetId, " +
                    "t.\"Time_sheet_Bill_NO\" AS timesheetBillNo, " +
                    "t.\"Week_Number\" AS weekNumber, " +
                    "t.\"Week_start_date\" AS weekStartDate, " +
                    "t.\"Week_end_date\" AS weekEndDate, " +
                    "t.\"Staff_id\" AS staffId, " +
                    "t.\"Date_of_week\" AS dateOfWeek, " +
                    "t.\"Day_of_week\" AS dayOfWeek, " +
                    "t.\"Job_events\" AS jobEvents, " +
                    "t.\"Regular_hour\" AS regularHour, " +
                    "t.\"Overtime_hour\" AS overtimeHour, " +
                    "t.\"Total_regular_hour\" AS totalRegularHour, " +
                    "t.\"Total_overtime_hour\" AS totalOvertimeHour, " +
                    "t.\"Total_regular_cost\" AS totalRegularCost, " +
                    "t.\"Total_overtime_cost\" AS totalOvertimeCost, " +
                    "t.\"Total_payable_cost\" AS totalPayableCost, " +
                    "cu.\"USER_FIRST_NAME\" || ' ' || cu.\"USER_LAST_NAME\" AS staffName, " +
                    "cu.\"USER_PHONE_NUMBER\" AS staffPhoneNo " +
                    "FROM \"" + tenantId + "\".\"timesheet\" t " +
                    "JOIN \"" + tenantId + "\".\"company_user\" cu ON t.\"Staff_id\" = cu.\"PK_USER_ID\" " +
                    "WHERE t.\"Time_sheet_Bill_NO\" = ? " +
                    "AND t.\"Staff_id\" = ?";
            List<Map<String, Object>> timesheetData = jdbcTemplate.queryForList(timesheetQuery, billNo, Integer.parseInt(staffId));
            if (timesheetData.isEmpty()) {
                return ResponseEntity.status(404).body(new GlobalResponseDTO(false, "No timesheet found for this bill number and staff ID"));
            }
            Map<String, Object> response = new HashMap<>();
            Map<String, Object> firstEntry = timesheetData.get(0);
            response.put("timesheetId", firstEntry.get("timesheetId"));
            response.put("timesheetBillNo", firstEntry.get("timesheetBillNo"));
            response.put("weekNumber", firstEntry.get("weekNumber"));
            response.put("weekStartDate",outputFormat.format(firstEntry.get("weekStartDate")));
            response.put("weekEndDate", outputFormat.format(firstEntry.get("weekEndDate")));
            response.put("timeSheetStatus", "Submitted");
            List<Map<String, Object>> entries = new ArrayList<>();
            double totalRegularHour = 0;
            double totalOvertimeHour = 0;
            double totalRegularCost = 0;
            double totalOvertimeCost = 0;
            double totalPayableCost = 0;
            for (Map<String, Object> row : timesheetData) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("dateOfWeek", outputFormat.format((Date) row.get("dateOfWeek")));
                entry.put("jobEvents", row.get("jobEvents"));
                entry.put("regularHour", row.get("regularHour"));
                entry.put("overtimeHour", row.get("overtimeHour"));
                entries.add(entry);
                totalRegularHour += ((BigDecimal) row.get("totalRegularHour")).doubleValue();
                totalOvertimeHour += ((BigDecimal) row.get("totalOvertimeHour")).doubleValue();
                totalRegularCost += ((BigDecimal) row.get("totalRegularCost")).doubleValue();
                totalOvertimeCost += ((BigDecimal) row.get("totalOvertimeCost")).doubleValue();
                totalPayableCost += ((BigDecimal) row.get("totalPayableCost")).doubleValue();
            }
            response.put("timeSheetData", entries);
            response.put("totalRegularHour", totalRegularHour);
            response.put("totalOvertimeHour", totalOvertimeHour);
            response.put("totalRegularCost", totalRegularCost);
            response.put("totalOvertimeCost", totalOvertimeCost);
            response.put("totalPayableCost", totalPayableCost);
            response.put("staffDetails", Map.of(
                    "staffId", staffId,
                    "staffName", firstEntry.get("staffName"),
                    "staffPhoneNo", firstEntry.get("staffPhoneNo")
            ));
            return ResponseEntity.ok(new GlobalResponseDTO(true, "Timesheet details retrieved successfully", response));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new GlobalResponseDTO(false, "Failed to retrieve timesheet details: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<GlobalResponseDTO> getInvoiceListsByJobId(Map<String, Object> request, Principal principal) {
        String tenantId = (String) request.get("tenantId");
        String userId = (String) request.get("userId");
        String jobId = (String) request.get("jobId");
        if (checkUserMatch(userId, tenantId, principal.getName())) {
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorized user."));
        }
        String fetchInvoiceListSql = "SELECT i.\"INVOICE_NUMBER\", i.\"JOB_ID\",i.\"STATUS\", i.\"CUSTOMER_ID\", cc.\"CUSTOMER_FIRST_NAME\", cc.\"CUSTOMER_FIRST_NAME\", cc.\"CUSTOMER_LAST_NAME\" " +
                "FROM \"" + tenantId + "\".\"invoice\" i " +
                "JOIN \"" + tenantId + "\".\"company_customer\" cc ON i.\"CUSTOMER_ID\" = cc.\"PK_CUSTOMER_ID\" " +
                "WHERE i.\"JOB_ID\" = ?";
        List<Map<String, Object>> invoiceList = jdbcTemplate.queryForList(fetchInvoiceListSql, Integer.parseInt(jobId));
        if (invoiceList.isEmpty()) {
            return ResponseEntity.ok().body(new GlobalResponseDTO(false, "No invoices found for the specified Job ID."));
        }
        List<Map<String, String>> invoicesFormatted = new ArrayList<>();
        for (Map<String, Object> invoice : invoiceList) {
            Map<String, String> formattedInvoice = new HashMap<>();
            formattedInvoice.put("invoiceNumber", invoice.get("INVOICE_NUMBER").toString());
            formattedInvoice.put("customerFirstName", invoice.get("CUSTOMER_FIRST_NAME").toString());
            formattedInvoice.put("customerLastName", invoice.get("CUSTOMER_LAST_NAME").toString());
            formattedInvoice.put("jobId", invoice.get("JOB_ID").toString());
            formattedInvoice.put("customerId", invoice.get("CUSTOMER_ID").toString());
            formattedInvoice.put("invoiceStatus", invoice.get("STATUS").toString());
            invoicesFormatted.add(formattedInvoice);
        }
        return ResponseEntity.ok().body(new GlobalResponseDTO(true, "Invoice list fetched successfully.", invoicesFormatted));
    }

    @Override
    public ResponseEntity<GlobalResponseDTO> saveMaterialUnit(Map<String, Object> request, Principal principal) {
        String tenantId = (String) request.get("tenantId");
        String categoryId = (String) request.get("category_id");
        String unitName = (String) request.get("unit_name");

        if (checkUserMatch((String) request.get("userId"), tenantId, principal.getName())) {
            return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorized user, we could not access the APIs from others' tokens"));
        }

        try {
            scheduleQuery.saveMaterialUnit(tenantId, unitName);
            return ResponseEntity.ok(new GlobalResponseDTO(true, "Success"));
        } catch (Exception e) {
            e.printStackTrace();
            errorLogService.errorLog(request, e.getMessage(), "", principal.getName(), "");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GlobalResponseDTO(false, "Failed to save or update material unit"));
        }
    }

    @Override
    public Map<String, String> getWorkingHours(Principal principal) {
        String userInfo = principal.getName();
        String[] parts = userInfo.split(",");
        String mobileNumber = parts[0];
        String tenentId = parts[1];
        String sqlCheck = "SELECT \"START_TIME\", \"END_TIME\", \"START_DATE_WORKING_HOUR\" " +
                          "FROM \"Bizfns\".\"COMPANY_TIME_INTERVAL\" " +
                          "WHERE \"TENANT_ID\" = ? AND \"USER_ID\" = ? " +
                          "AND \"START_DATE_WORKING_HOUR\" IS NOT NULL " +
                          "ORDER BY \"START_DATE_WORKING_HOUR\" DESC " +
                          "LIMIT 1";
        List<Map<String, Object>> workingHoursList = jdbcTemplate.queryForList(sqlCheck, tenentId, mobileNumber);
        Map<String, String> workingHoursResponse = new HashMap<>();
        if (workingHoursList.size() > 0 && workingHoursList.get(0).get("START_TIME") != null) {
            Map<String, Object> latestWorkingHour = workingHoursList.get(0);
            workingHoursResponse.put("fromDate", String.valueOf(latestWorkingHour.get("START_DATE_WORKING_HOUR")));
            workingHoursResponse.put("start", String.valueOf(latestWorkingHour.get("START_TIME")));
            workingHoursResponse.put("end", String.valueOf(latestWorkingHour.get("END_TIME")));
        } else {
            workingHoursResponse.put("start", "10");
            workingHoursResponse.put("end", "17");
        }
        return workingHoursResponse;
    }

}

