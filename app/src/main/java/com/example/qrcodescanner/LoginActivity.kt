package com.example.qrcodescanner

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.qrcodescanner.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity(), View.OnClickListener, View.OnFocusChangeListener,
    View.OnKeyListener {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        binding.loginBtn.setOnClickListener(this)
        binding.signUp.setOnClickListener(this)
        binding.emailEt.onFocusChangeListener = this
        binding.passwordEt.onFocusChangeListener = this
        binding.passwordEt.setOnKeyListener(this)

//        setupObservers()
    }

    private fun setupObservers() {
//        если прогресс бар вообще нужен, он в xml
        TODO("ВИДИМОСТЬ ПРОГРЕСС БАРА")
//        можешь функцию переназвать, я как с гайда брал название, хз что оно значит, может ты знаешь
//        снизу с сервака если есть пользователь, то перекидывает на страницу студента(препода условие надо)
//               если не тут, то в функции submitForm
//            if (user()) {
//              startActivity(Intent(this, StudentActivity::class.java))
//            }
//        мб и тут обсерверс понадобится
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

    private fun validate(): Boolean {
        var isValid = true

        if (!validateEmail()) isValid = false
        if (!validatePassword()) isValid = false

        return isValid
    }

    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                R.id.loginBtn -> {
                    submitForm()
                }

                R.id.signUp -> {
                    startActivity(Intent(this, RegisterActivity::class.java))
                }
            }
        }
    }

    override fun onFocusChange(view: View?, hasFocus: Boolean) {
        if (view != null) {
            when (view.id) {

                R.id.emailEt -> {
                    if (hasFocus) {
                        if (binding.emailTil.isErrorEnabled) {
                            binding.emailTil.isErrorEnabled = false
                        }
                    } else {
                        validateEmail()
                    }
                }

                R.id.passwordEt -> {
                    if (hasFocus) {
                        if (binding.passwordTil.isErrorEnabled) {
                            binding.passwordTil.isErrorEnabled = false
                        }
                    } else {
                        validatePassword()
                    }
                }
            }
        }
    }

    private fun submitForm() {
        if (validate()) {
            TODO("а тут входит")
        }
    }

    override fun onKey(view: View?, keyCode: Int, keyEvent: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_ENTER && keyEvent!!.action == KeyEvent.ACTION_UP) {
            submitForm()
        }

        return false
    }

}