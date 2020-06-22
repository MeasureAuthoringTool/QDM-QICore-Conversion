library notfound version '0.0.019'

using FHIR version '4.0.1'

include FHIRHelpers version '4.0.001' called FHIRHelpers
include SupplementalDataElements_FHIR4 version '2.0.000' called SDE
include MATGlobalCommonFunctions_FHIR4 version '5.0.000' called Global
include MATGlobalCommonFunctionsMike version '6.0.000' called Mike2

codesystem "LOINC": 'http://loinc.org'

code "Birth date": '21112-8' from "LOINC" display 'Birth date'

parameter "Measurement Period" Interval<DateTime>

context Patient

define "SDE Ethnicity":
  SDE."SDE Ethnicity"

define "SDE Payer":
  SDE."SDE Payer"

define "SDE Race":
  SDE."SDE Race"

define "SDE Sex":
  SDE."SDE Sex"

define "num":
  1

define "denom":
  2

define "ip":
  3

