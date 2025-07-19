package io.vacco.murmux.http;

import java.util.Objects;
import java.util.function.BiConsumer;

public class MxLog {

  public static BiConsumer<String, Object[]> debugFn, infoFn, warnFn, errorFn;

  public static void setDebugLogger(BiConsumer<String, Object[]> logFn) {
    debugFn = Objects.requireNonNull(logFn);
  }

  public static void setInfoLogger(BiConsumer<String, Object[]> logFn) {
    infoFn = Objects.requireNonNull(logFn);
  }

  public static void setWarnLogger(BiConsumer<String, Object[]> logFn) {
    warnFn = Objects.requireNonNull(logFn);
  }

  public static void setErrorLogger(BiConsumer<String, Object[]> logFn) {
    errorFn = Objects.requireNonNull(logFn);
  }

  private static void apply(BiConsumer<String, Object[]> fn, String fmt, Object ... args) {
    if (fn != null) {
      fn.accept(fmt, args);
    }
  }

  public static void debug(String fmt, Object ... args) {
    apply(debugFn, fmt, args);
  }

  public static void info(String fmt, Object ... args) {
    apply(infoFn, fmt, args);
  }

  public static void warn(String fmt, Object ... args) {
    apply(warnFn, fmt, args);
  }

  public static void error(String fmt, Object ... args) {
    apply(errorFn, fmt, args);
  }

}
