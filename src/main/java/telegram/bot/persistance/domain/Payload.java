package telegram.bot.persistance.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class Payload {
  private String selectedService = "fetchlocation";
  private String customerView = "desktop";
  private String locale = "pl_PL";
  private String selectedServiceValue;
  private String slId = "1241241241";
  @JsonProperty("articles")
  private List<Product> productList = new ArrayList<>();

  public String getSelectedService() {
    return selectedService;
  }

  public String getCustomerView() {
    return customerView;
  }

  public String getLocale() {
    return locale;
  }

  public String getSelectedServiceValue() {
    return selectedServiceValue;
  }

  public void setSelectedServiceValue(String selectedServiceValue) {
    this.selectedServiceValue = selectedServiceValue;
  }

  public String getSlId() {
    return slId;
  }

  public void setSlId(String slId) {
    this.slId = slId;
  }

  public List<Product> getProductList() {
    productList.add(new Product());
    return productList;
  }

  static class Product {
    private Long articleNo = 60149673L;
    private Integer count = 1;

    public Long getArticleNo() {
      return articleNo;
    }

    public Integer getCount() {
      return count;
    }
  }
}

