import { WebSocketConnection, DEFAULT_WEBSOCKET_OPTIONS } from './websocket-connection';

describe('WebSocketConnection - Reconnection Logic', () => {
  let mockWebSocket: jasmine.SpyObj<WebSocket>;
  let connection: WebSocketConnection;
  const testUrl = 'ws://localhost:8080';
  const testSessionId = 'test-session-123';

  beforeEach(() => {
    // Create a mock WebSocket
    mockWebSocket = jasmine.createSpyObj('WebSocket', [
      'addEventListener',
      'removeEventListener',
      'send',
      'close',
    ]);
    mockWebSocket.readyState = WebSocket.CONNECTING;

    // Spy on the global WebSocket constructor
    spyOn(window, 'WebSocket').and.returnValue(mockWebSocket as any);

    // Create connection with default options (maxReconnectAttempts = 5)
    connection = new WebSocketConnection(testUrl, testSessionId, {
      maxReconnectAttempts: 3, // Use 3 for faster testing
      reconnectDelay: 100, // Use 100ms for faster testing
      autoReconnect: true,
    });
  });

  afterEach(() => {
    // Clean up any pending timers
    jasmine.clock().uninstall();
    jasmine.clock().install();

    // Close connection if it's still open
    if (connection) {
      connection.close();
    }
  });

  describe('Initial Connection', () => {
    it('should reset reconnectAttempts to 0 on initial connection success', async () => {
      // Connect to the WebSocket
      const connectPromise = connection.connect();

      // Simulate WebSocket opening
      mockWebSocket.readyState = WebSocket.OPEN;
      const openHandler = mockWebSocket.addEventListener.calls
        .allArgs()
        .find(([event]) => event === 'open')?.[1];
      if (openHandler) {
        openHandler();
      }

      await connectPromise;

      // Verify reconnectAttempts is reset to 0
      const state = connection.getState();
      expect(state.reconnectAttempts).toBe(0);
    });

    it('should set isReconnecting to false on initial connection', async () => {
      // Connect to the WebSocket
      const connectPromise = connection.connect();

      // Simulate WebSocket opening
      mockWebSocket.readyState = WebSocket.OPEN;
      const openHandler = mockWebSocket.addEventListener.calls
        .allArgs()
        .find(([event]) => event === 'open')?.[1];
      if (openHandler) {
        openHandler();
      }

      await connectPromise;

      // Verify isReconnecting is false
      const state = connection.getState();
      expect(state.reconnectAttempts).toBe(0);
    });
  });

  describe('Reconnection Logic', () => {
    it('should NOT reset reconnectAttempts on reconnection success', async () => {
      jasmine.clock().install();

      // Initial connection
      let connectPromise = connection.connect();
      mockWebSocket.readyState = WebSocket.OPEN;
      let openHandler = mockWebSocket.addEventListener.calls
        .allArgs()
        .find(([event]) => event === 'open')?.[1];
      if (openHandler) {
        openHandler();
      }
      await connectPromise;

      // Verify initial state
      let state = connection.getState();
      expect(state.reconnectAttempts).toBe(0);

      // Simulate disconnection
      mockWebSocket.readyState = WebSocket.CLOSED;
      const closeHandler = mockWebSocket.addEventListener.calls
        .allArgs()
        .find(([event]) => event === 'close')?.[1];
      if (closeHandler) {
        closeHandler({ code: 1006, reason: 'Abnormal closure' });
      }

      // Wait for reconnection delay
      jasmine.clock().tick(100);

      // Simulate reconnection success
      mockWebSocket.readyState = WebSocket.OPEN;
      openHandler = mockWebSocket.addEventListener.calls
        .allArgs()
        .find(([event]) => event === 'open')?.[1];
      if (openHandler) {
        openHandler();
      }

      // Verify reconnectAttempts is NOT reset (should be 1)
      state = connection.getState();
      expect(state.reconnectAttempts).toBe(1);

      jasmine.clock().uninstall();
    });

    it('should stop reconnection after maxReconnectAttempts is reached', async () => {
      jasmine.clock().install();

      // Initial connection
      let connectPromise = connection.connect();
      mockWebSocket.readyState = WebSocket.OPEN;
      let openHandler = mockWebSocket.addEventListener.calls
        .allArgs()
        .find(([event]) => event === 'open')?.[1];
      if (openHandler) {
        openHandler();
      }
      await connectPromise;

      // Simulate 3 disconnections (maxReconnectAttempts = 3)
      for (let i = 1; i <= 3; i++) {
        // Disconnect
        mockWebSocket.readyState = WebSocket.CLOSED;
        const closeHandler = mockWebSocket.addEventListener.calls
          .allArgs()
          .find(([event]) => event === 'close')?.[1];
        if (closeHandler) {
          closeHandler({ code: 1006, reason: 'Abnormal closure' });
        }

        // Wait for reconnection delay
        jasmine.clock().tick(100 * Math.pow(1.5, i - 1));

        // Simulate reconnection success
        mockWebSocket.readyState = WebSocket.OPEN;
        openHandler = mockWebSocket.addEventListener.calls
          .allArgs()
          .find(([event]) => event === 'open')?.[1];
        if (openHandler) {
          openHandler();
        }

        // Verify reconnectAttempts increments
        const state = connection.getState();
        expect(state.reconnectAttempts).toBe(i);
      }

      // Simulate 4th disconnection
      mockWebSocket.readyState = WebSocket.CLOSED;
      const closeHandler = mockWebSocket.addEventListener.calls
        .allArgs()
        .find(([event]) => event === 'close')?.[1];
      if (closeHandler) {
        closeHandler({ code: 1006, reason: 'Abnormal closure' });
      }

      // Wait for reconnection delay (should not reconnect)
      jasmine.clock().tick(100 * Math.pow(1.5, 3));

      // Verify no new reconnection attempt was made
      const state = connection.getState();
      expect(state.reconnectAttempts).toBe(3); // Should stay at 3

      jasmine.clock().uninstall();
    });

    it('should reset reconnectAttempts when manually calling connect()', async () => {
      jasmine.clock().install();

      // Initial connection
      let connectPromise = connection.connect();
      mockWebSocket.readyState = WebSocket.OPEN;
      let openHandler = mockWebSocket.addEventListener.calls
        .allArgs()
        .find(([event]) => event === 'open')?.[1];
      if (openHandler) {
        openHandler();
      }
      await connectPromise;

      // Simulate disconnection and reconnection
      mockWebSocket.readyState = WebSocket.CLOSED;
      let closeHandler = mockWebSocket.addEventListener.calls
        .allArgs()
        .find(([event]) => event === 'close')?.[1];
      if (closeHandler) {
        closeHandler({ code: 1006, reason: 'Abnormal closure' });
      }

      jasmine.clock().tick(100);

      mockWebSocket.readyState = WebSocket.OPEN;
      openHandler = mockWebSocket.addEventListener.calls
        .allArgs()
        .find(([event]) => event === 'open')?.[1];
      if (openHandler) {
        openHandler();
      }

      // Verify reconnectAttempts is 1
      let state = connection.getState();
      expect(state.reconnectAttempts).toBe(1);

      // Manually call connect() again
      connectPromise = connection.connect();
      mockWebSocket.readyState = WebSocket.OPEN;
      openHandler = mockWebSocket.addEventListener.calls
        .allArgs()
        .find(([event]) => event === 'open')?.[1];
      if (openHandler) {
        openHandler();
      }
      await connectPromise;

      // Verify reconnectAttempts is reset to 0
      state = connection.getState();
      expect(state.reconnectAttempts).toBe(0);

      jasmine.clock().uninstall();
    });
  });

  describe('Unstable Connection Scenario', () => {
    it('should prevent infinite reconnection loop with unstable connection', async () => {
      jasmine.clock().install();

      // Initial connection
      let connectPromise = connection.connect();
      mockWebSocket.readyState = WebSocket.OPEN;
      let openHandler = mockWebSocket.addEventListener.calls
        .allArgs()
        .find(([event]) => event === 'open')?.[1];
      if (openHandler) {
        openHandler();
      }
      await connectPromise;

      // Simulate unstable connection: connect, disconnect, connect, disconnect, ...
      for (let i = 1; i <= 5; i++) {
        // Disconnect
        mockWebSocket.readyState = WebSocket.CLOSED;
        const closeHandler = mockWebSocket.addEventListener.calls
          .allArgs()
          .find(([event]) => event === 'close')?.[1];
        if (closeHandler) {
          closeHandler({ code: 1006, reason: 'Abnormal closure' });
        }

        // Wait for reconnection delay
        jasmine.clock().tick(100 * Math.pow(1.5, i - 1));

        // Simulate reconnection success
        mockWebSocket.readyState = WebSocket.OPEN;
        openHandler = mockWebSocket.addEventListener.calls
          .allArgs()
          .find(([event]) => event === 'open')?.[1];
        if (openHandler) {
          openHandler();
        }

        // Verify reconnectAttempts increments and doesn't reset
        const state = connection.getState();
        expect(state.reconnectAttempts).toBe(i);
      }

      // After 3 attempts, should stop reconnecting
      const finalState = connection.getState();
      expect(finalState.reconnectAttempts).toBe(3);

      jasmine.clock().uninstall();
    });
  });
});