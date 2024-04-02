package io.vacco.mrx;

import com.github.mizosoft.methanol.Methanol;
import io.vacco.murmux.Murmux;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;

import static com.github.mizosoft.methanol.MutableRequest.GET;
import static io.vacco.murmux.http.MxExchanges.HCacheControl;
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
    .baseUri("http://localhost:8080")
    .build();

  static {
    beforeAll(() -> mx = new Murmux()
      .rootHandler(xc -> xc.withStatus(_204).commit())
      .listen(8080));

    describe("Server config options", () -> {
      it("Can accept requests with the default number of headers", () -> {
        var res0 = client.send(GET("/"), ofString());
        assertEquals(_204.code, res0.statusCode());
      });

      it("Cannot accept requests with more than 6 headers", () -> {
        mx = mx.configMaxRequestHeaders(6);
        var res3 = client.send(GET("/"), ofString());
        assertEquals(_500.code, res3.statusCode());
      });
    });
  }
}
