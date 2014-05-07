package f00f.net.irc.martyr.commands;

import f00f.net.irc.martyr.InCommand;
import f00f.net.irc.martyr.Mode;
import f00f.net.irc.martyr.clientstate.Channel;
import f00f.net.irc.martyr.clientstate.ClientState;
import f00f.net.irc.martyr.modes.channel.AnonChannelMode;
import f00f.net.irc.martyr.modes.channel.BanMode;
import f00f.net.irc.martyr.modes.channel.ExceptionMode;
import f00f.net.irc.martyr.modes.channel.InviteMaskMode;
import f00f.net.irc.martyr.modes.channel.InviteOnlyMode;
import f00f.net.irc.martyr.modes.channel.KeyMode;
import f00f.net.irc.martyr.modes.channel.LimitMode;
import f00f.net.irc.martyr.modes.channel.ModeratedMode;
import f00f.net.irc.martyr.modes.channel.NoExtMsgMode;
import f00f.net.irc.martyr.modes.channel.OperMode;
import f00f.net.irc.martyr.modes.channel.PrivateMode;
import f00f.net.irc.martyr.modes.channel.SecretMode;
import f00f.net.irc.martyr.modes.channel.TopicLockMode;
import f00f.net.irc.martyr.modes.channel.VoiceMode;
import f00f.net.irc.martyr.util.FullNick;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Defines the ChannelMode command.  Can be used to send a Channel
 * mode.  For receiving, this defines which channel modes Martyr knows
 * about and passes them on to the Channel object.  Note that the
 * actual logic of what happens when a mode arrives lies in the
 * clientstate.Channel object.
 */
public class ChannelModeCommand extends ModeCommand
{
    private static HashMap<Character,Mode> modeTypes;

    private String prefix;
    private final String channelName;
	private FullNick sender;
	
	private final List modes;


	/**
	 * For receiving a mode command.
     * @param prefix Currently unused prefix string
     * @param channelName Channel that the mode change is in reference to
     * @param params List of params to be parsed
     */
	public ChannelModeCommand( String prefix, String channelName,
		StringTokenizer params )
	{
		makeModes();

        this.prefix = prefix;
        this.channelName = channelName;

            modes = parseModes( modeTypes, params );

//            System.out.println( modes );
	}

	/**
	 * For sending a mode discovery.
     *
     * @param channelName Channel that the mode change is in reference to
     */
	public ChannelModeCommand( String channelName )
	{	
		sender = null;
		this.channelName = channelName;

		// Empty list, no modes.
		modes = new LinkedList();
	}

	public void makeModes()
	{
		if( modeTypes == null )
		{
			modeTypes = new HashMap<>();
			
			registerMode( modeTypes, new BanMode() );
			registerMode( modeTypes, new KeyMode() );
			registerMode( modeTypes, new OperMode() );
			registerMode( modeTypes, new VoiceMode() );
			registerMode( modeTypes, new LimitMode() );
			// registerMode( modeTypes, new QuietMode() );
			registerMode( modeTypes, new SecretMode() );
			registerMode( modeTypes, new PrivateMode() );
			registerMode( modeTypes, new NoExtMsgMode() );
			registerMode( modeTypes, new ExceptionMode() );
			registerMode( modeTypes, new TopicLockMode() );
			registerMode( modeTypes, new ModeratedMode() );
			registerMode( modeTypes, new InviteMaskMode() );
			registerMode( modeTypes, new InviteOnlyMode() );
			registerMode( modeTypes, new AnonChannelMode() );
		}
	}
	
	/**
	 * Shouldn't be called, as ModeCommand should be responsible for parsing
	 * and creating this class.
     * @return 
	 */
    @Override
	public InCommand parse( String prefix, String identifier, String params )
	{
		throw new IllegalStateException( "Don't call this method!" );
	}
	
    @Override
	public String render()
	{
		return "MODE " + channelName + renderParams();
	}

	public String renderParams()
	{
		Iterator modesI = modes.iterator();

		String modes = "";
		String params = "";

		while( modesI.hasNext() )
		{
			Mode mode = (Mode)modesI.next();
			
			if( mode.getSign() != Mode.Sign.NOSIGN )
			{
				modes += (mode.getSign() == Mode.Sign.POSITIVE ? "+" : "-" );
			}
			modes += mode.getChar();
			
			if( mode.getParam() != null )
			{
				// Does the parameter list already have params?
				// If so, stick in a space.
				if( params.length() > 0 )
				{
					params += " ";
				}
				params += mode.getParam();
			}
		}
		
		return modes + " " + params;
	}
	
	public String getChannel()
	{
		return channelName;
	}
	
	public FullNick getSender()
	{
		return sender;
	}

    public String getPrefix() {
        return prefix;
    }

    /**
	 * Passes the modes on to the clientstate.Channel object.
     * @return 
	 */
    @Override
	public boolean updateClientState( ClientState state )
	{
		boolean changed = false;
		
		Iterator modesI = modes.iterator();
		Channel channel = state.getChannel( channelName );
		
		while( modesI.hasNext() )
		{
			Mode mode = (Mode)modesI.next();
			
			channel.setMode( mode );
			
			changed = true;
		}
		
		return changed;
	}
	
	
}


