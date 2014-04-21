package f00f.net.irc.martyr.replies;

import f00f.net.irc.martyr.InCommand;
import f00f.net.irc.martyr.State;
import f00f.net.irc.martyr.commands.UnknownCommand;


/**
 * A container for unknown replies.
 */
public class UnknownReply extends UnknownCommand
{

    public static boolean isReply(String ident) {
        char c = ident.charAt(0);
        return ( c == '0' || c == '2' || c == '3' );
    }
	private final String replyStr;
	private final int replyCode;

	public UnknownReply( String ident )
	{
		replyStr = ident;
		replyCode = Integer.parseInt( ident );
	}

	public int getReplyCode()
	{
		return replyCode;
	}

	public String getReply()
	{
		return replyStr;
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
        throw new UnsupportedOperationException("UnknownReply does no parsing.");
    }

	/**
     * Unknown, so we don't know what the identifier is ahead of time.
     * @return 
     */
    @Override
	public String getIrcIdentifier(  )
        {
            return replyStr;
        }

    @Override
	public String toString()
        {
            return "UnknownReply[" + replyStr + "]";
        }


}


