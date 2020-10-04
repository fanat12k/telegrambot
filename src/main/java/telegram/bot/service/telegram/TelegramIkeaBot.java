package telegram.bot.service.telegram;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vdurmont.emoji.EmojiParser;
import io.micronaut.context.annotation.Property;
import io.micronaut.core.util.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import telegram.bot.persistance.domain.*;
import telegram.bot.persistance.repository.TelegramRepository;
import telegram.bot.service.SitePropertyService;
import telegram.bot.service.telegram.model.TelegramConstant;
import telegram.bot.service.telegram.model.TelegramIkeaConstants;

import javax.inject.Singleton;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static telegram.bot.service.telegram.model.TelegramConstant.ACCESS_DENIED;
import static telegram.bot.service.telegram.model.TelegramIkeaConstants.*;
import static telegram.bot.util.EncryptionUtil.encodeHmacSHA1;
import static telegram.bot.util.HttpClientUtil.buildHttpClient;

@Singleton
public class TelegramIkeaBot extends TelegramLongPollingBot {
  static final AtomicBoolean running = new AtomicBoolean();
  private CloseableHttpClient httpClient;
  private ObjectMapper objectMapper;
  private String cryptoKey;
  private String botKey;
  private String login;
  private String password;
  private TelegramRepository telegramRepository;
  private SitePropertyService sitePropertyService;


  public TelegramIkeaBot(SitePropertyService sitePropertyService,
                         KeyStore keyStore,
                         TelegramRepository telegramRepository,
                         @Property(name = "telegram.ikea.secret.key") String cryptoKey,
                         @Property(name = "telegram.ikea.key") String botKey,
                         @Property(name = "telegram.ikea.login") String login,
                         @Property(name = "telegram.ikea.password") String password,
                         ObjectMapper objectMapper) {
    this.httpClient = buildHttpClient(keyStore);
    this.objectMapper = objectMapper;
    this.sitePropertyService = sitePropertyService;
    this.botKey = botKey;
    this.login = login;
    this.password = password;
    this.cryptoKey = cryptoKey;
    this.telegramRepository = telegramRepository;
    init();
  }

  public void init() {
    login(login, password);
  }

  @Override
  public void onUpdateReceived(Update update) {
    Long userId = Long.valueOf(update.getMessage().getFrom().getId());
    String userName = update.getMessage().getFrom().getUserName();

    List<TelegramUser> telegramUserList = telegramRepository.list(BOT_NAME);

    Optional<TelegramUser> telegramUser = telegramUserList.stream().filter(user -> user.getUserId().equals(userId)).findFirst();

    if (telegramUser.isEmpty()) {
      telegramRepository.save(new TelegramUser(userId, userName));
    }

    if (update.hasMessage() && update.getMessage().hasText() && telegramUser.isPresent() && telegramUser.get().isAccess()) {
      Long chatId = update.getMessage().getChatId();
      if (update.getMessage().getText().contains(AVAILABLE)) {
        availableHoursPrint(chatId);
      } else if (SHOP_LIST.contains(update.getMessage().getText())) {
        sendHoursMassage(chatId, update.getMessage().getText());
      } else if (update.getMessage().getText().contains(TelegramConstant.START)) {
        sendMassage(chatId, TelegramIkeaConstants.SUPPORT_COMMANDS);
      }
    } else {
      sendMassage(update.getMessage().getChatId(), EmojiParser.parseToUnicode(ACCESS_DENIED));
    }

  }

  public void availableHoursPrint(Long chatId) {

    if (!running.compareAndSet(false, true)) {
      sendMassage(chatId, ALREADY_RUNNING);
    }
    try {
      List<IkeaShopItem> ikeaShopItemList = getIkeaShopList().stream().filter(values -> SHOP_LIST.contains(values.getName())).collect(Collectors.toList());
      StringBuilder stringBuilder = new StringBuilder();
      ikeaShopItemList.stream().filter(Objects::nonNull).forEach(ikeaShopItem -> {
        List<String> availableDates = getListPickUpDates(ikeaShopItem.getShopId());
        if (!availableDates.isEmpty()) {
          stringBuilder.append(ikeaShopItem.getName()).append(" : ").append(availableDates.get(0)).append("\n");
        }
      });
      sendMassage(chatId, stringBuilder.toString());
    } finally {
      running.set(false);
    }


  }


