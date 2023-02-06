package examples;

import io.vacco.murmux.Murmux;

public class Basic {
  public static void main(String[] args) {
    LoggerInit.apply();
    new Murmux()
      .rootHandler(xc -> xc.commitText("Hello world"))
      .listen(8080);
  }
}
