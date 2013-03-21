package f00f.net.irc.martyr;
/**
 * Some Frequenty Asked Questions.
 *
 * <h2>How do I use Martyr?</h2>
 * Take a good look at the IRCConnection class.  Also check out the
 * programs in the test package.  There are several small testing
 * programs that show how to use the fundamentals of Martyr.  For a
 * better example, check out the Justin bot, included in martyr's
 * /examples directory.
 *
 * <h2>How do I get rid of all the output?</h2>
 * <p>Martyr uses log4j.
 * </p>
 *
 * <h2>How do I add socks support?</h2>
 * <p>
 * Martyr does not directly support SOCKS, but it can work with an external
 * SOCKS library.  <code>IRCConnection</code> allows you to pass a custom
 * <code>Socket</code> instance into the <code>connect(...)</code> method.
 * Thus, a library such as {@link <a href="http://sourceforge.net/projects/jsocks/">jsocks</a>} is ideal for use with Martyr.
 * </p>
 *
 * <p>
 * Note that while <code>IRCConnection.connect</code> allows you to pass in
 * your own <code>Socket</code>, <code>AutoReconnect</code> does not
 * have a way to pass your own <code>Socket</code> to
 * <code>IRCConnection</code> on a reconnect.  You can either subclass
 * <code>AutoReconnect</code> and override the getSocket method, or
 * subclass IRCConnection and override connect.</p>
 *
 * @see f00f.net.irc.martyr.IRCConnection
 */
public class A_FAQ
{
	private A_FAQ()
	{
	}
}
