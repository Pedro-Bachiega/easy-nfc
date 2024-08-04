package com.pedrobneto.easynfc.initiator

data class NfcResult<T>(
    val status: DataStreamStatus,
    val data: T? = null,
    val error: Throwable? = null
)
