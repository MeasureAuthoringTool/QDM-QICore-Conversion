library Bonnie593Test version '0.0.001'

using QDM version '5.5'

include OpioidDataWithNoInjectableFormulationsAndBulprenorphine version '0.3.000' called OpioidDataLibrary

codesystem "SNOMEDCT": 'urn:oid:2.16.840.1.113883.6.96'

valueset "All Primary and Secondary Cancer": 'urn:oid:2.16.840.1.113762.1.4.1111.161'
valueset "Encounter Hospital Outpatient": 'urn:oid:2.16.840.1.113883.3.3157.1002.14'
valueset "Ethnicity": 'urn:oid:2.16.840.1.114222.4.11.837'
valueset "Hospice Care": 'urn:oid:2.16.840.1.113883.3.3157.1004.20'
valueset "Inpatient encounter CCDE": 'urn:oid:2.16.840.1.113762.1.4.1104.8'
valueset "Left Against Medical Advice": 'urn:oid:2.16.840.1.113883.3.117.1.7.1.308'
valueset "Nonprimary Total Hip, Total Knee Replacement": 'urn:oid:2.16.840.1.113762.1.4.1206.5'
valueset "ONC Administrative Sex": 'urn:oid:2.16.840.1.113762.1.4.1'
valueset "Opioid Medications": 'urn:oid:2.16.840.1.113883.3.3157.1004.26'
valueset "Palliative Care": 'urn:oid:2.16.840.1.113762.1.4.1111.162'
valueset "Payer": 'urn:oid:2.16.840.1.114222.4.11.3591'
valueset "Race": 'urn:oid:2.16.840.1.114222.4.11.836'
valueset "Sickle Cell Disease and Related Blood Disorders": 'urn:oid:2.16.840.1.113762.1.4.1029.35'
valueset "Substance Abuse": 'urn:oid:2.16.840.1.113883.3.464.1003.106.12.1004'
valueset "Total Hip, Total Knee Replacement": 'urn:oid:2.16.840.1.113762.1.4.1029.96'

code "Every eight hours (qualifier value)": '307469008' from "SNOMEDCT" display 'Every eight hours (qualifier value)'
code "Every eight to twelve hours (qualifier value)": '396140003' from "SNOMEDCT" display 'Every eight to twelve hours (qualifier value)'
code "Every forty eight hours (qualifier value)": '396131002' from "SNOMEDCT" display 'Every forty eight hours (qualifier value)'
code "Every four hours (qualifier value)": '225756002' from "SNOMEDCT" display 'Every four hours (qualifier value)'
code "Every seventy two hours (qualifier value)": '396143001' from "SNOMEDCT" display 'Every seventy two hours (qualifier value)'
code "Every six hours (qualifier value)": '307468000' from "SNOMEDCT" display 'Every six hours (qualifier value)'
code "Every six to eight hours (qualifier value)": '396139000' from "SNOMEDCT" display 'Every six to eight hours (qualifier value)'
code "Every thirty six hours (qualifier value)": '396126004' from "SNOMEDCT" display 'Every thirty six hours (qualifier value)'
code "Every three to four hours (qualifier value)": '225754004' from "SNOMEDCT" display 'Every three to four hours (qualifier value)'
code "Every three to six hours (qualifier value)": '396127008' from "SNOMEDCT" display 'Every three to six hours (qualifier value)'
code "Every twelve hours (qualifier value)": '307470009' from "SNOMEDCT" display 'Every twelve hours (qualifier value)'
code "Every twenty four hours (qualifier value)": '396125000' from "SNOMEDCT" display 'Every twenty four hours (qualifier value)'
code "Every two to four hours (qualifier value)": '225752000' from "SNOMEDCT" display 'Every two to four hours (qualifier value)'
code "Four times daily (qualifier value)": '307439001' from "SNOMEDCT" display 'Four times daily (qualifier value)'
code "Once daily (qualifier value)": '229797004' from "SNOMEDCT" display 'Once daily (qualifier value)'
code "One to four times a day (qualifier value)": '396109005' from "SNOMEDCT" display 'One to four times a day (qualifier value)'
code "One to three times a day (qualifier value)": '396108002' from "SNOMEDCT" display 'One to three times a day (qualifier value)'
code "One to two times a day (qualifier value)": '396107007' from "SNOMEDCT" display 'One to two times a day (qualifier value)'
code "Three times daily (qualifier value)": '229798009' from "SNOMEDCT" display 'Three times daily (qualifier value)'
code "Three to four times a day (qualifier value)": '396112008' from "SNOMEDCT" display 'Three to four times a day (qualifier value)'
code "Twice a day (qualifier value)": '229799001' from "SNOMEDCT" display 'Twice a day (qualifier value)'
code "Two to four times a day (qualifier value)": '396111001' from "SNOMEDCT" display 'Two to four times a day (qualifier value)'

