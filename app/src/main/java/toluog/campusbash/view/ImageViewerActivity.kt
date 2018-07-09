package toluog.campusbash.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.activity_image_viewer.*
import toluog.campusbash.R
import toluog.campusbash.utils.AppContract
import toluog.campusbash.utils.loadImage

class ImageViewerActivity : AppCompatActivity() {
    private var imageSource: Any? = null
    private var imageTitle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_viewer)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val bundle = intent.extras
        imageSource = bundle.get(AppContract.IMAGE_SRC)
        imageTitle = bundle.getString(AppContract.IMAGE_NAME)

        src_image.loadImage(imageSource)
        if(imageTitle != null) {
            image_name.visibility = View.VISIBLE
            image_name.text = imageTitle
        } else {
            image_name.visibility = View.INVISIBLE
        }

    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.view_image_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        when (id){
            android.R.id.home -> onBackPressed()
            R.id.menu_share -> share()
        }
        return true
    }

    private fun share() {
        if(imageSource == null) return
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_TEXT, imageSource.toString())
        shareIntent.type = "text/plain"
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_with)))
    }
}
