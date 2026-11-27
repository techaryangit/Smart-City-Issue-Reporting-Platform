package com.smartcity.issuereporting.service;

import com.smartcity.issuereporting.dto.Dtos.*;
import com.smartcity.issuereporting.model.*;
import com.smartcity.issuereporting.repository.*;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;

@Service
public class ComplaintService {

    private final ComplaintRepository complaints;
    private final ComplaintUpdateRepository updates;
    private final UserRepository users;
    private final DepartmentRepository departments;
    private final FileStorageService storage;
    private final NotificationService notifications;

    public ComplaintService(ComplaintRepository complaints, ComplaintUpdateRepository updates,
                            UserRepository users, DepartmentRepository departments,
                            FileStorageService storage, NotificationService notifications) {
        this.complaints = complaints;
        this.updates = updates;
        this.users = users;
        this.departments = departments;
        this.storage = storage;
        this.notifications = notifications;
    }

    // =====================================================================
    //  Citizen operations
    // =====================================================================

    @Transactional
    public ComplaintDto create(String email, String title, Category category, String description,
                               String address, Double lat, Double lng, MultipartFile image) {
        User citizen = users.findByEmail(email).orElseThrow();
        Complaint c = new Complaint();
        c.setTitle(title.trim());
        c.setCategory(category);
        c.setDescription(description.trim());
        c.setAddress(address.trim());
        c.setLatitude(lat);
        c.setLongitude(lng);
        c.setCitizen(citizen);
        c.setImagePath(storage.store(image));
        c = complaints.save(c);

        // unique, human-friendly tracking ID derived from the primary key
        c.setComplaintCode(String.format("CMP-%d-%06d", LocalDate.now().getYear(), c.getId()));
        updates.save(new ComplaintUpdate(c, Status.PENDING, "Complaint registered", citizen));

        notifications.notify(citizen, "Your complaint " + c.getComplaintCode() + " has been registered.", c.getComplaintCode());
        for (User admin : users.findByRole(Role.ADMIN)) {
            notifications.notify(admin, "New complaint " + c.getComplaintCode() + " (" + category + ") received.", c.getComplaintCode());
        }
        return toDto(c, true);
    }

    @Transactional(readOnly = true)
    public List<ComplaintDto> listMine(String email) {
        User u = users.findByEmail(email).orElseThrow();
        return complaints.findByCitizenIdOrderByCreatedAtDesc(u.getId()).stream().map(c -> toDto(c, false)).toList();
    }

