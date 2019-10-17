/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.cms.mat.fhir.commons.model;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author duanedecouteau
 */
@Entity
@Table(name = "MEASURE_SHARE")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MeasureShare.findAll", query = "SELECT m FROM MeasureShare m"),
    @NamedQuery(name = "MeasureShare.findByMeasureShareId", query = "SELECT m FROM MeasureShare m WHERE m.measureShareId = :measureShareId")})
public class MeasureShare implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "MEASURE_SHARE_ID")
    private String measureShareId;
    @JoinColumn(name = "SHARE_LEVEL_ID", referencedColumnName = "SHARE_LEVEL_ID")
    @ManyToOne(optional = false)
    private ShareLevel shareLevelId;
    @JoinColumn(name = "MEASURE_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private Measure measureId;
    @JoinColumn(name = "MEASURE_OWNER_USER_ID", referencedColumnName = "USER_ID")
    @ManyToOne(optional = false)
    private User measureOwnerUserId;
    @JoinColumn(name = "SHARE_USER_ID", referencedColumnName = "USER_ID")
    @ManyToOne(optional = false)
    private User shareUserId;

    public MeasureShare() {
    }

    public MeasureShare(String measureShareId) {
        this.measureShareId = measureShareId;
    }

    public String getMeasureShareId() {
        return measureShareId;
    }

    public void setMeasureShareId(String measureShareId) {
        this.measureShareId = measureShareId;
    }

    public ShareLevel getShareLevelId() {
        return shareLevelId;
    }

    public void setShareLevelId(ShareLevel shareLevelId) {
        this.shareLevelId = shareLevelId;
    }

    public Measure getMeasureId() {
        return measureId;
    }

    public void setMeasureId(Measure measureId) {
        this.measureId = measureId;
    }

    public User getMeasureOwnerUserId() {
        return measureOwnerUserId;
    }

    public void setMeasureOwnerUserId(User measureOwnerUserId) {
        this.measureOwnerUserId = measureOwnerUserId;
    }

    public User getShareUserId() {
        return shareUserId;
    }

    public void setShareUserId(User shareUserId) {
        this.shareUserId = shareUserId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (measureShareId != null ? measureShareId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MeasureShare)) {
            return false;
        }
        MeasureShare other = (MeasureShare) object;
        if ((this.measureShareId == null && other.measureShareId != null) || (this.measureShareId != null && !this.measureShareId.equals(other.measureShareId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.MeasureShare[ measureShareId=" + measureShareId + " ]";
    }
    
}
