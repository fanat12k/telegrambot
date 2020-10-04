package telegram.bot.service.telegram.model;

import java.util.Map;

import static java.util.Map.entry;

public class TelegramConstant {
  public static final String HTTP_CLIENT_AGENT = "Mozilla/5.0 (Macintosh Intel Mac OS X 10.12 rv:55.0) Gecko/20100101 Firefox/55.0";
  public static final String SUCCESS_RESPONSE_MASSAGE = ":white_check_mark: :white_check_mark: :white_check_mark: %s : %s :white_check_mark: :white_check_mark: :white_check_mark:";
  public static final String ERROR_RESPONSE_MASSAGE = ":skull_crossbones: :skull_crossbones: :skull_crossbones: %s : %s :skull_crossbones: :skull_crossbones: :skull_crossbones:";
  public static final String STATUS = "Status";
  public static final String START = "/start";
  public static final String RESTART = "Restart";
  public static final String BUILD = "Build Static";
  public static final String CLEAR = "Clear Cache";
  public static final String WHITE_CHECK_MARK_RESTART = ":white_check_mark: Restart";
  public static final String WHITE_CHECK_MARK_GOOD_RESULT = ":white_check_mark: Good";
  public static final String INFORMATION_SOURCE_STATUS = ":information_source: Status";
  public static final String BUILD_STATIC = ":cityscape: Build Static";
  public static final String CLEAR_CACHE = ":picture_frame: Clear Cache";
  public static final String RESTART_TERMINAL_COMMAND = "sudo systemctl restart site.java";
  public static final String BUILD_STATIC_COMMAND = "sh /home/menesty/development/workspace/site-build.sh ";
  public static final String NO_ENTRY_SIGN_RESTART = ":no_entry_sign: To math restart";
  public static final String ERROR_MASSAGE = ":x: :x: :x: Critical error :x: :x: :x:";
  public static final String ERROR = "Error";
  public static final String DEFAULT_MAIL = "lunat12k@gmail.com";
  public static final String X_AUTH_EMAIL = "X-Auth-Email";
  public static final String X_AUTH_KEY = "X_Auth_Key";
  public static final String CLEAR_CACHE_URL = "https://api.cloudflare.com/client/v4/zones/bfc04606e6d0155c80da663780c9c357/purge_cache";
  public static final String CLEAR_CACHE_URL_BODY = "{\"purge_everything\":true}";
  public static final String SUPPORT_COMMANDS = "Support commands /restart /status";
  public static final String FOTEL_BOT_NAME = "Fotel";
  public static final String ACCESS_DENIED = " :radioactive: :radioactive: :radioactive: Access denied :radioactive: :radioactive: :radioactive:";
  public static final Map<String, String> CHECK_URL_LIST = Map.ofEntries(entry("product", "https://fotel.com.ua/group/rx/rozprodazh-tovariv-dlya-domu/quantity"),
          entry("static", "https://fotel.com.ua/static/favicons/apple-touch-icon.png"),
          entry("delivery", "https://fotel.com.ua/checkout/delivery/provider/1/city/8d5a980d-391c-11dd-90d9-001a92567626/departments")
  );


  public TelegramConstant() {
  }
}

