package bubblehealth.extract;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MedicationExtractor {

    private static final Pattern MEDICATION_PATTERN = Pattern.compile(
            "(?i)\\b([A-Za-z]+(?:-[A-Za-z]+)?)\\s+\\d+(?:mcg|mg|g|kg|ml|l|dl|IU|units|" +
                    "mg/ml|mcg/ml|g/ml|mg/kg|mcg/kg|mg/m2|tablet|capsule|drops|spray|puffs|" +
                    "suppository|ampoule|vial|mEq|mMol)?\\b"
    );

    // Common instruction words that otherwise slip through the dosage regex
    // (e.g. "FOR 14 days" -> "FOR 14") and aren't medication names.
    private static final Set<String> NON_DRUG_WORDS = Set.of(
            "for", "the", "and", "take", "apply", "daily", "twice", "mouth",
            "area", "with", "from", "into", "onto", "days", "day", "by"
    );

    public List<String> extractMedications(String text) {
        List<String> medications = new ArrayList<>();
        Matcher matcher = MEDICATION_PATTERN.matcher(text);
        while (matcher.find()) {
            String match = matcher.group().trim();
            String drugNamePart = match.split("\\s+")[0];
            if (drugNamePart.length() < 3 || NON_DRUG_WORDS.contains(drugNamePart.toLowerCase())) {
                continue;
            }
            medications.add(match);
        }
        return medications;
    }

    public String findMatchingLine(String multiLineText, String searchString) {
        for (String line : multiLineText.split("\n")) {
            if (line.contains(searchString)) {
                return line;
            }
        }
        return null;
    }

    /** Extracts medication+dosage strings out of already privacy-filtered label lines. */
    public List<String> extractFromLines(List<String> safeLines) {
        List<String> medications = new ArrayList<>();
        for (String line : safeLines) {
            medications.addAll(extractMedications(line));
        }
        return medications;
    }

    /** Strips the trailing dosage/unit text, leaving just the drug name for API lookups. */
    public String stripDosage(String medicationString) {
        return medicationString.replaceAll("(?i)\\s+\\d+.*$", "").trim();
    }
}
