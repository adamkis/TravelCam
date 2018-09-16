package site.adamkis.travelcam

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private val SELECT_SINGLE_PICTURE = 101
    private val IMAGE_TYPE = "image/*"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        select_image_from_gallery.setOnClickListener {
            chooseGalleryPhoto()
        }
    }

    private fun chooseGalleryPhoto() {
        val intent = Intent()
        intent.type = IMAGE_TYPE
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent,
                getString(R.string.select_picture)), SELECT_SINGLE_PICTURE)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_SINGLE_PICTURE) {

                val selectedImageUri = data.data
                try {
                    image_preview.setImageURI(selectedImageUri)
                } catch (e: IOException) {
                    Log.e(MainActivity::class.java.simpleName, "Failed to load image", e)
                }
            }
        } else {
            // report failure
            Toast.makeText(applicationContext, R.string.msg_failed_to_get_intent_data, Toast.LENGTH_LONG).show()
            Log.d(MainActivity::class.java.simpleName, "Failed to get intent data, result code is " + resultCode)
        }
    }

}
