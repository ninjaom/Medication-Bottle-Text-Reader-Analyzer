package bubblehealth.api.model;

public class DrugInfo {

    public final String queriedName;
    public final String matchedName;
    public final String dosageAndAdministration;
    public final String sideEffects;
    public final String interactions;
    public final String warnings;
    public final boolean found;

    private DrugInfo(String queriedName, String matchedName, String dosageAndAdministration,
                      String sideEffects, String interactions, String warnings, boolean found) {
        this.queriedName = queriedName;
        this.matchedName = matchedName;
        this.dosageAndAdministration = dosageAndAdministration;
        this.sideEffects = sideEffects;
        this.interactions = interactions;
        this.warnings = warnings;
        this.found = found;
    }

    public static DrugInfo notFound(String queriedName) {
        return new DrugInfo(queriedName, null, null, null, null, null, false);
    }

    public static DrugInfo of(String queriedName, String matchedName, String dosageAndAdministration,
                               String sideEffects, String interactions, String warnings) {
        return new DrugInfo(queriedName, matchedName, dosageAndAdministration,
                sideEffects, interactions, warnings, true);
    }
}
