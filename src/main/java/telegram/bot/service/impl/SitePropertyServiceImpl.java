package telegram.bot.service.impl;

import telegram.bot.persistance.domain.SiteProperty;
import telegram.bot.persistance.repository.SitePropertyRepository;
import telegram.bot.service.SitePropertyService;

import javax.inject.Singleton;
import java.time.LocalDateTime;
import java.util.Optional;

@Singleton
public class SitePropertyServiceImpl implements SitePropertyService {
  private SitePropertyRepository sitePropertyRepository;

  public SitePropertyServiceImpl(SitePropertyRepository sitePropertyRepository) {
    this.sitePropertyRepository = sitePropertyRepository;
  }

  @Override
  public SiteProperty get(String key) {
    Optional<SiteProperty> property = sitePropertyRepository.get(key);
    return property.orElse(new SiteProperty());
  }

  public void update(String key, LocalDateTime value) {
    SiteProperty property = new SiteProperty(key, value);
    updateOrSave(property);
  }

  private SiteProperty updateOrSave(SiteProperty property) {
    if (!sitePropertyRepository.update(property)) {
      sitePropertyRepository.save(property);
    }

    return property;
  }
}
