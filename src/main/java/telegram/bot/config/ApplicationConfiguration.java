package telegram.bot.config;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.telegram.telegrambots.meta.TelegramBotsApi;

import javax.inject.Singleton;
import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

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


  @Bean
  public KeyStore defaultBankKeyStore(@Value("${application.data.path}/certificate") String path, @Property(name = "application.keystore.password") String keystorePassword) throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
    KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());

    try (FileInputStream fis = new FileInputStream(new File(path + "/telegramkeystore.jks"))) {
      trustStore.load(fis, keystorePassword.toCharArray());
    }
    return trustStore;
  }
}
