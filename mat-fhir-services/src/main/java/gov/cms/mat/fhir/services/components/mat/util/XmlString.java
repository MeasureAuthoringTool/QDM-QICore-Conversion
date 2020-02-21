package gov.cms.mat.fhir.services.components.mat.util;

import java.util.ArrayList;
import java.util.List;

public class XmlString {
    private static final String START_TAG = "<group sequence=";
    private static final String END_TAG = "</group>";
    private static final String XML_BASE = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><measureGrouping>%s</measureGrouping>";

    private final String xml;
    private int pointer = 0;

    public XmlString(String xml) {
        this.xml = xml;
    }

    public List<String> process() {
        List<String> docs = new ArrayList<>();
        while (true) {
            String data = getMeasurePackageDetails();

            if (data == null) {
                break;
            } else {
                docs.add(String.format(XML_BASE, data));
            }
        }

        return docs;
    }

    public String getMeasurePackageDetails() {
        int start = xml.indexOf(START_TAG, pointer);

        if (start == -1) {
            return null;
        }

        pointer = xml.indexOf(END_TAG, start);
        return xml.substring(start, pointer + END_TAG.length());
    }

}
