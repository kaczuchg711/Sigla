package com.example.securingweb.model;

import java.util.List;

public class BiblePassage {
    private final String originalReference;
    private final List<BibleVerse> verses;
    private final String errorMessage;

    public BiblePassage(String originalReference, List<BibleVerse> verses, String errorMessage) {
        this.originalReference = originalReference;
        this.verses = verses;
        this.errorMessage = errorMessage;
    }

    public String getOriginalReference() {
        return originalReference;
    }

    public List<BibleVerse> getVerses() {
        return verses;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean hasError() {
        return errorMessage != null && !errorMessage.isBlank();
    }
}
