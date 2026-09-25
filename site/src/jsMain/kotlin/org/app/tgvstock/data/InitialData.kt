package org.app.tgvstock.data

import org.app.tgvstock.models.Packaging
import org.app.tgvstock.models.Product

object InitialData {
    val defaultProducts: List<Product> = listOf(
        Product("p1", "MANYAN", 8000.0, 12, "Bières", 25),
        Product("p2", "33 EXPORT 65", 9500.0, 12, "Bières", 40),
        Product("p3", "33 EXPORT 50", 8500.0, 20, "Bières", 30),
        Product("p4", "DOPEL 65", 9500.0, 12, "Bières", 20),
        Product("p5", "DOPEL 50", 8500.0, 20, "Bières", 15),
        Product("p6", "CASTEL 65", 9500.0, 12, "Bières", 35),
        Product("p7", "CASTEL 50", 8500.0, 20, "Bières", 25),
        Product("p8", "BEAUFORT", 9500.0, 12, "Bières", 30),
        Product("p9", "ISSEMBECK", 10500.0, 12, "Bières", 18),
        Product("p10", "BOOSTER COLA", 9500.0, 20, "Alcopops", 20),
        Product("p11", "BOOSTER GIN TONIC", 9500.0, 20, "Alcopops", 20),
        Product("p12", "MUTSIC", 10500.0, 12, "Bières", 22),
        Product("p13", "KADJI", 10500.0, 12, "Bières", 25),
        Product("p14", "P GUINNESS", 21000.0, 24, "Guinness", 30),
        Product("p15", "G GUINNESS", 15000.0, 12, "Guinness", 28),
        Product("p16", "ORIGINE", 10000.0, 12, "Bières", 15),
        Product("p17", "ICE ANANAS", 19000.0, 24, "Alcopops", 12),
        Product("p18", "ICE BLACK", 19000.0, 24, "Alcopops", 12),
        Product("p19", "SMOOTH 12", 14500.0, 12, "Guinness", 16),
        Product("p20", "SMOOTH 15", 12500.0, 15, "Guinness", 14),
        Product("p21", "MALTA GUINNESS", 15000.0, 24, "Non-Alcoolisé", 20),
        Product("p22", "TOP 1L", 3000.0, 12, "Gazeuses", 30),
        Product("p23", "TOP 33CL", 3000.0, 24, "Gazeuses", 25),
        Product("p24", "ORANGINA", 4500.0, 24, "Gazeuses", 15),
        Product("p25", "VIMTO 1L", 4500.0, 12, "Gazeuses", 18),
        Product("p26", "DJINO 1L", 4500.0, 12, "Gazeuses", 20),
        Product("p27", "WORD COLA 1L", 3500.0, 12, "Gazeuses", 22),
        Product("p28", "YOUZOU 1L", 3500.0, 12, "Gazeuses", 15),
        Product("p29", "EAU 1.5L", 2000.0, 6, "Eaux", 40),
        Product("p30", "33 EXPORT Canette", 20000.0, 24, "Canettes", 15),
        Product("p31", "ISSEMBECK Canette", 20000.0, 24, "Canettes", 10),
        Product("p32", "BEAUFORT Canette", 20000.0, 24, "Canettes", 15),
        Product("p33", "3X GM", 19000.0, 24, "Canettes", 12),
        Product("p34", "BULLET", 19000.0, 24, "Canettes", 10),
        Product("p35", "BAVARIA Cannette", 19000.0, 24, "Canettes", 12),
        Product("p36", "HEINEKEN BOUTEILLE", 21000.0, 24, "Bières", 18),
        Product("p37", "FAXE", 19000.0, 24, "Canettes", 14),
        Product("p38", "BEAR BEER", 19000.0, 24, "Canettes", 12),
        Product("p39", "GUINNESS CANNETTE", 20000.0, 24, "Canettes", 15),
        Product("p40", "VODY", 19000.0, 24, "Canettes", 16),
        Product("p41", "BARON", 17000.0, 24, "Canettes", 10),
        Product("p42", "CHILL", 9500.0, 12, "Bières", 14),
        Product("p43", "TOP 50", 7500.0, 20, "Gazeuses", 20),
        Product("p44", "WORD COLA 50", 7500.0, 20, "Gazeuses", 18),
        Product("p45", "DESPERADO", 22000.0, 24, "Bières", 12),
        Product("p46", "MALTA CANNETTE", 10000.0, 24, "Non-Alcoolisé", 15)
    )

    val defaultPackaging: List<Packaging> = listOf(
        Packaging("pkg1", "Casier 65cl (12 bouteilles)", "65cl", emptyCrates = 45, fullCrates = 160, depositValuePerCrate = 3000.0),
        Packaging("pkg2", "Casier 50cl (20 bouteilles)", "50cl", emptyCrates = 30, fullCrates = 110, depositValuePerCrate = 3500.0),
        Packaging("pkg3", "Casier 33cl (24 bouteilles)", "33cl", emptyCrates = 25, fullCrates = 95, depositValuePerCrate = 4000.0),
        Packaging("pkg4", "Casier Guinness 65cl (12 bout.)", "Guinness", emptyCrates = 20, fullCrates = 44, depositValuePerCrate = 3000.0),
        Packaging("pkg5", "Casier Top / Gazeuses 1L (12 bout.)", "1L", emptyCrates = 28, fullCrates = 70, depositValuePerCrate = 2500.0),
        Packaging("pkg6", "Emballages / Cartons Canettes", "Canettes", emptyCrates = 15, fullCrates = 110, depositValuePerCrate = 0.0)
    )
}
