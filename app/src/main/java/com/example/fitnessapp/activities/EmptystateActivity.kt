package com.example.fitnessapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnessapp.R
import com.example.fitnessapp.adapters.ActivityAdapter
import com.example.fitnessapp.database.AppDatabase
import com.example.fitnessapp.entities.ActivityEntity
import com.example.fitnessapp.fragments.ActivityFragment
import com.example.fitnessapp.fragments.ProfileFragment
import com.example.fitnessapp.helpers.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EmptystateActivity : AppCompatActivity() {

    private lateinit var topTabSwitcher: View
    private lateinit var addActivityButton: View
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateMessage: View
    private lateinit var adapter: ActivityAdapter
    private lateinit var db: AppDatabase
    private var allActivities: List<ActivityEntity> = listOf()
    private lateinit var sessionManager: SessionManager
    private lateinit var loggedInUsername: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emptystate)

        db = AppDatabase.getDatabase(this)
        sessionManager = SessionManager(this)
        loggedInUsername = sessionManager.getUserLogin() ?: "unknown_user"

        topTabSwitcher = findViewById(R.id.top_tab_indicator)
        addActivityButton = findViewById(R.id.add_activity_button)
        recyclerView = findViewById(R.id.recycler_view)
        emptyStateMessage = findViewById(R.id.empty_state_message)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ActivityAdapter(this, listOf())
        recyclerView.adapter = adapter

        lifecycleScope.launch {
            allActivities = withContext(Dispatchers.IO) {
                db.activityDao().getAllActivities()
            }
            updateRecyclerView(loggedInUsername)
        }

        val topNavigationView = findViewById<BottomNavigationView>(R.id.top_navigation)
        topNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_my_activity -> {
                    updateRecyclerView(loggedInUsername)
                    true
                }

                R.id.navigation_users_activity -> {
                    updateRecyclerView(null)
                    true
                }

                else -> false
            }
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, ActivityFragment(), "ActivityFragment")
                .commit()
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_activity -> {
                    switchFragment(ActivityFragment(), "ActivityFragment")
                    true
                }

                R.id.navigation_profile -> {
                    switchFragment(ProfileFragment(), "ProfileFragment")
                    true
                }

                else -> false
            }
        }

        addActivityButton.setOnClickListener {
            val intent = Intent(this, NewActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_DETAIL && resultCode == RESULT_OK) {
            lifecycleScope.launch {
                allActivities = withContext(Dispatchers.IO) {
                    db.activityDao().getAllActivities()
                }
                updateRecyclerView(loggedInUsername)
            }
        }
    }

    private fun showEmptyState(show: Boolean) {
        if (show) {
            recyclerView.visibility = View.GONE
            emptyStateMessage.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyStateMessage.visibility = View.GONE
        }
    }

    private fun updateRecyclerView(username: String?) {
        val filteredActivities = if (username != null) {
            allActivities.filter { it.author == username }
        } else {
            allActivities.filter { it.author != loggedInUsername }
        }
        runOnUiThread {
            if (filteredActivities.isEmpty()) {
                showEmptyState(true)
            } else {
                showEmptyState(false)
                adapter = ActivityAdapter(this, filteredActivities)
                recyclerView.adapter = adapter
            }
        }
    }

    private fun switchFragment(fragment: Fragment, tag: String) {
        val fragmentManager = supportFragmentManager
        val existingFragment = fragmentManager.findFragmentByTag(tag)

        fragmentManager.beginTransaction().apply {
            fragmentManager.fragments.forEach { hide(it) }
            if (existingFragment != null) {
                show(existingFragment)
            } else {
                add(R.id.fragment_container, fragment, tag)
            }
        }.commit()

        val topNavigationView = findViewById<BottomNavigationView>(R.id.top_navigation)
        val addActivityButton = findViewById<View>(R.id.add_activity_button)
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        val emptyStateMessage = findViewById<View>(R.id.empty_state_message)

        if (tag == "ProfileFragment") {
            topNavigationView.alpha = 0f
            addActivityButton.visibility = View.GONE
            recyclerView.visibility = View.GONE
            emptyStateMessage.visibility = View.GONE
        } else {
            topNavigationView.alpha = 1f
            addActivityButton.visibility = View.VISIBLE
            recyclerView.visibility = View.VISIBLE
            emptyStateMessage.visibility = View.GONE
        }
    }

    companion object {
        const val REQUEST_CODE_DETAIL = 1
    }
}