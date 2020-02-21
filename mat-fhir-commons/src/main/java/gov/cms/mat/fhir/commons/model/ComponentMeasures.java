package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@Entity
@Table(name = "COMPONENT_MEASURES")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ComponentMeasures.findAll", query = "SELECT c FROM ComponentMeasures c"),
    @NamedQuery(name = "ComponentMeasures.findById", query = "SELECT c FROM ComponentMeasures c WHERE c.id = :id")})
public class ComponentMeasures implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Lob
    @Column(name = "ALIAS")
    private String alias;
    @JoinColumn(name = "COMPONENT_MEASURE_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private Measure componentMeasureId;
    @JoinColumn(name = "COMPOSITE_MEASURE_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private Measure compositeMeasureId;

    public ComponentMeasures() {
    }

    public ComponentMeasures(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Measure getComponentMeasureId() {
        return componentMeasureId;
    }

    public void setComponentMeasureId(Measure componentMeasureId) {
        this.componentMeasureId = componentMeasureId;
    }

    public Measure getCompositeMeasureId() {
        return compositeMeasureId;
    }

    public void setCompositeMeasureId(Measure compositeMeasureId) {
        this.compositeMeasureId = compositeMeasureId;
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
        if (!(object instanceof ComponentMeasures)) {
            return false;
        }
        ComponentMeasures other = (ComponentMeasures) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.ComponentMeasures[ id=" + id + " ]";
    }
    
}
