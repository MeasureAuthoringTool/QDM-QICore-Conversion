package gov.cms.mat.fhir.services.cql;

import gov.cms.mat.fhir.commons.model.HumanReadableArtifacts;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import org.apache.commons.lang3.tuple.Pair;
import org.cqframework.cql.gen.cqlParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LibraryCqlVisitorFactoryImpl implements LibraryCqlVisitorFactory {
    private final CQLAntlrUtils cqlAntlrUtils;
    private final HapiFhirServer hapiServier;

    @Value("${mat-fhir-base}")
    private String matFhirBaseUrl;

    public LibraryCqlVisitorFactoryImpl(CQLAntlrUtils cqlAntlrUtils,
                                        HapiFhirServer hapiServier) {
        this.cqlAntlrUtils = cqlAntlrUtils;
        this.hapiServier = hapiServier;
    }

    public LibraryCqlVisitor getLibraryCqlVisitor() {
        return new LibraryCqlVisitor(hapiServier,
                cqlAntlrUtils,
                this,
                matFhirBaseUrl);
    }

    public LibraryCqlVisitor visit(String cql) {
        LibraryCqlVisitor result = getLibraryCqlVisitor();
        cqlParser.LibraryContext ctx = cqlAntlrUtils.getLibraryContext(cql);
        result.visit(ctx);
        return result;
    }

    public Pair<LibraryCqlVisitor, HumanReadableArtifacts> visitAndCollateHumanReadable(String cql) {
        // HuamnReadable has to be recursively run while FHIR json related artifacts are just based on the lib.

        // First visit everything in the tree.
        LibraryCqlVisitor left = visit(cql);

        // Now collate all the HumanReadables in the tree.
        HumanReadableArtifacts right = new HumanReadableArtifacts();
        collateHumanReadable(right, left);
        return Pair.of(left, right);
    }

    private void collateHumanReadable(HumanReadableArtifacts result, LibraryCqlVisitor child) {
        // Populate contents.
        result.getTerminologyValueSetModels().addAll(child.getHumanReadableArtifacts().getTerminologyValueSetModels());
        result.getTerminologyCodeModels().addAll(child.getHumanReadableArtifacts().getTerminologyCodeModels());
        result.getDataReqValueSets().addAll(child.getHumanReadableArtifacts().getDataReqValueSets());
        result.getDataReqCodes().addAll(child.getHumanReadableArtifacts().getDataReqCodes());

        // Now populate all children recursively.
        child.getLibMap().values().forEach((pair) -> collateHumanReadable(result, pair.getRight()));
    }
}
