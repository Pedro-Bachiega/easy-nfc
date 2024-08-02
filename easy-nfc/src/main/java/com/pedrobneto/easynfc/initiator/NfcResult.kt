package com.pedrobneto.easynfc.initiator

data class NfcResult<T>(
    val status: NfcStatus,
    val data: T? = null,
    val error: Throwable? = null
)
