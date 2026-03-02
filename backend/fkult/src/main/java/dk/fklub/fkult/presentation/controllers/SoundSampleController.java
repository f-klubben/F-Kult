package dk.fklub.fkult.presentation.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.fklub.fkult.business.services.SoundSampleService;

import java.util.List;
import java.io.File;
import java.net.MalformedURLException;

import dk.fklub.fkult.presentation.DTOs.SoundSampleRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.util.Set;


@RestController
@RequestMapping("/api/sound-sample")
public class SoundSampleController {

    private static final Set<String> ALLOWED_FILE_EXTENSIONS = Set.of(
        "mp4", "webm", "mov", "m4v", "mp3", "wav", "ogg", "m4a", "flac"
    );

    private static final Set<String> ALLOWED_LINK_DOMAINS = Set.of(
        "youtube.com", "youtu.be", "x.com", "twitter.com", "instagram.com", "facebook.com", "tiktok.com"
    );

    // Download copy of soundsampleservice to use its functions
    private final SoundSampleService service;
    public SoundSampleController(SoundSampleService service) {
        this.service = service;
    }

    // Fetch function to upload a sound sample to the database
    @PostMapping("/upload")
    public ResponseEntity<String> upload(
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart(value = "soundSample", required = true) String soundSampleJson) {

        // 1. Validate JSON exists
        if (soundSampleJson == null || soundSampleJson.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Missing soundSample JSON payload.");
        }

        // 2. If a file is provided → validate its extension
        if (file != null && !file.isEmpty()) {
            String filename = file.getOriginalFilename();
            if (filename == null || !filename.contains(".")) {
                return ResponseEntity.badRequest().body("File must have an extension.");
            }

            String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();

            if (!ALLOWED_FILE_EXTENSIONS.contains(ext)) {
                return ResponseEntity.badRequest().body(
                    "Unsupported file type: ." + ext + 
                    ". Allowed types: " + ALLOWED_FILE_EXTENSIONS
                );
            }
        }

        // 3. Extract the link from JSON if needed
        if (file == null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(soundSampleJson);
                String link = node.has("link") ? node.get("link").asText() : null;

                if (link != null && !link.isEmpty()) {
                    // Validate link domain
                    boolean allowed = ALLOWED_LINK_DOMAINS.stream()
                        .anyMatch(link::contains);

                    if (!allowed) {
                        return ResponseEntity.badRequest().body(
                            "Unsupported link domain. Allowed: " + ALLOWED_LINK_DOMAINS
                        );
                    }
                }
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Invalid JSON format.");
            }
        }

        // 4. Everything is valid → forward to service
        return ResponseEntity.ok(service.upload(file, soundSampleJson));
    }


    // Fetch function to delete an existing sound sample by either link or file name
    @DeleteMapping("/delete")
    public String delete(
            @RequestPart(value = "link", required = false) String link,
            @RequestPart(value = "fileName", required = false) String fileName,
            @RequestPart(value = "id", required = true) String id) {
        return service.delete(link, fileName, id);
    }

    // Fetch function to get all sound samples 
    @GetMapping("/get-all")
    public List<SoundSampleRequest> getSoundSamples(
        @RequestParam(defaultValue = "false") boolean quick, 
        @RequestParam(defaultValue = "false") boolean weighted) {
            return service.getAllSoundSamples(quick, weighted);
    }

    // Fetch function to get all sound samples 
    @GetMapping("/download")
    public ResponseEntity<Resource> download(@RequestParam String filePath) throws MalformedURLException {
        Resource file = service.getSoundSampleFile(filePath);

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(file);
    }
}