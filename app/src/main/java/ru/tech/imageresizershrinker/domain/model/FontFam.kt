package ru.tech.imageresizershrinker.domain.model

sealed class FontFam(val ordinal: Int) {
    data object Montserrat : FontFam(1)
    data object Caveat : FontFam(2)
    data object Comfortaa : FontFam(3)
    data object Handjet : FontFam(4)
    data object YsabeauSC : FontFam(5)
    data object Jura : FontFam(6)
    data object Podkova : FontFam(7)
    data object Tektur : FontFam(8)
    data object DejaVu : FontFam(9)
    data object BadScript : FontFam(10)
    data object RuslanDisplay : FontFam(11)
    data object System : FontFam(0)

    companion object {
        fun fromOrdinal(int: Int?): FontFam = when (int) {
            0 -> System
            1, null -> Montserrat
            2 -> Caveat
            3 -> Comfortaa
            4 -> Handjet
            5 -> YsabeauSC
            6 -> Jura
            7 -> Podkova
            8 -> Tektur
            9 -> DejaVu
            10 -> BadScript
            11 -> RuslanDisplay
            else -> System
        }
    }
}
