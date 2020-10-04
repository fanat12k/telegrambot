package telegram.bot.util;

import io.micronaut.core.util.StringUtils;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class EncryptionUtil {
  public static String encodeHmacSHA1(String key, String data) throws Exception {
    if (StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(data)) {
      Mac sha256HMAC = Mac.getInstance("HmacSHA1");
      SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA1");
      sha256HMAC.init(secretKey);
      return Hex.encodeHexString(sha256HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    } else {
      return "";
    }
  }
}
