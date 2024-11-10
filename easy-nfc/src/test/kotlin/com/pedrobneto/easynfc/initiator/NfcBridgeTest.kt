package com.pedrobneto.easynfc.initiator

import android.nfc.tech.IsoDep
import app.cash.turbine.test
import com.pedrobneto.easynfc.asByteArray
import com.pedrobneto.easynfc.model.ApduCommand
import com.pedrobneto.easynfc.model.ApduCommandHeader
import com.pedrobneto.easynfc.model.SelectAidCommand
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class NfcBridgeTest {

    private val aid = "0x01020304"
    private val isoDepTag = mockk<IsoDep>()

    private val mockCommand =
        ApduCommand(header = ApduCommandHeader.writeBinary, content = "Mock command")
    private val successResult = byteArrayOf(0x90.toByte(), 0x00.toByte())

    private val CoroutineScope.bridge get() = NfcBridge(aid, isoDepTag, this)

    @Before
    fun setUp() {
        every { isoDepTag.isConnected } returns true
        every { isoDepTag.connect() } just Runs
        every { isoDepTag.close() } just Runs
        every { isoDepTag.transceive(any()) } returns successResult
    }

    @Test
    fun `when sendCommandsTransforming is called, it should return a NfcDataStream`() = runTest {
        val stream = bridge.sendCommandsTransforming(
            listOf(SelectAidCommand(aid) to ByteArray::decodeToString)
        )

        stream.flow.test {
            assert(awaitItem().status == NfcDataStream.Status.CONNECTING)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when sendCommandsTransforming is called not accepting empty and the stream data is empty, should close with error`() =
        runTest {
            val stream = bridge.sendCommandsTransforming(
                commands = listOf(mockCommand to ByteArray::decodeToString),
                acceptEmpty = false
            )

            every { isoDepTag.transceive(mockCommand.content) } returns byteArrayOf()

            stream.flow.test {
                assert(awaitItem().status == NfcDataStream.Status.CONNECTING)
                assert(awaitItem().status == NfcDataStream.Status.CONNECTED)
                assert(awaitItem().status == NfcDataStream.Status.CLOSED_WITH_ERROR)
            }
        }

    @Test
    fun `when sendCommandsTransforming is called and select aid fails, should close with error`() =
        runTest {
            val stream =
                bridge.sendCommandsTransforming(listOf(mockCommand to ByteArray::decodeToString))

            every { isoDepTag.transceive(SelectAidCommand(aid).content) } returns byteArrayOf()

            stream.flow.test {
                assert(awaitItem().status == NfcDataStream.Status.CONNECTING)
                assert(awaitItem().status == NfcDataStream.Status.CONNECTED)
                assert(awaitItem().status == NfcDataStream.Status.CLOSED_WITH_ERROR)
            }

            verify(exactly = 0) { isoDepTag.transceive(mockCommand.content) }
        }

    @Test
    fun `when sendCommands is called, it should return a NfcDataStream`() = runTest {
        val stream = bridge.sendCommands(listOf(mockCommand))
        stream.flow.test {
            assert(awaitItem().status == NfcDataStream.Status.CONNECTING)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when sendData is called with a byteArray content, it should return a NfcDataStream`() = runTest {
        val stream = bridge.sendData(
            header = ApduCommandHeader.writeBinary,
            content = "Testing send data".encodeToByteArray()
        )
        stream.flow.test {
            assert(awaitItem().status == NfcDataStream.Status.CONNECTING)
            assert(awaitItem().status == NfcDataStream.Status.CONNECTED)
            assert(awaitItem().status == NfcDataStream.Status.CLOSED)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when sendData is called with a string content, it should return a NfcDataStream`() = runTest {
        val stream = bridge.sendData(
            header = ApduCommandHeader.writeBinary,
            content = "Testing send data",
            aid = aid.asByteArray,
            acceptEmpty = true
        )
        stream.flow.test {
            assert(awaitItem().status == NfcDataStream.Status.CONNECTING)
            assert(awaitItem().status == NfcDataStream.Status.CONNECTED)
            assert(awaitItem().status == NfcDataStream.Status.CLOSED)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when sendData is called with a command, it should return a NfcDataStream`() = runTest {
        val stream = bridge.sendData(
            command = ApduCommand(header = ApduCommandHeader.writeBinary, content = "Testing send data"),
            aid = aid.asByteArray,
            acceptEmpty = true
        )
        stream.flow.test {
            assert(awaitItem().status == NfcDataStream.Status.CONNECTING)
            assert(awaitItem().status == NfcDataStream.Status.CONNECTED)
            assert(awaitItem().status == NfcDataStream.Status.CLOSED)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
