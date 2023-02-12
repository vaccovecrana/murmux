package io.vacco.mrx;

import com.github.mizosoft.methanol.*;
import examples.LoggerInit;
import io.vacco.murmux.Murmux;
import io.vacco.murmux.http.*;
import io.vacco.murmux.middleware.MxRouter;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;
import org.slf4j.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.function.*;
import java.net.http.HttpRequest.BodyPublishers;

import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static com.github.mizosoft.methanol.MutableRequest.*;
import static io.vacco.murmux.http.MxExchanges.*;
import static io.vacco.murmux.http.MxStatus.*;
import static io.vacco.murmux.http.MxMime.*;
import static j8spec.J8Spec.*;
import static org.junit.Assert.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class MxHandlerTest {

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

  public static final Methanol client = Methanol
    .newBuilder()
    .baseUri("http://localhost:8080")
    .build();

  static {
    LoggerInit.apply();
    beforeAll(() -> mx = new Murmux()
      .rootHandler(xc -> xc.withStatus(_204).commit())
      .listen(8080));

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
      var res = client.send(GET("/"), ofString());
      assertEquals(hello, res.body());
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

      assertEquals(_201.code, client.send(
        POST("/book", BodyPublishers.ofString(duckBookJson))
          .header(HContentType, json.type), ofString()
      ).statusCode());

      assertEquals(_302.code, client.send(POST("/login",
        FormBodyPublisher.newBuilder()
          .query(paramUsername, username)
          .query(paramPassword, password)
          .build()
      ), ofString()).statusCode());

      var res3 = client.send(
        GET("/logo.png?size=800&format=.jpeg")
          .header(HCacheControl, "no-cache"),
        ofString()
      );
      log.info("[{}]", res3.body().length());
      assertEquals(_200.code, res3.statusCode());
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

      var res0 = client.send(GET("/book/999999"), ofString());
      assertEquals(duckBookJson, res0.body());
      assertEquals(_200.code, res0.statusCode());

      var res1 = client.send(
        MutableRequest.create("/momo").method(
          "PUT", BodyPublishers.ofString(duckBookJson)
        ), ofString()
      );
      assertEquals(what, res1.body());
      assertEquals(_418.code, res1.statusCode());

      assertEquals(_417.code, client.send(GET("/quote"), ofString()).statusCode());
    });

    it("Rejects prefix route paths with parameters",
      c -> c.expected(IllegalArgumentException.class),
      () -> mx.rootHandler(new MxRouter()
        .prefix("/momo/{lolId}", xc -> xc.withStatus(_200)))
    );

    it("Accepts requests on prefix routes", () -> {
      mx.rootHandler(
        new MxRouter()
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

      var res0 = client.send(GET("/api/book/list").header(HXRequestedWith, "XMLHttpRequest"), ofString());
      assertEquals(duckBookJson, res0.body());
      assertEquals(_200.code, res0.statusCode());

      var res1 = client.send(GET("/api/book/8888"), ofString());
      assertEquals(duckBookJson, res1.body());
      assertEquals(_200.code, res1.statusCode());

      var res2 = client.send(GET("/ui/book/list"), ofString());
      assertEquals(htmlBooks, res2.body());
      assertEquals(_200.code, res2.statusCode());
    });

    it("Generates cookies", () -> {
      var cookie = new MxCookie(cookieName, hello);
      assertEquals(cookie, cookie);
      mx.rootHandler(xc -> xc.withStatus(_200)
        .withCookie(cookie)
        .withBody(txt, cookieSet)
        .commit()
      );
      var res = client.send(GET("/give-me-cookies"), ofString());
      assertEquals(cookieSet, res.body());
      assertEquals(_200.code, res.statusCode());
    });

    it("Reads cookies", () -> {
      mx.rootHandler(xc -> {
        assertFalse(xc.cookies.isEmpty());
        xc.commitText(xc.cookies.toString());
      });
      assertEquals(_200.code, client.send(
        GET("/give-me-cookies").header(HCookie, cookieClient), ofString()
      ).statusCode());
    });

    it("Reads auth headers", () -> {
      mx.rootHandler(xc -> xc.commitText(xc.auth.toString()));
      assertEquals(_200.code, client.send(
        GET("/").header(HAuthorization, authValue), ofString()
      ).statusCode());
    });

    it("Serves URL stream responses", () -> {
      mx.rootHandler(xc -> xc
        .withStatus(_200)
        .withBody(MxHandlerTest.class.getResource("/glossary.json"))
        .commit()
      );
      assertEquals(_200.code, client.send(GET("/"), ofString()).statusCode());
    });

    it("Stops the server", () -> mx.stop());
  }
}
