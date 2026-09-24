package bubblehealth.api;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Thin client over the NIH RxNav / RxNorm REST API (https://rxnav.nlm.nih.gov),
 * free and keyless. Used to normalize a noisy OCR'd drug name into a name
 * openFDA is more likely to recognize, correcting simple misreads along the way.
 */
public class RxNormClient {

    private static final String BASE_URL = "https://rxnav.nlm.nih.gov/REST";

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /** Resolves a (possibly misspelled) drug name to a canonical RxNorm name, or null if none found. */
    public String resolveCanonicalName(String rawName) throws IOException, InterruptedException {
        if (isKnownName(rawName)) {
            return rawName;
        }
        return firstSpellingSuggestion(rawName);
    }

    private boolean isKnownName(String name) throws IOException, InterruptedException {
        String url = BASE_URL + "/rxcui.json?name=" + encode(name);
        JSONObject json = getJson(url);
        JSONArray ids = json.optJSONObject("idGroup") != null
                ? json.getJSONObject("idGroup").optJSONArray("rxnormId")
                : null;
        return ids != null && !ids.isEmpty();
    }

    private String firstSpellingSuggestion(String name) throws IOException, InterruptedException {
        String url = BASE_URL + "/spellingsuggestions.json?name=" + encode(name);
        JSONObject json = getJson(url);
        JSONObject group = json.optJSONObject("suggestionGroup");
        JSONObject list = group != null ? group.optJSONObject("suggestionList") : null;
        JSONArray suggestions = list != null ? list.optJSONArray("suggestion") : null;
        if (suggestions == null || suggestions.isEmpty()) {
            return null;
        }
        return suggestions.getString(0);
    }

    private JSONObject getJson(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        return new JSONObject(response.body());
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
