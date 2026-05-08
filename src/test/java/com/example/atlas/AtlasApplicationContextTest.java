package com.example.atlas;

import com.example.atlas.telegram.TelegramBotAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Map;

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
        }
    }
}
