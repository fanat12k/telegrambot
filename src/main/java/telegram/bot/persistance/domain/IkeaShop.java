package telegram.bot.persistance.domain;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.LinkedHashMap;
import java.util.Map;

public class IkeaShop {
  Map<String, IkeaShopItem> details = new LinkedHashMap<>();

  public Map<String, IkeaShopItem> getDetails() {
    return details;
  }

  @JsonAnySetter
  public void setDetails(String key, IkeaShopItem value) {
    details.put(key, value);
  }

}
