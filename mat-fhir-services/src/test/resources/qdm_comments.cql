// library MATGlobalCommonFunctions version '0.0.999'
library MATGlobalCommonFunctions version '1.0.000' // this best lib ever

/*
using QDM version '5.2'
*/

// using QDM version '5.2'

using QDM version '5.4' // I like five four

include MATGlobalCommonFunctions version '4.0.000' called Global
include SupplementalDataElements version '1.0.0' called SDE
include AdultOutpatientEncounters version '1.2.345' called ADP
include AdvancedIllnessandFrailtyExclusion version '5.6.789' called AVI
include Hospice version '0.1.234' called HSP
include TJCOverall version '5.6.789' called TJC
include VTEICU version '5.6.789' called VTE   // I am a comment

/*
include BAD_GUY version '5.6.999' called AWFUL
*/

// include UNDER version '5.6.789' called UNDER

/*
codesystem "LOINC": 'urn:oid:2.16.840.1.113883.6.22'
*/

codesystem "LOINC": 'urn:oid:2.16.840.1.113883.6.1'
codesystem "CPT": 'urn:oid:2.16.840.1.113883.6.12'
codesystem "DischargeDisposition": 'urn:oid:2.16.840.1.113883.12.112' // I am a codesystem
codesystem "SNOMEDCT": 'urn:oid:2.16.840.1.113883.6.96'

/*
valueset "Status Epilepticus": 'urn:oid:2.16.840.1.113883.17.4077.3.2016'
*/

/* valueset "Payer": 'urn:oid:2.16.840.1.114222.4.11.3591' */


valueset "Acute Care or Inpatient Facility": 'urn:oid:2.16.840.1.113883.17.4077.3.2056' // I am a valuesystem
valueset "Acute Hypotension": 'urn:oid:2.16.840.1.113883.17.4077.3.2011'
valueset "Cardiac Arrest": 'urn:oid:2.16.840.1.113883.17.4077.3.2032'
valueset "Comfort Measures": 'urn:oid:1.3.6.1.4.1.33895.1.3.0.45'
valueset "Declined Sepsis Care": 'urn:oid:2.16.840.1.113883.17.4077.3.2047'
valueset "Emergency Department Visit": 'urn:oid:2.16.840.1.113883.3.526.3.1520'
valueset "End Stage Liver Disease": 'urn:oid:2.16.840.1.113883.17.4077.3.1000'

//valueset "Trauma": 'urn:oid:2.16.840.1.113883.3.464.1003.113.12.1036'

parameter "Measurement Period" Interval<DateTime>

context Patient

define function "ToDate"(Value DateTime ):
  DateTime(year from Value, month from Value, day from Value, 0, 0, 0, 0, timezone from Value)

//define function "ToDate"(Value DateTime ):
//  DateTime(year from Value, month from Value, day from Value, 0, 0, 0, 0, timezone from Value)


define function "CalendarAgeInDaysAt"(BirthDateTime DateTime, AsOf DateTime ):
  days between ToDate(BirthDateTime)and ToDate(AsOf)

define function "CalendarAgeInDays"(BirthDateTime DateTime ):
  CalendarAgeInDaysAt(BirthDateTime, Today())

define function "CalendarAgeInMonthsAt"(BirthDateTime DateTime, AsOf DateTime ):
  months between ToDate(BirthDateTime)and ToDate(AsOf)

define function "CalendarAgeInMonths"(BirthDateTime DateTime ):
  CalendarAgeInMonthsAt(BirthDateTime, Today())

define function "CalendarAgeInYearsAt"(BirthDateTime DateTime, AsOf DateTime ):
  years between ToDate(BirthDateTime)and ToDate(AsOf)

define function "CalendarAgeInYears"(BirthDateTime DateTime ):
  CalendarAgeInYearsAt(BirthDateTime, Today())

define function "LengthInDays"(Value Interval<DateTime> ): // I am define line 1
  difference in days between start of Value and  // I am define line 2
  end of Value // I am define line 3

/*
define function "LengthInDays"(Value Interval<DateTime> ):
  difference in days between start of Value and
  end of Value
*/

define "Denominator Exclusions":
  "Patient Transferred from Acute Care or Inpatient Facility" // union1
    union "Patient Left Before Treatment Completion" // union2
    union "Patient Died During Emergency Department Visit for Septic Shock"
    union "Patient Receiving Comfort Measures"
    union "Patient Declined Sepsis Care"
    union "Emergency Department Visit for Septic Shock with Other Secondary Diagnosis"
    union "Patient Has End Stage Liver Disease or Status Epilepticus"
    //union "Patient Currently Taking Hydroxychloroquine"  commented out union
    union "Patient Currently Taking Epinephrine"

/*
define "No VTE Prophylaxis Device Order":
  (["ServiceRequest": "Venous foot pumps (VFP)"]
    union ["ServiceRequest": "Intermittent pneumatic compression devices Test"]
    union ["ServiceRequest": "Graduated compression stockings Test"]
  ) DeviceOrder
    where DeviceOrder.status = 'completed'
      and DeviceOrder.doNotPerform is true
*/
