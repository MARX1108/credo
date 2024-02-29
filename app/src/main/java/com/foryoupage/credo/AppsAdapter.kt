package com.foryoupage.credo
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.analytics.FirebaseAnalytics
import android.os.Bundle
class AppsAdapter(private val context: Context, private var appsList: List<ResolveInfo>,private val firebaseAnalytics: FirebaseAnalytics ) :
    RecyclerView.Adapter<AppsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appName: TextView = view.findViewById(R.id.app_name)
        val appIcon: ImageView = view.findViewById(R.id.app_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_app, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appInfo = appsList[position]
        val app = appsList[position]
        val pm = context.packageManager
        holder.appName.text = app.loadLabel(pm)
        holder.appIcon.setImageDrawable(app.loadIcon(pm))

        holder.itemView.setOnClickListener {
            // Launch the app
            val packageName = appInfo.activityInfo.packageName
            val launchIntent = pm.getLaunchIntentForPackage(app.activityInfo.packageName)
            context.startActivity(launchIntent)

            val bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, packageName)
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "app_launch")
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)

            if (launchIntent != null) {
                context.startActivity(launchIntent)
                // Log the app launch
                Log.d("AppLaunch", "Launched app package: $packageName, Timestamp: ${System.currentTimeMillis()}")
            } else {
                // Handle the case where there is no launch intent available
                Log.d("AppLaunch", "Unable to find launch intent for package: $packageName")
            }
        }
    }

    override fun getItemCount() = appsList.size

    fun updateList(newList: List<ResolveInfo>) {
        appsList = newList
        notifyDataSetChanged()
    }
}
