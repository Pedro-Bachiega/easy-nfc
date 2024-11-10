package com.pedrobneto.easynfc

import org.junit.Test

@Suppress("ClassName")
internal class _extensionsKtTest {

    @Test
    fun `when calling asByteArray, should return correct byte array`() {
        val hexString = "0xABCDEF"
        val byteArray = hexString.asByteArray
        assert(byteArray.contentEquals(byteArrayOf(0xAB.toByte(), 0xCD.toByte(), 0xEF.toByte())))
    }

    @Test
    fun `when calling asByteArray with incorrect length, should return correct byte array`() {
        val hexString = "0xABCDEFG"
        val byteArray = hexString.asByteArray
        assert(byteArray.contentEquals(byteArrayOf(0xAB.toByte(), 0xCD.toByte(), 0xEF.toByte())))
    }

    @Test
    fun `when calling asInt, should return correct int`() {
        assert(byteArrayOf(0x10.toByte()).asInt == 16)
        assert(byteArrayOf(0x00, 0x10.toByte()).asInt == 16)
        assert(byteArrayOf(0x00, 0x00, 0x00, 0x10.toByte()).asInt == 16)
    }

    @Test(expected = IllegalStateException::class)
    fun `when calling asInt with empty array, should throw exception`() {
        byteArrayOf().asInt
    }

    @Test
    fun `when calling toByteArray, should return correct byte array`() {
        val int = 16
        val byteArray = int.toByteArray(1)
        assert(byteArray.contentEquals(byteArrayOf(0x10.toByte())))
    }

    @Test
    fun `when calling toByteArray with custom size, should pad correctly`() {
        val int = 16
        val byteArray = int.toByteArray(4)
        assert(byteArray.contentEquals(byteArrayOf(0x00, 0x00, 0x00, 0x10.toByte())))
    }

    @Test(expected = IllegalStateException::class)
    fun `when calling toByteArray with invalid size, should throw exception`() {
        val input = 16
        input.toByteArray(0)
    }

    @Test
    fun `when transforming int to byte array and back to int, should return same value`() {
        assert(10.toByteArray(1).asInt == 10)
        assert(360.toByteArray(2).asInt == 360)
        assert(25000.toByteArray(3).asInt == 25000)
    }
}
