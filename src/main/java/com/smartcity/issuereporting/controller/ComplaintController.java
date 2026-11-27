package com.smartcity.issuereporting.controller;

import com.smartcity.issuereporting.dto.Dtos.ComplaintDto;
import com.smartcity.issuereporting.model.Category;
import com.smartcity.issuereporting.service.ComplaintService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintService service;

    public ComplaintController(ComplaintService service) { this.service = service; }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ComplaintDto> create(
            @RequestParam String title,
            @RequestParam Category category,
            @RequestParam String description,
            @RequestParam String address,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) MultipartFile image,
            Authentication auth) {
        if (title.isBlank() || description.isBlank() || address.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title, description and location are required");
        }
        if (title.length() > 150 || description.length() > 3000 || address.length() > 255) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One of the fields is too long");
        }
        if ((latitude != null && (latitude < -90 || latitude > 90)) || (longitude != null && (longitude < -180 || longitude > 180))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid coordinates");
        }
        ComplaintDto dto = service.create(auth.getName(), title, category, description, address, latitude, longitude, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/mine")
    public List<ComplaintDto> mine(Authentication auth) { return service.listMine(auth.getName()); }

    @GetMapping("/{code}")
    public ComplaintDto track(@PathVariable String code, Authentication auth) {
        return service.getByCode(code, auth.getName());
    }
}
