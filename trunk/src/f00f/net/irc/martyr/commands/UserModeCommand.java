package f00f.net.irc.martyr.commands;

import f00f.net.irc.martyr.InCommand;
import f00f.net.irc.martyr.Mode;
import f00f.net.irc.martyr.clientstate.ClientState;
import f00f.net.irc.martyr.modes.user.InvisibleMode;
import f00f.net.irc.martyr.util.FullNick;
import java.util.HashMap;
import java.util.StringTokenizer;


/**
 * Defines a user MODE command.
 */
public class UserModeCommand extends ModeCommand
{


    private final FullNick user;
	private final FullNick sender;
	//private List modes;

	private static HashMap<Character,Mode> modeTypes;
	
	public UserModeCommand( String prefix, String userStr, StringTokenizer tokens )
	{
//		System.out.println( prefix );
		sender = new FullNick( prefix );
		user = new FullNick( userStr );
	
		if( !sender.equals( user ) ) 
		{

			return;
		}
		
		makeModeTypes();

		//modes = parseModes( modeTypes, tokens );

//		System.out.println( modes );
	}
	
	private void makeModeTypes()
	{
		if( modeTypes == null )
		{
			modeTypes = new HashMap<>();
			
			// Add new mode types here
			registerMode( modeTypes, new InvisibleMode() );
		}
	}
	
	
	/**
	 * Should not be called, as ModeCommand does the parsing and instantiation
	 * of this class.
	 */
	public InCommand parse( String prefix, String identifier, String params )
	{
		throw new IllegalStateException( "Don't call this method!" );
	}
	
	public String render()
	{
		throw new UnsupportedOperationException("Can't send user modes, yet." );
	}
	
	public FullNick getUser()
	{
		return user;
	}

    public FullNick getSender() {
        return sender;
    }

    {
	}

    public boolean updateClientState( ClientState state )
	{
		// TODO implement
		return false;
	}
	
	public String toString()
	{
		return "UserModeCommand";
	}
	

}


