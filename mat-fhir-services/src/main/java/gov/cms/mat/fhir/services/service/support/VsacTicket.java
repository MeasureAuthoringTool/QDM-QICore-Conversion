package gov.cms.mat.fhir.services.service.support;

import lombok.Getter;

import java.time.Instant;

public abstract class VsacTicket {
    @Getter
    private final String ticket;
    private final Instant timeStamp;

    public VsacTicket(String ticket) {
        this.ticket = ticket;
        timeStamp = Instant.now();
    }

    protected abstract long getTimeOutSeconds();

    public boolean isInValid() {
        return timeStamp.plusSeconds(getTimeOutSeconds()).isBefore(Instant.now());
    }
}
