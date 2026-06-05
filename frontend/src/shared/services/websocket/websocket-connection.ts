import { BehaviorSubject, Observable } from 'rxjs';

export interface WebSocketConnectionOptions {
  maxReconnectAttempts?: number;
  reconnectDelay?: number;
  heartbeatIntervalMs?: number;
  autoReconnect?: boolean;
  queryParams?: Record<string, string>;
  protocols?: string[]; // WebSocket sub-protocols for authentication
}

export interface WebSocketSessionConfig {
  sessionId: string;
  sessionMode: 'new' | 'resume';
}

export interface WebSocketSessionState {
  sessionId: string;
  isConnected: boolean;
  lastActivity: number;
  reconnectAttempts: number;
}

export interface IWebSocketBaseMessage {
  type: string;
  [propName: string]: any;
}

export interface IWebSocketMessage extends IWebSocketBaseMessage {
  timestamp: number | string;
}

/**
 * WebSocket 默认配置
 */
export const DEFAULT_WEBSOCKET_OPTIONS: Required<WebSocketConnectionOptions> = {
  maxReconnectAttempts: 10,
  reconnectDelay: 3000,
  heartbeatIntervalMs: 3000,
  autoReconnect: true,
  queryParams: {},
  protocols: [],
};

/**
 * Represents a single WebSocket connection for a specific session.
 *
 * Features:
 * - Session-bound connection
 * - Event-based message routing
 * - Heartbeat management
 * - Reconnection logic
 */
export class WebSocketConnection {
  // Identity
  private url: string;
  private sessionId: string;

  // WebSocket instance
  private ws: WebSocket | null = null;

  // Connection state
  private isConnected: boolean = false;
  private lastActivity: number = Date.now();

  // Reconnection settings
  private reconnectAttempts = 0;
  private maxReconnectAttempts = DEFAULT_WEBSOCKET_OPTIONS.maxReconnectAttempts;
  private reconnectDelay = DEFAULT_WEBSOCKET_OPTIONS.reconnectDelay;
  private autoReconnect = true;
  private isReconnecting = false; // Flag to distinguish between initial connection and reconnection

  // Heartbeat settings
  private heartbeatInterval = null;
  private heartbeatIntervalMs = DEFAULT_WEBSOCKET_OPTIONS.heartbeatIntervalMs;

  // Event handlers (Map of eventType -> Set of handlers)
  private messageHandlers = new Map<string, Function[]>();

  private sessionConfig: WebSocketSessionConfig | null = null;
  private stateSubject$: BehaviorSubject<WebSocketSessionState> =
    new BehaviorSubject(this.getState());
  private queryParams: Record<string, string> = {};
  private protocols: string[] = [];

  /**
   * Create a new WebSocketConnection
   */
  constructor(
    url: string,
    sessionId: string,
    options: WebSocketConnectionOptions = DEFAULT_WEBSOCKET_OPTIONS,
  ) {
    this.url = url;
    this.sessionId = sessionId;

    const {
      maxReconnectAttempts = DEFAULT_WEBSOCKET_OPTIONS.maxReconnectAttempts,
      reconnectDelay = DEFAULT_WEBSOCKET_OPTIONS.reconnectDelay,
      heartbeatIntervalMs = DEFAULT_WEBSOCKET_OPTIONS.heartbeatIntervalMs,
      autoReconnect = DEFAULT_WEBSOCKET_OPTIONS.autoReconnect,
      queryParams = {},
      protocols = [],
    } = options;

    this.maxReconnectAttempts = maxReconnectAttempts;
    this.reconnectDelay = reconnectDelay;
    this.heartbeatIntervalMs = heartbeatIntervalMs;
    this.autoReconnect = autoReconnect;
    this.queryParams = queryParams;
    this.protocols = protocols;

    this.stateSubject$.next(this.getState());
  }

  // ==================== Event System ====================

