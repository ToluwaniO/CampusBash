package toluog.campusbash.view

import android.app.ProgressDialog
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
import org.json.JSONObject
import toluog.campusbash.R
import toluog.campusbash.utils.AppContract
import toluog.campusbash.utils.FirebaseManager
import toluog.campusbash.utils.Util
import toluog.campusbash.utils.loadImage

class SetupProfileActivity : AppCompatActivity() {

    private lateinit var imagePicker: ImagePicker
    private lateinit var viewModel: GeneralViewModel
    private val fbManager = FirebaseManager()
    private lateinit var dialog: ProgressDialog
    private val TAG = SetupProfileActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_profile)
        dialog = indeterminateProgressDialog(message = R.string.please_wait)
        dialog.dismiss()
        viewModel = ViewModelProviders.of(this).get(GeneralViewModel::class.java)
        val user = FirebaseManager.getUser()
        imagePicker = ImagePicker(this, null) {
            if (Util.validImageSize(it, this)) {
                profileImage.loadImage(it)
                updatePhoto(it)
            } else {
                snackbar(container, R.string.max_image_size)
            }
        }

        if(user != null) {
            setupView(user)
        }

        profileImage.setOnClickListener { imagePicker.choosePicture(true) }

        nextButton.setOnClickListener {
            postData()
        }
    }

    override fun onDestroy() {
        dialog.dismiss()
        super.onDestroy()
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
                    userNameView.text.toString().trim(), user)
            fbManager.updateProfileField(AppContract.FIREBASE_USER_SUMMARY,
                    summaryView.text.toString().trim(), user)
            fbManager.updateProfileField(AppContract.FIREBASE_USER_STUDENT_ID,
                    fixStudentId(studentIdView.text.toString().trim()), user)
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
        dialog.show()
        val studentId = fixStudentId(studentIdView.text.toString().trim())
        Log.d(TAG, studentId)
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

    private fun fixStudentId(id: String): String {
        Log.d(TAG, id)
        var studentNumber = id
        while (studentNumber.startsWith("0")) {
            studentNumber = studentNumber.removePrefix("0")
        }
        Log.d(TAG, studentNumber)
        return studentNumber
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
