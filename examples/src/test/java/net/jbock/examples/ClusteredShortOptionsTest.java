package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

class ClusteredShortOptionsTest {

    private final ClusteredShortOptionsParser parser = new ClusteredShortOptionsParser();

    private final ParserTestFixture<ClusteredShortOptions> f = ParserTestFixture.create(parser::parse);

    @Test
    void testAttached() {
        f.assertThat("-abcfInputFile.txt")
                .has(ClusteredShortOptions::aaa, true)
                .has(ClusteredShortOptions::bbb, true)
                .has(ClusteredShortOptions::ccc, true)
                .has(ClusteredShortOptions::file, "InputFile.txt");
    }

    @Test
    void testSurprise() {
        f.assertThat("-abcf=InputFile.txt")
                .has(ClusteredShortOptions::aaa, true)
                .has(ClusteredShortOptions::bbb, true)
                .has(ClusteredShortOptions::ccc, true)
                .has(ClusteredShortOptions::file, "=InputFile.txt"); // !
    }

    @Test
    void testAa() {
        f.assertThat("--aa", "-bcfInputFile.txt")
                .has(ClusteredShortOptions::aaa, true)
                .has(ClusteredShortOptions::bbb, true)
                .has(ClusteredShortOptions::ccc, true)
                .has(ClusteredShortOptions::file, "InputFile.txt");
    }

    @Test
    void testDetached() {
        f.assertThat("-abcf", "InputFile.txt")
                .has(ClusteredShortOptions::aaa, true)
                .has(ClusteredShortOptions::bbb, true)
                .has(ClusteredShortOptions::ccc, true)
                .has(ClusteredShortOptions::file, "InputFile.txt");
    }

    @Test
    void testClustering() {
        f.assertThat("-abc", "-fInputFile.txt")
                .has(ClusteredShortOptions::aaa, true)
                .has(ClusteredShortOptions::bbb, true)
                .has(ClusteredShortOptions::ccc, true)
                .has(ClusteredShortOptions::file, "InputFile.txt");
        f.assertThat("-ab", "-cfInputFile.txt")
                .has(ClusteredShortOptions::aaa, true)
                .has(ClusteredShortOptions::bbb, true)
                .has(ClusteredShortOptions::ccc, true)
                .has(ClusteredShortOptions::file, "InputFile.txt");
        f.assertThat("-a", "-b", "-c", "-fInputFile.txt")
                .has(ClusteredShortOptions::aaa, true)
                .has(ClusteredShortOptions::bbb, true)
                .has(ClusteredShortOptions::ccc, true)
                .has(ClusteredShortOptions::file, "InputFile.txt");
        f.assertThat("-a", "-b", "-c", "-f", "InputFile.txt")
                .has(ClusteredShortOptions::aaa, true)
                .has(ClusteredShortOptions::bbb, true)
                .has(ClusteredShortOptions::ccc, true)
                .has(ClusteredShortOptions::file, "InputFile.txt");
    }
}