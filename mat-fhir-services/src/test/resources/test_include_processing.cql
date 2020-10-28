library JEK_TobaccoUseScreeningandCessationInterventionCQL version '2.0.000'

using QDM version '5.4'

include MATGlobalCommonFunctions version '4.0.000' called Global
include SupplementalDataElements version '1.0.0' called SDE

include AdultOutpatientEncounters version '1.2.345' called ADP
include AdvancedIllnessandFrailtyExclusion version '5.6.789' called AVI
include Hospice version '0.1.234' called HSP
include TJCOverall version '5.6.789' called TJC
include VTEICU version '5.6.789' called VTE

valueset "Current Smoking Tobacco User": 'urn:oid:2.16.840.1.113762.1.4.1111.99'
valueset "Current Tobacco Non User": 'urn:oid:2.16.840.1.113762.1.4.1111.96'
valueset "Current Tobacco Smoker": 'urn:oid:2.16.840.1.113883.3.600.2390'
valueset "Ethnicity": 'urn:oid:2.16.840.1.114222.4.11.837'
valueset "Limited Life Expectancy": 'urn:oid:2.16.840.1.113883.3.526.3.1259'
valueset "Medical Reason": 'urn:oid:2.16.840.1.113883.3.526.2.313'
valueset "Not Screened for Tobacco Use, Reason Not Given": 'urn:oid:2.16.840.1.113762.1.4.1182.5'
valueset "ONC Administrative Sex": 'urn:oid:2.16.840.1.113762.1.4.1'
valueset "Patient Encounter": 'urn:oid:2.16.840.1.113762.1.4.1116.428'
valueset "Patient Encounters": 'urn:oid:2.16.840.1.113762.1.4.1116.438'
valueset "Patient Provider Interaction": 'urn:oid:2.16.840.1.113883.3.526.3.1012'
valueset "Patient Reason": 'urn:oid:2.16.840.1.113883.3.526.2.311'
valueset "Payer": 'urn:oid:2.16.840.1.114222.4.11.3591'
valueset "Preventative Encounter": 'urn:oid:2.16.840.1.113762.1.4.1116.429'
valueset "Race": 'urn:oid:2.16.840.1.114222.4.11.836'
valueset "Reason No Smoking Cessation Intervention": 'urn:oid:2.16.840.1.113762.1.4.1116.342'
valueset "System Reason": 'urn:oid:2.16.840.1.113883.3.526.2.310'
valueset "Tobacco Cessation not Done, Reason not Specified": 'urn:oid:2.16.840.1.113762.1.4.1116.435'
valueset "Tobacco Non-User": 'urn:oid:2.16.840.1.113883.3.526.3.1189'
valueset "Tobacco Screening not Done, Medical Reason": 'urn:oid:2.16.840.1.113762.1.4.1116.437'
valueset "Tobacco Status Unknown": 'urn:oid:2.16.840.1.113762.1.4.1116.269'
valueset "Tobacco Use Cessation Counseling": 'urn:oid:2.16.840.1.113883.3.526.3.509'
valueset "Tobacco Use Cessation Pharmacotherapy": 'urn:oid:2.16.840.1.113762.1.4.1116.343'
valueset "Tobacco Use Screening": 'urn:oid:2.16.840.1.113883.3.526.3.1278'
valueset "Tobacco User with Cessation Intervention": 'urn:oid:2.16.840.1.113762.1.4.1116.432'
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
define "Age":
  AgeInYearsAt(start of "Measurement Period")>= 18
define "Preventative Enc":
  ["Encounter, Performed": "Preventative Encounter"] PreventEnc
  	where PreventEnc.relevantPeriod starts during "Measurement Period"
define "Office Visits":
  ( ["Encounter, Performed": "Patient Encounter"]
  	union ["Encounter, Performed": "Patient Provider Interaction"] ) Encounter
  	where Encounter.relevantPeriod starts during "Measurement Period"
define "Initial Population":
  "Age"
  	and "Most Recent Encounter" is not null
  	and ( exists "Preventative Enc"
  			or Count("Office Visits")>= 2
  	)
define "Denominator 1":
  "Initial Population"
define "Denominator 3":
  "Initial Population"
define "Smokes Tobacco":
  ( ( ["Assessment, Performed": "Tobacco Use Screening"] TobaccoUseScreening ) User
  	where User.result in "Current Tobacco Smoker" ) User
  	with "Most Recent Encounter" ENC
  		such that ( User.authorDatetime 731 days or less before start of ENC.relevantPeriod )
define "Tobacco Screening Nonsmoker":
  ( ( ["Assessment, Performed": "Tobacco Use Screening"] Tobacco
  		where Tobacco.result in "Tobacco Non-User"
  			or Tobacco.result in "Tobacco Status Unknown"
  ) ) NonUser
  	with "Most Recent Encounter" ENC
  		such that ( NonUser.authorDatetime 731 days or less before start of ENC.relevantPeriod )
define "Tobacco Screening Nonsmoker Dx":
  ( ( ["Procedure, Performed": "Current Tobacco Non User"]
  		union ["Encounter, Performed": "Current Tobacco Non User"] ) NonUser
  		with "Most Recent Encounter" ENC
  			such that NonUser.relevantPeriod starts 731 days or less before start of ENC.relevantPeriod
  )
