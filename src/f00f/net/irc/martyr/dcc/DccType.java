package f00f.net.irc.martyr.dcc;

public class DccType
{
	public static final DccType CHAT = new DccType( "CHAT" );
	public static final DccType SEND = new DccType( "SEND" );

    public static DccType getInstance(String typeStr) throws InvalidDccException {
        typeStr = typeStr.toUpperCase();
        
        if( typeStr.equals( CHAT.toString() ) )
        {
            return CHAT;
        }
        else if( typeStr.equals( SEND.toString() ) )
        {
            return SEND;
        }
        else {
            throw new InvalidDccException("Unknown DCC type " + typeStr);
        }
    }

	private final String dccType;

	private DccType( String type )
	{
		this.dccType = type;
	}
	
	/**
	 * Returns the DCC TYPE tag, either CHAT or SEND.
     * @return 
	 */
        @Override
	public String toString()
	{
		return dccType;
	}

}
