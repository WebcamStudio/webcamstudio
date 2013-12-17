package f00f.net.irc.martyr;

import f00f.net.irc.martyr.commands.InviteCommand;
import f00f.net.irc.martyr.commands.IsonCommand;
import f00f.net.irc.martyr.commands.JoinCommand;
import f00f.net.irc.martyr.commands.KickCommand;
import f00f.net.irc.martyr.commands.MessageCommand;
import f00f.net.irc.martyr.commands.ModeCommand;
import f00f.net.irc.martyr.commands.NickCommand;
import f00f.net.irc.martyr.commands.NoticeCommand;
import f00f.net.irc.martyr.commands.PartCommand;
import f00f.net.irc.martyr.commands.PingCommand;
import f00f.net.irc.martyr.commands.QuitCommand;
import f00f.net.irc.martyr.commands.TopicCommand;
import f00f.net.irc.martyr.commands.WelcomeCommand;
import f00f.net.irc.martyr.errors.AlreadyRegisteredError;
import f00f.net.irc.martyr.errors.CannotSendToChanError;
import f00f.net.irc.martyr.errors.CantKillServerError;
import f00f.net.irc.martyr.errors.ChanOPrivsNeededError;
import f00f.net.irc.martyr.errors.ChannelBannedError;
import f00f.net.irc.martyr.errors.ChannelInviteOnlyError;
import f00f.net.irc.martyr.errors.ChannelLimitError;
import f00f.net.irc.martyr.errors.ChannelWrongKeyError;
import f00f.net.irc.martyr.errors.ErroneusNicknameError;
import f00f.net.irc.martyr.errors.FileErrorError;
import f00f.net.irc.martyr.errors.KeySetError;
import f00f.net.irc.martyr.errors.LoadTooHighError;
import f00f.net.irc.martyr.errors.NeedMoreParamsError;
import f00f.net.irc.martyr.errors.NickCollisionError;
import f00f.net.irc.martyr.errors.NickInUseError;
import f00f.net.irc.martyr.errors.NoAdminInfoError;
import f00f.net.irc.martyr.errors.NoLoginError;
import f00f.net.irc.martyr.errors.NoMotdError;
import f00f.net.irc.martyr.errors.NoNicknameGivenError;
import f00f.net.irc.martyr.errors.NoOperHostError;
import f00f.net.irc.martyr.errors.NoOriginError;
import f00f.net.irc.martyr.errors.NoPermForHostError;
import f00f.net.irc.martyr.errors.NoPrivilegesError;
import f00f.net.irc.martyr.errors.NoRecipientError;
import f00f.net.irc.martyr.errors.NoSuchChannelError;
import f00f.net.irc.martyr.errors.NoSuchNickError;
import f00f.net.irc.martyr.errors.NoSuchServerError;
import f00f.net.irc.martyr.errors.NoTextToSendError;
import f00f.net.irc.martyr.errors.NotOnChannelError;
import f00f.net.irc.martyr.errors.NotRegisteredError;
import f00f.net.irc.martyr.errors.PasswdMismatchError;
import f00f.net.irc.martyr.errors.SummonDisabledError;
import f00f.net.irc.martyr.errors.TooManyChannelsError;
import f00f.net.irc.martyr.errors.TooManyTargetsError;
import f00f.net.irc.martyr.errors.UModeUnknownFlagError;
import f00f.net.irc.martyr.errors.UnknownCommandError;
import f00f.net.irc.martyr.errors.UnknownModeError;
import f00f.net.irc.martyr.errors.UserNotInChannelError;
import f00f.net.irc.martyr.errors.UserOnChannelError;
import f00f.net.irc.martyr.errors.UsersDisabledError;
import f00f.net.irc.martyr.errors.UsersDontMatchError;
import f00f.net.irc.martyr.errors.WasNoSuchNickError;
import f00f.net.irc.martyr.errors.WildTopLevelError;
import f00f.net.irc.martyr.errors.YoureBannedCreepError;
import f00f.net.irc.martyr.replies.AwayReply;
import f00f.net.irc.martyr.replies.ChannelCreationReply;
import f00f.net.irc.martyr.replies.LUserClientReply;
import f00f.net.irc.martyr.replies.LUserMeReply;
import f00f.net.irc.martyr.replies.LUserOpReply;
import f00f.net.irc.martyr.replies.ListEndReply;
import f00f.net.irc.martyr.replies.ListReply;
import f00f.net.irc.martyr.replies.ListStartReply;
import f00f.net.irc.martyr.replies.ModeReply;
import f00f.net.irc.martyr.replies.NamesEndReply;
import f00f.net.irc.martyr.replies.NamesReply;
import f00f.net.irc.martyr.replies.NowAwayReply;
import f00f.net.irc.martyr.replies.TopicInfoReply;
import f00f.net.irc.martyr.replies.UnAwayReply;
import f00f.net.irc.martyr.replies.WhoisChannelsReply;
import f00f.net.irc.martyr.replies.WhoisEndReply;
import f00f.net.irc.martyr.replies.WhoisIdleReply;
import f00f.net.irc.martyr.replies.WhoisServerReply;
import f00f.net.irc.martyr.replies.WhoisUserReply;
import java.util.Hashtable;

