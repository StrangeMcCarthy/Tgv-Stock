package org.app.tgvstock.data

import org.app.tgvstock.models.Packaging
import org.app.tgvstock.models.Product

object InitialData {
    val defaultProducts: List<Product> = listOf(
        Product("p1", "MANYAN", 9000.0, 12, "Bières", 0),
        Product("p2", "33 EXPORT 65", 10500.0, 12, "Bières", 0),
        Product("p3", "33 EXPORT 50", 9500.0, 20, "Bières", 0),
        Product("p4", "DOPEL 65", 10500.0, 12, "Bières", 0),
        Product("p5", "DOPEL 50", 9500.0, 20, "Bières", 0),
        Product("p6", "CASTEL 65", 10500.0, 12, "Bières", 0),
        Product("p7", "CASTEL 50", 9500.0, 20, "Bières", 0),
        Product("p8", "BEAUFORT", 10500.0, 12, "Bières", 0),
        Product("p9", "ISSEMBECK", 11500.0, 12, "Bières", 0),
        Product("p10", "BOOSTER COLA", 10500.0, 20, "Alcopops", 0),
        Product("p11", "BOOSTER GIN TONIC", 10500.0, 20, "Alcopops", 0),
        Product("p12", "MUTSIC", 11500.0, 12, "Bières", 0),
        Product("p13", "KADJI", 11500.0, 12, "Bières", 0),
        Product("p14", "P GUINNESS", 21000.0, 24, "Guinness", 0),
        Product("p15", "G GUINNESS", 16000.0, 12, "Guinness", 0),
        Product("p16", "ORIGINE", 11000.0, 12, "Bières", 0),
        Product("p17", "ICE ANANAS", 20000.0, 24, "Alcopops", 0),
        Product("p18", "ICE BLACK", 20000.0, 24, "Alcopops", 0),
        Product("p19", "SMOOTH 12", 15500.0, 12, "Guinness", 0),
        Product("p20", "SMOOTH 15", 13500.0, 15, "Guinness", 0),
        Product("p21", "MALTA GUINNESS", 16000.0, 24, "Non-Alcoolisé", 0),
        Product("p22", "TOP 1L", 3000.0, 12, "Gazeuses", 0),
        Product("p23", "TOP 33CL", 3000.0, 24, "Gazeuses", 0),
        Product("p24", "ORANGINA", 4500.0, 24, "Gazeuses", 0),
        Product("p25", "VIMTO 1L", 4500.0, 12, "Gazeuses", 0),
        Product("p26", "DJINO 1L", 4500.0, 12, "Gazeuses", 0),
        Product("p27", "WORD COLA 1L", 3500.0, 12, "Gazeuses", 0),
        Product("p28", "YOUZOU 1L", 3500.0, 12, "Gazeuses", 0),
        Product("p29", "EAU 1.5L", 2000.0, 6, "Eaux", 0),
        Product("p30", "33 EXPORT Canette", 19000.0, 24, "Canettes", 0),
        Product("p31", "ISSEMBECK Canette", 20000.0, 24, "Canettes", 0),
        Product("p32", "BEAUFORT Canette", 20000.0, 24, "Canettes", 0),
        Product("p33", "3X GM", 19000.0, 24, "Canettes", 0),
        Product("p34", "BULLET", 19000.0, 24, "Canettes", 0),
        Product("p35", "BAVARIA Cannette", 19000.0, 24, "Canettes", 0),
        Product("p36", "HEINEKEN CANETTE", 24000.0, 24, "Canettes", 0),
        Product("p37", "HEINEKEN BOUTEILLE", 21000.0, 24, "Bières", 0),
        Product("p38", "FAXE", 19000.0, 24, "Canettes", 0),
        Product("p39", "BEAR BEER", 19000.0, 24, "Canettes", 0),
        Product("p40", "GUINNESS CANNETTE", 20000.0, 24, "Canettes", 0),
        Product("p41", "VODY", 19000.0, 24, "Canettes", 0),
        Product("p42", "DESPERADO", 20000.0, 24, "Bières", 0),
        Product("p43", "CHILL", 9500.0, 12, "Bières", 0),
        Product("p44", "TOP 50", 6000.0, 20, "Gazeuses", 0),
        Product("p45", "WORD COLA 50", 6000.0, 20, "Gazeuses", 0),
        Product("p46", "BARON", 17000.0, 24, "Canettes", 0),
        Product("p47", "MALTA CANNETTES", 10000.0, 24, "Non-Alcoolisé", 0)
    )

    val defaultPackaging: List<Packaging> = listOf(
        Packaging("pkg1", "Casier Boissons du Cameroun 65cl (12 bout.)", "BRASSERIES", emptyCrates = 0, fullCrates = 0, depositValuePerCrate = 3000.0),
        Packaging("pkg2", "Casier Boissons du Cameroun 50cl (20 bout.)", "BRASSERIES", emptyCrates = 0, fullCrates = 0, depositValuePerCrate = 3500.0),
        Packaging("pkg3", "Casier Boissons du Cameroun 33cl (24 bout.)", "BRASSERIES", emptyCrates = 0, fullCrates = 0, depositValuePerCrate = 4000.0),
        Packaging("pkg4", "Casier Guinness 65cl (12 bout.)", "GUINNESS", emptyCrates = 0, fullCrates = 0, depositValuePerCrate = 3000.0),
        Packaging("pkg5", "Casier Guinness 33cl (24 bout.)", "GUINNESS", emptyCrates = 0, fullCrates = 0, depositValuePerCrate = 4000.0),
        Packaging("pkg6", "Casier PET / Gazeuses 1L (12 bout.)", "PET", emptyCrates = 0, fullCrates = 0, depositValuePerCrate = 2500.0),
        Packaging("pkg7", "Emballages / Cartons Canettes", "Canettes", emptyCrates = 0, fullCrates = 0, depositValuePerCrate = 0.0)
    )
}
