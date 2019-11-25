/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.cms.mat.fhir.commons.objects;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author duanedecouteau
 */
@XmlRootElement
public class CQLSourceForTranslation {
    private String id;
    private String measureId;
    private String cql;
    private String qdmVersion;
    private String releaseVersion;

    /**
     * @return the id
     */
    @XmlElement
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the measureId
     */
    @XmlElement
    public String getMeasureId() {
        return measureId;
    }

    /**
     * @param measureId the measureId to set
     */
    public void setMeasureId(String measureId) {
        this.measureId = measureId;
    }

    /**
     * @return the cql
     */
    public String getCql() {
        return cql;
    }

    /**
     * @param cql the cql to set
     */
    @XmlElement
    public void setCql(String cql) {
        this.cql = cql;
    }

    /**
     * @return the qdmVersion
     */
    @XmlElement
    public String getQdmVersion() {
        return qdmVersion;
    }

    /**
     * @param qdmVersion the qdmVersion to set
     */
    
    public void setQdmVersion(String qdmVersion) {
        this.qdmVersion = qdmVersion;
    }

    /**
     * @return the releaseVersion
     */
    @XmlElement
    public String getReleaseVersion() {
        return releaseVersion;
    }

    /**
     * @param releaseVersion the releaseVersion to set
     */
    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }
    
    
    
}
