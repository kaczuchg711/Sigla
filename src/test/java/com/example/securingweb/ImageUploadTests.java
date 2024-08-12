package com.example.securingweb;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ImageUploadTests {

    @Autowired
    private MockMvc mockMvc;

    @ParameterizedTest
    @ValueSource(strings = {"testimage.jpg", "testimage.bmp"})
    public void whenImageUploaded_thenVerifyFileExists(String fileName) throws Exception {
        // Create a mock image file to upload
        byte[] imageBytes = Files.readAllBytes(Paths.get("src/test/resources/" + fileName));
        MockMultipartFile mockImage = new MockMultipartFile(
                "file",
                fileName,
                "image/jpeg",
                imageBytes
        );

        // Perform the file upload with CSRF token
        MvcResult result = mockMvc.perform(multipart("/upload")
                        .file(mockImage)
                        .with(csrf()))  // Add CSRF token to the request
                .andExpect(status().is3xxRedirection()) // Expect redirection after upload
                .andReturn();

        // Verify that the file was uploaded to the correct location
        String projectDir = System.getProperty("user.dir");
        File uploadedFile = new File(projectDir + "/uploaded_img/" + fileName);

        assertThat(uploadedFile.exists()).isTrue();  // Check if the file exists
        assertThat(uploadedFile.length()).isEqualTo(mockImage.getSize());  // Check if the file size matches
    }
}
