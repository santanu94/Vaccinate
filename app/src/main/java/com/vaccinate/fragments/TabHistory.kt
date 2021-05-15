package com.vaccinate.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.vaccinate.R
import com.vaccinate.fragments.adapters.HistoryViewAdapter
import com.vaccinate.util.FileReadWrite
import kotlinx.android.synthetic.main.activity_available_slots.*
import kotlinx.android.synthetic.main.fragment_tab_history.*
import org.json.JSONArray
import org.json.JSONObject


class TabHistory : Fragment() {
    private lateinit var frw : FileReadWrite
    private val sessionHistory = ArrayList<String>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        frw = FileReadWrite("${requireContext().dataDir}/service_history.json")

        return inflater.inflate(R.layout.fragment_tab_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        clearHistory.setOnClickListener {
            frw.delete()
            sessionHistory.clear()
            updateView()
        }
    }

    override fun onResume() {
        super.onResume()
        sessionHistory.clear()

        val records: String = try {
            frw.read()
        } catch (e : Exception) {
            ""
        }

        var session = ""
        val lines = records.split("\n")
        for (i in lines.size - 1 downTo 0) {
            if (lines[i] == "") {
                if (session != "") {
                    sessionHistory.add(session)
                }
                session = ""
                continue
            }
            val json = JSONObject(lines[i])

            val keyList = json.keys()
            while (keyList.hasNext()) {
                val key = keyList.next()
                val value = json[key]

                if (key == "Centers") {
                    session = if (value == "null") {
                        "Centers Found: 0"
                    } else {
                        "Centers Found: ${JSONArray(value.toString()).length()}\n" + session
                    }
                    continue
                }

                if ((key == "Status") && session.contains(key, false)) {
                    continue
                }

                session = "${key}: $value\n" + session
            }
        }
        if (session != "")
            sessionHistory.add(session)

        updateView()
    }

    private fun updateView() {
        val historyViewAdapter = HistoryViewAdapter(sessionHistory, requireContext())
        historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        historyRecyclerView.adapter = historyViewAdapter

        if (sessionHistory.size == 0) {
            clearHistory.visibility = View.INVISIBLE
        } else {
            clearHistory.visibility = View.VISIBLE
        }
    }
}