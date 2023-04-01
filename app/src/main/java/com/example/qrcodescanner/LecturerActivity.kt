package com.example.qrcodescanner

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.qrcodescanner.databinding.ActivityLecturerBinding
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.zxing.WriterException
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URL
import java.time.LocalDateTime
import java.util.*

class LecturerActivity : AppCompatActivity(), View.OnClickListener {

    private var img: ImageView? = null
    private var btnGenerate: Button? = null
    private lateinit var binding: ActivityLecturerBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var tvLatitude: Double = 0.0
    private var tvLongitude: Double = 0.0
    var client: OkHttpClient = OkHttpClient()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLecturerBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        img = findViewById(R.id.imageView)
        btnGenerate = findViewById(R.id.button)

        btnGenerate?.setOnClickListener {
            Thread(Runnable {
                generateQrCode()
            }).start()
        }

        binding.student.setOnClickListener(this)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        getCurrentLocation()
    }

    private fun postToken(
        userId: String,
        date: LocalDateTime,
        geoWidth: Double,
        geoHeight: Double
    ): String {
        val client = OkHttpClient()
        val url = URL("https://qr-codes.onrender.com/api/qr/")

        val mapperAll = ObjectMapper()
        val jacksonObj = mapperAll.createObjectNode()
        jacksonObj.put("userId", userId)
        jacksonObj.put("date", date.toString())
        jacksonObj.put("geoWidth", geoWidth)
        jacksonObj.put("geoHeight", geoHeight)
        val jacksonString = jacksonObj.toString()

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jacksonString.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        val response = client.newCall(request).execute()

        return response.body!!.string()
    }

    private fun getUserId(): String {
        val url = URL("https://qr-codes.onrender.com/api/user/me")

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        val response = client.newCall(request).execute()

        Log.d("LecturerActivity", response.body!!.string())
        return response.body!!.string()
    }

    private fun getCurrentLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermission()
                    return
                }
                fusedLocationProviderClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        Toast.makeText(this, "Null Received", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Get Success", Toast.LENGTH_SHORT).show()
                        tvLatitude = location.latitude
                        tvLongitude = location.longitude
                    }
                }
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermission()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_REQUEST_ACCESS_LOCATION
        )
    }

    companion object {
        private const val PERMISSION_REQUEST_ACCESS_LOCATION = 100
    }

    private fun checkPermissions(): Boolean {

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }

        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_ACCESS_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(applicationContext, "Granted", Toast.LENGTH_SHORT).show()
                getCurrentLocation()
            } else {
                Toast.makeText(applicationContext, "Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun generateQrCode() {
        val date: LocalDateTime = LocalDateTime.now()
        val userId: String = getUserId()
        val token: String = postToken(userId, date, tvLatitude, tvLongitude)
        val qrGenerator = QRGEncoder(token, null, QRGContents.Type.TEXT, 200)

        try {
            val btnMap = qrGenerator.bitmap
            img?.setImageBitmap(btnMap)
        } catch (e: WriterException) {
            Log.v("LecturerActivity", e.toString())
        }
    }

    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                R.id.student -> {
                    startActivity(Intent(this, StudentActivity::class.java))
                }
            }
        }
    }

}