package com.vaccinate

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.vaccinate.util.FileReadWrite
import com.vaccinate.util.VaccineCenterCard
import kotlinx.android.synthetic.main.activity_available_slots.*
import org.json.JSONArray

class AvailableSlotsActivity : AppCompatActivity() {
    private lateinit var mediaPlayer: MediaPlayer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_available_slots)
        turnScreenOnAndKeyguardOff()

        val centersString = if (intent.getStringExtra("centers") != null) {
            intent.getStringExtra("centers")!!
        } else {
            FileReadWrite("${dataDir}/centers.json").read()
        }
        val vaccinationCenterCardList: MutableList<VaccineCenterCard> = mutableListOf()
        val jsonArrayCenter = JSONArray(centersString)
        for (i in 0 until jsonArrayCenter.length()) {
            val card = VaccineCenterCard(
                jsonArrayCenter.getJSONObject(i).getString("center"),
                jsonArrayCenter.getJSONObject(i).getString("address"),
                jsonArrayCenter.getJSONObject(i).getString("districtName"),
                jsonArrayCenter.getJSONObject(i).getString("pincode"),
                jsonArrayCenter.getJSONObject(i).getString("date"),
                jsonArrayCenter.getJSONObject(i).getString("availableCapacity")
            )
            vaccinationCenterCardList.add(card)
        }

        // Stopping polling service
        val serviceIntent = Intent(this, PollService::class.java)
        stopService(serviceIntent)

        val cardViewAdapter = CardViewAdapter(vaccinationCenterCardList, this)
        centerRecyclerView.layoutManager = LinearLayoutManager(this)
        centerRecyclerView.adapter = cardViewAdapter

        playAudio()
        vibratePhone()

        muteBtn.setOnClickListener {
            mediaPlayer.stop()
        }

    }

    private fun playAudio() {
        val alarm: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        mediaPlayer = MediaPlayer.create(this, alarm)
        mediaPlayer.setVolume(1.0F, 1.0F)
        mediaPlayer.start()
    }

    private fun vibratePhone() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(200)
        }
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer.stop()
        turnScreenOffAndKeyguardOn()
    }
}