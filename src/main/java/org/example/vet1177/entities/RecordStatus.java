package org.example.vet1177.entities;

public enum RecordStatus {
    OPEN, IN_PROGRESS, AWAITING_INFO, CLOSED;

    public boolean isFinal() {
        return this == CLOSED;
    }
}
