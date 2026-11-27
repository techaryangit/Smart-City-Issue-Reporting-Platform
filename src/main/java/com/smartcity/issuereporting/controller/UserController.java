package com.smartcity.issuereporting.controller;

import com.smartcity.issuereporting.dto.Dtos.*;
import com.smartcity.issuereporting.service.NotificationService;
import com.smartcity.issuereporting.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Profile, notifications and the (read-only) department list used by every role. */
@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final NotificationService notificationService;

    public UserController(UserService userService, NotificationService notificationService) {
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @GetMapping("/profile")
    public UserDto profile(Authentication auth) { return userService.me(auth.getName()); }

    @PutMapping("/profile")
    public UserDto updateProfile(@Valid @RequestBody ProfileRequest r, Authentication auth) {
        return userService.updateProfile(auth.getName(), r);
    }

    @PutMapping("/profile/password")
    public Map<String, String> changePassword(@Valid @RequestBody PasswordRequest r, Authentication auth) {
        userService.changePassword(auth.getName(), r);
        return Map.of("message", "Password updated");
    }

    @GetMapping("/notifications")
    public List<NotificationDto> notifications(Authentication auth) { return notificationService.list(auth.getName()); }

    @PostMapping("/notifications/read-all")
    public Map<String, String> readAll(Authentication auth) {
        notificationService.markAllRead(auth.getName());
        return Map.of("message", "Marked as read");
    }

    @GetMapping("/departments")
    public List<DepartmentDto> departments() { return userService.listDepartments(); }
}
