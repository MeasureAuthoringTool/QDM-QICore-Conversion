library AppropriateEDtUtilizationofCTforPE version '2.1.000'

using QDM version '5.5'

include MATGlobalCommonFunctions version '4.1.000' called Global

codesystem "LOINC": 'urn:oid:2.16.840.1.113883.6.1'
codesystem "CPT:2018": 'urn:oid:2.16.840.1.113883.6.12'
codesystem "SNOMEDCT:2017-09": 'urn:oid:2.16.840.1.113883.6.96'
codesystem "SOP": 'urn:oid:2.16.840.1.113883.3.221.5'

valueset "Aortic Dissection": 'urn:oid:2.16.840.1.113883.17.4077.3.1026'
valueset "Chest CT with Contrast": 'urn:oid:2.16.840.1.113883.17.4077.3.2064'
valueset "CT Pulmonary Angiogram CTPA": 'urn:oid:2.16.840.1.113883.17.4077.3.1020'
valueset "D dimer": 'urn:oid:2.16.840.1.113883.17.4077.3.1024'
valueset "Dangerous Mechanism of Injury": 'urn:oid:2.16.840.1.113883.17.4077.3.1018'
valueset "Emergency Department Visit": 'urn:oid:2.16.840.1.113883.3.526.3.1520'
valueset "Ethnicity": 'urn:oid:2.16.840.1.114222.4.11.837'
valueset "ONC Administrative Sex": 'urn:oid:2.16.840.1.113762.1.4.1'
valueset "Payer": 'urn:oid:2.16.840.1.114222.4.11.3591'
valueset "Pregnancy": 'urn:oid:2.16.840.1.113883.3.526.3.378'
valueset "Race": 'urn:oid:2.16.840.1.114222.4.11.836'
valueset "Trauma": 'urn:oid:2.16.840.1.113883.3.464.1003.113.12.1036'

code "Birth date": '21112-8' from "LOINC" display 'Birth date'
code "Critical care, evaluation and management of the critically ill or critically injured patient; first 30-74 minutes": '99291' from "CPT" display 'Critical care, evaluation and management of the critically ill or critically injured patient; first 30-74 minutes'
code "D-dimer above reference range (finding)": '449830004' from "SNOMEDCT" display 'D-dimer above reference range (finding)'
code "High (qualifier value)": '75540009' from "SNOMEDCT" display 'High (qualifier value)'
code "Moderate (severity modifier) (qualifier value)": '6736007' from "SNOMEDCT" display 'Moderate (severity modifier) (qualifier value)'
code "Positive (qualifier value)": '10828004' from "SNOMEDCT" display 'Positive (qualifier value)'
code "Probability of suspected pulmonary embolism prior to imaging": '77351-5' from "LOINC" display 'Probability of suspected pulmonary embolism prior to imaging'

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

define "CT of Chest Ordered During Emergency Department Visit":
  "Emergency Department Visit During Measurement Period" EDVisitMP
    with ["Diagnostic Study, Order": "Chest CT with Contrast"] ChestCTOrdered
      such that ChestCTOrdered.authorDatetime during EDVisitMP.relevantPeriod

define "CT of Chest and CT Pulmonary Angiogram Ordered During Same Emergency Department Visit":
  "CT Pulmonary Angiogram Ordered During Emergency Department Visit"
    intersect "CT of Chest Ordered During Emergency Department Visit"

define "CT Pulmonary Angiogram Ordered During Emergency Department Visit":
  "Emergency Department Visit During Measurement Period" EDVisitMP
    with ["Diagnostic Study, Order": "CT Pulmonary Angiogram CTPA"] CTPAOrder
      such that CTPAOrder.authorDatetime during EDVisitMP.relevantPeriod

define "CT Pulmonary Angiogram Ordered for Aortic Dissection During Emergency Department Visit":
  "CT Pulmonary Angiogram Ordered During Emergency Department Visit"
    intersect ( "Emergency Department Visit During Measurement Period" EDVisitMP
        with ["Diagnostic Study, Order": "CT Pulmonary Angiogram CTPA"] CTPAOrder
          such that CTPAOrder.authorDatetime during EDVisitMP.relevantPeriod
            and CTPAOrder.reason in "Aortic Dissection"
    )

