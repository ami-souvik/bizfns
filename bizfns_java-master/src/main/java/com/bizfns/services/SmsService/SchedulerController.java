package com.bizfns.services.SmsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class SchedulerController {

    @Autowired
    private final Scheduler scheduler;

    @Autowired
    public SchedulerController(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @GetMapping("/checkScheduledJobs")
    public String checkScheduledJobs() {
        try {
            scheduler.checkScheduledJobs();
            return "Scheduled jobs check completed successfully.";
        } catch (Exception e) {
            e.printStackTrace();
            return "An error occurred while checking scheduled jobs.";
        }
    }
}
