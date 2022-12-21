package com.klinechat.klinechatwebrtcexample.call


import android.os.Bundle
import android.os.Parcelable
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.ajalt.timberkt.Timber
import com.xwray.groupie.GroupieAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.parcelize.Parcelize

import com.klinechat.klinechatwebrtcexample.databinding.CallActivityBinding
import com.klinechat.klinechatwebrtcexample.showDebugMenuDialog
import com.klinechat.klinechatwebrtcexample.showSelectAudioDeviceDialog
import com.klinechat.klinechatwebrtcexample.viewModelByFactory

// import from SDK
import com.klinechat.webrtcsdk.CallViewModel
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.collect


class CallActivity : AppCompatActivity() {


    // init sdk view model
    val viewModel: CallViewModel by viewModelByFactory {
        val args = intent.getParcelableExtra<BundleArgs>(KEY_ARGS)
            ?: throw NullPointerException("args is null!")

        CallViewModel(args.url, args.token, application)
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


            // ROOM EVENTS EXAMPLE
            viewModel.room.events.collect {
                when (it) {
                    is RoomEvent.FailedToConnect -> {
                        Timber.e { "Room event: failed to connect" }
                    }
                    is RoomEvent.Disconnected -> {
                        Timber.e { "Room event: disconnected" }
                    }
                    is RoomEvent.ParticipantConnected -> {
                        Timber.e { "Room event: new participant connected" }
                    }
                    is RoomEvent.TrackMuted -> {
                     //   it.participant.identity
                        Timber.e { "Room event: someone is muted" }
                    }
                    is RoomEvent.ActiveSpeakersChanged -> {
                        Timber.e { "Room event: active speaker changed" }
                    }
                    is RoomEvent.RoomMetadataChanged -> {
                        Timber.e { "Room event: room metada changed" }
                    }
                    else -> {
                        Timber.e { "Room event: $it" }
                    }
                }
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
                if (enabled) com.klinechat.webrtcsdk.R.drawable.outline_videocam_24
                else com.klinechat.webrtcsdk.R.drawable.outline_videocam_off_24
            )
            binding.flipCamera.isEnabled = enabled
        }

        viewModel.micEnabled.observe(this) { enabled ->
            binding.mic.setOnClickListener { viewModel.setMicEnabled(!enabled) }
            binding.mic.setImageResource(
                if (enabled) com.klinechat.webrtcsdk.R.drawable.outline_mic_24
                else com.klinechat.webrtcsdk.R.drawable.outline_mic_off_24
            )
        }

        binding.flipCamera.setOnClickListener {
            viewModel.flipCamera()

        }

        binding.exit.setOnClickListener {
            finish()
        }

        // Controls row 2
        binding.audioSelect.setOnClickListener {
            showSelectAudioDeviceDialog(viewModel)
        }

        binding.debugMenu.setOnClickListener {
            showDebugMenuDialog(viewModel)
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