define "CT Pulmonary Angiogram Ordered for Trauma or Dangerous Mechanism of Injury During Emergency Department Visit":
  "CT Pulmonary Angiogram Ordered During Emergency Department Visit"
    intersect ( "Emergency Department Visit During Measurement Period" EDVisitMP
        with ["Diagnostic Study, Order": "CT Pulmonary Angiogram CTPA"] CTPAOrder
          such that CTPAOrder.authorDatetime during EDVisitMP.relevantPeriod
            and ( CTPAOrder.reason in "Trauma"
                or CTPAOrder.reason in "Dangerous Mechanism of Injury"
            )
    )

define "Denominator":
  "Initial Population"

define "Denominator Exception":
  "CT Pulmonary Angiogram Ordered for Aortic Dissection During Emergency Department Visit"

define "Denominator Exclusions":
  "Patient is Pregnant During Emergency Department Visit"
    union "CT of Chest and CT Pulmonary Angiogram Ordered During Same Emergency Department Visit"
    union "CT Pulmonary Angiogram Ordered for Trauma or Dangerous Mechanism of Injury During Emergency Department Visit"

define "Emergency Department Visit During Measurement Period":
  ( ["Encounter, Performed": "Emergency Department Visit"]
    union ["Encounter, Performed": "Critical care, evaluation and management of the critically ill or critically injured patient; first 30-74 minutes"] ) EDvisit
    where EDvisit.relevantPeriod starts during "Measurement Period"

define "Numerator":
  "CT Pulmonary Angiogram Ordered During Emergency Department Visit"
    intersect ( "Elevated or Positive D Dimer Result Within 24 Hours Prior to Order for CTPA"
        union "Moderate or High PreTest Clinical Probability for Pulmonary Embolism Within 24 Hours Prior to Order for CTPA"
    )

define "Patient is Pregnant During Emergency Department Visit":
  "CT Pulmonary Angiogram Ordered During Emergency Department Visit" EDVisitCTPA
    with ["Diagnosis": "Pregnancy"] Pregnancy
      such that Pregnancy.prevalencePeriod overlaps EDVisitCTPA.relevantPeriod

define "Initial Population":
  "CT Pulmonary Angiogram Ordered During Emergency Department Visit" EDVisitCTPA
    with ["Patient Characteristic Birthdate": "Birth date"] BirthDate
      such that Global."CalendarAgeInYearsAt" ( BirthDate.birthDatetime, start of EDVisitCTPA.relevantPeriod ) >= 18

define "Elevated or Positive D Dimer Result Within 24 Hours Prior to Order for CTPA":
  from
    "Emergency Department Visit During Measurement Period" EDVisitMP,
    ["Diagnostic Study, Order": "CT Pulmonary Angiogram CTPA"] CTPAOrder,
    ["Laboratory Test, Performed": "D dimer"] DDimerPerformed
    where CTPAOrder.authorDatetime during EDVisitMP.relevantPeriod
      and DDimerPerformed.resultDatetime 24 hours or less before CTPAOrder.authorDatetime
      and ( DDimerPerformed.result ~ "D-dimer above reference range (finding)"
          or DDimerPerformed.result ~ "Positive (qualifier value)"
      )
    return EDVisitMP

define "Moderate or High PreTest Clinical Probability for Pulmonary Embolism Within 24 Hours Prior to Order for CTPA":
  from
    "Emergency Department Visit During Measurement Period" EDVisitMP,
    ["Diagnostic Study, Order": "CT Pulmonary Angiogram CTPA"] CTPAOrder,
    ["Assessment, Performed": "Probability of suspected pulmonary embolism prior to imaging"] PreTestPE
    where CTPAOrder.authorDatetime during EDVisitMP.relevantPeriod
      and PreTestPE.relevantDatetime 24 hours or less before CTPAOrder.authorDatetime
      and ( PreTestPE.result ~ "Moderate (severity modifier) (qualifier value)"
          or PreTestPE.result ~ "High (qualifier value)"
      )
    return EDVisitMP

