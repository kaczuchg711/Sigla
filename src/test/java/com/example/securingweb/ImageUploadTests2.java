//package com.example.securingweb;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.MvcResult;
//
//import java.io.File;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//public class ImageUploadTests2 {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Test
//    public void whenImageUploaded_thenVerifyFileExists() throws Exception {
//        // Create a mock image file to upload
//        byte[] imageBytes = Files.readAllBytes(Paths.get("src/test/resources/testimage.jpg"));
//        MockMultipartFile mockImage = new MockMultipartFile(
//                "file",
//                "testimage.jpg",
//                "image/jpeg",
//                imageBytes
//        );
//
//        // Perform the file upload with CSRF token
//        MvcResult result = mockMvc.perform(multipart("/upload")
//                        .file(mockImage)
//                        .with(csrf()))  // Add CSRF token to the request
//                .andExpect(status().is3xxRedirection()) // Expect redirection after upload
//                .andReturn();
//
//        // Verify that the file was uploaded to the correct location
//        String projectDir = System.getProperty("user.dir");
//        File uploadedFile = new File(projectDir + "/uploaded_img/testimage.jpg");
//
//        assertThat(uploadedFile.exists()).isTrue();  // Check if the file exists
//        assertThat(uploadedFile.length()).isEqualTo(mockImage.getSize());  // Check if the file size matches
//    }
//}
