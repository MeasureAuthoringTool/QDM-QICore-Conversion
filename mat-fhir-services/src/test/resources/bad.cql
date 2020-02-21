library SepsisBundle version '2.0.000'

using QDM version '5.5'

include MATGlobalCommonFunctions version '4.1.000' called Global

codesystem "LOINC": 'urn:oid:2.16.840.1.113883.6.1'
codesystem "CPT": 'urn:oid:2.16.840.1.113883.6.12'
codesystem "DischargeDisposition": 'urn:oid:2.16.840.1.113883.12.112'
codesystem "SNOMEDCT": 'urn:oid:2.16.840.1.113883.6.96'

valueset "Acute Care or Inpatient Facility": 'urn:oid:2.16.840.1.113883.17.4077.3.2056'
valueset "Acute Hypotension": 'urn:oid:2.16.840.1.113883.17.4077.3.2011'
valueset "Acute Pulmonary Edema": 'urn:oid:2.16.840.1.113883.17.4077.3.2014'
valueset "Anuria": 'urn:oid:2.16.840.1.113883.17.4077.3.2034'
valueset "Cardiac Arrest": 'urn:oid:2.16.840.1.113883.17.4077.3.2032'
valueset "Comfort Measures": 'urn:oid:1.3.6.1.4.1.33895.1.3.0.45'
valueset "Crystalloids for Sepsis": 'urn:oid:2.16.840.1.113883.17.4077.3.2048'
valueset "Declined Sepsis Care": 'urn:oid:2.16.840.1.113883.17.4077.3.2047'
valueset "Ejection Fraction": 'urn:oid:2.16.840.1.113883.3.526.3.1134'
valueset "Emergency Department Visit": 'urn:oid:2.16.840.1.113883.3.526.3.1520'
valueset "End Stage Renal Disease": 'urn:oid:2.16.840.1.113883.3.526.3.353'
valueset "Ethnicity": 'urn:oid:2.16.840.1.114222.4.11.837'
valueset "Gastrointestinal Hemorrhage": 'urn:oid:2.16.840.1.113883.17.4077.3.2051'
valueset "Heart Failure": 'urn:oid:2.16.840.1.113883.3.526.3.376'
valueset "Hemorrhagic Stroke": 'urn:oid:2.16.840.1.113883.3.117.1.7.1.212'
valueset "Hospital Admission or Observation": 'urn:oid:2.16.840.1.113883.17.4077.3.2001'
valueset "Infection": 'urn:oid:2.16.840.1.113883.17.4077.3.2054'
valueset "Ischemic Stroke": 'urn:oid:2.16.840.1.113883.3.117.1.7.1.247'
valueset "IV Antibiotics for Sepsis": 'urn:oid:2.16.840.1.113883.17.4077.3.2045'
valueset "Left Before Treatment Completion": 'urn:oid:2.16.840.1.113883.17.4077.3.2049'
valueset "Left Ventricular Assist Device": 'urn:oid:2.16.840.1.113883.17.4077.3.2013'
valueset "Myocardial Infarction": 'urn:oid:2.16.840.1.113883.3.526.3.403'
valueset "ONC Administrative Sex": 'urn:oid:2.16.840.1.113762.1.4.1'
valueset "Payer": 'urn:oid:2.16.840.1.114222.4.11.3591'
valueset "Race": 'urn:oid:2.16.840.1.114222.4.11.836'
valueset "Second and Third Degree Burn": 'urn:oid:2.16.840.1.113883.17.4077.3.2053'
valueset "Seizure": 'urn:oid:2.16.840.1.113883.17.4077.3.2015'
valueset "Sepsis": 'urn:oid:2.16.840.1.113883.17.4077.3.2033'
valueset "Septic Shock": 'urn:oid:2.16.840.1.113883.17.4077.3.2046'
valueset "Serum Lactate": 'urn:oid:2.16.840.1.113883.17.4077.3.2019'
valueset "Toxicological Emergency": 'urn:oid:2.16.840.1.113883.17.4077.3.2055'
valueset "Trauma": 'urn:oid:2.16.840.1.113883.3.464.1003.113.12.1036'

code "Birth date": '21112-8' from "LOINC" display 'Birth date'
code "Critical care, evaluation and management of the critically ill or critically injured patient; first 30-74 minutes": '99291' from "CPT" display 'Critical care, evaluation and management of the critically ill or critically injured patient; first 30-74 minutes'
code "Expired (i.e. dead)": '20' from "DischargeDisposition" display 'Expired (i.e. dead)'
code "Patient deceased during stay (discharge status = dead) (finding)": '371828006' from "SNOMEDCT" display 'Patient deceased during stay (discharge status = dead) (finding)'
code "Severe (severity modifier) (qualifier value)": '24484000' from "SNOMEDCT" display 'Severe (severity modifier) (qualifier value)'

