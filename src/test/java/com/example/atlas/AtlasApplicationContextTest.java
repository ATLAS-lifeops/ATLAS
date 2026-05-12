package com.example.atlas;

import com.example.atlas.telegram.TelegramBotAdapter;
import com.example.atlas.telegram.TelegramMessageSender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AtlasApplicationContextTest {

    @Test
    void contextLoadsWithTelegramDisabled() {
        SpringApplication application = new SpringApplication(AtlasApplication.class);
        application.setDefaultProperties(Map.of(
                "atlas.telegram.enabled", "false",
                "spring.main.web-application-type", "none",
                "spring.autoconfigure.exclude",
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
        ));

        try (ConfigurableApplicationContext context = application.run()) {
            assertThatThrownBy(() -> context.getBean(TelegramBotAdapter.class))
                    .isInstanceOf(NoSuchBeanDefinitionException.class);
            assertThatThrownBy(() -> context.getBean(TelegramMessageSender.class))
                    .isInstanceOf(NoSuchBeanDefinitionException.class);
        }
    }

    @Test
    void contextLoadsWithTelegramEnabledAndToken() {
        SpringApplication application = new SpringApplication(AtlasApplication.class);
        application.setDefaultProperties(baseTestProperties());

        try (ConfigurableApplicationContext context = application.run(
                "--atlas.telegram.enabled=true",
                "--atlas.telegram.bot-token=test-token",
                "--atlas.telegram.bot-username=atlas_test_bot"
        )) {
            assertThat(context.getBean(TelegramBotAdapter.class).botUsername())
                    .isEqualTo("atlas_test_bot");
        }
    }

    @Test
    void contextFailsWhenTelegramEnabledWithoutToken() {
        SpringApplication application = new SpringApplication(AtlasApplication.class);
        application.setDefaultProperties(baseTestProperties());

        assertThatThrownBy(() -> application.run(
                "--atlas.telegram.enabled=true",
                "--atlas.telegram.bot-token=",
                "--atlas.telegram.bot-username=atlas_test_bot"
        ))
                .hasStackTraceContaining("ATLAS Telegram integration is enabled")
                .hasStackTraceContaining("ATLAS_TELEGRAM_BOT_TOKEN");
    }

    private Map<String, Object> baseTestProperties() {
        java.util.HashMap<String, Object> values = new java.util.HashMap<>();
        values.put("spring.main.web-application-type", "none");
        values.put(
                "spring.autoconfigure.exclude",
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
        );
        return values;
    }
}
