package com.kjr.rfp.controller;

import com.kjr.rfp.model.Resume;
import com.kjr.rfp.service.FileStorageService;
import com.kjr.rfp.service.parser.ResumeParserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/resumes")
public class ResumeController {
    private final ResumeParserService resumeParserService;

    private final FileStorageService fileStorageService;

    @Autowired
    public ResumeController(ResumeParserService resumeParserService, FileStorageService fileStorageService) {
        this.resumeParserService = resumeParserService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/upload")
    public String showUploadForm(Model model) {
        model.addAttribute("resume", new Resume());
        return "upload-resume";
    }

    @PostMapping("/upload")
    public String uploadResume(@RequestParam("file") MultipartFile file, Model model) {
        try {
            String fileId = resumeParserService.storeFile(file);
            Resume resume = resumeParserService.parseResume(file);
            resume.setFileId(fileId);
            Resume savedResume = resumeParserService.saveResume(resume);
            model.addAttribute("resume", savedResume);
            return "preview-resume";
        } catch (Exception e) {
            model.addAttribute("error", "Error processing resume: " + e.getMessage());
            return "upload-resume";
        }
    }

    @GetMapping("/search")
    public String showSearchForm(Model model) {
        model.addAttribute("searchQuery", "");
        return "search-resumes";
    }

    @GetMapping("/search/results")
    public String searchResumes(@RequestParam String query, Model model) {
        List<List<Resume>> allResumes = resumeParserService.getResumes();
        // Simple search implementation - you might want to enhance this
        List<Resume> filteredResumes = allResumes.stream()
                .flatMap(List::stream)
                .filter(r -> r.getName().toLowerCase().contains(query.toLowerCase()) ||
                        r.getEmail().toLowerCase().contains(query.toLowerCase()) ||
                        r.getSkills().toString().toLowerCase().contains(query.toLowerCase()))
                .map(this::maskResumeDetails) // Add this line to mask sensitive data
                .toList();

        model.addAttribute("resumes", filteredResumes);
        model.addAttribute("searchQuery", query);
        return "search-results";
    }

    @GetMapping("/preview/{id}")
    public String previewResume(@PathVariable String id, Model model) {
        Optional<Resume> resumeOpt = resumeParserService.getResumeById(id)
                .orElseThrow(() -> new RuntimeException("Resume not found"));

        Resume resume = resumeOpt.get();
        // Mask sensitive information
        resume.setEmail(maskEmail(resume.getEmail()));
        if (resume.getPhone() != null) {
            resume.setPhone(maskPhone(resume.getPhone()));
        }

        model.addAttribute("resume", resume);
        return "preview-resume";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/download/{id}")
    public void downloadFile(@PathVariable String id, HttpServletResponse response, HttpServletRequest request) {
        try {
            // Authentication check
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new AccessDeniedException("Authentication required");
            }
            // Track downloads per session
            Integer downloadCount = (Integer) request.getSession().getAttribute("downloadCount");
            if (downloadCount == null) {
                downloadCount = 0;
            }

            // Check if maximum downloads reached
            if (downloadCount >= 2) { // Example: limit to 5 downloads per session
                response.sendRedirect("/resumes/download-limit-reached");
                return;
            }

            Resume resume = resumeParserService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Resume not found"));

            GridFsResource resource = fileStorageService.getFileResource(resume.getFileId());
            if (resource == null) {
                throw new RuntimeException("File not found");
            }

            // Set response headers
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + resume.getFileName() + "\"");
            response.setContentType(resource.getContentType());

            // Stream the file
            try (InputStream inputStream = resource.getInputStream();
                 OutputStream outputStream = response.getOutputStream()) {
                IOUtils.copy(inputStream, outputStream);
            }

            // Increment download count
            request.getSession().setAttribute("downloadCount", downloadCount + 1);
            request.getSession().setAttribute("justDownloaded", true);

            response.sendRedirect("/resumes/thanks");
            return;

        } catch (IOException e) {
            throw new RuntimeException("Error downloading file", e);
        }
    }
    @GetMapping("/download-limit-reached")
    public String downloadLimitReached(Model model) {
        model.addAttribute("message", "You have reached the maximum number of downloads for this session. Please login again.");
        return "download-limit";
    }


    @GetMapping("/thanks")
    public String downloadThanks(Model model, HttpServletRequest request) {
        // Check if the user just downloaded a file
        Boolean justDownloaded = (Boolean) request.getSession().getAttribute("justDownloaded");

        if (justDownloaded == null || !justDownloaded) {
            // If not coming from a download, redirect to search page
            return "redirect:/resumes/search";
        }

        // Clear the session attribute
        request.getSession().removeAttribute("justDownloaded");

        model.addAttribute("message", "Thank you for downloading the resume!");
        return "download-thanks";
    }


    @GetMapping("/logout-after-download")
    public String logoutAfterDownload(HttpServletRequest request) {
        // Manually invalidate the session
        new SecurityContextLogoutHandler().logout(request, null, null);
        return "redirect:/login?logout";
    }


    private String maskEmail(String email) {
        if (email == null || email.isEmpty()) return "";
        int atIndex = email.indexOf('@');
        if (atIndex <= 2) return "***" + email.substring(atIndex);
        return email.substring(0, 2) + "***" + email.substring(atIndex);
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.isEmpty()) return "";
        if (phone.length() <= 4) return "****";
        return "****" + phone.substring(phone.length() - 4);
    }

    private Resume maskResumeDetails(Resume resume) {
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
