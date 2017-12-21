package net.jbock.examples;

import static net.jbock.examples.fixture.PrintFixture.printFixture;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class EvilArgumentsTest {

  @Test
  public void basicTest() {
    EvilArguments args = EvilArguments_Parser.parse(
        new String[]{"--fancy=1", "--fAncy=2", "--f_ancy=3", "--blub=4", "--Blub=5"});
    assertThat(args.fancy(), is("1"));
    assertThat(args.fAncy(), is("2"));
    assertThat(args.f_ancy(), is("3"));
    assertThat(args.blub(), is("4"));
    assertThat(args.Blub(), is("5"));
  }

  @Test
  public void testPrint() {
    printFixture(EvilArguments_Parser::printUsage).assertPrints(
        "SYNOPSIS",
        "  --fancy=FANCY_0 --fAncy=F_ANCY_1 --f_ancy=F_ANCY_2 --blub=BLUB_3 --Blub=BLUB_4",
        "",
        "DESCRIPTION",
        "",
        "  --fancy VALUE",
        "",
        "  --fAncy VALUE",
        "",
        "  --f_ancy VALUE",
        "",
        "  --blub VALUE",
        "",
        "  --Blub VALUE",
        "",
        "");
  }
}
