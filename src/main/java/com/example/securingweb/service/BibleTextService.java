package com.example.securingweb.service;

import com.example.securingweb.model.BiblePassage;
import com.example.securingweb.model.BibleVerse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BibleTextService {

    private static final Logger logger = LoggerFactory.getLogger(BibleTextService.class);
    private static final Pattern SIGLA_PATTERN = Pattern.compile(
            "(?i)^(?<book>[1-3]?\\s*[\\p{L}\\.]{1,})\\s*(?<chapter>\\d{1,3})\\s*[,:\\.]\\s*(?<start>\\d{1,3})(?:\\s*[-–]\\s*(?<end>\\d{1,3}))?$"
    );
    private static final Map<String, String> BOOK_ALIASES = createBookAliasMap();

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Map<Integer, Map<Integer, String>>> bibleIndex = new HashMap<>();

    @PostConstruct
    void loadBibleText() {
        try (InputStream inputStream = getClass().getResourceAsStream("/bible/bible_texts.json")) {
            if (inputStream == null) {
                logger.warn("Nie znaleziono pliku z tekstem Biblii. Funkcjonalność pobierania wersetów będzie ograniczona.");
                return;
            }
            List<BibleVerseEntry> entries = objectMapper.readValue(inputStream, new TypeReference<>() {});
            for (BibleVerseEntry entry : entries) {
                bibleIndex
                        .computeIfAbsent(entry.book(), key -> new HashMap<>())
                        .computeIfAbsent(entry.chapter(), key -> new HashMap<>())
                        .put(entry.verse(), entry.text());
            }
            logger.info("Załadowano {} wersetów z zasobów.", entries.size());
        } catch (IOException e) {
            logger.error("Nie udało się załadować tekstu Biblii", e);
        }
    }

    public List<BiblePassage> resolvePassages(List<String> rawSigla) {
        if (rawSigla == null || rawSigla.isEmpty()) {
            return Collections.emptyList();
        }

        List<BiblePassage> passages = new ArrayList<>();
        for (String siglum : rawSigla) {
            if (siglum == null || siglum.isBlank()) {
                continue;
            }

            String cleanedSiglum = siglum.trim();
            Optional<BibleReference> referenceOptional = parseReference(cleanedSiglum);
            if (referenceOptional.isEmpty()) {
                passages.add(new BiblePassage(cleanedSiglum, Collections.emptyList(), "Nie udało się zinterpretować sigli."));
                continue;
            }

            BibleReference reference = referenceOptional.get();
            Map<Integer, Map<Integer, String>> chapters = bibleIndex.get(reference.book());
            if (chapters == null) {
                passages.add(new BiblePassage(cleanedSiglum, Collections.emptyList(), "Brak tekstu dla księgi: " + cleanedSiglum));
                continue;
            }

            Map<Integer, String> versesInChapter = chapters.get(reference.chapter());
            if (versesInChapter == null) {
                passages.add(new BiblePassage(cleanedSiglum, Collections.emptyList(), "Brak tekstu dla rozdziału: " + cleanedSiglum));
                continue;
            }

            List<BibleVerse> verses = new ArrayList<>();
            List<Integer> missingVerses = new ArrayList<>();
            for (int verse = reference.startVerse(); verse <= reference.endVerse(); verse++) {
                String text = versesInChapter.get(verse);
                if (text == null) {
                    missingVerses.add(verse);
                } else {
                    verses.add(new BibleVerse(verse, text));
                }
            }

            if (verses.isEmpty()) {
                passages.add(new BiblePassage(cleanedSiglum, Collections.emptyList(), "Nie znaleziono wersetów dla: " + cleanedSiglum));
            } else if (!missingVerses.isEmpty()) {
                String errorMessage = "Brak części wersetów: " + missingVerses;
                passages.add(new BiblePassage(cleanedSiglum, verses, errorMessage));
            } else {
                passages.add(new BiblePassage(cleanedSiglum, verses, null));
            }
        }
        return passages;
    }

    private Optional<BibleReference> parseReference(String rawSiglum) {
        Matcher matcher = SIGLA_PATTERN.matcher(rawSiglum.replace('\n', ' '));
        if (!matcher.find()) {
            return Optional.empty();
        }

        String bookGroup = matcher.group("book");
        String chapterGroup = matcher.group("chapter");
        String startGroup = matcher.group("start");
        String endGroup = matcher.group("end");

        if (bookGroup == null || chapterGroup == null || startGroup == null) {
            return Optional.empty();
        }

        String normalizedBook = normalizeBook(bookGroup);
        if (normalizedBook.isBlank()) {
            return Optional.empty();
        }

        int chapter = Integer.parseInt(chapterGroup);
        int startVerse = Integer.parseInt(startGroup);
        int endVerse = endGroup == null ? startVerse : Integer.parseInt(endGroup);
        if (endVerse < startVerse) {
            int tmp = startVerse;
            startVerse = endVerse;
            endVerse = tmp;
        }

        return Optional.of(new BibleReference(normalizedBook, chapter, startVerse, endVerse));
    }

    private String normalizeBook(String book) {
        String withoutDots = book.replace(".", " ");
        String noDiacritics = Normalizer.normalize(withoutDots, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        String normalized = noDiacritics
                .replaceAll("\\s+", "")
                .toUpperCase(Locale.ROOT);
        return BOOK_ALIASES.getOrDefault(normalized, normalized);
    }

    private record BibleVerseEntry(String book, int chapter, int verse, String text) {
    }

    private record BibleReference(String book, int chapter, int startVerse, int endVerse) {
    }

    private static Map<String, String> createBookAliasMap() {
        Map<String, String> aliases = new HashMap<>();
        aliases.put("GEN", "GEN");
        aliases.put("RDZ", "GEN");
        aliases.put("GN", "GEN");

        aliases.put("EX", "EXOD");
        aliases.put("EXOD", "EXOD");
        aliases.put("WYJ", "EXOD");
        aliases.put("WJ", "EXOD");

        aliases.put("PS", "PS");
        aliases.put("PSALM", "PS");
        aliases.put("PSLM", "PS");

        aliases.put("J", "JOHN");
        aliases.put("JN", "JOHN");
        aliases.put("JAN", "JOHN");
        aliases.put("JOHN", "JOHN");

        aliases.put("1KOR", "1COR");
        aliases.put("IKOR", "1COR");
        aliases.put("1COR", "1COR");

        aliases.put("MT", "MAT");
        aliases.put("MAT", "MAT");

        return aliases;
    }
}
