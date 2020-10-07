MAT_API_KEY=LOCAL_KEY
#FHIR_SERVICES_URL='http://localhost:9080'
#FHIR_SERVICES_URL=http://internal-mat-dev-ecs-lb-1195232407.us-east-1.elb.amazonaws.com:9080
FHIR_SERVICES_URL=http://internal-mat-test-ecs-lb-1803224691.us-east-1.elb.amazonaws.com:9080
#FHIR_SERVICES_URL=http://internal-mat-prod-ecs-lb-1778389775.us-east-1.elb.amazonaws.com:9080
#FHIR_SERVICES_URL=https://matdev.semanticbits.com/mat-fhir-services

# FHIRHelpers-4-0-001 = 33b6e38e5e0543ada0e8942b91d42795
# AdultOutpatientEncounters-FHIR4-2-0-000 = 11a37cbfc8634b00a69a56d005f10fd7
# AdvancedIllnessandFrailtyExclusion-FHIR4-5-0-000 = 09b6df1d5cfb4ed787d63ffd8e4e32a8
# Hospice-FHIR4-2-0-000 = 35e8cdb0bb204ce1a39682093adbf5f1
# MATGlobalCommonFunctions-FHIR4-5-0-000 = 1b9d3281e08c4053a24a9f6b911d680e
# SupplementalDataElements-FHIR4-2-0-000 = 747a00aa82d340f29f257d848c125742
# TJCOverall-FHIR4-5-0-000 = 195c13832b09499ca1e1ccc4fb5fd77b
# VTEICU-FHIR4-4-0-000 = 544333b6ef7e47268672589073a33538

COMMON_LIBS=('33b6e38e5e0543ada0e8942b91d42795' '11a37cbfc8634b00a69a56d005f10fd7' '09b6df1d5cfb4ed787d63ffd8e4e32a8' '35e8cdb0bb204ce1a39682093adbf5f1' '1b9d3281e08c4053a24a9f6b911d680e' '747a00aa82d340f29f257d848c125742' '195c13832b09499ca1e1ccc4fb5fd77b' '544333b6ef7e47268672589073a33538')

echo "Deleting all libs"
curl -H 'MAT-API-KEY: '${MAT_API_KEY} --location --request DELETE $FHIR_SERVICES_URL'/library/deleteAll'

echo""
echo""
echo "Deleting all measures"
curl -H 'MAT-API-KEY: '${MAT_API_KEY} --location --request DELETE $FHIR_SERVICES_URL"/measure/deleteAll"

echo""
echo""
echo "Pushing all FHIR Global Common Libs..."
# shellcheck disable=SC2068
for l in ${COMMON_LIBS[@]}; do
  echo ""
  echo "Pushing $l"
  curl -H 'MAT-API-KEY: '${MAT_API_KEY} --location --request POST $FHIR_SERVICES_URL"/library/pushStandAloneLibrary?id=$l"
done
echo""
echo "Complete"