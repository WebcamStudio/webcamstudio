package f00f.net.irc.martyr.replies;

import f00f.net.irc.martyr.InCommand;
import f00f.net.irc.martyr.util.ParameterIterator;
import java.util.Date;



public class WhoisIdleReply extends AbstractWhoisReply
{
    

    private int idleTime;
	private Date loginTime;

	/**
	 * Factory constructor.
	 * */
	public WhoisIdleReply()
	{
	}

	public WhoisIdleReply( String params )
	{
		super( params );
	}

	public String getIrcIdentifier()
	{
		return "317";
	}

	/**
	 * @return seconds idle
	 * */
	public int getIdleTime()
	{
		return idleTime;
	}

	/** 
	 * @return login time, if provided, null otherwise
	 * */
	public Date getLoginTime()
	{
		return loginTime;
	}

	protected void parseParams( ParameterIterator pi )
	{
		String idleTimeStr = (String)pi.next(); // Idle name
		idleTime = Integer.parseInt( idleTimeStr );
		if( pi.hasNext() && ! pi.nextIsLast() )
		{
			String loginTimeStr = (String)pi.next(); // Idle description
			loginTime = new Date( Long.parseLong( loginTimeStr ) * 1000 );
		}

	}

	public InCommand parse( String prefix, String identifier, String params )
	{
		return new WhoisIdleReply( params );
	}

}

