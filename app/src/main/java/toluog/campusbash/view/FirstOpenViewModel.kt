package toluog.campusbash.view

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import com.google.firebase.firestore.FirebaseFirestore
import toluog.campusbash.data.AppDatabase
import toluog.campusbash.data.Repository
import toluog.campusbash.model.University

/**
 * Created by oguns on 12/28/2017.
 */
class FirstOpenViewModel(app: Application): GeneralViewModel(app){

    fun getUniversities(country: String) = repo.getUnis(country)

}