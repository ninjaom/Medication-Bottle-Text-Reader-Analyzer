package bubblehealth;

import bubblehealth.api.DrugInfoService;
import bubblehealth.api.model.DrugInfo;
import bubblehealth.capture.CameraCapture;
import bubblehealth.extract.MedicationExtractor;
import bubblehealth.extract.PrivacyFilter;
import bubblehealth.ocr.TextScraper;

import java.nio.file.Path;
import java.util.List;

public class Main {

    private static final String DEFAULT_TESSDATA_PATH = "/opt/homebrew/share/tessdata";

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("--demo")) {
            runDemo();
            return;
        }
        runLive(args);
    }

    /** Captures a photo of a bottle label with the webcam and looks up the medication. */
    private static void runLive(String[] args) throws Exception {
        String tessdataPath = System.getenv().getOrDefault("TESSDATA_PREFIX", DEFAULT_TESSDATA_PATH);
        String capturePath = args.length > 0
                ? args[0]
                : Path.of(System.getProperty("java.io.tmpdir"), "bubblehealth-capture.jpg").toString();

        System.out.println("BubbleHealth - point the camera at a medication label.");

        CameraCapture cameraCapture = new CameraCapture();
        String savedFrame = cameraCapture.captureFrame(capturePath);
        if (savedFrame == null) {
            System.out.println("No image captured. Exiting.");
            return;
        }

        TextScraper textScraper = new TextScraper(tessdataPath);
        PrivacyFilter privacyFilter = new PrivacyFilter();
        MedicationExtractor extractor = new MedicationExtractor();
        DrugInfoService drugInfoService = new DrugInfoService();

        List<String> safeLines = privacyFilter.filterPrivateLines(textScraper.getTextLines(savedFrame));
        List<String> medications = extractor.extractFromLines(safeLines);

        if (medications.isEmpty()) {
            System.out.println("Could not find a medication name and dosage on the label. Try again with better lighting.");
            return;
        }

        for (String medication : medications) {
            String drugName = extractor.stripDosage(medication);
            System.out.println("Looking up: " + drugName);
            DrugInfo info = drugInfoService.lookup(drugName);
            System.out.println(drugInfoService.formatForDisplay(info));
        }
    }

    /** Runs the pipeline against sample label text - no camera or Tesseract required. */
    private static void runDemo() throws Exception {
        String[] sampleLabels = {
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

        PrivacyFilter privacyFilter = new PrivacyFilter();
        MedicationExtractor extractor = new MedicationExtractor();
        DrugInfoService drugInfoService = new DrugInfoService();

        for (String label : sampleLabels) {
            System.out.println("=== Raw OCR text ===");
            System.out.println(label);

            List<String> safeLines = privacyFilter.filterPrivateLines(label);
            System.out.println("Privacy-filtered lines sent onward: " + safeLines);

            List<String> medications = extractor.extractFromLines(safeLines);
            System.out.println("Detected medication strings: " + medications);

            for (String medication : medications) {
                String drugName = extractor.stripDosage(medication);
                System.out.println("Looking up: " + drugName);
                DrugInfo info = drugInfoService.lookup(drugName);
                System.out.println(drugInfoService.formatForDisplay(info));
            }
            System.out.println("---------------------\n");
        }
    }
}
