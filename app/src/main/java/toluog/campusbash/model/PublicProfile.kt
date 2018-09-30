package toluog.campusbash.model

data class PublicProfile(var uid: String = "", var photoUrl: String = "", var userName: String = "",
                         var summary: String = "", var university: String = "", var country: String = "",
                         var followers: Long = 0, var following: Long = 0)