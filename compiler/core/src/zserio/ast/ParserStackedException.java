package zserio.ast;

import java.util.ArrayDeque;

/**
 * ParserException extended by additional message stack to allow structured error messages.
 */
public class ParserStackedException extends ParserException
{
    /**
     * Constructor from the main error message.
     *
     * @param location AST location where the error occurred.
     * @param message  The main message for the error.
     */
    public ParserStackedException(AstLocation location, String message)
    {
        super(location, message);
        messageStack = new ArrayDeque<Message>();
    }

    /**
     * Constructor from main parser exception.
     *
     * @param exception The main parser exception containing the main error message.
     */
    public ParserStackedException(ParserException exception)
    {
        super(exception.getLocation(), exception.getMessage()); // the main error message

        if (exception instanceof ParserStackedException)
        {
            // copy the exception's extended message stack
            final ParserStackedException stackedException = (ParserStackedException)exception;
            messageStack = stackedException.messageStack.clone();
        }
        else
        {
            messageStack = new ArrayDeque<Message>();
        }
    }

    /**
     * Gets the extended message stack.
     *
     * @return Extended message stack.
     */
    public Iterable<Message> getMessageStack()
    {
        return messageStack;
    }

    /**
     * Helper class to store message with location.
     */
    public static class Message
    {
        /**
         * Constructor.
         *
         * @param location Location of the message source.
         * @param message Message as a string.
         */
        public Message(AstLocation location, String message)
        {
            this.location = location;
            this.message = message;
        }

        /**
         * Gets AST location of the message source.
         *
         * @return AST location.
         */
        public AstLocation getLocation()
        {
            return location;
        }

        /**
         * Gets the message as a string.
         *
         * @return Message.
         */
        public String getMessage()
        {
            return message;
        }

        private final AstLocation location;
        private final String message;
    }

    /**
     * Pushes new message on the top of the extended message stack.
     *
     * @param location AST location where the error occurred.
     * @param message  The extended message for the error.
     */
    public void pushMessage(AstLocation location, String message)
    {
        messageStack.push(new Message(location, message));
    }

    private static final long serialVersionUID = 9091438895972505215L;

    private final transient ArrayDeque<Message> messageStack;
}
