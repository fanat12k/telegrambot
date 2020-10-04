package telegram.bot.persistance.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import static telegram.bot.service.telegram.model.TelegramIkeaConstants.SHOP_REPLACE_VALUE;

public class IkeaShopItem {
  private String name;
  private boolean isClosed;
  private String closingTimes;
  @JsonIgnore
  private String shopId;

  public String getName() {
    return name.replace(SHOP_REPLACE_VALUE, "");
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isClosed() {
    return isClosed;
  }

  public void setClosed(boolean closed) {
    isClosed = closed;
  }

  public String getClosingTimes() {
    return closingTimes;
  }

  public void setClosingTimes(String closingTimes) {
    this.closingTimes = closingTimes;
  }

  public String getShopId() {
    return shopId;
  }

  public void setShopId(String shopId) {
    this.shopId = shopId;
  }
}
