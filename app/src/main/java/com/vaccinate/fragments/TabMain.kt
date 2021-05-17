package com.vaccinate.fragments

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.Toast
import androidx.core.view.isVisible
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.vaccinate.PollService
import com.vaccinate.R
import com.vaccinate.databinding.FragmentTabMainBinding
import org.json.JSONObject

class TabMain : Fragment() {
    private lateinit var stateIds: ArrayList<String>
    private lateinit var stateNames: ArrayList<String>
    private val districtIds: MutableList<String> = mutableListOf()
    private lateinit var selectedStateName : String
    private var selectedDistrictsNameList : MutableList<String> = mutableListOf()
    private var ageGroup : String?= null
    private var dose : String?= null
    private lateinit var binding: FragmentTabMainBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentTabMainBinding.inflate(layoutInflater, container, false)

        // itemSelectedListener for stateAutoComplete
        binding.stateAutoComplete.onItemClickListener = AdapterView.OnItemClickListener {
                parent : AdapterView<*>?,
                _: View?,
                position: Int,
                _: Long ->
            run {
                if (parent != null) {
                    val stateId: String = stateIds[position]
                    selectedStateName = parent.getItemAtPosition(position).toString()
                    resetDistrictCheckBox(stateId)
                }
            }
        }

        // itemSelectedListener for ageGroupAutoComplete
        binding.ageGroupAutoComplete.onItemClickListener = AdapterView.OnItemClickListener {
                _ : AdapterView<*>?,
                _: View?,
                position: Int,
                _: Long ->
            run {
                    ageGroup = if (position == 0) "18" else "45"
                    binding.doseInputLayout.visibility = View.VISIBLE
                }
         }

        // add agGroupAdapter
        val agGroupAdapter: ArrayAdapter<String> = ArrayAdapter(
            requireContext(),
            R.layout.support_simple_spinner_dropdown_item,
            arrayOf("18-44", "45+")
        )
        binding.ageGroupAutoComplete.setAdapter(agGroupAdapter)

        // itemSelectedListener for doseAutoComplete
        binding.doseAutoComplete.onItemClickListener = AdapterView.OnItemClickListener {
                _ : AdapterView<*>?,
                _: View?,
                position: Int,
                _: Long ->
            run {
                dose = if (position == 0) "available_capacity_dose1" else "available_capacity_dose2"
            }
        }

        // add agGroupAdapter
        val doseAdapter: ArrayAdapter<String> = ArrayAdapter(
            requireContext(),
            R.layout.support_simple_spinner_dropdown_item,
            arrayOf("Dose 1", "Dose 2")
        )
        binding.doseAutoComplete.setAdapter(doseAdapter)

        // onClickListener for startStopBtn
        binding.startStopBtn.setOnClickListener {
            startStopService()
        }

