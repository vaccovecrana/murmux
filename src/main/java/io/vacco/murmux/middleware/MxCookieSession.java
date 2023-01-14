package io.vacco.murmux.middleware;

import io.vacco.murmux.filter.MxFilterTask;
import io.vacco.murmux.http.MxCookie;
import io.vacco.murmux.http.MxHandler;
import io.vacco.murmux.http.MxSessionCookie;
import io.vacco.murmux.http.MxRequest;
import io.vacco.murmux.http.MxResponse;
import io.vacco.murmux.util.MxIo;

import java.util.concurrent.ConcurrentHashMap;

/**
 * A middleware to create in-memory cookie-sessions.
 */
public class MxCookieSession implements MxHandler, MxFilterTask {

  private final ConcurrentHashMap<String, MxSessionCookie> cookies = new ConcurrentHashMap<>();
  private final String cookieName;
  private final long maxAge;

  MxCookieSession(String cookieName, long maxAge) {
    this.cookieName = cookieName;
    this.maxAge = maxAge;
  }

  @SuppressWarnings("SuspiciousMethodCalls")
  @Override
  public void handle(MxRequest req, MxResponse res) {
    MxCookie cookie = req.getCookie(cookieName);

    if (cookie != null && cookies.containsKey(cookie.getValue())) {
      req.addMiddlewareContent(this, cookies.get(cookie.getValue()));
    } else {
      String token;

      do {
        token = MxIo.randomToken(32, 16);
      } while (cookies.contains(token));

      cookie = new MxCookie(cookieName, token).setMaxAge(maxAge);
      res.setCookie(cookie);

      MxSessionCookie sessionCookie = new MxSessionCookie(maxAge);
      cookies.put(token, sessionCookie);

      req.addMiddlewareContent(this, sessionCookie);
    }
  }

  @Override
  public void onStart() {}

  @Override
  public void onStop() {
    cookies.clear();
  }

  @Override
  public long getDelay() {
    return 60000;
  }

  @Override
  public void onUpdate() {
    long current = System.currentTimeMillis();
    cookies.forEach(
      (cookieHash, cookie) -> {
        if (current > cookie.getCreated() + cookie.getMaxAge()) {
          cookies.remove(cookieHash);
        }
      });
  }
}
