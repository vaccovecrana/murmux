package io.vacco.murmux.http;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents an HTTP Authorization header value and encapsulates authorization data.
 */
public class MxAuth {

  public static final String HEADER_NAME = "Authorization";

  private final String type;
  private final String data;

  public MxAuth(String authHeader) {
    String[] parts =
      Stream.of(authHeader.split(" "))
        .filter(s -> !s.isEmpty())
        .toArray(String[]::new);
    this.type = parts[0];
    this.data = parts[1];
  }

  /**
   * @param req Request instance
   * @return A list of authorization options that are contained in the given request. Authorization
   * options can be separated by a comma in the Authorization header.
   */
  public static List<MxAuth> get(MxRequest req) {
    List<String> headerVals = req.getHeader(HEADER_NAME);

    if (!headerVals.isEmpty()) {
      String authHeader = headerVals.get(0);
      return Collections.unmodifiableList(
        Stream.of(authHeader.split(",")).map(MxAuth::new).collect(Collectors.toList()));
    }

    return Collections.emptyList();
  }

  /**
   * Validates the given request authentication using each of the given predicates. If any of the
   * predicates returns <code>true</code>, the request is counted as validly authorized and the
   * method returns <code>true</code>.
   *
   * @param req        Request instance
   * @param validators Validators
   * @return If authorization was successful
   */
  @SafeVarargs
  public static boolean validate(MxRequest req, Predicate<MxAuth>... validators) {
    for (MxAuth auth : get(req)) {
      for (Predicate<MxAuth> validator : validators) {
        if (validator.test(auth)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * @param type The expected type of the authorization
   * @param data The expected data of the authorization
   * @return A predicate that can be used with {@link MxAuth#validate(MxRequest, Predicate...)}
   * to test for a single type of authorization
   */
  public static Predicate<MxAuth> validator(String type, String data) {
    return (auth -> auth.getType().equals(type) && auth.getData().equals(data));
  }

  /**
   * @return The Authorization type
   */
  public String getType() {
    return type;
  }

  /**
   * @return The Authorization data
   */
  public String getData() {
    return data;
  }

  /**
   * @return The Authorization data base64 decoded
   */
  public String getDataBase64Decoded() {
    return new String(Base64.getDecoder().decode(data));
  }
}
