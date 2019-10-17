package model

class Team(val name: String, val forumId: String, val simId: String)

fun getNSFLTeamList() = NSFLTeam.values().map { Team(it.name, it.forumId, it.simId) }

fun getDSFLTeamList() = DSFLTeam.values().map { Team(it.name, it.forumId, it.simId) }

enum class NSFLTeam(val forumId: String, val simId: String) {
    BALTIMORE_HAWKS("70", "1"),
    CHICAGO_BUTCHERS("325", "9"),
    COLORADO_YETI("56", "3"),
    PHILADELPHIA_LIBERTY("111", "7"),
    YELLOWKNIFE_WRAITHS("58", "2"),
    ARIZONA_OUTLAWS("73", "4"),
    AUSTIN_COPPERHEADS("328", "10"),
    NEW_ORLEANS_SECOND_LINE("114", "8"),
    ORANGE_COUNTY_OTTERS("54", "5"),
    SAN_JOSE_SABERCATS("52", "6")
}

enum class DSFLTeam(val forumId: String, val simId: String) {
    MYRTLE_BEACH_BUCCANEERS("161", "3"),
    KANSAS_CITY_COYOTES("159", "4"),
    PORTLAND_PYTHONS("165", "1"),
    NORFOLK_SEAWOLVES("163", "2"),
    SAN_ANTONIO_MARSHALS("157", "6"),
    TIJUANA_LUCHADORES("155", "5")
}