parameter "Measurement Period" Interval<DateTime>

context Patient

define "Active Opioid Prescriptions In The Opioid Episode":
  /** Active Periods with Allowed Gaps with Medications and Durations **/
  "Opioid Episode" Period
    let Prescriptions: ( "Qualifying Opioid Prescription Orders" Opioid
        where Opioid.medPeriod during Period
    )
    return {
      opioidEpisode: Period,
      activeMeds: Prescriptions Med
        return Med.med,
      activePeriod: Prescriptions Med
        return Med.medPeriod,
      cmd: "CumulativeMedicationDuration"(Prescriptions Med
          return Med.medPeriod
      )

  /** Note that if we use daysSupplied, the result will not be correct as
  the days were truncated to the 90th day.  The best way is to obtain all the days in the period less the truncated days.
  cmd: "TestCumulativeMedicationDuration"(Prescriptions P
  return P.med
  )
  **/
    }

define "Active Prescriptions In The Opioid Episode With A Total MME":
  "Active Opioid Prescriptions In The Opioid Episode" Period
    let ScriptsMMEDays: "CalculateDailyMMEPerPrescription"(Period.activeMeds, Period.opioidEpisode)
    return {
      opioidEpisode: Period.opioidEpisode,
      meds: Period.activeMeds,
      cmd: Period.cmd,
      mmeDays: ScriptsMMEDays/*,
  mmeSumDays: "CalculatesTotalMMESumDays"(ScriptsMMEDays) */
    }

define "Denominator":
  "Initial Population"

define "Denominator Exclusion":
  "Had More Than One THA-TKA Procedures Within 180 Days Period"
    or exists "Had Opioid Prescription Within 90 Days Prior To Admission Date"
    or exists "Diagnosed With Opioid Use Disorder 365 Days Prior To Admission Date"
    or exists ( "Had Hospice Care Within 455 Days Period" )
    or exists ( "Diagnosed With Cancer Within 455 Days Period" )
    or exists ( "Diagnosed With Sickle Cell Disease Within 455 Days Period" )
    or exists ( "Discharged Against Medical Advice During Index Admission" )

define "Had Hospice Care Within 455 Days Period":
  "Hospice Care Order Within 455 Days Period"
    union "Hospice Care Performed Within 455 Days"

/*Check if patient had more than one THA/TKA procedures within 180 days period (i.e. 90 days prior to admission date and 90 days after discharge)*/
define "Had More Than One THA-TKA Procedures Within 180 Days Period":
  Count("THA-TKA Procedures Within 180-Days Period")> 1

define "Had Palliative Care Within 730 Days Period":
  "Palliative Care Order Within 730 Days Period"
    union "Palliative Care Performed Within 730 Days Period"

define "Months Containing 28 Days":
  /*Identifies the months in the year containing 28 days for determining number of days the medication is prescribed*/
  { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28 }

define "Months Containing 29 Days":
  /*Identifies the months in the year containing 29 days for determining the number of days a medication prescribed*/
  { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29 }

define "Months Containing 30 Days":
  /*Identifies the months in the year containing 30 days for determining the number of days a medication is prescribed*/
  { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30 }

