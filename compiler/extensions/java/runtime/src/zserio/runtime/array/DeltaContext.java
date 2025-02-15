package zserio.runtime.array;

import java.io.IOException;
import java.math.BigInteger;

import zserio.runtime.array.ArrayElement.IntegralArrayElement;
import zserio.runtime.array.ArrayTraits.IntegralArrayTraits;
import zserio.runtime.io.BitStreamReader;
import zserio.runtime.io.BitStreamWriter;

/**
 * Context for delta packing created for each packable field.
 *
 * Contexts are always newly created for each array operation (bitSizeOfPacked, initializeOffsetsPacked,
 * readPacked, writePacked). They must be initialized at first via calling the init method for each packable
 * element present in the array. After the full initialization, only a single method (bitSizeOf, read, write)
 * can be repeatedly called for exactly the same sequence of packable elements.
 *
 Note that bitSizeOfDescriptor, writeDescriptor and readDescriptor methods finish the context initialization
 * and must be called before bitSizeOf, write and read operations respectively. Finishing of the context
 * initialization is made in the bitSizeOfDescriptor and writeDescriptor methods to safe one iteration in
 * the Array wrapper which would be otherwise needed to do the job.
 */
public class DeltaContext
{
    /**
     * Calls the initialization step for a single element.
     *
     * @param arrayTraits Standard array traits.
     * @param element Current element.
     */
    public void init(IntegralArrayTraits arrayTraits, IntegralArrayElement element)
    {
        numElements++;
        unpackedBitSize += arrayTraits.bitSizeOf(element);

        if (previousElement == null)
        {
            previousElement = element.toBigInteger();
            firstElementBitSize = unpackedBitSize;
        }
        else
        {
            if (maxBitNumber <= MAX_BIT_NUMBER_LIMIT)
            {
                isPacked = true;
                final BigInteger bigElement = element.toBigInteger();
                final BigInteger delta = bigElement.subtract(previousElement);
                final byte maxBitNumber = bitLength(delta);
                if (maxBitNumber > this.maxBitNumber)
                {
                    this.maxBitNumber = maxBitNumber;
                    if (maxBitNumber > MAX_BIT_NUMBER_LIMIT)
                        isPacked = false;
                }
                previousElement = bigElement;
            }
        }
    }

    /**
     * Returns length of the descriptor stored in the bit stream in bits.
     *
     * @return Length of the descriptor stored in the bit stream in bits.
     */
    public int bitSizeOfDescriptor()
    {
        finishInit(); // called from here for better performance

        return isPacked ? 1 + MAX_BIT_NUMBER_BITS : 1;
    }

    /**
     * Returns length of the packed element stored in the bit stream in bits.
     *
     * @param arrayTraits Standard array traits.
     * @param element Value of the current element.
     *
     * @return Length of the packed element stored in the bit stream in bits.
     */
    public int bitSizeOf(IntegralArrayTraits arrayTraits, IntegralArrayElement element)
    {
        if (!processingStarted || !isPacked)
        {
            processingStarted = true;
            return arrayTraits.bitSizeOf(element);
        }
        else
        {
            return maxBitNumber + (maxBitNumber > 0 ? 1 : 0);
        }
    }

    /**
     * Reads the delta packing descriptor from the bit stream. Called for all contexts before the first element
     * is read.
     *
     * @param reader Bit stream reader.
     *
     * @throws IOException Failure during bit stream manipulation.
     */
    public void readDescriptor(BitStreamReader reader) throws IOException
    {
        isPacked = reader.readBool();
        if (isPacked)
            maxBitNumber = (byte)reader.readBits(MAX_BIT_NUMBER_BITS);
    }

    /**
     * Reads a packed element from the bit stream.
     *
     * @param arrayTraits Standard array traits.
     * @param reader Bit stream reader.
     *
     * @return Packed element.
     *
     * @throws IOException Failure during bit stream manipulation.
     */
    public IntegralArrayElement read(IntegralArrayTraits arrayTraits, BitStreamReader reader) throws IOException
    {
        if (!processingStarted || !isPacked)
        {
            processingStarted = true;
            final IntegralArrayElement element = arrayTraits.read(reader);
            previousElement = element.toBigInteger();
            return element;
        }
        else
        {
            if (maxBitNumber > 0)
            {
                final BigInteger delta = BigInteger.valueOf(reader.readSignedBits(maxBitNumber + 1));
                previousElement = previousElement.add(delta);
            }

            return arrayTraits.fromBigInteger(previousElement);
        }
    }

    /**
     * Writes the delta packing descriptor to the bit stream. Called for all contexts before the first element
     * is written.
     *
     * @param writer Bit stream writer.
     *
     * @throws IOException Failure during bit stream manipulation.
     */
    public void writeDescriptor(BitStreamWriter writer) throws IOException
    {
        finishInit(); // called from here for better performance

        writer.writeBool(isPacked);
        if (isPacked)
            writer.writeBits(maxBitNumber, MAX_BIT_NUMBER_BITS);
    }

    /**
     * Writes the packed element to the bit stream.
     *
     * @param arrayTraits Standard array traits.
     * @param writer Bit stream writer.
     * @param element Current element.
     *
     * @throws IOException Failure during bit stream manipulation.
     */
    public void write(IntegralArrayTraits arrayTraits, BitStreamWriter writer, IntegralArrayElement element)
            throws IOException
    {
        if (!processingStarted || !isPacked)
        {
            processingStarted = true;
            previousElement = element.toBigInteger();
            arrayTraits.write(writer,  element);
        }
        else
        {
            if (maxBitNumber > 0)
            {
                final BigInteger bigElement = element.toBigInteger();
                final BigInteger delta = bigElement.subtract(previousElement);
                writer.writeSignedBits(delta.longValue(), maxBitNumber + 1);
                previousElement = bigElement;
            }
        }
    }

    private void finishInit()
    {
        if (isPacked)
        {
            final int deltaBitSize = maxBitNumber + (maxBitNumber > 0 ? 1 : 0);
            final int packedBitSizeWithDescriptor = 1 + MAX_BIT_NUMBER_BITS + // descriptor
                    firstElementBitSize + (numElements - 1) * deltaBitSize;
            final int unpackedBitSizeWithDescriptor = 1 + unpackedBitSize;
            if (packedBitSizeWithDescriptor >= unpackedBitSizeWithDescriptor)
                isPacked = false;
        }
    }

    private static byte bitLength(BigInteger element)
    {
        // need to call abs() first to get the same behavior as in Python and C++
        return (byte)element.abs().bitLength();
    }

    private static final byte MAX_BIT_NUMBER_BITS = 6;
    private static final byte MAX_BIT_NUMBER_LIMIT = 62;

    private boolean isPacked = false;
    private boolean processingStarted = false;
    private byte maxBitNumber = 0;
    private BigInteger previousElement; // BigInteger covers all integral array element values

    private int unpackedBitSize = 0;
    private int firstElementBitSize = 0;
    private int numElements = 0;
}
