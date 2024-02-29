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
import android.app.AppOpsManager
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
import android.provider.Settings
import android.app.usage.UsageStatsManager
import android.content.pm.PackageManager
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.view.WindowInsetsController
import androidx.core.view.WindowCompat
//import android.content.Intent

class MainActivity : ComponentActivity() {
    private lateinit var searchEditText: EditText
    private lateinit var appsListRecyclerView: RecyclerView
    private lateinit var appsAdapter: AppsAdapter
    private var allApps: List<ResolveInfo> = listOf()
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var notificationManager: NotificationManager
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()

        setContentView(R.layout.activity_main)

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        searchEditText = findViewById(R.id.search_bar)
        appsListRecyclerView = findViewById(R.id.apps_list)
        appsListRecyclerView.layoutManager = LinearLayoutManager(this)

        allApps = getAllInstalledApps()
        appsAdapter = AppsAdapter(this, allApps, firebaseAnalytics)
        appsListRecyclerView.adapter = appsAdapter

//        val tvTime = findViewById<TextView>(R.id.tvTime)
//        val tvDate = findViewById<TextView>(R.id.tvDate)
//        updateTimeAndDate(tvTime, tvDate)

//        Permission Check---------------------
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        checkDndPermission()

        if (!hasUsageStatsPermission()) {
            // Guide the user to enable the permission
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
        promptSetDefaultLauncher()


        searchEditText.addTextChangedListener { text ->

            fun afterTextChanged(s: Editable) {
                Log.d("AppSearch", "User searched for: ${s.toString()}")
            }

            // Filter your list based on the search query
            val filteredApps = if (text.isNullOrEmpty() || text.length < 3) {
                appsListRecyclerView.visibility = View.GONE // Hide the list when there's no search query
                listOf()
            } else {

                val bundle = Bundle()
                bundle.putString(FirebaseAnalytics.Param.SEARCH_TERM, text.toString())
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SEARCH, bundle)

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

    private fun logAppLaunch(appPackageName: String) {
        val launchBundle = Bundle().apply {
            putString("app_package_name", appPackageName)
            putLong("launch_time", System.currentTimeMillis())
        }
        firebaseAnalytics.logEvent("app_launch", launchBundle)
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
    private var startTime = 0L
    override fun onResume() {
        super.onResume()
        startTime = System.currentTimeMillis()
        setDndMode(true)
//        timeUpdater.run() // Start updates when the activity is in the foreground
    }

    override fun onPause() {
        super.onPause()
        val timeSpent = System.currentTimeMillis() - startTime
        val timeBundle = Bundle()
        timeBundle.putLong("time_spent_on_launcher", timeSpent)
        firebaseAnalytics.logEvent("launcher_time_spent", timeBundle)
        setDndMode(false)
//        handler.removeCallbacks(timeUpdater) // Stop updates when the activity is not visible
    }

    private fun isDefaultLauncher(): Boolean {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolveInfo?.activityInfo?.packageName == packageName
    }

    private fun promptSetDefaultLauncher() {
        if (!isDefaultLauncher()) {
            // Your app is not the default launcher
            // Guide the user to the settings page to set it as default
            val intent = Intent(Settings.ACTION_HOME_SETTINGS)
            startActivity(intent)
        }
    }
    private fun checkDndPermission() {
        if (!notificationManager.isNotificationPolicyAccessGranted) {
            val intent = Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            startActivity(intent)
        }
    }
    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), packageName)
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun scheduleAppUsageLogging() {
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                logAppUsageStats()
                handler.postDelayed(this, 600000) // Schedule the next run in 10 minutes
            }
        }
        handler.post(runnable)
    }

    private fun logAppUsageStats() {
        val usageStatsManager = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 600000 // Last 10 minutes

        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)

        stats.forEach { stat ->
            // Log each app's package name and total foreground usage time in milliseconds
            val bundle = Bundle().apply {
                putString("app_package_name", stat.packageName)
                putLong("usage_time_ms", stat.totalTimeInForeground)
            }
            firebaseAnalytics.logEvent("app_usage", bundle)
        }
    }

    private fun setDndMode(enabled: Boolean) {
        if (notificationManager.isNotificationPolicyAccessGranted) {
            val interruptionFilter = if (enabled) NotificationManager.INTERRUPTION_FILTER_NONE else NotificationManager.INTERRUPTION_FILTER_ALL
            notificationManager.setInterruptionFilter(interruptionFilter)
        }
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