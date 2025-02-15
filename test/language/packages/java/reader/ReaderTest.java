package reader;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import zserio.runtime.ZserioError;
import zserio.runtime.io.ByteArrayBitStreamWriter;
import zserio.runtime.io.ByteArrayBitStreamReader;

public class ReaderTest
{
    @Test
    public void readWrite() throws Exception
    {
        final long[] indexes = new long[ARRAY_SIZE];
        final Element[] array = new Element[ARRAY_SIZE];
        final long[] indexesForParameterized = new long[ARRAY_SIZE];
        final ParameterizedElement[] parameterizedArray = new ParameterizedElement[ARRAY_SIZE];
        for (int i = 0; i < ARRAY_SIZE; ++i)
        {
            array[i] = new Element(i);
            parameterizedArray[i] = new ParameterizedElement(i, i);
        }
        final reader.Test test = new reader.Test(indexes, array, indexesForParameterized, parameterizedArray);

        final ByteArrayBitStreamWriter writer = new ByteArrayBitStreamWriter();
        test.write(writer);

        final ByteArrayBitStreamReader reader = new ByteArrayBitStreamReader(writer.toByteArray());
        final reader.Test readTest = new reader.Test(reader);

        assertEquals(ARRAY_SIZE, readTest.getArray().length);
        assertEquals(ARRAY_SIZE, readTest.getParameterizedArray().length);
        assertEquals(test, readTest);
    }

    private static final int ARRAY_SIZE = 10;
}
