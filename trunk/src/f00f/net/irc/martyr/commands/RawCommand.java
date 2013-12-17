package f00f.net.irc.martyr.commands;

import f00f.net.irc.martyr.OutCommand;
import java.util.StringTokenizer;

public class RawCommand implements OutCommand
{

    private final String sourceString;
    private final String ident;

    /**
     * Tries to use the first "word" in the command as the identifier.
     * Using this constructor is not recommended.
     *
     * @param raw Raw command to send to server
     */
    public RawCommand( String raw )
    {
        sourceString = raw;
        StringTokenizer tokens = new StringTokenizer( raw );
        ident = tokens.nextToken();
    }

    /**
     * The rendered command will be <code>identifier + " " +
     * parameters</code>.  This constructure simply allows a correct
     * response to the <code>getIrcIdentifier</code> method.
     *
     * @param identifier Command identifier
     * @param parameters Parameters to pass
     */
    public RawCommand( String identifier, String parameters )
    {
        ident = identifier;
        sourceString = ident + " " + parameters;
    }

    /**
     * Returns the identifier, if supplied, or null.
     */
    public String getIrcIdentifier()
    {
        return ident;
    }

    /**
     * Simply returns the string given in the constructor.
     */
    public String render()
    {
        return sourceString;
    }

}


