package telegram.bot.persistance.domain;

public class TelegramUser {
  private Long userId;
  private boolean access;
  private String userName;

  public TelegramUser(Long userId, String userName) {
    this.userId = userId;
    this.userName = userName;
  }

  public TelegramUser() {

  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public boolean isAccess() {
    return access;
  }

  public void setAccess(boolean access) {
    this.access = access;
  }
}
