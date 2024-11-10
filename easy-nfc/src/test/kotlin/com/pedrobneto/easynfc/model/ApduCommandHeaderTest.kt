package com.pedrobneto.easynfc.model

import org.junit.Assert.assertEquals
import org.junit.Test

internal class ApduCommandHeaderTest {

    @Test
    fun `when creating from string, should create header correctly`() {
        val header = ApduCommandHeader.from("00A40400")
        assertEquals(0x00.toByte(), header.clazz)
        assertEquals(0xA4.toByte(), header.instruction)
        assertEquals(0x04.toByte(), header.parameter1)
        assertEquals(0x00.toByte(), header.parameter2)
    }

    @Test
    fun `when creating from byte array, should create header correctly`() {
        val header = ApduCommandHeader.from(
            byteArrayOf(
                0x00.toByte(),
                0xA4.toByte(),
                0x04.toByte(),
                0x00.toByte()
            )
        )
        assertEquals(0x00.toByte(), header.clazz)
        assertEquals(0xA4.toByte(), header.instruction)
        assertEquals(0x04.toByte(), header.parameter1)
        assertEquals(0x00.toByte(), header.parameter2)
    }

    @Test
    fun `selectAid command header should have correct bytes`() {
        val header = ApduCommandHeader.selectAid
        assertEquals(0x00.toByte(), header.clazz)
        assertEquals(0xA4.toByte(), header.instruction)
        assertEquals(0x04.toByte(), header.parameter1)
        assertEquals(0x00.toByte(), header.parameter2)
    }

    @Test
    fun `updateBinary command header should have correct bytes`() {
        val header = ApduCommandHeader.updateBinary
        assertEquals(0x00.toByte(), header.clazz)
        assertEquals(0xD6.toByte(), header.instruction)
        assertEquals(0x00.toByte(), header.parameter1)
        assertEquals(0x00.toByte(), header.parameter2)
    }

    @Test
    fun `writeBinary command header should have correct bytes`() {
        val header = ApduCommandHeader.writeBinary
        assertEquals(0x00.toByte(), header.clazz)
        assertEquals(0xD0.toByte(), header.instruction)
        assertEquals(0x00.toByte(), header.parameter1)
        assertEquals(0x00.toByte(), header.parameter2)
    }
}
