package com.kjr.rfp.repository;

import com.kjr.rfp.model.Resume;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResumeRepository extends MongoRepository<Resume, String> {
    Resume findByEmail(String email);
    Optional<Resume> findById(String id);
}