  public void sendHoursMassage(Long chatId, String cityName) {

    IkeaShopItem ikeaShopItem = getIkeaShopList().stream().filter(Objects::nonNull).filter(value -> value.getName().contains(cityName)).findFirst().orElse(null);

    if (Objects.nonNull(ikeaShopItem)) {

      StringBuilder stringBuilder = new StringBuilder();

      List<String> availableDates = getListPickUpDates(ikeaShopItem.getShopId());

      if (!availableDates.isEmpty()) {
        stringBuilder.append(cityName).append("\n");
        availableDates.forEach(value -> stringBuilder.append(value).append("\n"));
        sendMassage(chatId, stringBuilder.toString());
      }
    }

  }


  private List<String> getListPickUpDates(String shopId) {
    List<String> dates = new ArrayList<>();

    String checkoutLink = getCheckoutLink(shopId);

    if (StringUtils.isEmpty(checkoutLink)) {
      return Collections.emptyList();
    }

    HttpGet httpGet = new HttpGet(checkoutLink);

    try (CloseableHttpResponse closeableHttpResponse = httpClient.execute(httpGet)) {
      EntityUtils.consume(closeableHttpResponse.getEntity());
    } catch (IOException e) {
      e.printStackTrace();
    }

    HttpGet httpGet1 = new HttpGet(CUSTOMER_DATA_URL);

    try (CloseableHttpResponse response = httpClient.execute(httpGet1)) {

      Document document = Jsoup.parse(EntityUtils.toString(response.getEntity()));

      Elements rows = document.select(PICKUP_DATE_SELECTOR);

      for (Element row : rows.get(0).getAllElements()) {

        if (row.getElementsByClass(DYNAMIC_UPDATE_CLASS).isEmpty()) {
          dates.add(row.text());
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return dates;
  }

  public void scheduledCheckStatus() {

    List<TelegramUser> telegramUserList = new ArrayList<>(telegramRepository.list(BOT_NAME));

    Map<String, ShopsAvailableHours> shopsHours = getIkeaShopList().stream().filter(values -> SHOP_LIST.contains(values.getName())).collect(Collectors.toMap(IkeaShopItem::getShopId, value -> {
      List<String> result = getListPickUpDates(value.getShopId());
      ShopsAvailableHours shopsAvailableHours = new ShopsAvailableHours();
      shopsAvailableHours.setShopName(value.getName());
      shopsAvailableHours.setWorkHours(!result.isEmpty() ? result.get(0) : "");
      return shopsAvailableHours;
    }));

    shopsHours.forEach((key, shopsAvailableHours) -> {
      SiteProperty siteProperty = sitePropertyService.get(key);
      if (StringUtils.isNotEmpty(siteProperty.getValue()) && !shopsAvailableHours.getWorkHours().equals(siteProperty.getValue())) {
        String text = shopsAvailableHours.getShopName() + " " + siteProperty.getValue() + " > " + shopsAvailableHours.getWorkHours();
        sitePropertyService.update(key, shopsAvailableHours.getWorkHours());
        telegramUserList.stream().filter(TelegramUser::isAccess).forEach(telegramUser -> sendMassage(telegramUser.getUserId(), EmojiParser.parseToUnicode(text)));
      } else if (StringUtils.isNotEmpty(shopsAvailableHours.getWorkHours())) {
        sitePropertyService.update(key, shopsAvailableHours.getWorkHours());
      }
    });

  }

  public String getCheckoutLink(String shopId) {
    HttpPost httpPost = new HttpPost(CHECKOUT_LINK_URL);

    Payload payload = new Payload();
    payload.setSelectedServiceValue(shopId);

    String jsonResult = null;
    try {
      jsonResult = objectMapper.writeValueAsString(payload);
      httpPost.setEntity(new StringEntity(PAYLOAD + jsonResult + CHECKOUT_DELIVERY_URL + encodeHmacSHA1(cryptoKey, jsonResult), ContentType.DEFAULT_TEXT));

    } catch (Exception e) {
      e.printStackTrace();
      return "";
    }

    try (CloseableHttpResponse closeableHttpResponse = httpClient.execute(httpPost)) {
      ClickAndCollectResponse clickAndCollectResponse = objectMapper.readValue(EntityUtils.toString(closeableHttpResponse.getEntity()), ClickAndCollectResponse.class);
      if (Objects.nonNull(clickAndCollectResponse) && clickAndCollectResponse.isStatus().equals("OK")) {
        return clickAndCollectResponse.getTarget();
      }

    } catch (Exception e) {
      e.printStackTrace();
      return "";
    }
    return "";
  }

  public List<IkeaShopItem> getIkeaShopList() {
    HttpGet httpGet = new HttpGet(SHOP_LIST_URL);
    List<IkeaShopItem> ikeaShopItemList = new ArrayList<>();
    try (CloseableHttpResponse closeableHttpResponse = httpClient.execute(httpGet)) {
      IkeaShop product = objectMapper.readValue(EntityUtils.toString(closeableHttpResponse.getEntity()), IkeaShop.class);
      product.getDetails().forEach((key, value) -> {
        if (SHOP_LIST.contains(value.getName())) {
          IkeaShopItem ikeaShopItem = new IkeaShopItem();
          ikeaShopItem.setName(value.getName());
          ikeaShopItem.setShopId(key);
          ikeaShopItemList.add(ikeaShopItem);
        }
      });
    } catch (IOException e) {
      e.printStackTrace();
    }
    return ikeaShopItemList;
  }

  public String login(String login, String password) {

    HttpPost httpPost = new HttpPost(LOGIN_URL);

    List<NameValuePair> params = new ArrayList<>();

    params.add(new BasicNameValuePair("username", login));
    params.add(new BasicNameValuePair("password", password));

    httpPost.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

    try (CloseableHttpResponse closeableHttpResponse = httpClient.execute(httpPost)) {
      if (closeableHttpResponse.getFirstHeader(LOCATION) == null) {
        login(login, password);
        // throw new LoginIkeaException("Invalid login or password for user : " + user.getLogin());
      }

      return closeableHttpResponse.getFirstHeader(LOCATION).getValue();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return "";
  }


  public synchronized void setButtons(SendMessage sendMessage) {
    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
    sendMessage.setReplyMarkup(replyKeyboardMarkup);
    replyKeyboardMarkup.setSelective(true);
    replyKeyboardMarkup.setResizeKeyboard(false);
    replyKeyboardMarkup.setOneTimeKeyboard(false);

    List<KeyboardRow> keyboard = new ArrayList<>();

    KeyboardRow firstRow = new KeyboardRow();
    firstRow.add(EmojiParser.parseToUnicode("Kraków"));
    firstRow.add(EmojiParser.parseToUnicode("Lublin"));
    KeyboardRow secondRow = new KeyboardRow();
    secondRow.add(EmojiParser.parseToUnicode("Warszawa Janki"));
    secondRow.add(EmojiParser.parseToUnicode("Warszawa Targówek"));

    KeyboardRow lastRow = new KeyboardRow();
    lastRow.add(EmojiParser.parseToUnicode(AVAILABLE_HOURS));

    keyboard.add(firstRow);
    keyboard.add(secondRow);
    keyboard.add(lastRow);

    replyKeyboardMarkup.setKeyboard(keyboard);
  }

  private void sendMassage(Long chatId, String answer) {
    SendMessage message = new SendMessage()
            .setChatId(chatId)
            .setText(answer);

    setButtons(message);

    try {
      execute(message);
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }

  @Override
  public String getBotUsername() {
    return BOT_NAME;
  }

  @Override
  public void onClosing() {
    super.onClosing();

    if (httpClient != null) {
      try {
        httpClient.close();
      } catch (IOException e) {
        //skip
      }
    }
  }

  @Override
  public String getBotToken() {
    return botKey;
  }
}
