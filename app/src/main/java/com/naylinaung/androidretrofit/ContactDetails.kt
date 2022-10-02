package com.naylinaung.androidretrofit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.squareup.picasso.Picasso
import retrofit2.HttpException
import java.io.IOException

class ContactDetails : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_details)

        val did: String = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val name: String = intent.getStringExtra("contact_name").toString()
        val id: String = intent.getStringExtra("contact_id").toString()
        val contactName = findViewById<TextView>(R.id.contactName)
        val callBtn = findViewById<Button>(R.id.callContact)
        if(id==did){
            contactName.text = name+" ( Current Device _)"
            callBtn.text = "--"
        }else{
            contactName.text = name

            callBtn.setOnClickListener {


                lifecycleScope.launchWhenCreated {

                    val response = try {
                        val callData = CallContactRequest(id, did)
                        RetrofitInstance.api.callContact(callData)
                    } catch(e: IOException){
                        return@launchWhenCreated
                    } catch (e: HttpException) {
                        return@launchWhenCreated
                    }

                    if(response.isSuccessful && response.body() != null){
                        val resp = response.body()!!
                            Log.d("CALL", "call successful")

                        if(resp.room_token!=null) {
                            val intent = Intent(this@ContactDetails, CallActivity::class.java).apply {
                                putExtra(
                                    CallActivity.KEY_ARGS,
                                    CallActivity.BundleArgs(
                                        "",
                                        resp.room_token
                                    )
                                )
                            }

                            startActivity(intent)
                        }

                    }else{
                            Log.d("CALL", "call failed")
                    }

                }

            }
        }
        val contactImg = findViewById<ImageView>(R.id.contactImage)
        val url = "https://i.pravatar.cc/600"
        Picasso.get().load(url).into(contactImg)



    }
}