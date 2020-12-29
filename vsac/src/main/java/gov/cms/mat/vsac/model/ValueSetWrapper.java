package gov.cms.mat.vsac.model;

import lombok.ToString;

import java.util.ArrayList;

/**
 * This class holds list of MatValueSet.
 **/
@ToString
public class ValueSetWrapper {
    /**
     * List of MatValueSet.
     **/
    private ArrayList<VsacValueSet> vsacValueSetList;

    /**
     * Getter Method.
     *
     * @return valueSetList.
     **/
    public final ArrayList<VsacValueSet> getVsacValueSetList() {
        return vsacValueSetList;
    }

    /**
     * Setter Method.
     *
     * @param vsacValueSet - List of MatValueSet.
     **/
    public final void setVsacValueSetList(final ArrayList<VsacValueSet> vsacValueSet) {
        this.vsacValueSetList = vsacValueSet;
    }

}
