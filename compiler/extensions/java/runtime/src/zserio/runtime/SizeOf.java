package zserio.runtime;

import zserio.runtime.array.PackingContextNode;

/**
 * Interface for a size calculation for classes generated by Zserio.
 */
public interface SizeOf
{
    /**
     * Initializes whole packing context subtree.
     *
     * @param contextNode Packing context node.
     */
    public void initPackingContext(PackingContextNode contextNode);

    /**
     * Gets the bit length of object stored in bit stream.
     *
     * It is supposed that the current bit stream position is zero. The bit size of object can depend on
     * current bit stream position because of alignment.
     *
     * @return Length of the object in number of bits.
     */
    public int bitSizeOf();

    /**
     * Gets the bit length of object stored in bit stream.
     *
     * @param bitPosition Current bit stream position.
     *
     * @return Length of the object in number of bits.
     */
    public int bitSizeOf(long bitPosition);

    /**
     * Gets the bit length of packed object stored in bit stream.
     *
     * @param contextNode Packing context node.
     * @param bitPosition Current bit stream position.
     *
     * @return Length of the packed object in number of bits.
     */
    public int bitSizeOf(PackingContextNode contextNode, long bitPosition);
}
