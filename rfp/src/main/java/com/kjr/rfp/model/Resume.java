package com.kjr.rfp.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "resumes")
@Data
public class Resume {
    @Id
    private String id;
    private String fileName;
    private String name;
    private String email;
    private String phone;
    private String summary;
    private List<String> skills;
    private List<Experience> experiences;
    private List<Education> educations;
    private String fileId;  // Add this field to store GridFS file ID
}

