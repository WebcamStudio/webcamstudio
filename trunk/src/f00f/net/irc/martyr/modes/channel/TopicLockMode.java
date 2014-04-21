package f00f.net.irc.martyr.modes.channel;

import f00f.net.irc.martyr.Mode;

/**
 *    <p>Topic - The channel flag 't' is used to restrict the usage of the TOPIC
 *    command to channel operators.</p>
 * (From RFC2811)
 */
public class TopicLockMode extends GenericChannelMode
{
        @Override
	public boolean requiresParam()
	{
		return false;
	}
	
        @Override
	public char getChar()
	{
		return 't';
	}
	
        @Override
	public Mode newInstance()
	{
		return new TopicLockMode();
	}
}

