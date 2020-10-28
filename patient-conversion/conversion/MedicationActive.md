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
      <td><strong>Medication, Active</strong></td>
      <td>MedicationRequest</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>&nbsp;</td>
      <td>MedicationRequest.status</td>
      <td>Constrain to “active”</td>
    </tr>
    <tr>
      <td>&nbsp;</td>
      <td>MedicationRequest.intent</td>
      <td>Constrain to “order”</td>
    </tr>
    <tr>
      <td>&nbsp;</td>
      <td>MedicationRequest.category</td>
      <td>inpatient, outpatient, community, patient-specified</td>
    </tr>
    <tr>
      <td><strong>QDM Attribute</strong></td>
      <td>&nbsp;</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>code</td>
      <td>MedicationRequest.medication[x]</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>id</td>
      <td>MedicationRequest.id</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>dosage</td>
      <td>MedicationRequest.dosageInstruction.doseAndRate.dose[x]</td>
      <td>Amount of medication per dose</td>
    </tr>
    <tr>
      <td>frequency</td>
      <td>MedicationRequest.dosageInstruction.timing</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>route</td>
      <td>MedicationRequest.dosageInstruction.route</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>&nbsp;</td>
      <td>MedicationRequest.reasonCode</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>relevant dateTime</td>
      <td>MedicationRequest.dosageInstruction.timing with Timing.event dateTime</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>relevant period</td>
      <td>MedicationRequest.dosageInstruction.timing with Timing.repeat.bounds[x] Period</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>&nbsp;</td>
      <td>MedicationRequest.authoredOn</td>
      <td>Author dateTime not referenced in QDM</td>
    </tr>
    <tr>
      <td>recorder</td>
      <td>MedicationRequest.requester</td>
      <td>To address all medications on a medication list, use MedicationRequest with status = active; intent = order; and requester = organization (for prescribed medications for which an order exists), practitioner (for medications entered by clinicians but not ordered), and patient or RelatedPerson (for patient/related person reported)</td>
    </tr>
  </tbody>
</table>