  /**
   * Register a message handler for a specific message type
   */
  on(messageType: string, handler: Function): void {
    console.log(`[WSConnection:${this.sessionId}] Registering handler for '${messageType}'`);
    if (!this.messageHandlers.has(messageType)) {
      this.messageHandlers.set(messageType, []);
    }
    this.messageHandlers.get(messageType).push(handler);
    console.log(`[WSConnection:${this.sessionId}] Handler registered for '${messageType}', total count: ${this.messageHandlers.get(messageType).length}`);
  }

  /**
   * Remove a message handler
   */
  off(messageType: string, handler: Function): void {
    if (!this.messageHandlers.has(messageType)) {
      return;
    }
    const handlers = this.messageHandlers.get(messageType);
    const index = handlers.indexOf(handler);
    if (index > -1) {
      handlers.splice(index, 1);
    }
  }

  /**
   * Emit an event to all registered handlers
   */
  emit(messageType: string, data: any): void {
    console.log(`[WSConnection:${this.sessionId}] emit called:`, {
      messageType,
      data,
      handlerCount: this.messageHandlers.get(messageType)?.length || 0,
    });

    const handlers = this.messageHandlers.get(messageType) || [];
    console.log(`[WSConnection:${this.sessionId}] handlers for ${messageType}:`, handlers);

    handlers.forEach((handler) => {
      try {
        console.log(`[WSConnection:${this.sessionId}] executing handler for ${messageType}`);
        handler(data);
      } catch (error) {
        console.error(
          `[WSConnection:${this.sessionId}] Handler error for ${messageType}:`,
          error,
        );
      }
    });

    console.log(`[WSConnection:${this.sessionId}] emit completed for ${messageType}`);
  }

  // ==================== Connection Lifecycle ====================

  /**
   * Build WebSocket URL with query parameters
   */
  private buildWebSocketUrl(): string {
    if (!this.queryParams || Object.keys(this.queryParams).length === 0) {
      return this.url;
    }

    const queryString = new URLSearchParams(this.queryParams).toString();
    const separator = this.url.includes('?') ? '&' : '?';
    return `${this.url}${separator}${queryString}`;
  }

