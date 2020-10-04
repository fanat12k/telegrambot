package telegram.bot.util;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import static telegram.bot.service.telegram.model.TelegramConstant.HTTP_CLIENT_AGENT;

public class HttpClientUtil {
  public static CloseableHttpClient buildHttpClient(KeyStore trustStore) {

    if (trustStore == null) {
      return buildHttpClient(false);
    }
    SSLContext sslcontext = null;
    try {
      sslcontext = SSLContexts.custom()
              .loadTrustMaterial(trustStore, new TrustSelfSignedStrategy())
              .build();
    } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
      e.printStackTrace();
    }

    if(sslcontext == null){
      return buildHttpClient(false);
    }

    RequestConfig rc = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
    HttpClientBuilder builder = HttpClients.custom().setSSLSocketFactory(new SSLConnectionSocketFactory(sslcontext, new DefaultHostnameVerifier())).setDefaultRequestConfig(rc).setUserAgent(HTTP_CLIENT_AGENT);

    return builder.build();
  }

  private static CloseableHttpClient buildHttpClient(boolean redirect) {
    RequestConfig rc = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
    HttpClientBuilder builder = HttpClients.custom().setDefaultRequestConfig(rc)
            .setUserAgent(HTTP_CLIENT_AGENT);

    if (redirect) {
      builder.setRedirectStrategy(new LaxRedirectStrategy());
    }

    return builder.build();
  }
}
