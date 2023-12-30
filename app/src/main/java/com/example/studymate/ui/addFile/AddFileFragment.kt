package com.example.studymate.ui.addFile

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.media.ExifInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.doOnLayout
import androidx.fragment.app.FragmentTransaction
import com.example.studymate.R
import com.example.studymate.data.model.ApplicationState
import com.example.studymate.databinding.FragmentAddFileBinding
import com.example.studymate.ui.LoadingDialog
import com.example.studymate.ui.mainFragments.profile.ProfileFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.ktx.storage
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.Text.TextBlock
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.*

private const val TAG = "ALL_FILE_FRAGMENT"
private const val CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034
class AddFileFragment : Fragment() {


    companion object {
        private const val MAX_FONT_SIZE = 110F
    }

    private var _binding: FragmentAddFileBinding? = null
    private lateinit var fAuth: FirebaseAuth
    private val db = Firebase.firestore
    private val userViewModel = ApplicationState
    private lateinit var loadingDialog: LoadingDialog
    private val binding get() = _binding!!
    private val storage = Firebase.storage
    private lateinit var fileImageView: ImageView
    private lateinit var transcript: String
    private var translatedText: String? = null

    private var photoName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddFileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.addFileToolbar.setNavigationOnClickListener{
            navigateBack()
        }

        fileImageView = binding.fileImage