parameter "Measurement Period" Interval<DateTime>

context Patient

define "SDE Ethnicity":
  ["Patient Characteristic Ethnicity": "Ethnicity"]

define "SDE Payer":
  ["Patient Characteristic Payer": "Payer"]

define "SDE Race":
  ["Patient Characteristic Race": "Race"]

define "SDE Sex":
  ["Patient Characteristic Sex": "ONC Administrative Sex"]

define "Emergency Department Visit During Measurement Period With Order for Admission":
  ( ["Encounter, Performed": "Emergency Department Visit"]
    union ["Encounter, Performed": "Critical care, evaluation and management of the critically ill or critically injured patient; first 30-74 minutes"] ) EDVisit
    with ["Encounter, Order": "Hospital Admission or Observation"] OrderForAdmission
      such that EDVisit.relevantPeriod during "Measurement Period"
        and OrderForAdmission.authorDatetime during EDVisit.relevantPeriod

define "Patient Declined Sepsis Care":
  "Emergency Department Visit for Septic Shock" SepticShockEDVisit
    with ["Assessment, Performed": "Declined Sepsis Care"] DeclinedSepsisCare
      such that DeclinedSepsisCare.authorDatetime during SepticShockEDVisit.relevantPeriod

define "Patient Left Before Treatment Completion":
  "Emergency Department Visit for Septic Shock" SepticShockEDVisit
    where SepticShockEDVisit.dischargeDisposition in "Left Before Treatment Completion"

define "Patient Receiving Comfort Measures":
  ( "Emergency Department Visit for Septic Shock" SepticShockEDVisit
      with ["Intervention, Order": "Comfort Measures"] OrderForComfortMeasures
        such that OrderForComfortMeasures.authorDatetime during SepticShockEDVisit.relevantPeriod
  )
    union ( "Emergency Department Visit for Septic Shock" SepticShockEDVisit
        with ["Intervention, Performed": "Comfort Measures"] ReceivingComfortMeasures
          such that ReceivingComfortMeasures.relevantPeriod overlaps after SepticShockEDVisit.relevantPeriod
    )

define "Patient Transferred from Acute Care or Inpatient Facility":
  "Emergency Department Visit for Septic Shock" SepticShockEDVisit
    where SepticShockEDVisit.admissionSource in "Acute Care or Inpatient Facility"

define "1 L or More of Crystalloids Ordered During Emergency Department Visit for Septic Shock":
  "Emergency Department Visit for Septic Shock" SepticShockEDVisit
    with ["Medication, Order": "Crystalloids for Sepsis"] CrystalloidsOrder
      such that CrystalloidsOrder.authorDatetime during SepticShockEDVisit.relevantPeriod
        and CrystalloidsOrder.dosage >= 1 'L'

define "Antibiotics Ordered During Emergency Department Visit for Septic Shock":
  "Emergency Department Visit for Septic Shock" SepticShockEDVisit
    with ["Medication, Order": "IV Antibiotics for Sepsis"] SepsisAntibioticsOrder
      such that SepsisAntibioticsOrder.authorDatetime during SepticShockEDVisit.relevantPeriod

define "Denominator":
  "Initial Population"

define "Initial Population":
  ( "Emergency Department Visit for Septic Shock" SepticShockEDVisit
      with ["Patient Characteristic Birthdate": "Birth date"] BirthDate
        such that Global."CalendarAgeInYearsAt" ( BirthDate.birthDatetime, start of SepticShockEDVisit.relevantPeriod ) >= 18
  )

define "Emergency Department Visit with Septic Shock Diagnosis":
  "Emergency Department Visit During Measurement Period With Order for Admission" EDEncounter
    where exists ( EDEncounter.diagnoses EDEncounterDiagnosis
        where EDEncounterDiagnosis.code in "Septic Shock"
    )

define "Emergency Department Visit for Septic Shock":
  "Emergency Department Visit with Septic Shock Diagnosis"
    union "Emergency Department Visit with Acute Hypotension and Sepsis or Infection Diagnoses"

