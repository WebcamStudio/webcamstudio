package f00f.net.irc.martyr.services;

import f00f.net.irc.martyr.IRCConnection;
import f00f.net.irc.martyr.commands.ChannelModeCommand;
import f00f.net.irc.martyr.commands.JoinCommand;
import f00f.net.irc.martyr.commands.PingCommand;
import f00f.net.irc.martyr.commands.PongCommand;
import java.util.Observable;
import java.util.Observer;

/**
 * AutoResponder is where commands that should be auto-responded (such
 * as PING-PONG) should go.
 */
public class AutoResponder implements Observer
{

    private final IRCConnection connection;
    private boolean enabled = false;

    public AutoResponder( IRCConnection connection )
    {
        this.connection = connection;
        enable();
    }

    public void enable()
    {
        if( enabled ) {
            return;
        }

        connection.addCommandObserver( this );
        enabled = true;
    }

    public void disable()
    {
        if( !enabled ) {
            return;
        }

        connection.removeCommandObserver( this );
        enabled = false;
    }

    /**
     * Does the work of figuring out what to respond to.
     * If a PING is received, send a PONG.  If we JOIN a channel, send a
     * request for modes.
     *
     * @param updated */
    @Override
    public void update( Observable observer, Object updated )
    {

        if( updated instanceof PingCommand )
        {
            // We need to do some pongin'!
            PingCommand ping = (PingCommand)updated;

            String response = ping.getPingSource();

            connection.sendCommand( new PongCommand( response ) );
        }
        else if( updated instanceof JoinCommand )
        {
            // Determine if we joined, and if we did, trigger a MODE discovery
            // request.
            JoinCommand join = (JoinCommand)updated;

            if( join.weJoined( connection.getClientState() ) )
            {
                connection.sendCommand(
                    new ChannelModeCommand( join.getChannel() ) );
            }
        }
    }

    // END AutoResponder
}
 


 
