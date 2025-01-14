package com.bizfns.services.Notification;

import com.bizfns.services.GlobalDto.GlobalResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.*;

@Service
public class NotificationServiceImpl implements NotificationService{
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Override
    public ResponseEntity<GlobalResponseDTO> getNotificationMaster(Principal principal) {
        String userInfo = principal.getName();
        String[] parts = userInfo.split(",");
        String tenantId = parts[1];
        List<Map<String, Object>> reminderEventDetailsList = getReminderEventDetailsData(tenantId);
        if (reminderEventDetailsList == null || reminderEventDetailsList.isEmpty()) {
            return ResponseEntity.ok(new GlobalResponseDTO(true, "No reminder event details data found", null));
        }
        Map<String, Map<String, Object>> eventsMap = new HashMap<>();
        for (Map<String, Object> reminderEventDetail : reminderEventDetailsList) {
            String eventName = (String) reminderEventDetail.get("EVENT_NAME");
            Integer reminderId = (Integer) reminderEventDetail.get("REMINDER_ID");
            Integer eventId = (Integer) reminderEventDetail.get("EVENT_ID");
            Map<String, Object> eventMap = eventsMap.computeIfAbsent(eventName, name -> {
                Map<String, Object> newEvent = new HashMap<>();
                newEvent.put("eventName", eventName);
                newEvent.put("eventId", eventId);
                newEvent.put("reminders", new ArrayList<Map<String, Object>>());
                return newEvent;
            });
            Map<String, Object> reminder = new HashMap<>();
            reminder.put("reminderId", reminderId.toString());
            reminder.put("reminder", reminderEventDetail.get("REMINDER_NAME"));
            reminder.put("sms", reminderEventDetail.get("SMS"));
            reminder.put("push", reminderEventDetail.get("PUSH"));
            ((List<Map<String, Object>>) eventMap.get("reminders")).add(reminder);
        }
        List<Map<String, Object>> eventsList = new ArrayList<>(eventsMap.values());
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("events", eventsList);
        GlobalResponseDTO response = new GlobalResponseDTO(true, "Reminder event data fetched successfully", responseMap);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<GlobalResponseDTO> saveNotificationMaster(List<Map<String, Object>> notificationData, Principal principal) {
        String userInfo = principal.getName();
        String[] parts = userInfo.split(",");
        String tenantId = parts[1];
        for (Map<String, Object> notification : notificationData) {
            String reminderId = (String) notification.get("reminderId");
            Boolean sms = (Boolean) notification.get("sms");
            Boolean push = (Boolean) notification.get("push");
            updateReminderEventDetails(reminderId, sms, push, tenantId);
        }
        return ResponseEntity.ok(new GlobalResponseDTO(true, "Notification master updated successfully", null));
    }

    private void updateReminderEventDetails(String reminderId, Boolean sms, Boolean push,String tenantId) {
        String updateQuery = "UPDATE \"" + tenantId + "\".reminder_event_details " +
                "SET \"SMS\" = ?, \"PUSH\" = ? " +
                "WHERE \"REMINDER_ID\" = ? ";
        jdbcTemplate.update(updateQuery, sms, push,Integer.parseInt(reminderId));
    }

    private List<Map<String, Object>> getReminderEventDetailsData(String tenantId) {
        String query = "SELECT notif.\"EVENT_ID\", notif.\"EVENT_NAME\", rem.\"REMINDER_ID\", rem.\"REMINDER_NAME\", rem.\"REMINDER_MESSAGE\", det.\"SMS\", det.\"PUSH\", det.\"FEEDBACK_REQUEST\", rem.\"CREATED\", rem.\"UPDATED\" " +
                "FROM \"Bizfns\".\"NOTIFICATION_EVENT_MASTER\" notif " +
                "JOIN \"Bizfns\".\"REMINDER_EVENT_MASTER\" rem ON notif.\"EVENT_ID\" = rem.\"FK_EVENT_ID\" " +
                "JOIN \"" +tenantId+ "\".\"reminder_event_details\" det ON rem.\"REMINDER_ID\" = det.\"REMINDER_ID\"";
        return jdbcTemplate.queryForList(query);
    }
}
