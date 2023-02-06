package examples;

import io.vacco.murmux.Murmux;
import io.vacco.murmux.http.MxStatus;
import io.vacco.murmux.middleware.*;
import java.nio.file.Paths;

/*
 * Try:
 *
   curl http://localhost:8080/api/book/list \
      --verbose \
      -X OPTIONS \
      -H "Access-Control-Request-Method: POST" \
      -H "Access-Control-Request-Headers: content-type" \
      -H "Origin: https://reqbin.com"
 */
public class Cors {
  public static void main(String[] args) {
    LoggerInit.apply();
    new Murmux().rootHandler(
      new MxRouter()
        .prefix("/api", new MxCors(
          new MxCors.Options().withMaxAge(3600),
          new MxRouter()
            .get(
              "/api/book/list", xc ->
                xc.withStatus(MxStatus._200)
                  .withBody(Paths.get("./src/test/resources/books.json"))
                  .commit()
            )
        ))
    ).listen(8080);
  }
}
