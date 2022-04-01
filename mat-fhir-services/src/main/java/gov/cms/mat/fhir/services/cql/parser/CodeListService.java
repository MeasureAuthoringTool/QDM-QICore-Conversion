package gov.cms.mat.fhir.services.cql.parser;

import gov.cms.mat.fhir.rest.dto.spreadsheet.CodeSystemEntry;
import java.util.List;

public interface CodeListService {
    /**
     * Deprecated
     * @return Returns a hash map keyed by oid, e.g. urn:oid:2.16.840.1.113883.12.292, and valued by VSACCodeSystemDTO with
     * fhir model 4.0.1 url, e.g. http://hl7.org/fhir/sid/cvx, and default VSAC version.
     */

    /**
     * @return List of CodeSystemEntries
     */
    List<CodeSystemEntry> getOidToVsacCodeSystemMap();
}
