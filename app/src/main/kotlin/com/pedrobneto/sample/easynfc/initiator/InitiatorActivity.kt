package com.pedrobneto.sample.easynfc.initiator

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pedrobneto.easynfc.initiator.NfcBridge
import com.pedrobneto.easynfc.initiator.NfcHelper
import com.pedrobneto.easynfc.model.ApduCommand
import com.pedrobneto.easynfc.model.ApduCommandHeader
import com.pedrobneto.sample.easynfc.R
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class InitiatorActivity : AppCompatActivity(R.layout.activity_initiator) {

    private val labelNfcStatus: TextView by lazy { findViewById(R.id.label_nfc_status) }
    private val labelNfcLiveDataStatus: TextView by lazy { findViewById(R.id.label_nfc_live_data_status) }
    private val labelNfcFlowStatus: TextView by lazy { findViewById(R.id.label_nfc_flow_status) }
    private val inputNfcContent: EditText by lazy { findViewById(R.id.input_content) }

    private var nfcHelper = NfcHelper("F0394148148100")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        nfcHelper.setOnTagReadListener(::onTagReadListener)
            .setOnStartReading { labelNfcStatus.text = "NFC Reader: Reading" }
            .setOnStopReading { labelNfcStatus.text = "NFC Reader: Stopped" }
    }

    override fun onStart() {
        super.onStart()
        nfcHelper.startReading(this)
    }

    override fun onStop() {
        nfcHelper.stopReading(this)
        super.onStop()
    }

    private fun onTagReadListener(nfcBridge: NfcBridge) {
        val content =
            inputNfcContent.text?.toString()?.takeIf(String::isNotBlank) ?: "Sent via NFC :)"
        val commands = content.split("\n").let { lines ->
            lines.mapIndexed { index, line ->
                val header =
                    if (index < lines.lastIndex) ApduCommandHeader.updateBinary
                    else ApduCommandHeader.writeBinary
                ApduCommand(header = header, content = line)
            }
        }

        nfcBridge.sendCommands(commands = commands).run {
            liveData.observe(this@InitiatorActivity) {
                labelNfcLiveDataStatus.text = "LiveData NFC tag status: ${it.status.name}"
            }

            flow.onEach { labelNfcFlowStatus.text = "Flow NFC tag status: ${it.status.name}" }
                .launchIn(lifecycleScope)
        }
    }
}
