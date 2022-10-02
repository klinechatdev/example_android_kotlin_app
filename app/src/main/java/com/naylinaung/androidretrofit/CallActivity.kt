package com.naylinaung.androidretrofit


import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.Parcelable
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupieAdapter
import com.naylinaung.androidretrofit.databinding.CallActivityBinding
import com.naylinaung.androidretrofit.dialog.showAddParticipantDialog
import com.naylinaung.androidretrofit.dialog.showDebugMenuDialog
import com.naylinaung.androidretrofit.dialog.showSelectAudioDeviceDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.parcelize.Parcelize

class CallActivity : AppCompatActivity() {

//    val viewModel: CallViewModel by viewModelByFactory {
//        CallViewModel("wss://webrtc.codingelephant.tech", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ2aWRlbyI6eyJyb29tSm9pbiI6dHJ1ZSwicm9vbSI6InN0YXJrLXRvd2VyIiwiY2FuUHVibGlzaCI6dHJ1ZSwiY2FuU3Vic2NyaWJlIjp0cnVlfSwiaWF0IjoxNjYxNzE2ODk0LCJuYmYiOjE2NjE3MTY4OTQsImV4cCI6MTY2MTczODQ5NCwiaXNzIjoiQVBJcUZaNHhlWlhvUnhRIiwic3ViIjoiMDk5NzU0NTIxODQiLCJqdGkiOiIwOTk3NTQ1MjE4NCJ9.b_smycF5jeChIo8GEuJeJGfe8nRMyPjBBXLYiEHTHkk", application)
//    }

    val viewModel: CallViewModel by viewModelByFactory {
        val args = intent.getParcelableExtra<BundleArgs>(KEY_ARGS)
            ?: throw NullPointerException("args is null!")
        CallViewModel("wss://webrtc.codingelephant.tech", args.token, application)
    }
    lateinit var binding: CallActivityBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        binding = CallActivityBinding.inflate(layoutInflater)

        setContentView(binding.root)

        // Audience row setup
        val audienceAdapter = GroupieAdapter()
        binding.audienceRow.apply {
            layoutManager = LinearLayoutManager(this@CallActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = audienceAdapter
        }

        lifecycleScope.launchWhenCreated {
            viewModel.participants
                .collect { participants ->
                    val items = participants.map { participant -> ParticipantItem(viewModel.room, participant) }
                    audienceAdapter.update(items)
                }
        }

        // speaker view setup
        val speakerAdapter = GroupieAdapter()
        binding.speakerView.apply {
            layoutManager = LinearLayoutManager(this@CallActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = speakerAdapter
        }
        lifecycleScope.launchWhenCreated {
            viewModel.primarySpeaker.collectLatest { speaker ->
                val items = listOfNotNull(speaker)
                    .map { participant -> ParticipantItem(viewModel.room, participant, speakerView = true) }
                speakerAdapter.update(items)
            }
        }

        // Controls setup
        viewModel.cameraEnabled.observe(this) { enabled ->
            binding.camera.setOnClickListener { viewModel.setCameraEnabled(!enabled) }
            binding.camera.setImageResource(
                if (enabled) R.drawable.outline_videocam_24
                else R.drawable.outline_videocam_off_24
            )
            binding.flipCamera.isEnabled = enabled
        }

        viewModel.micEnabled.observe(this) { enabled ->
            binding.mic.setOnClickListener { viewModel.setMicEnabled(!enabled) }
            binding.mic.setImageResource(
                if (enabled) R.drawable.outline_mic_24
                else R.drawable.outline_mic_off_24
            )
        }

        viewModel.participantLeft.observe( this) { yes->
            if(yes){
                goToHome()
            }
        }

        binding.flipCamera.setOnClickListener { viewModel.flipCamera() }


        binding.exit.setOnClickListener {
            //finish()
            goToHome()
        }

        // Controls row 2
        binding.audioSelect.setOnClickListener {
            showSelectAudioDeviceDialog(viewModel)
        }

        binding.add.setOnClickListener {
            showAddParticipantDialog(viewModel)
        }



    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launchWhenResumed {
            viewModel.error.collect {
                if (it != null) {
                    Toast.makeText(this@CallActivity, "Error: $it", Toast.LENGTH_LONG).show()
                    viewModel.dismissError()
                }
            }
        }

        lifecycleScope.launchWhenResumed {
            viewModel.dataReceived.collect {
                Toast.makeText(this@CallActivity, "Data received: $it", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun goToHome() {
        val i: Intent = Intent(this, MainActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(i)
        finish()
    }

    override fun onDestroy() {
        binding.audienceRow.adapter = null
        binding.speakerView.adapter = null
        super.onDestroy()
    }

    companion object {
        const val KEY_ARGS = "args"
    }

    @Parcelize
    data class BundleArgs(val url: String, val token: String) : Parcelable
}