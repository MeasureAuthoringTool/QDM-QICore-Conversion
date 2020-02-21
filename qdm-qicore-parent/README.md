Remove old images

docker system prune



we have to manually copy the cache files for docker to build
cd mat-fhir-services
cp -R /opt/vsac/cache/ cache/

