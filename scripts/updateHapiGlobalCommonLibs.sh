#FHIR_SERVICES_URL='http://localhost:9080'
FHIR_SERVICES_URL=http://internal-mat-dev-ecs-lb-1195232407.us-east-1.elb.amazonaws.com:9080/
#FHIR_SERVICES_URL=https://matdev.semanticbits.com/mat-fhir-services
COMMON_LIBS=('FHIRHelpers-4-0-001' 'MATGlobalCommonFunctions-FHIR4-5-0-000' 'SupplementalDataElements-FHIR4-2-0-000' 'AdultOutpatientEncounters-FHIR4-2-0-000' 'AdvancedIllnessandFrailtyExclusion-FHIR4-5-0-000' 'AdvancedIllnessandFrailtyExclusion-FHIR4-5-0-000' 'Hospice-FHIR4-2-0-000' 'TJCOverall-FHIR4-5-0-000' 'VTEICU-FHIR4-4-0-000')

echo "Deleting all libs"
curl --location --request DELETE $FHIR_SERVICES_URL'/library/deleteAll'

echo""
echo""
echo "Deleting all measures"
curl --location --request DELETE $FHIR_SERVICES_URL"/measure/deleteAll"

echo""
echo""
echo "Pushing all FHIR Global Common Libs..."
# shellcheck disable=SC2068
for l in ${COMMON_LIBS[@]}; do
  echo ""
  echo "Pushing $l"
  curl --location --request POST $FHIR_SERVICES_URL"/library/pushStandAloneLibrary?id=$l"
done
echo""
echo "Complete"





