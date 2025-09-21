package com.kjr.rfp.service.parser;

import com.kjr.rfp.model.Resume;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public interface ResumeParserService {
    Resume parseResume(MultipartFile file) throws Exception;

    Resume saveResume(Resume resume);

    Optional<Resume> getResumeByEmail(String email);

    List<List<Resume>> getResumes();

    Optional<Optional<Resume>> getResumeById(String id);

    String storeFile(MultipartFile file) throws IOException;

    Optional<Resume> findById(String id);
}


