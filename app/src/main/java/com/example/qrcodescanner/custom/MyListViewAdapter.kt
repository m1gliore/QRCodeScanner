package com.example.qrcodescanner.custom

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.qrcodescanner.R

class MyListViewAdapter(private val context: Activity, private val arrayList: ArrayList<User>) :
    ArrayAdapter<User>(context, R.layout.list_item, arrayList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater: LayoutInflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.list_item, null)

        val lesson: TextView = view.findViewById(R.id.lesson)
        val group: TextView = view.findViewById(R.id.group)
        val dateTime: TextView = view.findViewById(R.id.date_time)

        lesson.text = arrayList[position].className
        group.text = arrayList[position].groupNames
        dateTime.text = arrayList[position].date

        return view
    }

}