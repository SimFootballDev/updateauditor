package model

class Team(val id: String, val name: String)

fun getNSFLTeamList() = NSFLTeam.values().map { Team(it.id, it.name) }

fun getDSFLTeamList() = DSFLTeam.values().map { Team(it.id, it.name) }

enum class NSFLTeam(val id: String) {
    BALTIMORE_HAWKS("70"),
    CHICAGO_BUTCHERS("325"),
    COLORADO_YETI("56"),
    PHILADELPHIA_LIBERTY("111"),
    YELLOWKNIFE_WRAITHS("58"),
    ARIZONA_OUTLAWS("73"),
    AUSTIN_COPPERHEADS("328"),
    NEW_ORLEANS_SECOND_LINE("114"),
    ORANGE_COUNTY_OTTERS("54"),
    SAN_JOSE_SABERCATS("52")
}

enum class DSFLTeam(val id: String) {
    PALM_BEACH_SOLAR_BEARS("161"),
    KANSAS_CITY_COYOTES("159"),
    PORTLAND_PYTHONS("165"),
    NORFOLK_SEAWOLVES("163"),
    SAN_ANTONIO_MARSHALS("157"),
    TIJUANA_LUCHADORES("155")
}