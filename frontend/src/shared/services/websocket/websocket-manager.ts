import {
  WebSocketConnection,
  WebSocketConnectionOptions,
} from './websocket-connection';

export class WebSocketManager {
  private connections: Map<string, WebSocketConnection> = new Map();

  public shutdown(): void {
    console.log('[WebSocketManager] Shutting down');

    for (const [sessionId] of this.connections.entries()) {
      this.closeConnection(sessionId);
    }
  }

  public createConnection(
    url: string,
    sessionId: string,
    options?: WebSocketConnectionOptions,
  ): WebSocketConnection {
    if (!sessionId) {
      throw new Error('[WebSocketManager] sessionId is required');
    }

    if (this.connections.has(sessionId)) {
      return this.connections.get(sessionId);
    }

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${protocol}//${window.location.host}${url}`;
    const conn = new WebSocketConnection(wsUrl, sessionId, options);
    this.connections.set(sessionId, conn);
    return conn;
  }

  public async closeConnection(sessionId): Promise<void> {
    const conn = this.connections.get(sessionId);

    if (!conn) {
      return;
    }

    try {
      await conn.disconnect();
    } catch (error) {
      console.error(`[WebSocketManager] Error closing ${sessionId}:`, error);
    }

    this.connections.delete(sessionId);
  }

  public hasConnection(sessionId): boolean {
    return this.connections.has(sessionId);
  }

  public getExistingConnection(sessionId): WebSocketConnection | null {
    return this.connections.get(sessionId) || null;
  }

  public updateSessionId(rawSessionId, newSessionId){
    const conn = this.connections.get(rawSessionId);

    if(!conn) {
      return false;
    }

    this.connections.delete(rawSessionId);
    conn.setSessionId(newSessionId);
    this.connections.set(newSessionId, conn);
    return true;
  }
}
