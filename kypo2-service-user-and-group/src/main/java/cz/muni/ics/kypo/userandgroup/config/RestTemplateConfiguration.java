package cz.muni.ics.kypo.userandgroup.config;

import cz.muni.ics.kypo.userandgroup.api.dto.enums.SpringProfiles;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.List;

@Configuration
@PropertySource(value = "file:${path.to.config.file}")
public class RestTemplateConfiguration {

    @Autowired
    private RestTemplateHeaderModifierInterceptor restTemplateHeaderModifierInterceptor;

    @Value("${server.ssl.trust-store: #{null}}")
    private String trustStore;
    @Value("${server.ssl.trust-store-password: #{null}}")
    private String trustStorePassword;
    @Autowired
    private Environment environment;

    @Bean
    public RestTemplate prodRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate = setRestTemplateSSL(restTemplate);
        setMessageConverters(restTemplate);
        return setInterceptors(restTemplate);
    }

    private RestTemplate setMessageConverters(RestTemplate restTemplate) {
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
        return restTemplate;
    }

    private RestTemplate setInterceptors(RestTemplate restTemplate) {
        List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
        interceptors.add(restTemplateHeaderModifierInterceptor);
        restTemplate.setInterceptors(interceptors);
        return restTemplate;
    }

    private RestTemplate setRestTemplateSSL(RestTemplate restTemplate) {
        String[] profiles = this.environment.getActiveProfiles();
        if (profiles != null && profiles.length > 0) {
            for (int i = 0; i < profiles.length; i++) {
                if (profiles[i].equals(SpringProfiles.PROD.name())) {
                    if (trustStore != null && trustStorePassword != null) {
                        return new RestTemplate(getHttpClientFactoryForRestTemplate());
                    } else {
                        throw new ExceptionInInitializerError("Path to trust store and trust store password must be defined.");
                    }
                }
            }
        }
        return restTemplate;
    }

    private HttpComponentsClientHttpRequestFactory getHttpClientFactoryForRestTemplate() {
        SSLContext sslContext = null;
        try {
            sslContext = new SSLContextBuilder()
                    .loadTrustMaterial(new File(trustStore), trustStorePassword.toCharArray())
                    .setProtocol("TLSv1.2")
                    .build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException | CertificateException | IOException e) {
            throw new ExceptionInInitializerError("Path to trust store and trust store password must be defined.");
        }
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);
        HttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(socketFactory)
                .build();
        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }

}
