package f00f.net.irc.martyr.modes.channel;

import f00f.net.irc.martyr.Mode;

/**
 *    <p>User Limit - A user limit may be set on channels by using the
 *    channel flag 'l'.  When the limit is reached, servers MUST
 *    forbid their local users to join the channel.</p>
 * 
 *    <p>The value of the limit MUST only be made available to the channel
 *    members in the reply sent by the server to a MODE query.</p>
 * (From RFC2811)
*/
public class LimitMode extends GenericChannelMode
{
        @Override
	public boolean requiresParam()
	{
		return true;
	}
	
        @Override
	public char getChar()
	{
		return 'l';
	}
	
        @Override
	public Mode newInstance()
	{
		return new LimitMode();
	}
}

