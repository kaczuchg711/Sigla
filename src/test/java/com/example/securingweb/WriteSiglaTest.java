package com.example.securingweb;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
public class WriteSiglaTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void whenImageUploaded_thenVerifySiglaExtracted() throws Exception {
        // Load image bytes from the test image file
        byte[] imageBytes = Files.readAllBytes(Paths.get("src/test/resources/text_book_photo.png")); // Ensure the image exists here
        MockMultipartFile mockImage = new MockMultipartFile(
                "file",
                "testimage.png",
                "image/png",
                imageBytes
        );

        // Perform file upload request and expect status 200 OK
        mockMvc.perform(multipart("/upload")
                        .file(mockImage)
                        .with(csrf()))  // Add CSRF token
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("2 Kor 6, 1")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Flp 2, 13")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Ef 2, 8-10")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("1 Kor 15, 10")));
    }
}
