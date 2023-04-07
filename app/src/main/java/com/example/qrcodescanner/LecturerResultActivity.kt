package com.example.qrcodescanner

import android.os.Bundle
import android.os.PersistableBundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.example.qrcodescanner.databinding.ActivityLecturerResultBinding

class LecturerResultActivity: AppCompatActivity() {

    private lateinit var binding: ActivityLecturerResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLecturerResultBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
    }

}