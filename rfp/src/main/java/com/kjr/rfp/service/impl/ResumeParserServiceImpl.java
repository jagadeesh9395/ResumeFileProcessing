package com.kjr.rfp.service.impl;

import com.kjr.rfp.model.Education;
import com.kjr.rfp.model.Experience;
import com.kjr.rfp.model.Resume;
import com.kjr.rfp.repository.ResumeRepository;
import com.kjr.rfp.service.parser.ResumeParserService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ResumeParserServiceImpl implements ResumeParserService {
    ResumeRepository resumeRepository;

    @Autowired
    public ResumeParserServiceImpl(ResumeRepository resumeRepository) {
        this.resumeRepository = resumeRepository;
    }


    @Override
    public Resume parseResume(MultipartFile file) throws Exception {
        String fileName = file.getOriginalFilename();
        String content = extractTextFromFile(file);

        Resume resume = new Resume();
        resume.setFileName(fileName);
        resume.setName(extractName(content));
        resume.setEmail(extractEmail(content));
        resume.setPhone(extractPhone(content));
        resume.setSummary(extractSummary(content));
        resume.setSkills(extractSkills(content));
        resume.setExperiences(extractExperiences(content));
        resume.setEducations(extractEducations(content));

        return resume;
    }

    @Override
    public Resume saveResume(Resume resume) {
        return resumeRepository.save(resume);
    }

    @Override
    public Optional<Resume> getResumeByEmail(String email) {
        return Optional.ofNullable(resumeRepository.findByEmail(email));
    }

    @Override
    public List<List<Resume>> getResumes() {
        return List.of(resumeRepository.findAll());
    }

    @Override
    public Optional<Optional<Resume>> getResumeById(String id) {
        return Optional.of(resumeRepository.findById(id));
    }

    private String extractTextFromFile(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();

        if (fileName != null && fileName.toLowerCase().endsWith(".pdf")) {
            try (InputStream is = file.getInputStream(); PDDocument document = PDDocument.load(is)) {
                return new PDFTextStripper().getText(document);
            }
        } else if (fileName != null && fileName.toLowerCase().endsWith(".docx")) {
            try (InputStream is = file.getInputStream(); XWPFDocument document = new XWPFDocument(is)) {
                return new XWPFWordExtractor(document).getText();
            }
        }
        // Add support for other formats as needed
        return "";
    }

    private String extractName(String content) {
        // Pattern 1: Look for name at the very top of the document (most common case)
        Pattern namePattern = Pattern.compile(
                "^(?i)(?:[A-Z][a-z]+(?:\\s+[A-Z][a-z]+)+)(?=\\s*\\n)",
                Pattern.MULTILINE
        );
        Matcher matcher = namePattern.matcher(content);
        if (matcher.find()) {
            return matcher.group().trim();
        }

        // Pattern 2: Look for name after common headings
        Pattern headingPattern = Pattern.compile(
                "(?i)(?:name|full name|contact information|personal details)[:\\s]*\\n([A-Z][a-z]+(?:\\s+[A-Z][a-z]+)+)",
                Pattern.MULTILINE
        );
        matcher = headingPattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        // Pattern 3: Look for name in email address (fallback)
        Pattern emailPattern = Pattern.compile(
                "\\b([A-Z][a-z]+\\.[A-Z][a-z]+|[A-Z][a-z]+)\\b(?=@|\\s)",
                Pattern.MULTILINE
        );
        matcher = emailPattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1).replace(".", " ").trim();
        }

        return "Unknown"; // Fallback if no name found
    }


    private String extractEmail(String content) {
        Pattern pattern = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
        Matcher matcher = pattern.matcher(content);
        return matcher.find() ? matcher.group() : "";
    }

    private String extractPhone(String content) {
        Pattern pattern = Pattern.compile("(\\+\\d{1,3}[- ]?)?\\d{10}");
        Matcher matcher = pattern.matcher(content);
        return matcher.find() ? matcher.group() : "";
    }

    private String extractSummary(String content) {
        // Try to find summary section using common headings
        Pattern sectionPattern = Pattern.compile(
                "(?i)(?:Summary|Professional Summary|Profile|Career Summary|About Me)[:\\s]*(.*?)(?=\\n\\s*\\n|$)",
                Pattern.DOTALL
        );

        Matcher sectionMatcher = sectionPattern.matcher(content);
        if (sectionMatcher.find()) {
            return sectionMatcher.group(1).trim();
        }

        // Fallback to first few lines if no dedicated section found
        String[] lines = content.split("\\r?\\n");
        return lines.length > 0 ? lines[0] : "";
    }


    private List<String> extractSkills(String content) {
        // First try to find skills section using common headings
        Pattern sectionPattern = Pattern.compile(
                "(?i)(?:Skillset|TECHNICAL SKILLS|Skills|Technical Skills|Key Skills|Core Competencies)[:\\s]*(.*?)(?=\\n\\s*\\n|$)",
                Pattern.DOTALL
        );

        Matcher sectionMatcher = sectionPattern.matcher(content);
        if (sectionMatcher.find()) {
            String skillsSection = sectionMatcher.group(1).trim();
            // Extract individual skills from the section
            return Arrays.asList(skillsSection.split("[,\\n]\\s*"));
        }

        // Fallback to simple keyword matching if no section found
        List<String> commonSkills = Arrays.asList(
                "Java", "Spring", "Python", "JavaScript", "SQL",
                "MongoDB", "React", "Angular", "Node.js", "AWS",
                "Docker", "Kubernetes", "Git", "REST API", "Microservices"
        );

        List<String> foundSkills = new ArrayList<>();
        for (String skill : commonSkills) {
            if (content.toLowerCase().contains(skill.toLowerCase())) {
                foundSkills.add(skill);
            }
        }
        return foundSkills;
    }

    private List<Experience> extractExperiences(String content) {
        List<Experience> experiences = new ArrayList<>();

        // First try to find experience section using common headings
        Pattern sectionPattern = Pattern.compile(
                "(?i)(?:Work Experience|PROFESSIONAL EXPERIENCE|Employment History|Career History)[:\\s]*(.*?)(?=\\n\\s*(?:Education|Skills|$))",
                Pattern.DOTALL
        );

        Matcher sectionMatcher = sectionPattern.matcher(content);
        if (sectionMatcher.find()) {
            String experienceSection = sectionMatcher.group(1).trim();

            // Extract individual experiences from the section
            Pattern experiencePattern = Pattern.compile(
                    "(?i)(.*?)\\s*\\n(.*?)\\s*\\n(.*?)\\s*\\n(.*?)(?=\\n\\s*\\n|$)",
                    Pattern.DOTALL
            );

            Matcher experienceMatcher = experiencePattern.matcher(experienceSection);
            while (experienceMatcher.find()) {
                Experience exp = new Experience();
                exp.setCompany(experienceMatcher.group(1).trim());
                exp.setPosition(experienceMatcher.group(2).trim());
                exp.setDuration(experienceMatcher.group(3).trim());
                exp.setDescription(experienceMatcher.group(4).trim());
                experiences.add(exp);
            }
        } else {
            // Fallback to simple pattern matching if no section found
            Pattern pattern = Pattern.compile("(?i)(.*?)\\s*\\n(.*?)\\s*\\n(.*?)\\s*\\n(.*?)(?=\\n\\n|$)");
            Matcher matcher = pattern.matcher(content);

            while (matcher.find()) {
                Experience exp = new Experience();
                exp.setCompany(matcher.group(1).trim());
                exp.setPosition(matcher.group(2).trim());
                exp.setDuration(matcher.group(3).trim());
                exp.setDescription(matcher.group(4).trim());
                experiences.add(exp);
            }
        }

        return experiences;
    }


    private List<Education> extractEducations(String content) {
        List<Education> educations = new ArrayList<>();

        // First try to find education section using common headings
        Pattern sectionPattern = Pattern.compile(
                "(?i)(?:Educational Qualification|Academic Profile|Education|Academic Background|Academic Qualifications)[:\\s]*(.*?)(?=\\n\\s*(?:Experience|Skills|$))",
                Pattern.DOTALL
        );

        Matcher sectionMatcher = sectionPattern.matcher(content);
        if (sectionMatcher.find()) {
            String educationSection = sectionMatcher.group(1).trim();

            // Extract individual education entries from the section
            Pattern educationPattern = Pattern.compile(
                    "(?i)(.*?)\\s*\\n(.*?)\\s*\\n(.*?)\\s*\\n(.*?)(?=\\n\\s*\\n|$)",
                    Pattern.DOTALL
            );

            Matcher educationMatcher = educationPattern.matcher(educationSection);
            while (educationMatcher.find()) {
                Education edu = new Education();
                edu.setInstitution(educationMatcher.group(1).trim());
                Pattern degreePattern = Pattern.compile("(?<degree>(?:" +
                        "B\\.?[A-Z]*\\.?|Bachelor(?:'s)?|BBA|BCA|Bsc|B.Tech|B.E|B.A|B.Com" +  // Bachelor degrees
                        "M\\.?[A-Z]*\\.?|Master(?:'s)?|MCA|MBA|M.Tech|M.E|M.A|M.Com|M.Sc|M.B.A|M.E" +     // Master degrees
                        "PhD|Doctorate" +                     // Doctoral degrees
                        "Associate|Diploma|Certificate" +     // Other qualifications
                        ")[\\s\\w\\-]+(?:in|of)?\\s*[\\w\\s]+)");
                Matcher degreeMatcher = degreePattern.matcher(educationMatcher.group(1).trim()
                        .replaceAll("\\s+", " ") // Normalize whitespace
                        .replaceAll("\\b(in|of)\\b", "in") // Standardize "in/of"
                        .replaceAll("\\s+", " "));
                String degreeFromInstitution = degreeMatcher.find() ? degreeMatcher.group() : null;
                edu.setDegree(degreeFromInstitution);
                edu.setFieldOfStudy(educationMatcher.group(3).trim());
                Pattern yearInInstitution = Pattern.compile("\\b(19|20)\\d{2}\\b");
                Matcher yearMatcher = yearInInstitution.matcher(educationMatcher.group(1).trim());
                String yearFromInstitution = yearMatcher.find() ? yearMatcher.group() : null;
                edu.setYear(yearFromInstitution);
                educations.add(edu);
            }
        } else {
            // Fallback to simple pattern matching if no section found
            Pattern pattern = Pattern.compile("(?i)(.*?)\\s*\\n(.*?)\\s*\\n(.*?)\\s*\\n(.*?)(?=\\n\\n|$)");
            Matcher matcher = pattern.matcher(content);

            while (matcher.find()) {
                Education edu = new Education();
                edu.setInstitution(matcher.group(1).trim());
                edu.setDegree(matcher.group(2).trim());
                edu.setFieldOfStudy(matcher.group(3).trim());
                edu.setYear(matcher.group(4).trim());
                educations.add(edu);
            }
        }

        return educations;
    }

}

