package f00f.net.irc.martyr.commands;

import f00f.net.irc.martyr.CommandRegister;
import f00f.net.irc.martyr.InCommand;
import f00f.net.irc.martyr.Mode;
import f00f.net.irc.martyr.OutCommand;
import f00f.net.irc.martyr.State;
import f00f.net.irc.martyr.clientstate.Channel;
import f00f.net.irc.martyr.clientstate.ClientState;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;


/**
 * Defines MODE command.  Since the MODE command is of two distinct
 * types, this class is really more of a command mini-factory.  It
 * determines which type of command it is, either a UserModeCommand or
 * a ChannelModeCommand.
 *
 */
public class ModeCommand implements InCommand, OutCommand
{


    public static final String IDENTIFIER = "MODE";

    public static void registerMode(Map<Character,Mode> modes, Mode mode) {
        Character modeChar = mode.getChar();
        
        if( modes.get( modeChar ) != null )
        {
        }
        
        modes.put( modeChar, mode );
    }
	private String source;
	
	/** For use as a factory */
	public ModeCommand()
	{
	}
	
    @Override
	public Iterator getAttributeKeys()
	{
		return new LinkedList().iterator();
	}

    @Override
	public String getAttribute( String key )
	{
		return null;
	}

    @Override
	public State getState(  )
        {
            return State.REGISTERED;
        }
	
    @Override
	public void selfRegister(CommandRegister reg)
        {
            reg.addCommand( IDENTIFIER, this );
        }
	
    @Override
	public String getIrcIdentifier(  )
        {
            return IDENTIFIER;
        }

	// Example
    @Override
    public InCommand parse(String prefix, String identifier, String params)
    {
        // there are two kinds of modes.  Either a channel mode, or a user
        // mode.  We need to figure out which we are dealing with, and
        // return that.
        
        // TODO: Research: Should we specify delimiters other than whitespace?
        StringTokenizer tokens = new StringTokenizer( params );
        
        String str = tokens.nextToken();
        
        //log.debug("ModeCommand: Prefix: " + prefix + " str: " + str
        //	+ " total: " + params);
        
        // Malformed command.
        if( str == null ) {
            return null;
        }
        
        // Should we check to see if the string is really a channel
        // that we know about?
        if( Channel.isChannel( str ) )
        {
            return new ChannelModeCommand( prefix, str, tokens );
        }
        else
        {
            return new UserModeCommand( prefix, str, tokens );
        }
    }
	
	/**
     * Should not be called, as ModeCommand doesn't actually represent a
     * command.  Use UserModeCommand or ChannelModeCommand instead.
     */@Override
	public String render(  )
     {
         throw new IllegalStateException("Don't try to send ModeCommand!");
     }

    @Override
	public void setSourceString(String source)
        {
            this.source = source;
        }
	
    @Override
	public String getSourceString(  )
        {
            return source;
        }
	
	/**
     * Does nothing, as this is a factory command.
     * @param cs
     */
    @Override
    public boolean updateClientState(ClientState cs)
    {
        // Nothing here, move on.
        return false;
    }

    @Override
	public String toString(  )
        {
            return "ModeCommand";
        }
	
	/** Takes a mode string, such as: '+ooo A B C' or '+o A +o B' or even
     * '+o-o A B' and returns a List containing Mode objects that
     * correspond to the modes specified.
     *
     * @param modes is a Map of Character to Mode objects.
     * @param tokens is the sequence of tokens making up the parameters of
     * the command.
     * @return List of modes
     */
    public List<Mode> parseModes(Map<Character,Mode> modes, StringTokenizer tokens)
    {
        LinkedList<Mode> results = new LinkedList<>();
        
        while( true )
        {
            if( tokens.hasMoreTokens() )
            {
                parseOneModeSet( modes, tokens, results );
            }
            else
            {
                return results;
            }
        }
    }
	
	/**
     * Parses one group of modes.  '+ooo A B C' and not '+o A +o B'.  It
     * will parse the first group it finds and will ignore the rest.
     *
     * @param modes Map of character to Mode objects.
     * @param tokens Sequence of tokens making up the parameters of the command.
     * @param results List of Mode results to be filled in
     */
	private void parseOneModeSet( Map<Character,Mode> modes, StringTokenizer tokens, List<Mode> results)
        {
            // A list of modes that we have.
            LinkedList<Mode> localModes = new LinkedList<>();
            
            Mode.Sign sign = Mode.Sign.NOSIGN;
            String chars = tokens.nextToken();
            
            int stop = chars.length();
            for( int i = 0; i < stop; ++i )
            {
                char lookingAt = chars.charAt( i );
                if( lookingAt == '+' ) {
                    sign = Mode.Sign.POSITIVE;
                } else if( lookingAt == '-' ) {
                    sign = Mode.Sign.NEGATIVE;
                } else if( lookingAt == ':' ) {
                    // This is to get around a bug in some ircds
                    continue;
                } else
                {
                    // A real mode character!
                    Mode mode = modes.get( lookingAt );
                    if( mode == null )
                    {
                        //TODO: Is there some way we can figure out if the mode
                        // we don't know anything about needs a parameter?
                        // Things get messy if it does need a parameter, and we
                        // don't eat the string.
                        
                    }
                    else
                    {
                        mode = mode.newInstance();
                        mode.setSign( sign );
                        localModes.add( mode );
                    }
                }
            }
            
            // Now we know what modes are specified, and whether they are
            // positive or negative.  Now we need to fill in the parameters for
            // any that require parameters, and place the results in the result
            // list.
            for (Mode localMode : localModes) {
                /*
                * What we do if the server doesn't pass us a parameter
                * for a mode is rather undefined - except that we don't
                * want to run off the end of the tokens.  So we just
                * ignore it.  The problem is that we don't always know
                * when a server is going to send us a parameter or not.
                * We can only hope that servers don't send ambiguous
                * masks followed by more modes instead of a parameter.
                */
                if (localMode != null && localMode.requiresParam() && tokens.hasMoreTokens()) {
                    localMode.setParam(tokens.nextToken());
                }
                
                results.add(localMode);
            }
        }

}


