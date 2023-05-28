package com.example.qrcodescanner.custom

data class Course(
    val className: String,
    val date: String,
    val groupNames: String,
    val students: ArrayList<Person>
)
