package com.kjr.rfp.controller;

import com.kjr.rfp.model.Resume;
import com.kjr.rfp.service.parser.ResumeParserService;
import com.kjr.rfp.util.DataMaskingUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/resumes")
public class ResumeController {
    private final ResumeParserService resumeParserService;

    @Autowired
    public ResumeController(ResumeParserService resumeParserService) {
        this.resumeParserService = resumeParserService;
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

    @GetMapping("/preview/{id}")
    public String previewResume(@PathVariable String id, Model model, HttpServletRequest request) {
        Optional<Resume> resumeOpt = resumeParserService.getResumeById(id)
                .orElseThrow(() -> new RuntimeException("Resume not found"));

        Resume resume = resumeOpt.get();
        // Mask sensitive information using DataMaskingUtil
        resume.setEmail(DataMaskingUtil.maskEmail(resume.getEmail()));
        if (resume.getPhone() != null) {
            resume.setPhone(DataMaskingUtil.maskPhone(resume.getPhone()));
        }
        if (resume.getTitle() == null) {
            resume.setTitle("Professional Resume");
        }

        model.addAttribute("resume", resume);
        model.addAttribute("currentUri", request.getRequestURI()); // Instead of #request.getRequestURI()
        model.addAttribute("downloadCount", request.getSession().getAttribute("downloadCount"));

        return "preview-resume";
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
                .map(DataMaskingUtil::maskResumeDetails) // Use DataMaskingUtil here
                .toList();

        model.addAttribute("resumes", filteredResumes);
        model.addAttribute("searchQuery", query);
        return "search-results";
    }

}
