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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Controller
@RequestMapping("/download")
public class DownloadController {

    private final ResumeParserService resumeParserService;
    private final FileStorageService fileStorageService;

    @Autowired
    public DownloadController(ResumeParserService resumeParserService, FileStorageService fileStorageService) {
        this.resumeParserService = resumeParserService;
        this.fileStorageService = fileStorageService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/resume/{id}")
    public void downloadFile(@PathVariable String id, HttpServletResponse response, HttpServletRequest request) throws IOException {
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
        if (downloadCount >= 2) {
            response.sendRedirect("/download/limit-reached");
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
    }

    @GetMapping("/limit-reached")
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

    @GetMapping("/logout")
    public String logoutAfterDownload(HttpServletRequest request) {
        // Manually invalidate the session
        new SecurityContextLogoutHandler().logout(request, null, null);
        return "redirect:/login?logout";
    }
}

