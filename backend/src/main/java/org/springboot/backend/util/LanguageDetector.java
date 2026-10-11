package org.springboot.backend.util;

import org.springframework.stereotype.Component;

@Component
public class LanguageDetector {

    public String detect(String text) {
        long devanagari = text.codePoints().filter(code -> code >= 0x0900 && code <= 0x097F).count();
        return devanagari > 0 ? "hi" : "en";
    }
}
