package dk.fklub.fkult.business.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.fklub.fkult.persistence.entities.SoundSample;
import dk.fklub.fkult.persistence.entities.User;
import dk.fklub.fkult.persistence.repository.SoundSampleRepository;
import dk.fklub.fkult.presentation.DTOs.SoundSampleRequest;
import dk.fklub.fkult.business.services.shuffleFilter.*;

import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.*;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class SoundSampleService {
    //Upload dir path which fits with the backend filepath
    private static final String UPLOAD_DIR = java.nio.file.Paths.get("").toAbsolutePath().resolve("soundSampleUploads").toString();
    // Download copy of repository to run its code
    private final SoundSampleRepository repository;
    private final UserService userService;

    public SoundSampleService(SoundSampleRepository repository, UserService userService) {
        this.repository = repository;
        this.userService = userService;
    }

    // Handles sound sample uploads with either link or file
    public String upload(MultipartFile file, String soundSampleJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            SoundSample soundSample = mapper.readValue(soundSampleJson, SoundSample.class);

            // Validate input
            if (file == null && (soundSample.getLink() == null || soundSample.getLink().isEmpty())) {
                return "Upload failed: either link or file must be provided for upload.";
            }

            //path which gets inserted into db
            String sqlPath = null; 

            // Checks if its a file that got uploaded
            if (file != null) {
                //create a new upload directory
                File uploadDir = new File(UPLOAD_DIR);
                if (!uploadDir.exists()) uploadDir.mkdirs();

                //find the files original name
                String orgFilename = file.getOriginalFilename();
                String fileName = orgFilename;

                // Split into filename and file type
                String splitName = fileName;
                String fileType = "";
                int dotIndex = fileName.lastIndexOf(".");

                //checks if there is any . and if there is split the filename and filetype from each other
                if (dotIndex != -1) {
                    splitName = fileName.substring(0, dotIndex);
                    fileType = fileName.substring(dotIndex);
                }

                // rename if filename already exists with a _1, _2, ...
                File destFile = new File(uploadDir, fileName);
                int counter = 1;
                while (destFile.exists()) {
                    String newFilename = splitName + "_" + counter + fileType;
                    destFile = new File(uploadDir, newFilename);
                    counter++;
                }

                // Build paths
                file.transferTo(destFile);
                sqlPath = "soundSampleUploads" + File.separator + destFile.getName();
            }

            // Save path to database (null if link-only)
            soundSample.setFilePath(sqlPath);
            repository.save(soundSample);

            return "Upload complete!";

        } catch (Exception e) {

            e.printStackTrace();
            return "Upload failed: " + e.getMessage();
        }
    }


    // Handles deletion of existing sound sample from given link or file name
    public String delete(String link, String fileName, String id) {

        // Input validation
        if (link == null && fileName == null && id == null) {
            return "No link or file path provided for deletion.";
        }
        if (link != null && fileName != null) {
            return "Please provide either link or file path for deletion, not both.";
        }

        // If deletion is based on file name, find and delete file
        String filePath = null;
        if (fileName != null) {

            //define file path
            filePath = "soundSampleUploads" + File.separator + fileName;

            //get file through absolute path
            File file = new File(UPLOAD_DIR + File.separator + fileName).getAbsoluteFile();

            //if file exists delete, if not return error or abortion
            if (file.exists()) {
                if (file.delete()) {
                    System.out.println("Deleted file: " + file.getPath());
                } else {
                    return "Failed to delete file at: " + file.getPath() + ". Aborting database deletion.";
                }
            } else {
                return "File not found at " + file.getPath() + ". Aborting database deletion.";
            }
        }

        // Delete sound sample object in the database
        return repository.delete(link, filePath, id);
    }

    // Get all sound samples
    public List<SoundSampleRequest> getAllSoundSamples(Boolean quick, Boolean weighted){
        List<SoundSample> allSoundSamples = repository.getAll();
        List<SoundSampleRequest> soundSamplesRequests = new ArrayList<>();

        if (quick && weighted) {
            return new ArrayList<>();
        }
        
        // Option for shuffle
        ShuffleFilter shuffleFilter = new ShuffleFilter();
        if (quick) {
            allSoundSamples = shuffleFilter.quickShuffle(allSoundSamples);
        } else if(weighted) {
            allSoundSamples = shuffleFilter.weightedShuffle(allSoundSamples);
        }

        List<User> allUsers = userService.getAllUsers();
        String name = null, username = null;
        for (SoundSample soundSample : allSoundSamples) {
            Long id = soundSample.getId();
            // Convert userId to username
            for (User user : allUsers) {
                if (soundSample.getUserId().equals(user.getId())) {
                    name = user.getName();
                    username = user.getUsername();
                    break;
                }
            }

            // Get the file or link
            if (soundSample.getFilePath() != null) {
                String filePath = soundSample.getFilePath();
                String folder = "soundSampleUploads" + File.separator;
                int indexFolder = filePath.indexOf(folder);
                String fileName = filePath;
                if (indexFolder != -1) {
                    fileName = filePath.substring(folder.length() + indexFolder, filePath.length());
                }

                SoundSampleRequest soundSamplesRequest = new SoundSampleRequest(fileName, username, name, id);
                soundSamplesRequests.add(soundSamplesRequest);
            } else {
                SoundSampleRequest soundSamplesRequest = new SoundSampleRequest(soundSample.getLink(), username, name, id);
                soundSamplesRequests.add(soundSamplesRequest);
            }
        }

        return soundSamplesRequests;
    }

    // Returns a Spring Resource of sound sample file using a string "filename"
    public Resource getSoundSampleFile(String fileName) throws MalformedURLException {

        // Resolve and normalize the file path to prevent invalid/unsafe paths
        Path path = java.nio.file.Paths.get(UPLOAD_DIR).resolve(fileName).normalize();

        // Wrap the file path in a Spring Resource abstraction
        Resource resource = new UrlResource(path.toUri());

        // Ensure the file actually exists before returning it
        if (!resource.exists()) {
            throw new RuntimeException("File not found: " + fileName);
        }

        return resource;
    }
}