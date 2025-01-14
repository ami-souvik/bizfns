package com.bizfns.services.SmsService;

import com.bizfns.services.Notification.PushNotificationServiceForNotf;
import com.bizfns.services.Query.ScheduleQuery;
import com.bizfns.services.Service.SchemaService;
import com.twilio.Twilio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class Scheduler {

    @Autowired
    private final ScheduleQuery scheduleQuery;
    @Autowired
    private final SmsGateway smsGateway;
    @Autowired
    SchemaService schemaService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    PushNotificationServiceForNotf pushNotificationServiceForNotf;

    @Value("${twilio.account.sid}")
    private String twilioAccountSid;

    @Value("${twilio.auth.token}")
    private String twilioAuthToken;

    @Value("${twilio.phone.number}")
    private String twilioPhoneNumber;

    public Scheduler(ScheduleQuery scheduleQuery, SmsGateway smsGateway) {
        this.scheduleQuery = scheduleQuery;
        this.smsGateway = smsGateway;
    }
    @PostConstruct
    private void init() {
        Twilio.init(twilioAccountSid, twilioAuthToken);
    }
    @Scheduled(fixedDelay = 60000)
    public void checkScheduledJobs() {
        List<String> schemas = schemaService.getSchemaNames();
        for (String schema : schemas) {
            List<Map<String, Object>> scheduledJobs = scheduleQuery.getScheduledJobs(schema);
            for (Map<String, Object> job : scheduledJobs) {
                String jobId = convertToString(job.get("PK_JOB_ID"));
                String jobStartTime = convertToString(job.get("JOB_START_TIME"));
                String jobEndTime = convertToString(job.get("JOB_END_TIME"));
                String jobDate = convertToString(job.get("JOB_DATE"));
                String notificationType = isJobDue(jobDate, jobStartTime, jobEndTime);
                int reminderId = getReminderIdForNotificationType(notificationType);
                //String token = "c27KE31xRf6lyREYQfyQL3:APA91bEb49XpfXmkmrwpcQFj0ZS1uzMNzcwtbywe0bZCw8r3__3wb1KymuI2Scq4b1M6zLBLgqrJdFXJunICPWXQq-PD2mIfc2h7VPTg5EnMvVPwoEMia5nxz_uay-wT0EI4-P-r3e30";
                if (reminderId != -1) {
                    boolean sendSms = shouldSendSms(schema, reminderId);
                    if (sendSms) {
                        String message = getReminderMessage(schema, reminderId);
                        if (message != null) {
                            //smsGateway.sendSms(customerPhoneNumber, message);
                            //pushNotificationServiceForNotf.sendNotification(token, "Schedule Creation", message);
                            // Step 1: Fetch staff details (comma-separated staff IDs) from the job_master table
                            String staffQuery = "SELECT \"STAFF_DETAILS\" FROM \"maxt5963\".\"job_master\" WHERE \"PK_JOB_ID\" = ?";
                            String staffDetails = jdbcTemplate.queryForObject(staffQuery, new Object[]{Integer.parseInt(jobId)}, String.class);
                            if (staffDetails != null && !staffDetails.isEmpty()) {
                                String[] staffIds = staffDetails.split(",");
                                String staffPhoneQuery = "SELECT \"USER_PHONE_NUMBER\", \"PK_USER_ID\" FROM \"" + schema + "\".\"company_user\" WHERE \"PK_USER_ID\" IN (" + String.join(",", staffIds) + ")";
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

                            String userQuery = "SELECT um.\"MOBILE_NUMBER\", fcm.\"FCM_TOKEN\"" +
                                    "FROM \"Bizfns\".\"USER_MASTER\" um " +
                                    "JOIN \"Bizfns\".\"FCM_TOKEN\" fcm ON um.\"MOBILE_NUMBER\" = fcm.\"USER_ID\"" +
                                    "WHERE um.\"SCHEMA_NAME\" = ? AND um.\"USER_TYPE\" = 'Company'";
                            List<Map<String, Object>> userDetails = jdbcTemplate.query(userQuery, new Object[]{schema}, (rs, rowNum) -> {
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
                                }*/
                            }
                        }
                    }
                }
            }
        }
    }

    public void throwMessageBasedOnScheduleStatus(String phNumber , String smsMsg) {
            smsGateway.sendSms(phNumber, smsMsg);
        }
    private String convertToString(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof String) {
            return (String) value;
        }
        return String.valueOf(value);
    }
    private String isJobDue(String jobDate, String jobStartTime, String jobEndTime) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime jobDateTime,jobCompTime;
        try {
            jobDateTime = LocalDateTime.parse(jobDate + " " + jobStartTime, formatter);
            jobCompTime = LocalDateTime.parse(jobDate + " " + jobEndTime, formatter);
        } catch (DateTimeParseException e) {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            jobDateTime = LocalDateTime.parse(jobDate + " " + jobStartTime, formatter);
            jobCompTime = LocalDateTime.parse(jobDate + " " + jobEndTime, formatter);

        }
        LocalDateTime oneHourAfter = now.plusHours(1);
        LocalDateTime oneDayAfter = now.plusDays(1);
        if (now.equals(jobDateTime)){
            return "ScheduledDate";
        } else if (jobDateTime.isBefore(now) && jobDateTime.isAfter(now.minusMinutes(1))){
            return "Started";
        } else if ((jobDateTime).isAfter(now) && (jobDateTime.isAfter(oneHourAfter) && jobDateTime.isBefore(oneHourAfter.plusMinutes(1)))) {
            return "HourBefore";
        } else if ((jobDateTime).isAfter(now) && (jobDateTime.isAfter(oneDayAfter) && jobDateTime.isBefore(oneDayAfter.plusMinutes(1)))) {
            return "DayBefore";
        } else if (jobCompTime.isBefore(now) && jobCompTime.isAfter(now.minusMinutes(1))) {
            return "Completed";
        } else {
            return "NotDue";
        }
    }

    private int getReminderIdForNotificationType(String notificationType) {
        switch (notificationType) {
            case "DayBefore":
                return 1;
            case "HourBefore":
                return 2;
            case "Started":
                return 3;
            case "Completed":
                return 4;
            default:
                return -1;
        }
    }
    private boolean shouldSendSms(String schema, int reminderId) {
        String sql = "SELECT \"SMS\" FROM \"" + schema + "\".\"reminder_event_details\" WHERE \"REMINDER_ID\" = ?";
        Boolean smsFlag = jdbcTemplate.queryForObject(sql, Boolean.class, reminderId);
        return smsFlag != null && smsFlag;
    }
    private String getReminderMessage(String schema, int reminderId) {
        String sql = "SELECT \"REMINDER_MESSAGE\" FROM \"Bizfns\".\"REMINDER_EVENT_MASTER\" WHERE \"REMINDER_ID\" = ?";
        return jdbcTemplate.queryForObject(sql, String.class, reminderId);
    }
}