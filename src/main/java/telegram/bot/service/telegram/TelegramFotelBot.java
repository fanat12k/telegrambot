package telegram.bot.service.telegram;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vdurmont.emoji.EmojiParser;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import telegram.bot.persistance.domain.TelegramUser;
import telegram.bot.persistance.repository.TelegramRepository;
import telegram.bot.service.SitePropertyService;
import telegram.bot.service.telegram.model.CloudFlareRequestResult;
import telegram.bot.service.telegram.model.TelegramConstant;

import javax.inject.Singleton;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static telegram.bot.Constants.*;
import static telegram.bot.service.telegram.model.TelegramConstant.*;
import static telegram.bot.util.HttpClientUtil.buildHttpClient;

@Singleton
public class TelegramFotelBot extends TelegramLongPollingBot {
  private CloseableHttpClient httpClient;
  private ObjectMapper objectMapper;
  private SitePropertyService sitePropertyService;
  private TelegramRepository telegramRepository;

  TelegramFotelBot(ObjectMapper objectMapper, SitePropertyService sitePropertyService, TelegramRepository telegramRepository) {
    this.sitePropertyService = sitePropertyService;
    this.objectMapper = objectMapper;
    this.httpClient = buildHttpClient(null);
    this.telegramRepository = telegramRepository;
  }


  @Override
  public void onUpdateReceived(Update update) {

    Long userId = Long.valueOf(update.getMessage().getFrom().getId());
    String userName = update.getMessage().getFrom().getUserName();

    List<TelegramUser> telegramUserList = telegramRepository.list(FOTEL_BOT_NAME);

    Optional<TelegramUser> telegramUser = telegramUserList.stream().filter(user -> user.getUserId().equals(userId)).findFirst();

    if (telegramUser.isEmpty()) {
      telegramRepository.save(new TelegramUser(userId, userName));
    }

    LocalDateTime lastRestartTime = sitePropertyService.get(LAST_RESTART_DATE).asDateTime();

    if (update.hasMessage() && update.getMessage().hasText() && telegramUser.isPresent() && telegramUser.get().isAccess()) {
      Long chatId = update.getMessage().getChatId();
      if (update.getMessage().getText().contains(TelegramConstant.STATUS)) {
        checkSendStatus(update.getMessage().getChatId());
      } else if (update.getMessage().getText().contains(TelegramConstant.RESTART)) {
        long minutes = Objects.isNull(sitePropertyService.get(LAST_RESTART_DATE).asDateTime()) ? 0 : ChronoUnit.MINUTES.between(lastRestartTime, LocalDateTime.now());
        if (Objects.isNull(lastRestartTime) || minutes > 3) {
          sitePropertyService.update(LAST_RESTART_DATE, LocalDateTime.now());
          restart(update.getMessage().getChatId());
        } else {
          sendMassage(chatId, EmojiParser.parseToUnicode(TelegramConstant.NO_ENTRY_SIGN_RESTART));
        }
      } else if (update.getMessage().getText().contains(TelegramConstant.BUILD)) {
        buildStatic(update.getMessage().getChatId());
      } else if (update.getMessage().getText().contains(CLEAR)) {
        clearCache(update.getMessage().getChatId());
      } else if (update.getMessage().getText().contains(TelegramConstant.START)) {
        sendMassage(chatId, TelegramConstant.SUPPORT_COMMANDS);
      }
    } else {
      sendMassage(update.getMessage().getChatId(), EmojiParser.parseToUnicode(ACCESS_DENIED));
    }
  }

  public void restart(Long chatId) {
    try {
      Process proc = Runtime.getRuntime().exec(TelegramConstant.RESTART_TERMINAL_COMMAND);
      proc.waitFor();
      sendMassage(chatId, EmojiParser.parseToUnicode(TelegramConstant.WHITE_CHECK_MARK_GOOD_RESULT));
    } catch (Exception e) {
      sendMassage(chatId, EmojiParser.parseToUnicode(TelegramConstant.ERROR_MASSAGE));
      e.printStackTrace();
    }
  }


  public void buildStatic(Long chatId) {
    try {
      Process proc = Runtime.getRuntime().exec(TelegramConstant.BUILD_STATIC_COMMAND + getBotToken() + " " + chatId);
      proc.waitFor();
    } catch (Exception e) {
      sendMassage(chatId, EmojiParser.parseToUnicode(TelegramConstant.ERROR_MASSAGE));
      e.printStackTrace();
    }
  }

