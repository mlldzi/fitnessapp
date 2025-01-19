package com.example.fitnessapp.activities

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.fitnessapp.R
import com.example.fitnessapp.entities.UserEntity
import com.example.fitnessapp.helpers.SessionManager
import com.example.fitnessapp.database.UserDatabase
import kotlinx.coroutines.launch


class RegistrationActivity : AppCompatActivity() {
    private lateinit var userDatabase: UserDatabase
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration_form)

        val loginField = findViewById<EditText>(R.id.login_edittext)
        val usernameField = findViewById<EditText>(R.id.username_edittext)
        val passwordField = findViewById<EditText>(R.id.password_edittext)
        val repeatPasswordField = findViewById<EditText>(R.id.repeat_password_edittext)
        val genderGroup = findViewById<RadioGroup>(R.id.gender_group)
        val registerButton = findViewById<Button>(R.id.register_button)
        val backArrow = findViewById<ImageView>(R.id.back_arrow)

        userDatabase = UserDatabase.getDatabase(this)
        sessionManager = SessionManager(this)

        backArrow.setOnClickListener {
            navigateToMainActivity()
        }

        registerButton.setOnClickListener {
            val login = loginField.text.toString().trim()
            val username = usernameField.text.toString().trim()
            val password = passwordField.text.toString().trim()
            val repeatPassword = repeatPasswordField.text.toString().trim()
            val genderId = genderGroup.checkedRadioButtonId

            if (login.isEmpty() || username.isEmpty() || password.isEmpty() || repeatPassword.isEmpty() || genderId == -1) {
                Toast.makeText(this, "Все поля должны быть заполнены", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val gender = findViewById<RadioButton>(genderId)?.text.toString()

            if (password == repeatPassword) {
                lifecycleScope.launch {
                    val existingUser = userDatabase.userDao().findUserByLogin(login)
                    if (existingUser != null) {
                        Toast.makeText(
                            this@RegistrationActivity,
                            "Логин уже существует",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val user = UserEntity(
                            login = login,
                            username = username,
                            password = password,
                            gender = gender
                        )
                        userDatabase.userDao().insert(user)
                        sessionManager.createLoginSession(login)
//                        logAllUsers()
                        navigateToEmptystateActivity()
                    }
                }
            } else {
                Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
            }
        }

        setupAgreementText()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToEmptystateActivity() {
        val intent = Intent(this, EmptystateActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setupAgreementText() {
        val agreementTextView = findViewById<TextView>(R.id.agreement_text)
        val text = getString(R.string.agree_text)
        val spannableString = SpannableString(text)

        val purpleColor = ContextCompat.getColor(this, R.color.purple)
        val policyText = "политикой конфиденциальности"
        val agreementText = "пользовательское соглашение"

        val policyStart = text.indexOf(policyText)
        val policyEnd = policyStart + policyText.length
        val agreementStart = text.indexOf(agreementText)
        val agreementEnd = agreementStart + agreementText.length

        if (policyStart != -1 && agreementStart != -1) {
            spannableString.setSpan(
                ForegroundColorSpan(purpleColor),
                policyStart,
                policyEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableString.setSpan(
                ForegroundColorSpan(purpleColor),
                agreementStart,
                agreementEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        agreementTextView.text = spannableString
    }

//    private suspend fun logAllUsers() {
//        val users = userDatabase.userDao().getAllUsers()
//        for (user in users) {
//            Log.d(
//                "RegistrationActivity",
//                "User: ${user.login}, Username: ${user.username}, Gender: ${user.gender}"
//            )
//        }
//    }
}