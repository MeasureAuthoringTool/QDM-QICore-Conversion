#!/bin/sh
export VSAC_USER=FIXME
export VSAC_PASS=FIXME

export MAT_DB_URL='jdbc:mysql://host.docker.internal:3306/MAT?serverTimezone=UTC&max_allowed_packet=16777216'
export MAT_DB_USER=root
export MAT_DB_PASS=changeme

export HAPI_DB=hapi_r4
export HAPI_DB_URL='jdbc:mysql://hapi-fhir-mysql:3306/'${HAPI_DB}'?serverTimezone=UTC&max_allowed_packet=16777216'
export HAPI_DB_USER=root
export HAPI_DB_PASS=changeme

export MONGO_DB_URL='mongodb://host.docker.internal:27018'

export HAPI_FHIR_URL_PUBLIC='http://host-docker-internal:6060/hapi-fhir-jpaserver/'
export HAPI_FHIR_URL='http://hapi-fhir-jpaserver:6060/hapi-fhir-jpaserver/fhir/'
export FHIR_SERVICES_URL='http://host.docker.internal:9080'
export QDM_QICORE_URL='http://host.docker.internal:9090'
export CQL_CONVERSION_URL='http://host.docker.internal:7070'
export VSAC_VASE_URL='https://vsac.nlm.nih.gov'
export VSAC_SERVICE_URL='http://umlsks.nlm.nih.gov'
export HEALTH_CHECK_URL='http://host.docker.internal:9080'

export QDM_SPREADSHEET='https://spreadsheets.google.com/feeds/list/1uFtfWIHndogk-aoqROpuFSwHfcEKjaELw2Ph567uiNo/o7a94yh/public/values?alt=json'

export MAT_LOG_LEVEL='WARN'
