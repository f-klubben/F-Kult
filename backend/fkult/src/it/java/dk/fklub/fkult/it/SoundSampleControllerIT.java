package dk.fklub.fkult.it;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SoundSampleControllerIT {
    
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    //create dummy file and path which is restarted before and after each test
    private static final String UPLOAD_DIR = "soundSampleUploads";
    private static final String UPLOAD_FILENAME = "test.wav";

    @BeforeAll
    static void ensureUploadDir(){
        new File(UPLOAD_DIR).mkdir();
    }

    @AfterAll
    static void cleanup() throws Exception {
        Files.deleteIfExists(Path.of(UPLOAD_DIR, UPLOAD_FILENAME));
    }

    //test get all sound samples function
    @Test
    @Order(1)
    void getAll() throws Exception{
        mvc.perform(get("/api/sound-sample/get-all")).andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().string(containsString("example.com/sample.mp3")));
    }

    //test upload link function
    @Test
    @Order(2)
    void uploadLink() throws Exception{
        String json = """
            {"link":"https://youtube.com/random.mp3", "userId":2}
        """;

        MockMultipartFile soundSampleJson = new MockMultipartFile("soundSample", "", "application/json", json.getBytes());

        mvc.perform(multipart("/api/sound-sample/upload").file(soundSampleJson))
        .andExpect(status().isOk()).andExpect(content().string("Upload complete!"));
    }

    //test upload file function
    @Test
    @Order(3)
    void uploadFile() throws Exception {
        byte[] bytes = "RIF----WAVEfmt ".getBytes();

        MockMultipartFile file = new MockMultipartFile("file",UPLOAD_FILENAME, "audio/wav", bytes);

        String json = """
                {"userId":2}
                """;
        
        MockMultipartFile soundSamleJson = new MockMultipartFile("soundSample", "", "application/json", json.getBytes());

        mvc.perform(multipart("/api/sound-sample/upload").file(file).file(soundSamleJson))
        .andExpect(status().isOk()).andExpect(content().string(containsString("Upload complete!")));

        Assertions.assertTrue(Files.exists(Path.of(UPLOAD_DIR, UPLOAD_FILENAME)), "Uploaded file should be written to " + UPLOAD_DIR);
    }

    //test delete file by name function
    @Test
    @Order(4)
    void deleteByFileName() throws Exception {
        MockMultipartFile fileName = new MockMultipartFile("fileName", "", "text/plain", UPLOAD_FILENAME.getBytes());
        MockMultipartFile id = new MockMultipartFile("id", "", "text/plain", "1".getBytes()
    );

        //perform api call to delete
        mvc.perform(multipart("/api/sound-sample/delete").file(fileName).file(id).with(req -> {req.setMethod("DELETE"); return req;}))
        .andExpect(status().isOk()).andExpect(content().string(notNullValue()));

        //assert file is gone
        Assertions.assertFalse(Files.exists(Path.of(UPLOAD_DIR, UPLOAD_FILENAME)), "File should be deleted by the service before DB deletion");
    }

    //test delete function if missing value
    @Test
    @Order(5)
    void deleteMissingValue() throws Exception {
        MockMultipartFile id = new MockMultipartFile("id", "", "text/plain", "1".getBytes());
        mvc.perform(multipart("/api/sound-sample/delete").file(id).with(req -> {req.setMethod("DELETE"); return req;}))
        .andExpect(status().isOk()).andExpect(content().string(containsString("No link or file")));
    }

    //get all, with weighted outcome (both quick and wigthed)
    @Test
    @Order(6)
    void getAllWhenQuickAndWeightedTrue() throws Exception{
        mvc.perform(get("/api/sound-sample/get-all").param("quick","true").param("weighted","true"))
        .andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().string(containsString("[]")));
    }

    //Collect file for media player if it exists test
    @Test
    @Order(7)
    void downloadFileWhenExists() throws Exception{
        Files.write(Path.of(UPLOAD_DIR, UPLOAD_FILENAME), "beep".getBytes());

        mvc.perform(get("/api/sound-sample/download").param("filePath", UPLOAD_FILENAME))
        .andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_OCTET_STREAM))
        .andExpect(content().bytes("beep".getBytes()));

    }
}
