import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.opencv.videoio.VideoCapture;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BubbleHealth {

    public static class TextScraper {
        public static void performOCR(String imagePath, String tessdataPath) {
            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath(tessdataPath); // Set the path to tessdata
            tesseract.setLanguage("eng"); // Set language to English

            try {
                String result = tesseract.doOCR(new java.io.File(imagePath));
                System.out.println("OCR Result: " + result);
            } catch (TesseractException e) {
                System.err.println("Error during OCR: " + e.getMessage());
            }
        }
    }

    public static class CameraCapture {
        static {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // Load OpenCV native library
        }

        public static void captureVideo() {
            VideoCapture camera = new VideoCapture(0);

            if (!camera.isOpened()) {
                System.out.println("Error: Camera is not available.");
                return;
            }

            Mat frame = new Mat();
            System.out.println("Press 'q' to exit the application.");

            while (true) {
                if (camera.read(frame)) {
                    HighGui.imshow("Camera Feed", frame);

                    if (HighGui.waitKey(30) == 'q') {
                        break;
                    }
                } else {
                    System.out.println("Error: Failed to capture image.");
                    break;
                }
            }
            camera.release();
            HighGui.destroyAllWindows();
        }
    }

    public static class MedicationExtractor {

        public static List<String> extractMedications(String text) {
            String medicationPattern = "(?i)\\b([A-Za-z]+(?:-[A-Za-z]+)?)\\s+\\d+(?:mcg|mg|g|kg|ml|l|dl|IU|units|mg/ml|mcg/ml|g/ml|mg/kg|mcg/kg|mg/m2|tablet|capsule|drops|spray|puffs|suppository|ampoule|vial|mEq|mMol)?\\b";
            List<String> medications = new ArrayList<>();
            Pattern pattern = Pattern.compile(medicationPattern);
            Matcher matcher = pattern.matcher(text);

            while (matcher.find()) {
                medications.add(matcher.group());
            }

            return medications;
        }

        public static String findMatchingLine(String multiLineString, String searchString) {
            String[] lines = multiLineString.split("\n");

            for (String line : lines) {
                if (line.contains(searchString)) {
                    return line;
                }
            }
            return null;
        }

        public static void processTexts() {
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
                List<String> medications = extractMedications(text);
                System.out.println("Possible Medication strings to send to drug database:");
                medications.stream().map(medication -> findMatchingLine(text, medication)).forEach(System.out::println);
                System.out.println("---------------------\n\n");
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("BubbleHealth Application");

        // Camera Capture Example
        // Uncomment below to run Camera Capture
        CameraCapture.captureVideo();

        // Text Scraper Example
        TextScraper.performOCR("C:\\Users\\omlok\\Documents\\Bubble Health\\CameraMedicineScraper\\processed_frame.jpg",
                "C:\\Users\\omlok\\Downloads\\tessdata");

        // Medication Extractor Example
        MedicationExtractor.processTexts();
    }
}
