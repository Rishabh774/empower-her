package com.empowerher.services;

import com.empowerher.dto.SchemeDto;
import com.empowerher.entities.Category;
import com.empowerher.entities.Scheme;
import com.empowerher.events.SchemeCreatedEvent;
import com.empowerher.repositories.CategoryRepository;
import com.empowerher.repositories.SchemeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Service
public class SchemeService {

    @Autowired
    private SchemeRepository schemeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Value("${file.upload-dir:uploads/}")
    private String UPLOAD_DIR;

    // Allowed file extensions
    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".webp");
    private static final List<String> ALLOWED_PDF_EXTENSIONS = Arrays.asList(".pdf");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public List<Scheme> getAllSchemes() {
        return schemeRepository.findAllWithCategory();
    }

    public List<Scheme> getActiveSchemes() {
        return schemeRepository.findByActiveTrue();
    }

    public Scheme getSchemeById(Long id) {
        return schemeRepository.findByIdWithCategory(id).orElse(null);
    }

    public Scheme saveScheme(Scheme scheme) {
        return schemeRepository.save(scheme);
    }

    public Scheme createScheme(SchemeDto schemeDto) throws IOException {
        Scheme scheme = new Scheme();
        
        // Set basic fields
        scheme.setTitle(schemeDto.getTitle());
        scheme.setLevel(schemeDto.getLevel());
        scheme.setShortDesc(schemeDto.getShortDesc());
        scheme.setFullDesc(schemeDto.getFullDesc());
        scheme.setApplyLink(schemeDto.getApplyLink());
        
        // Process YouTube URL
        if (schemeDto.getVideoUrl() != null && !schemeDto.getVideoUrl().trim().isEmpty()) {
            String processedVideoUrl = processYouTubeUrl(schemeDto.getVideoUrl());
            scheme.setVideoUrl(processedVideoUrl);
            System.out.println("YouTube URL processed: " + processedVideoUrl);
        } else {
            scheme.setVideoUrl(schemeDto.getVideoUrl());
        }
        
        scheme.setBlogContent(schemeDto.getBlogContent());
        scheme.setCreatedAt(LocalDateTime.now());
        scheme.setActive(true);

        // Set category
        Category category = categoryRepository.findById(schemeDto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        scheme.setCategory(category);

        // Handle file uploads with security validation
        if (schemeDto.getImageFile() != null && !schemeDto.getImageFile().isEmpty()) {
            System.out.println("Processing image file: " + schemeDto.getImageFile().getOriginalFilename());
            validateFile(schemeDto.getImageFile(), ALLOWED_IMAGE_EXTENSIONS, "image");
            String imageUrl = saveFile(schemeDto.getImageFile(), "images");
            scheme.setImageUrl("/uploads/images/" + imageUrl);
            System.out.println("Image saved with URL: " + scheme.getImageUrl());
        } else {
            System.out.println("No image file provided or file is empty");
        }

        if (schemeDto.getPdfFile() != null && !schemeDto.getPdfFile().isEmpty()) {
            System.out.println("Processing PDF file: " + schemeDto.getPdfFile().getOriginalFilename());
            validateFile(schemeDto.getPdfFile(), ALLOWED_PDF_EXTENSIONS, "PDF");
            String pdfUrl = saveFile(schemeDto.getPdfFile(), "pdfs");
            scheme.setPdfUrl("/uploads/pdfs/" + pdfUrl);
            System.out.println("PDF saved with URL: " + scheme.getPdfUrl());
        } else {
            System.out.println("No PDF file provided or file is empty");
        }

        Scheme savedScheme = schemeRepository.save(scheme);
        System.out.println("Scheme saved successfully with ID: " + savedScheme.getId());
        
        // Event publish karen for email notifications
        eventPublisher.publishEvent(new SchemeCreatedEvent(this, savedScheme));
        System.out.println("Scheme creation event published for: " + savedScheme.getTitle());
        
        return savedScheme;
    }

    public Scheme updateScheme(Long id, SchemeDto schemeDto) throws IOException {
        Scheme existingScheme = getSchemeById(id);
        if (existingScheme == null) {
            throw new RuntimeException("Scheme not found with id: " + id);
        }

        // Update fields
        existingScheme.setTitle(schemeDto.getTitle());
        existingScheme.setLevel(schemeDto.getLevel());
        existingScheme.setShortDesc(schemeDto.getShortDesc());
        existingScheme.setFullDesc(schemeDto.getFullDesc());
        existingScheme.setApplyLink(schemeDto.getApplyLink());
        
        // Process YouTube URL
        if (schemeDto.getVideoUrl() != null && !schemeDto.getVideoUrl().trim().isEmpty()) {
            String processedVideoUrl = processYouTubeUrl(schemeDto.getVideoUrl());
            existingScheme.setVideoUrl(processedVideoUrl);
            System.out.println("YouTube URL processed: " + processedVideoUrl);
        } else {
            existingScheme.setVideoUrl(schemeDto.getVideoUrl());
        }
        
        existingScheme.setBlogContent(schemeDto.getBlogContent());

        // Update category if changed
        if (!existingScheme.getCategory().getId().equals(schemeDto.getCategoryId())) {
            Category category = categoryRepository.findById(schemeDto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            existingScheme.setCategory(category);
        }

        // Handle file uploads with security validation
        if (schemeDto.getImageFile() != null && !schemeDto.getImageFile().isEmpty()) {
            System.out.println("Updating image file: " + schemeDto.getImageFile().getOriginalFilename());
            validateFile(schemeDto.getImageFile(), ALLOWED_IMAGE_EXTENSIONS, "image");
            String imageUrl = saveFile(schemeDto.getImageFile(), "images");
            existingScheme.setImageUrl("/uploads/images/" + imageUrl);
            System.out.println("Image updated with URL: " + existingScheme.getImageUrl());
        }

        if (schemeDto.getPdfFile() != null && !schemeDto.getPdfFile().isEmpty()) {
            System.out.println("Updating PDF file: " + schemeDto.getPdfFile().getOriginalFilename());
            validateFile(schemeDto.getPdfFile(), ALLOWED_PDF_EXTENSIONS, "PDF");
            String pdfUrl = saveFile(schemeDto.getPdfFile(), "pdfs");
            existingScheme.setPdfUrl("/uploads/pdfs/" + pdfUrl);
            System.out.println("PDF updated with URL: " + existingScheme.getPdfUrl());
        }

        Scheme updatedScheme = schemeRepository.save(existingScheme);
        System.out.println("Scheme updated successfully with ID: " + updatedScheme.getId());
        return updatedScheme;
    }

    public void deleteScheme(Long id) {
        if (!schemeRepository.existsById(id)) {
            throw new RuntimeException("Scheme not found with id: " + id);
        }
        schemeRepository.deleteById(id);
    }

    public List<Scheme> searchSchemes(String query) {
        try {
            System.out.println("Searching for: " + query);
            List<Scheme> results = schemeRepository.searchSchemes(query);
            System.out.println("Found " + results.size() + " schemes for query: " + query);
            return results;
        } catch (Exception e) {
            System.err.println("Error in searchSchemes: " + e.getMessage());
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }

    public List<Scheme> getSchemesByLevel(String level) {
        return schemeRepository.findByLevel(level);
    }

    public List<Scheme> getSchemesByCategory(Long categoryId) {
        return schemeRepository.findByCategoryId(categoryId);
    }

    // Get featured schemes for home page
    public List<Scheme> getFeaturedSchemes(int limit) {
        List<Scheme> activeSchemes = schemeRepository.findByActiveTrue();
        return activeSchemes.stream()
                .limit(limit)
                .collect(java.util.stream.Collectors.toList());
    }

    private String saveFile(MultipartFile file, String subdirectory) throws IOException {
        // Create directory if not exists
        Path uploadPath = Paths.get(UPLOAD_DIR + subdirectory);
        System.out.println("Creating upload directory: " + uploadPath.toAbsolutePath());
        
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            System.out.println("Directory created successfully");
        }

        // Generate unique filename
        String originalFileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFileName);
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

        // Save file
        Path filePath = uploadPath.resolve(uniqueFileName);
        System.out.println("Saving file to: " + filePath.toAbsolutePath());
        
        Files.copy(file.getInputStream(), filePath);
        System.out.println("File saved successfully: " + uniqueFileName);

        return uniqueFileName;
    }

    private void validateFile(MultipartFile file, List<String> allowedExtensions, String fileType) {
        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(fileType + " file size exceeds maximum limit of 10MB");
        }

        // Check if file is empty
        if (file.isEmpty()) {
            throw new IllegalArgumentException(fileType + " file is empty");
        }

        // Check file extension
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            throw new IllegalArgumentException(fileType + " file name is null");
        }
        
        String fileExtension = getFileExtension(originalFileName);
        
        if (fileExtension == null || !allowedExtensions.contains(fileExtension.toLowerCase())) {
            throw new IllegalArgumentException("Invalid " + fileType + " file type. Allowed: " + allowedExtensions);
        }

        // Check for null bytes in filename (path traversal protection)
        if (originalFileName.contains("\0")) {
            throw new IllegalArgumentException("Invalid file name");
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return null;
        }
        return fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
    }

    // YouTube URL Processing Methods
    private String processYouTubeUrl(String videoUrl) {
        if (videoUrl == null || videoUrl.trim().isEmpty()) {
            return null;
        }
        
        try {
            System.out.println("Processing YouTube URL: " + videoUrl);
            
            String embedUrl = videoUrl;
            
            // Regular YouTube URL to Embed URL conversion
            if (videoUrl.contains("youtube.com/watch")) {
                String videoId = extractYouTubeId(videoUrl);
                if (videoId != null) {
                    embedUrl = "https://www.youtube.com/embed/" + videoId;
                    System.out.println("Converted to embed URL: " + embedUrl);
                }
            }
            // YouTube Shorts URL conversion
            else if (videoUrl.contains("youtube.com/shorts")) {
                String videoId = videoUrl.substring(videoUrl.lastIndexOf("/") + 1);
                embedUrl = "https://www.youtube.com/embed/" + videoId;
                System.out.println("Converted shorts to embed URL: " + embedUrl);
            }
            // YouTu.be URL conversion
            else if (videoUrl.contains("youtu.be")) {
                String videoId = videoUrl.substring(videoUrl.lastIndexOf("/") + 1);
                embedUrl = "https://www.youtube.com/embed/" + videoId;
                System.out.println("Converted youtu.be to embed URL: " + embedUrl);
            }
            // Already embed URL
            else if (videoUrl.contains("youtube.com/embed")) {
                System.out.println("Already embed URL: " + videoUrl);
            }
            
            return embedUrl;
        } catch (Exception e) {
            System.err.println("Error processing YouTube URL: " + e.getMessage());
            return videoUrl; // Return original URL if processing fails
        }
    }

    private String extractYouTubeId(String youtubeUrl) {
        try {
            String pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*";
            Pattern compiledPattern = Pattern.compile(pattern);
            Matcher matcher = compiledPattern.matcher(youtubeUrl);
            
            if (matcher.find()) {
                return matcher.group();
            }
        } catch (Exception e) {
            System.err.println("Error extracting YouTube ID: " + e.getMessage());
        }
        return null;
    }
}