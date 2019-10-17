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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
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
@Table(name = "ATTRIBUTES_MODES")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "AttributesModes.findAll", query = "SELECT a FROM AttributesModes a"),
    @NamedQuery(name = "AttributesModes.findById", query = "SELECT a FROM AttributesModes a WHERE a.id = :id"),
    @NamedQuery(name = "AttributesModes.findByAttributeId", query = "SELECT a FROM AttributesModes a WHERE a.attributeId = :attributeId")})
public class AttributesModes implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "ATTRIBUTE_ID")
    private String attributeId;
    @JoinColumn(name = "MODE_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private Modes modeId;

    public AttributesModes() {
    }

    public AttributesModes(Integer id) {
        this.id = id;
    }

    public AttributesModes(Integer id, String attributeId) {
        this.id = id;
        this.attributeId = attributeId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(String attributeId) {
        this.attributeId = attributeId;
    }

    public Modes getModeId() {
        return modeId;
    }

    public void setModeId(Modes modeId) {
        this.modeId = modeId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AttributesModes)) {
            return false;
        }
        AttributesModes other = (AttributesModes) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.AttributesModes[ id=" + id + " ]";
    }
    
}
