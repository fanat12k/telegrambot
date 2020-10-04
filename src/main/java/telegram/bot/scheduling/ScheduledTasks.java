package telegram.bot.scheduling;

import io.micronaut.scheduling.annotation.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import telegram.bot.service.telegram.TelegramFotelBot;
import telegram.bot.service.telegram.TelegramIkeaBot;

@Service
public class ScheduledTasks {
  private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledTasks.class);
  private TelegramFotelBot telegramFotelBot;
  private TelegramIkeaBot telegramIkeaBot;


  public ScheduledTasks(TelegramFotelBot telegramFotelBot,TelegramIkeaBot telegramIkeaBot) {
    this.telegramFotelBot = telegramFotelBot;
    this.telegramIkeaBot = telegramIkeaBot;
  }

  @Scheduled(initialDelay = "2m", fixedRate = "10m")
  public void refreshIkeaData() {
    try {
      telegramIkeaBot.scheduledCheckStatus();
    } catch (Exception e) {
      LOGGER.error("Error", e);
    }
  }

  @Scheduled(initialDelay = "60s", fixedRate = "2m")
  public void refreshData() {
    try {
      telegramFotelBot.scheduledCheckStatus();
    } catch (Exception e) {
      LOGGER.error("Error", e);
    }
  }

}
