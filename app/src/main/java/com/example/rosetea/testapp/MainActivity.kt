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
import android.support.v7.app.AlertDialog
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import android.net.Uri


class MainActivity : AppCompatActivity() {

    lateinit var imageFilePath: String
    private val GALLERY = 1
    private val CAMERA = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Prepare the button to take an image
        cameraButton.setOnClickListener()
        {
            showPictureDialog()
//            try {
//                val imageFile = createImageFile()
//                val callCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//                if (callCameraIntent.resolveActivity(packageManager) != null)
//                {
//                    val authorities = packageName + ".fileprovider"
//                    val imageURI = FileProvider.getUriForFile(this, authorities, imageFile)
//                    callCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI)
//                    startActivityForResult(callCameraIntent, CAMERA_REQUEST_CODE)
//                }
//            } catch (e: Exception) {
//                Toast.makeText(this, "Could not create file!", Toast.LENGTH_SHORT).show()
//            }
        }
    }

    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle("Select Action")
        val pictureDialogItems = arrayOf("Select photo from gallery", "Capture photo from camera")
        pictureDialog.setItems(pictureDialogItems
        ) { dialog, which ->
            when (which) {
                0 -> choosePhotoFromGallary()
                1 -> takePhotoFromCamera()
            }
        }
        pictureDialog.show()
    }


    fun choosePhotoFromGallary() {
        val galleryIntent = Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        startActivityForResult(galleryIntent, GALLERY)
    }

    private fun takePhotoFromCamera() {
        try {
                val imageFile = createImageFile()
                val callCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (callCameraIntent.resolveActivity(packageManager) != null)
                {
                    val authorities = packageName + ".fileprovider"
                    val imageURI = FileProvider.getUriForFile(this, authorities, imageFile)
                    callCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI)
                    startActivityForResult(callCameraIntent, CAMERA)
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Could not create file!", Toast.LENGTH_SHORT).show()
            }
    }

    //Take an image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode)
        {
            CAMERA -> {

                if (resultCode == Activity.RESULT_OK){
                    photoImage.setImageBitmap(setScaleBitmap())
                }
            }
            GALLERY ->
            {
                if (data != null)
                {
                    val contentURI = data!!.data
                    imageFilePath =data.data.path
                    try
                    {
                        val selectedImageURI = data.data
                        val imageFile = File(getRealPathFromURI(selectedImageURI))
                        imageFilePath = imageFile.absolutePath
                       photoImage.setImageBitmap(setScaleBitmap())

                    }
                    catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(this@MainActivity, "Failed!", Toast.LENGTH_SHORT).show()
                    }

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

    private fun getRealPathFromURI(contentURI: Uri): String {
        val result: String
        val cursor = contentResolver.query(contentURI, null, null, null, null)
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath()
        } else {
            cursor.moveToFirst()
            val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            result = cursor.getString(idx)
            cursor.close()
        }
        return result
    }
}
