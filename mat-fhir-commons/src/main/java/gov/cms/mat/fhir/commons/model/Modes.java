/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.cms.mat.fhir.commons.model;

import java.io.Serializable;
import java.util.Collection;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author duanedecouteau
 */
@Entity
@Table(name = "MODES")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Modes.findAll", query = "SELECT m FROM Modes m"),
    @NamedQuery(name = "Modes.findById", query = "SELECT m FROM Modes m WHERE m.id = :id"),
    @NamedQuery(name = "Modes.findByModeName", query = "SELECT m FROM Modes m WHERE m.modeName = :modeName")})
public class Modes implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "MODE_NAME")
    private String modeName;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "modeId")
    private Collection<AttributesModes> attributesModesCollection;

    public Modes() {
    }

    public Modes(Integer id) {
        this.id = id;
    }

    public Modes(Integer id, String modeName) {
        this.id = id;
        this.modeName = modeName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getModeName() {
        return modeName;
    }

    public void setModeName(String modeName) {
        this.modeName = modeName;
    }

    @XmlTransient
    public Collection<AttributesModes> getAttributesModesCollection() {
        return attributesModesCollection;
    }

    public void setAttributesModesCollection(Collection<AttributesModes> attributesModesCollection) {
        this.attributesModesCollection = attributesModesCollection;
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
        if (!(object instanceof Modes)) {
            return false;
        }
        Modes other = (Modes) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.Modes[ id=" + id + " ]";
    }
    
}
