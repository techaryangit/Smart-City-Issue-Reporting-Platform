package com.smartcity.issuereporting.service;

import com.smartcity.issuereporting.dto.Dtos.*;
import com.smartcity.issuereporting.model.Department;
import com.smartcity.issuereporting.model.Role;
import com.smartcity.issuereporting.model.User;
import com.smartcity.issuereporting.repository.DepartmentRepository;
import com.smartcity.issuereporting.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService {

    private final UserRepository users;
    private final DepartmentRepository departments;
    private final PasswordEncoder encoder;

    public UserService(UserRepository users, DepartmentRepository departments, PasswordEncoder encoder) {
        this.users = users;
        this.departments = departments;
        this.encoder = encoder;
    }

    public static UserDto toDto(User u) {
        Department d = u.getDepartment();
        return new UserDto(u.getId(), u.getFullName(), u.getEmail(), u.getPhone(), u.getAddress(),
                u.getRole().name(), d == null ? null : d.getId(), d == null ? null : d.getName(),
                u.isEnabled(), u.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public UserDto me(String email) {
        return toDto(find(email));
    }

    public User find(String email) {
        return users.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    @Transactional
    public UserDto register(RegisterRequest r) {
        String email = r.email().trim().toLowerCase();
        if (users.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        }
        User u = new User();
        u.setFullName(r.fullName().trim());
        u.setEmail(email);
        u.setPassword(encoder.encode(r.password()));
        u.setPhone(r.phone());
        u.setAddress(r.address());
        u.setRole(Role.CITIZEN);
        return toDto(users.save(u));
    }

    @Transactional
    public UserDto updateProfile(String email, ProfileRequest r) {
        User u = find(email);
        u.setFullName(r.fullName().trim());
        u.setPhone(r.phone());
        u.setAddress(r.address());
        return toDto(users.save(u));
    }

    @Transactional
    public void changePassword(String email, PasswordRequest r) {
        User u = find(email);
        if (!encoder.matches(r.currentPassword(), u.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }
        u.setPassword(encoder.encode(r.newPassword()));
        users.save(u);
    }

    // ---------- admin operations ----------
    @Transactional(readOnly = true)
    public List<UserDto> listAll() {
        return users.findAll().stream().map(UserService::toDto).toList();
    }

    @Transactional
    public UserDto setEnabled(Long id, boolean enabled, String actingEmail) {
        User u = users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (u.getEmail().equals(actingEmail)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot disable your own account");
        }
        u.setEnabled(enabled);
        return toDto(users.save(u));
    }

    @Transactional
    public UserDto createOfficer(OfficerRequest r) {
        String email = r.email().trim().toLowerCase();
        if (users.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        }
        Department d = departments.findById(r.departmentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));
        User u = new User();
        u.setFullName(r.fullName().trim());
        u.setEmail(email);
        u.setPassword(encoder.encode(r.password()));
        u.setRole(Role.OFFICER);
        u.setDepartment(d);
        return toDto(users.save(u));
    }

    @Transactional(readOnly = true)
    public List<UserDto> listOfficers(Long departmentId) {
        List<User> list = departmentId == null
                ? users.findByRole(Role.OFFICER)
                : users.findByRoleAndDepartmentId(Role.OFFICER, departmentId);
        return list.stream().map(UserService::toDto).toList();
    }

    // ---------- departments ----------
    @Transactional(readOnly = true)
    public List<DepartmentDto> listDepartments() {
        return departments.findAll().stream()
                .map(d -> new DepartmentDto(d.getId(), d.getName(), d.getDescription())).toList();
    }

    @Transactional
    public DepartmentDto createDepartment(DepartmentRequest r) {
        if (departments.existsByNameIgnoreCase(r.name().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Department already exists");
        }
        Department d = departments.save(new Department(r.name().trim(), r.description()));
        return new DepartmentDto(d.getId(), d.getName(), d.getDescription());
    }
}
