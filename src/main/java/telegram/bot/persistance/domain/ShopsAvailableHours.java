package telegram.bot.persistance.domain;

public class ShopsAvailableHours {
  private String shopName;
  private String key;
  private String workHours;

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getShopName() {
    return shopName;
  }

  public void setShopName(String shopName) {
    this.shopName = shopName;
  }

  public String getWorkHours() {
    return workHours;
  }

  public void setWorkHours(String workHours) {
    this.workHours = workHours;
  }
}
