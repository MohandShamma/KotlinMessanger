package com.example.messenger.RegisterationLogin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.messenger.Messages.LatestMessages
import com.example.messenger.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.login_activity.*

class LoginActivity : AppCompatActivity (){
    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)


        login_button.setOnClickListener {
            val email = email_edit_text_login.text.toString()
            val password = password_edit_text_login.text.toString()

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email , password)
                .addOnCompleteListener() {
                    if (!it.isSuccessful) return@addOnCompleteListener

                    //If successful
                    Log.d("LoginActivity" , "User logged in successfully")
                    Toast.makeText(this  , "User logged in successfully " ,Toast.LENGTH_SHORT).show()
                    val intent = Intent(this , LatestMessages::class.java)
                    startActivity(intent)
                }

                .addOnFailureListener {
                    Log.d("LoginActivity" , "user not found ${it.message}")
                    Toast.makeText(this , "${it.message}" , Toast.LENGTH_SHORT).show()
                }

        }

        back_to_register.setOnClickListener {
            finish()
        }
    }
}