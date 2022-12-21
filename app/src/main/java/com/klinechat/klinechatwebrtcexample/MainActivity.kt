package com.klinechat.klinechatwebrtcexample


import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.klinechat.klinechatwebrtcexample.api.RetrofitInstance
import com.klinechat.klinechatwebrtcexample.databinding.MainActivityBinding


// imports from library
import com.klinechat.webrtcsdk.MainViewModel
import com.klinechat.webrtcsdk.util.requestNeededPermissions
import retrofit2.HttpException
import java.io.IOException


class MainActivity : AppCompatActivity() {

   // val viewModel by viewModels<MainViewModel>()

    private val TAG = "MainActivity"
    private lateinit var binding: MainActivityBinding
    private lateinit var contactAdapter: ContactAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

         binding = MainActivityBinding.inflate(layoutInflater)



        setContentView(binding.root)
        setupRecyclerView()
        requestNeededPermissions { }
        registerDevice();

        lifecycleScope.launchWhenCreated {

            binding.progressBar.isVisible = true
            val response = try {

                RetrofitInstance.api.getContacts("")

            } catch(e: IOException){
                binding.progressBar.isVisible = false
                Toast.makeText(this@MainActivity, "Could not load contacts from server!", Toast.LENGTH_LONG).show()
                return@launchWhenCreated
            } catch (e: HttpException) {
                binding.progressBar.isVisible = false
                return@launchWhenCreated
            }

            if(response.isSuccessful && response.body() != null){
                val resp = response.body()!!
                contactAdapter.contacts = resp.data
            }

            binding.progressBar.isVisible = false

        }




    }

    private fun setupRecyclerView() = binding.contactList.apply {
        contactAdapter = ContactAdapter()
        adapter = contactAdapter
        layoutManager = LinearLayoutManager( this@MainActivity)

    }

    private fun registerDevice() {

        Log.d(TAG, "logging regToken...")
        // [START log_reg_token]
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }
                lifecycleScope.launchWhenCreated {

                    // Get new FCM registration token
                    val token = task.result

                    // Log and toast
                    val msg = "FCM Registration token: $token"
                    Log.d(TAG, msg)
                    //  Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                    val did: String = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                    val regData = SaveTokenRequest(did, Build.MANUFACTURER +" "+ Build.MODEL,token)
                    //send to server
                    val respd = try {
                        RetrofitInstance.api.saveToken(regData)
                    } catch(e: IOException){
                        Log.d(TAG, "io exception")
                        return@launchWhenCreated
                    } catch (e: HttpException) {
                        Log.d(TAG, "http exception")
                        return@launchWhenCreated
                    }

                    if(respd.isSuccessful && respd.body() != null){
                        val resp = respd.body()!!

                        Log.d(TAG, "device registration ok")
                    }else{
                        Log.d(TAG, "something wrong with device registration")
                    }
                }
            })
        // [END log_reg_token]

    }


}