package gov.cms.mat.vsac.model;

import lombok.ToString;

import java.util.List;

/**
 * Container for holding Code System information.
 **/
@ToString
public class MatConceptList {
    /**
     * Container for holding Code System information.
     **/
    private List<MatConcept> conceptList;

    /**
     * Getter - conceptList.
     *
     * @return - conceptList.
     **/
    public final List<MatConcept> getConceptList() {
        return conceptList;
    }

    /**
     * Setter - conceptList.
     *
     * @param conceptLists -List of MatConcept.
     **/
    public final void setConceptList(final List<MatConcept> conceptLists) {
        this.conceptList = conceptLists;
    }

}