        loadingDialog = LoadingDialog(requireActivity())
        binding.addFileToolbar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.toolbar_add_file_submit -> {
                    if(binding.fileName.text?.isEmpty() == true) binding.fileName.error = "Please enter a file name"
                    else if(binding.fileDescription.text?.isEmpty() == true) binding.fileDescription.error = "Please enter a description"
                    else if(fileImageView.drawable == null ) Toast.makeText(context, "Please take a photo", Toast.LENGTH_SHORT).show()
                    else {
                        fileImageView.isDrawingCacheEnabled = true
                        fileImageView.buildDrawingCache()
                        val bitmap = (fileImageView.drawable as BitmapDrawable).bitmap
                        val baos = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                        val data = baos.toByteArray()
                        val name = binding.fileName.text.toString()
                        addFile(name, data)
                    }
                    true
                }
                else -> false
            }
        }

        binding.takeFilePhoto.setOnClickListener {
            photoName = "IMG_${Date()}.JPG"
            val photoFile = File(
                requireContext().applicationContext.filesDir,
                photoName
            )
            val photoUri = FileProvider.getUriForFile(
                requireContext(),
                "com.studymate.FileProvider",
                photoFile
            )

            takePhoto.launch(photoUri)
        }
    }

    private fun runTextTranslation(text: String) {
        if(binding.translateFromLanguage.selectedItem.toString() == binding.translateToLanguage.selectedItem.toString()) {
            loadingDialog.dismiss()
            return
        }
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(getLanguageCode(binding.translateFromLanguage.selectedItem.toString()))
            .setTargetLanguage(getLanguageCode(binding.translateToLanguage.selectedItem.toString()))
            .build()
        val translator = Translation.getClient(options)
        var conditions = DownloadConditions.Builder()
            .build()
        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                translator.translate(text)
                    .addOnSuccessListener { translatedText ->
                        this.translatedText = translatedText
                    }
                    .addOnCompleteListener {
                        loadingDialog.dismiss()
                    }
            }
            .addOnFailureListener {
                loadingDialog.dismiss()
                Toast.makeText(context, "Failed to download language model", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getLanguageCode(language: String): String {
        return when (language) {
            "English" -> TranslateLanguage.ENGLISH
            "French" -> TranslateLanguage.FRENCH
            "German" -> TranslateLanguage.GERMAN
            "Spanish" -> TranslateLanguage.SPANISH
            "Italian" -> TranslateLanguage.ITALIAN
            "Portuguese" -> TranslateLanguage.PORTUGUESE
            "Dutch" -> TranslateLanguage.DUTCH
            "Russian" -> TranslateLanguage.RUSSIAN
            "Chinese" -> TranslateLanguage.CHINESE
            "Japanese" -> TranslateLanguage.JAPANESE
            else -> ""
        }
    }

    private fun addFile(fileName: String, bitmap: ByteArray) {
        loadingDialog.show()
        val storageRef = storage.reference
        val path = "${userViewModel.getUsername().value}/$fileName"
        val newFileRef = storageRef.child(path)


        var uploadTask = newFileRef.putBytes(bitmap)
        Log.d(TAG, "In Add file")
        newFileRef.metadata
            .addOnCompleteListener {
                Log.d(TAG, "IN complete")
                Log.d(TAG, it.toString())

                if(it.isSuccessful && it.result.name != null){
                    //TODO: File already exists with that name
                    Log.d(TAG, "SUCCESSFUL")
                    Log.d(TAG, it.toString())
                    loadingDialog.dismiss()
                    Toast.makeText(context, "File Name already in use", Toast.LENGTH_SHORT).show()
                }
                else if(!it.isSuccessful){
                    Log.d(TAG, "NO SUCCESSFUL")
                    //CASE 2: Can add file
                    val errorCode = (it.exception as StorageException).errorCode
                    if(errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
                        uploadTask
                            .addOnFailureListener {exception ->
                                // Handle unsuccessful uploads
                                Log.d(TAG, "REGISTER FAILURE")
                                Log.d(TAG, exception.toString())
                                loadingDialog.dismiss()
                                Toast.makeText(context, "Error uploading photo, please try again", Toast.LENGTH_SHORT).show()
                            }.addOnSuccessListener { taskSnapshot ->
                                newFileRef.downloadUrl
                                    .addOnSuccessListener {url ->
                                        Log.d(TAG, "REGISTER SUCC")
                                        Log.d(TAG, taskSnapshot.toString())
                                        Log.d(TAG, taskSnapshot.metadata.toString())
                                        val description = binding.fileDescription.text.toString()
                                        val date = Date()
                                        val fileToAdd = hashMapOf(
                                            "filePath" to path,
                                            "name" to fileName,
                                            "description" to description,
                                            "tanslateLanguage" to "English",
                                            "transscript" to transcript,
                                            "creator" to userViewModel.getUsername().value,
                                            "createdDate" to date,
                                            "downloadURL" to url,
                                            "translatedText" to translatedText
                                        )
                                        db.collection("files").add(fileToAdd)
                                            .addOnCompleteListener {task ->
                                                loadingDialog.dismiss()
                                                if(task.isSuccessful) {
                                                    userViewModel.addFile(com.example.studymate.data.model.File(path, fileName, description,
                                                        "English",  userViewModel.getUsername().value, transcript, date,
                                                        url.toString(), translatedText
                                                    ))
                                                    navigateBack()
                                                }
                                                else {
                                                    Toast.makeText(context, "Error uploading photo, please try again", Toast.LENGTH_SHORT).show()
                                                }
                                            }

                                    }
                                    }.addOnFailureListener { exception ->
                                        Log.d(TAG, "REGISTER FAILURE")
                                        Log.d(TAG, exception.toString())
                                        loadingDialog.dismiss()
                                        Toast.makeText(context, "Error uploading photo, please try again", Toast.LENGTH_SHORT).show()
                                    }
                    }
                }
            }

    }

    private fun navigateBack() {
        if (parentFragmentManager.backStackEntryCount > 1)
            parentFragmentManager.popBackStack()
        else {
            val ft: FragmentTransaction = parentFragmentManager.beginTransaction()
            ft.replace(R.id.main_fragment, ProfileFragment())
            ft.commit()
        }
    }

    private fun updatePhoto(photoFileName: String?) {
        val imageView = binding.fileImage

        Log.d("WORKING", "THe photo id is $photoFileName")
        Log.d("WORKING", "The check val is: ${imageView.tag != photoFileName}")
        //Commented this out so it works on updating the photo with face detection results
        //if (imageView.tag != photoFileName) {
        Log.d("WORKING", "check1")
        val photoFile = photoFileName?.let {
            File(requireContext().applicationContext.filesDir, it)
        }
        if (photoFile?.exists() == true) {
            Log.d("WORKING", "check2")
            imageView.doOnLayout { measuredView ->
                val scaledBitmap = getScaledBitmap(
                    photoFile.path,
                    measuredView.width,
                    measuredView.height
                )
                imageView.setImageBitmap(scaledBitmap)
                imageView.tag = photoFileName
                imageView.contentDescription =
                    getString(R.string.study_file_added)
            }
        } else {
            imageView.setImageBitmap(null)
            imageView.tag = null
            imageView.contentDescription =
                getString(R.string.no_study_file_added)
        }
    }

    private fun File.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int) {
        outputStream().use { out ->
            bitmap.compress(format, quality, out)
            out.flush()
            Log.d(TAG, "Done writing")
        }
    }

    private fun getBitmapFromFile(photoFile: File): Bitmap? {
        if (photoFile.exists()) {
            return BitmapFactory.decodeFile(photoFile.path)
        }
        return null
    }

    private val takePhoto = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { didTakePhoto: Boolean ->
        if (didTakePhoto && photoName != null) {
            val photoFile = File(requireContext().applicationContext.filesDir, photoName)
            var bitmap: Bitmap? = rotateImageFromCamera(photoFile.path)
            if (bitmap != null) {
                photoFile.writeBitmap(bitmap, Bitmap.CompressFormat.PNG, 85)
            }
            if(bitmap == null) bitmap = getBitmapFromFile(photoFile)


            if (bitmap != null) {
                runTextRecognition(photoFile, bitmap)
            }


        }
    }

    private fun runTextRecognition(photoFile: File, initBitmap: Bitmap) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        loadingDialog.show()
        updatePhoto(photoName)
        transcript = ""
        val result = recognizer.process(InputImage.fromBitmap(initBitmap, 0))
            .addOnSuccessListener { visionText ->
                // Task completed successfully
                // ...
                Log.d(TAG, "Successful Text Detection")
                Log.d(TAG, visionText.toString())
                transcript = visionText.text
                Log.d(TAG, "The full text is:  ${visionText.text}")
                runTextTranslation(visionText.text)
                var bitmap: Bitmap? = getBitmapFromFile(photoFile)
                if (bitmap != null) {
                    bitmap = drawDetectionResult(bitmap, visionText.textBlocks)
                    photoFile.writeBitmap(bitmap, Bitmap.CompressFormat.PNG, 85)
                    updatePhoto(photoName)
                }
            }
            .addOnFailureListener { e ->
                loadingDialog.dismiss()
                Toast.makeText(context, "Error running text recognition", Toast.LENGTH_SHORT).show()
            }
    }

    private fun drawDetectionResult(
        bitmap: Bitmap,
        detectionResults: List<TextBlock>
    ): Bitmap {
        val outputBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(outputBitmap)
        val pen = Paint()
        pen.textAlign = Paint.Align.LEFT
        Log.d(TAG, "In draw results")

        detectionResults.forEachIndexed {index, block ->

            // draw bounding box
            pen.color = Color.RED
            pen.strokeWidth = 20F
            pen.style = Paint.Style.STROKE
            val box = block.boundingBox ?: return outputBitmap
            canvas.drawRect(box, pen)

            val tagSize = Rect(0, 0, 0, 0)

            // calculate the right font size
            pen.style = Paint.Style.FILL_AND_STROKE
            pen.color = Color.YELLOW
            pen.strokeWidth = 2F

            pen.textSize = MAX_FONT_SIZE
            pen.getTextBounds("Face", 0, "Face".length, tagSize)
            val fontSize: Float = pen.textSize * box.width() / tagSize.width()

            // adjust the font size so texts are inside the bounding box
            if (fontSize < pen.textSize) pen.textSize = fontSize

            var margin = (box.width() - tagSize.width()) / 2.0F
            if (margin < 0F) margin = 0F
            canvas.drawText(
                block.text, box.left + margin,
                box.top + tagSize.height().times(1F), pen
            )
        }
        return outputBitmap
    }


    private fun rotateImageFromCamera(photoFilePath: String): Bitmap? {
        // Create and configure BitmapFactory
        // Create and configure BitmapFactory
        val bounds = BitmapFactory.Options()
        bounds.inJustDecodeBounds = true
        BitmapFactory.decodeFile(photoFilePath, bounds)
        val opts = BitmapFactory.Options()
        val bm = BitmapFactory.decodeFile(photoFilePath, opts)
        // Read EXIF Data
        // Read EXIF Data
        var exif: ExifInterface? = null
        try {
            exif = ExifInterface(photoFilePath)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val orientString =
            exif!!.getAttribute(ExifInterface.TAG_ORIENTATION)
        val orientation =
            orientString?.toInt() ?: ExifInterface.ORIENTATION_NORMAL
        var rotationAngle = 0
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90
        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180
        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270

        val matrix = Matrix()
        matrix.setRotate(rotationAngle.toFloat(), bm.width.toFloat() / 2, bm.height.toFloat() / 2)

        return Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true)
    }
}