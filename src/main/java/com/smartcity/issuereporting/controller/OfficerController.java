package com.smartcity.issuereporting.controller;

import com.smartcity.issuereporting.dto.Dtos.ComplaintDto;
import com.smartcity.issuereporting.dto.Dtos.StatusRequest;
import com.smartcity.issuereporting.service.ComplaintService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Field-officer endpoints: only complaints assigned to the logged-in officer. */
@RestController
@RequestMapping("/api/officer")
public class OfficerController {

    private final ComplaintService complaints;

    public OfficerController(ComplaintService complaints) { this.complaints = complaints; }

    @GetMapping("/complaints")
    public List<ComplaintDto> assigned(Authentication auth) { return complaints.listAssigned(auth.getName()); }

    @PutMapping("/complaints/{id}/status")
    public ComplaintDto status(@PathVariable Long id, @Valid @RequestBody StatusRequest r, Authentication auth) {
        return complaints.updateStatus(id, r, auth.getName());
    }
}
