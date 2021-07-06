package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;
import net.jbock.Parameters;

import java.util.List;

@Command
abstract class RmArguments {

    /**
     * @return a boolean
     */
    @Option(names = {"--recursive", "-r"},
            description = {
                    "ALLES TURISTEN UND NONTEKNISCHEN LOOKENSPEEPERS!",
                    "DAS KOMPUTERMASCHINE IST NICHT FUR DER GEFINGERPOKEN UND MITTENGRABEN!",
                    "ODERWISE IST EASY TO SCHNAPPEN DER SPRINGENWERK, BLOWENFUSEN UND POPPENCORKEN MIT SPITZENSPARKEN.",
                    "IST NICHT FUR GEWERKEN BEI DUMMKOPFEN.",
                    "DER RUBBERNECKEN SIGHTSEEREN KEEPEN DAS COTTONPICKEN HANDER IN DAS POCKETS MUSS.",
                    "ZO RELAXEN UND WATSCHEN DER BLINKENLICHTEN."})
    abstract boolean recursive();

    @Option(names = {"--force", "-f"},
            description = "Use the force, Luke.")
    abstract boolean force();

    @Parameters(description = "This is a list that may be empty.")
    abstract List<String> otherTokens();
}
