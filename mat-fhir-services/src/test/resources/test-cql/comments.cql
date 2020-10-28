/* I am comment also*/
library BonnieTestDRCBirthdate version '0.0.005'

/*CMS936
Patient-based
(EH or EP?)
Proportion scoring*/

using QDM version '5.4'

include BirthdateDead version '1.0.000' called BD

codesystem "SNOMEDCT": 'urn:oid:2.16.840.1.113883.6.96'

valueset "Ethnicity": 'urn:oid:2.16.840.1.114222.4.11.837'
valueset "ONC Administrative Sex": 'urn:oid:2.16.840.1.113762.1.4.1'
valueset "Payer": 'urn:oid:2.16.840.1.114222.4.11.3591'
valueset "Race": 'urn:oid:2.16.840.1.114222.4.11.836'

code "Ugly scar (disorder)": '49325007' from "SNOMEDCT" display 'Ugly scar (disorder)'

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

define "IPOP Encounters":
	["Encounter, Performed"] E
		where E.relevantPeriod overlaps "Measurement Period"

define "Initial Population":
	exists "IPOP Encounters"

define "Denominator Exclusions":
	exists "IPOP Encounters" E
		with ["Diagnostic Study, Performed": result in "MyCodes"] S
			such that S.authorDatetime during E.relevantPeriod

define "MyCodes":
	{ BD."Birth date", "Ugly scar (disorder)", BD."Dead (finding)" }

define "Numerator":
	exists "IPOP Encounters" E
		with ["Diagnostic Study, Performed": result in BD."Dead (finding)"] S
			such that S.authorDatetime during E.relevantPeriod

define "Denominator":
	"Initial Population"
		and "Foo"

define "Foo":
	exists ["Patient Characteristic Birthdate": BD."Birth date"]