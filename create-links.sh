#!/bin/sh
sudo mkdir -p /opt/library
sudo mkdir -p /opt/vsac
sudo mkdir -p /opt/library/uncoverted/
sudo ln -f -s `pwd`/mat-fhir-services/fhir/  /opt/library/fhir
sudo ln -f -s `pwd`/mat-fhir-services/cache/ /opt/vsac/cache