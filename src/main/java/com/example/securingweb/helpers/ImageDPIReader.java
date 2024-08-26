package com.example.securingweb.helpers;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import java.io.File;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageDPIReader {

    private static final Logger logger = LoggerFactory.getLogger(ImageDPIReader.class);

    public static int[] getDPI(File file)  {
        try {
            ImageInputStream inputStream = ImageIO.createImageInputStream(file);
            Iterator<ImageReader> readers = ImageIO.getImageReaders(inputStream);

            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                reader.setInput(inputStream);

                IIOMetadata metadata = reader.getImageMetadata(0);
                String[] names = metadata.getMetadataFormatNames();
                for (String name : names) {
                    Node root = metadata.getAsTree(name);
                    int[] dpi = getDPIFromNode(root);
                    if (dpi != null) {
                        return dpi;
                    }
                }
            }
        } catch (java.lang.Exception e) {
            logger.warn("problem with reading dpi set default {300, 300}");
            return new int[]{300, 300};  // lub inna wartość, którą uznasz za odpowiednią

        }
        return null;
    }

    private static int[] getDPIFromNode(Node node) {
        if (node == null) {
            return null;
        }

        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node childNode = nodeList.item(i);
            if (childNode.getNodeName().equalsIgnoreCase("HorizontalPixelSize")) {
                float horizontalSize = Float.parseFloat(childNode.getTextContent());
                int dpiX = (int) (25.4f / horizontalSize + 0.5f);

                // Szukaj także "VerticalPixelSize" w tym samym węźle
                for (int j = 0; j < nodeList.getLength(); j++) {
                    Node siblingNode = nodeList.item(j);
                    if (siblingNode.getNodeName().equalsIgnoreCase("VerticalPixelSize")) {
                        float verticalSize = Float.parseFloat(siblingNode.getTextContent());
                        int dpiY = (int) (25.4f / verticalSize + 0.5f);
                        return new int[]{dpiX, dpiY};
                    }
                }
                return new int[]{dpiX, dpiX}; // Zakładając, że DPI jest symetryczne, jeśli nie znaleziono VerticalPixelSize
            }
        }

        // Przeszukiwanie rekursywne
        for (int i = 0; i < nodeList.getLength(); i++) {
            int[] dpi = getDPIFromNode(nodeList.item(i));
            if (dpi != null) {
                return dpi;
            }
        }

        return null;
    }
}