  /**
   * Connect to WebSocket server
   */
  connect(config: WebSocketSessionConfig | null = null): Promise<void> {
    return new Promise((resolve, reject) => {
      console.log(`[WSConnection:${this.sessionId}] connect() called`);
      console.log(`[WSConnection:${this.sessionId}] Current messageHandlers:`, {
        error: this.messageHandlers.get('error')?.length || 0,
        close: this.messageHandlers.get('close')?.length || 0,
        message: this.messageHandlers.get('message')?.length || 0,
        status: this.messageHandlers.get('status')?.length || 0,
      });


      if (config) {
        this.sessionConfig = config;
      }

      const wsUrl = this.buildWebSocketUrl();
      console.log(`[WSConnection:${this.sessionId}] Connecting to ${wsUrl}`);

      try {
        // Use protocols if provided, otherwise fallback to query parameters
        if (this.protocols && this.protocols.length > 0) {
          console.log(
            `[WSConnection:${this.sessionId}] Using WebSocket protocols for authentication`,
          );
          this.ws = new WebSocket(wsUrl, this.protocols);
        } else {
          this.ws = new WebSocket(wsUrl);
        }
      } catch (error) {
        console.error(
          `[WSConnection:${this.sessionId}] Failed to create WebSocket:`,
          error,
        );
        reject(error);
        return;
      }

      this.ws.onopen = () => {
        console.log(`[WSConnection:${this.sessionId}] Connected`);
        this.isConnected = true;

        // Only reset reconnectAttempts if this is NOT a reconnection attempt
        // This prevents infinite reconnection loops when connection is unstable
        if (!this.isReconnecting) {
          this.reconnectAttempts = 0;
          console.log(
            `[WSConnection:${this.sessionId}] Initial connection successful, reset reconnectAttempts to 0`,
          );
        } else {
          console.log(
            `[WSConnection:${this.sessionId}] Reconnection successful (attempt ${this.reconnectAttempts}/${this.maxReconnectAttempts}), keeping reconnectAttempts counter`,
          );
        }

        this.lastActivity = Date.now();

        this.emit('status', {
          connected: true,
          text: 'Connected',
          sessionId: this.sessionId,
        });

        this.startHeartbeat();
        this.stateSubject$.next(this.getState());

        resolve();
      };

      this.ws.onmessage = (event) => {
        this.lastActivity = Date.now();
        this.stateSubject$.next(this.getState());

        try {
          const message = JSON.parse(event.data);

          // ✅ CRITICAL FIX: Tag message with _sessionId BEFORE any handlers
          // This ensures routing works even if handlers are cleared and re-registered
          // The sessionId is the connection's bound sessionId (set during creation)
          if (!message._sessionId) {
            message._sessionId = this.sessionId;
          }

          // Emit to generic 'message' handlers
          this.emit('message', message);

          // Emit to type-specific handlers
          this.emit(message.type, message);
        } catch (error) {
          console.error(
            `[WSConnection:${this.sessionId}] Failed to parse message:`,
            error,
          );
        }
      };

      this.ws.onerror = (error) => {
        console.error(`[WSConnection:${this.sessionId}] Error:`, error);
        this.emit('status', {
          connected: false,
          text: 'Error',
          sessionId: this.sessionId,
        });
        this.emit('error', { error, sessionId: this.sessionId });
      };

      this.ws.onclose = (event) => {
        console.log(
          `[WSConnection:${this.sessionId}] Disconnected, code: ${event.code}, reason: ${event.reason}`,
        );
        this.isConnected = false;
        this.stopHeartbeat();

        this.emit('status', {
          connected: false,
          text: 'Disconnected',
          sessionId: this.sessionId,
        });

        console.log(`[WSConnection:${this.sessionId}] About to emit 'close' event`);
        this.emit('close', {
          code: event.code,
          reason: event.reason,
          sessionId: this.sessionId,
        });
        console.log(`[WSConnection:${this.sessionId}] Finished emitting 'close' event`);

        this.stateSubject$.next(this.getState());

        // Auto-reconnect if enabled
        if (this.autoReconnect && this.maxReconnectAttempts > 0) {
          this.attemptReconnect();
        }
      };
    });
  }

  /**
   * Disconnect without auto-reconnect
   */
  disconnect(): Promise<void> {
    return new Promise((resolve) => {
      console.log(`[WSConnection:${this.sessionId}] Disconnecting...`);

      if (!this.ws) {
        console.log(
          `[WSConnection:${this.sessionId}] No WebSocket to disconnect`,
        );
        resolve();
        return;
      }

      // Disable auto-reconnect before closing
      const previousAutoReconnect = this.autoReconnect;
      this.autoReconnect = false;

      // Set up one-time close handler
      const onClose = () => {
        console.log(
          `[WSConnection:${this.sessionId}] Disconnected successfully`,
        );
        this.ws = null;
        this.isConnected = false;
        this.stateSubject$.next(this.getState());

        // ✅ CRITICAL FIX: Emit status event to notify App
        // This ensures App.isConnected is updated when connection is closed
        this.emit('status', {
          connected: false,
          text: 'Disconnected',
          sessionId: this.sessionId,
        });

        // Restore auto-reconnect setting after disconnect completes
        setTimeout(() => {
          this.autoReconnect = previousAutoReconnect;
        }, 100);

        resolve();
      };

      this.ws.addEventListener('close', onClose, { once: true });
      this.stopHeartbeat();
      this.ws.close();
    });
  }

  /**
   * Close the connection immediately (no promise, no cleanup wait)
   */
  close() {
    this.autoReconnect = false;
    this.stopHeartbeat();

    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }

