package com.example.qrcodescanner.custom

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class Person(
    val className: String?,
    val groupNames: String?,
    val date: LocalDateTime?
)
