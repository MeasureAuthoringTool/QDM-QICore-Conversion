
[
~~_QDM::AdverseEvent_,~~ 
~~QDM::AllergyIntolerance, 
QDM::AssessmentOrder, 
QDM::AssessmentPerformed,
QDM::AssessmentRecommended,~~
~~QDM::CareGoal,~~
~~QDM::CommunicationPerformed,~~ 
~~QDM::DeviceApplied,~~
~~QDM::DeviceOrder,~~  --> not required 
~~QDM::Diagnosis,~~ 
~~QDM::DiagnosticStudyOrder,~~
~~QDM::DiagnosticStudyPerformed,~~ 
~~QDM::EncounterOrder,~~
~~QDM::EncounterPerformed,~~
~~QDM::FamilyHistory,~~
~~QDM::ImmunizationAdministered,
QDM::ImmunizationOrder,~~ 
~~QDM::InterventionOrder,~~ 
~~QDM::InterventionPerformed,~~ 
~~QDM::InterventionRecommended,~~ 
~~QDM::LaboratoryTestOrder,~~ 
~~QDM::LaboratoryTestPerformed,~~ 
~~QDM::MedicationActive,~~ 
~~QDM::MedicationAdministered,~~ ``
~~QDM::MedicationDischarge,~~ 
~~QDM::MedicationDispensed, 
QDM::MedicationOrder,~~ 
~~QDM::Participation,~~
~~QDM::PhysicalExamPerformed,~~ 
~~QDM::ProcedureOrder,~~ 
~~QDM::ProcedurePerformed,~~
~~QDM::MedicationDispensed,~~ 
~~QDM::MedicationOrder,~~ 
~~QDM::Participation,
QDM::PhysicalExamPerformed,~~ 
~~QDM::ProcedureOrder,~~ 
~~QDM::ProcedurePerformed,~~
~~QDM::SubstanceAdministered,~~ 
QDM::SubstanceOrder, -- Not done cannot convert 
QDM::SubstanceRecommended, -- Not done cannot convert only contains one
~~QDM::Symptom~~
]

 [QDM::PatientCharacteristic, 
 ~~QDM::PatientCharacteristicBirthdate,~~ 
 ~~QDM::PatientCharacteristicEthnicity,~~
   ~~QDM::PatientCharacteristicExpired,~~ 
   ~~QDM::PatientCharacteristicPayer,~~ 
   ~~QDM::PatientCharacteristicRace,~~ 
   ~~QDM::PatientCharacteristicSex~~ ]

---
**FIXED - uses json node and dynamically figure out the type.**

cqm_patients id: 5d654a171c76ba7ea32ea080 has a bad entry in fhirDataElements[2] array concerning components[0]:

It has the result as a string value 43510-06-10T08:00:00+00:00

While the vast majority of the other ones have it mapped as a code E.G. id: 5d6547af1c76ba7ea32e1d95
```javascript
 "components" : [ 
                    {
                        "result" : {
                            "code" : "78371-2",
                            "version" : null,
                            "descriptor" : null,
                            "system" : "2.16.840.1.113883.6.1"
                        }
                    }
                ],
```
---
**FIXED - uses json node and dynamically figure out the type.**

cqm_patients id: 5d654a171c76ba7ea32ea080 has a bad entry in fhirDataElements[0] array concerning result it is mapped as 
int32 69.

While the vast majority of the other ones have it mapped as a code E.G. id: 5d65454d1c76ba7ea32d989a

```javascript
"result" : {
                    "code" : "285981000119103",
                    "version" : null,
                    "system" : "2.16.840.1.113883.6.96",
                    "display" : "Acute Or Evolving Mi"
                },
```
---

cqm_patients id: 5d654ae61c76ba7ea32ed30c has a bad entry in fhirDataElements[1] array concerning targetOutcome it is mapped
like a QdmQuantity.
 
