library SepsisLactateClearanceRate version '1.0.001'

using FHIR version '4.0.1'

include MATGlobalCommonFunctions_FHIR version '4.1.000' called Global

codesystem "LOINC": 'urn:oid:2.16.840.1.113883.6.1'
codesystem "CPT": 'urn:oid:2.16.840.1.113883.6.12'
codesystem "DischargeDisposition": 'urn:oid:2.16.840.1.113883.12.112'
codesystem "SNOMEDCT": 'urn:oid:2.16.840.1.113883.6.96'

valueset "Acute Care or Inpatient Facility": 'urn:oid:2.16.840.1.113883.17.4077.3.2056'
valueset "Acute Hypotension": 'urn:oid:2.16.840.1.113883.17.4077.3.2011'
valueset "Cardiac Arrest": 'urn:oid:2.16.840.1.113883.17.4077.3.2032'
valueset "Comfort Measures": 'urn:oid:1.3.6.1.4.1.33895.1.3.0.45'
valueset "Declined Sepsis Care": 'urn:oid:2.16.840.1.113883.17.4077.3.2047'
valueset "Emergency Department Visit": 'urn:oid:2.16.840.1.113883.3.526.3.1520'
valueset "End Stage Liver Disease": 'urn:oid:2.16.840.1.113883.17.4077.3.1000'
valueset "Epinephrine": 'urn:oid:2.16.840.1.113883.17.4077.3.2018'
valueset "Ethnicity": 'urn:oid:2.16.840.1.114222.4.11.837'
valueset "Gastrointestinal Hemorrhage": 'urn:oid:2.16.840.1.113883.17.4077.3.2051'
valueset "Hemorrhagic Stroke": 'urn:oid:2.16.840.1.113883.3.117.1.7.1.212'
valueset "Hospital Admission or Observation": 'urn:oid:2.16.840.1.113883.17.4077.3.2001'
valueset "Infection": 'urn:oid:2.16.840.1.113883.17.4077.3.2054'
valueset "Ischemic Stroke": 'urn:oid:2.16.840.1.113883.3.117.1.7.1.247'
valueset "Lactate Clearance Rate": 'urn:oid:2.16.840.1.113883.17.4077.3.2031'
valueset "Left Before Treatment Completion": 'urn:oid:2.16.840.1.113883.17.4077.3.2049'
valueset "Myocardial Infarction": 'urn:oid:2.16.840.1.113883.3.526.3.403'
valueset "ONC Administrative Sex": 'urn:oid:2.16.840.1.113762.1.4.1'
valueset "Payer": 'urn:oid:2.16.840.1.114222.4.11.3591'
valueset "Race": 'urn:oid:2.16.840.1.114222.4.11.836'
valueset "Seizure": 'urn:oid:2.16.840.1.113883.17.4077.3.2015'
valueset "Sepsis": 'urn:oid:2.16.840.1.113883.17.4077.3.2033'
valueset "Septic Shock": 'urn:oid:2.16.840.1.113883.17.4077.3.2046'
valueset "Serum Lactate": 'urn:oid:2.16.840.1.113883.17.4077.3.2019'
valueset "Status Epilepticus": 'urn:oid:2.16.840.1.113883.17.4077.3.2016'
valueset "Trauma": 'urn:oid:2.16.840.1.113883.3.464.1003.113.12.1036'

code "Birth date": '21112-8' from "LOINC" display 'Birth date'
code "Critical care, evaluation and management of the critically ill or critically injured patient; first 30-74 minutes": '99291' from "CPT" display 'Critical care, evaluation and management of the critically ill or critically injured patient; first 30-74 minutes'
code "Expired (i.e. dead)": '20' from "DischargeDisposition" display 'Expired (i.e. dead)'
code "Patient deceased during stay (discharge status = dead) (finding)": '371828006' from "SNOMEDCT" display 'Patient deceased during stay (discharge status = dead) (finding)'

parameter "Measurement Period" Interval<DateTime>

context Patient

define "Emergency Department Visit During Measurement Period With Order for Admission":
  ( ["Encounter": "Emergency Department Visit"]
    union ["Encounter": "Critical care, evaluation and management of the critically ill or critically injured patient; first 30-74 minutes"] ) EDVisit
    with ["ServiceRequest": "Hospital Admission or Observation"] OrderForAdmission
      such that EDVisit.period during "Measurement Period"
        and OrderForAdmission.authoredOn during EDVisit.period

define "Emergency Department Visit for Septic Shock":
  "Emergency Department Visit with Septic Shock Diagnosis"
    union "Emergency Department Visit with Acute Hypotension and Sepsis or Infection Diagnoses"

