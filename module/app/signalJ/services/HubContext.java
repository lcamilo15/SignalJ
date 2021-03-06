package signalJ.services;

import signalJ.models.RequestContext;

public interface HubContext<T> {
	public ClientsContext<T> clients();
	
	public GroupsContext groups();

    public RequestContext context();

    public void onConnected();

    public void onReconnected();

    public void onDisconnected();
}