define "Months Containing 31 Days":
  /*Identifies the months in the year containing 31 days for determining the number of days a medication is prescribed*/
  { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31 }

define "MonthsInYear":
  /*Identifies the number of months in a calendar year to determine the number of days a medication is prescribed*/
  { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 }

define "Numerator":
  exists "Qualifying Opioid Prescriptions With Medication Info"
    and exists "Qualifying Opioid Prescription Orders"

define "Opioid Episode":
  /*** Overall Active Opioid Period ***/
  Interval[start of First("Qualifying Opioid Prescription Orders" OpioidPeriod
      sort by start of medPeriod
  ).medPeriod,
  end of Last("Qualifying Opioid Prescription Orders" OpioidPeriod
      sort by
      end of medPeriod
  ).medPeriod]

/*This returns a list of active opioid medication orders during or overlaping the measurement period.*/
define "Opioid Medication Order during Measurement Period":
  ["Medication, Order": "Opioid Medications"] OpioidMedicationOrder
    where "MedicationRelevantPeriod"(OpioidMedicationOrder)overlaps "Measurement Period"

define "Qualifying Opioid Prescription Periods":
  /** Opioid Medication Periods **/
  collapse ( "Qualifying Opioid Prescription Orders" OpioidPeriod
      return OpioidPeriod.medPeriod
  )

define "Qualifying Opioid Prescriptions With Medication Info":
  "Qualifying Opioid Prescription Orders" Opioid
    let medicationPostDischarge: "Qualifying Opioid Prescription Orders",
    activePrescriptions: "Active Opioid Prescriptions In The Opioid Episode",
    activePrescriptionWithMME: "Active Prescriptions In The Opioid Episode With A Total MME"
  /***,durationWithTimeDifference: "Testing Duration In Days Of An Interval With Time Difference",
  durationWithSameTime: "Testing Duration In Days Of An Interval With Same Time",
  durationWithSameDayDifferentTime: "Testing Duration In Days Of An Interval With Same Day Different Time"
  ***/
    return {
      medicationPostDischarge: medicationPostDischarge,
      activePrescriptions: activePrescriptions,
      activePrescriptionWithMME: activePrescriptionWithMME//,
    //durationWithSameTime: durationWithSameTime,
    //durationWithTimeDifference: durationWithTimeDifference,
    //durationWithSameDayDifferentTime: durationWithSameDayDifferentTime
    }

define "Patient Encounters":
  ( ["Encounter, Performed": "Inpatient encounter CCDE"]
    union ["Encounter, Performed": "Encounter Hospital Outpatient"] ) Encounter

define "Encounter Related To Elective Primary THA and TKA Procedures With Age":
  "Encounters Related To Elective Primary THA and TKA Procedures During Measurement Period" Procedure
    with ["Patient Characteristic Birthdate"] PatientBirthday
      such that "CalendarAgeInYearAt"(PatientBirthday.birthDatetime, start of Procedure.relevantPeriod)>= 18

define "Encounters Related To Elective Primary THA and TKA Procedures During Measurement Period":
  "Patient Encounters" Encounters
    with "Primary THA-TKA Procedures" QualifyingProcedures
      such that QualifyingProcedures.relevantPeriod starts during Encounters.relevantPeriod
        and Encounters.relevantPeriod overlaps "Measurement Period"

define "Primary THA-TKA Procedures":
  ["Procedure, Performed": "Total Hip, Total Knee Replacement"] Procedure

define "Non-Primary THA_TKA Procedures":
  ["Procedure, Performed": "Nonprimary Total Hip, Total Knee Replacement"] NonPrimaryProcedures

/*Check if patient had Palliative or Hospice Care 365 days prior to admission date or 365 days after discharge date*/
define "Palliative Care Performed Within 730 Days Period":
  "Encounters Related To Elective Primary THA and TKA Procedures During Measurement Period" Admission
    with ["Intervention, Performed": "Palliative Care"] CarePerformed
      such that CarePerformed.relevantPeriod during Interval[start of Admission.relevantPeriod - 365 days,
      end of Admission.relevantPeriod + 365 days]

