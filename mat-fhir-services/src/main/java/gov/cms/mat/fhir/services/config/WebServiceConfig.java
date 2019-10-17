/*
 * Copyright 2017 Cognitive Medicine Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.cms.mat.fhir.services.config;



import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import gov.cms.mat.fhir.services.rest.MeasureTranslationService;
import java.util.Arrays;
import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.swagger.Swagger2Feature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author duanedecouteau
 */
@Configuration()
public class WebServiceConfig {
    @Autowired
    private Bus bus;
    
    @Autowired
    private MeasureTranslationService translationservice;
        

    
    @Bean
    public Server rsServer() {
        Swagger2Feature swag = new Swagger2Feature();
        JAXRSServerFactoryBean endpoint = new JAXRSServerFactoryBean();
        endpoint.setBus(bus);
        endpoint.setAddress("/");
        endpoint.setProvider(new JacksonJaxbJsonProvider());
        endpoint.setServiceBeans(Arrays.<Object>asList(translationservice));
        endpoint.setFeatures(Arrays.asList(swag));
        return endpoint.create();
    }
    
}
