package net.jbock.examples;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VariousArgumentsTest {

  @Test
  void bigDecimal() {
    VariousArguments_Parser.ParseResult parsed = VariousArguments_Parser.create().parse(new String[]{
        "--file", "/etc/hosts",
        "--bigDecimal", "3.14159265358979323846264338327950288419716939937510",
        "--bigInteger", "60221407600000000000000",
        "--path", "/home",
        "--localDate", "2001-02-01",
        "--localDateTime", "2018-09-10T15:03:22.872",
        "--offsetDateTime", "2018-09-10T15:03:22.874+02:00",
        "--zonedDateTime", "2018-09-10T15:04:00.938+02:00[Europe/Berlin]",
        "--uri", "http://localhost:8080",
        "--charset", "UTF-8",
        "--pattern", "^[abc]*$",
        "--instant", "1970-01-01T00:00:00Z",
        "6.02214076e23",
        "60221407600000000000000",
        "/etc/hosts",
        "/home",
        "2001-02-01",
        "2018-09-10T15:03:22.872",
        "2018-09-10T15:03:22.874+02:00",
        "2018-09-10T15:04:00.938+02:00[Europe/Berlin]",
        "http://localhost:8080",
        "ISO-8859-1",
        "^[abc]*$",
        "1970-01-01T00:00:00Z"
    });
    assertTrue(parsed instanceof VariousArguments_Parser.ParsingSuccess);
    VariousArguments args = ((VariousArguments_Parser.ParsingSuccess) parsed).result();
    assertEquals(new BigDecimal("3.14159265358979323846264338327950288419716939937510"), args.bigDecimal());
    assertEquals(Optional.of(Paths.get("/home")), args.pathPos());
    assertEquals(URI.create("http://localhost:8080"), args.uri());
    assertEquals(Optional.of(URI.create("http://localhost:8080")), args.uriPos());
    assertEquals(StandardCharsets.UTF_8, args.charset());
    assertEquals(Optional.of(StandardCharsets.ISO_8859_1), args.charsetPos());
  }
}