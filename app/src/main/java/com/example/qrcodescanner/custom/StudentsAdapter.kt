package com.example.qrcodescanner.custom

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.qrcodescanner.R

class StudentsAdapter(private val context: Activity, private val arrayList: ArrayList<Person>) :
    ArrayAdapter<Person>(context, R.layout.list_item_student, arrayList) {

    @SuppressLint("ViewHolder", "InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater: LayoutInflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.list_item_student, null)

        val fio: TextView = view.findViewById(R.id.fio)

        fio.text = arrayList[position].name

        return view
    }
}

