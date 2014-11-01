package actors;

import akka.actor.UntypedActor;
import hubs.Chat;
import hubs.ChatPage;
import models.ChatMessage;
import signalJ.GlobalHost;
import signalJ.services.HubContext;

public class Robot extends UntypedActor {
    @Override
    public void onReceive(Object message) throws Exception {
        HubContext<ChatPage> hub = GlobalHost.getHub(Chat.class);
        hub.clients().all.messageToRoom(new ChatMessage("Robot", "I am alive!", null));
    }
}