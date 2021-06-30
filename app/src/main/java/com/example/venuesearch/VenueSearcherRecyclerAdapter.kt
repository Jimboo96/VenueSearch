package com.example.venuesearch

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.venuesearch.data.Venue

class VenueSearcherRecyclerAdapter(private var dataSet: List<Venue>) :
    RecyclerView.Adapter<VenueSearcherRecyclerAdapter.ViewHolder>() {

    private lateinit var context: Context

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.name_text)
        val addressText: TextView = view.findViewById(R.id.address_text)
        val distanceText: TextView = view.findViewById(R.id.distance_text)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        context = viewGroup.context
        val view = LayoutInflater.from(context)
            .inflate(R.layout.venue_searcher_item, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.nameText.text = dataSet[position].name

        var addressTextVisibility = View.GONE
        dataSet[position].address?.let { address ->
            addressTextVisibility = View.VISIBLE
            viewHolder.addressText.text = address
        }
        viewHolder.addressText.visibility = addressTextVisibility

        viewHolder.distanceText.text = context.resources.getString(
            R.string.distance_text,
            dataSet[position].distance.toString()
        )
    }

    override fun getItemCount() = dataSet.size

    fun updateDataSet(newDataSet: List<Venue>) {
        dataSet = newDataSet
        notifyDataSetChanged()
    }
}