package com.example.fitnessapp.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.fitnessapp.R
import com.example.fitnessapp.entities.UserEntity
import com.example.fitnessapp.database.UserDatabase
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var userDatabase: UserDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration_form)

        userDatabase = UserDatabase.getDatabase(this)

        val loginEditText = findViewById<EditText>(R.id.login_edittext)
        val usernameEditText = findViewById<EditText>(R.id.username_edittext)
        val passwordEditText = findViewById<EditText>(R.id.password_edittext)
        val genderGroup = findViewById<RadioGroup>(R.id.gender_group)

        findViewById<Button>(R.id.register_button).setOnClickListener {
            val login = loginEditText.text.toString()
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            val gender = findViewById<RadioButton>(genderGroup.checkedRadioButtonId).text.toString()

            lifecycleScope.launch {
                val user = UserEntity(
                    login = login,
                    username = username,
                    password = password,
                    gender = gender
                )
                userDatabase.userDao().insert(user)
            }
        }
    }
}