package org.protobeans.undertow.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletException;

import org.protobeans.core.annotation.InjectFrom;
import org.protobeans.undertow.annotation.EnableUndertow;
import org.protobeans.undertow.annotation.Initializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.SpringServletContainerInitializer;
import org.springframework.web.WebApplicationInitializer;

import io.undertow.Undertow;
import io.undertow.Undertow.Builder;
import io.undertow.predicate.Predicate;
import io.undertow.predicate.Predicates;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.encoding.ContentEncodingRepository;
import io.undertow.server.handlers.encoding.EncodingHandler;
import io.undertow.server.handlers.encoding.GzipEncodingProvider;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainerInitializerInfo;

@Configuration
@InjectFrom(EnableUndertow.class)
public class UndertowConfig {
    protected DeploymentInfo deploymentInfo = Servlets.deployment();
    
    private String host;
    
    private String port;
    
    private Initializer[] initializers;
    
    private Undertow undertow;
    
    private String resourcesPath;
    
    private String welcomePage;
    
    private String errorPage;
    
    private int sessionTimeout;
    
    @Autowired
    private List<Class<? extends WebApplicationInitializer>> springInitializers = new ArrayList<>();
    
    @SuppressWarnings("resource")
    protected Builder configure() throws ServletException {
        deploymentInfo.setContextPath("/")
                      .setDeploymentName("app.war")
                      .setClassLoader(this.getClass().getClassLoader())
                      .setDefaultSessionTimeout(sessionTimeout)
                      .addWelcomePage(welcomePage)
                      .setResourceManager(new ClassPathResourceManager(this.getClass().getClassLoader(), resourcesPath));
        
        if (!errorPage.isEmpty()) {
            deploymentInfo.addErrorPage(Servlets.errorPage(errorPage));
        }
        
        for (Initializer initializer : initializers) {
            deploymentInfo.addServletContainerInitalizer(new ServletContainerInitializerInfo(initializer.initializer(), new HashSet<>(Arrays.asList(initializer.handleTypes()))));
        }

        if (!springInitializers.isEmpty()) {
            Set<Class<?>> springInitializersSet = new HashSet<>();
            
            for (Class<? extends WebApplicationInitializer> initializer : springInitializers) {
                springInitializersSet.add(initializer);
            }
            
            deploymentInfo.addServletContainerInitalizer(new ServletContainerInitializerInfo(SpringServletContainerInitializer.class, springInitializersSet));
        }
        
        final EncodingHandler handler = new EncodingHandler(new ContentEncodingRepository().addEncodingHandler("gzip", 
                new GzipEncodingProvider(8), 50, Predicates.and(Predicates.maxContentSize(1024), 
                                                               new CompressibleMimeTypePredicate("text/html",
                                                                                                 "text/xml",
                                                                                                 "text/plain",
                                                                                                 "text/css",
                                                                                                 "text/javascript",
                                                                                                 "application/javascript",
                                                                                                 "application/json"))))
                                                          .setNext(createServletDeploymentHandler());
        
        return Undertow.builder().addHttpListener(Integer.parseInt(port), host)
                                 .setHandler(handler);
    }
    
    private HttpHandler createServletDeploymentHandler() throws ServletException {
        DeploymentManager manager = Servlets.defaultContainer().addDeployment(deploymentInfo);
        
        manager.deploy();
        
        return manager.start();
    }
    
    @PostConstruct
    public void start() throws ServletException {
        undertow = configure().build();
        undertow.start();
    }
    
    @PreDestroy
    public void stop() {
        undertow.stop();
    }
    
    private static class CompressibleMimeTypePredicate implements Predicate {
        private final List<MimeType> mimeTypes;

        public CompressibleMimeTypePredicate(String... mimeTypes) {
            this.mimeTypes = new ArrayList<>(mimeTypes.length);
            for (String mimeTypeString : mimeTypes) {
                this.mimeTypes.add(MimeTypeUtils.parseMimeType(mimeTypeString));
            }
        }

        @Override
        public boolean resolve(HttpServerExchange value) {
            String contentType = value.getResponseHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
            
            if (contentType != null) {
                for (MimeType mimeType : this.mimeTypes) {
                    if (mimeType.isCompatibleWith(MimeTypeUtils.parseMimeType(contentType))) {
                        return true;
                    }
                }
            }
            
            return false;
        }
    }
}