        // disable keyboard input
        binding.stateAutoComplete.inputType = InputType.TYPE_NULL
        binding.ageGroupAutoComplete.inputType = InputType.TYPE_NULL
        binding.doseAutoComplete.inputType = InputType.TYPE_NULL
        return binding.root
    }

    private fun resetStateSpinner() {
        val url = "https://cdn-api.co-vin.in/api/v2/admin/location/states"
        val stringRequest = StringRequest(
            Request.Method.GET,
            url,
            { response ->
                val responseJSON = JSONObject(response)

                if (responseJSON.has("states")) {
                    val stateList = responseJSON.getJSONArray("states")

                    stateNames = ArrayList(stateList.length())
                    stateIds = ArrayList(stateList.length())

                    for (i in 0 until stateList.length()) {
                        stateNames.add(stateList.getJSONObject(i).getString("state_name"))
                        stateIds.add(stateList.getJSONObject(i).getInt("state_id").toString())
                    }

                    val stateAdapter: ArrayAdapter<String> = ArrayAdapter(
                        requireContext(),
                        R.layout.support_simple_spinner_dropdown_item,
                        stateNames
                    )
                    binding.stateAutoComplete.setAdapter(stateAdapter)
                    binding.stateInputLayout.visibility = View.VISIBLE
                }
            },
            {}
        )
        Volley.newRequestQueue(requireContext()).add(stringRequest)
    }

    private fun resetDistrictCheckBox(stateId: String) {
        // clear existing checkboxes
        if (binding.checkBoxHolderLayout.childCount > 0) {
            binding.checkBoxHolderLayout.removeAllViews()
            binding.scrollView.setBackgroundResource(0)
        }

        val url = "https://cdn-api.co-vin.in/api/v2/admin/location/districts/$stateId"
        val stringRequest = StringRequest(
            Request.Method.GET,
            url,
            { response ->
                val responseJSON = JSONObject(response)

                if (responseJSON.has("districts")) {
                    binding.scrollView.setBackgroundResource(R.drawable.district_selector_border)

                    val districtList = responseJSON.getJSONArray("districts")
                    val checkBoxList: ArrayList<CheckBox> = ArrayList(districtList.length())
                    for (i in 0 until districtList.length()) {
                        val checkBox = CheckBox(requireContext())
                        checkBox.text = districtList.getJSONObject(i).getString("district_name")
                        checkBox.setOnCheckedChangeListener { _, is_checked ->
                            if (is_checked) {
                                districtIds.add(districtList.getJSONObject(i).getInt("district_id").toString())
                                selectedDistrictsNameList.add(districtList.getJSONObject(i).getString("district_name"))
                                if (!binding.ageGroupInputLayout.isVisible) {
                                    binding.ageGroupInputLayout.visibility = View.VISIBLE
                                }
                            } else {
                                districtIds.remove(districtList.getJSONObject(i).getInt("district_id").toString())
                                selectedDistrictsNameList.remove(districtList.getJSONObject(i).getString("district_name"))
                            }
                        }
                        binding.checkBoxHolderLayout.addView(checkBox)
                        checkBoxList.add(checkBox)
                    }
                }
            },
            {}
        )
        Volley.newRequestQueue(requireContext()).add(stringRequest)
    }

    private fun startStopService() {
        Log.d("ccss", districtIds.toString())
        if (binding.startStopBtn.text == "Start") {
            if (districtIds.size > 0 && ageGroup != null) {
                val serviceIntent = Intent(requireContext(), PollService::class.java)
                serviceIntent.putExtra("Dose", dose)
                serviceIntent.putExtra("AgeGroup", ageGroup)
                serviceIntent.putExtra("State", selectedStateName)
                serviceIntent.putStringArrayListExtra("DistrictIDList", ArrayList(districtIds))
                serviceIntent.putStringArrayListExtra("DistrictNamesList", ArrayList(selectedDistrictsNameList))
                requireContext().startService(serviceIntent)
                Toast.makeText(requireContext(), "Service Started", Toast.LENGTH_SHORT).show()
                binding.startStopBtn.text = "Stop"

                // reset
                binding.checkBoxHolderLayout.removeAllViews()
                binding.scrollView.setBackgroundResource(0)
                binding.stateAutoComplete.clearListSelection()
                binding.ageGroupInputLayout.visibility = View.INVISIBLE
                binding.doseInputLayout.visibility = View.INVISIBLE
                districtIds.clear()
            }
        } else {
            val serviceIntent = Intent(requireContext(), PollService::class.java)
            requireContext().stopService(serviceIntent)
            Toast.makeText(requireContext(), "Service Stopped", Toast.LENGTH_SHORT).show()
            binding.startStopBtn.text = "Start"
        }
    }

    private fun isServiceRunning(mClass: Class<PollService>): Boolean {
        val manager: ActivityManager = requireContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        for (service: ActivityManager.RunningServiceInfo in manager.getRunningServices(Int.MAX_VALUE)) {
            if (mClass.name.equals(service.service.className)) {
                return true
            }
        }
        return false
    }

    override fun onStart() {
        super.onStart()

        // Select State
        resetStateSpinner()

        setButtonText()
    }

    private fun setButtonText() {
        if (isServiceRunning(PollService::class.java)) {
            binding.startStopBtn.text = "Stop"
        } else {
            binding.startStopBtn.text = "Start"
        }
    }
}