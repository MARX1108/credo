package com.foryoupage.credo
import android.content.Intent
import android.content.pm.ResolveInfo
import android.widget.EditText
//import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import android.os.Bundle
import android.view.View
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

class MainActivity : ComponentActivity() {
    private lateinit var searchEditText: EditText
    private lateinit var appsListRecyclerView: RecyclerView
    private lateinit var appsAdapter: AppsAdapter
    private var allApps: List<ResolveInfo> = listOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        searchEditText = findViewById(R.id.search_bar)
        appsListRecyclerView = findViewById(R.id.apps_list)
        appsListRecyclerView.layoutManager = LinearLayoutManager(this)

        allApps = getAllInstalledApps()
        appsAdapter = AppsAdapter(this, allApps)
        appsListRecyclerView.adapter = appsAdapter

//        searchEditText.addTextChangedListener { text ->
//            val filteredApps = allApps.filter {
//                it.loadLabel(packageManager).toString().contains(text.toString(), ignoreCase = true)
//            }
//            appsAdapter.updateList(filteredApps)
//        }

        searchEditText.addTextChangedListener { text ->
            // Filter your list based on the search query
            val filteredApps = if (text.isNullOrEmpty()) {
                appsListRecyclerView.visibility = View.GONE // Hide the list when there's no search query
                listOf()
            } else {
                appsListRecyclerView.visibility = View.VISIBLE // Show the list when there's a search query
                allApps.filter {
                    it.loadLabel(packageManager).toString().contains(text.toString(), ignoreCase = true)
                }
            }
            appsAdapter.updateList(filteredApps)
        }
//        appsListRecyclerView.itemAnimator = DefaultItemAnimator()
    }

    private fun getAllInstalledApps(): List<ResolveInfo> {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        return packageManager.queryIntentActivities(intent, 0)
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