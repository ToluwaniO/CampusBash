package toluog.campusbash.view

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import com.myhexaville.smartimagepicker.ImagePicker
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_setup_profile.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.imageURI
import org.jetbrains.anko.intentFor
import toluog.campusbash.R
import toluog.campusbash.utils.AppContract
import toluog.campusbash.utils.FirebaseManager

class SetupProfileActivity : AppCompatActivity() {

    private lateinit var imagePicker: ImagePicker
    private var profileChanged = false
    private val fbManager = FirebaseManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_profile)
        val user = FirebaseManager.getUser()
        imagePicker = ImagePicker(this, null, {
            profileImage.imageURI = it
            updatePhoto(it)
        })

        if(user != null) {
            userNameView.setText(user.displayName)
        }

        profileImage.setOnClickListener { imagePicker.choosePicture(true) }

        nextButton.setOnClickListener {
            updateFields()
            startActivity(intentFor<MainActivity>())
        }

    }

    private fun updatePhoto(imageUri: Uri) {
        doAsync { fbManager.uploadProfilePhoto(imageUri) }
    }

    private fun updateFields() {
        val user = FirebaseManager.getUser()
        if(user != null) {
            fbManager.updateProfileField(AppContract.FIREBASE_USER_USERNAME,
                    userNameView.text.toString(), user)
            fbManager.updateProfileField(AppContract.FIREBASE_USER_SUMMARY,
                    summaryView.text.toString(), user)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        imagePicker.handleActivityResult(resultCode, requestCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        imagePicker.handlePermission(requestCode, grantResults)
    }
}
