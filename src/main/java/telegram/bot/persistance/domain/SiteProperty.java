package telegram.bot.persistance.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

public class SiteProperty {
  private String key;
  private String value;

  public SiteProperty() {
  }

  public SiteProperty(String key, String value) {
    this.key = key;
    this.value = value;
  }

  public SiteProperty(String key, LocalDateTime value) {
    this.key = key;
    this.value = Optional.ofNullable(value).map(val -> value.format(DateTimeFormatter.ISO_DATE_TIME)).orElse(null);
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public BigDecimal asBigDecimal() {
    return Optional.ofNullable(value).map(BigDecimal::new).orElse(null);
  }

  public LocalDateTime asDateTime() {
    return Optional.ofNullable(value).map(val -> {
      try {
        return LocalDateTime.parse(val, DateTimeFormatter.ISO_DATE_TIME);
      } catch (DateTimeParseException exception) {
        return null;
      }
    }).orElse(null);
  }
}
