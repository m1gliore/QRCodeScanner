package com.example.qrcodescanner

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.qrcodescanner.databinding.ActivityLecturerBinding
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.android.gms.location.*
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URL

class LecturerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLecturerBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private val permissionRequestAccessLocation = 100
    private var tvLatitude: Double = 0.0
    private var tvLongitude: Double = 0.0
    private var client: OkHttpClient = OkHttpClient()
    private var jwtToken: String? = ""

    private fun getJwtToken(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("jwtToken", null)
    }

    private fun getName(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("name", null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLecturerBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = createLocationRequest()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    tvLatitude = location.latitude
                    tvLongitude = location.longitude
                }
            }
        }

        val btnGenerate: Button = findViewById(R.id.button)
        val header: TextView = findViewById(R.id.textView2)
        val name = getName(this)
        val greeting = getString(R.string.greeting, name)
        header.text = greeting
        jwtToken = getJwtToken(this)

        btnGenerate.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                generateQrCode()
            }
        }
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
            R.id.action_settings -> {
                val role = getRole(this)
                if (role == "ROLE_STUDENT") {
                    startActivity(Intent(this, StudentResultActivity::class.java))
                } else if (role == "ROLE_LECTURER") {
                    startActivity(Intent(this, LecturerResultActivity::class.java))
                }
                true
            }
            R.id.action_exit -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getRole(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("role", null)
    }

    override fun onResume() {
        super.onResume()
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                startLocationUpdates()
            } else {
                openLocationSettings()
            }
        } else {
            requestLocationPermission()
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    @SuppressLint("VisibleForTests")
    private fun createLocationRequest(): LocationRequest {
        val locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        return locationRequest
    }

    private fun startLocationUpdates() {
        if (checkPermissions()) {
            try {
                fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    null
                )
            } catch (e: SecurityException) {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            permissionRequestAccessLocation
        )
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == permissionRequestAccessLocation) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (isLocationEnabled()) {
                    Toast.makeText(
                        applicationContext,
                        "Location Permission Granted",
                        Toast.LENGTH_SHORT
                    ).show()
                    startLocationUpdates()
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Location is disabled",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(applicationContext, "Location Permission Denied", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun openLocationSettings() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }

    private suspend fun postToken(
        geoWidth: Double,
        geoHeight: Double,
        className: String,
        groupNames: String
    ): String = withContext(Dispatchers.IO) {
        val url = URL("https://qr-codes.onrender.com/api/qr/")
        val mapper = ObjectMapper()
        val jacksonObj = mapper.createObjectNode()
        jacksonObj.put("geoWidth", geoWidth)
        jacksonObj.put("geoHeight", geoHeight)
        jacksonObj.put("className", className)
        jacksonObj.put("groupNames", groupNames)
        val jacksonString = jacksonObj.toString()

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jacksonString.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $jwtToken")
            .post(body)
            .build()

        val response = client.newCall(request).execute()

        return@withContext response.body?.string() ?: ""
    }

    private suspend fun generateQrCode() {
        val className: String = binding.lesson.text.toString()
        val groupNames: String = binding.group.text.toString()

        val token: String = postToken(tvLatitude, tvLongitude, className, groupNames)

        val qrCodeWriter = QRCodeWriter()
        val matrix: BitMatrix

        try {
            matrix = qrCodeWriter.encode(token, BarcodeFormat.QR_CODE, 2500, 2500)
        } catch (e: WriterException) {
            Log.v("LecturerActivity", e.toString())
            return
        }

        val width = matrix.width
        val height = matrix.height
        val pixels = IntArray(width * height)

        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (matrix.get(x, y)) Color.BLACK else Color.WHITE
            }
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)

        withContext(Dispatchers.Main) {
            binding.imageView.setImageBitmap(bitmap)
        }
    }
}
