package f00f.net.irc.martyr.commands;

import f00f.net.irc.martyr.OutCommand;

/**
 * Implements a WHOIS command, to query details about a user.
 *
 */
public class WhoisCommand implements OutCommand
{
	private static final String WHOIS = "WHOIS";

	private final String target;

	/**
	 * @param target the nick or mask that you wish to know about.
	 */
	public WhoisCommand( String target )
	{
		this.target = target;
	}

	/**
	 * @return "WHOIS"
	 */
        @Override
	public String getIrcIdentifier()
	{
		return WHOIS;
	}

	/**
	 * Simply returns the string given in the constructor.
	 */
        @Override
	public String render()
	{
		return WHOIS + " " + target;
	}
}


