package org.protobeans.webservices.example;

import org.protobeans.mvc.MvcEntryPoint;
import org.protobeans.undertow.annotation.EnableUndertow;
import org.protobeans.webservices.annotation.EnableWebServices;
import org.protobeans.webservices.example.ws.endpoint.CountryEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

@EnableUndertow
@EnableWebServices
@EnableWs
@ComponentScan(basePackageClasses = CountryEndpoint.class)
public class Main {
    @Bean(name = "countries")
    public DefaultWsdl11Definition countriesByNameWsdl11Definition() {
        DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
        
        wsdl11Definition.setPortTypeName("CountriesPort");
        wsdl11Definition.setLocationUri("/ws");
        wsdl11Definition.setSchema(countriesSchema());
        
        return wsdl11Definition;
    }
    
    @Bean
    public XsdSchema countriesSchema() {
        return new SimpleXsdSchema(new ClassPathResource("ws/countries.xsd"));
    }
    
    public static void main(String[] args) {
        MvcEntryPoint.run(Main.class);
    }
}