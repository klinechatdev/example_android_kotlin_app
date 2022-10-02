package com.naylinaung.androidretrofit.dialog


import android.R
import android.app.Activity
import android.app.AlertDialog
import android.widget.ArrayAdapter
import com.naylinaung.androidretrofit.CallViewModel


fun Activity.showAddParticipantDialog(callViewModel: CallViewModel) {
    val builder = with(AlertDialog.Builder(this)) {
        setTitle("Add a Friend to Call")

        val audioDevices = arrayListOf<String>()
        val arrayAdapter = ArrayAdapter<String>(this@showAddParticipantDialog, R.layout.select_dialog_item)
       // arrayAdapter.addAll(audioDevices.map { it.name })

        setAdapter(arrayAdapter) { dialog, index ->
            //TODO
            dialog.dismiss()
        }
    }
    builder.show()
}