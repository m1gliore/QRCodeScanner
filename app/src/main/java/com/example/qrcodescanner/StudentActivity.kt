package com.example.qrcodescanner

import android.Manifest.permission
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.*
import com.example.qrcodescanner.databinding.ActivityStudentBinding
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.net.URL


class StudentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentBinding
    private lateinit var codeScanner: CodeScanner
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private val permissionRequestAccessLocation = 100
    private var tvLatitude: Double = 0.0
    private var tvLongitude: Double = 0.0
    private lateinit var retrofit: Retrofit
    private var client: OkHttpClient = OkHttpClient()
    private var jwtToken: String? = ""

    private suspend fun patchToken(
        jwtToken: String,
        token: String,
        geoWidth: Double,
        geoHeight: Double
    ): String = withContext(Dispatchers.IO) {
        val url = URL("https://qr-codes.onrender.com/api/qr/")
        val mapper = ObjectMapper()
        val jacksonObj = mapper.createObjectNode()
        jacksonObj.put("token", token)
        jacksonObj.put("geoWidth", geoWidth)
        jacksonObj.put("geoHeight", geoHeight)
        val jacksonString = jacksonObj.toString()

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jacksonString.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $jwtToken")
            .patch(body)
            .build()

        val response = client.newCall(request).execute()

        return@withContext response.body?.string() ?: ""
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

    private fun getJwtToken(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("jwtToken", null)
    }

    private fun getRole(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("role", null)
    }

    private var hasCameraPermission = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStudentBinding.inflate(LayoutInflater.from(this))
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

        jwtToken = getJwtToken(this)

        if (ContextCompat.checkSelfPermission(
                this,
                permission.CAMERA
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(permission.CAMERA), 123)
        } else {
            hasCameraPermission = true
        }

        retrofit = Retrofit.Builder()
            .baseUrl("https://qr-codes.onrender.com/")
            .addConverterFactory(JacksonConverterFactory.create())
            .client(OkHttpClient())
            .build()
    }

    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)

    private fun startScanning() {
        if (hasCameraPermission) {
            val scannerView: CodeScannerView = findViewById(R.id.scanner_view)
            codeScanner = CodeScanner(this, scannerView)
            codeScanner.camera = CodeScanner.CAMERA_BACK
            codeScanner.formats = CodeScanner.ALL_FORMATS

            codeScanner.autoFocusMode = AutoFocusMode.SAFE
            codeScanner.scanMode = ScanMode.SINGLE
            codeScanner.isAutoFocusEnabled = true
            codeScanner.isFlashEnabled = false

            codeScanner.decodeCallback = DecodeCallback { result ->

                coroutineScope.launch(Dispatchers.IO) {
                    try {
                        val response = withContext(Dispatchers.IO) {
                            patchToken(
                                jwtToken.toString(),
                                result.text,
                                tvLatitude,
                                tvLongitude
                            )
                        }

                        if (response.isEmpty()) {
                            runOnUiThread {
                                Toast.makeText(
                                    this@StudentActivity,
                                    "Посещение зачтено",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            val jsonResponse = JSONObject(response)
                            if (jsonResponse.has("message")) {
                                val message = jsonResponse.getString("message")
                                runOnUiThread {
                                    Toast.makeText(
                                        this@StudentActivity,
                                        "Произошла ошибка: $message",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                runOnUiThread {
                                    Toast.makeText(
                                        this@StudentActivity,
                                        "Произошла непредвиденная ошибка",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(
                                this@StudentActivity,
                                "Произошла непредвиденная ошибка",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                runOnUiThread {

                }
            }

            codeScanner.errorCallback = ErrorCallback { error ->
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Camera initialization error: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            scannerView.setOnClickListener {
                codeScanner.startPreview()
            }

            codeScanner.startPreview()
        } else {
            val scannerView: CodeScannerView = findViewById(R.id.scanner_view)
            codeScanner = CodeScanner(this, scannerView)
            codeScanner.camera = CodeScanner.CAMERA_BACK
            codeScanner.formats = CodeScanner.ALL_FORMATS

            codeScanner.autoFocusMode = AutoFocusMode.SAFE
            codeScanner.scanMode = ScanMode.SINGLE
            codeScanner.isAutoFocusEnabled = true
            codeScanner.isFlashEnabled = false

            codeScanner.decodeCallback = DecodeCallback { result ->

                coroutineScope.launch(Dispatchers.IO) {
                    try {
                        val response = withContext(Dispatchers.IO) {
                            patchToken(
                                jwtToken.toString(),
                                result.text,
                                tvLatitude,
                                tvLongitude
                            )
                        }

                        if (response.isEmpty()) {
                            runOnUiThread {
                                Toast.makeText(
                                    this@StudentActivity,
                                    "Посещение зачтено",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            val jsonResponse = JSONObject(response)
                            if (jsonResponse.has("message")) {
                                val message = jsonResponse.getString("message")
                                runOnUiThread {
                                    Toast.makeText(
                                        this@StudentActivity,
                                        "Произошла ошибка: $message",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                runOnUiThread {
                                    Toast.makeText(
                                        this@StudentActivity,
                                        "Произошла непредвиденная ошибка",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(
                                this@StudentActivity,
                                "Произошла непредвиденная ошибка",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                runOnUiThread {

                }
            }

            codeScanner.errorCallback = ErrorCallback { error ->
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Camera initialization error: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            scannerView.setOnClickListener {
                codeScanner.startPreview()
            }

            codeScanner.startPreview()
        }
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
            permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this,
                    permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                permission.ACCESS_FINE_LOCATION,
                permission.ACCESS_COARSE_LOCATION
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

        if (requestCode == 123 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            hasCameraPermission = true
            startScanning()
        }
    }

    private fun openLocationSettings() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()

        if (checkCameraPermission()) {
            if (::codeScanner.isInitialized) {
                codeScanner.startPreview()
            } else {
                startScanning()
            }
        } else {
            requestCameraPermission()
        }

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

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(permission.CAMERA),
            123
        )
    }

    override fun onPause() {
        super.onPause()
        if (::codeScanner.isInitialized) {
            codeScanner.releaseResources()
        }
        stopLocationUpdates()
    }
}
