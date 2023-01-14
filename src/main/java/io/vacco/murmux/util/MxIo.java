package io.vacco.murmux.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.security.SecureRandom;

public final class MxIo {

  private MxIo() {}

  /**
   * Write all data from an InputStream in a String
   *
   * @param is The source InputStream
   * @return The data as string
   */
  public static String streamToString(InputStream is) {
    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(is));
      StringBuilder sb = new StringBuilder();
      String line;

      while ((line = br.readLine()) != null) {
        sb.append(line);
      }

      return sb.toString();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Returns the MIME-Type of a file.
   *
   * @param file The file.
   * @return The MIME-Type.
   */
  public static MxMediaType getContentType(Path file) {
    String ex = getExtension(file);
    MxMediaType contentType = MxMediaType.getByExtension(ex);

    if (contentType == null) {
      return MxMediaType._bin;
    }

    return contentType;
  }

  /**
   * Generates a random token with SecureRandom
   *
   * @param byteLength The token length
   * @param radix      The base
   * @return A token with the base of radix
   */
  public static String randomToken(int byteLength, int radix) {
    SecureRandom secureRandom = new SecureRandom();
    byte[] token = new byte[byteLength];
    secureRandom.nextBytes(token);
    return new BigInteger(1, token).toString(radix); // hex encoding
  }

  /**
   * @return Your ip.
   * @throws UnknownHostException If resolving fails
   */
  public static String getYourIp() throws UnknownHostException {
    return Inet4Address.getLocalHost().getHostAddress();
  }

  /**
   * Extract the extension from the file.
   *
   * @param file The file.
   * @return The extension.
   */
  public static String getExtension(Path file) {
    String path = file.getFileName().toString();
    int index = path.lastIndexOf('.') + 1;

    // No extension present
    if (index == 0) {
      return null;
    }

    return path.substring(index);
  }
}
