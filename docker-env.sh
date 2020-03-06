#!/bin/sh
export VSAC_USER=FIX_ME!
export VSAC_PASS=FIX_ME!

export MAT_DB_URL='jdbc:mysql://host.docker.internal:3306/MAT?serverTimezone=UTC&max_allowed_packet=16777216'
export MAT_DB_USER=FIX_ME!
export MAT_DB_PASS=FIX_ME!

export HAPI_DB=hapi
export HAPI_DB_URL='jdbc:mysql://host.docker.internal:3306/'${HAPI_DB}'?serverTimezone=UTC&max_allowed_packet=16777216'
export HAPI_DB_USER=FIX_ME!
export HAPI_DB_PASS=FIX_ME!

export MONGO_DB_URL='mongodb://host.docker.internal'

export HAPI_FHIR_URL_PUBLIC='http://localhost:6060/fhir/'
export HAPI_FHIR_URL='http://host.docker.internal:6060/fhir/'
export FHIR_SERVICES_URL='http://host.docker.internal:9080'
export QDM_QICORE_URL='http://host.docker.internal:9090'
export CQL_CONVERSION_URL='http://host.docker.internal:7070'
export VSAC_VASE_URL='https://vsac.nlm.nih.gov'
export VSAC_SERVICE_URL='http://umlsks.nlm.nih.gov'
export HEALTH_CHECK_URL='http://host.docker.internal:9080'

export QDM_SPREADSHEET='https://spreadsheets.google.com/feeds/list/1uFtfWIHndogk-aoqROpuFSwHfcEKjaELw2Ph567uiNo/o7a94yh/public/values?alt=json'