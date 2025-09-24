package com.kjr.rfp.util;

import com.kjr.rfp.model.Resume;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DataMaskingUtil {

    public static String maskEmail(String email) {
        if (email == null || email.isEmpty()) return "";
        int atIndex = email.indexOf('@');
        if (atIndex <= 2) return "***" + email.substring(atIndex);
        return email.substring(0, 2) + "***" + email.substring(atIndex);
    }

    public static String maskPhone(String phone) {
        if (phone == null || phone.isEmpty()) return "";
        if (phone.length() <= 4) return "****";
        return "****" + phone.substring(phone.length() - 4);
    }

    public static Resume maskResumeDetails(Resume resume) {
        // Create a copy of the resume to avoid modifying the original
        Resume maskedResume = new Resume();
        maskedResume.setId(resume.getId());
        maskedResume.setName(resume.getName());
        maskedResume.setEmail(maskEmail(resume.getEmail()));
        maskedResume.setPhone(maskPhone(resume.getPhone()));
        maskedResume.setSkills(resume.getSkills());
        maskedResume.setFileId(resume.getFileId());
        maskedResume.setFileName(resume.getFileName());
        // Add any other fields you need to copy
        return maskedResume;
    }
}

