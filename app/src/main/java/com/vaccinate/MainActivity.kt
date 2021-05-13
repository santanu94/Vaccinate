package com.vaccinate

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.vaccinate.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private val districtIds : MutableList<String> = mutableListOf()
    private lateinit var ageGroup : String
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //setContentView(R.layout.activity_main)

        // Select State
        stateSpinner()

        // Select Age Group
        val agGroupAdapter : ArrayAdapter<String> = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, arrayOf<String>("18-44", "45+"))
        ageGroupSelector.adapter = agGroupAdapter

        ageGroupSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                ageGroup = if (position == 0) "18" else "45"
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        setButtonText()
        binding.startStopBtn.setOnClickListener {
            startStopService()
        }
    }

    private fun stateSpinner() {
        val url = "https://cdn-api.co-vin.in/api/v2/admin/location/states"
        val stringRequest = StringRequest(
            Request.Method.GET,
            url,
            { response ->
                val responseJSON = JSONObject(response)

                if (responseJSON.has("states")) {
                    val stateList = responseJSON.getJSONArray("states")

                    val stateNames : ArrayList<String> = ArrayList(stateList.length()+1)
                    val stateIds : ArrayList<String> = ArrayList(stateList.length())
                    stateNames.add("Select State")
                    stateIds.add("0")


                    for (i in 0 until stateList.length()) {
                        stateNames.add(stateList.getJSONObject(i).getString("state_name"))
                        stateIds.add(stateList.getJSONObject(i).getInt("state_id").toString())
                    }

                    val stateAdapter : ArrayAdapter<String> = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, stateNames)
                    stateSelector.adapter = stateAdapter
                    stateSelector.visibility = View.VISIBLE

                    stateSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            val stateId : String = stateIds[position]
                            if (stateId != "0") {
                                districtCheckBox(stateId)
                            }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }
                }
            },
            {}
        )
        Volley.newRequestQueue(this).add(stringRequest)
    }

    private fun districtCheckBox(stateId : String) {
        // clear existing checkboxes
        if (checkBoxHolderLayout.childCount > 0) {
            Log.d("ccss", "great")
            checkBoxHolderLayout.removeAllViews()
        }

        val url = "https://cdn-api.co-vin.in/api/v2/admin/location/districts/$stateId"
        val stringRequest = StringRequest(
            Request.Method.GET,
            url,
            { response ->
                val responseJSON = JSONObject(response)

                if (responseJSON.has("districts")) {
                    val districtList = responseJSON.getJSONArray("districts")
                    val checkBoxList : ArrayList<CheckBox> = ArrayList(districtList.length())


                    for (i in 0 until districtList.length()) {
                        val checkBox = CheckBox(this)
                        checkBox.text = districtList.getJSONObject(i).getString("district_name")
                        checkBox.setOnCheckedChangeListener{ _, is_checked ->
                            if (is_checked) {
                                districtIds.add(districtList.getJSONObject(i).getInt("district_id").toString())
                            } else {
                                districtIds.remove(districtList.getJSONObject(i).getInt("district_id").toString())
                            }
                        }
                        checkBoxHolderLayout.addView(checkBox)
                        checkBoxList.add(checkBox)
                    }
                }
            },
            {}
        )
        Volley.newRequestQueue(this).add(stringRequest)
    }

    private fun startStopService() {
        Log.d("ccss", districtIds.toString())
        if (binding.startStopBtn.text == "Start") {
            if (districtIds.size > 0) {
                val serviceIntent = Intent(this, PollVac::class.java)
                serviceIntent.putExtra("AgeGroup", ageGroup)
                serviceIntent.putStringArrayListExtra("DistrictList", ArrayList(districtIds))
                startService(serviceIntent)
                Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show()
                binding.startStopBtn.text = "Stop"
                districtIds.clear()
            }
        } else {
            val serviceIntent = Intent(this, PollVac::class.java)
            stopService(serviceIntent)
            Toast.makeText(this, "Service Stopped", Toast.LENGTH_SHORT).show()
            binding.startStopBtn.text = "Start"
        }
    }

    private fun isServiceRunning(mClass : Class<PollVac>) : Boolean {
        val manager : ActivityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        for (service : ActivityManager.RunningServiceInfo in manager.getRunningServices(Int.MAX_VALUE)) {
            if (mClass.name.equals(service.service.className)) {
                return true
            }
        }
        return false
    }

    override fun onRestart() {
        super.onRestart()
        setButtonText()
    }

    private fun setButtonText() {
        if (isServiceRunning(PollVac::class.java)) {
            binding.startStopBtn.text = "Stop"
        } else {
            binding.startStopBtn.text = "Start"
        }
    }
}