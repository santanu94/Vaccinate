package com.vaccinate.util

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

data class VaccineCenterCard(
    val center: String,
    val address: String,
    val districtName: String,
    val pincode: String,
    val date: String,
    val availableCapacity: String) {

    override fun toString(): String {
        return """{"center": "${this.center}", "address": "${this.address}", "districtName": "${this.districtName}", "pincode": "${this.pincode}", "date": "${this.date}", "availableCapacity": "${this.availableCapacity}"}"""
    }
}