package gov.cms.mat.fhir.rest.dto;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author duanedecouteau
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FhirIncludeLibraryReferences {

    String name;
    String version;
    String referenceEndpoint;
    boolean searchResult;
}
