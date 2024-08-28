package com.example.securingweb;

import com.example.securingweb.helpers.ImageDPIReader;
import net.sourceforge.tess4j.TessAPI;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
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
                String[] sigla = getSiglaFromImage(uploadFile);
                System.out.println("AAAAAA");
                System.out.println("AAAAAA");
                System.out.println("AAAAAA");
                System.out.println("AAAAAA");
                System.out.println(Arrays.toString(sigla));
                model.addAttribute("sigla", sigla);

            } catch (IOException e) {
                logger.error("Error while uploading file", e);
                model.addAttribute("message", "Failed to upload file: " + e.getMessage());
            }
        } else {
            model.addAttribute("message", "Please select a file to upload.");
        }
        logger.info("before redirect");
        return "home";
    }

    private String[] getSiglaFromImage(File file) {
        try {
            BufferedImage bufferedImage = ImageIO.read(file);
            int[] dpi = ImageDPIReader.getDPI(file);

            int expectedDPI = 300; // Ustalona wartość DPI, której oczekujesz
            double scaleX = (double) expectedDPI / dpi[0];
            double scaleY = (double) expectedDPI / dpi[1];

            if (scaleX != 1.0 || scaleY != 1.0)
                bufferedImage = scaleImage(bufferedImage, scaleX, scaleY);


            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath("/usr/share/tesseract-ocr/4.00/tessdata");
            tesseract.setLanguage("pol");
            tesseract.setOcrEngineMode(TessAPI.TessOcrEngineMode.OEM_LSTM_ONLY); // Przykład użycia LSTM, który jest bardziej dokładny
            tesseract.setPageSegMode(TessAPI.TessPageSegMode.PSM_AUTO); // Automatyczne wykrywanie układu strony


            // Rozpoznanie tekstu na obrazie z BufferedImage
            String text = tesseract.doOCR(bufferedImage);

            String[] sigla = extractSigla(text);
            for (int i = 0; i < sigla.length; i++) {
                sigla[i] = sigla[i].replace("\n", " ");
            }
            return sigla;
        } catch (IOException | TesseractException e) {
            e.printStackTrace();
            logger.warn("problem with reading sigla. empty table returned");
            return new String[0]; // W przypadku błędu zwróć pustą tablicę
        } catch (Exception e) {
            logger.info("Problem in getSiglaFromImage");
            throw new RuntimeException(e);
        }
    }

    private BufferedImage scaleImage(BufferedImage originalImage, double scaleX, double scaleY) {
        int newWidth = (int) (originalImage.getWidth() * scaleX);
        int newHeight = (int) (originalImage.getHeight() * scaleY);
        BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, originalImage.getType());
        Graphics2D g2d = scaledImage.createGraphics();
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        return scaledImage;
    }

    private String[] extractSigla(String text) {
        // Wzorzec regex dla sigli biblijnych
        Pattern pattern = Pattern.compile("\\b(?:\\d*\\s*[1-3]?[A-Za-z]{2,}\\s*\\d{1,3},\\s*\\d{1,3}(?:-\\d{1,3})?)\\b");

        Matcher matcher = pattern.matcher(text);

        // Przechowywanie wszystkich dopasowanych sigli
        List<String> siglaList = new ArrayList<>();
        while (matcher.find()) {
            siglaList.add(matcher.group());
        }
        return siglaList.toArray(new String[0]);
    }
}