package org.example.vet1177.entities;

public enum RecordStatus {
    OPEN("Öppen"),
    IN_PROGRESS("Under behandling"),
    AWAITING_INFO("Väntar på svar"),
    CLOSED("Avslutad");

    private final String displayLabel;

    RecordStatus(String displayLabel) {
        this.displayLabel = displayLabel;
    }

    // Svensk visningstext. Används i serverskrivna aktivitetsloggar så att
    // loggraderna i activity_log lagras på samma språk som övriga beskrivningar.
    // Frontend har motsvarande STATUS_MAP i statusHelper.js.
    public String displayLabel() {
        return displayLabel;
    }

    public boolean isFinal() {
        return this == CLOSED;
    }
}
