package net.jbock.contrib;

import net.jbock.model.CommandModel;
import net.jbock.model.Option;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StandardErrorHandlerTest {

    @Test
    void testPrint() {
        String expectation = """
                USAGE
                  hello-world [OPTIONS]
                
                OPTIONS
                  -v\s
                """;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StandardErrorHandler.builder()
                .withAnsi(false)
                .withOutputStream(new PrintStream(out))
                .build()
                .printUsageDocumentation(createModel());
        assertEquals(expectation, out.toString());
    }

    private CommandModel createModel() {
        return CommandModel.builder()
                .withProgramName("hello-world")
                .addOption(Option.nullary()
                        .withNames(List.of("-v"))
                        .build())
                .build();
    }

}
