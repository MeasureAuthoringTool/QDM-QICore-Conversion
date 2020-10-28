library SepsisAntibioticsOrdered version '2.1.000'

using QDM version '5.5'

include MATGlobalCommonFunctions version '4.1.000' called Global

context Patient

define "SDE Ethnicity":
  ["Patient Characteristic Ethnicity": "Ethnicity"]

define "Denominator":
  "Initial Population"


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

define "Test 4":
  ["Encounter, Performed": "Inpatient"] Encounter
    return { relevantPeriod: Encounter.relevantPeriod }

define "Test 5":
  ["Encounter, Performed": "Inpatient"] Encounter
    sort by start of relevantPeriod


define "Test 6":
  "A define that does a retrieve" Encounter
     where Encounter.relevantPeriod during "Measurement Period"

define "Test 7":
   ["Encounter, Performed": "Inpatient"] Encounter
      with ["Care Goal": "Some Valueset"] Goal
        such that start of Goal.relevantPeriod during Encounter.relevantPeriod

define "Test 8":
   ["Encounter, Performed": "Inpatient"] Encounter
    with ["Device, Applied": "Some Valueset"] Device
      such that Device.relevantPeriod.low during Encounter.relevantPeriod


define "Test 9":
   ["Encounter, Performed": "Inpatient"] Encounter
   with ["Diagnostic Study, Performed": "Some Valueset"] Study
     such that Study.relevantPeriod.high during Encounter.relevantPeriod

define "Test 10":
 ["Encounter, Performed": "Inpatient"] Encounter
   with ["Medication, Order": "Some Valueset"] Med
     such that Med.relevantDatetime during Encounter.relevantPeriod

define "Test 11":
  ["Encounter, Performed": "Inpatient"] Encounter
    with ["Intervention, Performed": "Some Valueset"] Proc
      such that Proc.authorDatetime during Encounter.relevantPeriod

define "Test 12":
  ["Patient Characteristic Expired": "Some Valueset"] CharExp
    return { relevantPeriod: CharExp.cause }

define "Test 13":
  ["Assessment, Not Ordered": "Some Valueset"] Assess
    where Assess.authorDatetime during "Measurement Period"


define "Test 14":
  ["Assessment, Not Ordered": "Some Valueset"] Assess
      return { relevantPeriod: Assess.authorDatetime }


define "Test 15":
    ["Assessment, Not Ordered": "Some Valueset"] Assessment
      with ["Laboratory Test, Performed": "Streptococcus Test"] LabTest
        such that LabTest.resultDateTime during Assessment.relevantPeriod



define "Test 16":
  ( ["Encounter, Performed": "Tobacco User with Cessation Intervention"]
    union ["Procedure, Performed": "Tobacco User with Cessation Intervention"]
    union ["Encounter, Performed": "Tobacco Use Cessation Counseling"]
    union ["Procedure, Performed": "Tobacco Use Cessation Counseling"] ) Counseling
      with "Most Recent Encounter" ENC
        such that ( Counseling.relevantPeriod starts 731 days or less after start of ENC.relevantPeriod
            or Counseling.relevantPeriod starts before start of ENC.relevantPeriod
    )

define function "Test 17" (foo Boolean):
  ["Assessment, Performed"]

define "Test 18":
  ["Encounter, Performed"]