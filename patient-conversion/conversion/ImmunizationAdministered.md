<table class="grid">
  <thead>
    <tr>
      <th><strong>QDM Context</strong></th>
      <th><strong>US Core R4</strong></th>
      <th><strong>Comments</strong></th>
        <th><strong>Conversion</strong></th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><strong>Immunization, Administered</strong></td>
      <td>Immunization</td>
      <td>&nbsp;</td>
      <td>QDM::ImmunizationAdministered</td>
    </tr>
    <tr>
      <td>&nbsp;</td>
      <td>Immunization.status<</td>
      <td>Constrain to Completed, entered-in-error, not-done</td>
    </tr>
    <tr>
      <td><strong>QDM Attributes</strong></td>
      <td>&nbsp;</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>code</td>
      <td>Immunization.vaccineCode</td>
      <td>&nbsp;</td>
        <td>qdmDataElement.getDataElementCodes())</td>
       
    </tr>
    <tr>
      <td>id</td>
      <td>Immunization.id</td>
      <td>&nbsp;</td>
       <td>qdmDataElement.get_id()</td>
    </tr>
    <tr>
      <td>Dosage</td>
      <td>Immunization.doseQuantity</td>
      <td>&nbsp;</td>
       <td>qdmDataElement.getDosage()</td>
    </tr>
    <tr>
      <td>Negation Rationale</td>
      <td>See Below</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>Route</td>
      <td>Immunization.route</td>
      <td>&nbsp;</td>
      <td>dmDataElement.getRoute()</td>
    </tr>
    <tr>
      <td>Reason</td>
      <td>Immunization.reasonCode</td>
      <td>&nbsp;</td>
      <td>**No** Data found in qdmDataElement.getReason()</td>
    </tr>
    <tr>
      <td>Relevant dateTime</td>
      <td>Immunization.occurrence[x]</td>
      <td>&nbsp;</td>
      <td>qdmDataElement.getRelevantDatetime()</td>
    </tr>
    <tr>
      <td>author dateTime</td>
      <td>Immunization.recorded</td>
      <td>&nbsp;</td>
      <td>qdmDataElement.getAuthorDatetime()</td>
    </tr>
    <tr>
      <td>Performer</td>
      <td>Immunization.performer.actor</td>
      <td>&nbsp;</td>
      <td>**No** Data found in qdmDataElement.getPerformer() </td>
    </tr>
  </tbody>
</table>