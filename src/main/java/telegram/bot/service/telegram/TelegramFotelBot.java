package telegram.bot.service.telegram;

import com.vdurmont.emoji.EmojiParser;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import telegram.bot.persistance.domain.TelegramUser;
import telegram.bot.persistance.repository.TelegramRepository;
import telegram.bot.service.SitePropertyService;
import telegram.bot.service.telegram.model.TelegramConstant;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static telegram.bot.Constants.FOTEL_TELEGRAM_KEY;
import static telegram.bot.Constants.LAST_RESTART_DATE;
import static telegram.bot.service.telegram.model.TelegramConstant.ACCESS_DENIED;

@Singleton
public class TelegramFotelBot extends TelegramLongPollingBot {
  private HttpClient httpClient;
  private SitePropertyService sitePropertyService;
  private TelegramRepository telegramRepository;

  TelegramFotelBot(SitePropertyService sitePropertyService, TelegramRepository telegramRepository) {
    this.sitePropertyService = sitePropertyService;
    this.telegramRepository = telegramRepository;
  }

  @PostConstruct
  void init() {
    httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
  }

  @Override
  public void onUpdateReceived(Update update) {

    Long userId = Long.valueOf(update.getMessage().getFrom().getId());

    List<TelegramUser> telegramUserList = telegramRepository.list();

    Optional<TelegramUser> telegramUser = telegramUserList.stream().filter(user -> user.getUserId().equals(userId)).findFirst();

    if (telegramUser.isEmpty()) {
      telegramRepository.save(new TelegramUser(userId));
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
      sendMassage(chatId, EmojiParser.parseToUnicode(TelegramConstant.WHITE_CHECK_MARK_RESTART));
    } catch (Exception e) {
      sendMassage(chatId, EmojiParser.parseToUnicode(TelegramConstant.ERROR));
      e.printStackTrace();
    }
  }


  public void buildStatic(Long chatId) {
    try {
      Process proc = Runtime.getRuntime().exec(TelegramConstant.BUILD_STATIC_COMMAND + getBotToken() + " " + chatId);
      proc.waitFor();
    } catch (Exception e) {
      sendMassage(chatId, EmojiParser.parseToUnicode(TelegramConstant.ERROR));
      e.printStackTrace();
    }
  }

  public void scheduledCheckStatus() {
    List<TelegramUser> telegramUserList = new ArrayList<>(telegramRepository.list());

    if (!telegramUserList.isEmpty()) {
      int responseCode = checkStatusServer();
      if (responseCode != 200) {
        telegramUserList.stream().filter(TelegramUser::isAccess).forEach(telegramUser -> sendMassage(telegramUser.getUserId(), EmojiParser.parseToUnicode(String.format(TelegramConstant.ERROR_RESPONSE_MASSAGE, responseCode))));
      }
    }
  }

  public void checkSendStatus(Long chatId) {
    int responseCode = checkStatusServer();
    if (responseCode == 200) {
      sendMassage(chatId, EmojiParser.parseToUnicode(TelegramConstant.SUCCESS_RESPONSE_MASSAGE));
    } else {
      sendMassage(chatId, EmojiParser.parseToUnicode(String.format(TelegramConstant.ERROR_RESPONSE_MASSAGE, responseCode)));
    }
  }

  public int checkStatusServer() {

    HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(TelegramConstant.CHECK_URL))
            .build();

    HttpResponse<String> response = null;
    try {
      response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      return response.statusCode();
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
      return 999;
    }
  }


  public synchronized void setButtons(SendMessage sendMessage) {
    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
    sendMessage.setReplyMarkup(replyKeyboardMarkup);
    replyKeyboardMarkup.setSelective(true);
    replyKeyboardMarkup.setResizeKeyboard(true);
    replyKeyboardMarkup.setOneTimeKeyboard(false);

    List<KeyboardRow> keyboard = new ArrayList<>();

    KeyboardRow keyboardFirstRow = new KeyboardRow();
    keyboardFirstRow.add(EmojiParser.parseToUnicode(TelegramConstant.INFORMATION_SOURCE_STATUS));

    KeyboardRow keyboardSecondRow = new KeyboardRow();
    keyboardSecondRow.add(EmojiParser.parseToUnicode(TelegramConstant.WHITE_CHECK_MARK_RESTART));

    KeyboardRow keyboardThirdRow = new KeyboardRow();
    keyboardSecondRow.add(EmojiParser.parseToUnicode(TelegramConstant.BUILD_STATIC));

    keyboard.add(keyboardFirstRow);
    keyboard.add(keyboardSecondRow);
    keyboard.add(keyboardThirdRow);
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
