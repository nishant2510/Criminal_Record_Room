package com.example.minorproject.ui

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.minorproject.R
import com.example.minorproject.data.CasePriority
import com.example.minorproject.data.Criminal
import com.example.minorproject.data.Gender
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.list_item.*

class CriminalListAdapter(private val listener: (Long) -> Unit): ListAdapter<Criminal, CriminalListAdapter.ViewHolder>(DiffCallback()){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemLayout = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return ViewHolder(itemLayout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder (override val containerView: View) : RecyclerView.ViewHolder(containerView),
        LayoutContainer {
        init{
            itemView.setOnClickListener{
                listener.invoke(getItem(absoluteAdapterPosition).id)
            }
        }

        fun bind(criminal: Criminal){
            with(criminal) {

                criminal_name.text = name
                aadhaar_number.text = aadhaarNumber
                case_priority.text =  CasePriority.values()[criminal.priority].name
                criminal_age.text = containerView.context.resources.getString(R.string.years, criminal.age)
                criminal_gender.text = Gender.values()[criminal.gender].name

                with(photo){
                    if(isNotEmpty()){
                        criminal_photo.setImageURI(Uri.parse(this))
                    }
                    else{
                        criminal_photo.setImageResource(R.drawable.blank)
                    }
                }
            }
        }
    }
}

class DiffCallback : DiffUtil.ItemCallback<Criminal>() {
    override fun areItemsTheSame(oldItem: Criminal, newItem: Criminal): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Criminal, newItem: Criminal): Boolean {
        return oldItem == newItem
    }
}