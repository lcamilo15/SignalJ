package signalJ.services;
import play.libs.Akka;
import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.actor.Props;

public class ActorLocator {
	private final static ActorRef signalJActor = Akka.system().actorOf(Props.create(SignalJActor.class), "signalJ");
	private final static ActorRef hubsActor = Akka.system().actorOf(Props.create(HubsActor.class), "hubs");
	private static ActorRef channelsActor;
	
	public static ActorRef getSignalJActor() {
		return signalJActor;
	}
	public static ActorRef getHubsActor() {
		return hubsActor;
	}

	public static ActorRef getChannelsActor(ActorContext context) {
		if(channelsActor == null) channelsActor = context.actorOf(Props.create(ChannelsActor.class), "channels");
		return channelsActor;
	}
	
	public static ActorRef getChannelsActor() {
		return channelsActor;
	}
	
	public static ActorRef getChannelActor(ActorContext context, String name) {
		return context.actorOf(Props.create(ChannelActor.class), name);
	}
	
	public static ActorRef getUserActor(ActorContext context, String name) {
		return context.actorOf(Props.create(UserActor.class), name);
	}
}