package f00f.net.irc.martyr.modes.user;

import f00f.net.irc.martyr.Mode;

public class InvisibleMode extends GenericUserMode
{
        @Override
	public char getChar()
	{
		return 'i';
	}
	
        @Override
	public boolean requiresParam()
	{
		return false;
	}
	
        @Override
	public Mode newInstance()
	{
		return new InvisibleMode();
	}
}

