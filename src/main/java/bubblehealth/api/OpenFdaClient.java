package bubblehealth.api;

import bubblehealth.api.model.DrugInfo;
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
 * Thin client over the openFDA drug label API (https://open.fda.gov), free and
 * keyless (rate-limited to 40 req/min; set OPENFDA_API_KEY to raise that to
 * 240 req/min with a free key from open.fda.gov/apis/authentication). Labels
 * come straight from FDA-approved package inserts, so a single lookup covers
 * dosage, side effects, and interaction warnings.
 */
public class OpenFdaClient {

    private static final String BASE_URL = "https://api.fda.gov/drug/label.json";

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final String apiKey = System.getenv("OPENFDA_API_KEY");

    public DrugInfo lookup(String queriedName, String canonicalName) throws IOException, InterruptedException {
        String nameToSearch = canonicalName != null ? canonicalName : queriedName;

        JSONObject result = search("openfda.generic_name", nameToSearch);
        if (result == null) {
            result = search("openfda.brand_name", nameToSearch);
        }
        if (result == null) {
            return DrugInfo.notFound(queriedName);
        }

        String matchedName = firstOpenFdaValue(result, "brand_name");
        if (matchedName == null) {
            matchedName = firstOpenFdaValue(result, "generic_name");
        }
        if (matchedName == null) {
            matchedName = nameToSearch;
        }

        return DrugInfo.of(
                queriedName,
                matchedName,
                firstField(result, "dosage_and_administration"),
                firstField(result, "adverse_reactions"),
                firstField(result, "drug_interactions"),
                firstNonNull(
                        firstField(result, "warnings"),
                        firstField(result, "warnings_and_cautions"),
                        firstField(result, "contraindications")
                )
        );
    }

    private JSONObject search(String field, String name) throws IOException, InterruptedException {
        String query = field + ":\"" + name + "\"";
        StringBuilder url = new StringBuilder(BASE_URL)
                .append("?search=").append(encode(query))
                .append("&limit=1");
        if (apiKey != null && !apiKey.isBlank()) {
            url.append("&api_key=").append(encode(apiKey));
        }

        HttpRequest request = HttpRequest.newBuilder(URI.create(url.toString()))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            return null; // openFDA returns 404 when nothing matches
        }

        JSONObject json = new JSONObject(response.body());
        JSONArray results = json.optJSONArray("results");
        if (results == null || results.isEmpty()) {
            return null;
        }
        return results.getJSONObject(0);
    }

    private String firstField(JSONObject result, String field) {
        JSONArray arr = result.optJSONArray(field);
        return (arr == null || arr.isEmpty()) ? null : arr.getString(0);
    }

    private String firstOpenFdaValue(JSONObject result, String field) {
        JSONObject openfda = result.optJSONObject("openfda");
        if (openfda == null) return null;
        JSONArray arr = openfda.optJSONArray(field);
        return (arr == null || arr.isEmpty()) ? null : arr.getString(0);
    }

    private String firstNonNull(String... values) {
        for (String v : values) {
            if (v != null) return v;
        }
        return null;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
