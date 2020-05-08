#!/bin/sh
export VSAC_USER=FIXME
export VSAC_PASS=FIXME

export MAT_DB_URL='jdbc:mysql://host.docker.internal:3306/mat?serverTimezone=UTC&max_allowed_packet=16777216'
export MAT_DB_USER=root
export MAT_DB_PASS=changeme

export HAPI_DB=hapi
export HAPI_DB_URL='jdbc:mysql://hapi-fhir-mysql:3306/'${HAPI_DB}'?serverTimezone=UTC&max_allowed_packet=16777216&createDatabaseIfNotExist=true&autoReconnect=true&allowPublicKeyRetrieval=true&useSSL=false'
export HAPI_DB_USER=admin	
export HAPI_DB_PASS=admin

export MONGO_DB_URL='mongodb://host.docker.internal:27018'

export HAPI_FHIR_URL_PUBLIC='http://localhost:6060/hapi-fhir-jpaserver/fhir'
export HAPI_FHIR_URL='http://hapi-fhir-jpaserver:6060/hapi-fhir-jpaserver/fhir/'
export FHIR_SERVICES_URL='http://host.docker.internal:9080'
export QDM_QICORE_URL='http://host.docker.internal:9090'
export CQL_CONVERSION_URL='http://host.docker.internal:7070'
export VSAC_VASE_URL='https://vsac.nlm.nih.gov'
export VSAC_SERVICE_URL='http://umlsks.nlm.nih.gov'
export HEALTH_CHECK_URL='http://host.docker.internal:9080'

export GOOGLE_MAT_ATTRIBUTES_URL='https://spreadsheets.google.com/feeds/list/1uFtfWIHndogk-aoqROpuFSwHfcEKjaELw2Ph567uiNo/o7a94yh/public/values?alt=json'
export GOOGLE_QDM_QI_CORE_MAPPING_URL='https://spreadsheets.google.com/feeds/list/1uFtfWIHndogk-aoqROpuFSwHfcEKjaELw2Ph567uiNo/ohe8k21/public/values?alt=json'
export GOOGLE_DATA_TYPES_URL='https://spreadsheets.google.com/feeds/list/1uFtfWIHndogk-aoqROpuFSwHfcEKjaELw2Ph567uiNo/ox8mj1u/public/values?alt=json'
export GOOGLE_REQJUIRED_MEASURE_FIELDS_URL='https://spreadsheets.google.com/feeds/list/1uFtfWIHndogk-aoqROpuFSwHfcEKjaELw2Ph567uiNo/opl7qh/public/values?alt=json'
export GOOGLE_RESOURCE_DEFINITION_URL='https://spreadsheets.google.com/feeds/list/1uFtfWIHndogk-aoqROpuFSwHfcEKjaELw2Ph567uiNo/o1h1xy0/public/values?alt=json'

export CODESYSTEM_SHEET_URL="https://spreadsheets.google.com/feeds/list/15YvJbG3LsyqqN4ZIgRd88fgScbE95eK6fUilwHRw0Z0/od6/public/values?alt=json"

export MAT_LOG_LEVEL='INFO'
