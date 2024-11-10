package com.pedrobneto.easynfc.model

import android.nfc.tech.IsoDep
import com.pedrobneto.easynfc.asByteArray
import com.pedrobneto.easynfc.toByteArray
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

internal class ApduCommandTest {

    // region Getters
    @Test
    fun `when getDataSize is called, should return the correct data size`() {
        val data = "Testing data size".encodeToByteArray()
        val command = ApduCommand(header = ApduCommandHeader.writeBinary, content = data)
        assert(command.getDataSize(dataLengthByteQuantity = 1) == data.size)
    }

    @Test
    fun `when getDataSize is called with a custom data length byte quantity, should return the correct data size`() {
        val data = "Testing data size with custom data length field byte size".encodeToByteArray()
        val command = ApduCommand(
            header = ApduCommandHeader.writeBinary,
            content = data,
            contentSizeByteLength = 3
        )
        assert(command.getDataSize(dataLengthByteQuantity = 3) == data.size)
    }

    @Test
    fun `when getData is called, should return the correct data`() {
        val data = "Testing data".encodeToByteArray()
        val command = ApduCommand(header = ApduCommandHeader.writeBinary, content = data)
        assert(command.getData(dataLengthByteQuantity = 1).contentEquals(data))
    }

    @Test
    fun `when getData is called with a custom data length byte quantity, should return the correct data`() {
        val data = "Testing data with custom data length field byte size".encodeToByteArray()
        val command = ApduCommand(
            header = ApduCommandHeader.writeBinary,
            content = data,
            contentSizeByteLength = 3
        )
        assert(command.getData(dataLengthByteQuantity = 3).contentEquals(data))
    }

    @Test
    fun `when getDataAsString is called, should return the correct data as a string`() {
        val data = "Testing data as string"
        val command = ApduCommand(header = ApduCommandHeader.writeBinary, content = data)
        assert(command.getDataAsString(dataLengthByteQuantity = 1) == "Testing data as string")
    }

    @Test
    fun `when getDataAsString is called with a custom data length byte quantity, should return the correct data as a string`() {
        val data = "Testing data as string with custom data length field byte size"
        val command = ApduCommand(
            header = ApduCommandHeader.writeBinary,
            content = data,
            contentSizeByteLength = 3
        )
        assert(command.getDataAsString(dataLengthByteQuantity = 3) == "Testing data as string with custom data length field byte size")
    }
    // endregion Getters

    // region Constructors
    @Test
    fun `when creating command manually, should create command correctly`() {
        val data = "Testing manual creation".encodeToByteArray()
        val command = ApduCommand(
            clazz = 0x00.toByte(),
            instruction = 0x01.toByte(),
            parameter1 = 0x02.toByte(),
            parameter2 = 0x03.toByte(),
            fullContent = data.size.toByteArray(3) + data
        )

        val header = command.getHeader()
        assert(header.clazz == 0x00.toByte())
        assert(header.instruction == 0x01.toByte())
        assert(header.parameter1 == 0x02.toByte())
        assert(header.parameter2 == 0x03.toByte())
        assert(command.getData(3).contentEquals(data))
    }

    @Test
    fun `when creating content manually, should create command correctly`() {
        val data = "Testing manual creation".encodeToByteArray()
        val command = ApduCommand(
            header = ApduCommandHeader.writeBinary,
            fullContent = data.size.toByteArray(1) + data
        )
        assert(command.getData(1).contentEquals(data))
    }

    @Test
    fun `when passing header manually, should create command correctly`() {
        val command = ApduCommand(
            clazz = 0x00.toByte(),
            instruction = 0x01.toByte(),
            parameter1 = 0x02.toByte(),
            parameter2 = 0x03.toByte(),
            content = "Testing manual header"
        )
        val header = command.getHeader()
        assert(header.clazz == 0x00.toByte())
        assert(header.instruction == 0x01.toByte())
        assert(header.parameter1 == 0x02.toByte())
        assert(header.parameter2 == 0x03.toByte())
    }

    @Test
    fun `when creating from string, should create command correctly`() {
        val command = ApduCommand(
            header = ApduCommandHeader.updateBinary,
            content = "Testing string parse"
        )
        assert(command.getDataAsString() == "Testing string parse")
    }

    @Test
    fun `when passing header as a hex string, should create command correctly`() {
        val command = ApduCommand(
            headerHexString = "00010203",
            content = "Testing manual header"
        )
        val header = command.getHeader()
        assert(header.clazz == 0x00.toByte())
        assert(header.instruction == 0x01.toByte())
        assert(header.parameter1 == 0x02.toByte())
        assert(header.parameter2 == 0x03.toByte())
    }
    // endregion Constructors

    // region Execution
    @Test
    fun `when calling executeOnTag, should send full content to tag`() {
        val successResult = byteArrayOf(0x90.toByte(), 0x00.toByte())
        val isoDepTag = mockk<IsoDep>()
        every { isoDepTag.transceive(any()) } returns successResult

        val command = ApduCommand(
            header = ApduCommandHeader.writeBinary,
            content = "Testing execute on tag"
        )
        val result = command.executeOnTag(isoDepTag)
        assert(result.contentEquals(successResult))
        verify(exactly = 1) { isoDepTag.transceive(command.content) }
    }
    // endregion Execution

    // region Extension
    @Test
    fun `when calling SelectAidCommand, should return a working select aid command`() {
        val aid = "0x000102030405"
        val command = SelectAidCommand(aid = aid)
        val header = command.getHeader()

        assert(header == ApduCommandHeader.selectAid)
        assert(command.getData().contentEquals(aid.asByteArray))
    }

    @Test
    fun `when calling plus operator, should return a working combined command`() {
        val firstCommand = ApduCommand(
            header = ApduCommandHeader.writeBinary,
            content = "Test command"
        )

        val secondCommand = ApduCommand(
            header = ApduCommandHeader.updateBinary,
            content = " is complete"
        )

        val combinedCommand = firstCommand + secondCommand
        assert(combinedCommand.getHeader() == ApduCommandHeader.updateBinary)
        assert(combinedCommand.getDataAsString() == "Test command is complete")
    }

    @Test
    fun `when calling plus operator with data length byte quantity, should return a working combined command`() {
        val firstCommand = ApduCommand(
            header = ApduCommandHeader.writeBinary,
            content = "Test command",
            contentSizeByteLength = 3
        )

        val secondCommand = ApduCommand(
            header = ApduCommandHeader.updateBinary,
            content = " is complete",
            contentSizeByteLength = 3
        )

        val combinedCommand = firstCommand.plus(other = secondCommand, dataLengthByteQuantity = 3)
        assert(combinedCommand.getDataAsString(3) == "Test command is complete")
    }
    // endregion Extension
}
