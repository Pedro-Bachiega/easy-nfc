package com.pedrobneto.sample.easynfc.initiator

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pedrobneto.easynfc.initiator.NfcBridge
import com.pedrobneto.easynfc.initiator.NfcDataStream
import com.pedrobneto.easynfc.initiator.NfcHelper
import com.pedrobneto.easynfc.model.ApduCommand
import com.pedrobneto.easynfc.model.ApduCommandHeader
import com.pedrobneto.sample.easynfc.R
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val labelNfcStatus: TextView by lazy { findViewById(R.id.label_nfc_status) }
    private val labelNfcLiveDataStatus: TextView by lazy { findViewById(R.id.label_nfc_live_data_status) }
    private val labelNfcFlowStatus: TextView by lazy { findViewById(R.id.label_nfc_flow_status) }

    private var nfcHelper = NfcHelper("F0394148148100")

    private val statusList = mutableSetOf<NfcDataStream.Status>()

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
        val mockPixQRCode =
            "00020101021126360014br.gov.bcb.pix0114+55119484444945204000053039865802BR5919PEDRO BACHIEGA NETO6009SAO PAULO622905251J8N7326A92QEA2C91T65TNFZ6304B630"
        nfcBridge.sendData(
            command = ApduCommand(header = ApduCommandHeader.writeBinary, content = mockPixQRCode)
        ).run {
            liveData.observe(this@MainActivity) {
                statusList += it.status
                labelNfcLiveDataStatus.text =
                    "LiveData NFC tag status: ${statusList.joinToString("\n")}"
            }

            flow.onEach {
                statusList += it.status
                labelNfcFlowStatus.text =
                    "Flow NFC tag status: ${statusList.joinToString("\n")}"
            }.launchIn(lifecycleScope)
        }
    }
}
