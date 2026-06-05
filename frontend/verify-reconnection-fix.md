# WebSocket 重连修复验证

## 问题描述
WebSocket 连接异常后无限重连，`maxReconnectAttempts` 配置没有生效。

## 问题根源
在 `onopen` 事件处理器中，每次连接成功都重置 `reconnectAttempts = 0`，导致重连计数器永远不会达到 `maxReconnectAttempts`。

## 解决方案
添加 `isReconnecting` 标志来区分初始连接和重连：
- 在 `connect()` 开始时设置 `isReconnecting = false`
- 在 `attemptReconnect()` 开始时设置 `isReconnecting = true`
- 在 `onopen` 中，只在 `!isReconnecting` 时重置 `reconnectAttempts`

## 修改内容

### 1. 添加 `isReconnecting` 标志（第 71 行）
```typescript
private isReconnecting = false; // Flag to distinguish between initial connection and reconnection
```

### 2. 在 `connect()` 中重置标志（第 200-201 行）
```typescript
// Reset reconnection flag on new connection attempt
this.isReconnecting = false;
```

### 3. 修改 `onopen` 逻辑（第 233-244 行）
```typescript
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
```

### 4. 在 `attemptReconnect()` 中设置标志（第 418-419 行）
```typescript
// Mark this as a reconnection attempt
this.isReconnecting = true;
this.reconnectAttempts++;
```

## 测试场景

### 场景 1：初始连接成功
- 调用 `connect()` → `isReconnecting = false`
- 连接成功 → `onopen` 触发 → `reconnectAttempts = 0` ✅

### 场景 2：重连成功后不重置计数器
- 初始连接成功 → `reconnectAttempts = 0`
- 连接断开 → `attemptReconnect()` → `isReconnecting = true`, `reconnectAttempts = 1`
- 重连成功 → `onopen` 触发 → `reconnectAttempts` 保持为 1 ✅

### 场景 3：达到最大重连次数后停止
- 重连 3 次（maxReconnectAttempts = 3）→ `reconnectAttempts = 3`
- 第 4 次断开 → `attemptReconnect()` 检查 `reconnectAttempts >= maxReconnectAttempts` → 停止重连 ✅

### 场景 4：手动调用 connect() 重置计数器
- 重连 2 次 → `reconnectAttempts = 2`
- 手动调用 `connect()` → `isReconnecting = false`
- 连接成功 → `onopen` 触发 → `reconnectAttempts = 0` ✅

## 预期结果
- ✅ 不再无限重连
- ✅ 重连次数限制真正生效
- ✅ 初始连接正常工作
- ✅ 手动重连可以重置计数器
- ✅ 添加了详细的调试日志

## 测试文件
已创建测试文件：`src/shared/services/websocket/websocket-connection.spec.ts`

测试用例包括：
- 初始连接成功后重置计数器
- 重连成功后不重置计数器
- 达到最大重连次数后停止
- 手动调用 connect() 重置计数器
- 不稳定连接场景测试