package com.vaccinate.fragments.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vaccinate.R
import kotlinx.android.synthetic.main.session_card.view.*

class HistoryViewAdapter(private val sessionHistoryList : ArrayList<String>, val context : Context) : RecyclerView.Adapter<HistoryViewAdapter.ViewHolder>() {
    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(session: String) {
            itemView.sessionDetails.text = session
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.session_card, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(sessionHistoryList[position])
    }

    override fun getItemCount(): Int {
        return sessionHistoryList.size
    }
}