package telegram.bot;

import io.micronaut.runtime.Micronaut;
import org.telegram.telegrambots.ApiContextInitializer;

public class Application {
  public static void main(String... arg) {
    ApiContextInitializer.init();
    Micronaut.run(Application.class);
  }
}