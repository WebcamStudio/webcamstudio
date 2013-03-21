package f00f.net.irc.martyr.commands;

import f00f.net.irc.martyr.OutCommand;

/**
 * Defines the AWAY message which is used to indicate that you are or are not away from the keyboard.
 *
 * @author Daniel Henninger
 */
public class AwayCommand implements OutCommand
{

    String awayMessage = null;

    /**
     * A blank AWAY command indicates that you are no longer away.
     */
    public AwayCommand()
    {
        // Nothing to do
    }

    /**
     * An AWAY command with a message indicates that you are in fact away.
     *
     * @param awayMessage Message indicating why you are away.
     */
    public AwayCommand(String awayMessage)
    {
        this.awayMessage = awayMessage;
    }

    /**
     * @see f00f.net.irc.martyr.OutCommand#render()
     */
    public String render()
    {
        String ret = getIrcIdentifier();
        if (awayMessage != null && awayMessage.length() > 0) {
            ret = ret + " " + awayMessage;
        }
        return ret;
    }

    /**
     * @see f00f.net.irc.martyr.Command#getIrcIdentifier()
     */
    public String getIrcIdentifier()
    {
        return "AWAY";
    }

}