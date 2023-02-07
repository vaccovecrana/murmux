package io.vacco.mrx;

import com.ultraspatial.httpsender.*;
import examples.LoggerInit;
import io.vacco.murmux.Murmux;
import io.vacco.murmux.http.*;
import io.vacco.murmux.middleware.*;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;
import org.slf4j.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.Assert.*;
import static io.vacco.murmux.http.MxStatus.*;
import static j8spec.J8Spec.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class MxMiddlewareTest {

  public static Murmux mx;

  static {
    LoggerInit.apply();
    beforeAll(() -> mx = new Murmux("localhost")
      .rootHandler(xc -> xc.withStatus(_204).commit())
      .listen(8080));

    if (GraphicsEnvironment.isHeadless()) {
      MxCoreTest.sleep.apply(25000L);
    } else {
      MxCoreTest.sleep.apply(3000L);
    }

    final Logger log = LoggerFactory.getLogger(MxMiddlewareTest.class);

    // TODO https://github.com/alasdairg/http-sender/issues/2
    Field fHeaders;
    try {
      fHeaders = Response.class.getDeclaredField("headers");
      fHeaders.setAccessible(true);
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    }

    it("Tracks cookie based sessions", () -> {

      var cookieName = "sessionId";
      var loggedOut = "Logged out";
      var active = "Session active";
      var sessUrl = "http://localhost:8080/session";

      mx.rootHandler(
        new MxRouter()
          .get("/logout", xc -> xc.commitText(loggedOut))
          .get("/session", new MxMemory<>(
            cookieName, 3000,
            () -> new MxSession<>().withData(45L),
            nxc -> {
              MxSession<Long> session = nxc.loadMiddlewareContent(MxMemory.class);
              assertEquals(45L, session.data.longValue());
              nxc.commitText(active);
            },
            ixc -> ixc.withRedirect("/logout").commit()
          ))
      );

      var getSession = new Get(sessUrl);
      var cookieA = new MxCookie[1];
      try (var res = getSession.execute()) {
        assertEquals(_200.code, res.getResponseCode());
        assertEquals(res.bodyAsString(), active);
        @SuppressWarnings("unchecked")
        var headers = (Map<String, HeaderValues>) fHeaders.get(res);
        var raw = headers.get(null).getValues().get(0);
        var cookies = MxExchanges.parseCookiesTxt(raw);
        log.info("Raw: {}", raw);
        log.info("Cookies: {}", cookies);
        cookieA[0] = cookies.get(cookieName);
      }

      MxCoreTest.sleep.apply(4000L);

      var getSession2 = new Get(sessUrl).header(MxExchanges.HCookie, cookieA[0].toString());
      try (var res = getSession2.execute()) {
        assertEquals(_200.code, res.getResponseCode());
        assertEquals(res.bodyAsString(), loggedOut);
      }
    });

    it("Accepts CORS requests", () -> {
      var url = "http://localhost:8080";
      mx.rootHandler(new MxCors(
        new MxCors.Options()
          .withMethods(MxMethod.OPTIONS, MxMethod.GET, MxMethod.POST)
          .withMaxAge(120)
          .withOrigin(url),
        xc -> xc.commitText("CORS API request")
      ));
      var opts = new Options(url);
      try (var res = opts.execute()) {
        assertEquals(_204.code, res.getResponseCode());
      }
      var get = new Get(url);
      try (var res = get.execute()) {
        assertEquals(_200.code, res.getResponseCode());
      }
    });

    it("Accepts static content requests", () -> {
      mx.rootHandler(
        new MxRouter()
          .prefix("/src", new MxStatic(MxStatic.Origin.FileSystem, Paths.get(".")))
          .prefix("/murmux.png", new MxStatic(MxStatic.Origin.Classpath, Paths.get("/")))
      );
      var getFs = new Get("http://localhost:8080/src/test/resources/murmux.png");
      try (var res = getFs.execute()) {
        assertEquals(_200.code, res.getResponseCode());
      }
      var getCp = new Get("http://localhost:8080/murmux.png");
      try (var res = getCp.execute()) {
        assertEquals(_200.code, res.getResponseCode());
      }
    });

    it("Stops the server", () -> mx.stop());
  }
}
