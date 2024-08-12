package com.example.securingweb;

import com.example.securingweb.helpers.ImageDPIReader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import javax.imageio.ImageIO;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageReader;

@Controller
public class MainController {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @GetMapping({"/"})
    public String home(Model model) {
        logger.info("Accessing home controller");

        // Pass calculation result to the model
        model.addAttribute("message", "Witamy na stronie początkowej!");

        return "home"; // Return the name of the Thymeleaf template
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, Model model) {
        logger.info("Handling file upload");
        if (!file.isEmpty()) {
            try {
                logger.info("In try in Handling file upload");
                // Save the file to a local directory
                String uploadDir = "uploaded_img/";
                File directory = new File(uploadDir);
                if (!directory.exists()) {
                    directory.mkdir();
                }
                String projectDir = System.getProperty("user.dir");
                File uploadFile = new File(projectDir + "/uploaded_img/" + file.getOriginalFilename());
                file.transferTo(uploadFile);
                model.addAttribute("message", "File uploaded successfully: " + file.getOriginalFilename());
                String[] Sigla = getSiglaFromImage(uploadFile);
                System.out.println("AAAAAA");
                System.out.println("AAAAAA");
                System.out.println("AAAAAA");
                System.out.println("AAAAAA");
                System.out.println(Arrays.toString(Sigla));

            } catch (IOException e) {
                logger.error("Error while uploading file", e);
                model.addAttribute("message", "Failed to upload file: " + e.getMessage());
            }
        } else {
            model.addAttribute("message", "Please select a file to upload.");
        }

        return "redirect:/"; // Redirect to the home view
    }

    private String[] getSiglaFromImage(File file) {
        try {
            // Odczytaj obraz z pliku i przekonwertuj na BufferedImage
            BufferedImage bufferedImage = ImageIO.read(file);
            int[] dpi = ImageDPIReader.getDPI(file);
            // Inicjalizacja Tesseract OCR
            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath("/usr/share/tesseract-ocr/4.00/tessdata"); // Zmień ścieżkę na właściwą dla twojego systemu
            tesseract.setLanguage("pol"); // Zakładamy, że tekst jest w języku polskim

            // Rozpoznanie tekstu na obrazie z BufferedImage
            String text = tesseract.doOCR(bufferedImage);

            // Wyodrębnienie sigli z tekstu
            return extractSigla(text);

        } catch (IOException | TesseractException e) {
            e.printStackTrace();
            return new String[0]; // W przypadku błędu zwróć pustą tablicę
        } catch (Exception e) {
            logger.info("Problem with reading DPI in ImageDPIReader");
            throw new RuntimeException(e);
        }
    }

    private String[] extractSigla(String text) {
        // Wzorzec regex dla sigli biblijnych
        Pattern pattern = Pattern.compile("\\b(?:[A-Za-z]{2}\\s*\\d+,?\\s*\\d+(?:-\\d+)?(?:,\\d+)?)+\\b");
        Matcher matcher = pattern.matcher(text);

        // Przechowywanie wszystkich dopasowanych sigli
        List<String> siglaList = new ArrayList<>();
        while (matcher.find()) {
            siglaList.add(matcher.group());
        }

        return siglaList.toArray(new String[0]);
    }
}