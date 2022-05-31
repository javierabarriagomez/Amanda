package com.saludencamino.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class ViewPageAdapter(private var images: List<Int>):RecyclerView.Adapter<ViewPageAdapter.Page2ViewHolder>() {

    inner class Page2ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val itemImage: ImageView = itemView.findViewById(R.id.myImage)
        init{

        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewPageAdapter.Page2ViewHolder {
        return Page2ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.main_item_1,parent,false))
    }
    override fun onBindViewHolder(holder: ViewPageAdapter.Page2ViewHolder, position: Int) {
        holder.itemImage.setImageResource(images[position])
    }


    override fun getItemCount(): Int {
        return images.size
    }

}