/**
 * CommandRegister is basically a big hashtable that maps IRC
 * identifiers to command objects that can be used as factories to
 * do self-parsing.  CommandRegister is also the central list of
 * commands.
 */
public class CommandRegister
{

    private final Hashtable<String,InCommand> commands;
    public CommandRegister()
    {
        commands = new Hashtable<>();

        // Note that currently, we only have to register commands that
        // can be received from the server.
        new InviteCommand().selfRegister( this );
        new JoinCommand().selfRegister( this );
        new KickCommand().selfRegister( this );
        new MessageCommand().selfRegister( this );
        new ModeCommand().selfRegister( this );
        new IsonCommand().selfRegister( this );
        new NickCommand().selfRegister( this );
        new NoticeCommand().selfRegister( this );
        new PartCommand().selfRegister( this );
        new PingCommand().selfRegister( this );
        new QuitCommand().selfRegister( this );
        new TopicCommand().selfRegister( this );
        new WelcomeCommand().selfRegister( this );

        // Register errors
        new AlreadyRegisteredError().selfRegister( this );
        new CannotSendToChanError().selfRegister( this );
        new CantKillServerError().selfRegister( this );
        new ChannelBannedError().selfRegister( this );
        new ChannelInviteOnlyError().selfRegister( this );
        new ChannelLimitError().selfRegister( this );
        new ChannelWrongKeyError().selfRegister( this );
        new ChanOPrivsNeededError().selfRegister( this );
        new ErroneusNicknameError().selfRegister( this );
        new FileErrorError().selfRegister( this );
        new KeySetError().selfRegister( this );
        new LoadTooHighError().selfRegister( this );
        new NeedMoreParamsError().selfRegister( this );
        new NickCollisionError().selfRegister( this );
        new NickInUseError().selfRegister( this );
        new NoAdminInfoError().selfRegister( this );
        new NoLoginError().selfRegister( this );
        new NoMotdError().selfRegister( this );
        new NoNicknameGivenError().selfRegister( this );
        new NoOperHostError().selfRegister( this );
        new NoOriginError().selfRegister( this );
        new NoPermForHostError().selfRegister( this );
        new NoPrivilegesError().selfRegister( this );
        new NoRecipientError().selfRegister( this );
        new NoSuchChannelError().selfRegister( this );
        new NoSuchNickError().selfRegister( this );
        new NoSuchServerError().selfRegister( this );
        new NoTextToSendError().selfRegister( this );
        new NotOnChannelError().selfRegister( this );
        new NotRegisteredError().selfRegister( this );
        new PasswdMismatchError().selfRegister( this );
        new SummonDisabledError().selfRegister( this );
        new TooManyChannelsError().selfRegister( this );
        new TooManyTargetsError().selfRegister( this );
        new UModeUnknownFlagError().selfRegister( this );
        new UnknownCommandError().selfRegister( this );
        new UnknownModeError().selfRegister( this );
        new UserNotInChannelError().selfRegister( this );
        new UserOnChannelError().selfRegister( this );
        new UsersDisabledError().selfRegister( this );
        new UsersDontMatchError().selfRegister( this );
        new WasNoSuchNickError().selfRegister( this );
        new WildTopLevelError().selfRegister( this );
        new YoureBannedCreepError().selfRegister( this );

        // Register replies
        new ChannelCreationReply().selfRegister( this );
        new AwayReply().selfRegister( this );
        new ListEndReply().selfRegister( this );
        new ListReply().selfRegister( this );
        new ListStartReply().selfRegister( this );
        new LUserClientReply().selfRegister( this );
        new LUserMeReply().selfRegister( this );
        new LUserOpReply().selfRegister( this );
        new ModeReply().selfRegister( this );
        new NamesEndReply().selfRegister( this );
        new NamesReply().selfRegister( this );
        new NowAwayReply().selfRegister( this );
        new TopicInfoReply().selfRegister( this );
        new UnAwayReply().selfRegister( this );
        new WhoisChannelsReply().selfRegister( this );
        new WhoisEndReply().selfRegister( this );
        new WhoisIdleReply().selfRegister( this );
        new WhoisServerReply().selfRegister( this );
        new WhoisUserReply().selfRegister( this );
    }

    public void addCommand( String ident, InCommand command )
    {
        commands.put( ident, command );
    }

    public InCommand getCommand( String ident )
    {
        return commands.get( ident );
    }

}

