# 設計模式落點對照 (Design Pattern Map)

本系統實作的所有設計模式 / 架構模式,以及它們在程式碼裡的具體位置。
demo 時可以拿這份對照表回答「這個 pattern 在哪一行?」的問題。

---

## 1. State Pattern (★ 核心)

**問題**: 座位狀態變化複雜,直接用 `if/else` 或 `switch` 會導致 SeatService 內條件分支爆炸,
每加一個新狀態就要動到既有所有分支,違反 Open/Closed Principle。

**解法**: 把每個狀態抽出成獨立類別,每個類別只實作自己合法的轉移。

| 角色 | 類別 / 檔案 |
|------|-------------|
| Context | `domain/Seat.java` (持有 `state` 欄位,所有 action 都委派給它) |
| State 抽象 | `domain/state/SeatState.java` (interface,default 方法丟例外) |
| Concrete States | `IdleState.java`, `ReservedState.java`, `OccupiedState.java`, `TempAwayState.java` |
| Singleton 持有器 | `domain/state/SeatStates.java` (Flyweight,共享無狀態的 state 物件) |
| 例外 | `exception/IllegalStateTransitionException.java` (→ HTTP 409) |

**驗證測試**: `SeatStateTransitionTest.java` 共 10 條。

**關鍵程式碼**:
```java
// Seat.java —— Context 委派
public synchronized void checkIn(String userId) {
    this.state = state.checkIn(this, userId);
}

// IdleState.java —— 只實作自己合法的轉移
@Override
public SeatState checkIn(Seat seat, String userId) {
    seat.setCurrentUserId(userId);
    seat.setSessionStart(Instant.now());
    return SeatStates.OCCUPIED;     // 回傳新狀態,Context 自動切換
}

// 其他不合法的方法 (reserve、leaveTemporarily…) 走 interface 的 default,
// 會自動丟 IllegalStateTransitionException → REST 回 409 Conflict
```

**擴充示範**: 想加「清潔中 (CLEANING)」狀態? 只要新增一個 `CleaningState.java`,
在 `SeatStates.java` 加一行常數,改 `Idle` 的 `release()` 讓它在某些條件下回到 `CLEANING`
(例如使用滿 4 小時)。**不需要修改任何 service 或 controller**。

---

## 2. Observer Pattern (★ 核心)

**問題**: 噪音超標時要通知多個對象 (管理員、數位看板、行動 App、未來的 LINE Notify),
而且未來會繼續增加新通道。如果通知邏輯寫死在 `NoiseMonitor` 裡,
每加一個通道就要改它,並讓它依賴新的服務。

**解法**: 抽出 `AlertObserver` 介面,讓 NoiseMonitor 只認介面,實際通道各自實作。

| 角色 | 類別 / 檔案 |
|------|-------------|
| Subject | `observer/NoiseMonitor.java` (持有 `List<AlertObserver>`) |
| Observer 抽象 | `observer/AlertObserver.java` (interface) |
| Event 值物件 | `observer/AlertEvent.java` (record) |
| Concrete Observer 1 | `observer/AdminPushChannel.java` (推播給管理員) |
| Concrete Observer 2 | `observer/DigitalSignageChannel.java` (數位看板) |

**Spring 自動連線**: 兩個 Concrete Observer 都標 `@Component`,
constructor 注入 `NoiseMonitor` 並在裡面 `monitor.register(this)`。
啟動時 Spring 會自動把它們掛上去。

**驗證測試**: `NoiseObserverTest.java` 共 5 條,特別驗證 **edge-trigger** 行為:
噪音持續超標時不會重複發警示,只有跨越門檻的瞬間才觸發。

**關鍵程式碼**:
```java
// NoiseMonitor.java —— edge-triggered
public void recordNoise(String zoneId, double dB) {
    AlertEvent event = null;
    synchronized (this) {
        boolean wasActive = alertActive.getOrDefault(zoneId, false);
        boolean isActive  = dB > getThreshold(zoneId);
        if (isActive && !wasActive) {
            event = AlertEvent.noiseHigh(zoneId, dB, getThreshold(zoneId));
            alertActive.put(zoneId, true);
        } else if (!isActive && wasActive) {
            alertActive.put(zoneId, false);
        }
    }
    if (event != null) notifyAll(event);
}
```

