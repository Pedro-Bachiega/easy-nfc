package com.pedrobneto.sample.easynfc.initiator

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.pedrobneto.sample.easynfc.R

internal class ReceiverActivity : AppCompatActivity(R.layout.activity_receiver) {

    private val labelNfcData: TextView by lazy { findViewById(R.id.label_nfc_data) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateText(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        updateText(intent)
    }

    private fun updateText(intent: Intent?) {
        intent?.getStringExtra(EXTRA_NFC_DATA)?.let { content ->
            labelNfcData.text = "NFC new content: $content"
        }
    }

    companion object {
        const val EXTRA_NFC_DATA = "EXTRA_NFC_DATA"
    }
}
