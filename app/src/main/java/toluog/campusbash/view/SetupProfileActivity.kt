package toluog.campusbash.view

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import com.myhexaville.smartimagepicker.ImagePicker
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_setup_profile.*
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.progressDialog
import org.json.JSONObject
import toluog.campusbash.R
import toluog.campusbash.utils.AppContract
import toluog.campusbash.utils.FirebaseManager
import toluog.campusbash.utils.loadImage

class SetupProfileActivity : AppCompatActivity() {

    private lateinit var imagePicker: ImagePicker
    private lateinit var viewModel: GeneralViewModel
    private val fbManager = FirebaseManager()
    private val TAG = SetupProfileActivity::class.java.simpleName

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
            postData()
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

    private fun postData() {
        if(!validate()) return
        val dialog = indeterminateProgressDialog(message = R.string.please_wait)
        dialog.show()
        val studentId = studentIdView.text.toString()
        if (!studentId.isBlank()) {
            FirebaseManager.isNewStudentId(studentId).addOnSuccessListener {
                Log.d(TAG, it)
                val obj = JSONObject(it)
                val isNew: Boolean? = if (obj.has("isNew")) {
                    obj.getBoolean("isNew")
                } else {
                    null
                }
                when {
                    isNew == null -> longSnackbar(container, R.string.could_not_verify_student_id)
                    isNew -> {
                        updateFields()
                        startActivity(intentFor<MainActivity>())
                        finish()
                    }
                    else -> studentIdView.error = getString(R.string.student_id_exists)
                }
                dialog.dismiss()
            }.addOnFailureListener {
                Log.d(TAG, it.message)
                dialog.dismiss()
            }
        } else {
            updateFields()
            dialog.dismiss()
            startActivity(intentFor<MainActivity>())
            finish()
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