```javascript
   "targetOutcome": {
          "unit": "",
          "value": 2
        },
```

While the vast majority of the other ones have it mapped as a code E.G. id: 5d6547af1c76ba7ea32e1d95 fhirDataElements[0]:

```javascript
 "targetOutcome" : {
                    "code" : "82325-2",
                    "version" : null,
                    "system" : "2.16.840.1.113883.6.1",
                    "display" : "How severe is your knee stiffness after first wakening in the ..."
                },
```

---
**FIXED we ignore .**

cqm_patients id: 5d65454c1c76ba7ea32d9874 qdmpatient.extenddata maps _**correctly_** with example below


```javascript
 "origin_data": [
         {
           "patient_id": "55d230976c5d1c5f69000021",
           "measure_ids": [
             "42BF391F-38A3-4C0F-9ECE-DCD47E9609D9"
           ],
           "cms_id": "CMS104v2",
           "user_id": "55d208496c5d1c5f5f000000",
           "user_email": "elehner@mitre.org"
         }
       ],
```
---
**FIXED we ignore .**

Where patient id: 5d65454c1c76ba7ea62d9874 has it mapped as 2d array seen once where it went 7 layers deep for no 
apparent reason:

```javascript
"origin_data" : [ 
                [ 
                    {
                        "patient_id" : "55d230976c5d1c5f69000021",
                        "measure_ids" : [ 
                            "42BF391F-38A3-4C0F-9ECE-DCD47E9609D9"
                        ],
                        "cms_id" : "CMS104v2",
                        "user_id" : "55d208496c5d1c5f5f000000",
                        "user_email" : "elehner@mitre.org"
                    }
                ]
            ],
```

---

**FIXED we use jsonNode and echo back the output exactly like received.**

Where patient id: 5d65454e1c76ba7ea32d98f2 it has the OBSERV properties in expectedValues mapped as int and int array

```javascript
  "expectedValues": [
    {
      "measure_id": "9A033274-3D9B-11E1-8634-00237D5BF174",
      "population_index": 0,
      "IPP": 1,
      "MSRPOPL": 1,
      "MSRPOPLEX": 0,
      "OBSERV": [
        45
      ]
    },
    {
      "measure_id": "9A033274-3D9B-11E1-8634-00237D5BF174",
      "population_index": 1,
      "STRAT": 1,
      "IPP": 1,
      "MSRPOPL": 1,
      "MSRPOPLEX": 0,
      "OBSERV": [
        45
      ]
    },
    {
      "measure_id": "9A033274-3D9B-11E1-8634-00237D5BF174",
      "population_index": 2,
      "STRAT": 0,
      "IPP": 0,
      "MSRPOPL": 0,
      "MSRPOPLEX": 0,
      "OBSERV": 0
    }
  ],
```


---

## QDM::PatientCharacteristic

This first attribute seems out of place with the description **Patient Characteristic: Male**

The second attribute is the way that most all patients determine sex. We need to remove these.
```javascript
 "fhirDataElements": [
      {
        "_id": "5aeb774ab848463d625b28f1",
        "dataElementCodes": [
          {
            "code": "M",
            "system": "2.16.840.1.113883.5.1"
          }
        ],
        "_type": "QDM::PatientCharacteristic",
        "qdmTitle": "Patient Characteristic",
        "hqmfOid": "2.16.840.1.113883.10.20.28.4.53",
        "qdmCategory": "patient_characteristic",
        "qdmVersion": "5.5",
        "authorDatetime": "1951-02-14T08:00:00.000Z",
        "description": "Patient Characteristic: Male",
        "codeListId": "2.16.840.1.113883.3.560.100.1"
      },

 {
        "_id": "5d6545cc1c76ba7ea32db359",
        "dataElementCodes": [
          {
            "code": "M",
            "system": "2.16.840.1.113883.5.1",
            "display": "M",
            "version": null,
            "_type": "QDM::Code"
          }
        ],
        "_type": "QDM::PatientCharacteristicSex",
        "qdmTitle": "Patient Characteristic Sex",
        "hqmfOid": "2.16.840.1.113883.10.20.28.4.55",
        "qdmCategory": "patient_characteristic",
        "qdmStatus": "gender",
        "qdmVersion": "5.5"
      }

```

