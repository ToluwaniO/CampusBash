package toluog.campusbash

import toluog.campusbash.model.Event
import toluog.campusbash.model.University

/**
 * Created by oguns on 12/28/2017.
 */
class TestContract {

    companion object {
        val events = arrayListOf<Event>(Event("1"), Event("2"), Event("3"))
        val unis = arrayListOf<University>(University("1"), University("2"), University("3"))
    }
}