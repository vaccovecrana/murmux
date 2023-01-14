package examples;

import io.vacco.murmux.Murmux;

public class UrlQuery {
  public static void main(String[] args) {
    LoggerInit.apply();
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
  }
}
