package com.example.qrcodescanner

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.appcompat.app.AppCompatActivity
import com.example.qrcodescanner.databinding.ActivityLecturerBinding
import com.google.zxing.WriterException

class LecturerActivity : AppCompatActivity() {

    var img: ImageView? = null
    var btnGenerate: Button? = null
    private lateinit var binding: ActivityLecturerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLecturerBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        img = findViewById(R.id.imageView)
        btnGenerate = findViewById(R.id.button)

        btnGenerate?.setOnClickListener {
            generateQrCode()
        }
    }

    private fun generateQrCode() {
        val text: String = binding.generationText.text.toString()
        val qrGenerator = QRGEncoder(text, null, QRGContents.Type.TEXT, 200)

        try {
            val btnMap = qrGenerator.bitmap
            img?.setImageBitmap(btnMap)
        } catch (e: WriterException) {
            Log.v(TAG, e.toString())
        }
    }

}