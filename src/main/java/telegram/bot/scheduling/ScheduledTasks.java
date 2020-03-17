package telegram.bot.scheduling;

import io.micronaut.scheduling.annotation.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import telegram.bot.service.telegram.TelegramFotelBot;

@Service
public class ScheduledTasks {
  private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledTasks.class);
  private TelegramFotelBot telegramFotelBot;


  public ScheduledTasks(TelegramFotelBot telegramFotelBot) {
    this.telegramFotelBot = telegramFotelBot;
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
