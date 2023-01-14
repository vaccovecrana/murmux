package io.vacco.murmux.middleware;

import java.io.IOException;

/**
 * Class which provides middleware.
 */
public final class MxMiddleware {

  private MxMiddleware() {}

  /**
   * Create a new cookie-session middleware. You can access and edit to session-cookie data via
   * request.getMiddlewareContent('SessionCookie').
   *
   * @param cookieName A name for the session-cookie, it's recommend to use NOT SID for security
   *                   reasons
   * @param maxAge     A max-age for the cookie
   * @return New CookieSession
   */
  public static MxCookieSession cookieSession(String cookieName, long maxAge) {
    return new MxCookieSession(cookieName, maxAge);
  }

  /**
   * This class serves an entire folder which can contains static file for your web application, You
   * can use <code>StaticOptions</code> to add som configurations.
   *
   * @param directoryPath The root directory
   * @return A fresh FileProvider
   * @throws IOException If path cannot be found or something like that
   */
  public static MxFileProvider statics(String directoryPath) throws IOException {
    return new MxFileProvider(directoryPath, new MxFileProviderOptions());
  }

  /**
   * This class serves an entire folder which can contains static file for your web application, You
   * can use <code>StaticOptions</code> to add som configurations.
   *
   * @param directoryPath The root directory
   * @param staticOptions Optional options for the file serving.
   * @return A fresh FileProvider
   * @throws IOException If path cannot be found or something like that
   */
  public static MxFileProvider statics(String directoryPath, MxFileProviderOptions staticOptions)
    throws IOException {
    return new MxFileProvider(directoryPath, staticOptions);
  }

  public static MxCors cors(MxCorsOptions options) {
    return new MxCors(options);
  }

  public static MxCors cors() {
    return new MxCors(new MxCorsOptions());
  }
}
