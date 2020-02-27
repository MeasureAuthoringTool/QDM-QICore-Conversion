package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Collection;


@Entity
@Table(name = "MEASURE_DETAILS")
@XmlRootElement
public class MeasureDetails implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Lob
    @Column(name = "DESCRIPTION")
    private String description;
    @Lob
    @Column(name = "COPYRIGHT")
    private String copyright;
    @Lob
    @Column(name = "DISCLAIMER")
    private String disclaimer;
    @Lob
    @Column(name = "STRATIFICATION")
    private String stratification;
    @Lob
    @Column(name = "RISK_ADJUSTMENT")
    private String riskAdjustment;
    @Lob
    @Column(name = "RATE_AGGREGATION")
    private String rateAggregation;
    @Lob
    @Column(name = "RATIONALE")
    private String rationale;
    @Lob
    @Column(name = "CLINICAL_RECOMMENDATION")
    private String clinicalRecommendation;
    @Lob
    @Column(name = "IMPROVEMENT_NOTATION")
    private String improvementNotation;
    @Lob
    @Column(name = "DEFINITION")
    private String definition;
    @Lob
    @Column(name = "GUIDANCE")
    private String guidance;
    @Lob
    @Column(name = "TRANSMISSION_FORMAT")
    private String transmissionFormat;
    @Lob
    @Column(name = "INITIAL_POPULATION")
    private String initialPopulation;
    @Lob
    @Column(name = "DENOMINATOR")
    private String denominator;
    @Lob
    @Column(name = "DENOMINATOR_EXCLUSIONS")
    private String denominatorExclusions;
    @Lob
    @Column(name = "NUMERATOR")
    private String numerator;
    @Lob
    @Column(name = "NUMERATOR_EXCLUSIONS")
    private String numeratorExclusions;
    @Lob
    @Column(name = "MEASURE_OBSERVATIONS")
    private String measureObservations;
    @Lob
    @Column(name = "MEASURE_POPULATION")
    private String measurePopulation;
    @Lob
    @Column(name = "MEASURE_POPULATION_EXCLUSIONS")
    private String measurePopulationExclusions;
    @Lob
    @Column(name = "DENOMINATOR_EXCEPTIONS")
    private String denominatorExceptions;
    @Lob
    @Column(name = "SUPPLEMENTAL_DATA_ELEMENTS")
    private String supplementalDataElements;
    @Lob
    @Column(name = "MEASURE_SET")
    private String measureSet;

    @Column(name = "MEASURE_ID")
    private String measureId;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "measureDetailsId", fetch = FetchType.EAGER)
    @OrderBy("referenceNumber")
    private Collection<MeasureDetailsReference> measureDetailsReferenceCollection;

    public MeasureDetails() {
    }

    public MeasureDetails(Integer id) {
        this.id = id;
    }

    @XmlElement
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @XmlElement
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @XmlElement
    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    @XmlElement
    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }

    @XmlElement
    public String getStratification() {
        return stratification;
    }

    public void setStratification(String stratification) {
        this.stratification = stratification;
    }

    @XmlElement
    public String getRiskAdjustment() {
        return riskAdjustment;
    }

    public void setRiskAdjustment(String riskAdjustment) {
        this.riskAdjustment = riskAdjustment;
    }

    @XmlElement
    public String getRateAggregation() {
        return rateAggregation;
    }

    public void setRateAggregation(String rateAggregation) {
        this.rateAggregation = rateAggregation;
    }

    @XmlElement
    public String getRationale() {
        return rationale;
    }

    public void setRationale(String rationale) {
        this.rationale = rationale;
    }

    @XmlElement
    public String getClinicalRecommendation() {
        return clinicalRecommendation;
    }

    public void setClinicalRecommendation(String clinicalRecommendation) {
        this.clinicalRecommendation = clinicalRecommendation;
    }

    @XmlElement
    public String getImprovementNotation() {
        return improvementNotation;
    }

    public void setImprovementNotation(String improvementNotation) {
        this.improvementNotation = improvementNotation;
    }

    @XmlElement
    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    @XmlElement
    public String getGuidance() {
        return guidance;
    }

    public void setGuidance(String guidance) {
        this.guidance = guidance;
    }

    @XmlElement
    public String getTransmissionFormat() {
        return transmissionFormat;
    }

    public void setTransmissionFormat(String transmissionFormat) {
        this.transmissionFormat = transmissionFormat;
    }

    @XmlElement
    public String getInitialPopulation() {
        return initialPopulation;
    }

    public void setInitialPopulation(String initialPopulation) {
        this.initialPopulation = initialPopulation;
    }

    @XmlElement
    public String getDenominator() {
        return denominator;
    }

    public void setDenominator(String denominator) {
        this.denominator = denominator;
    }

    @XmlElement
    public String getDenominatorExclusions() {
        return denominatorExclusions;
    }

    public void setDenominatorExclusions(String denominatorExclusions) {
        this.denominatorExclusions = denominatorExclusions;
    }

    @XmlElement
    public String getNumerator() {
        return numerator;
    }

    public void setNumerator(String numerator) {
        this.numerator = numerator;
    }

    @XmlElement
    public String getNumeratorExclusions() {
        return numeratorExclusions;
    }

    public void setNumeratorExclusions(String numeratorExclusions) {
        this.numeratorExclusions = numeratorExclusions;
    }

    @XmlElement
    public String getMeasureObservations() {
        return measureObservations;
    }

    public void setMeasureObservations(String measureObservations) {
        this.measureObservations = measureObservations;
    }

    @XmlElement
    public String getMeasurePopulation() {
        return measurePopulation;
    }

    public void setMeasurePopulation(String measurePopulation) {
        this.measurePopulation = measurePopulation;
    }

    @XmlElement
    public String getMeasurePopulationExclusions() {
        return measurePopulationExclusions;
    }

    public void setMeasurePopulationExclusions(String measurePopulationExclusions) {
        this.measurePopulationExclusions = measurePopulationExclusions;
    }

    @XmlElement
    public String getDenominatorExceptions() {
        return denominatorExceptions;
    }

    public void setDenominatorExceptions(String denominatorExceptions) {
        this.denominatorExceptions = denominatorExceptions;
    }

    @XmlElement
    public String getSupplementalDataElements() {
        return supplementalDataElements;
    }

    public void setSupplementalDataElements(String supplementalDataElements) {
        this.supplementalDataElements = supplementalDataElements;
    }

    @XmlElement
    public String getMeasureSet() {
        return measureSet;
    }

    public void setMeasureSet(String measureSet) {
        this.measureSet = measureSet;
    }

    @XmlElement
    public String getMeasureId() {
        return measureId;
    }

    public void setMeasureId(String measureId) {
        this.measureId = measureId;
    }

    @XmlTransient
    public Collection<MeasureDetailsReference> getMeasureDetailsReferenceCollection() {
        return measureDetailsReferenceCollection;
    }

    public void setMeasureDetailsReferenceCollection(Collection<MeasureDetailsReference> measureDetailsReferenceCollection) {
        this.measureDetailsReferenceCollection = measureDetailsReferenceCollection;
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
        if (!(object instanceof MeasureDetails)) {
            return false;
        }
        MeasureDetails other = (MeasureDetails) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.MeasureDetails[ id=" + id + " ]";
    }
    
}
