package com.example.qrcodescanner

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.qrcodescanner.custom.MyListViewAdapter
import com.example.qrcodescanner.custom.User
import com.example.qrcodescanner.databinding.ActivityStudentResultBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class StudentResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentResultBinding
    private lateinit var userArrayList: ArrayList<User>

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStudentResultBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        val lessons = arrayOf(
            "ВДИШП",
            "РИС",
            "Эконометрика",
            "СИТиОД",
            "Статистика",
            "КиОКИ",
            "БухУчет"
        )

        val groups = arrayOf(
            "072302",
            "072301",
            "072303",
            "072304",
            "073601",
            "073602",
            "073603"
        )

        val dateTime = arrayOf(
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
        )

        userArrayList = ArrayList()

        for (i in lessons.indices) {

            val user = User(lessons[i], groups[i], dateTime[i])
            userArrayList.add(user)

        }

        binding.listView.adapter = MyListViewAdapter(this, userArrayList)
    }

}