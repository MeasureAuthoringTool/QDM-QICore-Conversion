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
      <td><a href="StructureDefinition-qicore-medicationdispense.html">MedicationDispense</a></td>
      <td>&nbsp;</td>
      <td>QDM::MedicationDispensed</td>
    </tr>
    <tr>
      <td>&nbsp;</td>
      <td><a href="StructureDefinition-qicore-medicationdispense-definitions.html#MedicationDispense.status">MedicationDispense.status</a></td>
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
      <td><a href="StructureDefinition-qicore-medicationdispense-definitions.html#MedicationDispense.medication[x]">MedicationDispense.medication[x]</a></td>
      <td>&nbsp;</td>
      <td>qdmDataElement.getDataElementCodes()</td>
    </tr>
    <tr>
      <td>id</td>
      <td><a href="StructureDefinition-qicore-medicationdispense-definitions.html#MedicationDispense.id">MedicationDispense.id</a></td>
      <td>&nbsp;</td>
      <td>qdmDataElement.get_id() </td>
    </tr>
    <tr>
      <td>dosage</td>
      <td><a href="StructureDefinition-qicore-medicationdispense-definitions.html#MedicationDispense.dosageInstruction">MedicationDispense.dosageInstruction</a></td>
      <td>ordered, calculated</td>
      <td>qdmDataElement.getDosage() is a QdmQuantity convertred to  dosageDoseAndRateComponent.setDose(convertQuantity(qdmDataElement.getDosage())) </td>
    </tr>
    <tr>
      <td>supply</td>
      <td><a href="StructureDefinition-qicore-medicationdispense-definitions.html#MedicationDispense.quantity">MedicationDispense.quantity</a></td>
      <td>&nbsp;</td>
      <td>No Data in for element for qdmDataElement.getSupply()</td>
    </tr>
    <tr>
      <td>days supplied</td>
      <td><a href="StructureDefinition-qicore-medicationdispense-definitions.html#MedicationDispense.daysSupply">MedicationDispense.daysSupply</a></td>
      <td>&nbsp;</td>
      <td>qdmDataElement.getDaysSupplied()</td>
    </tr>
    <tr>
      <td>frequency</td>
      <td><a href="StructureDefinition-qicore-medicationdispense-definitions.html#MedicationDispense.dosageInstruction.timing">MedicationDispense.dosageInstruction.timing</a></td>
      <td>See dosageInstruction</td>
      <td>qdmDataElement.getFrequency()</td>
    </tr>
    <tr>
      <td>refills</td>
      <td><a href="StructureDefinition-qicore-medicationdispense-definitions.html#MedicationDispense.authorizingPrescription">MedicationDispense.authorizingPrescription</a></td>
      <td>Reference authorizing prescription (<a href="StructureDefinition-qicore-medicationrequest.html">MedicationRequest</a>) which contains Medication.Request.dispsenseRequest.numberOfRepeatsAllowed</td>
      <td> (int) qdmDataElement.getRefills() No data for element </td>
    </tr>
    <tr>
      <td>&nbsp;</td>
      <td><a href="StructureDefinition-qicore-medicationrequest-definitions.html#MedicationRequest.dispenseRequest.numberOfRepeatsAllowed">MedicationRequest.dispenseRequest.numberOfRepeatsAllowed</a></td>
      <td>Timing schedule (e.g., every 8 hours). <a href="StructureDefinition-qicore-medicationdispense-definitions.html#MedicationDispense.authorizingPrescription">MedicationDispense.authorizingPrescription</a> provides reference to the applicable <a href="StructureDefinition-qicore-medicationrequest.html">MedicationRequest</a> for this information.</td>
    </tr>
    <tr>
      <td>route</td>
      <td><a href="StructureDefinition-qicore-medicationdispense-definitions.html#MedicationDispense.dosageInstruction.route">MedicationDispense.dosageInstruction.route</a></td>
      <td>See dosageInstruction</td>
      <td>qdmDataElement.getRoute()</td>
    </tr>
    <tr>
      <td>setting</td>
      <td><a href="StructureDefinition-qicore-medicationdispense-definitions.html#MedicationDispense.category">MedicationDispense.category</a></td>
      <td>Inpatient, Outpatient, Community, Discharge</td>
       <td>No Data in for element for qdmDataElement.getSetting() </td>
    </tr>
    <tr>
      <td>reason</td>
      <td><a href="StructureDefinition-qicore-medicationdispense-definitions.html#MedicationDispense.statusReason[x]">MedicationDispense.statusReason[x]</a></td>
      <td>The reason for ordering or not ordering the medication</td>
    </tr>
    <tr>
      <td>relevant dateTime</td>
      <td><a href="StructureDefinition-qicore-medicationdispense-definitions.html#MedicationDispense.whenHandedOver">MedicationDispense.whenHandedOver</a></td>
      <td>When provided to patient or representative</td>
      <td>qdmDataElement.getRelevantDatetime()</td>
    </tr>
    <tr>
      <td>relevant Period</td>
      <td><a href="StructureDefinition-qicore-medicationrequest-definitions.html#MedicationRequest.dosageInstruction.timing">MedicationRequest.dosageInstruction.timing</a> with <a href="http://hl7.org/fhir/R4/datatypes-definitions.html#Timing.repeat.bounds_x_">Timing.repeat.bounds[x]</a> Period</td>
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
      <td><a href="StructureDefinition-qicore-medicationdispense-definitions.html#MedicationDispense.authorizingPrescription">MedicationDispense.authorizingPrescription</a></td>
      <td>Reference authorizing prescription (MedicationRequest) which contains Medication.Request.requester</td>
      <td>No Data in for element for qdmDataElement.getPrescriber()  </td>
    </tr>
    <tr>
      <td>&nbsp;</td>
      <td><a href="StructureDefinition-qicore-medicationrequest-definitions.html#MedicationRequest.requester">MedicationRequest.requester</a></td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>Dispenser</td>
      <td><a href="StructureDefinition-qicore-medicationdispense-definitions.html#MedicationDispense.performer.actor">MedicationDispense.performer.actor</a></td>
      <td>&nbsp;</td>
       <td>No data for dmDataElement.getDispenser() </td>
    </tr>
  </tbody>
</table>

----

medicationDispense.setSubject(createReference(fhirPatient));
