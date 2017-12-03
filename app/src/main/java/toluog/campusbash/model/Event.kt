package toluog.campusbash.model

/**
 * Created by oguns on 12/2/2017.
 */
data class Event(var eventId: String, var eventName: String, var eventType: String, var description: String, var placeholderUrl: String?,
                 var eventVideoUrl: String?, var university: String, var locationAddress: String, var latLng: LatLng,
                 var startTime: String, var endTime: String, var notes: String = "", var creator: Creator)