package f00f.net.irc.martyr.replies;

import f00f.net.irc.martyr.InCommand;

public class LUserMeReply extends GenericStringReply
{

    public LUserMeReply()
    {
    }

    public LUserMeReply( String string )
    {
        super( string );
    }

    @Override
    public String getIrcIdentifier()
    {
        return "255";
    }

    @Override
    public InCommand parse( String prefix, String identifier, String params )
    {
        return new LUserMeReply( getParameter( params, 1 ) );
    }

}

