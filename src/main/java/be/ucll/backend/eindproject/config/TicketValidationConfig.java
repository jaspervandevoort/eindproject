package be.ucll.backend.eindproject.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(TicketValidationProperties.class)
public class TicketValidationConfig {

    @Bean
    public RestClient ticketValidationRestClient(TicketValidationProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.connectTimeoutMs());
        requestFactory.setReadTimeout(properties.readTimeoutMs());

        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }
}
