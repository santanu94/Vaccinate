package com.vaccinate

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.card.view.*

class CardViewAdapter(val vaccinationCenterCardList : MutableList<VaccineCenterCard>, val context : Context) : RecyclerView.Adapter<CardViewAdapter.ViewHolder>() {
    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(vaccineCenterCard: VaccineCenterCard) {
            itemView.center.text = vaccineCenterCard.center
            itemView.address.text = vaccineCenterCard.address
            itemView.districtName.text = vaccineCenterCard.districtName
            itemView.pincode.text = vaccineCenterCard.pincode
            itemView.date.text = vaccineCenterCard.date
            itemView.availableCapacity.text = vaccineCenterCard.availableCapacity
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.card, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(vaccinationCenterCardList[position])
    }

    override fun getItemCount(): Int {
        return vaccinationCenterCardList.size
    }
}