package examples;

import io.vacco.shax.logging.ShOption;

public class LoggerInit {

  public static void apply() {
    ShOption.setSysProp(ShOption.IO_VACCO_SHAX_DEVMODE, "true");
    ShOption.setSysProp(ShOption.IO_VACCO_SHAX_PRETTYPRINT, "true");
  }

}
