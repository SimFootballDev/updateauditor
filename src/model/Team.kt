package model

class Team(val name: String, val forumId: String, val simId: String)

fun getNSFLTeamList() = NSFLTeam.values().map { Team(it.name, it.forumId, it.simId) }

fun getDSFLTeamList() = DSFLTeam.values().map { Team(it.name, it.forumId, it.simId) }

enum class NSFLTeam(val forumId: String, val simId: String) {
    BALTIMORE_HAWKS("83", "1"),
    CHICAGO_BUTCHERS("142", "9"),
    COLORADO_YETI("67", "3"),
    PHILADELPHIA_LIBERTY("145", "7"),
    YELLOWKNIFE_WRAITHS("69", "2"),
    ARIZONA_OUTLAWS("86", "4"),
    AUSTIN_COPPERHEADS("128", "10"),
    NEW_ORLEANS_SECOND_LINE("148", "8"),
    ORANGE_COUNTY_OTTERS("65", "5"),
    SAN_JOSE_SABERCATS("63", "6"),
    SARASOTA_SAILFISH("307", "11"),
    HONOLULU_HAHALUA("309", "12")
}

enum class DSFLTeam(val forumId: String, val simId: String) {
    MYRTLE_BEACH_BUCCANEERS("196", "3"),
    KANSAS_CITY_COYOTES("194", "4"),
    PORTLAND_PYTHONS("200", "1"),
    NORFOLK_SEAWOLVES("198", "2"),
    MINNESOTA_GREY_DUCKS("192", "6"),
    TIJUANA_LUCHADORES("190", "5"),
    DALLAS_BIRDDOGS("298", "7"),
    LONDON_ROYALS("295", "8")
}
