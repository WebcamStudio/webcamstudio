package f00f.net.irc.martyr.commands;

import f00f.net.irc.martyr.InCommand;
import f00f.net.irc.martyr.clientstate.Channel;
import f00f.net.irc.martyr.clientstate.ClientState;
import f00f.net.irc.martyr.util.FullNick;



/**
 * Defines KICK command.
 */
public class KickCommand extends AbstractCommand
{



    private String channel;
    private FullNick userKicker;
    private FullNick userKicked;
    private String comment;

    /** For use as a factory */
    public KickCommand()
    {
        this( null, null, null, null );
    }

    public KickCommand( FullNick userKicker, String channel,
        String userKicked, String comment )
    {
        this.userKicker = userKicker;
        this.channel = channel;
        this.userKicked = new FullNick( userKicked );
        this.comment = comment;
    }

    public KickCommand( String channel, String userToKick, String comment )
    {
        this( null, channel, userToKick, comment );
    }

    @Override
    public InCommand parse( String prefix, String identifier, String params )
    {
        return new KickCommand(
            new FullNick( prefix ),
            getParameter( params, 0 ),
            getParameter( params, 1 ),
            getParameter( params, 2 )
        );
    }

    @Override
    public String getIrcIdentifier()
    {
        return "KICK";
    }

    @Override
    public String renderParams()
    {
        return channel + " " + userKicked + " :" + comment;
    }

    public String getChannel()
    {
        return channel;
    }

    public FullNick getKicker()
    {
        return userKicker;
    }

    public FullNick getKicked()
    {
        return userKicked;
    }

    public String getComment()
    {
        return comment;
    }

    public boolean kickedUs( ClientState state )
    {
        return userKicked.equals( state.getNick() );
    }

    @Override
    public boolean updateClientState( ClientState state )
    {
        if( kickedUs( state ) )
        {
            // We've been kicked.

            state.removeChannel( channel );
            return true;
        }
        else
        {
            // Someone else was kicked.

            // 1) Grab group
            Channel channelObj = state.getChannel( channel );
            channelObj.removeMember( userKicked, this );
            return true;
        }
    }

}


