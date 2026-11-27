package com.smartcity.issuereporting.config;

import com.smartcity.issuereporting.model.Department;
import com.smartcity.issuereporting.model.Role;
import com.smartcity.issuereporting.model.User;
import com.smartcity.issuereporting.repository.DepartmentRepository;
import com.smartcity.issuereporting.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/** Creates default departments and demo accounts on first start. Change these passwords before deploying! */
@Component
public class DataSeeder implements CommandLineRunner {

    private final DepartmentRepository departments;
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public DataSeeder(DepartmentRepository departments, UserRepository users, PasswordEncoder encoder) {
        this.departments = departments;
        this.users = users;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (departments.count() == 0) {
            departments.saveAll(List.of(
                    new Department("Roads & Infrastructure", "Potholes, broken roads, footpaths"),
                    new Department("Sanitation & Waste", "Garbage collection and cleanliness"),
                    new Department("Water Supply", "Water leakages and supply issues"),
                    new Department("Electricity & Lighting", "Streetlights and public electrical works"),
                    new Department("Drainage & Sewerage", "Blocked drains, waterlogging, sewage")));
        }
        if (!users.existsByEmail("admin@smartcity.gov")) {
            User admin = new User();
            admin.setFullName("System Administrator");
            admin.setEmail("admin@smartcity.gov");
            admin.setPassword(encoder.encode("Admin@123"));
            admin.setRole(Role.ADMIN);
            users.save(admin);
        }
        if (!users.existsByEmail("officer@smartcity.gov")) {
            User officer = new User();
            officer.setFullName("Demo Field Officer");
            officer.setEmail("officer@smartcity.gov");
            officer.setPassword(encoder.encode("Officer@123"));
            officer.setRole(Role.OFFICER);
            officer.setDepartment(departments.findAll().get(0));
            users.save(officer);
        }
    }
}