  public void scheduledCheckStatus() {
    List<TelegramUser> telegramUserList = new ArrayList<>(telegramRepository.list(FOTEL_BOT_NAME));
    Map<String, Integer> responseCode = checkStatusServer();
    StringBuilder resultString = new StringBuilder();

    responseCode.forEach((key, value) -> {
      if (!value.equals(200)) {
        resultString.append(String.format(EmojiParser.parseToUnicode(TelegramConstant.ERROR_RESPONSE_MASSAGE), key, value)).append("\n");
      }
    });
    if (resultString.length() != 0) {
      telegramUserList.stream().filter(TelegramUser::isAccess).forEach(telegramUser -> sendMassage(telegramUser.getUserId(), EmojiParser.parseToUnicode(resultString.toString())));
    }
  }

  public void checkSendStatus(Long chatId) {
    Map<String, Integer> responseCode = checkStatusServer();
    StringBuilder resultString = new StringBuilder();

    responseCode.forEach((key, value) -> {
      if (value.equals(200)) {
        resultString.append(String.format(EmojiParser.parseToUnicode(TelegramConstant.SUCCESS_RESPONSE_MASSAGE), key, value)).append("\n");
      } else {
        resultString.append(String.format(EmojiParser.parseToUnicode(TelegramConstant.ERROR_RESPONSE_MASSAGE), key, value)).append("\n");
      }
    });

    sendMassage(chatId, EmojiParser.parseToUnicode(resultString.toString()));
  }

  public void clearCache(Long chatId) {
    HttpPost httpPost = new HttpPost(CLEAR_CACHE_URL);

    httpPost.setHeader(X_AUTH_EMAIL, DEFAULT_MAIL);
    httpPost.setHeader(X_AUTH_KEY, sitePropertyService.get(CLOUD_FLARE_KEY).getValue());

    try {
      httpPost.setEntity(new StringEntity(objectMapper.writeValueAsString(CLEAR_CACHE_URL_BODY), ContentType.APPLICATION_JSON));
    } catch (JsonProcessingException e) {
      sendMassage(chatId, EmojiParser.parseToUnicode(TelegramConstant.ERROR_MASSAGE));
      // e.printStackTrace();
    }

    try (CloseableHttpResponse closeableHttpResponse = httpClient.execute(httpPost)) {
      CloudFlareRequestResult cloudFlareRequestResult = objectMapper.readValue(EntityUtils.toString(closeableHttpResponse.getEntity()), CloudFlareRequestResult.class);
      if (Objects.nonNull(cloudFlareRequestResult) && cloudFlareRequestResult.isSuccess()) {
        sendMassage(chatId, EmojiParser.parseToUnicode(TelegramConstant.WHITE_CHECK_MARK_GOOD_RESULT));
      } else {
        sendMassage(chatId, EmojiParser.parseToUnicode(TelegramConstant.ERROR_MASSAGE));
      }

    } catch (IOException e) {
      e.printStackTrace();
      sendMassage(chatId, EmojiParser.parseToUnicode(TelegramConstant.ERROR_MASSAGE));
    }
  }


  public Map<String, Integer> checkStatusServer() {

    Map<String, Integer> result = new HashMap<>();

    CHECK_URL_LIST.forEach((key, value) -> {
      HttpGet httpPost = new HttpGet(value);
      try (CloseableHttpResponse closeableHttpResponse = httpClient.execute(httpPost)) {
        result.put(key, closeableHttpResponse.getStatusLine().getStatusCode());
      } catch (IOException e) {
        result.put(ERROR, 999);
        e.printStackTrace();
      }
    });
    return result;
  }


  public synchronized void setButtons(SendMessage sendMessage) {
    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
    sendMessage.setReplyMarkup(replyKeyboardMarkup);
    replyKeyboardMarkup.setSelective(true);
    replyKeyboardMarkup.setResizeKeyboard(true);
    replyKeyboardMarkup.setOneTimeKeyboard(false);

    List<KeyboardRow> keyboard = new ArrayList<>();

    KeyboardRow keyboardInfoRow = new KeyboardRow();
    keyboardInfoRow.add(EmojiParser.parseToUnicode(TelegramConstant.INFORMATION_SOURCE_STATUS));

    KeyboardRow keyboardBuildStaticRow = new KeyboardRow();
    keyboardBuildStaticRow.add(EmojiParser.parseToUnicode(TelegramConstant.BUILD_STATIC));
    keyboardBuildStaticRow.add(EmojiParser.parseToUnicode(CLEAR_CACHE));

    KeyboardRow keyboardRestartRow = new KeyboardRow();
    keyboardRestartRow.add(EmojiParser.parseToUnicode(WHITE_CHECK_MARK_RESTART));
    keyboard.add(keyboardInfoRow);
    keyboard.add(keyboardRestartRow);
    keyboard.add(keyboardBuildStaticRow);

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
    return TelegramConstant.FOTEL_BOT_NAME;
  }


  @Override
  public String getBotToken() {
    return sitePropertyService.get(FOTEL_TELEGRAM_KEY).getValue();
  }
}
