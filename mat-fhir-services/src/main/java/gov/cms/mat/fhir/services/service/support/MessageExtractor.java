package gov.cms.mat.fhir.services.service.support;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class MessageExtractor {
    private static final String END_STRING = "\",";
    private static final String START_STRING = ",\"message\":\"";

    private final String body;
    int start;
    int end;

    public MessageExtractor(String body) {
        this.body = body;
    }

    public List<String> parseMany() {
        String message = parse();

        return processMessages(message);
    }

    public String parse() {
        start = body.indexOf(START_STRING);

        if (start < 0) {
            return handleNotFound("Cannot find body start string: " + START_STRING);
        }

        end = body.indexOf(END_STRING, start);

        if (end < 0) {
            return handleNotFound("Cannot find body end string: " + END_STRING);
        }

        return body.substring(start + START_STRING.length(), end);
    }

    private List<String> processMessages(String message) {
        String[] messages = message.split(",/n");
        log.debug("Found {} messages.", messages.length);

        return Stream.of(messages)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private String handleNotFound(String message) {
        log.warn(message + " body: " + body);
        return message;
    }
}
