 package com.vaccinate

import android.app.*
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.android.volley.Request
import com.android.volley.toolbox.RequestFuture
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.vaccinate.Constants.ALERT_CHANNEL_ID
import com.vaccinate.Constants.ALERT_NOTIFICATION_ID
import com.vaccinate.Constants.SERVICE_CHANNEL_ID
import com.vaccinate.Constants.SERVICE_NOTIFICATION_ID
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.random.Random
import java.io.File

 class PollVac : Service() {
    private lateinit var ageGroup : String
    private lateinit var districtList : ArrayList<String>
    private var stopThread = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ageGroup = intent?.getStringExtra("AgeGroup").toString()
        districtList = intent?.getStringArrayListExtra("DistrictList") as ArrayList<String>

        showNotification()

        val runnable = Runnable {
            while(true)
            {
                if (stopThread) {
                    break
                }
                val vaccinationCenterCardList: MutableList<VaccineCenterCard> = mutableListOf()

                for (i in 0 until districtList.size) {
                    val districtId = districtList[i]
                    val date = getDate()
                    val url =
                        "https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/calendarByDistrict?district_id=$districtId&date=$date"

                    val future : RequestFuture<String> = RequestFuture.newFuture()
                    val stringRequest = StringRequest(Request.Method.GET, url, future, future)
                    Volley.newRequestQueue(this).add(stringRequest)

                    try {
                        val response : String = future.get(5, TimeUnit.SECONDS)
                        vaccinationCenterCardList += getVaccinationCenterList(response)
                    } catch ( e: Exception) {
                        Log.d("ccss", e.toString())
                    }
                }
                if (vaccinationCenterCardList.size > 0) {
                    File("${dataDir}/centers.json").writeText(JSONArray(vaccinationCenterCardList.toString()).toString())
                    runAlertNotification()
                    Log.d("ccss", "done")
                    break
                }
                Thread.sleep(max(30000, districtList.size * 10).toLong())
            }
        }
        val thread = Thread(runnable)
        thread.start()

        return START_STICKY
    }

    private fun getVaccinationCenterList(response : String): MutableList<VaccineCenterCard> {
        val vaccinationCenterCardList: MutableList<VaccineCenterCard> = mutableListOf()
        val responseJSON = JSONObject(response)

        if (responseJSON.has("centers")) {
            val centerList = responseJSON.getJSONArray("centers")
            for (i in 0 until centerList.length()) {
                val center = centerList.getJSONObject(i)

                if (center.has("sessions")) {
                    val sessionsList = center.getJSONArray("sessions")
                    for (j in 0 until sessionsList.length()) {
                        val session = sessionsList.getJSONObject(j)

                        if (session.getString("min_age_limit") == ageGroup && session.getInt(
                                "available_capacity"
                            ) > 0
                        ) {
                            val card = VaccineCenterCard(
                                center.getString("name"),
                                center.getString("address"),
                                center.getString("district_name"),
                                center.getString("pincode"),
                                session.getString("date"),
                                session.getString("available_capacity")
                            )
                            vaccinationCenterCardList.add(card)
                        }
                    }
                }
            }
        }
        return vaccinationCenterCardList
    }

    private fun getDate(): String {
        val dateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        return dateTime.format(formatter)
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(SERVICE_CHANNEL_ID, "Services", NotificationManager.IMPORTANCE_DEFAULT)
        val serviceChannelManager = getSystemService(NotificationManager::class.java)
        serviceChannelManager.createNotificationChannel(serviceChannel)

        val alertChannel = NotificationChannel(ALERT_CHANNEL_ID, "something something", NotificationManager.IMPORTANCE_HIGH)
        val alertChannelManager = getSystemService(NotificationManager::class.java)
        alertChannelManager.createNotificationChannel(alertChannel)
    }

    private fun showNotification() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
        val notification = Notification
            .Builder(this, SERVICE_CHANNEL_ID)
            .setContentText("Searching for Vaccination Slots")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(SERVICE_NOTIFICATION_ID, notification)
    }

    private fun runAlertNotification() {
        val alertIntent = Intent(this, AvailableSlotsActivity::class.java)
        val alertPendingIntent = PendingIntent.getActivity(this, Random.nextInt(), alertIntent, 0)

        val alertNotification = NotificationCompat
            .Builder(this, ALERT_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Vaccinate")
            .setContentText("Vaccination Centers with available slots found")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(Notification.CATEGORY_CALL)
//            .setAutoCancel(false)
            .setVibrate(longArrayOf(0, 3000, 1000, 3000))
            .setFullScreenIntent(alertPendingIntent, true)
            .setOngoing(true)
            .build()

        startForeground(ALERT_NOTIFICATION_ID, alertNotification)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("ccss", "ondestroy")
        stopThread = true
        stopSelf()
    }
}