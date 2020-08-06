package gov.cms.mat.fhir.services.components.reporting;

public enum HapiResourcePersistedState {
    NEW("New"), // if new not persisted
    VALIDATION("Validation"),
    EXISTS("Exists"),
    CREATED("Created");

    public final String value;

    HapiResourcePersistedState(String value) {
        this.value = value;
    }
}
