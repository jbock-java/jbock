package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;

@Command
interface PrimitiveArguments {

    @Option(names = {"--B", "-B"})
    byte simpleByte();

    @Option(names = {"--S", "-S"})
    short simpleShort();

    @Option(names = {"--I", "-I"})
    int simpleInt();

    @Option(names = {"--L", "-L"})
    long simpleLong();

    @Option(names = {"--F", "-F"})
    float simpleFloat();

    @Option(names = {"--D", "-D"})
    double simpleDouble();

    @Option(names = {"--C", "-C"})
    char simpleChar();
}
