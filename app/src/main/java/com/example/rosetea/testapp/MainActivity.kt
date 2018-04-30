package com.example.rosetea.testapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    val CAMERA_REQUEST_CODE = 0
    lateinit var imageFilePath: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Prepare the button to take an image
        cameraButton.setOnClickListener()
        {
            try {
                val imageFile = createImageFile()
                val callCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (callCameraIntent.resolveActivity(packageManager) != null)
                {
                    val authorities = packageName + ".fileprovider"
                    val imageURI = FileProvider.getUriForFile(this, authorities, imageFile)
                    callCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI)
                    startActivityForResult(callCameraIntent, CAMERA_REQUEST_CODE)
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Could not create file!", Toast.LENGTH_SHORT).show()
            }

        }
    }

    //Take an image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode)
        {
            CAMERA_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK){
                    photoImage.setImageBitmap(setScaleBitmap())
                }
            }
            else->
            {
                Toast.makeText(this, "Unrecognize request code", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //Create a file image and folder
    @Throws(IOException::class) //throw exception if something fail
    fun createImageFile() : File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (!storageDirectory.exists())
            storageDirectory.mkdir()


        val imageFile = createTempFile(imageFileName, ".jpg", storageDirectory)
        imageFilePath = imageFile.absolutePath

        return imageFile
    }

    fun setScaleBitmap() : Bitmap {
        val imageViewWidth = photoImage.width
        val imageViewHeight = photoImage.height

        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(imageFilePath, bmOptions)
        val bitmapWidth = bmOptions.outWidth
        val bitmapHeight = bmOptions.outHeight

        val scaleFactor = Math.min(bitmapWidth/imageViewWidth, bitmapHeight/imageViewHeight)
        bmOptions.inSampleSize = scaleFactor
        bmOptions.inJustDecodeBounds = false

        return BitmapFactory.decodeFile(imageFilePath, bmOptions)

    }

}
