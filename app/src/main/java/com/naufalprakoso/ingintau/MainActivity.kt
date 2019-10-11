package com.naufalprakoso.ingintau

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import kotlinx.android.synthetic.main.activity_main.*

import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)
        btn_choose_image.setOnClickListener { setChooseImage() }
    }

    private fun setChooseImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                val selectedImageUri = data.data
                var bitmap: Bitmap? = null
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImageUri)
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                if (null != selectedImageUri) iv_image.setImageURI(selectedImageUri)
                runTextRecognition(bitmap)
            }
        }
    }

    private fun runTextRecognition(mSelectedImage: Bitmap?) {
        val image = FirebaseVisionImage.fromBitmap(mSelectedImage!!)
        val detector = FirebaseVision.getInstance().visionTextDetector
        detector.detectInImage(image).addOnSuccessListener { processTextRecognitionResult(it) }
    }

    @SuppressLint("SetTextI18n")
    private fun processTextRecognitionResult(texts: FirebaseVisionText) {
        val blocks = texts.blocks
        if (blocks.size == 0) {
            tv_text.text = "No text found!"
            return
        }
        val sb = StringBuilder()
        for (i in blocks.indices) {
            val lines = blocks[i].lines
            for (j in lines.indices) {
                val elements = lines[j].elements
                for (k in elements.indices) sb.append(elements[k].text).append(" ")
                sb.append("\n")
            }
            sb.append("\n")
        }

        val dataSet = arrayOf("sabeb", "nich", "kuy", "afgan", "nego", "cmiiw", "oot", "ily", "idk", "fyi", "takis", "sabi", "sa ae", "yxgq", "dah", "sokin", "skuy", "sans", "mantul", "komuk", "kepo", "sokap")
        val resultSet = arrayOf("bebas", "nih", "yuk", "sadis", "nawar", "correct me if im wrong", "out of topic", "i like you", "i dont know", "for your information", "sikat", "bisa", "bisa aja", "ya kali enggak", "deh", "sini", "yuk", "santai", "mantap", "muka", "ingin tau", "so kenal")

        val result = sb.toString()

        for (i in dataSet.indices) {
            if (containsIgnoreCase(result, dataSet[i])) {
                val idxStart = result.toLowerCase(Locale.getDefault()).indexOf(dataSet[i].toLowerCase(Locale.getDefault()))
                sb.replace(idxStart, idxStart + dataSet[i].length, resultSet[i])
            }
        }

        tv_text.text = sb.toString()
    }

    private fun containsIgnoreCase(str: String?, searchStr: String?): Boolean {
        if (str == null || searchStr == null) return false

        val length = searchStr.length
        if (length == 0)
            return true

        for (i in str.length - length downTo 0) {
            if (str.regionMatches(i, searchStr, 0, length, ignoreCase = true))
                return true
        }
        return false
    }
}

