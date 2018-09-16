package site.adamkis.travelcam

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmark
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.io.ByteArrayOutputStream
import android.content.pm.ActivityInfo
import android.graphics.BitmapFactory
import android.support.v4.graphics.BitmapCompat


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
//                    var image = FirebaseVisionImage.fromFilePath(applicationContext, uri)
//                    image = FirebaseVisionImage.fromBitmap(bitmap)
//                    image_preview.setImageURI(selectedImageUri)
                    var bitmap = getBitmapFromUri(selectedImageUri)
                    bitmap = resizeBitmap4(bitmap)
                    image_preview.setImageBitmap(bitmap)
                    startLandmarkDetection(bitmap)
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

    private fun startLandmarkDetection(bitmap: Bitmap) {
        Log.d(MainActivity::class.java.simpleName, "Bitmap size: " + BitmapCompat.getAllocationByteCount(bitmap) + " byte")
        var image: FirebaseVisionImage? = null
        try { // TODO remove try catch
            image = FirebaseVisionImage.fromBitmap(bitmap)
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

    @Throws(IOException::class)
    private fun getBitmapFromUri(uri: Uri): Bitmap {
        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
        return bitmap
    }

    // Best of quality is 80 and more, 3 is very low quality of image
    private fun compressBitmap(src: Bitmap, format: Bitmap.CompressFormat, quality: Int): Bitmap {
        val os = ByteArrayOutputStream()
        src.compress(format, quality, os)
        val array = os.toByteArray()
        return BitmapFactory.decodeByteArray(array, 0, array.size)
    }


    private fun resizeBitmap4(input: Bitmap): Bitmap {
        var bitmap: Bitmap = input
        val MAX_IMAGE_SIZE = 7000 * 1024
        var resizeMultiplier = 1.0
        while (BitmapCompat.getAllocationByteCount(bitmap) > MAX_IMAGE_SIZE) {
            resizeMultiplier -= 0.1
            Log.d(MainActivity::class.java.simpleName, "Bitmap resize: Multiplier: $resizeMultiplier Size: ${BitmapCompat.getAllocationByteCount(bitmap)}")
            bitmap = Bitmap.createScaledBitmap(input, (input.width * resizeMultiplier).toInt(), (input.height*resizeMultiplier).toInt(), true)
        }
        return bitmap
    }


    private fun resizeBitmap3(input: Bitmap): Bitmap {
        var bitmap = input
        val MAX_IMAGE_SIZE = 7000 * 1024
        var compressQuality = 80
        while (BitmapCompat.getAllocationByteCount(bitmap) > MAX_IMAGE_SIZE) {
            compressQuality -= 10
            Log.d(MainActivity::class.java.simpleName, "Bitmap resize: Quality: $compressQuality, Size: ${BitmapCompat.getAllocationByteCount(bitmap)}")
            bitmap = compressBitmap(bitmap, Bitmap.CompressFormat.JPEG, compressQuality)
        }
        return bitmap
    }

    private fun resizeBitmap2(input: Bitmap): Bitmap {
        var bitmap = Bitmap.createScaledBitmap(input, (input.width * 0.3).toInt(), (input.height*0.3).toInt(), true)
        bitmap = compressBitmap(bitmap, Bitmap.CompressFormat.JPEG, 80)
        return bitmap
    }

    private fun resizeBitmap(bitmap: Bitmap): Bitmap {
        val MAX_IMAGE_SIZE = 7000 * 1024
        var streamLength = MAX_IMAGE_SIZE
        var compressQuality = 105
        val bmpStream = ByteArrayOutputStream()
        var bmpPicByteArray: ByteArray = "".toByteArray()
        while (streamLength >= MAX_IMAGE_SIZE && compressQuality > 5) {
            try {
                bmpStream.flush()//to avoid out of memory error
                bmpStream.reset()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            compressQuality -= 5
            bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
            bmpPicByteArray = bmpStream.toByteArray()
            streamLength = bmpPicByteArray.size
            if (BuildConfig.DEBUG) {
                Log.d(MainActivity::class.java.simpleName, "Quality: " + compressQuality)
                Log.d(MainActivity::class.java.simpleName, "Size: " + streamLength)
            }
        }
        return BitmapFactory.decodeByteArray(bmpPicByteArray, 0, bmpPicByteArray.size)
    }

}
