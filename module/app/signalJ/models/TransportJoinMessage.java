package signalJ.models;

public interface TransportJoinMessage {
    public RequestContext getContext();
    public TransportType getTransportType();
}