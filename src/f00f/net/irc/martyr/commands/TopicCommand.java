package f00f.net.irc.martyr.commands;

import f00f.net.irc.martyr.CommandRegister;
import f00f.net.irc.martyr.InCommand;
import f00f.net.irc.martyr.clientstate.Channel;
import f00f.net.irc.martyr.clientstate.ClientState;
import java.util.Date;


public class TopicCommand extends AbstractCommand
{



    public static final String IDENTIFIER_PRIMARY = "TOPIC";
    public static final String IDENTIFIER_SECONDARY = "332";
    private String channel;
    private String topic;

    public TopicCommand()
    {
        this( null, null );
    }

    public TopicCommand( String channel, String topic )
    {
        this.channel = channel;
        this.topic = topic;
    }

    @Override
    public String getIrcIdentifier()
    {
        //
        // This command uses "TOPIC" on outgoing, so that is why we use
        // "TOPIC" here instead of "332".
        //
        return IDENTIFIER_PRIMARY;
    }

    @Override
    public void selfRegister( CommandRegister commandRegister )
    {
        commandRegister.addCommand( IDENTIFIER_PRIMARY, this );
        commandRegister.addCommand( IDENTIFIER_SECONDARY, this );
    }

    @Override
    public InCommand parse( String prefix, String identifier, String params )
    {
        // when the command is used as a reply, the nick is parameter 0.
        if( identifier.equals( IDENTIFIER_SECONDARY ) ) {
            return new TopicCommand( getParameter(params, 1), getParameter(params, 2) );
        } else {
            return new TopicCommand( getParameter(params, 0), getParameter(params, 1) );
        }
    }

    @Override
    public String renderParams()
    {
        return getChannel() + " :" + getTopic();
    }

    public String getTopic()
    {
        return topic;
    }

    public String getChannel()
    {
        return channel;
    }

    @Override
    public boolean updateClientState( ClientState state )
    {
        
        Channel chan = state.getChannel( channel );
        chan.setTopic( topic );
        chan.setTopicDate( new Date() );
        return true;
    }

}