define "Hospice Care Order Within 455 Days Period":
  "Encounters Related To Elective Primary THA and TKA Procedures During Measurement Period" IndexAdmission
    with ( ["Intervention, Order": "Hospice Care"]
      union ["Intervention, Recommended": "Hospice Care"] ) HPCareOrder
      such that HPCareOrder.authorDatetime during Interval[start of IndexAdmission.relevantPeriod - 365 days,
      end of IndexAdmission.relevantPeriod + 90 days]

define "Diagnosed With Cancer Within 455 Days Period":
  "Encounters Related To Elective Primary THA and TKA Procedures During Measurement Period" IndexAdmission
    with ["Diagnosis": "All Primary and Secondary Cancer"] CancerDiagnosis
      such that CancerDiagnosis.prevalencePeriod starts during Interval[start of IndexAdmission.relevantPeriod - 365 days,
      end of IndexAdmission.relevantPeriod + 90 days]

/*Check if patient was diagnosed with opioid use disorder 365 days prior to admission date*/
define "Diagnosed With Opioid Use Disorder 365 Days Prior To Admission Date":
  "Encounters Related To Elective Primary THA and TKA Procedures During Measurement Period" Encounter
    with ["Diagnosis": "Substance Abuse"] OpioidUseDisorder
      such that OpioidUseDisorder.prevalencePeriod starts 365 days or less on or before start of Encounter.relevantPeriod

define "Diagnosed With Sickle Cell Disease Within 455 Days Period":
  "Encounters Related To Elective Primary THA and TKA Procedures During Measurement Period" IndexAdmission
    with ["Diagnosis": "Sickle Cell Disease and Related Blood Disorders"] CancerDiagnosis
      such that CancerDiagnosis.prevalencePeriod starts during Interval[start of IndexAdmission.relevantPeriod - 365 days,
      end of IndexAdmission.relevantPeriod + 90 days]
        or CancerDiagnosis.authorDatetime during Interval[start of IndexAdmission.relevantPeriod - 365 days,
        end of IndexAdmission.relevantPeriod + 90 days]

define "Discharged Against Medical Advice During Index Admission":
  "Encounters Related To Elective Primary THA and TKA Procedures During Measurement Period" DischargedAMA
    where DischargedAMA.dischargeDisposition in "Left Against Medical Advice"
      and DischargedAMA.relevantPeriod starts during "Measurement Period"

define "Had Opioid Prescription Within 90 Days Prior To Admission Date":
  "Encounters Related To Elective Primary THA and TKA Procedures During Measurement Period" ProcedureDate
    with ["Medication, Order": "Opioid Medications"] MedOrder
      such that MedOrder.relevantPeriod starts 90 days or less on or before start of ProcedureDate.relevantPeriod

define "Had 2 Or More THA_TKA Procedures Codes During The Index Admission":
  ( "Patient Encounters" ) Encounter
    where Count(("Primary THA-TKA Procedures"
        union "Non-Primary THA_TKA Procedures")Procedures
        where Procedures.relevantPeriod starts during Encounter.relevantPeriod
          and Encounter.relevantPeriod starts during "Measurement Period"
    )> 2

define "Qualifying Opioid Prescription Orders":
  /***
  *** Opioid Medication Periods in Measurement Period
  ***/
  from
    ["Medication, Order": "Opioid Medications"] Opioid,
    "Encounters Related To Elective Primary THA and TKA Procedures During Measurement Period" QualifyingEncounter
    where Opioid.relevantPeriod starts 90 days or less on or after
    end of QualifyingEncounter.relevantPeriod
  /***
  where Opioid.relevantPeriod starts during Interval[
  end of QualifyingEncounter.relevantPeriod - 1 day,
  end of QualifyingEncounter.relevantPeriod + 90 days]
  ***/
    return {
  /** We need to truncate the days that extends beyond 90 days post discharge here as this updated medPeriod will be used to determine the opioid episode **/
      medPeriod: "MedicationRelevantPeriod"(Opioid)
        intersect Interval[
        end of QualifyingEncounter.relevantPeriod, ToDate(
          end of QualifyingEncounter.relevantPeriod + 90 days
        )],
      med: Opioid
    }

