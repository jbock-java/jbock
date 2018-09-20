package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;

@CommandLineArguments
abstract class PrimitiveArguments {

  @Parameter(shortName = 'B')
  abstract byte simpleByte();

  @Parameter(shortName = 'S')
  abstract short simpleShort();

  @Parameter(shortName = 'I')
  abstract int simpleInt();

  @Parameter(shortName = 'L')
  abstract long simpleLong();

  @Parameter(shortName = 'F')
  abstract float simpleFloat();

  @Parameter(shortName = 'D')
  abstract double simpleDouble();

  @Parameter(shortName = 'C')
  abstract char simpleChar();

}
