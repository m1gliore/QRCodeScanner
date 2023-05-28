package com.example.qrcodescanner

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.qrcodescanner.custom.Person
import com.example.qrcodescanner.custom.StudentsAdapter
import com.example.qrcodescanner.databinding.ActivityStudentsListBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class StudentsListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentsListBinding
    private lateinit var studentsAdapter: StudentsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentsListBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        val studentsJson = intent.getStringExtra("students")
        val listType = object : TypeToken<List<Person>>() {}.type
        val students: ArrayList<Person> = Gson().fromJson(studentsJson, listType)

        studentsAdapter = StudentsAdapter(this, students)
        binding.listViewLecturer.adapter = studentsAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_layout, menu)
        return true
    }

    private fun logout() {
        val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString("jwtToken", null)
        editor.apply()

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_exit -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
