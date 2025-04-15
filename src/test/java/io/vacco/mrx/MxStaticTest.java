package io.vacco.mrx;

import io.vacco.murmux.middleware.MxStatic;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;
import java.io.File;

import static j8spec.J8Spec.*;
import static org.junit.Assert.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class MxStaticTest {
  static {
    it("Resolves common paths", () -> {
      var baseDir = new File(System.getProperty("user.dir"));
      var testFile = new File(baseDir, "src/test/resources/static/images/test.png");
      // successful path resolution
      var result1 = MxStatic.resolveCommonPath(testFile, "resources/static");
      assertNotNull(result1);
      assertTrue(result1.getPath().endsWith("resources/static"));
      // path cannot be found
      var result2 = MxStatic.resolveCommonPath(testFile, "non/existent/path");
      assertNull(result2);
      // root-level path
      var result3 = MxStatic.resolveCommonPath(testFile, "src");
      assertNotNull(result3);
      assertTrue(result3.getPath().endsWith("src"));
    });
  }
}