**擴充示範**: 加 LINE Notify? 只要新增 `LineNotifyChannel.java` 實作 `AlertObserver`,
標上 `@Component`,在 constructor 註冊。**NoiseMonitor 一個字都不用改**。

---

## 3. Singleton Pattern

**位置**: `domain/state/SeatStates.java`

四個狀態物件 (`IDLE`, `RESERVED`, `OCCUPIED`, `TEMP_AWAY`) 都是 `public static final` 常數,
整個 JVM 只各有一個實例。因為狀態物件本身是無狀態的 (per-seat 的資料都放在 `Seat` Context 上),
所有座位可以安全共用同一份狀態物件 — 這也順便符合了 **Flyweight Pattern** 的核心精神。

Spring 預設的 bean scope 也是 singleton,所以 `NoiseMonitor`、各 Observer Channel、
所有 Service / Repository / Controller 在 Spring 容器內都是單例。

---

## 4. Repository Pattern

**位置**: `repository/SeatRepository.java`

把資料存取邏輯封裝在 Repository 後,Service 層完全不知道目前用的是
`ConcurrentHashMap` (in-memory) 還是 PostgreSQL。
從 in-memory 改 JPA 只需要改這一個檔案,整個 Service 層零改動。

```java
// service/SeatService.java —— 完全不知道底層是什麼
public Seat get(String seatId) {
    return repository.findById(seatId)
            .orElseThrow(() -> new SeatNotFoundException(seatId));
}
```

---

## 5. MVC (Model-View-Controller) 架構模式

| 角色 | 對應 |
|------|------|
| Model | `domain/*.java` + `repository/*.java` |
| View | `frontend/src/**/*.jsx` (React 元件) |
| Controller | `controller/*.java` (Spring `@RestController`) |

前端透過 REST 與後端 Controller 對話,Controller 把請求轉給 Service,
Service 操作 Domain 物件,結果以 DTO 回應。這是教科書式的 MVC,
跟 Unified Process 的 Use-Case → Domain Model → Design Model 推導鏈完美對齊。

---

## 6. DTO Pattern

**位置**: `dto/SeatDto.java`, `dto/ActionRequest.java`, `dto/NoiseRequest.java`

Domain 物件 (`Seat`) 和對外 API 形狀分離。理由:
- 不會把 internal `SeatState` 物件序列化成 JSON (它有方法,Jackson 會炸)
- API 演進時不影響 domain 模型
- 用 Java `record` 寫,不可變,程式碼超短

---

## 7. 設計原則總結 (對應 SRS 中提到的「OO 技術」)

| 原則 | 在哪展現 |
|------|---------|
| **Open/Closed** | 加新狀態 / 新通道都不用改既有程式碼 |
| **Liskov Substitution** | 任何 `SeatState` / `AlertObserver` 子類都可以替換進去 |
| **Interface Segregation** | `SeatState` 用 default 方法,只實作需要的;`AlertObserver` 介面只兩個方法 |
| **Dependency Inversion** | `NoiseMonitor` 依賴 `AlertObserver` 抽象,不依賴具體通道;Service 依賴 `Repository` 抽象 |
| **Single Responsibility** | 一個 State 類別只管一個狀態的轉移邏輯 |

---

## 對應期中報告 SDD 章節

| SDD 章節 | 程式碼 |
|---------|-------|
| 3.3.1 State Pattern | `domain/state/` |
| 3.3.2 Observer Pattern | `observer/` |
| 3.3.3 Singleton + Repository | `domain/state/SeatStates.java`, `repository/` |
| 3.3.4 MVC | `controller/` + 前端 components |
| 3.4 狀態機表 | `domain/state/*.java` 中各 state 的 override 對應 |
| 3.5 API 設計 | `controller/*.java` |
