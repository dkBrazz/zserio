package optional_members;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import javax.imageio.stream.FileImageInputStream;

import org.junit.Test;

import optional_members.optional_expression.BasicColor;
import optional_members.optional_expression.BlackColor;
import optional_members.optional_expression.Container;

public class OptionalExpressionTest
{
    @Test
    public void bitSizeOf()
    {
        final Container container = new Container();
        container.setBasicColor(BasicColor.WHITE);
        assertEquals(CONTAINER_BIT_SIZE_WITHOUT_OPTIONAL, container.bitSizeOf());

        container.setBasicColor(BasicColor.BLACK);
        container.setNumBlackTones(NUM_BLACK_TONES);
        container.setBlackColor(createBlackColor(NUM_BLACK_TONES));
        assertEquals(CONTAINER_BIT_SIZE_WITH_OPTIONAL, container.bitSizeOf());
    }

    @Test
    public void isNumBlackTonesUsed()
    {
        final Container container = new Container();
        container.setBasicColor(BasicColor.WHITE);
        assertFalse(container.isNumBlackTonesUsed());

        container.setBasicColor(BasicColor.BLACK);
        container.setNumBlackTones(NUM_BLACK_TONES);
        assertTrue(container.isNumBlackTonesUsed());
        assertEquals(NUM_BLACK_TONES, (short)container.getNumBlackTones());
    }

    @Test
    public void isBlackColorUsed()
    {
        final Container container = new Container();
        container.setBasicColor(BasicColor.WHITE);
        assertFalse(container.isBlackColorUsed());

        container.setBasicColor(BasicColor.BLACK);
        final BlackColor blackColor = createBlackColor(NUM_BLACK_TONES);
        container.setBlackColor(blackColor);
        assertTrue(container.isBlackColorUsed());
        assertTrue(blackColor.equals(container.getBlackColor()));
    }

    @Test
    public void equals()
    {
        final Container container1 = new Container();
        final Container container2 = new Container();
        assertTrue(container1.equals(container2));

        container1.setBasicColor(BasicColor.WHITE);
        container2.setBasicColor(BasicColor.BLACK);
        container2.setNumBlackTones(NUM_BLACK_TONES);
        container2.setBlackColor(createBlackColor(NUM_BLACK_TONES));
        assertFalse(container1.equals(container2));

        container2.setBasicColor(BasicColor.WHITE);
        container2.setNumBlackTones(null);
        container2.setBlackColor(null);
        assertTrue(container1.equals(container2));
    }

    @Test
    public void hashCodeMethod()
    {
        final Container container1 = new Container();
        final Container container2 = new Container();
        assertEquals(container1.hashCode(), container2.hashCode());

        container1.setBasicColor(BasicColor.WHITE);
        container2.setBasicColor(BasicColor.BLACK);
        container2.setNumBlackTones(NUM_BLACK_TONES);
        container2.setBlackColor(createBlackColor(NUM_BLACK_TONES));
        assertTrue(container1.hashCode() != container2.hashCode());

        container2.setBasicColor(BasicColor.WHITE);
        container2.setNumBlackTones(null);
        container2.setBlackColor(null);
        assertEquals(container1.hashCode(), container2.hashCode());
    }

    @Test
    public void initializeOffsets()
    {
        final Container container = new Container();
        container.setBasicColor(BasicColor.WHITE);
        final int bitPosition = 1;
        assertEquals(bitPosition + CONTAINER_BIT_SIZE_WITHOUT_OPTIONAL, container.initializeOffsets(bitPosition));

        container.setBasicColor(BasicColor.BLACK);
        container.setNumBlackTones(NUM_BLACK_TONES);
        container.setBlackColor(createBlackColor(NUM_BLACK_TONES));
        assertEquals(bitPosition + CONTAINER_BIT_SIZE_WITH_OPTIONAL, container.initializeOffsets(bitPosition));
    }

    @Test
    public void fileWrite() throws IOException
    {
        final Container container = new Container();
        container.setBasicColor(BasicColor.WHITE);
        final File whiteContainerFile = new File("white.bin");
        container.write(whiteContainerFile);
        checkContainerInFile(whiteContainerFile, BasicColor.WHITE, NUM_BLACK_TONES);
        Container readContainer = new Container(whiteContainerFile);
        assertEquals(BasicColor.WHITE, readContainer.getBasicColor());
        assertFalse(readContainer.isNumBlackTonesUsed());
        assertFalse(readContainer.isBlackColorUsed());

        container.setBasicColor(BasicColor.BLACK);
        container.setNumBlackTones(NUM_BLACK_TONES);
        final BlackColor blackColor = createBlackColor(NUM_BLACK_TONES);
        container.setBlackColor(blackColor);
        final File blackContainerFile = new File("black.bin");
        container.write(blackContainerFile);
        checkContainerInFile(blackContainerFile, BasicColor.BLACK, NUM_BLACK_TONES);
        readContainer = new Container(blackContainerFile);
        assertEquals(BasicColor.BLACK, readContainer.getBasicColor());
        assertEquals(NUM_BLACK_TONES, (short)readContainer.getNumBlackTones());
        assertTrue(blackColor.equals(readContainer.getBlackColor()));
        assertTrue(readContainer.isNumBlackTonesUsed());
        assertTrue(readContainer.isBlackColorUsed());
    }

    private static BlackColor createBlackColor(short numBlackTones)
    {
        final BlackColor blackColor = new BlackColor(numBlackTones);

        final int tones[] = new int[numBlackTones];
        for (short i = 0; i < numBlackTones; ++i)
            tones[i] = i + 1;
        blackColor.setTones(tones);

        return blackColor;
    }

    private static void checkContainerInFile(File file, BasicColor basicColor, short numBlackTones)
            throws IOException
    {
        final FileImageInputStream stream = new FileImageInputStream(file);

        if (basicColor == BasicColor.WHITE)
        {
            assertEquals(1, stream.length());
            assertEquals(basicColor.getValue(), stream.readByte());
        }
        else
        {
            assertEquals(1 + 1 + 4 * numBlackTones, stream.length());
            assertEquals(basicColor.getValue(), stream.readByte());
            assertEquals(numBlackTones, stream.readByte());
            for (short i = 0; i < numBlackTones; ++i)
                assertEquals(i + 1, stream.readInt());
        }

        stream.close();
    }

    private static short NUM_BLACK_TONES = 2;

    private static int CONTAINER_BIT_SIZE_WITHOUT_OPTIONAL = 8;
    private static int CONTAINER_BIT_SIZE_WITH_OPTIONAL = 8 + 8 + 32 * NUM_BLACK_TONES;
}
