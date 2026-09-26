package io.vacco.mrx;

import com.github.mizosoft.methanol.*;
import examples.LoggerInit;
import io.vacco.murmux.Murmux;
import io.vacco.murmux.http.*;
import io.vacco.murmux.middleware.MxRouter;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;

import java.util.*;

import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static com.github.mizosoft.methanol.MutableRequest.*;
import static io.vacco.murmux.http.MxStatus.*;
import static j8spec.J8Spec.*;
import static org.junit.Assert.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class MxErrorTest {

  public static Murmux mx;
  public static final int port = 8099;

  static final List<String> serverErrors = Collections.synchronizedList(new ArrayList<>());

  public static final Methanol client = Methanol
    .newBuilder()
    .baseUri("http://localhost:" + port)
    .build();

  private static void capture(String fmt, Object[] args) {
    var sb = new StringBuilder(fmt == null ? "" : fmt);
    for (var a : args) {
      if (a instanceof Throwable) {
        var t = (Throwable) a;
        sb.append(" | ")
          .append(t.getClass().getName())
          .append(": ")
          .append(t.getMessage());
      }
    }
    serverErrors.add(sb.toString());
  }

  static {
    LoggerInit.apply();
    MxLog.setErrorLogger(MxErrorTest::capture);
    MxLog.setWarnLogger((fmt, args) -> {});

    beforeAll(() -> mx = new Murmux("localhost")
      .rootHandler(xc -> xc.withStatus(_204).commit())
      .listen(port));

    it("Returns 500 when the root handler throws before commit", () -> {
      mx.rootHandler(xc -> {
        throw new IllegalStateException("boom");
      });
      assertEquals(_500.code, client.send(GET("/"), ofString()).statusCode());
    });

    it("Returns 404 when no route matches", () -> {
      mx.rootHandler(new MxRouter()
        .get("/only", xc -> xc.commitText("ok")));
      assertEquals(_404.code, client.send(GET("/missing"), ofString()).statusCode());
    });

    it("Keeps the committed response when the handler throws afterwards", () -> {
      mx.rootHandler(xc -> {
        xc.commitText("done");
        throw new IllegalStateException("after commit");
      });
      var res = client.send(GET("/"), ofString());
      assertEquals(_200.code, res.statusCode());
      assertEquals("done", res.body());
      assertFalse(
        "Server must not attempt to resend an already committed response",
        serverErrors.stream().anyMatch(s -> s.contains("headers already sent"))
      );
    });

    it("Returns 500 when static content is missing", () -> {
      mx.rootHandler(
        new MxRouter().prefix(
          "/src",
          new io.vacco.murmux.middleware.MxStatic(
            io.vacco.murmux.middleware.MxStatic.Origin.FileSystem,
            java.nio.file.Paths.get(".")
          )
        )
      );
      assertEquals(_500.code, client.send(GET("/src/nowhere.txt"), ofString()).statusCode());
    });

    it("Stops the server", () -> mx.stop());
  }
}
