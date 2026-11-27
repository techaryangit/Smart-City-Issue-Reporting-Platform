package com.smartcity.issuereporting.controller;

import com.smartcity.issuereporting.dto.Dtos.*;
import com.smartcity.issuereporting.model.Category;
import com.smartcity.issuereporting.model.Priority;
import com.smartcity.issuereporting.model.Status;
import com.smartcity.issuereporting.service.ComplaintService;
import com.smartcity.issuereporting.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/** Everything under /api/admin requires the ADMIN role (see SecurityConfig). */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ComplaintService complaints;
    private final UserService users;

    public AdminController(ComplaintService complaints, UserService users) {
        this.complaints = complaints;
        this.users = users;
    }

    // ----- complaints -----
    @GetMapping("/complaints")
    public List<ComplaintDto> list(@RequestParam(required = false) Category category,
                                   @RequestParam(required = false) Status status,
                                   @RequestParam(required = false) Priority priority,
                                   @RequestParam(required = false) String q) {
        return complaints.search(category, status, priority, q);
    }

    @GetMapping("/complaints/{id}")
    public ComplaintDto detail(@PathVariable Long id) {
        return complaints.getById(id);
    }

    @PutMapping("/complaints/{id}/assign")
    public ComplaintDto assign(@PathVariable Long id, @Valid @RequestBody AssignRequest r, Authentication auth) {
        return complaints.assign(id, r, auth.getName());
    }

    @PutMapping("/complaints/{id}/status")
    public ComplaintDto status(@PathVariable Long id, @Valid @RequestBody StatusRequest r, Authentication auth) {
        return complaints.updateStatus(id, r, auth.getName());
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() { return complaints.stats(); }

    @GetMapping("/report.csv")
    public ResponseEntity<byte[]> report(@RequestParam(required = false) Category category,
                                         @RequestParam(required = false) Status status,
                                         @RequestParam(required = false) Priority priority,
                                         @RequestParam(required = false) String q) {
        byte[] body = complaints.exportCsv(category, status, priority, q).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=complaints-report.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(body);
    }

    // ----- users / officers / departments -----
    @GetMapping("/users")
    public List<UserDto> users() { return users.listAll(); }

    @PutMapping("/users/{id}/enabled")
    public UserDto setEnabled(@PathVariable Long id, @RequestParam boolean value, Authentication auth) {
        return users.setEnabled(id, value, auth.getName());
    }

    @GetMapping("/officers")
    public List<UserDto> officers(@RequestParam(required = false) Long departmentId) {
        return users.listOfficers(departmentId);
    }

    @PostMapping("/officers")
    public ResponseEntity<UserDto> createOfficer(@Valid @RequestBody OfficerRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(users.createOfficer(r));
    }

    @PostMapping("/departments")
    public ResponseEntity<DepartmentDto> createDepartment(@Valid @RequestBody DepartmentRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(users.createDepartment(r));
    }
}