define "Emergency Department Visit for Septic Shock with Other Secondary Diagnosis":
  "Emergency Department Visit for Septic Shock" SepticShockEDVisit
    where exists ( SepticShockEDVisit.diagnoses SepticShockEDVisitDiagnosis
        where ( SepticShockEDVisitDiagnosis.code in "Cardiac Arrest"
            or SepticShockEDVisitDiagnosis.code in "Seizure"
            or SepticShockEDVisitDiagnosis.code in "Gastrointestinal Hemorrhage"
            or SepticShockEDVisitDiagnosis.code in "Hemorrhagic Stroke"
            or SepticShockEDVisitDiagnosis.code in "Ischemic Stroke"
            or SepticShockEDVisitDiagnosis.code in "Myocardial Infarction"
            or SepticShockEDVisitDiagnosis.code in "Trauma"
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

define "Emergency Department Visit with Septic Shock Diagnosis":
  "Emergency Department Visit During Measurement Period With Order for Admission" EDEncounter
    where exists ( EDEncounter.diagnoses EDEncounterDiagnosis
        where EDEncounterDiagnosis.code in "Septic Shock"
    )

define "Patient Currently Taking Epinephrine":
  "Emergency Department Visit for Septic Shock" SepticShockEDVisit
    with ["MedicationStatement": "Epinephrine"] ActiveEpinephrine
      such that SepticShockEDVisit.period during ActiveEpinephrine.effective

define "Patient Declined Sepsis Care":
  "Emergency Department Visit for Septic Shock" SepticShockEDVisit
    with ["Observation": "Declined Sepsis Care"] DeclinedSepsisCare
      such that DeclinedSepsisCare.Unknown during SepticShockEDVisit.period

define "Patient Has End Stage Liver Disease or Status Epilepticus":
  "Emergency Department Visit for Septic Shock" SepticShockEDVisit
    with ( ["Condition": "End Stage Liver Disease"]
      union ["Condition": "Status Epilepticus"] ) ExistingCondition
      such that ExistingCondition.onset overlaps before SepticShockEDVisit.period

define "Patient Left Before Treatment Completion":
  "Emergency Department Visit for Septic Shock" SepticShockEDVisit
    where SepticShockEDVisit.dischargeDisposition in "Left Before Treatment Completion"

define "Patient Receiving Comfort Measures":
  ( "Emergency Department Visit for Septic Shock" SepticShockEDVisit
      with ["ServiceRequest": "Comfort Measures"] OrderForComfortMeasures
        such that OrderForComfortMeasures.authoredOn during SepticShockEDVisit.period
  )
    union ( "Emergency Department Visit for Septic Shock" SepticShockEDVisit
        with ["Procedure": "Comfort Measures"] ReceivingComfortMeasures
          such that ReceivingComfortMeasures.dateTime overlaps after SepticShockEDVisit.period
    )

define "Patient Transferred from Acute Care or Inpatient Facility":
  "Emergency Department Visit for Septic Shock" SepticShockEDVisit
    where SepticShockEDVisit.hospitalization.admitSource in "Acute Care or Inpatient Facility"

define "SDE Ethnicity":
  ["Patient Characteristic Ethnicity": "Ethnicity"]

define "SDE Payer":
  ["Patient Characteristic Payer": "Payer"]

define "SDE Race":
  ["Patient Characteristic Race": "Race"]

define "SDE Sex":
  ["Patient Characteristic Sex": "ONC Administrative Sex"]

define "Patient Died During Emergency Department Visit for Septic Shock":
  "Emergency Department Visit for Septic Shock" SepticShockEDVisit
    where ( SepticShockEDVisit.hospitalization.dischargeDisposition ~ "Patient deceased during stay (discharge status = dead) (finding)"
        or SepticShockEDVisit.hospitalization.dischargeDisposition ~ "Expired (i.e. dead)"
    )

define "Initial Population":
  "Emergency Department Visit for Septic Shock" SepticShockEDVisit
    with ["Patient Characteristic Birthdate": "Birth date"] BirthDate
      such that Global."CalendarAgeInYearsAt" ( BirthDate.birthDatetime, start of SepticShockEDVisit.period ) >= 18

define "Numerator":
  "Emergency Department Visit for Septic Shock" SepticShockEDVisit
    with ["Observation": "Lactate Clearance Rate"] LactateClearanceRate
      such that LactateClearanceRate.effective starts 1 day or less after day of start of SepticShockEDVisit.period
        and LactateClearanceRate.component.value >= 10 '%'

define "Denominator Exclusions":
  "Patient Transferred from Acute Care or Inpatient Facility"
    union "Patient Left Before Treatment Completion"
    union "Patient Died During Emergency Department Visit for Septic Shock"
    union "Patient Receiving Comfort Measures"
    union "Patient Declined Sepsis Care"
    union "Emergency Department Visit for Septic Shock with Other Secondary Diagnosis"
    union "Patient Has End Stage Liver Disease or Status Epilepticus"
    union "Patient Currently Taking Epinephrine"

define "Patient Had Follow Up Serum Lactate Measurement After Elevated Serum Lactate Level During Emergency Department Visit for Septic Shock":
  from
    "Emergency Department Visit for Septic Shock" SepticShockEDVisit,
    ( First(["Observation": "Serum Lactate"] SerumLactateMeasurement
        where SerumLactateMeasurement.component.value > 2 'mmol/L'
        sort by issued
    )) FirstElevatedLactate,
    ["Observation": "Serum Lactate"] SubsequentSerumLactate
    where FirstElevatedLactate.issued during SepticShockEDVisit.period
      and SubsequentSerumLactate.component.value is not null
      and SubsequentSerumLactate.effective starts 6 hours or less after FirstElevatedLactate.issued
      and SubsequentSerumLactate.effective starts 1 hours or more after FirstElevatedLactate.issued
    return SepticShockEDVisit

define "Denominator":
  "Initial Population"
    intersect "Patient Had Follow Up Serum Lactate Measurement After Elevated Serum Lactate Level During Emergency Department Visit for Septic Shock"