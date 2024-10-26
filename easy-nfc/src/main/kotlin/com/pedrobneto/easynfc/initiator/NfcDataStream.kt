package com.pedrobneto.easynfc.initiator

import android.nfc.tech.IsoDep
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

private const val LOG_TAG = "NfcDataStream"

/**
 * A class responsible for giving you results based on your connection and command execution
 *
 * @see NfcResult
 */
class NfcDataStream<T> internal constructor(initialValue: NfcResult<T>) {

    private val _flow = MutableStateFlow(initialValue)
    val flow: Flow<NfcResult<T>> get() = _flow
    val liveData: LiveData<NfcResult<T>> get() = _flow.asLiveData()

    private fun emit(value: NfcResult<T>) {
        if (!_flow.tryEmit(value)) error("Could not emit value")
    }

    internal fun connect(
        scope: CoroutineScope,
        isoDep: IsoDep,
        selectAid: () -> Unit,
        func: (tag: IsoDep) -> T
    ) = apply {
        scope.launch {
            runCatching {
                isoDep.connect()

                if (isoDep.isConnected) {
                    Log.d(LOG_TAG, "Connected")
                    emit(NfcResult(status = Status.CONNECTED))
                } else {
                    error("Could not connect")
                }

                selectAid()

                val data = func(isoDep)
                isoDep.close()
                Log.d(LOG_TAG, "Closed")
                emit(NfcResult(status = Status.CLOSED, data = data))
            }.onFailure {
                isoDep.close()
                Log.e(LOG_TAG, "Closed with error: ${it.message}", it)
                emit(NfcResult(status = Status.CLOSED_WITH_ERROR, error = it))
            }
        }
    }

    enum class Status {
        CONNECTED, CONNECTING, CLOSED, CLOSED_WITH_ERROR
    }
}