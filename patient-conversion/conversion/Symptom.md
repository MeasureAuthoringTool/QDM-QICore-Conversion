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
      <td><strong>Symptom</strong></td>
      <td><a href="StructureDefinition-qicore-observation.html">Observation</a></td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>&nbsp;</td>
      <td><a href="StructureDefinition-qicore-observation-definitions.html#Observation.status">Observation.status</a></td>
      <td>restrict to preliminary, final, amended, corrected</td>
      <td>Observation.ObservationStatus.UNKNOWN</td>
    </tr>
    <tr>
      <td>&nbsp;</td>
      <td><a href="StructureDefinition-qicore-observation-definitions.html#Observation.category">Observation.category</a></td>
      <td>add symptom concept</td>
    </tr>
    <tr>
      <td><strong>QDM Attributes</strong></td>
      <td>&nbsp;</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>Code</td>
      <td><a href="StructureDefinition-qicore-observation-definitions.html#Observation.value[x]">Observation.value[x]</a></td>
      <td>Use codable concept</td>
      <td>qdmDataElement.getDataElementCodes()</td>
    </tr>
    <tr>
      <td>id</td>
      <td><a href="StructureDefinition-qicore-observation-definitions.html#Observation.id">Observation.id</a></td>
      <td>&nbsp;</td>
      <td>qdmDataElement.get_id()</td>
    </tr>
    <tr>
      <td>severity</td>
      <td><a href="StructureDefinition-qicore-observation-definitions.html#Observation.interpretation">Observation.interpretation</a></td>
      <td>Definition suggests high, low, normal - perhaps consider severe, moderate, mild.</td>
      <td>No data for qdmDataElement.getSeverity()</td>
    </tr>
    <tr>
      <td>prevalence period</td>
      <td><a href="StructureDefinition-qicore-observation-definitions.html#Observation.effective[x]">Observation.effective[x]</a></td>
      <td>dateTime, period, timing, instant</td>
      <td>qdmDataElement.getPrevalencePeriod()</td>
    </tr>
    <tr>
      <td>recorder</td>
      <td><a href="StructureDefinition-qicore-observation-definitions.html#Observation.performer">Observation.performer</a></td>
      <td>&nbsp;</td>
    </tr>
  </tbody>
</table>