package com.pedrobneto.easynfc.initiator

import android.nfc.tech.IsoDep
import app.cash.turbine.test
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

internal class NfcDataStreamTest {

    private val stream = NfcDataStream<String>()
    private val isoDep = mockk<IsoDep>()

    private val CoroutineScope.connection
        get() = stream.connect(this, isoDep, {}, { "data" })

    @Before
    fun setUp() {
        unmockkAll()
        every { isoDep.isConnected } returns true
        every { isoDep.connect() } just Runs
        every { isoDep.close() } just Runs
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `when failing to connect, emit closed with error status`() = runTest {
        every { isoDep.connect() } throws Exception()
        connection.flow.test {
            assert(awaitItem().status == NfcDataStream.Status.CONNECTING)
            assert(awaitItem().status == NfcDataStream.Status.CLOSED_WITH_ERROR)
        }
    }

    @Test
    fun `when tag is not connected, emit closed with error status`() = runTest {
        every { isoDep.isConnected } returns false
        connection.flow.test {
            assert(awaitItem().status == NfcDataStream.Status.CONNECTING)
            assert(awaitItem().status == NfcDataStream.Status.CLOSED_WITH_ERROR)
        }
    }

    @Test
    fun `when successfully connecting, emit connected status and finish with closed status`() =
        runTest {
            connection.flow.test {
                assert(awaitItem().status == NfcDataStream.Status.CONNECTING)
                assert(awaitItem().status == NfcDataStream.Status.CONNECTED)
                assert(awaitItem().status == NfcDataStream.Status.CLOSED)
            }
        }

    @Test
    fun `when successfully connecting but select aid fails, emit connected status and finish with closed with error status`() =
        runTest {
            stream.connect(this, isoDep, { throw Exception() }, { "data" }).flow.test {
                assert(awaitItem().status == NfcDataStream.Status.CONNECTING)
                assert(awaitItem().status == NfcDataStream.Status.CONNECTED)
                assert(awaitItem().status == NfcDataStream.Status.CLOSED_WITH_ERROR)
            }
        }

    @Test
    fun `when successfully connecting and getting data, emit connected status and data`() =
        runTest {
            connection.flow.test {
                assert(awaitItem().status == NfcDataStream.Status.CONNECTING)
                assert(awaitItem().status == NfcDataStream.Status.CONNECTED)
                awaitItem().let {
                    assert(it.status == NfcDataStream.Status.CLOSED)
                    assert(it.data == "data")
                }
            }
        }

    @Test
    fun `when successfully connecting but failed getting data, emit connected status and finish with closed with error status`() =
        runTest {
            stream.connect(this, isoDep, {}, { throw Exception() }).flow.test {
                assert(awaitItem().status == NfcDataStream.Status.CONNECTING)
                assert(awaitItem().status == NfcDataStream.Status.CONNECTED)
                assert(awaitItem().status == NfcDataStream.Status.CLOSED_WITH_ERROR)
            }
        }
}
