package f00f.net.irc.martyr.commands;

import f00f.net.irc.martyr.OutCommand;

import java.util.List;
import java.util.ArrayList;

/**
 * Defines the LIST command, which is used to get the topic and member count of certain channels, or all of them.
 *
 * @author Daniel Henninger
 */
public class ListCommand implements OutCommand
{

    /* List of channels we will request topic and member count info of. */
    List<String> channels = new ArrayList<String>();

    /**
     * No parameter passed to the LIST command represents a request for all channels.
     */
    public ListCommand()
    {
        // Nothing to do
    }

    /**
     * Request information about a single channel.
     *
     * @param channel Channel you want to request the topic and member count of.
     */
    public ListCommand(String channel)
    {
        this.channels.add(channel);
    }

    /**
     * Request information of multiple channels.
     *
     * @param channels List of channels you want to retrieve the topic and member count of.
     */
    public ListCommand(List<String> channels)
    {
        this.channels.addAll(channels);
    }

    /**
     * @see f00f.net.irc.martyr.OutCommand#render()
     */
    public String render()
    {
        String ret = getIrcIdentifier();
        if (channels.size() > 0)
        {
            ret = ret + " ";
            Boolean isFirst = true;
            for (String channel : channels) {
                if (isFirst) {
                    ret = ret + channel;
                    isFirst = false;
                }
                else {
                    ret = ret + "," + channel;
                }
            }
        }
        return ret;
    }

    /**
     * @see f00f.net.irc.martyr.Command#getIrcIdentifier()
     */
    public String getIrcIdentifier()
    {
        return "LIST";
    }

}
