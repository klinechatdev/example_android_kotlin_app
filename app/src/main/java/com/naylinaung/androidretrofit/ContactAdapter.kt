package com.naylinaung.androidretrofit

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.naylinaung.androidretrofit.databinding.ItemTodoBinding
import com.squareup.picasso.Picasso

class ContactAdapter : RecyclerView.Adapter<ContactAdapter.TodoViewHolder>() {

        inner class TodoViewHolder(val binding: ItemTodoBinding) : RecyclerView.ViewHolder(binding.root)

        private val diffCallback = object : DiffUtil.ItemCallback<Contact>() {
            override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
                return oldItem.id == newItem.id
            }
            override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
                return oldItem == newItem
            }
        }


        private val differ = AsyncListDiffer( this, diffCallback)

        var contacts: List<Contact>

        get() = differ.currentList
        set(value) { differ.submitList(value) }

        override fun getItemCount() = contacts.size

        override fun onCreateViewHolder( parent: ViewGroup, viewType: Int): TodoViewHolder {

            return TodoViewHolder(ItemTodoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ))
        }

        override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
            holder.binding.apply {
                val contact = contacts[position]
                tvTitle.text = contact.name
                Picasso.get().load("https://i.pravatar.cc/100").into(imageView)
            }

            holder.itemView.findViewById<Button>(R.id.callBtn).setOnClickListener(View.OnClickListener {
                //Toast.makeText(holder.itemView.context, "Calling...", Toast.LENGTH_SHORT).show()

                Intent(holder.itemView.context, ContactDetails::class.java).also {
                    it.putExtra("contact_name", contacts[position].name)
                    it.putExtra("contact_id", contacts[position].id)
                    holder.itemView.context.startActivity(it)
                }
            })


        }



}