package com.example.securingweb;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Controller
public class MainController{

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @GetMapping({"/"})
    public String home(Model model) {
        logger.info("Accessing home controller");

        // Example calculation
        int a = 10;
        int b = 20;
        int sum = a + b; // Calculate the sum of a and b

        // Pass calculation result to the model
        model.addAttribute("message", "Witamy na stronie początkowej!");
        model.addAttribute("calculationResult", sum); // Add the calculation result

        return "home"; // Return the name of the Thymeleaf template
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, Model model) {
        logger.info("Handling file upload");

        if (!file.isEmpty()) {
            try {
                // Save the file to a local directory
                String uploadDir = "uploads/";
                File directory = new File(uploadDir);
                if (!directory.exists()) {
                    directory.mkdir();
                }
                File uploadedFile = new File(uploadDir + file.getOriginalFilename());
                file.transferTo(uploadedFile);
                model.addAttribute("message", "File uploaded successfully: " + file.getOriginalFilename());
            } catch (IOException e) {
                logger.error("Error while uploading file", e);
                model.addAttribute("message", "Failed to upload file: " + e.getMessage());
            }
        } else {
            model.addAttribute("message", "Please select a file to upload.");
        }

        return "home"; // Return to the home view
    }
}