package com.naylinaung.androidretrofit

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.naylinaung.androidretrofit.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private lateinit var binding: ActivityMainBinding

    private lateinit var contactAdapter: ContactAdapter




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRecyclerView()
        logRegToken()

        requestNeededPermissions { }

        lifecycleScope.launchWhenCreated {


            binding.progressBar.isVisible = true
            val response = try {

                RetrofitInstance.api.getContacts("")
            } catch(e: IOException){
                binding.progressBar.isVisible = false
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

    private fun setupRecyclerView() = binding.rvTodos.apply {
        contactAdapter = ContactAdapter()
        adapter = contactAdapter
        layoutManager = LinearLayoutManager( this@MainActivity)

    }

    private fun logRegToken() {

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
                        val regData = SaveTokenRequest(did,Build.MANUFACTURER +" "+ Build.MODEL,token)
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


    private fun requestNeededPermissions(onHasPermissions: () -> Unit) {
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
                var hasDenied = false
                // Check if any permissions weren't granted.
                for (grant in grants.entries) {
                    if (!grant.value) {
                        Toast.makeText(this, "Missing permission: ${grant.key}", Toast.LENGTH_SHORT).show()

                        hasDenied = true
                    }
                }

                if (!hasDenied) {
                    onHasPermissions()
                }
            }

        // Assemble the needed permissions to request
        val neededPermissions = listOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA, )
            .let { perms ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    // Need BLUETOOTH_CONNECT permission on API S+ to output to bluetooth devices.
                    perms + listOf(Manifest.permission.BLUETOOTH_CONNECT)
                } else {
                    perms
                }
            }
            .filter { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_DENIED }
            .toTypedArray()

        if (neededPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(neededPermissions)
        } else {
            onHasPermissions()
        }
    }

}