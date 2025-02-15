import unittest
import os
import zserio

from testutils import getZserioApi, getApiDir

class BitfieldEnumTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.api = getZserioApi(__file__, "enumeration_types.zs").bitfield_enum

    def testValues(self):
        self.assertEqual(NONE_VALUE, self.api.Color.NONE.value)
        self.assertEqual(RED_VALUE, self.api.Color.RED.value)
        self.assertEqual(BLUE_VALUE, self.api.Color.BLUE.value)
        self.assertEqual(GREEN_VALUE, self.api.Color.GREEN.value)

    def testFromReader(self):
        writer = zserio.BitStreamWriter()
        writer.write_bits(self.api.Color.GREEN.value, COLOR_BITSIZEOF)
        reader = zserio.BitStreamReader(writer.byte_array, writer.bitposition)
        color = self.api.Color.from_reader(reader)
        self.assertEqual(GREEN_VALUE, color.value)

    def testBitSizeOf(self):
        self.assertEqual(COLOR_BITSIZEOF, self.api.Color.NONE.bitsizeof())
        self.assertEqual(COLOR_BITSIZEOF, self.api.Color.RED.bitsizeof())
        self.assertEqual(COLOR_BITSIZEOF, self.api.Color.BLUE.bitsizeof())
        self.assertEqual(COLOR_BITSIZEOF, self.api.Color.GREEN.bitsizeof())

    def testInitializeOffsets(self):
        self.assertEqual(COLOR_BITSIZEOF, self.api.Color.NONE.initialize_offsets(0))
        self.assertEqual(COLOR_BITSIZEOF + 1, self.api.Color.RED.initialize_offsets(1))
        self.assertEqual(COLOR_BITSIZEOF + 2, self.api.Color.BLUE.initialize_offsets(2))
        self.assertEqual(COLOR_BITSIZEOF + 3, self.api.Color.GREEN.initialize_offsets(3))

    def testWriteRead(self):
        writer = zserio.BitStreamWriter()
        self.api.Color.RED.write(writer)
        reader = zserio.BitStreamReader(writer.byte_array, writer.bitposition)
        self.assertEqual(RED_VALUE, reader.read_bits(COLOR_BITSIZEOF))

    def testWriteReadFile(self):
        color = self.api.Color.GREEN
        zserio.serialize_to_file(color, BLOB_NAME)

        readColor = zserio.deserialize_from_file(self.api.Color, BLOB_NAME)
        self.assertEqual(color, readColor)


BLOB_NAME = os.path.join(getApiDir(os.path.dirname(__file__)), "bitfield_enum.blob")

COLOR_BITSIZEOF = 3

NONE_VALUE = 0
RED_VALUE = 2
BLUE_VALUE = 3
GREEN_VALUE = 7
