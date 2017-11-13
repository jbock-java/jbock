package net.zerobuilder.examples.gradle;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class CurlTest {

  @Test
  public void testRemaining() throws Exception {
    Curl curl = Curl_Parser.parse(
        new String[]{"-H'Content-Type: application/json'", "-v", "http://localhost:8080"});
    Curl_Parser.printUsage(System.out, 2);
    assertThat(curl.urls().size(), is(1));
    assertThat(curl.urls().get(0), is("http://localhost:8080"));
  }
}
