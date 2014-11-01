package signalJ.services;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.japi.pf.ReceiveBuilder;
import play.Logger;
import signalJ.SignalJPlugin;
import signalJ.models.Messages;

import java.util.*;
import java.util.stream.Collectors;

public class GroupsActor extends AbstractActor {
    private final Map<String, List<UUID>> groups = new HashMap<>();
    private final Map<UUID, List<String>> usersInGroup = new HashMap<>();
    private final ActorRef signalJActor = SignalJPlugin.getSignalJActor();

    public GroupsActor() {
        receive(
            ReceiveBuilder.match(Messages.GroupJoin.class, groupJoin -> {
                if (!groups.containsKey(groupJoin.groupname)) {
                    groups.put(groupJoin.groupname, new ArrayList<UUID>());
                }
                final List<UUID> uuids = groups.get(groupJoin.groupname);
                if (!uuids.contains(groupJoin.uuid)) {
                    uuids.add(groupJoin.uuid);
                }
                if (!usersInGroup.containsKey(groupJoin.uuid)) {
                    usersInGroup.put(groupJoin.uuid, new ArrayList<String>());
                }
                final List<String> userGroups = usersInGroup.get(groupJoin.uuid);
                if (!userGroups.contains(groupJoin.groupname)) {
                    userGroups.add(groupJoin.groupname);
                }
                Logger.debug(groupJoin.uuid + " joined group: " + groupJoin.groupname);
            }).match(Messages.GroupLeave.class, groupLeave -> {
                leaveGroup(groupLeave.uuid, groupLeave.groupname);
                if (usersInGroup.containsKey(groupLeave.uuid)) {
                    final List<String> userGroups = usersInGroup.get(groupLeave.uuid);
                    userGroups.remove(groupLeave.groupname);
                    if (userGroups.isEmpty()) usersInGroup.remove(groupLeave.uuid);
                }
                Logger.debug(groupLeave.uuid + " left group: " + groupLeave.groupname);
            }).match(Messages.Quit.class, quit -> {
                if (usersInGroup.containsKey(quit.uuid)) {
                    final List<String> userGroups = usersInGroup.get(quit.uuid);
                    userGroups.stream().forEach(
                            group -> leaveGroup(quit.uuid, group)
                    );
                    usersInGroup.remove(quit.uuid);
                }
            }).match(Messages.ClientFunctionCall.class, clientFunctionCall -> {
                switch (clientFunctionCall.sendType) {
                    case All:
                    case Others:
                    case Caller:
                    case Clients:
                    case AllExcept:
                        throw new IllegalStateException("Only Groups should be handled by the Group Actor. SendType: " + clientFunctionCall.sendType);
                    case Group:
                        if (groups.containsKey(clientFunctionCall.groupName)) {
                            signalJActor.forward(new Messages.ClientFunctionCall(
                                    clientFunctionCall.method,
                                    clientFunctionCall.hubName,
                                    null,
                                    Messages.SendType.Clients,
                                    clientFunctionCall.name,
                                    clientFunctionCall.args,
                                    groups.get(clientFunctionCall.groupName).toArray(new UUID[0]),
                                    clientFunctionCall.allExcept,
                                    clientFunctionCall.groupName
                            ), getContext());
                        }
                        break;
                    case InGroupExcept:
                        final List<UUID> inGroupExcept = Arrays.asList(clientFunctionCall.allExcept);
                        if (groups.containsKey(clientFunctionCall.groupName)) {
                            final List<UUID> sendTo = (groups.get(clientFunctionCall.groupName).stream()
                                    .filter(uuid -> !inGroupExcept.contains(uuid)).collect(Collectors.toList()));
                            signalJActor.forward(new Messages.ClientFunctionCall(
                                    clientFunctionCall.method,
                                    clientFunctionCall.hubName,
                                    null,
                                    Messages.SendType.Clients,
                                    clientFunctionCall.name,
                                    clientFunctionCall.args,
                                    sendTo.toArray(new UUID[0]),
                                    clientFunctionCall.allExcept,
                                    clientFunctionCall.groupName
                            ), getContext());
                        }
                        break;
                }
            }).build()
        );
    }

    private void leaveGroup(final UUID uuid, final String group) {
        if(groups.containsKey(group)) {
            final List<UUID> uuids = groups.get(group);
            uuids.remove(uuid);
            if(uuids.isEmpty()) groups.remove(group);
        }
    }
}