define "THA-TKA Procedures Within 180-Days Period":
  from
    "Encounters Related To Elective Primary THA and TKA Procedures During Measurement Period" Encounter,
    ( "Primary THA-TKA Procedures"
      union "Non-Primary THA_TKA Procedures" ) Procedures
    where Procedures.relevantPeriod starts during Interval[start of Encounter.relevantPeriod - 90 days,
    end of Encounter.relevantPeriod + 90 days]
    return Procedures

/*Check if patient was recommended for Palliative or Hospice Care 365 days prior to admission date or 365 days after discharge date*/
define "Palliative Care Order Within 730 Days Period":
  "Encounters Related To Elective Primary THA and TKA Procedures During Measurement Period" IndexAdmission
    with ["Intervention, Order": "Palliative Care"] CareOrder
      such that CareOrder.authorDatetime during Interval[start of IndexAdmission.relevantPeriod - 365 days,
      end of IndexAdmission.relevantPeriod + 365 days]

define "Hospice Care Performed Within 455 Days":
  "Encounters Related To Elective Primary THA and TKA Procedures During Measurement Period" IndexAdmission
    with ["Intervention, Performed": "Hospice Care"] HPCarePerformed
      such that HPCarePerformed.relevantPeriod starts during Interval[start of IndexAdmission.relevantPeriod - 365 days,
      end of IndexAdmission.relevantPeriod + 90 days]

define "Initial Population":
  exists "Encounter Related To Elective Primary THA and TKA Procedures With Age"

define function "DosesPerDay"(frequency Code ):
  case
    when frequency ~ "Once daily (qualifier value)" then 1.0
    when frequency ~ "Twice a day (qualifier value)" then 2.0
    when frequency ~ "Three times daily (qualifier value)" then 3.0
    when frequency ~ "Four times daily (qualifier value)" then 4.0
    when frequency ~ "Every twenty four hours (qualifier value)" then 1.0
    when frequency ~ "Every twelve hours (qualifier value)" then 2.0
    when frequency ~ "Every thirty six hours (qualifier value)" then 0.67
    when frequency ~ "Every eight hours (qualifier value)" then 3.0
    when frequency ~ "Every four hours (qualifier value)" then 6.0
    when frequency ~ "Every six hours (qualifier value)" then 4.0
    when frequency ~ "Every seventy two hours (qualifier value)" then 0.34
    when frequency ~ "Every forty eight hours (qualifier value)" then 0.5
    when frequency ~ "Every eight to twelve hours (qualifier value)" then 2.0
    when frequency ~ "Every six to eight hours (qualifier value)" then 3.0
    when frequency ~ "Every three to four hours (qualifier value)" then 6.0
    when frequency ~ "Every three to six hours (qualifier value)" then 4.0
    when frequency ~ "Every two to four hours (qualifier value)" then 6.0
    when frequency ~ "One to four times a day (qualifier value)" then 4.0
    when frequency ~ "One to three times a day (qualifier value)" then 3.0
    when frequency ~ "One to two times a day (qualifier value)" then 2.0
    when frequency ~ "Two to four times a day (qualifier value)" then 4.0
    else null
  end

define function "IsTransdermalPatch"(doseFormCode Code ):
  ToInteger(doseFormCode.code)= 316987

define function "GetConversionFactor"(ingredientCode Code, dailyDose Quantity, doseFormCode Code ):
  case ToInteger(ingredientCode.code)
    when 1841 then 7
    when 2670 then 0.15
    when 3423 then 4
    when 4337 then (
      case
        when ToInteger(doseFormCode.code)in { 970789, 317007, 316992 } then 0.13
        when ToInteger(doseFormCode.code)= 346163 then 0.18
        when ToInteger(doseFormCode.code)in { 126542, 346163 } then 0.16
        when "IsTransdermalPatch"(doseFormCode)then 7.2
        else 1000
      end
    )
    when 5489 then 1
    when 6378 then 11
    when 6754 then 0.1
    when 6813 then (
      case
        when dailyDose.value between 1 and 20 then 4
        when dailyDose.value between 21 and 40 then 8
        when dailyDose.value between 41 and 60 then 10
        when dailyDose.value >= 61 then 12
        else 1000
      end
    )
    when 7052 then 1
    when 7676 then 1
    when 7804 then 1.5
    when 7814 then 3
    when 8001 then 0.37
    when 10689 then 0.1
    when 23088 then 0.25
    when 787390 then 0.4
    else 0
  end

