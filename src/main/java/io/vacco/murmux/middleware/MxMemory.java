package io.vacco.murmux.middleware;

import io.vacco.murmux.http.*;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Cookie based, in-memory session handler.
 *
 * @param <T> the backend session type.
 */
public class MxMemory<T> implements MxHandler {

  private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

  private final SecureRandom sr = new SecureRandom();
  private final Map<String, MxSession<T>> sessions = new ConcurrentHashMap<>();

  private final String cookieName;
  private final long maxAgeMs;
  private final MxHandler next, invalidate;
  private final Supplier<MxSession<T>> sessionSupplier;

  public MxMemory(String cookieName, long maxAgeMs,
                  Supplier<MxSession<T>> sessionSupplier,
                  MxHandler next, MxHandler invalidate) {
    this.cookieName = Objects.requireNonNull(cookieName);
    this.maxAgeMs = maxAgeMs;
    this.sessionSupplier = Objects.requireNonNull(sessionSupplier);
    this.next = Objects.requireNonNull(next);
    this.invalidate = Objects.requireNonNull(invalidate);
  }

  public String bytesToHex(byte[] bytes) {
    char[] hexChars = new char[bytes.length * 2];
    for (int j = 0; j < bytes.length; j++) {
      int v = bytes[j] & 0xFF;
      hexChars[j * 2] = HEX_ARRAY[v >>> 4];
      hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
    }
    return new String(hexChars);
  }

  public String randomToken(int byteLength) {
    byte[] token = new byte[byteLength];
    sr.nextBytes(token);
    return bytesToHex(token);
  }

  @Override public void handle(MxExchange xc) {
    var cookie = xc.cookies.get(cookieName);
    MxSession<T> ses;
    if (cookie != null && sessions.containsKey(cookie.value)) {
      ses = sessions.get(cookie.value);
      var diffMs = System.currentTimeMillis() - ses.createdUtcMs;
      if (diffMs >= maxAgeMs) {
        // TODO set cookie invalidation cookie properties for browser.
        // TODO https://stackoverflow.com/a/5285982/491160
        sessions.remove(cookie.value);
        invalidate.handle(xc);
        return;
      }
    } else {
      var nowUtcMs = System.currentTimeMillis();
      var token = randomToken(32);
      cookie = new MxCookie(cookieName, token).withMaxAge(maxAgeMs);
      ses = sessionSupplier.get().withCreatedUtcMs(nowUtcMs);
      xc.withCookie(cookie);
      sessions.put(token, ses);
    }
    xc.putAttachment(ses);
    next.handle(xc);
  }
}