This first attribute seems out of place with the description **Patient Characteristic: Payer**

A type already exists for date elements QDM::PatientCharacteristicPayer

Can we ignore ....

```javascript
  {
        "_id": "5aeb77d4b848463d625b5f84",
        "dataElementCodes": [
          {
            "code": "1",
            "system": "2.16.840.1.113883.3.221.5"
          }
        ],
        "_type": "QDM::PatientCharacteristic",
        "qdmTitle": "Patient Characteristic",
        "hqmfOid": "2.16.840.1.113883.10.20.28.4.53",
        "qdmCategory": "patient_characteristic",
        "qdmVersion": "5.5",
        "authorDatetime": "2012-01-31T08:00:00.000Z",
        "description": "Patient Characteristic: Payer",
        "codeListId": "2.16.840.1.114222.4.11.3591"
      },
```
---

Does **Patient Characteristic: Medicare ID** indicate the patient is on medicaire - How to map?

```javascript
   {
         "_id": "5aeb7769b848463d625b364d",
         "dataElementCodes": [
           {
             "code": "45397-7",
             "system": "2.16.840.1.113883.6.1"
           }
         ],
         "_type": "QDM::PatientCharacteristic",
         "qdmTitle": "Patient Characteristic",
         "hqmfOid": "2.16.840.1.113883.10.20.28.4.53",
         "qdmCategory": "patient_characteristic",
         "qdmVersion": "5.5",
         "authorDatetime": "2012-07-24T08:00:00.000Z",
         "description": "Patient Characteristic: Medicare ID",
         "codeListId": "2.16.840.1.113883.3.1240.15.2.4004"
       }
```
---

https://trifolia-fhir.lantanagroup.com/igs/lantana_hapi_r4/vrdr/Observation-9676ae27-2a89-4295-913c-0d6847300a3a.json.html

Does **Patient Characteristic: Education** How to map?

- Patient Characteristic: College Education
- Patient Characteristic: High School Education, 
- Patient Characteristic: Less Than High School Education
- Patient Characteristic: Eighth Grade Education

```javascript {
        "_id": "5cf6b7c3b8484632a744be98",
        "dataElementCodes": [
          {
            "code": "473461003",
            "system": "2.16.840.1.113883.6.96"
          }
        ],
        "_type": "QDM::PatientCharacteristic",
        "qdmTitle": "Patient Characteristic",
        "hqmfOid": "2.16.840.1.113883.10.20.28.4.53",
        "qdmCategory": "patient_characteristic",
        "qdmVersion": "5.5",
        "authorDatetime": "1972-05-20T08:00:00.000Z",
        "description": "Patient Characteristic: High School Education",
        "codeListId": "2.16.840.1.113762.1.4.1111.149"
      },
```

---

###### Insurance Providers

Insurance providers are supplies as a String to the BonniePatient extendedData element.
Do we need to include.****

```javascript {
[
  {
    "author_datetime": null,
    "codes": {
      "SOP": [
        "349"
      ]
    },
    "description": null,
    "end_time": null,
    "financial_responsibility_type": {
      "code": "SELF",
      "codeSystem": "HL7 Relationship Code"
    },
    "health_record_field": null,
    "member_id": "1234567890",
    "mood_code": "EVN",
    "name": "Other",
    "negationInd": null,
    "negationReason": null,
    "oid": null,
    "payer": {
      "name": "Other"
    },
    "reason": null,
    "relationship": null,
    "specifics": null,
    "start_time": 1199145600,
    "status_code": null,
    "time": null,
    "type": "OT"
  }
]
     

```
~~~~