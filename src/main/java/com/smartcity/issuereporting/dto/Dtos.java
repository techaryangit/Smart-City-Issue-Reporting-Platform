package com.smartcity.issuereporting.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.smartcity.issuereporting.model.Priority;
import com.smartcity.issuereporting.model.Status;

import java.time.LocalDateTime;
import java.util.List;

/** All request / response shapes of the REST API, grouped in one place. */
public final class Dtos {
    private Dtos() {}

    // ---------- requests ----------
    public record RegisterRequest(
            @NotBlank @Size(max = 100) String fullName,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 64, message = "Password must be 8-64 characters") String password,
            @Size(max = 20) String phone,
            @Size(max = 255) String address) {}

    public record LoginRequest(@NotBlank String email, @NotBlank String password) {}

    public record AssignRequest(@NotNull Long departmentId, Long officerId, Priority priority) {}

    public record StatusRequest(@NotNull Status status, @Size(max = 500) String remark) {}

    public record OfficerRequest(
            @NotBlank String fullName,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 64) String password,
            @NotNull Long departmentId) {}

    public record DepartmentRequest(@NotBlank @Size(max = 100) String name, @Size(max = 255) String description) {}

    public record ProfileRequest(@NotBlank @Size(max = 100) String fullName, @Size(max = 20) String phone, @Size(max = 255) String address) {}

    public record PasswordRequest(@NotBlank String currentPassword, @NotBlank @Size(min = 8, max = 64) String newPassword) {}

    // ---------- responses ----------
    public record UserDto(Long id, String fullName, String email, String phone, String address,
                          String role, Long departmentId, String departmentName,
                          boolean enabled, LocalDateTime createdAt) {}

    public record DepartmentDto(Long id, String name, String description) {}

    public record UpdateDto(String status, String remark, String updatedBy, LocalDateTime createdAt) {}

    public record ComplaintDto(Long id, String complaintCode, String title, String description,
                               String category, String status, String priority, String address,
                               Double latitude, Double longitude, String imageUrl,
                               String citizenName, String citizenEmail,
                               Long departmentId, String departmentName,
                               Long officerId, String officerName,
                               LocalDateTime createdAt, LocalDateTime updatedAt,
                               List<UpdateDto> history) {}

    public record NotificationDto(Long id, String message, String complaintCode, boolean read, LocalDateTime createdAt) {}
}
