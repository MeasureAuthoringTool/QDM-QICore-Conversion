package gov.cms.mat.cql;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public enum CqlNegations {
    NOT_PERFORMED("Not Performed"),
    NOT_ORDERED("Not Ordered"),
    NOT_RECOMMENDED("Not Recommended"),
    NOT_ADMINISTERED("Not Administered"),
    NOT_DISPENSED("Not dispensed");

    public final String tag;

    CqlNegations(String tag) {
        this.tag = tag;
    }

    public static String[] getNegations() {
        String[] negations = new String[CqlNegations.values().length];
        AtomicInteger x = new AtomicInteger();

        Arrays.stream(CqlNegations.values()).forEach(c -> negations[x.getAndIncrement()] = c.tag);

        return negations;
    }
}
