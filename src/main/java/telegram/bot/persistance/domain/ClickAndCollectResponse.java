package telegram.bot.persistance.domain;

public class ClickAndCollectResponse {
  private String status;
  private String target;

  public String isStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }
}
