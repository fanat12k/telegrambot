package telegram.bot.persistance.domain;

public class TelegramUser {
  private Long userId;
  private boolean access;

  public TelegramUser(Long userId) {
    this.userId = userId;
  }

  public TelegramUser(){

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
