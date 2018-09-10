package net.jbock.examples;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

class VariousArgumentsTest {

  @Test
  void bigDecimal() {
    Optional<VariousArguments> parsed = VariousArguments_Parser.create().parse(new String[]{
        "--file", "/etc/hosts",
        "--bigDecimal", "3.141592653589793",
        "--bigInteger", "121897123",
        "--path", "/home",
        "--localDate", "2001-02-01",
        "--localDateTime", "2018-09-10T15:03:22.872",
        "--offsetDateTime", "2018-09-10T15:03:22.874+02:00",
        "--zonedDateTime", "2018-09-10T15:04:00.938+02:00[Europe/Berlin]",
        "--uri", "http://localhost:8080"
    });
    assertTrue(parsed.isPresent());
  }
}