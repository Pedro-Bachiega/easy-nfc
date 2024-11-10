package com.pedrobneto.easynfc.initiator

import android.app.Activity
import android.content.Context
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

internal class NfcHelperTest {

    private val activity = mockk<Activity>()
    private val manager = mockk<NfcManager>()
    private val adapter = mockk<NfcAdapter>()

    private val helper = NfcHelper("0x01020304")

    private val onStartReadingListener = mockk<() -> Unit>()
    private val onStopReadingListener = mockk<() -> Unit>()
    private val onTagReadListener = mockk<NfcBridge.() -> Unit>()

    @Before
    fun setUp() {
        every { activity.getSystemService(Context.NFC_SERVICE) } returns manager
        every { manager.defaultAdapter } returns adapter
        every { adapter.isEnabled } returns true
        every { adapter.enableReaderMode(any(), any(), any(), any()) } just Runs
        every { adapter.disableReaderMode(any()) } just Runs

        every { onStartReadingListener.invoke() } just Runs
        every { onStopReadingListener.invoke() } just Runs
        every { onTagReadListener.invoke(any()) } just Runs
    }

    @Test
    fun `when startReading is called without a tagReadListener set, it should do nothing`() {
        helper.setOnStartReading(onStartReadingListener)
        helper.startReading(activity)
        verify(exactly = 0) { onStartReadingListener.invoke() }
    }

    @Test
    fun `when startReading is called with a tagReadListener set, onStartReadingListener should be called`() {
        helper.setOnTagReadListener(onTagReadListener)
        helper.setOnStartReading(onStartReadingListener)
        helper.startReading(activity)
        verify(exactly = 1) { onStartReadingListener.invoke() }
    }

    @Test
    fun `when startReading is called with a tagReadListener set, it should enable reader mode`() {
        helper.setOnTagReadListener(onTagReadListener)
        helper.startReading(activity)
        verify(exactly = 1) {
            adapter.enableReaderMode(
                activity,
                any(),
                NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                null
            )
        }
    }

    @Test
    fun `when startReading is called with a disabled adapter, it should not enable reader mode`() {
        every { adapter.isEnabled } returns false
        helper.setOnTagReadListener(onTagReadListener)
        helper.startReading(activity)
        verify(exactly = 0) { adapter.enableReaderMode(any(), any(), any(), any()) }
    }

    @Test
    fun `when stopReading is called, onStopReadingListener should be called`() {
        helper.setOnStopReading(onStopReadingListener)
        helper.stopReading(activity)
        verify(exactly = 1) { onStopReadingListener.invoke() }
    }

    @Test
    fun `when stopReading is called, it should disable reader mode`() {
        helper.setOnTagReadListener(onTagReadListener)
        helper.startReading(activity)
        helper.stopReading(activity)
        verify(exactly = 1) { adapter.disableReaderMode(activity) }
    }

    @Test
    fun `when registering a lifecycleOwner, onStart should call startReading`() {
        val activity = mockk<AppCompatActivity>()
        every { activity.lifecycle } returns mockk()

        helper.setOnTagReadListener(onTagReadListener)
        helper.setOnStartReading(onStartReadingListener)
        helper.onStart(activity)
        verify(exactly = 1) { onStartReadingListener.invoke() }
    }

    @Test
    fun `when registering a lifecycleOwner, onStop should call startReading`() {
        val activity = mockk<AppCompatActivity>()
        val fragment = mockk<Fragment>()
        every { fragment.activity } returns mockk()
        every { activity.lifecycle } returns mockk()

        helper.setOnTagReadListener(onTagReadListener)
        helper.setOnStopReading(onStopReadingListener)
        helper.onStart(fragment)
        helper.onStop(fragment)

        verify(exactly = 1) { onStopReadingListener.invoke() }
    }

    @Test
    fun `when registering a lifecycleOwner, onDestroy should remove observer`() {
        val activity = mockk<AppCompatActivity>()
        val lifecycle = mockk<Lifecycle>()
        every { activity.lifecycle } returns lifecycle
        every { lifecycle.removeObserver(any()) } just Runs

        helper.onDestroy(activity)

        verify(exactly = 1) { lifecycle.removeObserver(helper) }
    }

    @Test
    fun `when registering a lifecycleOwner, should add observer to it`() {
        val activity = mockk<AppCompatActivity>()
        val lifecycle = mockk<Lifecycle>()
        every { activity.lifecycle } returns lifecycle
        every { lifecycle.addObserver(any()) } just Runs

        helper.registerWithLifecycle(activity)

        verify(exactly = 1) { lifecycle.addObserver(helper) }
    }
}
