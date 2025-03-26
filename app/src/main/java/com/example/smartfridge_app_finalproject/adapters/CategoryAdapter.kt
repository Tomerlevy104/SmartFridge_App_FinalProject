package com.example.smartfridge_app_finalproject.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartfridge_app_finalproject.R
import com.example.smartfridge_app_finalproject.data.model.Category
import com.google.android.material.textview.MaterialTextView

/**
 *  Adapter for displaying categories in a RecyclerView.
 */
class CategoryAdapter(private val onCategoryClick: (Category) -> Unit) :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var categories = listOf<Category>()

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.category_image)
        private val textView: MaterialTextView = itemView.findViewById(R.id.category_name)

    /////////////////////////////////////////////////////////////////////////////////////////////////////
        fun bind(category: Category) {
            // Set image resource
            imageView.setImageResource(category.categoryImage)

            // Set category name
            textView.text = category.name

            // Set click listener
            itemView.setOnClickListener {
                onCategoryClick(category)
            }
        }
    /////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    override fun getItemCount() = categories.size

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    fun updateCategories(newCategories: List<Category>) {
        categories = newCategories
        notifyDataSetChanged()
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////
}