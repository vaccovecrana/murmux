package io.vacco.mrx;

import examples.LoggerInit;
import io.vacco.murmux.http.*;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;
import org.slf4j.*;
import java.time.Instant;

import static j8spec.J8Spec.*;
import static org.junit.Assert.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class MxCoreTest {

  static { LoggerInit.apply(); }

  private static final Logger log = LoggerFactory.getLogger(MxCoreTest.class);

  static {
    MxLog.setDebugLogger(log::debug);
    MxLog.setInfoLogger(log::info);
    MxLog.setWarnLogger(log::warn);
    MxLog.setErrorLogger(log::error);

    it("Defines request rules", () -> {
      var rule0 = MxRule.of(MxMethod.GET.method, "/hello", null);
      var rule1 = MxRule.of(MxMethod.PUT.method, "/hello", null);
      log.info(rule0.toString());
      log.info("[{} {}]", rule0.hashCode(), rule1.hashCode());
      assertNotEquals(rule0, rule1);
    });
    it("Parses URL request paths with parameters", () -> {
      var p0 = "/hello/world/{placeId}/region/{regionId}";
      var t0 = "/hello/world/12/region/2";
      var lol = MxPaths.matchUrl(p0, t0);
      assertNotNull(lol);
      assertFalse(lol.isEmpty());
      assertEquals("12", lol.get("placeId"));
      assertEquals("2", lol.get("regionId"));
    });
    it("Makes cookies", () -> {
      var cook = new MxCookie("sess", Integer.toHexString(123456))
        .withExpire(Instant.now().plusMillis(10000))
        .withHttpOnly(true)
        .withMaxAge(300_000)
        .withSecure(true);
      log.info(cook.toString());
    });
  }
}
