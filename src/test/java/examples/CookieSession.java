package examples;

import io.vacco.murmux.Murmux;
import io.vacco.murmux.middleware.*;

public class CookieSession {
  public static void main(String[] args) {
    new Murmux().rootHandler(
      new MxRouter()
        .get("/logout", xc -> xc.commitText("Session invalidated (you're logged out)"))
        .get("/session",
          new MxMemory<>(
            "f3v4", 9000,
            () -> new MxSession<Integer>().withData(0),
            xc -> {
              // Retrieve session using cookie value.
              MxSession<Integer> session = xc.loadMiddlewareContent(MxMemory.class);
              // We want to implement a simple counter.
              session.withData(session.data + 1);
              // Send an info message
              xc.commitText("Session cookie request called " + session.data + " times.");
            },
            xc -> xc.withRedirect("/logout").commit()
          )
        )
    ).listen(8080);
  }
}
