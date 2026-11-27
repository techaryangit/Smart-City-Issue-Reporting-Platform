package com.smartcity.issuereporting.repository;

import com.smartcity.issuereporting.model.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ComplaintRepository extends JpaRepository<Complaint, Long>, JpaSpecificationExecutor<Complaint> {
    Optional<Complaint> findByComplaintCode(String complaintCode);
    List<Complaint> findByCitizenIdOrderByCreatedAtDesc(Long citizenId);
    List<Complaint> findByAssignedOfficerIdOrderByCreatedAtDesc(Long officerId);

    @Query("select c.status, count(c) from Complaint c group by c.status")
    List<Object[]> countByStatus();

    @Query("select c.category, count(c) from Complaint c group by c.category")
    List<Object[]> countByCategory();
}