    /** Detailed view incl. history. Citizens see only their own; officers only their assigned ones. */
    @Transactional(readOnly = true)
    public ComplaintDto getByCode(String code, String email) {
        Complaint c = complaints.findByComplaintCode(code.trim().toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No complaint found with ID " + code));
        User u = users.findByEmail(email).orElseThrow();
        boolean allowed = switch (u.getRole()) {
            case ADMIN -> true;
            case CITIZEN -> c.getCitizen().getId().equals(u.getId());
            case OFFICER -> c.getAssignedOfficer() != null && c.getAssignedOfficer().getId().equals(u.getId());
        };
        if (!allowed) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to view this complaint");
        return toDto(c, true);
    }

    // =====================================================================
    //  Admin operations
    // =====================================================================

    @Transactional(readOnly = true)
    public ComplaintDto getById(Long id) {
        Complaint c = complaints.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint not found"));
        return toDto(c, true);
    }

    @Transactional(readOnly = true)
    public List<ComplaintDto> search(Category category, Status status, Priority priority, String q) {
        return complaints.findAll(spec(category, status, priority, q), Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream().map(c -> toDto(c, false)).toList();
    }

    private Specification<Complaint> spec(Category category, Status status, Priority priority, String q) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (category != null) ps.add(cb.equal(root.get("category"), category));
            if (status != null) ps.add(cb.equal(root.get("status"), status));
            if (priority != null) ps.add(cb.equal(root.get("priority"), priority));
            if (q != null && !q.isBlank()) {
                String like = "%" + q.trim().toLowerCase() + "%";
                ps.add(cb.or(
                        cb.like(cb.lower(root.get("address")), like),
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("complaintCode")), like)));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };
    }

    @Transactional
    public ComplaintDto assign(Long id, AssignRequest r, String adminEmail) {
        Complaint c = complaints.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint not found"));
        User admin = users.findByEmail(adminEmail).orElseThrow();
        Department dept = departments.findById(r.departmentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));
        c.setDepartment(dept);

        User officer = null;
        if (r.officerId() != null) {
            officer = users.findById(r.officerId())
                    .filter(o -> o.getRole() == Role.OFFICER)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Officer not found"));
            if (officer.getDepartment() == null || !officer.getDepartment().getId().equals(dept.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Officer does not belong to the selected department");
            }
        }
        c.setAssignedOfficer(officer);
        if (r.priority() != null) c.setPriority(r.priority());
        c.setUpdatedAt(java.time.LocalDateTime.now());

        String remark = "Assigned to " + dept.getName() + (officer != null ? " (" + officer.getFullName() + ")" : "")
                + ", priority " + c.getPriority();
        updates.save(new ComplaintUpdate(c, c.getStatus(), remark, admin));

        notifications.notify(c.getCitizen(), "Complaint " + c.getComplaintCode() + " was assigned to " + dept.getName() + ".", c.getComplaintCode());
        if (officer != null) {
            notifications.notify(officer, "Complaint " + c.getComplaintCode() + " has been assigned to you.", c.getComplaintCode());
        }
        return toDto(c, true);
    }

    /** Used by both admins (any status) and officers (only their own complaints, only IN_PROGRESS / RESOLVED). */
    @Transactional
    public ComplaintDto updateStatus(Long id, StatusRequest r, String email) {
        Complaint c = complaints.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint not found"));
        User actor = users.findByEmail(email).orElseThrow();

        if (actor.getRole() == Role.OFFICER) {
            if (c.getAssignedOfficer() == null || !c.getAssignedOfficer().getId().equals(actor.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This complaint is not assigned to you");
            }
            if (r.status() != Status.IN_PROGRESS && r.status() != Status.RESOLVED) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Officers can only mark complaints In Progress or Resolved");
            }
        }
        if (r.status() == Status.REJECTED && (r.remark() == null || r.remark().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please give a reason when rejecting a complaint");
        }

        c.setStatus(r.status());
        c.setUpdatedAt(java.time.LocalDateTime.now());
        updates.save(new ComplaintUpdate(c, r.status(), r.remark(), actor));
        notifications.notify(c.getCitizen(),
                "Complaint " + c.getComplaintCode() + " is now " + pretty(r.status()) + ".", c.getComplaintCode());
        return toDto(c, true);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> stats() {
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (Status s : Status.values()) byStatus.put(s.name(), 0L);
        for (Object[] row : complaints.countByStatus()) byStatus.put(((Status) row[0]).name(), (Long) row[1]);

        Map<String, Long> byCategory = new LinkedHashMap<>();
        for (Category cat : Category.values()) byCategory.put(cat.name(), 0L);
        for (Object[] row : complaints.countByCategory()) byCategory.put(((Category) row[0]).name(), (Long) row[1]);

        long total = complaints.count();
        long resolved = byStatus.get(Status.RESOLVED.name());
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("total", total);
        out.put("byStatus", byStatus);
        out.put("byCategory", byCategory);
        out.put("resolutionRate", total == 0 ? 0 : Math.round(resolved * 1000.0 / total) / 10.0);
        return out;
    }

    @Transactional(readOnly = true)
    public String exportCsv(Category category, Status status, Priority priority, String q) {
        StringBuilder sb = new StringBuilder("Complaint ID,Title,Category,Status,Priority,Address,Citizen,Department,Officer,Created At\n");
        for (ComplaintDto d : search(category, status, priority, q)) {
            sb.append(csv(d.complaintCode())).append(',').append(csv(d.title())).append(',')
              .append(csv(d.category())).append(',').append(csv(d.status())).append(',')
              .append(csv(d.priority())).append(',').append(csv(d.address())).append(',')
              .append(csv(d.citizenName())).append(',').append(csv(d.departmentName())).append(',')
              .append(csv(d.officerName())).append(',').append(csv(String.valueOf(d.createdAt()))).append('\n');
        }
        return sb.toString();
    }

    // =====================================================================
    //  Officer operations
    // =====================================================================

    @Transactional(readOnly = true)
    public List<ComplaintDto> listAssigned(String email) {
        User u = users.findByEmail(email).orElseThrow();
        return complaints.findByAssignedOfficerIdOrderByCreatedAtDesc(u.getId()).stream().map(c -> toDto(c, false)).toList();
    }

    // =====================================================================
    //  helpers
    // =====================================================================

    private ComplaintDto toDto(Complaint c, boolean withHistory) {
        Department d = c.getDepartment();
        User o = c.getAssignedOfficer();
        List<UpdateDto> history = null;
        if (withHistory) {
            history = updates.findByComplaintIdOrderByCreatedAtAsc(c.getId()).stream()
                    .map(u -> new UpdateDto(u.getStatus().name(), u.getRemark(),
                            u.getUpdatedBy() == null ? null : u.getUpdatedBy().getFullName(), u.getCreatedAt()))
                    .toList();
        }
        return new ComplaintDto(c.getId(), c.getComplaintCode(), c.getTitle(), c.getDescription(),
                c.getCategory().name(), c.getStatus().name(), c.getPriority().name(), c.getAddress(),
                c.getLatitude(), c.getLongitude(),
                c.getImagePath() == null ? null : "/uploads/" + c.getImagePath(),
                c.getCitizen().getFullName(), c.getCitizen().getEmail(),
                d == null ? null : d.getId(), d == null ? null : d.getName(),
                o == null ? null : o.getId(), o == null ? null : o.getFullName(),
                c.getCreatedAt(), c.getUpdatedAt(), history);
    }

    private static String pretty(Status s) {
        return switch (s) {
            case PENDING -> "Pending";
            case IN_PROGRESS -> "In Progress";
            case RESOLVED -> "Resolved";
            case REJECTED -> "Rejected";
        };
    }

    private static String csv(String v) {
        if (v == null) return "";
        String s = v.replace("\"", "\"\"");
        return "\"" + s + "\"";
    }
}
