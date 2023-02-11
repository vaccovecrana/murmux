package io.vacco.mrx;

import com.ultraspatial.httpsender.*;
import examples.LoggerInit;
import io.vacco.murmux.Murmux;
import io.vacco.murmux.http.*;
import io.vacco.murmux.middleware.MxRouter;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;
import org.slf4j.*;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.function.*;

import static java.lang.String.format;
import static io.vacco.murmux.http.MxStatus.*;
import static io.vacco.murmux.http.MxMime.*;
import static j8spec.J8Spec.*;
import static org.junit.Assert.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class MxHandlerTest {

  public static String httpLocalHost = "http://localhost:8080";

  public static Murmux mx;
  public static String hello = "Hello world";
  public static String welcome = "Welcome!";

  public static String what = "What?";
  public static String quote = "Mirrors Are More Fun Than Television";

  public static String cookieName = "my-cookie";
  public static String cookieSet = "Your cookie has been set";
  public static String cookieClient = "yummy_cookie=choco; tasty_cookie=strawberry";

  public static String authValue = "Bearer 0x1234FABC4DFE9";

  public static String paramFormat = "format";
  public static String paramSize = "size";
  public static String paramUsername = "username";
  public static String paramPassword = "password";
  public static String username = "gopher";
  public static String password = "123456";

  public static String duckBookJson = "{\"id\": 999999,\"title\": \"What about my Shreddahh?\"}";

  public static String htmlBooks = "<!DOCTYPE html>\n" +
    "<html>\n" +
    "<body>\n" +
    "<h2>Books</h2>\n" +
    "<ul>\n" +
    "  <li>Book 1</li>\n" +
    "  <li>Book 2</li>\n" +
    "  <li>Book 3</li>\n" +
    "</ul>\n" +
    "</body>\n" +
    "</html>";

  static {
    LoggerInit.apply();
    beforeAll(() -> mx = new Murmux()
      .rootHandler(xc -> xc.withStatus(_204).commit())
      .listen(8080));

    if (GraphicsEnvironment.isHeadless()) {
      MxCoreTest.sleep.apply(25000L);
    } else {
      MxCoreTest.sleep.apply(3000L);
    }

    Logger log = LoggerFactory.getLogger(MxHandlerTest.class);

    var logRequest = (Consumer<MxExchange>) xc -> {
      log.info(
        "[f: {}, st: {}, xhr: {}]",
        MxExchanges.isFresh(xc),
        MxExchanges.isStale(xc),
        MxExchanges.isXhr(xc)
      );
      log.info("[{} {}]", xc.method, xc.getHost());
      log.info("[{} {}]", xc.getProtocol(), xc.getPath());
      log.info("[{} {}]", xc.getUserAgent(), xc.getURI());
    };

    it("Accepts requests on any path and method", () -> {
      mx.rootHandler(xc -> xc.commitText(hello));
      var get = new Get(httpLocalHost);
      try (var res = get.execute()) {
        assertEquals(hello, res.bodyAsString());
      }
    });

    it("Accepts routing requests", () -> {
      mx.rootHandler(new MxRouter()
        .post("/book", xc -> {
          var f = Paths.get("./build/duck.json");
          MxExchanges.copyRequestBodyTo(xc, f);
          MxExchanges.copyRequestBodyTo(xc, f);
          xc.withStatus(_201).commit();
        })
        .post("/login", xc -> {
          assertNotNull(xc.getFormParam(paramUsername));
          assertNotNull(xc.getFormParam(paramPassword));
          xc.withRedirect("/welcome").commit();
        })
        .get("/welcome", xc -> {
          logRequest.accept(xc);
          xc.commitText(welcome);
        })
        .get("/logo.png", xc -> {
          logRequest.accept(xc);
          assertNotNull(xc.getQueryParam(paramSize));
          assertNotNull(xc.getQueryParam(paramFormat));
          xc.withBody(Paths.get("./src/test/resources/murmux.png"))
            .withAttachmentFile("murmux.png")
            .withStatus(_200)
            .commit();
        })
      );

      var post = new Post(format("%s/book", httpLocalHost))
        .contentType(json.type)
        .requestBody(duckBookJson);
      try (var res = post.execute()) {
        assertEquals(_201.code, res.getResponseCode());
      }
      var login = new FormPost(format("%s/login", httpLocalHost))
        .formField(paramUsername, username)
        .formField(paramPassword, password);
      try (var res = login.execute()) {
        assertEquals(_200.code, res.getResponseCode());
      }
      var logo = new Get(format("%s/logo.png", httpLocalHost))
        .queryParam(paramSize, "800")
        .queryParam(paramFormat, jpeg.extensions[0])
        .header(MxExchanges.HCacheControl, "no-cache");
      try (var res = logo.execute()) {
        var baos = new ByteArrayOutputStream();
        res.bodyAsStream().transferTo(baos);
        log.info("[{}]", baos.size());
        assertEquals(_200.code, res.getResponseCode());
      }
    });

    it("Accepts parameter path requests", () -> {
      mx.rootHandler(new MxRouter()
        .get("/book/{bookId}", xc -> {
          assertNotNull(xc.getPathParam("bookId"));
          xc.commitJson(duckBookJson);
        })
        .any("/momo",
          xc -> xc.withStatus(_418).withBody(txt, what).commit()
        )
        .get("/quote",
          xc -> xc.withStatus(_417)
            .withBody(quote.getBytes(StandardCharsets.UTF_8))
            .commit()
        )
      );

      var getBookId = new Get(format("%s/book/999999", httpLocalHost));
      try (var res = getBookId.execute()) {
        var body = res.bodyAsString();
        assertEquals(duckBookJson, body);
        assertEquals(_200.code, res.getResponseCode());
        log.info(body);
      }
      var putWhat = new Put(format("%s/momo", httpLocalHost));
      try (var res = putWhat.execute()) {
        var body = res.bodyAsString();
        assertEquals(what, body);
        assertEquals(_418.code, res.getResponseCode());
        log.info(body);
      }
      var getQuote = new Get(format("%s/quote", httpLocalHost));
      try (var res = getQuote.execute()) {
        var body = res.bodyAsString();
        assertEquals(_417.code, res.getResponseCode());
        log.info(body);
      }
    });

    it("Rejects prefix route paths with parameters",
      c -> c.expected(IllegalArgumentException.class),
      () -> mx.rootHandler(new MxRouter()
        .prefix("/momo/{lolId}", xc -> xc.withStatus(_200)))
    );

    it("Accepts requests on prefix routes", () -> {
      mx.rootHandler(new MxRouter()
        .prefix("/api",
          new MxRouter()
            .get("/api/book/list", xc -> {
              logRequest.accept(xc);
              xc.commitJson(duckBookJson);
            })
            .get("/api/book/{bookId}", xc -> xc.withStatus(_200).withBody(json, duckBookJson).commit())
        )
        .prefix("/ui",
          new MxRouter()
            .get("/ui/book/list", xc -> xc.commitHtml(htmlBooks))
        )
      );

      var getBooks = new Get(format("%s/api/book/list", httpLocalHost))
        .header(MxExchanges.HXRequestedWith, "XMLHttpRequest");
      try (var res = getBooks.execute()) {
        var body = res.bodyAsString();
        assertEquals(duckBookJson, body);
        assertEquals(_200.code, res.getResponseCode());
        log.info(body);
      }
      var getBookId = new Get(format("%s/api/book/8888", httpLocalHost));
      try (var res = getBookId.execute()) {
        var body = res.bodyAsString();
        assertEquals(duckBookJson, body);
        assertEquals(_200.code, res.getResponseCode());
        log.info(body);
      }
      var getUiBookList = new Get(format("%s/ui/book/list", httpLocalHost));
      try (var res = getUiBookList.execute()) {
        var body = res.bodyAsString();
        assertEquals(htmlBooks, body);
        assertEquals(_200.code, res.getResponseCode());
        log.info(body);
      }
    });

    it("Generates cookies", () -> {
      var cookie = new MxCookie(cookieName, hello);
      assertEquals(cookie, cookie);
      mx.rootHandler(xc -> xc.withStatus(_200)
        .withCookie(cookie)
        .withBody(txt, cookieSet)
        .commit()
      );
      var getCookie = new Get(format("%s/give-me-cookies", httpLocalHost));
      try (var res = getCookie.execute()) {
        var body = res.bodyAsString();
        assertEquals(cookieSet, body);
        assertEquals(_200.code, res.getResponseCode());
        log.info(body);
      }
    });

    it("Reads cookies", () -> {
      mx.rootHandler(xc -> xc.commitText(xc.cookies.toString()));
      var getCookie = new Get(format("%s/give-me-cookies", httpLocalHost))
        .header(MxExchanges.HCookie, cookieClient);
      try (var res = getCookie.execute()) {
        var body = res.bodyAsString();
        assertEquals(_200.code, res.getResponseCode());
        log.info(body);
      }
    });

    it("Reads auth headers", () -> {
      mx.rootHandler(xc -> xc.commitText(xc.auth.toString()));
      var login = new Get(httpLocalHost)
        .header(MxExchanges.HAuthorization, authValue);
      try (var res = login.execute()) {
        var body = res.bodyAsString();
        assertEquals(_200.code, res.getResponseCode());
        log.info(body);
      }
    });

    it("Serves URL stream responses", () -> {
      mx.rootHandler(xc -> xc
        .withStatus(_200)
        .withBody(MxHandlerTest.class.getResource("/glossary.json"))
        .commit()
      );
      var glossary = new Get(httpLocalHost);
      try (var res = glossary.execute()) {
        var body = res.bodyAsString();
        assertEquals(_200.code, res.getResponseCode());
        log.info(body);
      }
    });

    it("Stops the server", () -> mx.stop());
  }
}
