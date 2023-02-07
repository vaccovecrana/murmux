package io.vacco.mrx;

import examples.LoggerInit;
import io.vacco.murmux.http.*;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;
import org.slf4j.*;
import java.util.function.Function;

import static j8spec.J8Spec.*;
import static org.junit.Assert.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class MxCoreTest {

  static { LoggerInit.apply(); }
  private static final Logger log = LoggerFactory.getLogger(MxCoreTest.class);

  public static Function<Long, Void> sleep = (ms) -> {
    try {
      Thread.sleep(ms);
      return null;
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  };

  static {
    it("Defines request rules", () -> {
      var rule0 = MxRule.of(MxMethod.GET.method, "/hello", null);
      var rule1 = MxRule.of(MxMethod.PUT.method, "/hello", null);
      log.info(rule0.toString());
      log.info("[{} {}]", rule0.hashCode(), rule1.hashCode());
      assertEquals(rule0, rule0);
      assertNotEquals(rule0, rule1);
    });
    it("Parses URL request paths with parameters", () -> {
      var p0 = "/hello/world/:placeId";
      var t0 = "/hello/world/99988";
      var lol = MxPaths.matchUrl(p0, t0);
      assertNotNull(lol);
      assertFalse(lol.isEmpty());
      assertEquals(lol.get("placeId"), "99988");
    });
  }
}
