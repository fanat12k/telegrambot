package telegram.bot.config;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.telegram.telegrambots.meta.TelegramBotsApi;

import javax.inject.Singleton;
import javax.sql.DataSource;

@Factory
public class ApplicationConfiguration {
  @Singleton
  @Bean
  public JdbcTemplate jdbcTemplate(DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }

  @Singleton
  @Bean
  public TelegramBotsApi telegramBotsApi() {
    return new TelegramBotsApi();
  }
}