define function "ToUnifiedCodeForUnitsOfMeasure"(unit String ):
  case unit
    when 'MG' then 'mg'
    when 'MG/ACTUAT' then 'mg/{actuat}'
    when 'MG/HR' then 'mg/h'
    when 'MG/ML' then 'mg/mL'
    else 'unknown{' + unit + '}'
  end

define function "GetMedicationDailyDose"(ingredientCode Code, strength Quantity, doseFormCode Code, doseQuantity Quantity, dosesPerDay Decimal ):
  case
    when "IsTransdermalPatch"(doseFormCode)then if ToInteger(ingredientCode.code)= 4337 then Quantity { value: ( if doseQuantity is null then 0
        else doseQuantity.value * strength.value
    ), unit: strength.unit }
      else null
    when doseQuantity.unit in { 'mg', 'mcg' } then Quantity { value: ( if dosesPerDay is null then 0
        else dosesPerDay * doseQuantity.value
    ), unit: doseQuantity.unit }
    when doseQuantity.unit = 'mL'
      and ( PositionOf('/mL', strength.unit)= Length(strength.unit)- 3 ) then Quantity { value: ( if dosesPerDay is null then 0
        else dosesPerDay * doseQuantity.value * strength.value
    ), unit: Substring(strength.unit, 0, PositionOf('/', strength.unit))}
    else Quantity { value: ( if ( ( doseQuantity is null )
        or ( dosesPerDay is null )
    ) then 0
      else dosesPerDay * doseQuantity.value * strength.value
  ), unit: Substring(strength.unit, 0, PositionOf('/', strength.unit))}
  end

define function "EnsureMicrogramQuantity"(strength Quantity ):
  if strength.value < 0.1
    and ( PositionOf('mg', strength.unit)= 0 ) then Quantity { value: strength.value * 1000, unit: 'mcg' + Substring(strength.unit, 2)}
    else strength

define function "CalendarAgeInYearAt"(BirthDateTime DateTime, AsOf DateTime ):
  years between ToDate(BirthDateTime)and ToDate(AsOf)

define function "DaysInMonth"(yr Integer, mo Integer ):
  /*Calculates the number of days of a prescription relevant period*/
  ( case
    when mo in { 1, 3, 5, 7, 8, 10, 12 } then "Months Containing 31 Days"
    when mo = 2 then if "IsLeapYear"(yr)then "Months Containing 29 Days"
      else "Months Containing 28 Days"
    else "Months Containing 30 Days"
  end ) D
    return DateTime(yr, mo, D)

define function "IsLeapYear"(yr Integer ):
  /*Determines if year is a leap year*/
  ( yr mod 4 = 0
      and yr mod 100 != 0
  )
    or yr mod 400 = 0

define function "DaysInYear"(yr Integer ):
  /*Lists are flattened to a single list*/
  flatten ( MonthsInYear Mo
      return "DaysInMonth"(yr, Mo)
  )

define function "DaysInPeriod"(period Interval<DateTime> ):
  /*Calculates the number of days of a prescription and if the prescription is within the measurement period*/
  case
    when start of period is null
      or
      end of period is null then null
    when start of period same year as
    end of period then case
      when month from start of period = month from
      end of period then ( "DaysInMonth"(year from start of period, month from start of period)) DaysMonth
        where DaysMonth same day or after start of period
          and DaysMonth same day or before
          end of period
      else ( "DaysInYear"(year from start of period)) DaysYear
      where DaysYear same day or after start of period
        and DaysYear same day or before
        end of period
    end
    when difference in years between start of period and
    end of period = 1 then ( ( "DaysInYear"(year from start of period)) DaysYear
        where DaysYear same day or after start of period
          and DaysYear same day or before
          end of period
    )
      union ( ( "DaysInYear"(year from
          end of period
        )) DaysYear
          where DaysYear same day or after start of period
            and DaysYear same day or before
            end of period
      )
    else null
  end

