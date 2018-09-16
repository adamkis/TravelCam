package site.adamkis.travelcam

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmark
import com.google.firebase.ml.vision.common.FirebaseVisionImage
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
                    startLandmarkDetection(selectedImageUri)
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

    private fun startLandmarkDetection(uri: Uri) {
        var image: FirebaseVisionImage? = null
        try {
            image = FirebaseVisionImage.fromFilePath(applicationContext, uri)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        checkNotNull(image)
        val detector = FirebaseVision.getInstance().visionCloudLandmarkDetector
        // Or, to change the default settings:
        // FirebaseVisionCloudLandmarkDetector detector = FirebaseVision.getInstance()
        //         .getVisionCloudLandmarkDetector(options);
        showLoading(true)
        val result = detector.detectInImage(image!!)
                .addOnSuccessListener {
                    showLoading(false)
                    processLandmarks(it)
                }
                .addOnFailureListener {
                    // report failure
                    showLoading(false)
                    Toast.makeText(applicationContext, "Landmark Finding Task Failed", Toast.LENGTH_LONG).show()
                    Log.d(MainActivity::class.java.simpleName, "Landmark Finding Task Failed")
                    results.text = it.message
                }

    }

    private fun processLandmarks(landmarks: List<FirebaseVisionCloudLandmark>) {
        var resultsText = ""
        if (landmarks.isEmpty()){
            resultsText = "No match unfortunately"
        }
        for (landmark in landmarks) {
            val bounds: Rect? = landmark.boundingBox
            val landmarkName: String = landmark.landmark
            val entityId: String = landmark.entityId
            val confidence: Float = landmark.confidence

            // Multiple locations are possible, e.g., the location of the depicted
            // landmark and the location the picture was taken.
            for (loc in landmark.locations) {
                val latitude = loc.latitude
                val longitude = loc.longitude
            }
            resultsText += "-----------\n"
            resultsText += "${landmark.landmark}\n"
            resultsText += "Confidence: ${landmark.confidence*100}%\n"
        }
        results.text = resultsText
        Toast.makeText(applicationContext, resultsText, Toast.LENGTH_LONG).show()
        Log.d(MainActivity::class.java.simpleName, resultsText)
    }

    private fun showLoading(isLoading: Boolean) {
        var textToShow = "Loading Ended"
        if (isLoading){
            textToShow = "Loading Started"
        }
        results.text = textToShow
        Toast.makeText(applicationContext, textToShow, Toast.LENGTH_LONG).show()
        Log.d(MainActivity::class.java.simpleName, textToShow)
    }

//    @Throws(IOException::class)
//    private fun getBitmapFromUri(uri: Uri): Bitmap {
//        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
//        return bitmap
//    }

}
