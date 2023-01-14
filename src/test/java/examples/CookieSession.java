package examples;

import io.vacco.murmux.Murmux;
import io.vacco.murmux.http.MxSessionCookie;
import io.vacco.murmux.middleware.MxCookieSession;
import io.vacco.murmux.middleware.MxMiddleware;

public class CookieSession {
  public static void main(String[] args) {
    LoggerInit.apply();
    new Murmux() {
      {
        // You should use a meaningless cookie name for several security reasons, here f3v4.
        // You can also specify the maximum age of the cookie from the creation date and the file
        // types which are actually allowed.
        use(MxMiddleware.cookieSession("f3v4", 9000));

        // To use a session cookie we need to get the data from the middleware:
        get(
          "/session",
          (req, res) -> {
            // Retrieve session cookie.
            MxSessionCookie sessionCookie = req.getMiddlewareContent(MxCookieSession.class);
            int count;
            // Check if the data is null, we want to implement a simple counter.
            if (sessionCookie.getData() == null) {
              // Set the default data to 1 (first request with this session cookie)
              count = (Integer) sessionCookie.setData(1);
            } else {
              // Now we know that the cookie has an integer as data property, increase it
              count = (Integer) sessionCookie.setData((Integer) sessionCookie.getData() + 1);
            }
            // Send an info message
            res.send("Session cookie request called " + count + " times.");
          });
        // Start server
        listen(8080);
      }
    };
  }
}
