package com.foryoupage.credo
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ResolveInfo
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.widget.EditText
//import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.widget.addTextChangedListener
import com.foryoupage.credo.ui.theme.CredoTheme
import android.util.Log
import android.widget.TextView
import java.util.*
import com.google.firebase.analytics.FirebaseAnalytics

class MainActivity : ComponentActivity() {
    private lateinit var searchEditText: EditText
    private lateinit var appsListRecyclerView: RecyclerView
    private lateinit var appsAdapter: AppsAdapter
    private var allApps: List<ResolveInfo> = listOf()
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()

        setContentView(R.layout.activity_main)

        searchEditText = findViewById(R.id.search_bar)
        appsListRecyclerView = findViewById(R.id.apps_list)
        appsListRecyclerView.layoutManager = LinearLayoutManager(this)

        allApps = getAllInstalledApps()
        appsAdapter = AppsAdapter(this, allApps)
        appsListRecyclerView.adapter = appsAdapter

//        val tvTime = findViewById<TextView>(R.id.tvTime)
//        val tvDate = findViewById<TextView>(R.id.tvDate)
//        updateTimeAndDate(tvTime, tvDate)

        searchEditText.addTextChangedListener { text ->

            fun afterTextChanged(s: Editable) {
                Log.d("AppSearch", "User searched for: ${s.toString()}")
            }

            // Filter your list based on the search query
            val filteredApps = if (text.isNullOrEmpty() || text.length < 3) {
                appsListRecyclerView.visibility = View.GONE // Hide the list when there's no search query
                listOf()
            } else {
                appsListRecyclerView.visibility = View.VISIBLE // Show the list when there's a search query
                allApps.filter {
                    it.loadLabel(packageManager).toString().contains(text.toString(), ignoreCase = true)
                    // it.loadLabel(packageManager).toString().equals(text.toString(), ignoreCase = true)
                }
            }
            appsAdapter.updateList(filteredApps)
        }

        searchEditText.setOnTouchListener { v, event ->
            val drawableEnd = 2
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (searchEditText.right - searchEditText.compoundDrawables[drawableEnd].bounds.width())) {
                    searchEditText.text.clear()
                    return@setOnTouchListener true
                }
            }
            return@setOnTouchListener false
        }

        val animation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        appsListRecyclerView.startAnimation(animation)
    }

    private fun getAllInstalledApps(): List<ResolveInfo> {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        return packageManager.queryIntentActivities(intent, 0)
    }

    private fun updateTimeAndDate(tvTime: TextView, tvDate: TextView) {
        val currentTime = Calendar.getInstance().time
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())

        tvTime.text = timeFormat.format(currentTime)
        tvDate.text = dateFormat.format(currentTime)
    }

    private val handler = Handler(Looper.getMainLooper())
//    private val timeUpdater = object : Runnable {
//        override fun run() {
//            val currentTime = Calendar.getInstance().time
//            findViewById<TextView>(R.id.tvTime).text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(currentTime)
//            handler.postDelayed(this, 60000) // Schedule the next update in 60 seconds
//        }
//    }

    override fun onResume() {
        super.onResume()
//        timeUpdater.run() // Start updates when the activity is in the foreground
    }

    override fun onPause() {
        super.onPause()
//        handler.removeCallbacks(timeUpdater) // Stop updates when the activity is not visible
    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CredoTheme {
        Greeting("Android")
    }
}