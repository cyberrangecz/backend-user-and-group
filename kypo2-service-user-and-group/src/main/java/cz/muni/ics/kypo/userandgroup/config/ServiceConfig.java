package cz.muni.ics.kypo.userandgroup.config;

import cz.muni.ics.kypo.userandgroup.security.config.ResourceServerSecurityConfig;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author Pavel Seda
 * @author Dominik Pilar
 */
@Configuration
@Import({ResourceServerSecurityConfig.class, PersistenceConfig.class, ValidationMessagesConfig.class})
@ComponentScan(basePackages = {"cz.muni.ics.kypo.userandgroup.service", "cz.muni.ics.kypo.userandgroup.config", "cz.muni.ics.kypo.userandgroup.facade", "cz.muni.ics.kypo.userandgroup.mapping"})
@PropertySource("file:${path.to.config.file}")
public class ServiceConfig {

    @Autowired
    private RestTemplateHeaderModifierInterceptor restTemplateHeaderModifierInterceptor;

    @Value("${server.ssl.trust-store}")
    private String trustStore;
    @Value("${server.ssl.trust-store-password}")
    private String trustStorePassword;

    @Bean
    public RestTemplate restTemplate() throws Exception {
        SSLContext sslContext = new SSLContextBuilder()
                .loadTrustMaterial(new File(trustStore), trustStorePassword.toCharArray())
                .build();
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);
        HttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(socketFactory)
                .build();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
        List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
        interceptors.add(restTemplateHeaderModifierInterceptor);
        restTemplate.setInterceptors(interceptors);
        return restTemplate;
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

}