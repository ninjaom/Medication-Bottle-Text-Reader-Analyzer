import java.util.regex.*;
import java.util.ArrayList;
import java.util.List;

public class MedicationExtractor {

    /**
     * Extracts medication names with optional dosage information.
     * @param text The input text containing prescription details.
     * @return A list of extracted medication names with dosages.
     */
    public static List<String> extractMedications(String text) {
        // Regex for identifying medication names with dosages
        String medicationPattern = "(?i)\\b([A-Za-z]+(?:-[A-Za-z]+)?)\\s+\\d+(?:mcg|mg|g|kg|ml|l|dl|IU|units|mg/ml|mcg/ml|g/ml|mg/kg|mcg/kg|mg/m2|tablet|capsule|drops|spray|puffs|suppository|ampoule|vial|mEq|mMol)?\\b";
//String medicationPattern = "(?i)\\b([A-Za-z]+(?:\\s+[A-Za-z]+)*)\\s+\\d+(?:mcg|mg|g|kg|ml|l|dl|IU|units|mg/ml|mcg/ml|g/ml|mg/kg|mcg/kg|mg/m2|tablet|capsule|drops|spray|puffs|suppository|ampoule|vial|mEq|mMol)?\\b";
        //String medicationPattern = "(?i)\\b([A-Za-z]+(?:\\s+[A-Za-z]+)*)(?=\\s+\\d)(\\d+(?:\\.\\d+)?)\\s*(mcg|mg|g|kg|ml|l|dl|IU|units|mg/ml|mcg/ml|g/ml|mg/kg|mcg/kg|mg/m2|tablet|capsule|drops|spray|puffs|suppository|ampoule|vial|mEq|mMol)\\b";


        List<String> medications = new ArrayList<>();
        Pattern pattern = Pattern.compile(medicationPattern);
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            medications.add(matcher.group());
        }

        return medications;
    }

    public static String findMatchingLine(String multiLineString, String searchString) {
        // Split the multi-line string into an array of lines
        String[] lines = multiLineString.split("\n");

        // Iterate over each line and check if the search string exists
        for (String line : lines) {
            if (line.contains(searchString)) {
                return line;  // Return the first matching line
            }
        }

        // Return null if no match is found
        return null;
    }

    public static void main(String[] args) {
        // Input prescription text
        String[] texts = {
                """
            Jane Mary Doe II
            Amoxicillin 500mg ca
            MFG Sandoz
            Take 1 capsule by mouth
            twice daily
            for 10 days
            """,

                """
            Samuel Smith
            Vitamin D 1000 IU
            """,

                """
            Jane Demo Jr.
            Insulin 0.5 mL
            """,

                """
            Bugs B Bunny
            1234 Some Ln, SomeCity, TX, 70543
            MFG PERRIGO
            TRIAMCINOLONE 0.025% OIN
            APPLY TOPICALLY TO THE
            AFFECTED AREA TWICE
            DAILY FOR 14 days
            """
        };

        for (String text : texts) {

            // Extract medications
            List<String> medications = extractMedications(text);

            // Print the extracted medications
            System.out.println("Possible Medication strings to send to drug database:");
            for (String medication : medications) {
                System.out.println(findMatchingLine(text, medication));
            }
            System.out.println("---------------------\n\n");

        }
    }
}
