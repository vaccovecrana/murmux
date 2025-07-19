package examples;

import io.vacco.shax.logging.ShOption;
import org.slf4j.bridge.SLF4JBridgeHandler;
import java.util.logging.Level;

public class LoggerInit {

  public static void apply() {
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();
    java.util.logging.LogManager.getLogManager().getLogger("").setLevel(Level.FINER);

    ShOption.setSysProp(ShOption.IO_VACCO_SHAX_DEVMODE, "true");
    ShOption.setSysProp(ShOption.IO_VACCO_SHAX_PRETTYPRINT, "true");
  }

}
