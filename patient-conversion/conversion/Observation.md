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
      <td><strong>Assessment, Performed: General Use Case</strong></td>
      <td><a href="StructureDefinition-qicore-observation.html">Observation</a></td>
      <td>&nbsp;</td>
      <td>QDM:AssessmentPerformed</td>
    </tr>
    <tr>
      <td>&nbsp;</td>
      <td><a href="StructureDefinition-qicore-observation-definitions.html#Observation.category">Observation.category</a></td>
      <td>Since Assessment is a broad concept, the measure developer will need to select the appropriate category.</td>
    </tr>
    <tr>
      <td>&nbsp;</td>
      <td><a href="StructureDefinition-qicore-observation-definitions.html#Observation.status">Observation.status</a></td>
      <td>Constrain status to -&nbsp; final, amended, corrected</td>
      <td>Observation.ObservationStatus.UNKNOWN. If Negation Rational is not null, then status is set to Observation.ObservationStatus.FINAL</td>
    </tr>
    <tr>
      <td><strong>QDM Attributes</strong></td>
      <td>&nbsp;</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>code</td>
      <td><a href="StructureDefinition-qicore-observation-definitions.html#Observation.code">Observation.code</a></td>
      <td>&nbsp;</td>
      <td>qdmDataElement.getDataElementCodes()</td>
    </tr>
    <tr>
      <td>id</td>
      <td><a href="StructureDefinition-qicore-observation-definitions.html#Observation.id">Observation.id</a></td>
      <td>&nbsp;</td>
      <td>qdmDataElement.get_id()</td>
    </tr>
    <tr>
      <td>method</td>
      <td><a href="StructureDefinition-qicore-observation-definitions.html#Observation.method">Observation.method</a></td>
      <td>&nbsp;</td>
      <td>No data for qdmDataElement.getMethod()</td>
    </tr>
    <tr>
      <td>relatedTo</td>
      <td><a href="StructureDefinition-qicore-observation-definitions.html#Observation.basedOn">Observation.basedOn</a></td>
      <td>&nbsp;</td>
      <td>No data for qdmDataElement.getRelatedTo()</td>
    </tr>
    <tr>
      <td>&nbsp;</td>
      <td><a href="StructureDefinition-qicore-observation-definitions.html#Observation.partOf">Observation.partOf</a></td>
      <td>A larger event of which this particular Observation is a component or step. For example, an observation as part of a procedure.</td>
    </tr>
    <tr>
      <td>&nbsp;</td>
      <td><a href="StructureDefinition-qicore-observation-definitions.html#Observation.derivedFrom">Observation.derivedFrom</a></td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>Negation Rationale</td>
      <td>See Below</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>reason</td>
      <td><a href="StructureDefinition-qicore-observation-definitions.html#Observation.basedOn">Observation.basedOn</a></td>
      <td>The observation fulfills a plan, proposal or order - trace for authorization. Not a perfect&nbsp; fit for the intent in QDM (e.g., observation “reason” = a diagnosis)&nbsp; Is an extension needed?</td>
      <td>No data for qdmDataElement.getReason()</td>
    </tr>
    <tr>
      <td>result</td>
      <td><a href="StructureDefinition-qicore-observation-definitions.html#Observation.value[x]">Observation.value[x]</a></td>
      <td>&nbsp;</td>
      <td>qdmDataElement.getResult()</td>
    </tr>
    <tr>
      <td>&nbsp;</td>
      <td><a href="StructureDefinition-qicore-observation-definitions.html#Observation.interpretation">Observation.interpretation</a></td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>Relevant dateTime</td>
      <td><a href="StructureDefinition-qicore-observation-definitions.html#Observation.effective[x]">Observation.effective[x] dateTime</a></td>
      <td>&nbsp;</td>
      <td>No data for qdmDataElement.getRelevantDateTime()</td>
    </tr>
    <tr>
      <td>Relevant Period</td>
      <td><a href="StructureDefinition-qicore-observation-definitions.html#Observation.effective[x]">Observation.effective[x] Period</a></td>
      <td>&nbsp;</td>
      <td>qdmDataElement.getRelevantPeriod()</td>
    </tr>
    <tr>
      <td>Author dateTime</td>
      <td><a href="StructureDefinition-qicore-observation-definitions.html#Observation.issued">Observation.issued</a></td>
      <td>&nbsp;</td>
      <td>qdmDataElement.getAuthorDatetime() OR qdmDataElement.getResultDatetime()</td>
    </tr>
    <tr>
      <td>Component</td>
      <td><a href="StructureDefinition-qicore-observation-definitions.html#Observation.component">Observation.component</a></td>
      <td>&nbsp;</td>
      <td>List&lt;Observation.ObservationComponentComponent&gt;</td>
    </tr>
    <tr>
      <td>&nbsp;</td>
      <td><a href="StructureDefinition-qicore-observation-definitions.html#Observation.component.id">Observation.component.id</a></td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>Component code</td>
      <td><a href="StructureDefinition-qicore-observation-definitions.html#Observation.component.code">Observation.component.code</a></td>
      <td>&nbsp;</td>
      <td>A new codeSystem was created and converted into CodeableConcept</td>
    </tr>
    <tr>
      <td>Component result</td>
      <td><a href="StructureDefinition-qicore-observation-definitions.html#Observation.component.value[x]">Observation.component.value[x]</a></td>
      <td>&nbsp;</td>
      <td>qdmComponent.getResult()</td>
    </tr>
    <tr>
      <td>&nbsp;</td>
      <td><a href="StructureDefinition-qicore-observation-definitions.html#Observation.component.interpretation">Observation.component.interpretation</a></td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>&nbsp;</td>
      <td><a href="StructureDefinition-qicore-observation-definitions.html#Observation.component.dataAbsentReason">Observation.component.dataAbsentReason</a></td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>Performer</td>
      <td><a href="StructureDefinition-qicore-observation-definitions.html#Observation.performer">Observation.performer</a></td>
      <td>&nbsp;</td>
      <td>No data for qdmDataElement.getPerformer()</td>
    </tr>
  </tbody>
</table>

----
observation.setSubject(createReference(fhirPatient));