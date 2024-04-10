package io.vacco.mrx;

import com.github.mizosoft.methanol.Methanol;
import examples.LoggerInit;
import io.vacco.murmux.Murmux;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;

import static com.github.mizosoft.methanol.MutableRequest.GET;
import static io.vacco.murmux.http.MxStatus.*;
import static j8spec.J8Spec.*;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.junit.Assert.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class MxConfigTest {

  public static Murmux mx;

  public static final Methanol client = Methanol
    .newBuilder()
    .baseUri("http://localhost:8083")
    .build();

  static { LoggerInit.apply(); }

  static {
    beforeAll(() -> mx = new Murmux()
      .rootHandler(xc -> xc.withStatus(_204).commit())
      .listen(8083));

    describe("Server config options", () -> {
      it("Can accept requests with the default number of headers", () -> {
        var res0 = client.send(GET("/"), ofString());
        assertEquals(_204.code, res0.statusCode());
      });

      it("Cannot accept requests with more than 6 headers", () -> {
        var osName = System.getProperty("os.name");
        mx = mx.configMaxRequestHeaders(osName.equals("Mac OS X") ? 5 : 6);
        var req = GET("/");
        var res = client.send(req, ofString());
        assertEquals(_500.code, res.statusCode());
      });

      it("Cannot accept header with size greater than 39 characters", () -> {
        mx = mx.configMaxHeaderSize(39).configMaxRequestHeaders(Integer.MAX_VALUE);
        var req = GET("/");
        var res = client.send(req, ofString());
        assertEquals(_500.code, res.statusCode());
      });

      it("Cannot accept request with cookies greater than 0", () -> {
        mx = mx.configMaxCookies(0).configMaxHeaderSize(Integer.MAX_VALUE);
        var req = GET("/").header("Cookie", "username=test");
        var res = client.send(req, ofString());
        assertEquals(_500.code, res.statusCode());
      });
    });

    it("Stops the server", () -> mx.stop());
  }
}
