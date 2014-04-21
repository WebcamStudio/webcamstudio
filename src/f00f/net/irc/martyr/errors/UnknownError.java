package f00f.net.irc.martyr.errors;

import f00f.net.irc.martyr.InCommand;
import f00f.net.irc.martyr.State;
import f00f.net.irc.martyr.commands.UnknownCommand;


/**
 * Some unknown command, for which there is no factory.  This is a
 * special case command, created by IRCConnection if it can't find a
 * proper command object.
 */
public class UnknownError extends UnknownCommand
{

    public static boolean isError(String ident) {
        char c = ident.charAt(0);
        return ( c == '4' || c == '5' );
    }

	private final String errorStr;
	private final int errorCode;

	public UnknownError( String ident )
	{
		errorStr = ident;
		errorCode = Integer.parseInt( ident );
	}

	public int getErrorCode()
	{
		return errorCode;
	}

	public String getError()
	{
		return errorStr;
	}

    @Override
	public State getState(  )
        {
            return State.UNKNOWN;
        }

	/**
     * Never parsed.
     * @return 
     */
    @Override
    public InCommand parse(String prefix, String identifier, String params)
    {
        throw new UnsupportedOperationException("UnknownError does no parsing.");
    }

	/**
     * Unknown, so we don't know what the identifier is ahead of time.
     * @return 
     */
    @Override
	public String getIrcIdentifier(  )
        {
            return errorStr;
        }

    @Override
	public String toString()
        {
            return "UnknownError[" + errorStr + "]";
        }


}


