package toluog.campusbash.view

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import com.myhexaville.smartimagepicker.ImagePicker
import android.os.Bundle
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_setup_profile.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.intentFor
import toluog.campusbash.R
import toluog.campusbash.utils.AppContract
import toluog.campusbash.utils.FirebaseManager
import toluog.campusbash.utils.loadImage

class SetupProfileActivity : AppCompatActivity() {

    private lateinit var imagePicker: ImagePicker
    private lateinit var viewModel: GeneralViewModel
    private val fbManager = FirebaseManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_profile)
        viewModel = ViewModelProviders.of(this).get(GeneralViewModel::class.java)
        val user = FirebaseManager.getUser()
        imagePicker = ImagePicker(this, null) {
            profileImage.loadImage(it)
            updatePhoto(it)
        }

        if(user != null) {
            setupView(user)
        }

        profileImage.setOnClickListener { imagePicker.choosePicture(true) }

        nextButton.setOnClickListener {
            if(validate()) {
                updateFields()
                startActivity(intentFor<MainActivity>())
                finish()
            }
        }

    }

    private fun updatePhoto(imageUri: Uri) {
        doAsync { fbManager.uploadProfilePhoto(imageUri) }
    }

    private fun setupView(user: FirebaseUser) {
        viewModel.getProfileInfo(user)?.observe(this, Observer {
            it?.let {
                val profile = it[AppContract.FIREBASE_USER_PHOTO_URL] as String?
                val name = it[AppContract.FIREBASE_USER_USERNAME] as String?
                val summary = it[AppContract.FIREBASE_USER_SUMMARY] as String?
                val studentId = it[AppContract.FIREBASE_USER_STUDENT_ID] as String?

                if(profile == null) {
                    profileImage.setImageResource(R.drawable.adult_emoji)
                } else {
                    profileImage.loadImage(profile)
                }
                userNameView.setText(name ?: user.displayName)
                summaryView.setText(summary)
                studentIdView.setText(studentId)
            }
        })
    }

    private fun updateFields() {
        val user = FirebaseManager.getUser()
        if(user != null) {
            fbManager.updateProfileField(AppContract.FIREBASE_USER_USERNAME,
                    userNameView.text.toString(), user)
            fbManager.updateProfileField(AppContract.FIREBASE_USER_SUMMARY,
                    summaryView.text.toString(), user)
            fbManager.updateProfileField(AppContract.FIREBASE_USER_STUDENT_ID,
                    studentIdView.text.toString(), user)
        }
    }

    private fun validate(): Boolean {
        if(userNameView.text.toString().isBlank()) {
            userNameView.error = getString(R.string.field_cant_be_empty)
            return false
        }
        return true
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
