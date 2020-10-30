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
      <td><strong>* (list all that use)</strong></td>
      <td>MedicationRequest</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>Medication, Order active</td>
      <td>MedicationRequest.status</td>
      <td></td>
      <td>Depends on Implementation</td>
    </tr>
    <tr>
      <td>&nbsp;</td>
      <td>MedicationRequest.statusReason</td>
      <td>&nbsp;</td>
      <td>Not mapped</td>
    </tr>
    <tr>
      <td>Request context (order vs. recommended)</td>
      <td>MedicationRequest.intent</td>
      <td>&nbsp;</td>
      <td>Depends on Implementation</td>
    </tr>
    <tr>
      <td><strong>QDM Attributes</strong></td>
      <td>&nbsp;</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>code</td>
      <td>MedicationRequest.medication[x]</td>
      <td>RxNorm</td>
      <td>qdmDataElement.getDataElementCodes()</td>
    </tr>
    <tr>
      <td>id</td>
      <td>MedicationRequest.id</td>
      <td>&nbsp;</td>
      <td>qdmDataElement.get_id()</td>
    </tr>
    <tr>
      <td>dosage</td>
      <td>MedicationRequest.dosageInstruction.doseAndRate.dose[x]</td>
      <td>Range, quantity</td>
      <td>qdmDataElement.getDosage()</td>
    </tr>
    <tr>
      <td>supply</td>
      <td>MedicationRequest.dispenseRequest.quantity</td>
      <td>Amount to be dispensed in one fill</td>
      <td>qdmDataElement.getSupply() </td>
    </tr>
    <tr>
      <td>days supplied</td>
      <td>MedicationRequest.dispenseRequest.expectedSupplyDuration</td>
      <td>Duration</td>
      <td>qdmDataElement.getDaysSupplied()  </td>
    </tr>
    <tr>
      <td>frequency</td>
      <td>MedicationRequest.dosageInstruction.timing</td>
      <td>Timing schedule (e.g., every 8 hours)</td>
      <td>qdmDataElement.getFrequency()</td>
    </tr>
    <tr>
      <td>refills</td>
      <td>MedicationRequest.dispenseRequest.numberOfRepeatsAllowed</td>
      <td>Integer</td>
      <td>qdmDataElement.getRefills() </td>
    </tr>
    <tr>
      <td>route</td>
      <td>MedicationRequest.dosageInstruction.route</td>
      <td>&nbsp;</td>
      <td>qdmDataElement.getRoute()</td>
    </tr>
    <tr>
      <td>setting</td>
      <td>MedicationRequest.category</td>
      <td>Constrain category to: Inpatient, Outpatient, Community</td>
      <td>**No** data found in qdmDataElement.getSetting() </td>
    </tr>
    <tr>
      <td>reason</td>
      <td>MedicationRequest.reasonCode</td>
      <td>The reason for ordering or not ordering the medication</td>
      <td>**No** data found in qdmDataElement.getReason() </td>
    </tr>
    <tr>
      <td>relevant dateTime</td>
      <td>MedicationRequest.dosageInstruction.timing with <a href="http://hl7.org/fhir/R4/datatypes-definitions.html#Timing.event">Timing.event dateTime</td>
      <td>&nbsp;</td>
     <td> qdmDataElement.getRelevantDatetime()</td>
    </tr>
    <tr>
      <td>relevant Period</td>
      <td>MedicationRequest.dosageInstruction.timing with <a href="http://hl7.org/fhir/R4/datatypes-definitions.html#Timing.repeat.bounds_x_">Timing.repeat.bounds[x] Period</td>
      <td>The anticipated time from starting to stopping an ordered or dispensed medication can also be computed in an expression and derived from the duration attribute</td>
       <td>qdmDataElement.getRelevantPeriod() </td>
    </tr>
    <tr>
      <td>author dateTime</td>
      <td>MedicationRequest.authoredOn</td>
      <td>&nbsp;</td>
      <td>qdmDataElement.getAuthorDatetime()</td>
    </tr>
    <tr>
      <td>Negation Rationale</td>
      <td>See Below</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>Prescriber</td>
      <td>MedicationRequest.requester</td>
      <td>Note - MedicationRequest.performer indicates the performer expected to administer the medication</td>
        <td>**No** data found in qdmDataElement.getPrescriber() </td>
    </tr>
    <tr>
      <td>Requester</td>
      <td><a href="StructureDefinition-qicore-medicationrequest-definitions.html#MedicationRequest.requester">MedicationRequest.requester</a></td>
      <td>Note - MedicationRequest.performer indicates the performer expected to administer the medication</td>
       <td>**No** value in qdmDataElement for Requester </td>
    </tr>
  </tbody>
</table>