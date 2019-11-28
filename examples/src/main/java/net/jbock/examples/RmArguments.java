package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;
import net.jbock.PositionalParameter;

import java.util.List;

@CommandLineArguments
abstract class RmArguments {

  /**
   * ALLES TURISTEN UND NONTEKNISCHEN LOOKENSPEEPERS!
   * DAS KOMPUTERMASCHINE IST NICHT FUR DER GEFINGERPOKEN UND MITTENGRABEN!
   * ODERWISE IST EASY TO SCHNAPPEN DER SPRINGENWERK, BLOWENFUSEN UND POPPENCORKEN MIT SPITZENSPARKEN.
   * IST NICHT FUR GEWERKEN BEI DUMMKOPFEN.
   * DER RUBBERNECKEN SIGHTSEEREN KEEPEN DAS COTTONPICKEN HANDER IN DAS POCKETS MUSS.
   * ZO RELAXEN UND WATSCHEN DER BLINKENLICHTEN.
   *
   * @return a boolean
   */
  @Parameter(value = "recursive", mnemonic = 'r')
  abstract boolean recursive();

  /**
   * Use the force, Luke.
   */
  @Parameter(value = "force", mnemonic = 'f')
  abstract boolean force();

  /**
   * This is a list that may be empty.
   */
  @PositionalParameter(1)
  abstract List<String> otherTokens();
}
