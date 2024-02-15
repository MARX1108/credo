package com.foryoupage.credo
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
//import com.yourpackage.name.R // Make sure to replace with your actual package name

class AppsAdapter(private val context: Context, private var appsList: List<ResolveInfo>) :
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
        val app = appsList[position]
        val pm = context.packageManager
        holder.appName.text = app.loadLabel(pm)
        holder.appIcon.setImageDrawable(app.loadIcon(pm))

        holder.itemView.setOnClickListener {
            // Launch the app
            val launchIntent = pm.getLaunchIntentForPackage(app.activityInfo.packageName)
            context.startActivity(launchIntent)
        }
    }

    override fun getItemCount() = appsList.size

    fun updateList(newList: List<ResolveInfo>) {
        appsList = newList
        notifyDataSetChanged()
    }
}
