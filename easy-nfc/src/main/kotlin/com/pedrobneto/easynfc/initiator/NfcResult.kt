package com.pedrobneto.easynfc.initiator

data class NfcResult<T>(
    val status: NfcDataStream.Status,
    val data: T? = null,
    val error: Throwable? = null
)
