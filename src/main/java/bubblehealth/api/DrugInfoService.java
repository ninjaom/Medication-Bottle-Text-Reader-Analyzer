package bubblehealth.api;

import bubblehealth.api.model.DrugInfo;

import java.io.IOException;

/**
 * Combines RxNorm name normalization with an openFDA label lookup, and formats
 * the result in short, plain-language sections aimed at readers who may not be
 * comfortable digging through a package insert themselves.
 */
public class DrugInfoService {

    private final RxNormClient rxNormClient = new RxNormClient();
    private final OpenFdaClient openFdaClient = new OpenFdaClient();

    public DrugInfo lookup(String drugName) throws IOException, InterruptedException {
        String canonicalName = rxNormClient.resolveCanonicalName(drugName);
        return openFdaClient.lookup(drugName, canonicalName);
    }

    public String formatForDisplay(DrugInfo info) {
        if (!info.found) {
            return "Could not find information for \"" + info.queriedName + "\". "
                    + "Try rescanning the label or check the spelling.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Medication: ").append(info.matchedName).append("\n");
        sb.append("  Dosage: ").append(summarize(info.dosageAndAdministration)).append("\n");
        sb.append("  Possible side effects: ").append(summarize(info.sideEffects)).append("\n");
        sb.append("  Don't take with: ").append(summarize(info.interactions)).append("\n");
        sb.append("  Warnings: ").append(summarize(info.warnings)).append("\n");
        return sb.toString();
    }

    private String summarize(String text) {
        if (text == null || text.isBlank()) {
            return "No information available.";
        }
        String[] sentences = text.trim().split("(?<=[.!?])\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(3, sentences.length); i++) {
            sb.append(sentences[i]).append(" ");
        }
        return sb.toString().trim();
    }
}
