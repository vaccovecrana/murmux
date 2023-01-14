package examples;

import io.vacco.murmux.Murmux;
import io.vacco.murmux.middleware.MxCorsOptions;
import io.vacco.murmux.middleware.MxMiddleware;

public class CorsMiddleware {
  public static void main(String[] args) {
    LoggerInit.apply();
    var app =
      new Murmux() {
        {
          get(
            "/posts",
            (req, res) -> {
              String page = req.getQuery("page"); // Contains '12'
              String from = req.getQuery("from"); // Contains 'John'
              res.send("Page: " + page + ", from: " + from); // Send: "Page: 12, from: John"
            });
          // Start server
          listen(8080);
        }
      };

    var corsOptions = new MxCorsOptions();
    corsOptions.setOrigin("https://mypage.com");
    corsOptions.setAllowCredentials(true);
    corsOptions.setHeaders(new String[]{"GET", "POST"});
    corsOptions.setFilter(req -> false); // Custom validation if cors should be applied

    app.use(MxMiddleware.cors(corsOptions));
  }
}
