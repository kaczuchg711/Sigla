package com.example.securingweb.model;

public class BibleVerse {
    private final int verseNumber;
    private final String text;

    public BibleVerse(int verseNumber, String text) {
        this.verseNumber = verseNumber;
        this.text = text;
    }

    public int getVerseNumber() {
        return verseNumber;
    }

    public String getText() {
        return text;
    }
}
