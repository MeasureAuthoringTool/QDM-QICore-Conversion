package gov.cms.mat.fhir.services.cql;

import gov.cms.mat.fhir.commons.model.HumanReadableArtifacts;
import org.apache.commons.lang3.tuple.Pair;

public interface LibraryCqlVisitorFactory {
    LibraryCqlVisitor getLibraryCqlVisitor();
    LibraryCqlVisitor visit(String cql);
    Pair<LibraryCqlVisitor, HumanReadableArtifacts> visitAndCollateHumanReadable(String cql);
}
