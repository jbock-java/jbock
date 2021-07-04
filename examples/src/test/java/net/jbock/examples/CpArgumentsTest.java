package net.jbock.examples;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import io.jbock.util.Either;
import io.jbock.util.Optional;
import net.jbock.examples.CpArguments.Control;
import net.jbock.examples.fixture.ParserTestFixture;
import net.jbock.util.NotSuccess;
import net.jbock.util.ParseRequest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

class CpArgumentsTest {

    private final CpArgumentsParser parser = new CpArgumentsParser();

    private final ParserTestFixture<CpArguments> f =
            ParserTestFixture.create(parser::parse);

    @Test
    void errorMissingSource() {
        f.assertThat("-r").fails("Missing required parameter SOURCE");
    }

    @Test
    void enumValuesInMessage() {
        f.assertThat("a", "b", "--backup", "CLOUD").fails(
                "while converting option BACKUP (--backup): No enum constant " +
                        "net.jbock.examples.CpArguments.Control.CLOUD [NONE, NUMBERED, EXISTING, SIMPLE]");
    }

    @Test
    void errorMissingDest() {
        f.assertThat("a").fails("Missing required parameter DEST");
        f.assertThat("a", "-r").fails("Missing required parameter DEST");
        f.assertThat("-r", "a").fails("Missing required parameter DEST");
    }

    @Test
    void singleDashParameter() {
        f.assertThat("a", "-")
                .has(CpArguments::source, "a")
                .has(CpArguments::dest, "-")
                .has(CpArguments::recursive, false)
                .has(CpArguments::backup, Optional.empty())
                .has(CpArguments::suffix, Optional.empty());
    }

    @Test
    void dashNotIgnored() {
        f.assertThat("-a", "b").fails("Invalid option: -a");
    }

    @Test
    void tooMany() {
        f.assertThat("a", "b", "c").fails("Excess param: c");
    }

    @Test
    void tooManyAndFlag() {
        f.assertThat("-r", "a", "b", "c").fails("Excess param: c");
    }

    @Test
    void testNotClustering() {
        f.assertThat("-rs1", "a", "b").fails("Invalid option: -rs1");
    }

    @Test
    void flagInVariousPositions() {
        f.assertThat("-r", "a", "b")
                .has(CpArguments::source, "a")
                .has(CpArguments::dest, "b")
                .has(CpArguments::recursive, true)
                .has(CpArguments::backup, Optional.empty())
                .has(CpArguments::suffix, Optional.empty());
        f.assertThat("a", "-r", "b")
                .has(CpArguments::source, "a")
                .has(CpArguments::dest, "b")
                .has(CpArguments::recursive, true)
                .has(CpArguments::backup, Optional.empty())
                .has(CpArguments::suffix, Optional.empty());
        f.assertThat("a", "b", "-r")
                .has(CpArguments::source, "a")
                .has(CpArguments::dest, "b")
                .has(CpArguments::recursive, true)
                .has(CpArguments::backup, Optional.empty())
                .has(CpArguments::suffix, Optional.empty());
    }

    @Test
    void testEnum() {
        f.assertThat("a", "b", "--backup=NUMBERED")
                .has(CpArguments::source, "a")
                .has(CpArguments::dest, "b")
                .has(CpArguments::recursive, false)
                .has(CpArguments::backup, Optional.of(Control.NUMBERED))
                .has(CpArguments::suffix, Optional.empty());
        f.assertThat("-r", "a", "b", "--backup", "SIMPLE")
                .has(CpArguments::source, "a")
                .has(CpArguments::dest, "b")
                .has(CpArguments::recursive, true)
                .has(CpArguments::backup, Optional.of(Control.SIMPLE))
                .has(CpArguments::suffix, Optional.empty());
    }

    @Test
    void testAtFileSyntax() throws IOException {
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        Path foo = fs.getPath("/foo");
        Files.createDirectory(foo);
        Path path = foo.resolve("hello.txt");
        Files.write(path, List.of("-r", "\"a\"", "\"'b'\"", "--backup=\\", "'SIMPLE'", ""), StandardCharsets.UTF_8);
        Either<NotSuccess, CpArguments> result = parser.parse(
                ParseRequest.expansion(path, List.of()).build());
        f.assertThat(result)
                .has(CpArguments::source, "a")
                .has(CpArguments::dest, "'b'")
                .has(CpArguments::recursive, true)
                .has(CpArguments::backup, Optional.of(Control.SIMPLE))
                .has(CpArguments::suffix, Optional.empty());
    }

    @Test
    void testPrint() {
        f.assertPrintsHelp(
                "\u001B[1mUSAGE\u001B[m",
                "  cp-arguments [OPTIONS] SOURCE DEST",
                "",
                "\u001B[1mPARAMETERS\u001B[m",
                "  SOURCE ",
                "  DEST   ",
                "",
                "\u001B[1mOPTIONS\u001B[m",
                "  -r, --r             ",
                "  --backup BACKUP     ",
                "  -s, --suffix SUFFIX  Override the usual backup suffix",
                "");
    }
}
