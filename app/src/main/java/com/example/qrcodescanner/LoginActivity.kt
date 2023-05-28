package com.example.qrcodescanner

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.qrcodescanner.databinding.ActivityLoginBinding
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.URL

class LoginActivity : AppCompatActivity(), View.OnClickListener, View.OnFocusChangeListener,
    View.OnKeyListener {

    private lateinit var binding: ActivityLoginBinding
    private var client: OkHttpClient = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginBtn.setOnClickListener {
            submitForm()
        }
        binding.signUp.setOnClickListener(this)
        binding.emailEt.onFocusChangeListener = this
        binding.passwordEt.onFocusChangeListener = this
        binding.passwordEt.setOnKeyListener(this)
    }

    fun saveCredentials(context: Context, jwtToken: String, name: String, role: String) {
        val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("jwtToken", jwtToken)
        editor.putString("name", name)
        editor.putString("role", role)
        editor.apply()
    }

    private fun loginUser(username: String, password: String) {
        val url = URL("https://qr-codes.onrender.com/api/auth/authenticate")
        val mapper = ObjectMapper()
        val jacksonObj = mapper.createObjectNode()
        jacksonObj.put("username", username)
        jacksonObj.put("password", password)
        val jacksonString = jacksonObj.toString()

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jacksonString.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        this@LoginActivity,
                        "Ошибка при выполнении запроса",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody: String = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    try {
                        val jsonResponse = JSONObject(responseBody)
                        val jwtToken = jsonResponse.getString("token")
                        val decodedToken = decodeJwtToken(jwtToken)
                        val name = decodedToken.getString("name")
                        val roles = decodedToken.getJSONArray("roles")

                        if (roles.length() > 0) {
                            val role = roles.getJSONObject(0).getString("name")

                            runOnUiThread {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Авторизация прошла успешно",
                                    Toast.LENGTH_SHORT
                                ).show()

                                val intent = when (role) {
                                    "ROLE_STUDENT" -> {
                                        saveCredentials(this@LoginActivity, jwtToken, name, role)
                                        Intent(this@LoginActivity, StudentActivity::class.java)
                                    }
                                    "ROLE_LECTURER" -> {
                                        saveCredentials(this@LoginActivity, jwtToken, name, role)
                                        Intent(this@LoginActivity, LecturerActivity::class.java)
                                    }
                                    else -> null
                                }

                                if (intent != null) {
                                    startActivity(intent)
                                }
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Ошибка авторизации: роль не определена",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } catch (e: JSONException) {
                        runOnUiThread {
                            Toast.makeText(
                                this@LoginActivity,
                                "Ошибка при парсинге ответа сервера",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    val errorMessage = try {
                        val json = JSONObject(responseBody)
                        json.getString("message")
                    } catch (e: JSONException) {
                        "Ошибка сервера"
                    }

                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        })
    }

    private fun decodeJwtToken(token: String): JSONObject {
        val parts = token.split(".")
        val base64Body = parts[1]
        val body = String(Base64.decode(base64Body, Base64.URL_SAFE))
        return JSONObject(body)
    }


    private fun validateField(value: String, errorEmpty: String): Boolean {
        if (value.isEmpty()) {
            showError(errorEmpty)
            return false
        }
        return true
    }

    private fun validateEmail(): Boolean {
        val email = binding.emailEt.text.toString()
        return validateField(email, "Поле e-mail обязательно")
    }

    private fun validatePassword(): Boolean {
        val password = binding.passwordEt.text.toString()
        return validateField(password, "Поле пароль обязательно")
    }

    private fun validate(): Boolean {
        return validateEmail() && validatePassword()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.loginBtn -> submitForm()
            R.id.signUp -> startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    override fun onFocusChange(view: View?, hasFocus: Boolean) {
        if (!hasFocus) {
            when (view?.id) {
                R.id.emailEt -> validateEmail()
                R.id.passwordEt -> validatePassword()
            }
        }
    }

    private fun showError(errorMessage: String) {
        binding.emailTil.apply {
            isErrorEnabled = true
            error = errorMessage
        }
    }

    private fun submitForm() {
        if (validate()) {
            val username = binding.emailEt.text.toString()
            val password = binding.passwordEt.text.toString()

            loginUser(username, password)
        }
    }

    override fun onKey(view: View?, keyCode: Int, keyEvent: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_ENTER && keyEvent?.action == KeyEvent.ACTION_UP) {
            submitForm()
        }
        return false
    }

}