define "Denominator Exclusions":
  "Patient Transferred from Acute Care or Inpatient Facility"
    union "Patient Left Before Treatment Completion"
    union "Patient Died During Emergency Department Visit for Septic Shock"
    union "Patient Receiving Comfort Measures"
    union "Patient Declined Sepsis Care"
    union "Emergency Department Visit for Septic Shock with Other Secondary Diagnosis"
    union "Emergency Department Visit for Septic Shock and Patient Has Existing Condition"

define "Emergency Department Visit for Septic Shock and Patient Has Existing Condition":
  "Emergency Department Visit for Septic Shock" SepticShockEDVisit
    with ( ["Diagnosis": "Anuria"]
      union ["Diagnosis": "End Stage Renal Disease"]
      union ( ["Diagnosis": "Heart Failure"] HeartFailure
          where HeartFailure.severity ~ "Severe (severity modifier) (qualifier value)"
      )
      union ["Device, Applied": "Left Ventricular Assist Device"]
      union ( ["Diagnostic Study, Performed": "Ejection Fraction"] LVEF
          where LVEF.result < 20 '%'
      ) ) ExistingCondition
      such that ExistingCondition.prevalencePeriod overlaps before SepticShockEDVisit.relevantPeriod
        or ExistingCondition.relevantPeriod overlaps before SepticShockEDVisit.relevantPeriod

define "Emergency Department Visit for Septic Shock with Other Secondary Diagnosis":
  "Emergency Department Visit for Septic Shock" SepticShockEDVisit
    where exists ( SepticShockEDVisit.diagnoses SepticShockEDVisitDiagnosis
        where ( SepticShockEDVisitDiagnosis.code in "Cardiac Arrest"
            or SepticShockEDVisitDiagnosis.code in "Second and Third Degree Burn"
            or SepticShockEDVisitDiagnosis.code in "Seizure"
            or SepticShockEDVisitDiagnosis.code in "Gastrointestinal Hemorrhage"
            or SepticShockEDVisitDiagnosis.code in "Hemorrhagic Stroke"
            or SepticShockEDVisitDiagnosis.code in "Ischemic Stroke"
            or SepticShockEDVisitDiagnosis.code in "Myocardial Infarction"
            or SepticShockEDVisitDiagnosis.code in "Toxicological Emergency"
            or SepticShockEDVisitDiagnosis.code in "Trauma"
            or SepticShockEDVisitDiagnosis.code in "Acute Pulmonary Edema"
        )
    )

define "Emergency Department Visit with Acute Hypotension and Sepsis or Infection Diagnoses":
  "Emergency Department Visit During Measurement Period With Order for Admission" EDEncounter
    where exists ( EDEncounter.diagnoses EDEncounterDiagnosis
        where EDEncounterDiagnosis.code in "Acute Hypotension"
          and ( EDEncounterDiagnosis.code in "Infection"
              or EDEncounterDiagnosis.code in "Sepsis"
          )
    )

define "Patient Died During Emergency Department Visit for Septic Shock":
  "Emergency Department Visit for Septic Shock" SepticShockEDVisit
    where ( SepticShockEDVisit.dischargeDisposition ~ "Patient deceased during stay (discharge status = dead) (finding)"
        or SepticShockEDVisit.dischargeDisposition ~ "Expired (i.e. dead)"
    )

define "Numerator":
  "Serum Lactate Measurement Ordered During Emergency Department Visit for Septic Shock"
    intersect "Antibiotics Ordered During Emergency Department Visit for Septic Shock"
    intersect "1 L or More of Crystalloids Ordered During Emergency Department Visit for Septic Shock"

define "Serum Lactate Measurement Ordered During Emergency Department Visit for Septic Shock":
  "Emergency Department Visit for Septic Shock" SepticShockEDVisit
    with ["Laboratory Test, Order": "Serum Lactate"] SerumLactateOrder
      such that SerumLactateOrder.authorDatetime during SepticShockEDVisit.relevantPeriod

define "Sepsis Bundle Numerator":
  from
    "Emergency Department Visit for Septic Shock" SepticShockEDVisit,
    ["Laboratory Test, Order": "Serum Lactate"] SerumLactateOrder,
    ["Medication, Order": "IV Antibiotics for Sepsis"] SepsisAntibioticsOrder,
    ["Medication, Order": "Crystalloids for Sepsis"] CrystalloidsOrder
    where SerumLactateOrder.authorDatetime during SepticShockEDVisit.relevantPeriod
      and SepsisAntibioticsOrder.authorDatetime during SepticShockEDVisit.relevantPeriod
      and CrystalloidsOrder.authorDatetime during SepticShockEDVisit.relevantPeriod
      and CrystalloidsOrder.dosage >= 1 'L'