define "Cessation Programs":
  ( ["Encounter, Performed": "Tobacco User with Cessation Intervention"]
  	union ["Procedure, Performed": "Tobacco User with Cessation Intervention"]
  	union ["Encounter, Performed": "Tobacco Use Cessation Counseling"]
  	union ["Procedure, Performed": "Tobacco Use Cessation Counseling"] ) Counseling
  	with "Most Recent Encounter" ENC
  		such that ( Counseling.relevantPeriod starts 731 days or less before start of ENC.relevantPeriod
  				or Counseling.relevantPeriod starts before
  				end of "Measurement Period"
  		)
define "Tobacco Cessation":
  ( ["Intervention, Performed": "Tobacco Use Cessation Counseling"]
  	union ["Medication, Administered": "Tobacco Use Cessation Pharmacotherapy"]
  	union ["Medication, Order": "Tobacco Use Cessation Pharmacotherapy"] ) User
  	with "Most Recent Encounter" ENC
  		such that ( User.relevantPeriod starts 731 days or less before start of ENC.relevantPeriod
  				or User.relevantPeriod starts before
  				end of "Measurement Period"
  		)
define "Denominator 2":
  "Initial Population"
  	and exists "Smokes Tobacco"
define "Tobacco Screening Smoker":
  exists "Smokes Tobacco"
  	and exists "Tobacco Cessation"
define "Life Expectancy Limited":
  ["Diagnosis": "Limited Life Expectancy"] Life
  	where Life.prevalencePeriod starts 24 months or less before
  	end of "Measurement Period"
define "Tobacco Screening Not Done Reason Not Given":
  ["Encounter, Performed": "Not Screened for Tobacco Use, Reason Not Given"] ReasonNotDone2
  	with "Most Recent Encounter" ENC
  		such that ( ReasonNotDone2.relevantPeriod starts 731 days or less before start of ENC.relevantPeriod )
define "Tobacco Cessation Not Done Reason Not Given":
  ["Encounter, Performed": "Tobacco Cessation not Done, Reason not Specified"] ReasonNotDone2
  	with "Most Recent Encounter" ENC
  		such that ( ReasonNotDone2.relevantPeriod starts 731 days or less before start of ENC.relevantPeriod )
define "Most Recent Encounter":
  Last(((["Encounter, Performed": "Patient Encounters"]
  		ƒ ["Encounter, Performed": "Preventative Encounter"]
  		union ["Encounter, Performed": "Patient Provider Interaction"]) LastEnc
  		where LastEnc.relevantPeriod starts during "Measurement Period") EE
  		sort by start of relevantPeriod
  )
define "Numerator 1":
  not exists "Tobacco Screening Not Done Reason Not Given"
  	and ( exists "Tobacco Screening Nonsmoker"
  			or exists "Tobacco Screening Nonsmoker Dx"
  			or exists "Smokes Tobacco"
  	)
define "Numerator 2":
  not exists "Tobacco Cessation Not Done Reason Not Given"
  	and ( exists "Cessation Programs"
  			or exists "Tobacco Cessation"
  	)
define "Numerator 3":
  not exists "Tobacco Screening Not Done Reason Not Given"
  	and not exists "Tobacco Cessation Not Done Reason Not Given"
  	and ( ( exists "Tobacco Screening Nonsmoker"
  				or exists "Tobacco Screening Nonsmoker Dx"
  		)
  			or ( "Tobacco Screening Smoker"
  					and ( exists "Cessation Programs"
  							or exists "Tobacco Cessation"
  					)
  			)
  	)
define "Tobacco Screening Not Done Medical Reason":
  ( ["Procedure, Performed": "Tobacco Screening not Done, Medical Reason"] ) Intervention
  	with "Most Recent Encounter" ENC
  		such that ( Intervention.relevantPeriod starts 731 days or less before start of ENC.relevantPeriod )
define "Denominator Exceptions 1":
  exists "Tobacco Screening Not Done Medical Reason"
  	or exists "Life Expectancy Limited"
define "Reason Tobacco Screening not Done":
  ["Procedure, Performed": "Tobacco Screening not Done, Medical Reason"] ReasonNotDone
  	with "Most Recent Encounter" ENC
  		such that ( ReasonNotDone.relevantPeriod starts 731 days or less before start of ENC.relevantPeriod )
define "Reasons Cessation Intervention Not Done":
  ( ["Intervention, Performed": "Reason No Smoking Cessation Intervention"]
  	union ["Encounter, Performed": "Reason No Smoking Cessation Intervention"]
  	union ["Procedure, Performed": "Tobacco Screening not Done, Medical Reason"] ) Intervention
  	with "Most Recent Encounter" ENC
  		such that ( Intervention.relevantPeriod starts 731 days or less before start of ENC.relevantPeriod )
define "Denominator Exceptions 3":
  exists "Reasons Cessation Intervention Not Done"
  	or exists "Reasons Med not Admin"
  	or exists "Reason Tobacco Screening not Done"
  	or exists "Life Expectancy Limited"
define "Denominator Exceptions 2":
  exists "Reasons Med not Admin"
  	or exists "Reasons Cessation Intervention Not Done"
  	or exists "Life Expectancy Limited"
define "Reasons Med not Admin":
  ["Medication, Not Administered": "Tobacco Use Cessation Pharmacotherapy"] MedAdmin
  	with "Most Recent Encounter" ENC
  		such that ( MedAdmin.negationRationale in "Medical Reason" )
  			and ( MedAdmin.relevantPeriod starts 731 days or less before start of ENC.relevantPeriod )