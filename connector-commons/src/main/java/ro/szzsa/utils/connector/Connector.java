package ro.szzsa.utils.connector;

import ro.szzsa.utils.connector.exception.ConnectorException;

/**
 * Connects the server with the client.
 */
public interface Connector {

  String sendRequest(Request request) throws ConnectorException;
}
