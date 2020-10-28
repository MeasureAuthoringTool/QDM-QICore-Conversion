<table class="grid">
  <thead>
    <tr>
      <th><strong>QDM Context</strong></th>
      <th><strong>QI-Core R4</strong></th>
      <th><strong>Comments</strong></th>
      <th><strong>Conversion</strong></th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><strong>Medication, Dispensed</strong></td>
      <td>MedicationDispense</td>
      <td>&nbsp;</td>
      <td>QDM::MedicationDispensed</td>
    </tr>
    <tr>
      <td>&nbsp;</td>
      <td>MedicationDispense.status</td>
      <td>Constrain MedicationDispense.status to active, completed, on-hold</td>
      <td>Set to "unknown" unless element has negation</td>
    </tr>
    <tr>
      <td><strong>QDM Attributes</strong></td>
      <td>&nbsp;</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>code</td>
      <td>MedicationDispense.medication[x]</td>
      <td>&nbsp;</td>
      <td>qdmDataElement.getDataElementCodes()</td>
    </tr>
    <tr>
      <td>id</td>
      <td>MedicationDispense.id</td>
      <td>&nbsp;</td>
      <td>qdmDataElement.get_id() </td>
    </tr>
    <tr>
      <td>dosage</td>
      <td>MedicationDispense.dosageInstruction</td>
      <td>ordered, calculated</td>
      <td>qdmDataElement.getDosage() is a QdmQuantity converted to  dosageDoseAndRateComponent.setDose(convertQuantity(qdmDataElement.getDosage())) </td>
    </tr>
    <tr>
      <td>supply</td>
      <td>MedicationDispense.quantity</td>
      <td>&nbsp;</td>
      <td>No Data in for element for qdmDataElement.getSupply()</td>
    </tr>
    <tr>
      <td>days supplied</td>
      <td>MedicationDispense.daysSupply</td>
      <td>&nbsp;</td>
      <td>qdmDataElement.getDaysSupplied()</td>
    </tr>
    <tr>
      <td>frequency</td>
      <td>MedicationDispense.dosageInstruction.timing</td>
      <td>See dosageInstruction</td>
      <td>qdmDataElement.getFrequency()</td>
    </tr>
    <tr>
      <td>refills</td>
      <td>MedicationDispense.authorizingPrescription</td>
      <td>Reference authorizing prescription MedicationRequest which contains Medication.Request.dispsenseRequest.numberOfRepeatsAllowed</td>
      <td> (int) qdmDataElement.getRefills() No data for element </td>
    </tr>
    <tr>
      <td>&nbsp;</td>
      <td>MedicationRequest.dispenseRequest.numberOfRepeatsAllowed</td>
      <td>Timing schedule (e.g., every 8 hours).MedicationDispense.authorizingPrescription provides reference to the applicable MedicationRequest for this information.</td>
    </tr>
    <tr>
      <td>route</td>
      <td>MedicationDispense.dosageInstruction.route</td>
      <td>See dosageInstruction</td>
      <td>qdmDataElement.getRoute()</td>
    </tr>
    <tr>
      <td>setting</td>
      <td>MedicationDispense.category</td>
      <td>Inpatient, Outpatient, Community, Discharge</td>
       <td>No Data in for element for qdmDataElement.getSetting() & and not present in qdm-modelinfo  </td>
    </tr>
    <tr>
      <td>reason</td>
      <td>MedicationDispense.statusReason[x]</td>
      <td>The reason for ordering or not ordering the medication</td>
    </tr>
    <tr>
      <td>relevant dateTime</td>
      <td>MedicationDispense.whenHandedOver</td>
      <td>When provided to patient or representative</td>
      <td>qdmDataElement.getRelevantDatetime()</td>
    </tr>
    <tr>
      <td>relevant Period</td>
      <td>MedicationRequest.dosageInstruction.timing with Timing.repeat.bounds[x] Period</td>
      <td>The anticipated time from starting to stopping an ordered or dispensed medication can also be computed in an expression and derived from the duration attribute</td>
    </tr>
    <tr>
      <td>author dateTime</td>
      <td>&nbsp;</td>
      <td>&nbsp;</td>
      <td>Ignored qdmDataElement.getAuthorDatetime()</td>
    </tr>
    <tr>
      <td>Negation Rationale</td>
      <td>See Below</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>Prescriber</td>
      <td>MedicationDispense.authorizingPrescription</td>
      <td>Reference authorizing prescription (MedicationRequest) which contains Medication.Request.requester</td>
      <td>No Data in for element for qdmDataElement.getPrescriber()  </td>
    </tr>
    <tr>
      <td>&nbsp;</td>
      <td>MedicationRequest.requester</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>Dispenser</td>
      <td>MedicationDispense.performer.actor</td>
      <td>&nbsp;</td>
       <td>No data for dmDataElement.getDispenser() </td>
    </tr>
  </tbody>
</table>

----

medicationDispense.setSubject(createReference(fhirPatient));
