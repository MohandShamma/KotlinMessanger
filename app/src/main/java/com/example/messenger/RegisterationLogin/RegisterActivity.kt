package com.example.messenger.RegisterationLogin

import com.example.messenger.Models.User
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.example.messenger.Messages.LatestMessages
import com.example.messenger.Messages.NewMessageActivity
import com.example.messenger.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.register_activity.*
import java.util.*


@Suppress("DEPRECATION")
class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_activity)

        //Register button action
        register_button.setOnClickListener {
            register()
        }

        //Already have account button action
        already_have_account.setOnClickListener {
            val intent = Intent(this , LoginActivity::class.java)
            startActivity(intent)
        }

        //Photo selection button action
        photo_select_button.setOnClickListener {
            Log.d("RegisterActivity" , "Try to select photo")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type="image/*"
            startActivityForResult(intent , 0)

        }
    }

    //Overriding function for photo picking
    var selectedPhotoUri : Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data!=null)
        {
            Log.d("RegisterActivity" , "Photo selected")
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver , selectedPhotoUri)

            select_photo_image_view_register.setImageBitmap(bitmap)
            photo_select_button.alpha = 0f

            //val bitmapDrawable = BitmapDrawable(bitmap)
            //photo_select_button.setBackgroundDrawable(bitmapDrawable)
        }
    }

    //Register Function
    private fun register() {
        val username = username_edit_text_register.text.toString()
        val email = email_edit_text_register.text.toString()
        val password = password_edit_text_register.text.toString()

        if (email.isEmpty() || password.isEmpty())
        {
            Toast.makeText(this , "Please enter your email" , Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("RegisterActivity" , "Email is :" + email)
        Log.d("RegisterActivity" , "Password is " + password)

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email , password)
            .addOnCompleteListener(){
                if (!it.isSuccessful) return@addOnCompleteListener

                //if succesfully created
                uploadImageToFirebase()
                Log.d("RegisterActivity" , "User successfully created ${it.result!!.user!!.uid}")
            }
            .addOnFailureListener {
                Log.d("RegisterActivity" , "${it.message}")
                Toast.makeText(this , "${it.message}" , Toast.LENGTH_SHORT).show()
            }
    }

    //Function to upload image to firebase database
    private fun uploadImageToFirebase()
    {
        var filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("RegisterActivity" , "Photo uploaded ${it.metadata?.path}")
                ref.downloadUrl.addOnCompleteListener {
                    saveUsersToFirebaseDatabase(it.toString())
                    Log.d("RegisterActivity" , "Location : $it")
                }
                    .addOnFailureListener {
                        Log.d("RegisterActivity" , "Problem is ${it.message}")
                    }
            }
    }

    //Function to save users to firebase realtime database
    private fun saveUsersToFirebaseDatabase(profileImageUrl : String){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = User(uid , username_edit_text_register.text.toString() , profileImageUrl)
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("RegisterActivity" , "User successfully saved to database")

             val intent = Intent(this , LatestMessages::class.java)
             intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
             startActivity(intent)
            }
            .addOnFailureListener {
                Log.d("RegisterActivity" , "${it.message}")
            }
    }
}

