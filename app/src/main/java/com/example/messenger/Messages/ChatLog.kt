package com.example.messenger.Messages

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Adapter
import com.example.messenger.R
import com.google.firebase.auth.FirebaseAuth
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import com.example.messenger.Models.ChatMessage
import com.example.messenger.Models.User
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.activity_chat_log.view.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*

class ChatLog : AppCompatActivity() {

    companion object{
        val TAG = "ChatLog"
    }

    val adapter = GroupAdapter<ViewHolder>()

    var toUser: User? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title = toUser?.username

        recycler_view_chat_log.adapter = adapter
        listenToMessages()
        send_button_chat.setOnClickListener {
            Log.d(TAG, "Attempt to send message.......")
            performMessageSent()
        }
    }

    private fun listenToMessages(){
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        ref.addChildEventListener(object : ChildEventListener{


            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)
                if (chatMessage != null){
                    Log.d(TAG , chatMessage.text)

                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid)
                    {
                        val currentUser = LatestMessages.currentUser ?: return
                        adapter.add(ChatFromItem(chatMessage.text , currentUser))
                    }else {
                        adapter.add(ChatToItem(chatMessage.text , toUser!!))
                    }
                }

                recycler_view_chat_log.scrollToPosition(adapter.itemCount-1)
            }
            override fun onCancelled(error: DatabaseError) {

            }



            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }
        })
    }



    private fun performMessageSent(){
        val text = message_to_send.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid

        //val ref = FirebaseDatabase.getInstance().getReference("/messages").push()

        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId" ).push()
        val toRef = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()
        if (fromId  == null) return
        val chatmessage = ChatMessage(ref.key!!,text,fromId,toId!!,System.currentTimeMillis()/1000)
        ref.setValue(chatmessage)
            .addOnSuccessListener {
                Log.d(TAG , "MESSAGE SAVER TO FIREBASE ${ref.key}")
                message_to_send.text.clear()
                recycler_view_chat_log.scrollToPosition(adapter.itemCount-1)
            }
        toRef.setValue(chatmessage)
            .addOnSuccessListener {
                recycler_view_chat_log.scrollToPosition(adapter.itemCount-1)
            }

        val latestMessagesRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        latestMessagesRef.setValue(chatmessage)
        val latestMessagesToRef = FirebaseDatabase.getInstance().getReference("latest-messages/$toId/$fromId")
        latestMessagesToRef.setValue(chatmessage)
    }
}

class ChatFromItem (val text : String , val user : User): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.message_from_chat.text = text
        val uri = "https://firebasestorage.googleapis.com/v0/b/messenger-ea6e9.appspot.com/o/images%2F19d1c56d-31ce-435b-9dd4-4f4a1d78031f?alt=media&token=0d3a2f1a-ca69-4370-83cc-b2b79cfefab9"
        val target = viewHolder.itemView.image_from_chat
        Picasso.get().load(uri).into(target)
    }
    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}

class ChatToItem(val text: String , val user : User) : Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.message_to_chat.text = text

        val uri = "https://firebasestorage.googleapis.com/v0/b/messenger-ea6e9.appspot.com/o/images%2F19d1c56d-31ce-435b-9dd4-4f4a1d78031f?alt=media&token=0d3a2f1a-ca69-4370-83cc-b2b79cfefab9"
        val target = viewHolder.itemView.image_to_chat
        Picasso.get().load(uri).into(target)
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}