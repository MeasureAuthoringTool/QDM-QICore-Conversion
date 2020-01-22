package gov.cms.mat.fhir.services.components.mongo;

public enum HapiResourcePersistedState {
    NEW("New"), // if new not persisted
    EXISTS("Exists"), CREATED("Created");

    public final String value;

    HapiResourcePersistedState(String value) {
        this.value = value;
    }
}
