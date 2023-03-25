package com.example.qrcodescanner

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.qrcodescanner.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity(), View.OnClickListener, View.OnFocusChangeListener,
    View.OnKeyListener, TextWatcher {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        binding.fullNameEt.onFocusChangeListener = this
        binding.fullNameEt.addTextChangedListener(this)
        binding.emailEt.onFocusChangeListener = this
        binding.emailEt.addTextChangedListener(this)
        binding.passwordEt.onFocusChangeListener = this
        binding.confirmPasswordEt.onFocusChangeListener = this
        binding.confirmPasswordEt.setOnKeyListener(this)
        binding.confirmPasswordEt.addTextChangedListener(this)
        binding.registerBtn.setOnClickListener(this)
        binding.signIn.setOnClickListener(this)

//        setupObservers()
    }

    private fun setupObservers() {
//        если прогресс бар вообще нужен, он в xml
       TODO("ВИДИМОСТЬ ПРОГРЕСС БАРА")
//        можешь функцию переназвать, я как с гайда брал название, хз что оно значит, может ты знаешь
//        снизу с сервака если есть пользователь, то перекидывает на страницу входа
//        если не тут, то в функции onSubmit
//            if (user()) {
//              startActivity(Intent(this, LoginActivity::class.java))
//            }
    }

    private fun validateFullName(shouldUpdateView: Boolean = true): Boolean {
        var errorMessage: String? = null
        val value: String = binding.fullNameEt.text.toString()

        if (value.isEmpty()) {
            errorMessage = "Поле ФИО обязательно"
        }

        if (errorMessage != null && shouldUpdateView) {
            binding.fullNameTil.apply {
                isErrorEnabled = true
                error = errorMessage
            }
        }

        return errorMessage == null
    }

    private fun validateEmail(shouldUpdateView: Boolean = true): Boolean {
        var errorMessage: String? = null
        val value: String = binding.emailEt.text.toString()

        if (value.isEmpty()) {
            errorMessage = "Поле e-mail обязательно"
        } else if (!Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
            errorMessage = "e-mail не подходит требованиям"
        }

        if (errorMessage != null && shouldUpdateView) {
            binding.emailTil.apply {
                isErrorEnabled = true
                error = errorMessage
            }
        }

        return errorMessage == null
    }

    private fun validatePassword(shouldUpdateView: Boolean = true): Boolean {
        var errorMessage: String? = null
        val value: String = binding.passwordEt.text.toString()

        if (value.isEmpty()) {
            errorMessage = "Поле пароль обязательно"
        } else if (value.length < 6) {
            errorMessage = "Пароль должен иметь 6 или больше символов"
        }

        if (errorMessage != null && shouldUpdateView) {
            binding.passwordTil.apply {
                isErrorEnabled = true
                error = errorMessage
            }
        }

        return errorMessage == null
    }

    private fun validateConfirmPassword(shouldUpdateView: Boolean = true): Boolean {
        var errorMessage: String? = null
        val value: String = binding.confirmPasswordEt.text.toString()

        if (value.isEmpty()) {
            errorMessage = "Поле повторите пароль обязательно"
        } else if (value.length < 6) {
            errorMessage = "Пароль должен иметь 6 или больше символов"
        }

        if (errorMessage != null && shouldUpdateView) {
            binding.confirmPasswordTil.apply {
                isErrorEnabled = true
                error = errorMessage
            }
        }

        return errorMessage == null
    }

    private fun validatePasswordAndConfirmPassword(shouldUpdateView: Boolean = true): Boolean {
        var errorMessage: String? = null
        val password: String = binding.passwordEt.text.toString()
        val confirmPassword: String = binding.confirmPasswordEt.text.toString()

        if (password != confirmPassword) {
            errorMessage = "Пароли не совпадают"
        }

        if (errorMessage != null && shouldUpdateView) {
            binding.confirmPasswordTil.apply {
                isErrorEnabled = true
                error = errorMessage
            }
        }

        return errorMessage == null
    }

    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                R.id.loginBtn -> {
                    onSubmit()
                }

                R.id.signIn -> {
                    startActivity(Intent(this, LoginActivity::class.java))
                }
            }
        }
    }

    override fun onFocusChange(view: View?, hasFocus: Boolean) {
        if (view != null) {
            when (view.id) {
                //тут мб с сервака валидация какая
                R.id.fullNameEt -> {
                    if (hasFocus) {
                        if (binding.fullNameTil.isErrorEnabled) {
                            binding.fullNameTil.isErrorEnabled = false
                        }
                    } else {
                        if (validateFullName()) {
                            binding.fullNameTil.apply {
                                setStartIconDrawable(R.drawable.check_circle_24)
                                setStartIconTintList(ColorStateList.valueOf(Color.GREEN))
                            }
                        }
                    }
                }

                R.id.emailEt -> {
                    if (hasFocus) {
                        if (binding.emailTil.isErrorEnabled) {
                            binding.emailTil.isErrorEnabled = false
                        }
                    } else {
                        if (validateEmail()) {
                            binding.emailTil.apply {
                                setStartIconDrawable(R.drawable.check_circle_24)
                                setStartIconTintList(ColorStateList.valueOf(Color.GREEN))
                            }
                        }
                    }
                }

                R.id.passwordEt -> {
                    if (hasFocus) {
                        if (binding.passwordTil.isErrorEnabled) {
                            binding.passwordTil.isErrorEnabled = false
                        }
                    } else {
                        if (validatePassword() && binding.confirmPasswordEt.text!!.isNotEmpty() &&
                            validateConfirmPassword() && validatePasswordAndConfirmPassword()
                        ) {
                            if (binding.confirmPasswordTil.isErrorEnabled) {
                                binding.confirmPasswordTil.isErrorEnabled = false
                            }
                        }
                    }
                }

                R.id.confirmPasswordEt -> {
                    if (hasFocus) {
                        if (binding.confirmPasswordTil.isErrorEnabled) {
                            binding.confirmPasswordTil.isErrorEnabled = false
                        }
                    } else {
                        if (validateConfirmPassword() && validatePassword() && validatePasswordAndConfirmPassword()) {
                            if (binding.passwordTil.isErrorEnabled) {
                                binding.passwordTil.isErrorEnabled = false
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onKey(view: View?, keyCode: Int, keyEvent: KeyEvent?): Boolean {
        if (KeyEvent.KEYCODE_ENTER == keyCode && keyEvent!!.action == KeyEvent.ACTION_UP) {
            onSubmit()
        }
        return false
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        if (validateFullName(shouldUpdateView = false)) {
            binding.fullNameTil.apply {
                if (isErrorEnabled) isErrorEnabled = false
                setStartIconDrawable(R.drawable.check_circle_24)
                setStartIconTintList(ColorStateList.valueOf(Color.GREEN))
            }
        } else {
            if (binding.fullNameTil.startIconDrawable != null) {
                binding.fullNameTil.apply {
                    setStartIconDrawable(R.drawable.cancel_24)
                    setStartIconTintList(ColorStateList.valueOf(Color.RED))
                }
            }
        }

        if (validateEmail(shouldUpdateView = false)) {
            binding.emailTil.apply {
                if (isErrorEnabled) isErrorEnabled = false
                setStartIconDrawable(R.drawable.check_circle_24)
                setStartIconTintList(ColorStateList.valueOf(Color.GREEN))
            }
        } else {
            if (binding.emailTil.startIconDrawable != null) {
                binding.emailTil.apply {
                    setStartIconDrawable(R.drawable.cancel_24)
                    setStartIconTintList(ColorStateList.valueOf(Color.RED))
                }
            }
        }

        if (validatePassword(shouldUpdateView = false) && validateConfirmPassword(shouldUpdateView = false) &&
            validatePasswordAndConfirmPassword(shouldUpdateView = false)
        ) {
            binding.confirmPasswordTil.apply {
                if (isErrorEnabled) isErrorEnabled = false
                setStartIconDrawable(R.drawable.check_circle_24)
                setStartIconTintList(ColorStateList.valueOf(Color.GREEN))
            }
        } else {
            if (binding.confirmPasswordTil.startIconDrawable != null) {
                binding.confirmPasswordTil.apply {
                    setStartIconDrawable(R.drawable.cancel_24)
                    setStartIconTintList(ColorStateList.valueOf(Color.RED))
                }
            }
        }
    }

    override fun afterTextChanged(p0: Editable?) {}

    override fun onStart() {
        super.onStart()

//        если человек зареган, то при старте его не будет кидать на регистрацию
//        if (user()) {
//            startActivity(Intent(this, StudentActivity::class.java))
//        }
    }

    private fun onSubmit() {
        if (validate()) {
            TODO("сервак")
            //тут регает пользователя
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