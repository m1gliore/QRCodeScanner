package com.example.qrcodescanner

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.qrcodescanner.databinding.ActivityRegisterLecturerBinding
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.URL

class RegisterLecturerActivity : AppCompatActivity(), View.OnClickListener, View.OnFocusChangeListener,
    View.OnKeyListener, TextWatcher {

    private lateinit var binding: ActivityRegisterLecturerBinding
    private var client: OkHttpClient = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterLecturerBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        binding.fullNameEt.onFocusChangeListener = this
        binding.fullNameEt.addTextChangedListener(this)
        binding.emailEt.onFocusChangeListener = this
        binding.emailEt.addTextChangedListener(this)
        binding.passwordEt.onFocusChangeListener = this
        binding.confirmPasswordEt.onFocusChangeListener = this
        binding.confirmPasswordEt.setOnKeyListener(this)
        binding.confirmPasswordEt.addTextChangedListener(this)
        binding.registerBtn.setOnClickListener {
            onSubmit()
        }
        binding.signIn.setOnClickListener(this)
        binding.signUpStudent.setOnClickListener(this)
    }

    private fun registerUser(username: String, name: String, password: String) {
        val url = URL("https://qr-codes.onrender.com/api/lecturers/signup")
        val mapper = ObjectMapper()
        val jacksonObj = mapper.createObjectNode()
        jacksonObj.put("username", username)
        jacksonObj.put("name", name)
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
                        this@RegisterLecturerActivity,
                        "Ошибка при выполнении запроса",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody: String = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(
                            this@RegisterLecturerActivity,
                            "Регистрация прошла успешно",
                            Toast.LENGTH_SHORT
                        ).show()
                        startActivity(Intent(this@RegisterLecturerActivity, LoginActivity::class.java))
                    }
                } else {
                    val errorMessage = try {
                        val json = JSONObject(responseBody)
                        json.getString("message")
                    } catch (e: JSONException) {
                        "Ошибка сервера"
                    }

                    runOnUiThread {
                        Toast.makeText(this@RegisterLecturerActivity, errorMessage, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        })
    }

    private fun validateField(
        fieldEt: TextInputEditText,
        fieldTil: TextInputLayout,
        errorMessage: String
    ): Boolean {
        val value = fieldEt.text.toString()
        val isValid = value.isNotEmpty()

        if (!isValid) {
            fieldTil.apply {
                isErrorEnabled = true
                error = errorMessage
            }
        } else {
            fieldTil.apply {
                isErrorEnabled = false
                setStartIconDrawable(R.drawable.check_circle_24)
                setStartIconTintList(ColorStateList.valueOf(Color.GREEN))
            }
        }

        return isValid
    }

    private fun validateFullName(): Boolean {
        val errorMessage = "Поле ФИО обязательно"
        return validateField(binding.fullNameEt, binding.fullNameTil, errorMessage)
    }

    private fun validateEmail(): Boolean {
        val errorMessage = "Поле E-mail обязательно"
        return validateField(binding.emailEt, binding.emailTil, errorMessage)
    }

    private fun validatePassword(): Boolean {
        val errorMessage = "Поле пароль обязательно"
        return validateField(binding.passwordEt, binding.passwordTil, errorMessage)
    }

    private fun validateConfirmPassword(): Boolean {
        val errorMessage = "Поле повторите пароль обязательно"
        return validateField(binding.confirmPasswordEt, binding.confirmPasswordTil, errorMessage)
    }

    private fun validatePasswordAndConfirmPassword(): Boolean {
        val errorMessage = "Пароли не совпадают"
        val password = binding.passwordEt.text.toString()
        val confirmPassword = binding.confirmPasswordEt.text.toString()
        val isValid = password == confirmPassword

        if (!isValid) {
            binding.confirmPasswordTil.apply {
                isErrorEnabled = true
                error = errorMessage
            }
        } else {
            binding.confirmPasswordTil.apply {
                isErrorEnabled = false
                setStartIconDrawable(R.drawable.check_circle_24)
                setStartIconTintList(ColorStateList.valueOf(Color.GREEN))
            }
        }

        return isValid
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.registerBtn -> onSubmit()
            R.id.signIn -> startActivity(Intent(this, LoginActivity::class.java))
            R.id.signUpStudent -> startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    override fun onFocusChange(view: View?, hasFocus: Boolean) {
        if (hasFocus) {
            when (view?.id) {
                R.id.fullNameEt -> binding.fullNameTil.isErrorEnabled = false
                R.id.emailEt -> binding.emailTil.isErrorEnabled = false
                R.id.passwordEt -> binding.passwordTil.isErrorEnabled = false
                R.id.confirmPasswordEt -> binding.confirmPasswordTil.isErrorEnabled = false
            }
        } else {
            when (view?.id) {
                R.id.fullNameEt -> validateFullName()
                R.id.emailEt -> validateEmail()
                R.id.passwordEt -> {
                    if (validatePassword() && binding.confirmPasswordEt.text!!.isNotEmpty()) {
                        validateConfirmPassword()
                        validatePasswordAndConfirmPassword()
                    }
                }
                R.id.confirmPasswordEt -> {
                    if (validateConfirmPassword() && validatePassword()) {
                        validatePasswordAndConfirmPassword()
                    }
                }
            }
        }
    }

    override fun onKey(view: View?, keyCode: Int, keyEvent: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_ENTER && keyEvent?.action == KeyEvent.ACTION_UP) {
            onSubmit()
            return true
        }
        return false
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        if (validateFullName()) {
            binding.fullNameTil.apply {
                isErrorEnabled = false
                setStartIconDrawable(R.drawable.check_circle_24)
                setStartIconTintList(ColorStateList.valueOf(Color.GREEN))
            }
        } else {
            binding.fullNameTil.apply {
                setStartIconDrawable(R.drawable.cancel_24)
                setStartIconTintList(ColorStateList.valueOf(Color.RED))
            }
        }

        if (validateEmail()) {
            binding.emailTil.apply {
                isErrorEnabled = false
                setStartIconDrawable(R.drawable.check_circle_24)
                setStartIconTintList(ColorStateList.valueOf(Color.GREEN))
            }
        } else {
            binding.emailTil.apply {
                setStartIconDrawable(R.drawable.cancel_24)
                setStartIconTintList(ColorStateList.valueOf(Color.RED))
            }
        }

        if (validatePassword() && validateConfirmPassword() && validatePasswordAndConfirmPassword()) {
            binding.confirmPasswordTil.apply {
                isErrorEnabled = false
                setStartIconDrawable(R.drawable.check_circle_24)
                setStartIconTintList(ColorStateList.valueOf(Color.GREEN))
            }
        } else {
            binding.confirmPasswordTil.apply {
                setStartIconDrawable(R.drawable.cancel_24)
                setStartIconTintList(ColorStateList.valueOf(Color.RED))
            }
        }
    }

    override fun afterTextChanged(p0: Editable?) {}

    private fun onSubmit() {
        if (validate()) {
            val username = binding.emailEt.text.toString()
            val name = binding.fullNameEt.text.toString()
            val password = binding.passwordEt.text.toString()

            registerUser(username, name, password)
        }
    }

    private fun validate(): Boolean {
        var isValid = true

        if (!validateFullName()) isValid = false
        if (!validateEmail()) isValid = false
        if (!validatePassword()) isValid = false
        if (!validateConfirmPassword()) isValid = false
        if (isValid && !validatePasswordAndConfirmPassword()) isValid = false

        return isValid
    }
}
