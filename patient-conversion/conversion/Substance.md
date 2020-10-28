<table class="grid">
  <thead>
    <tr>
      <th><strong>QDM Context</strong></th>
      <th><strong>FHIR R4</strong></th>
      <th><strong>Comments</strong></th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><strong>Substance, Order/Recommended - For Diet Orders</strong></td>
      <td>NutritionOrder</td>
      <td>Limited to orders for diets or diets with supplements</td>
    </tr>
    <tr>
      <td>Substance Order/Recommended Activity</td>
      <td>NutritionOrder.status</td>
      <td>Constrain to Active, on-hold, Completed</td>
    </tr>
    <tr>
      <td>Substance, Order</td>
      <td>NutritionOrder.intent</td>
      <td>Constrain to “order” and child concepts</td>
    </tr>
    <tr>
      <td>Substance, Recommended</td>
      <td>NutritionOrder.intent</td>
      <td>Constrain to “plan”</td>
    </tr>
    <tr>
      <td><strong>QDM Attributes</strong></td>
      <td>&nbsp;</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>ORAL DIET</td>
      <td>NutritionOrder.oralDiet</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>code (to represent a diet order)</td>
      <td>NutritionOrder.oralDiet.type</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>&nbsp;</td>
      <td>NutrientOrder.oralDiet.nutrient.modifier</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>id</td>
      <td>NutritionOrder.id</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>dosage</td>
      <td>N/A</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>frequency</td>
      <td>NutritionOrder.Diet.schedule</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>negation rationale</td>
      <td>N/A</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>author dateTime</td>
      <td>NutritionOrder.dateTime</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>relevant period</td>
      <td>N/A</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>reason</td>
      <td>N/A</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>supply</td>
      <td>N/A</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>refills</td>
      <td>N/A</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>route</td>
      <td>NutritionOrder.oralDiet</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>Requester</td>
      <td>NutritionOrder.orderer</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>ENTERAL FORMULA</td>
      <td>NutritionOrder.enteralFormula</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>code (to represent a diet order)</td>
      <td>NutritionOrder.enteralFormula.baseFormulaType</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>Additive to diet order</td>
      <td>NutritionOrder.enteralFormula.additiveType</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>id</td>
      <td>N/A</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>dosage</td>
      <td>NutritionOrder.enterealFormula.administration.quantity</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>frequency</td>
      <td>NutritionOrder.enteralFormula.administration.rate[x]</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>negation rationale</td>
      <td>N/A</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>author dateTime</td>
      <td>NutritionOrder.dateTime</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>relevant period</td>
      <td>NutritionOrder.enteralFormula.administration.schedule</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>reason</td>
      <td>N/A</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>supply</td>
      <td>N/A</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>refills</td>
      <td>NutritionOrder.enteralFormula</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>route</td>
      <td>NutritionOrder.enteralFormula.routeofAdministration</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>Requester</td>
      <td>NutritionOrder.orderer</td>
      <td>&nbsp;</td>
    </tr>
  </tbody>
</table>