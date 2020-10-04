package telegram.bot.service;

import telegram.bot.persistance.domain.SiteProperty;

import java.time.LocalDateTime;

public interface SitePropertyService {
  SiteProperty get(String key);

  void update(String key, LocalDateTime value);

  void update(String key, String value);
}
