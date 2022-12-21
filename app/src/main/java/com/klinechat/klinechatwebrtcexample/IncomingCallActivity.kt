package com.klinechat.klinechatwebrtcexample


import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.klinechat.klinechatwebrtcexample.call.CallActivity
import com.squareup.picasso.Picasso


class IncomingCallActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incoming_call)
        val urlString = "wss://webrtc.klinechat.com"
        val caller_name = intent.getStringExtra("caller_name")
        val room_token = intent.getStringExtra("room_token")
     //   val contactName = findViewById<TextView>(R.id.contactName)
     //   val callBtn = findViewById<Button>(R.id.callContact)
        val callerName = findViewById<TextView>(R.id.callerName)
        val callerImg = findViewById<ImageView>(R.id.callerImg)
        val acceptBtn = findViewById<Button>(R.id.acceptBtn)
        val rejectBtn = findViewById<Button>(R.id.rejectBtn)

        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)


        callerName.text = caller_name
        Picasso.get().load("https://i.pravatar.cc/600").into(callerImg)

        acceptBtn.setOnClickListener {
            Log.d("CALL", "call accepted")
//            Intent(this, CallActivity::class.java).also {
//                  it.putExtra("caller_name", caller_name)
//                  it.putExtra("room_token", room_token)
//                startActivity(it)
//            }

            val intent = Intent(this, CallActivity::class.java).apply {
                putExtra(
                    CallActivity.KEY_ARGS,
                    CallActivity.BundleArgs(
                        urlString,
                        room_token!!
                    )
                )
            }

            startActivity(intent)

        }

        rejectBtn.setOnClickListener({
            Log.d("CALL", "call rejected")
        })

        vibratePhone()

    }

    private fun vibratePhone() {
        val vibrator = this?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(200)
        }
    }
}