define function "GetDrugIngredients"(rxNormCode Code ):
  OpioidDataLibrary."DrugIngredients" DrugIngredientElement
    where DrugIngredientElement.drugCode = ToInteger(rxNormCode.code)
    return {
      rxNormCode: Code { code: ToString(DrugIngredientElement.drugCode), system: '2.16.840.1.113883.6.88', display: DrugIngredientElement.drugName },
      doseFormCode: Code { code: ToString(DrugIngredientElement.doseFormCode), system: '2.16.840.1.113883.6.88', display: DrugIngredientElement.doseFormName },
      doseFormName: DrugIngredientElement.doseFormName,
      ingredientCode: Code { code: ToString(DrugIngredientElement.ingredientCode), system: '2.16.840.1.113883.6.88', display: DrugIngredientElement.ingredientName },
      ingredientName: DrugIngredientElement.ingredientName,
      strength: "EnsureMicrogramQuantity"(Quantity { value: DrugIngredientElement.strengthValue, unit: "ToUnifiedCodeForUnitsOfMeasure"(DrugIngredientElement.strengthUnit)})
    }

/*Note that this is a way to deal with a consequence of not being able to create a Medication with relevantPeriod that has an unknown ending boundary.*/
define function "MedicationRelevantPeriod"(OpioidMed "Medication, Order" ):
  Interval[start of OpioidMed.relevantPeriod, if
  end of OpioidMed.relevantPeriod = maximum DateTime
    and OpioidMed.daysSupplied is not null then start of OpioidMed.relevantPeriod + Quantity { value: OpioidMed.daysSupplied, unit: 'd' }
    else
  end of OpioidMed.relevantPeriod]

define function "CumulativeMedicationDuration"(Periods List<Interval<DateTime>> ):
  /*** The collapse operator returns the unique set of intervals that completely covers the ranges present in the given list of intervals. In other words, adjacent intervals within a sorted list are merged if they either overlap or meet.***/
  Sum((collapse(Periods))Period
      return all duration in days of Period
  )

define function "CalculateDailyMMEPerPrescription"(prescriptions List<"Medication, Order">, opioidEpisode Interval<DateTime> ):
  prescriptions OpioidMed
    let dosesPerDay: "DosesPerDay"(OpioidMed.frequency),
  /***
  MME: singleton from ( "CalculateMME"({ {
  rxNormCode: OpioidMed.code,
  doseQuantity: OpioidMed.dosage,
  dosesPerDay: dosesPerDay
  } }
  ),**/
    medPeriod: OpioidMed.relevantPeriod
      intersect opioidEpisode
    return {
      rxNormCode: OpioidMed.code,
  //authortime: OpioidMed.authorDatetime,
      dosesPerDay: dosesPerDay,
      medPeriod: medPeriod,
  //conversionFactor: MME.conversionFactor,
      daysInPeriod: "DaysInPeriod"(medPeriod)
  //mme: MME.MME
    }

define function "More Than One THA-TKA Procedures Within 90 Days"(procedure List<"Procedure, Performed"> ):
  procedure procedure_1
    with procedure procedure_2
      such that procedure_2.relevantPeriod starts 90 days or less after start of procedure_1.relevantPeriod
        and procedure_1.relevantPeriod starts during "Measurement Period"
        and procedure_2.relevantPeriod starts during "Measurement Period"

define function "TestCalculation"(medications List<Tuple{rxNormCodeCode,doseQuantityQuantity,dosesPerDayDecimal}> ):
  medications Med
    return { Med.rxNormCode, Med.doseQuantity, Med.dosesPerDay }
