package net.jbock.examples;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VariousArgumentsTest {

  @Test
  void bigDecimal() {
    Optional<VariousArguments> parsed = VariousArguments_Parser.create().parse(new String[]{
        "--file", "/etc/hosts",
        "--bigDecimal", "3.14159265358979323846264338327950288419716939937510",
        "--bigInteger", "60221407600000000000000",
        "--path", "/home",
        "--localDate", "2001-02-01",
        "--localDateTime", "2018-09-10T15:03:22.872",
        "--offsetDateTime", "2018-09-10T15:03:22.874+02:00",
        "--zonedDateTime", "2018-09-10T15:04:00.938+02:00[Europe/Berlin]",
        "--uri", "http://localhost:8080",
        "6.02214076e23",
        "60221407600000000000000",
        "/etc/hosts",
        "/home",
        "2001-02-01",
        "2018-09-10T15:03:22.872",
        "2018-09-10T15:03:22.874+02:00",
        "2018-09-10T15:04:00.938+02:00[Europe/Berlin]",
        "http://localhost:8080",
    });
    assertTrue(parsed.isPresent());
    VariousArguments args = parsed.get();
    assertEquals(new BigDecimal("3.14159265358979323846264338327950288419716939937510"), args.bigDecimal());
    assertEquals(Optional.of(Paths.get("/home")), args.pathPos());
    assertEquals(URI.create("http://localhost:8080"), args.uri());
    assertEquals(Optional.of(URI.create("http://localhost:8080")), args.uriPos());
  }
}