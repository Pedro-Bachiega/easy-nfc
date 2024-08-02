package com.pedrobneto.easynfc.initiator

import android.app.Activity
import android.content.Context
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.nfc.Tag
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.pedrobneto.easynfc.asByteArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob

private const val LOG_TAG = "NfcAdapter"

/**
 * A class to help you easily transfer data between NFC enabled devices.
 *
 * @param aid The AID (application id) of the target NFC device.
 * @param lifecycleOwner The lifecycle owner to attach the reader to. If null, the reader will not be attached.
 */
class NfcHelper(
    private val aid: ByteArray,
    lifecycleOwner: LifecycleOwner? = null,
) : DefaultLifecycleObserver {

    private val job: Job = SupervisorJob()
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + job)

    private var nfcAdapter: NfcAdapter? = null
    private var onTagReadListener: (NfcBridge.() -> Unit)? = null
    private var onStartReadingListener: (() -> Unit)? = null
    private var onStopReadingListener: (() -> Unit)? = null

    private val LifecycleOwner.activity: Activity?
        get() = when (this) {
            is Activity -> this
            is Fragment -> activity
            else -> error("LifecycleOwner must be an Activity or a Fragment")
        }

    /**
     * A class to help you easily transfer data between NFC enabled devices.
     *
     * @param aid The AID (application id) of the target NFC device.
     * @param lifecycleOwner The lifecycle owner to attach the reader to. If null, the reader will not be attached.
     */
    constructor(aid: String, lifecycleOwner: LifecycleOwner? = null) : this(
        aid.asByteArray,
        lifecycleOwner,
    )

    init {
        lifecycleOwner?.lifecycle?.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        owner.activity?.run(::startReading)
        onStartReadingListener?.invoke()
    }

    override fun onStop(owner: LifecycleOwner) {
        onStopReadingListener?.invoke()
        owner.activity?.run(::stopReading)
        super.onStop(owner)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        owner.lifecycle.removeObserver(this)
        super.onDestroy(owner)
    }

    private fun onTagRead(tag: Tag) {
        runCatching { onTagReadListener?.invoke(NfcBridge(aid = aid, tag = tag, scope = scope)) }
    }

    /**
     * Starts the NFC reader.
     * Only call this if you want to force the reader to start at a specific time that the lifecycle doesn't cover.
     *
     * @param activity The activity the reader is attached to.
     */
    fun startReading(activity: Activity) {
        if (onTagReadListener == null) {
            Log.e(LOG_TAG, "No onTagReadListener set, skipping setup")
            return
        }

        runCatching {
            nfcAdapter = (activity.getSystemService(Context.NFC_SERVICE) as? NfcManager)
                ?.defaultAdapter

            if (nfcAdapter?.isEnabled == true) {
                nfcAdapter?.enableReaderMode(
                    activity,
                    ::onTagRead,
                    NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                    null
                )
            }
        }
    }

    /**
     * Stops the NFC reader.
     * Only call this if you want to force the reader to stop at a specific time that the lifecycle doesn't cover.
     *
     * @param activity The activity the reader is attached to.
     */
    fun stopReading(activity: Activity) {
        runCatching { nfcAdapter?.disableReaderMode(activity) }
        nfcAdapter = null
    }

    /**
     * Sets the listener that will be called when a tag is read.
     *
     * @param listener The listener to be called.
     */
    fun setOnTagReadListener(listener: (NfcBridge.() -> Unit)) = apply {
        this.onTagReadListener = listener
    }

    /**
     * Sets the listener that will be called when the reader starts reading.
     *
     * @param listener The listener to be called.
     */
    fun setOnStartReading(listener: () -> Unit) = apply {
        this.onStartReadingListener = listener
    }

    /**
     * Sets the listener that will be called when the reader stops reading.
     *
     * @param listener The listener to be called.
     */
    fun setOnStopReading(listener: () -> Unit) = apply {
        this.onStopReadingListener = listener
    }
}