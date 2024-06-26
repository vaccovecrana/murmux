package io.vacco.mrx;

import com.github.mizosoft.methanol.*;
import examples.LoggerInit;
import io.vacco.murmux.Murmux;
import io.vacco.murmux.http.*;
import io.vacco.murmux.middleware.*;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;
import org.slf4j.*;

import java.nio.file.Paths;

import static java.net.http.HttpResponse.BodyHandlers.*;
import static com.github.mizosoft.methanol.MutableRequest.*;
import static org.junit.Assert.*;
import static io.vacco.murmux.http.MxExchanges.*;
import static io.vacco.murmux.http.MxStatus.*;
import static j8spec.J8Spec.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class MxMiddlewareTest {

  public static Murmux mx;
  public static final Methanol client = Methanol
    .newBuilder()
    .baseUri("http://localhost:8081")
    .build();

  static {
    LoggerInit.apply();
    beforeAll(() -> mx = new Murmux("localhost")
      .rootHandler(xc -> xc.withStatus(_204).commit())
      .listen(8081));

    final Logger log = LoggerFactory.getLogger(MxMiddlewareTest.class);

    it("Rejects routers with invalid paths",
      c -> c.expected(IllegalArgumentException.class),
      () -> new MxRouter()
        .put("/put", xc -> {})
        .patch("/patch", xc -> {})
        .delete("/delete", xc -> {})
        .connect("/connect", xc -> {})
        .options("/options", xc -> {})
        .trace("/trace", xc -> {})
        .head("/head", xc -> {})
        .any("*/some/weird/path", xc -> {})
    );

    it("Tracks cookie based sessions", () -> {
      var cookieName = "sessionId";
      var loggedOut = "Logged out";
      var active = "Session active";

      mx.rootHandler(
        new MxRouter()
          .get("/logout", xc -> xc.commitText(loggedOut))
          .get("/session", new MxMemory<>(
            cookieName, 3000,
            () -> new MxSession<>().withData(45L),
            nxc -> {
              @SuppressWarnings("unchecked")
              var session = (MxSession<Long>) nxc.getAttachment(MxSession.class);
              assertEquals(45L, session.data.longValue());
              nxc.commitText(active);
            },
            ixc -> ixc.withRedirect("/logout").commit()
          ))
      );

      var res = client.send(GET("/session"), ofString());
      assertEquals(_200.code, res.statusCode());
      assertEquals(res.body(), active);

      var oCookie = res.headers().firstValue(HSetCookie);
      assertTrue(oCookie.isPresent());

      var cookies = MxExchanges.parseCookiesTxt(oCookie.get());
      log.info("Raw: {}", oCookie.get());
      log.info("Cookies: {}", cookies);

      Thread.sleep(4000);

      var res1 = client.send(
        GET("/session").header(HCookie, cookies.get(cookieName).toString()),
        ofString()
      );
      assertEquals(_302.code, res1.statusCode());
      var oLoc = res1.headers().firstValue(HLocation);
      assertTrue(oLoc.isPresent());
      assertEquals(oLoc.get(), "/logout");
    });

    it("Accepts CORS requests", () -> {
      var url = "http://localhost:8081";
      mx.rootHandler(new MxCors(
        new MxCors.Options()
          .withMethods(MxMethod.OPTIONS, MxMethod.GET, MxMethod.POST)
          .withMaxAge(120)
          .withOrigin(url),
        xc -> xc.commitText("CORS API request")
      ));
      assertEquals(_204.code,
        client.send(
          MutableRequest.create("/").method("OPTIONS", BodyPublishers.noBody()),
          ofString()
        ).statusCode()
      );
      assertEquals(_200.code, client.send(GET("/"), ofString()).statusCode());
    });

    it("Accepts static content requests", () -> {
      mx.rootHandler(
        new MxRouter()
          .withName("Robert Paulson")
          .prefix("/src", new MxStatic(MxStatic.Origin.FileSystem, Paths.get(".")))
          .prefix("/murmux.png", new MxStatic(MxStatic.Origin.Classpath, Paths.get("/")))
          .noMatch(
            xc -> xc
              .withStatus(_502)
              .withBody(MxMime.txt, Paths.get("./src/test/resources/flamingo.txt"))
              .commit()
          )
      );
      assertEquals(_200.code, client.send(
        GET("/src/test/resources/murmux.png"), ofByteArray()
      ).statusCode());
      assertEquals(_200.code, client.send(
        GET("/murmux.png"), ofByteArray()
      ).statusCode());
      assertEquals(_502.code, client.send(
        GET("/nowhere"), ofString()
      ).statusCode());
    });

    it("Resolves media types for unknown static filesystem content", () ->{
      mx.rootHandler(
        new MxRouter().get(
          "/src/test/resources/data.map",
          new MxStatic(MxStatic.Origin.FileSystem, Paths.get("."))
            .withNoTypeResolver((p, o) -> MxMime.bin.type)
        )
      );
      assertEquals(_200.code, client.send(
        GET("/src/test/resources/data.map"), ofByteArray()
      ).statusCode());
    });

    it("Resolves media types for unknown static classpath content", () ->{
      mx.rootHandler(
        new MxRouter().get(
          "/data.map",
          new MxStatic(MxStatic.Origin.Classpath, Paths.get("/"))
            .withNoTypeResolver((p, o) -> MxMime.bin.type)
        )
      );
      assertEquals(_200.code, client.send(
        GET("/data.map"), ofByteArray()
      ).statusCode());
    });

    it("Stops the server", () -> mx.stop());
  }
}
