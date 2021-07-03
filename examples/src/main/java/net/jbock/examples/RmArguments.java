package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;
import net.jbock.Parameters;

import java.util.List;

@Command
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
    @Option(names = {"--recursive", "-r"})
    abstract boolean recursive();

    /**
     * Use the force, Luke.
     */
    @Option(names = {"--force", "-f"})
    abstract boolean force();

    /**
     * This is a list that may be empty.
     */
    @Parameters
    abstract List<String> otherTokens();
}
