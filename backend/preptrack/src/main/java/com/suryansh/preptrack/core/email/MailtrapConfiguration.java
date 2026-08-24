package com.suryansh.preptrack.core.email;

import io.mailtrap.client.MailtrapClient;
import io.mailtrap.config.MailtrapConfig;
import io.mailtrap.factory.MailtrapClientFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MailtrapProperties.class)
public class MailtrapConfiguration {

    @Bean
    public MailtrapClient mailtrapClient(MailtrapProperties properties) {
        MailtrapConfig config = new MailtrapConfig.Builder()
                .token(properties.token())
                .build();

        return MailtrapClientFactory.createMailtrapClient(config);
    }
}