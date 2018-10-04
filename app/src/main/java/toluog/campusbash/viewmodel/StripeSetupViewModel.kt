package toluog.campusbash.viewmodel

import android.app.Application

class StripeSetupViewModel(app: Application) : GeneralViewModel(app)  {

    private val TAG = EventsViewModel::class.java.simpleName

    fun createStripeAccount() = repo.createStripeAccount()

}