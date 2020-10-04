package telegram.bot.service.telegram;

import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.discovery.event.ServiceStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import javax.inject.Singleton;

@Singleton
public class TelegramBotService implements ApplicationEventListener<ServiceStartedEvent> {

  private static final Logger LOGGER = LoggerFactory.getLogger(TelegramBotService.class);

  private TelegramBotsApi telegramBotsApi;
  private TelegramIkeaBot telegramIkeaBot;
  private TelegramFotelBot telegramFotelBot;

  public TelegramBotService(TelegramFotelBot telegramFotelBot, TelegramBotsApi telegramBotsApi, TelegramIkeaBot telegramIkeaBot) {
    this.telegramBotsApi = telegramBotsApi;
    this.telegramIkeaBot = telegramIkeaBot;
    this.telegramFotelBot = telegramFotelBot;
  }


  @Override
  public void onApplicationEvent(ServiceStartedEvent event) {

    LOGGER.info("Registering Bots");
    try {
      telegramBotsApi.registerBot(telegramIkeaBot);
      telegramBotsApi.registerBot(telegramFotelBot);
    } catch (TelegramApiRequestException e) {
      LOGGER.error(e.getMessage(), e);
    }
    LOGGER.info("Finished Bots");
  }
}
