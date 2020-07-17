library SepsisAntibioticsOrdered version '2.1.000'

using QDM version '5.5'

include MATGlobalCommonFunctions version '4.1.000' called Global

context Patient

define "SDE Ethnicity":
  ["Patient Characteristic Ethnicity": "Ethnicity"]

define "Denominator":
  "Initial Population"

/*
define "Test 1":
  ["Encounter, Performed": "Inpatient"] Encounter
    where Encounter.relevantPeriod during "Measurement Period"


define "Test 2":
  ["Encounter, Performed": "Inpatient"] Encounter
    with ["Laboratory Test, Performed": "Streptococcus Test"] LabTest
      such that LabTest.resultDateTime during Encounter.relevantPeriod



define "Test 3":
  ["Encounter, Performed": "Inpatient"] Encounter
    without ["Laboratory Test, Performed": "Streptococcus Test"] LabTest
      such that LabTest.resultDateTime during Encounter.relevantPeriod
 */

define "Test 4":
  ["Encounter, Performed": "Inpatient"] Encounter
    return { relevantPeriod: Encounter.relevantPeriod }