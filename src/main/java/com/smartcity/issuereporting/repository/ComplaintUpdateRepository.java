package com.smartcity.issuereporting.repository;

import com.smartcity.issuereporting.model.ComplaintUpdate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintUpdateRepository extends JpaRepository<ComplaintUpdate, Long> {
    List<ComplaintUpdate> findByComplaintIdOrderByCreatedAtAsc(Long complaintId);
}
