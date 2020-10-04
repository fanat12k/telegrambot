package telegram.bot.persistance.domain;

public class IkeaParams {
  public String shopId;
  public String shopName;
  public String workHours;

  public String getShopId() {
    return shopId;
  }

  public void setShopId(String shopId) {
    this.shopId = shopId;
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
