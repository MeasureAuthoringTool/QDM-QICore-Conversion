
[QDM::AdverseEvent, QDM::AllergyIntolerance, QDM::AssessmentOrder, QDM::AssessmentPerformed, QDM::AssessmentRecommended,
 QDM::CareGoal, QDM::CommunicationPerformed, QDM::DeviceApplied, QDM::DeviceOrder, QDM::Diagnosis, QDM::DiagnosticStudyOrder,
 QDM::DiagnosticStudyPerformed, QDM::EncounterOrder, QDM::EncounterPerformed, QDM::FamilyHistory, QDM::ImmunizationAdministered,
  QDM::ImmunizationOrder, QDM::InterventionOrder, QDM::InterventionPerformed, QDM::InterventionRecommended, QDM::LaboratoryTestOrder, 
  QDM::LaboratoryTestPerformed, QDM::MedicationActive, QDM::MedicationAdministered, QDM::MedicationDischarge, QDM::MedicationDispensed, 
  QDM::MedicationOrder, QDM::Participation,
   QDM::PhysicalExamPerformed, QDM::ProcedureOrder, QDM::ProcedurePerformed, QDM::SubstanceAdministered, 
   QDM::SubstanceOrder, QDM::SubstanceRecommended, QDM::Symptom]

 [QDM::PatientCharacteristic, QDM::PatientCharacteristicBirthdate, QDM::PatientCharacteristicEthnicity,
   QDM::PatientCharacteristicExpired, QDM::PatientCharacteristicPayer, QDM::PatientCharacteristicRace, QDM::PatientCharacteristicSex, ]

---
cqm_patients id: 5d654a171c76ba7ea32ea080 has a bad entry in dataElements array concerning components[2]:

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

cqm_patients id: 5d654a171c76ba7ea32ea080 has a bad entry in dataElements[1] array concerning result it is mapped as int32 69.

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

cqm_patients id: 5d654ae61c76ba7ea32ed30c has a bad entry in dataElements[1] array concerning targetOutcome it is mapped
 like a QdmQuantity.
 
```javascript
   "targetOutcome": {
          "unit": "",
          "value": 2
        },
```

While the vast majority of the other ones have it mapped as a code E.G. id: 5d6547af1c76ba7ea32e1d95 dataElements[0]:

```javascript
 "targetOutcome" : {
                    "code" : "82325-2",
                    "version" : null,
                    "system" : "2.16.840.1.113883.6.1",
                    "display" : "How severe is your knee stiffness after first wakening in the morning during the last week [KOOS]"
                },
```

---

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

Where patient id: 5d65454c1c76ba7ea62d9874 has it mapped as 2d array seen once where it went 7 layers deep for no apparent 
reason:

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

Where patient id: 5d65454e1c76ba7ea32d98f2 it has the OBSERV properties in expectedValues maped as int and int array

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