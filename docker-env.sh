#!/bin/sh
export MAT_DB_URL='jdbc:mysql://host.docker.internal:3306/mat?serverTimezone=UTC&max_allowed_packet=16777216'
export MAT_DB_USER=root
export MAT_DB_PASS=changeme

export HAPI_DB=hapi-r4-db
export HAPI_DB_URL='jdbc:mysql://hapi-fhir-mysql:3306/'${HAPI_DB}'?serverTimezone=UTC&max_allowed_packet=16777216&autoReconnect=true&allowPublicKeyRetrieval=true&useSSL=false'
export HAPI_DB_USER=admin
export HAPI_DB_PASS=changeme

export LOG_LEVEL=INFO
export MAT_API_KEY=DISABLED

export HAPI_FHIR_URL_PUBLIC='http://localhost:6060/fhir'
export HAPI_FHIR_URL='http://hapi-fhir-jpaserver:6060/fhir/'
export FHIR_SERVICES_URL='http://host.docker.internal:9080'
export MAPPING_SERVICES_URL='http://host.docker.internal:9090'
export CQL_CONVERSION_URL='http://host.docker.internal:7070'
export VSAC_TICKET_URL_BASE='https://utslogin.nlm.nih.gov/cas/v1'
export VSAC_URL_BASE='https://vsac.nlm.nih.gov'
export HEALTH_CHECK_URL='http://host.docker.internal:9080'

export GOOGLE_MAT_ATTRIBUTES_URL='https://spreadsheets.google.com/feeds/list/1uFtfWIHndogk-aoqROpuFSwHfcEKjaELw2Ph567uiNo/o7a94yh/public/values?alt=json'
export GOOGLE_QDM_QI_CORE_MAPPING_URL='https://spreadsheets.google.com/feeds/list/1uFtfWIHndogk-aoqROpuFSwHfcEKjaELw2Ph567uiNo/ohe8k21/public/values?alt=json'
export GOOGLE_DATA_TYPES_URL='https://spreadsheets.google.com/feeds/list/1uFtfWIHndogk-aoqROpuFSwHfcEKjaELw2Ph567uiNo/ox8mj1u/public/values?alt=json'
export GOOGLE_REQUIRED_MEASURE_FIELDS_URL='https://spreadsheets.google.com/feeds/list/1uFtfWIHndogk-aoqROpuFSwHfcEKjaELw2Ph567uiNo/opl7qh/public/values?alt=json'
export GOOGLE_RESOURCE_DEFINITION_URL='https://spreadsheets.google.com/feeds/list/1uFtfWIHndogk-aoqROpuFSwHfcEKjaELw2Ph567uiNo/o1h1xy0/public/values?alt=json'

export GOOGLE_CONVERSION_DATA_TYPES_URL='https://spreadsheets.google.com/feeds/list/1lV1N4O7xmSxjRH6ghuCj3mYcntPsTz8qBcpQ6DO7Se4/1/public/values?alt=json'
export GOOGLE_ATTRIBUTES_URL='https://spreadsheets.google.com/feeds/list/1lV1N4O7xmSxjRH6ghuCj3mYcntPsTz8qBcpQ6DO7Se4/2/public/values?alt=json'
export GOOGLE_FHIR_LIGHTBOX_DATATYPE_ATTRIBUTE_ASSOCIATION_URL='https://spreadsheets.google.com/feeds/list/1ecwDn7YfmqYhXJqtAFwayyAXFQ4EZ2sEeyKT5TBtttc/3/public/values?alt=json'
export GOOGLE_FHIR_LIGHTBOX_DATATYPE_FOR_FUNCTION_ARGS_URL='https://spreadsheets.google.com/feeds/list/1ecwDn7YfmqYhXJqtAFwayyAXFQ4EZ2sEeyKT5TBtttc/2/public/values?alt=json'
export GOOGLE_POPULATION_BASIS_VALID_VALUES_URL='https://spreadsheets.google.com/feeds/list/1ecwDn7YfmqYhXJqtAFwayyAXFQ4EZ2sEeyKT5TBtttc/4/public/values?alt=json'
export GOOGLE_CODE_SYSTEM_ENTRY_URL='https://spreadsheets.google.com/feeds/list/1_heoWR09X3UnyEvyyHLzVXT_D1Hhdt3OPztpgr1hW_k/od6/public/values?alt=json'

export CODESYSTEM_SHEET_URL="https://spreadsheets.google.com/feeds/list/1_heoWR09X3UnyEvyyHLzVXT_D1Hhdt3OPztpgr1hW_k/od6/public/values?alt=json"

export MAT_FHIR_SWAGGER_URL='http://host.docker.internal:9080'
export MAPPING_SWAGGER_URL='http://host.docker.internal:9090'
export CQL_ELM_SWAGGER_URL='http://host.docker.internal:7070'
