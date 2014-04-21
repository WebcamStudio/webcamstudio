package f00f.net.irc.martyr.modes.user;

import f00f.net.irc.martyr.modes.GenericMode;

/**
 * 
 */
public abstract class GenericUserMode extends GenericMode
{
        @Override
	public boolean recordInChannel()
	{
		return false;
	}

	/**
	 * Well, this is kind of irrelevent isn't it?
     * @return 
	 */
        @Override
	public boolean onePerChannel()
	{
		return false;
	}
}


