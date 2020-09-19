package com.example.messenger.Messages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.messenger.R
import com.example.messenger.Models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*

class NewMessageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        supportActionBar?.title = "Select user"
        fetchUsers()
    }

    companion object{
        val USER_KEY = "User_Key"
    }

    private fun fetchUsers () {
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val adapter = GroupAdapter<ViewHolder>()
                snapshot.children.forEach {
                    Log.d("NewMessage" , it.toString())
                    val user = it.getValue(User::class.java)
                    if (user!= null)
                    {
                        adapter.add(UserItem(user))
                    }
                    adapter.setOnItemClickListener { item, view ->
                        val userItem = item as UserItem
                        val intent = Intent(view.context , ChatLog::class.java)
                        intent.putExtra(USER_KEY , userItem.user)
                        startActivity(intent)
                    }
                }
                recycler_view_new_messages.adapter = adapter
            }
            override fun onCancelled(error: DatabaseError) {
            }

        })

    }
}


class UserItem (val user : User): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        val url = "https://firebasestorage.googleapis.com/v0/b/messenger-ea6e9.appspot.com/o/images%2F1359c942-168c-4fc4-936c-a1d8d0fd2fbc?alt=media&token=35601ef1-4a7b-43a1-92df-16a236436907"
        Picasso.get().load(url).into(viewHolder.itemView.image_view_row_new_message)
        viewHolder.itemView.username_text_row_new_message.text = user.username

    }

    override fun getLayout(): Int {
        return R.layout.user_row_new_message
    }
}
