package com.example.fitnessapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.fitnessapp.R
import com.example.fitnessapp.helpers.SessionManager
import com.example.fitnessapp.database.UserDatabase
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var userDatabase: UserDatabase
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val loginField = findViewById<EditText>(R.id.login_edittext)
        val passwordField = findViewById<EditText>(R.id.password_edittext)
        val loginButton = findViewById<Button>(R.id.login_button)
        val backArrow = findViewById<ImageView>(R.id.back_arrow)

        userDatabase = UserDatabase.getDatabase(this)
        sessionManager = SessionManager(this)

        backArrow.setOnClickListener {
            navigateToMainActivity()
        }

        loginButton.setOnClickListener {
            val login = loginField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (login.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Все поля должны быть заполнены", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val user = userDatabase.userDao().findUserByLoginAndPassword(login, password)
                if (user != null) {
                    sessionManager.createLoginSession(login)
                    navigateToEmptystateActivity()
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "Неверный логин или пароль",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun navigateToEmptystateActivity() {
        val intent = Intent(this, EmptystateActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}