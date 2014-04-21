package f00f.net.irc.martyr.commands;

import f00f.net.irc.martyr.OutCommand;

/**
 * Defines PASS command, optional part of the handshake to register on the network.
 * @author Daniel Henninger
 */
public class PassCommand implements OutCommand
{

    public static final String IDENTIFIER = "PASS";
    private final String pass;

    /**
     * @param pass the password for the user who is authenticating
     * */
    public PassCommand(String pass)
    {
        this.pass = pass;
    }

    @Override
    public String render()
    {
        return IDENTIFIER + " " + pass;
    }

    @Override
    public String getIrcIdentifier()
    {
        return IDENTIFIER;
    }

}