    this.isConnected = false;
    this.stateSubject$.next(this.getState());

    // ✅ CRITICAL FIX: Emit status event to notify App
    // This ensures App.isConnected is updated when connection is closed
    this.emit('status', {
      connected: false,
      text: 'Disconnected',
      sessionId: this.sessionId,
    });
  }

  /**
   * Attempt to reconnect with exponential backoff
   */
  attemptReconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error(
        `[WSConnection:${this.sessionId}] Max reconnection attempts reached (${this.maxReconnectAttempts})`,
      );
      this.emit('status', {
        connected: false,
        text: 'Connection Lost',
        sessionId: this.sessionId,
      });
      return;
    }

    // Mark this as a reconnection attempt
    this.isReconnecting = true;
    this.reconnectAttempts++;
    const delay =
      this.reconnectDelay * Math.pow(1.5, this.reconnectAttempts - 1);

    this.stateSubject$.next(this.getState());
    console.log(
      `[WSConnection:${this.sessionId}] Reconnecting in ${delay}ms (attempt ${this.reconnectAttempts}/${this.maxReconnectAttempts})`,
    );

    setTimeout(() => {
      if (this.autoReconnect) {
        console.log(`[WSConnection:${this.sessionId}] Calling connect() for reconnection`);
        console.log(`[WSConnection:${this.sessionId}] Current messageHandlers before reconnection:`, {
          error: this.messageHandlers.get('error')?.length || 0,
          close: this.messageHandlers.get('close')?.length || 0,
          message: this.messageHandlers.get('message')?.length || 0,
          status: this.messageHandlers.get('status')?.length || 0,
        });
        this.connect(this.sessionConfig);
      }
    }, delay);
  }

  // ==================== Message Sending ====================

  /**
   * Send a message through WebSocket
   */
  send(data: IWebSocketMessage): boolean {
    if (!this.isConnected || !this.ws) {
      console.warn(
        `[WSConnection:${this.sessionId}] Not connected, cannot send message`,
      );
      return false;
    }

    try {
      this.ws.send(JSON.stringify(data));
      this.lastActivity = Date.now();
      this.stateSubject$.next(this.getState());
      return true;
    } catch (error) {
      console.error(
        `[WSConnection:${this.sessionId}] Failed to send message:`,
        error,
      );
      return false;
    }
  }

  // ==================== Heartbeat Management ====================

  /**
   * Start heartbeat timer
   */
  startHeartbeat() {
    if (this.heartbeatInterval) {
      return; // Already running
    }

    this.heartbeatInterval = setInterval(() => {
      if (
        this.isConnected &&
        this.ws &&
        this.ws.readyState === WebSocket.OPEN
      ) {
        this.send({ type: 'heartbeat', timestamp: new Date().getTime() });
      }
    }, this.heartbeatIntervalMs);

    console.log(
      `[WSConnection:${this.sessionId}] Heartbeat started (${this.heartbeatIntervalMs}ms)`,
    );
  }

  /**
   * Stop heartbeat timer
   */
  stopHeartbeat() {
    if (this.heartbeatInterval) {
      clearInterval(this.heartbeatInterval);
      this.heartbeatInterval = null;
      console.log(`[WSConnection:${this.sessionId}] Heartbeat stopped`);
    }
  }

  // ==================== Utility Methods ====================
  /**
   * Get connection state summary
   */
  getState(): WebSocketSessionState {
    return {
      sessionId: this.sessionId,
      isConnected: this.isConnected,
      lastActivity: this.lastActivity,
      reconnectAttempts: this.reconnectAttempts,
    };
  }

  getStateObservable(): Observable<WebSocketSessionState> {
    return this.stateSubject$.asObservable();
  }

  setSessionId(sessionId):void {
    this.sessionId = sessionId;
    this.stateSubject$.next(this.getState());
  }
}
