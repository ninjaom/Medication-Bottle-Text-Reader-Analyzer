package bubblehealth.extract;

import net.sourceforge.tess4j.Word;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


public class PrivacyFilter {

    private static final int NAME_LINES_FROM_TOP = 1;

    private static final Pattern ADDRESS_PATTERN = Pattern.compile(
            "(?i)\\d{1,6}\\s+[A-Za-z0-9.'\\s]+,.*[A-Z]{2}\\W*\\d{5}"
    );

    private static final Pattern BARE_NAME_PATTERN = Pattern.compile(
            "^(?:[A-Z][a-zA-Z.'-]*\\s+){1,3}[A-Z][a-zA-Z.'-]*\\.?$"
    );

    private static final Pattern LABEL_KEYWORDS = Pattern.compile(
            "(?i)\\b(mfg|mg|mcg|ml|iu|tablet|capsule|take|apply|daily|twice|" +
                    "topically|oral|mouth|days?|refill|qty|rx|pharmacy|discard)\\b"
    );

    /** Filters recognized text lines using their on-image position (top to bottom). */
    public List<String> filterPrivateLines(List<Word> lines) {
        List<String> safeLines = new ArrayList<>();
        int lineIndex = 0;
        for (Word word : lines) {
            String text = word.getText().trim();
            if (text.isEmpty()) continue;
            boolean isTopOfLabel = lineIndex < NAME_LINES_FROM_TOP;
            lineIndex++;
            if (isTopOfLabel || isPrivate(text)) continue;
            safeLines.add(text);
        }
        return safeLines;
    }

    /** Same filtering for callers that only have plain OCR text (no bounding boxes). */
    public List<String> filterPrivateLines(String multiLineText) {
        List<String> safeLines = new ArrayList<>();
        int lineIndex = 0;
        for (String raw : multiLineText.split("\n")) {
            String text = raw.trim();
            if (text.isEmpty()) continue;
            boolean isTopOfLabel = lineIndex < NAME_LINES_FROM_TOP;
            lineIndex++;
            if (isTopOfLabel || isPrivate(text)) continue;
            safeLines.add(text);
        }
        return safeLines;
    }

    private boolean isPrivate(String text) {
        if (ADDRESS_PATTERN.matcher(text).find()) return true;
        return !LABEL_KEYWORDS.matcher(text).find() && BARE_NAME_PATTERN.matcher(text).matches();
    }
}
