package telegram.bot.service.telegram.model;

import java.util.Arrays;
import java.util.List;

public class TelegramIkeaConstants {
  public static final String SHOP_LIST_URL = "https://ww8.ikea.com/clickandcollect/pl/receive/listfetchlocations?version=2";
  public static final String CHECKOUT_LINK_URL = "https://ww8.ikea.com/clickandcollect/pl/receive/";
  public static final String LOGIN_URL = "https://securem.ikea.com/pl/pl/login/";
  public static final String CUSTOMER_DATA_URL = "https://ww8.ikea.com/clickandcollect/pl/collect/customerdata";
  public static final String CHECKOUT_DELIVERY_URL = "&backUrl=https://order.ikea.com/pl/pl/checkout/delivery/&hmac=";
  public static final String SHOP_REPLACE_VALUE = " - sklep IKEA";
  public static final String PAYLOAD = "payload=";
  public static final String LOCATION = "location";
  public static final String BOT_NAME = "Ikea_pick_up_bot";
  public static final String PICKUP_DATE_SELECTOR = "#pickupDate";
  public static final String DYNAMIC_UPDATE_CLASS = "dynamicUpdate";
  public static final String ALREADY_RUNNING = "Already running!!!";
  public static final String SUPPORT_COMMANDS = "Support commands /start";
  public static final String AVAILABLE = "Доступні години";
  public static final String AVAILABLE_HOURS = ":hourglass: Доступні години";
  public static final List<String> SHOP_LIST = Arrays.asList("Warszawa Janki", "Kraków", "Lublin", "Warszawa Targówek");

}
