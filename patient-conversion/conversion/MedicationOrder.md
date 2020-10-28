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
      <td><strong>Medication, Order</strong></td>
      <td>MedicationRequest</td>
      <td>&nbsp;</td>
      <td>QDM::MedicationOrder</td>
    </tr>
    <tr>
      <td>Medication, Order active</td>
      <td>MedicationRequest.status</td>
      <td>Constrain to active, completed, on-hold</td>
    </tr>
    <tr>
      <td>&nbsp;</td>
      <td>MedicationRequest.statusReason</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>Request context (order vs. recommended)</td>
      <td>MedicationRequest.intent</td>
      <td>Constrain to “order” - note that QDM does not include Medication, Recommended - should that concept be desired, use MedicationRequest.intent constrained to “plan”</td>
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
    </tr>
    <tr>
      <td>id</td>
      <td>MedicationRequest.id</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>dosage</td>
      <td>MedicationRequest.dosageInstruction.doseAndRate.dose[x]</td>
      <td>Range, quantity</td>
    </tr>
    <tr>
      <td>supply</td>
      <td>MedicationRequest.dispenseRequest.quantity</td>
      <td>Amount to be dispensed in one fill</td>
    </tr>
    <tr>
      <td>days supplied</td>
      <td>MedicationRequest.dispenseRequest.expectedSupplyDuration</td>
      <td>Duration</td>
    </tr>
    <tr>
      <td>frequency</td>
      <td>MedicationRequest.dosageInstruction.timing</td>
      <td>Timing schedule (e.g., every 8 hours)</td>
    </tr>
    <tr>
      <td>refills</td>
      <td>MedicationRequest.dispenseRequest.numberOfRepeatsAllowed</td>
      <td>Integer</td>
    </tr>
    <tr>
      <td>route</td>
      <td>MedicationRequest.dosageInstruction.route</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>setting</td>
      <td>MedicationRequest.category</td>
      <td>Constrain category to: Inpatient, Outpatient, Community</td>
    </tr>
    <tr>
      <td>reason</td>
      <td>MedicationRequest.reasonCode</td>
      <td>The reason for ordering or not ordering the medication</td>
    </tr>
    <tr>
      <td>relevant dateTime</td>
      <td>MedicationRequest.dosageInstruction.timing with Timing.event dateTime</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>relevant Period</td>
      <td>MedicationRequest.dosageInstruction.timing< with Timing.repeat.bounds[x] Period</td>
      <td>The anticipated time from starting to stopping an ordered or dispensed medication can also be computed in an expression and derived from the duration attribute</td>
    </tr>
    <tr>
      <td>author dateTime</td>
      <td>MedicationRequest.authoredOn/td>
      <td>&nbsp;</td>
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
    </tr>
  </tbody>
</table>