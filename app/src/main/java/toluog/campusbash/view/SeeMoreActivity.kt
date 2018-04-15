package toluog.campusbash.view

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_see_more.*
import toluog.campusbash.R
import toluog.campusbash.utils.AppContract

class SeeMoreActivity : AppCompatActivity() {

    private lateinit var moreText: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_see_more)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        moreText = intent.extras[AppContract.MORE_TEXT] as String
        more_text.text = moreText
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        when (id){
            android.R.id.home -> onBackPressed()
        }
        return true
    }

}
