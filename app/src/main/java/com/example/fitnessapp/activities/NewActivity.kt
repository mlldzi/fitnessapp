package com.example.fitnessapp.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnessapp.R
import com.example.fitnessapp.adapters.ActivityTypeAdapter
import com.example.fitnessapp.database.AppDatabase
import com.example.fitnessapp.entities.ActivityEntity
import com.example.fitnessapp.entities.ActivityType
import com.example.fitnessapp.helpers.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NewActivity : AppCompatActivity() {

    private lateinit var activityMenu: ConstraintLayout
    private lateinit var activityInfoMenu: LinearLayout
    private lateinit var activityTypeText: TextView
    private lateinit var activityDistanceText: TextView
    private lateinit var activityTimeText: TextView
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private var selectedActivityType: String? = null
    private var distance = 0
    private var seconds = 0
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable
    private lateinit var db: AppDatabase
    private lateinit var sessionManager: SessionManager
    private lateinit var author: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new)

        db = AppDatabase.getDatabase(this)
        sessionManager = SessionManager(this)
        author = sessionManager.getUserLogin() ?: "unknown_user"

        activityMenu = findViewById(R.id.activity_menu)
        activityInfoMenu = findViewById(R.id.activity_info_menu)
        activityTypeText = findViewById(R.id.activity_type)
        activityDistanceText = findViewById(R.id.activity_distance)
        activityTimeText = findViewById(R.id.activity_time)
        startButton = findViewById(R.id.start_button)
        stopButton = findViewById(R.id.stop_button)

        val activityTypes = listOf(
            ActivityType(1, "Велосипед"),
            ActivityType(2, "Бег"),
            ActivityType(3, "Шаг")
        )
        val recyclerView = findViewById<RecyclerView>(R.id.activity_recycler_view)
        recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = ActivityTypeAdapter(this, activityTypes) { activityType ->
            selectedActivityType = activityType.name
            activityTypeText.text = selectedActivityType
        }

        startButton.setOnClickListener {
            if (selectedActivityType != null) {
                activityMenu.visibility = View.GONE
                activityInfoMenu.visibility = View.VISIBLE
                activityDistanceText.text = "0 метров"
                activityTimeText.text = "00:00:00"
                startStopwatch()
            } else {
                Toast.makeText(this, "Выберите тип активности", Toast.LENGTH_SHORT).show()
            }
        }

        stopButton.setOnClickListener {
            stopStopwatch()
        }
    }

    private fun startStopwatch() {
        distance = 0
        seconds = 0
        runnable = object : Runnable {
            override fun run() {
                seconds++
                if (seconds % 2 == 0) {
                    distance += 10
                    activityDistanceText.text = "$distance метров"
                }
                activityTimeText.text = formatTime(seconds)
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable)
    }

    private fun stopStopwatch() {
        handler.removeCallbacks(runnable)
        saveActivityToDatabase()
    }

    private fun saveActivityToDatabase() {
        val activityType = selectedActivityType ?: return
        val currentTime = System.currentTimeMillis()
        val activityEntity = ActivityEntity(
            id = 0,
            type = activityType,
            distanceInMeters = distance,
            timeInSeconds = seconds,
            startTime = currentTime - (seconds * 1000),
            endTime = currentTime,
            date = getCurrentDate(),
            author = author,
            comment = null
        )

        CoroutineScope(Dispatchers.IO).launch {
            db.activityDao().insertActivities(activityEntity)
            withContext(Dispatchers.Main) {
                navigateToEmptystateActivity()
            }
        }
    }

    private fun navigateToEmptystateActivity() {
        val intent = Intent(this, EmptystateActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun formatTime(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }

    private fun getCurrentDate(): String {
        val sdf = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }
}