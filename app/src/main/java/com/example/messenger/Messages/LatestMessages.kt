package com.example.messenger.Messages

import android.content.Intent
import  com.example.messenger.Models.User
import  com.example.messenger.Models.ChatMessage
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.messenger.R
import com.example.messenger.RegisterationLogin.RegisterActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_latest_messeges.*
import kotlinx.android.synthetic.main.latest_messages_row.*
import kotlinx.android.synthetic.main.latest_messages_row.view.*


class LatestMessages : AppCompatActivity() {

    companion object{
        var currentUser : User? = null
        val TAG = "LatestMessages"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messeges)

        recycler_view_latest_messages.adapter = adapter
        recycler_view_latest_messages.addItemDecoration(DividerItemDecoration(this , DividerItemDecoration.VERTICAL))

        adapter.setOnItemClickListener { item, view ->
            val intent = Intent(view.context , ChatLog::class.java)

            val row = item as LatestMessages
            intent.putExtra(NewMessageActivity.USER_KEY , row.chatPartenerUser)
            startActivity(intent)
        }
        fetchCurrentUser()
        verifyUserIsLoggedIn()

        listenForLatestMessages()

    }


    class LatestMessages (val chatMessages : ChatMessage): Item<ViewHolder>(){

        var chatPartenerUser : User? = null
        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.latest_message_latest_message.text = chatMessages.text

            val chatPartnerId : String
            if (chatMessages.fromId == FirebaseAuth.getInstance().uid){
                chatPartnerId = chatMessages.toId
            }else {
                chatPartnerId = chatMessages.fromId
            }

            val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
            ref.addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    chatPartenerUser = snapshot.getValue(User::class.java)
                    viewHolder.itemView.user_name_latest_message.text = chatPartenerUser?.username

                    val url = "https://firebasestorage.googleapis.com/v0/b/messenger-ea6e9.appspot.com/o/images%2F19d1c56d-31ce-435b-9dd4-4f4a1d78031f?alt=media&token=0d3a2f1a-ca69-4370-83cc-b2b79cfefab9"
                    Picasso.get().load(url).into(viewHolder.itemView.image_latest_message_row)
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }

        override fun getLayout(): Int {
            return R.layout.latest_messages_row
        }
    }

    val latestMessagesHashmap = HashMap<String , ChatMessage>()
    private fun refreshRecyclerView(){
        adapter.clear()
        latestMessagesHashmap.values.forEach {
            adapter.add(LatestMessages(it))
        }
    }
    private fun listenForLatestMessages(){
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
        ref.addChildEventListener(object : ChildEventListener{

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return
                adapter.add(LatestMessages(chatMessage))
                latestMessagesHashmap[snapshot.key!!] = chatMessage
                refreshRecyclerView()

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return
                adapter.add(LatestMessages(chatMessage))
                latestMessagesHashmap[snapshot.key!!] = chatMessage
                refreshRecyclerView()
            }
            override fun onCancelled(error: DatabaseError) {

            }
            override fun onChildRemoved(snapshot: DataSnapshot) {

            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }
        })
    }

    val adapter = GroupAdapter<ViewHolder>()
//    private fun setUpDummyRows(){
//
//
//        adapter.add(LatestMessages())
//        adapter.add(LatestMessages())
//        adapter.add(LatestMessages())
//        adapter.add(LatestMessages())
//        adapter.add(LatestMessages())
//    }

    private fun fetchCurrentUser(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser = snapshot.getValue(User::class.java)
                Log.d("LatestMessages" , "Current User ${currentUser?.username}")
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun verifyUserIsLoggedIn(){
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null)
        {
            val intent = Intent(this , RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item?.itemId)
        {
            R.id.menu_new_message -> {
                val intent = Intent(this , NewMessageActivity::class.java)
                startActivity(intent)
            }

            R.id.sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this , RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_resource_file, menu)
        return super.onCreateOptionsMenu(menu)
    }
}
