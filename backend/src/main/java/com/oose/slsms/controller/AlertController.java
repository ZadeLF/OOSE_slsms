package com.oose.slsms.controller;

import com.oose.slsms.observer.AdminPushChannel;
import com.oose.slsms.observer.AlertEvent;
import com.oose.slsms.observer.DigitalSignageChannel;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AdminPushChannel adminPush;
    private final DigitalSignageChannel signage;

    public AlertController(AdminPushChannel adminPush, DigitalSignageChannel signage) {
        this.adminPush = adminPush;
        this.signage = signage;
    }

    /** Recent admin-push alerts (newest first). */
    @GetMapping("/recent")
    public List<AlertEvent> recent() {
        return adminPush.recent();
    }

    /** What the digital sign for this zone is currently showing. */
    @GetMapping("/signage/{zoneId}")
    public Map<String, String> signage(@PathVariable String zoneId) {
        return Map.of("zoneId", zoneId, "message", signage.messageFor(zoneId));
    }
}
