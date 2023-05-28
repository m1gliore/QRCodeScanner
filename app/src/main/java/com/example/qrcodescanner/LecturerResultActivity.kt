package com.example.qrcodescanner

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.qrcodescanner.custom.*
import com.example.qrcodescanner.databinding.ActivityLecturerResultBinding
import com.google.gson.Gson
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class LecturerResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLecturerResultBinding
    private lateinit var userArrayList: ArrayList<Course>
    private var jwtToken: String? = ""

    private fun getJwtToken(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("jwtToken", null)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLecturerResultBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        jwtToken = getJwtToken(this)

        userArrayList = ArrayList()

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $jwtToken")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()

        val request = Request.Builder()
            .url("https://qr-codes.onrender.com/api/lecturers/my-couples")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@LecturerResultActivity, "Ошибка при выполнении запроса", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body
                if (responseBody != null) {
                    val responseData = responseBody.string()
                    runOnUiThread {
                        try {
                            val courses = parseCourses(responseData)
                            userArrayList.addAll(courses)
                            binding.listViewLecturer.adapter =
                                MyListViewCourseAdapter(this@LecturerResultActivity, userArrayList)
                        } catch (e: JSONException) {
                            runOnUiThread {
                                Toast.makeText(this@LecturerResultActivity, "Ошибка при обработке данных", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                }
            }
        })

        binding.listViewLecturer.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                val selectedUser = parent.getItemAtPosition(position) as Course
                val students = selectedUser.students
                val studentsJson = Gson().toJson(students)

                val intent = Intent(this, StudentsListActivity::class.java)
                intent.putExtra("students", studentsJson)
                startActivity(intent)
            }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_layout, menu)
        return true
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

    private fun parseCourses(responseData: String): List<Course> {
        val courses = mutableListOf<Course>()
        try {
            val jsonArray = JSONArray(responseData)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val date = formatDate(jsonObject.getString("date"))
                val className = jsonObject.getString("className")
                val groupNames = jsonObject.getString("groupNames")
                val studentsArray = jsonObject.getJSONArray("students")
                val students = ArrayList<Person>()

                for (j in 0 until studentsArray.length()) {
                    val studentObject = studentsArray.getJSONObject(j)
                    val studentId = studentObject.getInt("id")
                    val studentName = studentObject.getString("name")
                    val studentUsername = studentObject.getString("username")
                    val student = Person(studentId, studentName, studentUsername)
                    students.add(student)
                }

                val course = Course(className, date, groupNames, students)
                courses.add(course)
            }
        } catch (e: JSONException) {
            runOnUiThread {
                Toast.makeText(this@LecturerResultActivity, "Ошибка при обработке данных", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        return courses
    }

    private fun formatDate(dateTime: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val date: Date? = inputFormat.parse(dateTime)
        return if (date != null) {
            outputFormat.format(date)
        } else {
            ""
        }
    }
}
