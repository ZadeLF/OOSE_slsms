# SDD — 軟體設計文件 (Software Design Document)

> 智慧圖書館空間管理系統 (SLSMS) — Version 1.0
>
> 本文件由期中報告的 SDD 章節與 `docs/PATTERNS.md` 萃取、整理而成,並依「程式碼實際完成範圍」校正內容。
> 凡標註 **「V2 規劃中」** 的設計皆尚未在 Version 1.0 程式碼中實作。
> 詳細的 pattern 對應程式碼位置與關鍵程式碼片段,請參考 [`docs/PATTERNS.md`](./PATTERNS.md)。

## 目錄

1. [系統架構](#1-系統架構)
2. [領域模型 (Domain Model)](#2-領域模型-domain-model)
3. [設計模式應用 (Design Patterns)](#3-設計模式應用-design-patterns)
4. [座位狀態機 (Seat State Machine)](#4-座位狀態機-seat-state-machine)
5. [API 設計](#5-api-設計)

---

## 1. 系統架構

### 1.1 V1.0 實際架構

V1.0 採三層式 (3-Tier) 架構並結合 MVC 模式:

| 層次 | 技術選型(實際) | 主要職責 |
|---|---|---|
| 呈現層 (Presentation) | React 18 + Vite | 樓層圖渲染、座位操作面板、QR Code(`qrcode.react` 產生真實編碼)、噪音/溫度模擬器、報表檢視面板(Version 2.0) |
| 應用層 (Application) | Spring Boot 3 (Java 17) | REST API、State 轉換 (`domain/state/`)、Observer 通知 (`observer/`)、報表生成 (`report/`,Version 2.0) |
| 資料層 (Data) | **In-memory (`ConcurrentHashMap`)**,透過 `SeatRepository` 封裝 | 儲存 Floor / Zone / Seat 物件;**後端重啟即清空,無持久化** |
| 感測器輸入 | 前端「噪音模擬器」「溫度模擬器」直接呼叫 `POST /api/sensors/noise`、`POST /api/sensors/temperature` | 模擬感測器推送讀數,無實體感測器 / MQTT |
| 跨層服務 | 無(無認證、無 CI/CD) | — |

### 1.2 V2 規劃架構(尚未實作)

下表為期中報告原訂的目標架構,列為 Version 2.0 規劃方向,**V1.0 demo 不會展示這些元件**:

| 層次 | V2 規劃技術 | 說明 |
|---|---|---|
| 呈現層 | React + Tailwind, RWD | 強化行動裝置體驗 |
| 應用層 | Spring Boot 3 + JPA | 加入持久化 ORM |
| 資料層 | MySQL 8 + Redis 7 | MySQL 持久化使用紀錄;Redis 快取即時座位狀態 |
| 感測器層 | ESP32 + MQTT Broker | 真實感測器透過 Sensor Gateway 推送資料 |
| 跨層服務 | JWT Auth, Logging, CI/CD (GitHub Actions) | 安全性、可觀察性、自動化部署 |

---

## 2. 領域模型 (Domain Model)

### 2.1 V1.0 已實作類別

```
Floor 1 ──* Zone 1 ──* Seat 1 ──1 SeatState
                              └─ AlertEvent (透過 NoiseMonitor 產生)
```

| 類別 | 檔案位置 | 說明 |
|---|---|---|
| `Floor` | `domain/Floor.java` | 樓層,持有多個 `Zone` |
| `Zone` | `domain/Zone.java` | 區域,持有多個 `Seat` 及噪音門檻 (`noiseThresholdDb`) |
| `Seat` | `domain/Seat.java` | 座位,State Pattern 的 Context;持有 `state`、`currentUserId`、`reservationOwner`、`sessionStart`、`tempAwaySince`、`lastChange` |
| `SeatState` 及其實作 | `domain/state/*.java` | `IdleState`、`ReservedState`、`OccupiedState`、`TempAwayState` |
| `AlertEvent` | `observer/AlertEvent.java` | 環境警示事件(record),`type` 區分 `NOISE_HIGH` / `TEMP_HIGH` / `TEMP_LOW` |
| `NoiseMonitor` | `observer/NoiseMonitor.java` | Observer Pattern 的 Subject(噪音) |
| `TemperatureMonitor` | `observer/TemperatureMonitor.java` | Observer Pattern 的 Subject(溫度,雙邊界:上限/下限) |

### 2.2 與期中報告領域模型的差異(範圍縮減說明)

期中報告 3.2 節列出的 `Reservation`、`Reader`、`Admin`、`SensorReading`、`UsageRecord` 等類別,V1.0 **未獨立建模**,處理方式如下:

| 報告中的類別 | V1.0 實際狀況 |
|---|---|
| `Reservation` | 簡化為 `Seat` 物件上的屬性:`reservationOwner`(預約者)、`sessionStart`(使用起始時間),未獨立成類別;但 `reservationOwner` / `currentUserId` 的歸屬驗證已在 `IdleState`、`ReservedState`、`OccupiedState`、`TempAwayState` 中一致實作(見第 4 節) |
| `Reader` / `Admin` | 🔲 V2 規劃中 — V1.0 無使用者帳號模型,前端以固定字串 `demo-user-001` 代表操作者,無角色區分 |
| `SensorReading` | 簡化為 `NoiseMonitor` 內部以 `Map<zoneId, latestDb>` 暫存最新讀數,未獨立成持久化實體 |
| `UsageRecord` | ✅ Version 2.0(簡化版)— 不另建持久化實體,改由 `report/SeatUsageReportStrategy`、`report/EnvironmentAlertReportStrategy` 即時讀取 `SeatRepository`、`NoiseMonitor`、`TemperatureMonitor`、`AdminPushChannel` 的記憶體狀態並彙整為 `SeatUsageReport` / `EnvironmentAlertReport`(record) |

---

## 3. 設計模式應用 (Design Patterns)

> 完整程式碼片段與擴充示範請見 [`docs/PATTERNS.md`](./PATTERNS.md)。本節僅列出各模式的角色對應與完成狀態。

### 3.1 State Pattern — 座位動態狀態追蹤 ✅ 已完成(★ 核心)

| 角色 | 類別 / 檔案 |
|---|---|
| Context | `domain/Seat.java` |
| State 抽象 | `domain/state/SeatState.java`(interface,default 方法丟 `IllegalStateTransitionException`) |
| Concrete States | `IdleState`、`ReservedState`、`OccupiedState`、`TempAwayState` |
| Flyweight / Singleton 持有器 | `domain/state/SeatStates.java` |

驗證測試:`SeatStateTransitionTest.java`,共 14 條,全綠。

### 3.2 Observer Pattern — 噪音 / 溫度環境警示 ✅ 已完成(★ 核心;Version 2.0 擴充溫度感測)

| 角色 | 類別 / 檔案 |
|---|---|
| Subject 1(噪音) | `observer/NoiseMonitor.java` |
| Subject 2(溫度,Version 2.0 新增) | `observer/TemperatureMonitor.java` |
| Observer 抽象 | `observer/AlertObserver.java` |
| Event 值物件 | `observer/AlertEvent.java`(record,`type` ∈ `NOISE_HIGH` \| `TEMP_HIGH` \| `TEMP_LOW`) |
| Concrete Observer 1 | `observer/AdminPushChannel.java` |
| Concrete Observer 2 | `observer/DigitalSignageChannel.java` |

`AdminPushChannel`、`DigitalSignageChannel` 在建構子中同時向 `NoiseMonitor` 與 `TemperatureMonitor` 註冊(`register(this)`),不需新增任何 Observer 類別即可同時接收兩種感測器事件 —— 展示 Observer 架構對「擴充新感測器」開放、對既有 Observer 介面封閉(Open/Closed Principle)。`TemperatureMonitor` 採雙邊界邊緣觸發:溫度高於上限觸發 `TEMP_HIGH`,低於下限觸發 `TEMP_LOW`,回到範圍內才會重置觸發狀態,避免警示連續重複發送。

驗證測試:`NoiseObserverTest.java`(5 條)+ `TemperatureObserverTest.java`(7 條,含跨越上/下限觸發、邊界不重複觸發、回到範圍內再次觸發、多 observer 皆收到、噪音與溫度監測器互不影響等情境),全綠。

### 3.3 Singleton + Flyweight Pattern ✅ 已完成

`domain/state/SeatStates.java` 中 `IDLE`、`RESERVED`、`OCCUPIED`、`TEMP_AWAY` 為 `public static final` 常數,JVM 內共享單一實例。Spring 的 `NoiseMonitor`、各 Observer Channel、Service / Repository / Controller 預設皆為 singleton bean。

> 期中報告 3.3.3 提到的 `ConfigurationManager` Singleton 未獨立實作,噪音門檻目前由 `Zone.noiseThresholdDb` 屬性與 `NoiseMonitor` 持有,功能等價但未抽出獨立類別。

### 3.4 Repository Pattern ✅ 已完成(in-memory 版)

`repository/SeatRepository.java` 封裝資料存取,Service 層不直接接觸底層儲存結構。

> 期中報告中規劃「從 in-memory 改為 Spring Data JPA 只需改這一個檔案」— **JPA/MySQL 版本為 V2 規劃中**,V1.0 底層為 `ConcurrentHashMap`。

### 3.5 MVC (Model-View-Controller) ✅ 已完成

| 角色 | 對應 |
|---|---|
| Model | `domain/*.java`、`repository/*.java` |
| View | `frontend/src/**/*.jsx` |
| Controller | `controller/*.java` (`SeatController`、`SensorController`、`AlertController`) |

### 3.6 DTO Pattern ✅ 已完成

`dto/SeatDto.java`、`dto/ActionRequest.java`、`dto/NoiseRequest.java` — 以 Java `record` 實作,將 Domain 物件與對外 API 形狀分離。

### 3.7 Strategy Pattern — 報表生成 ✅ 已完成(Version 2.0)

期中報告 3.3.4 規劃以 `IReportStrategy` 處理多種使用分析方式(熱點分析、時段分析等),對應 UC-09。Version 2.0 以 `ReportStrategy` 介面實作此規劃:

| 角色 | 類別 / 檔案 |
|---|---|
| Strategy 介面 | `report/ReportStrategy.java`(`type()`、`title()`、`generate()`) |
| Concrete Strategy 1 | `report/SeatUsageReportStrategy.java`(`type = "seat-usage"`)— 座位使用率報表,回傳 `SeatUsageReport`(整體與各分區的 `countByState`、`occupancyRate`,內含 `ZoneUsage`) |
| Concrete Strategy 2 | `report/EnvironmentAlertReportStrategy.java`(`type = "environment-alerts"`)— 環境警示熱點報表,回傳 `EnvironmentAlertReport`(各分區噪音/溫度最新讀數、門檻、警示狀態與近期警示次數,內含 `ZoneEnvironment`) |
| Context / 派送者 | `report/ReportService.java` — Spring 注入 `List<ReportStrategy>`,以 `type()` 為 key 建立 map;`generate(type)` 派送至對應策略,未知 type 丟 `IllegalArgumentException`(對應 HTTP 400) |
| Controller | `controller/ReportController.java` — `GET /api/reports` 列出可用報表類型;`GET /api/reports/{type}` 產生報表 |

資料來源皆為現有記憶體狀態(`SeatRepository`、`NoiseMonitor`、`TemperatureMonitor`、`AdminPushChannel.recent()`),**不引入 JPA/資料庫**,持久化版本仍列為 V2 後續規劃。新增報表型態時,只需新增一個 `@Component` 實作 `ReportStrategy`,`ReportService`、`ReportController` 與呼叫端皆不需修改(Open/Closed Principle)。

驗證測試:`SeatUsageReportStrategyTest`(2 條)、`EnvironmentAlertReportStrategyTest`(2 條)、`ReportServiceTest`(4 條,驗證策略可替換/多型與未知 type 例外),共 8 條,全綠。

### 3.8 設計原則總結

| 原則 | 在哪展現 |
|---|---|
| Open/Closed | 加新狀態(State)/新通道(Observer)都不用改既有程式碼 |
| Liskov Substitution | 任何 `SeatState` / `AlertObserver` 子類都可以替換進去 |
| Interface Segregation | `SeatState` 用 default 方法,只實作需要的;`AlertObserver` 介面只兩個方法 |
| Dependency Inversion | `NoiseMonitor` 依賴 `AlertObserver` 抽象;`SeatService` 依賴 `SeatRepository` 抽象 |
| Single Responsibility | 一個 State 類別只管一個狀態的轉移邏輯 |

---

## 4. 座位狀態機 (Seat State Machine)

下表為座位狀態轉移圖。每一格表示「在當前狀態下接收某事件後」會轉移到的狀態,「—」表示非法轉移、系統將回傳 **409 Conflict**。

| 當前狀態 \ 事件 | `checkIn()` | `reserve()` | `leaveTemp()` | `comeBack()` | `release()` | `timeout()` |
|---|---|---|---|---|---|---|
| **Idle (空閒)** | Occupied | Reserved | — | — | — | — |
| **Reserved (已預約)** | Occupied (本人) | — | — | — | Idle | Idle |
| **Occupied (使用中)** | — | — | TempAway | — | Idle | — |
| **TempAway (暫時離開)** | — | — | — | Occupied | Idle | Idle |

✅ 已驗證:所有上述轉移與非法轉移(回傳 409)皆由 `SeatStateTransitionTest.java`(14 條)覆蓋,全綠。

> ✅ **使用者歸屬驗證**:`release()`、`leaveTemp()`(Occupied)與 `comeBack()`、`release()`(TempAway)皆會檢查呼叫者 `userId` 是否等於 `seat.getCurrentUserId()`,不符則丟出 `IllegalStateTransitionException`(回傳 409),確保座位使用中/暫離期間不會被非本人操作。對應測試:`occupied_release_by_other_user_rejected`、`occupied_leave_temp_by_other_user_rejected`、`temp_away_come_back_by_other_user_rejected`、`temp_away_release_by_other_user_rejected`。此驗證機制與 `ReservedState.checkIn()` 對 `reservationOwner` 的檢查一致,共同構成簡化版的「使用者歸屬」概念(對應 2.2 節 `Reservation`/`Reader` 範圍縮減說明)。`timeout()` 為系統/排程觸發,不帶 `userId`,不受此限制。

> ⚠️ **排程說明**:`timeout()` 轉移(對應 FR-03 預約逾時、FR-05 暫離逾時)在 domain 層已正確實作並通過單元測試(TC-05、TC-07),
> 但 V1.0 **沒有背景排程器 (scheduler) 自動呼叫 `timeout()`**——也就是說,V1.0 demo 中座位不會「自動」逾時釋放,
> 需由程式呼叫對應方法觸發。**自動排程觸發機制列為 V2 規劃項目。**

---

## 5. API 設計

下表為 **V1.0 實際已實作並可呼叫**的 REST API,均為公開端點(無權限驗證,V2 規劃中)。

| Method | Endpoint | 說明 | 對應 Controller |
|---|---|---|---|
| GET | `/api/floors/{floorId}/seats` | 取得指定樓層所有座位狀態 | `SeatController` |
| GET | `/api/zones/{zoneId}/seats` | 取得指定區域所有座位狀態 | `SeatController` |
| GET | `/api/seats/{seatId}` | 取得單一座位狀態 | `SeatController` |
| POST | `/api/seats/{seatId}/reserve` | 即時預約座位(body: `{userId}`) | `SeatController` |
| POST | `/api/seats/{seatId}/checkin` | QR Code 報到(body: `{userId}`) | `SeatController` |
| POST | `/api/seats/{seatId}/leave-temp` | 切換為暫時離開(body: `{userId}`) | `SeatController` |
| POST | `/api/seats/{seatId}/come-back` | 從暫離恢復為使用中(body: `{userId}`) | `SeatController` |
| POST | `/api/seats/{seatId}/release` | 釋放座位(body: `{userId}`) | `SeatController` |
| POST | `/api/sensors/noise` | 推送噪音讀數(body: `{zoneId, dB}`),邊緣觸發警示 | `SensorController` |
| GET | `/api/sensors/noise/{zoneId}` | 查詢區域最新噪音值、門檻與警示狀態 | `SensorController` |
| POST | `/api/sensors/temperature` | 推送溫度讀數(body: `{zoneId, celsius}`),邊緣觸發警示(Version 2.0 新增) | `SensorController` |
| GET | `/api/sensors/temperature/{zoneId}` | 查詢區域最新溫度、上下限與警示狀態(Version 2.0 新增) | `SensorController` |
| GET | `/api/alerts/recent` | 取得近期警示列表(噪音 + 溫度) | `AlertController` |
| GET | `/api/alerts/signage/{zoneId}` | 取得該區數位看板目前顯示訊息 | `AlertController` |
| GET | `/api/reports` | 列出可用報表類型(Version 2.0 新增) | `ReportController` |
| GET | `/api/reports/{type}` | 產生指定類型的報表,`type` ∈ `seat-usage` \| `environment-alerts`(Version 2.0 新增,Strategy Pattern) | `ReportController` |

### V2 規劃中的 API(尚未實作)

| Method | Endpoint | 說明 |
|---|---|---|
| POST | `/api/seats/{id}/reservations` | 分時段預訂(對應 FR-04) |
| POST | `/api/admin/seats/{id}/force-release` | 管理員強制釋放座位(對應 FR-07) |
| GET | `/api/reports/{type}?format=csv` | 報表 CSV 匯出、每日排程產出(對應 FR-08 後續規劃) |

### 錯誤回應格式(已實作,`exception/GlobalExceptionHandler.java`)

| HTTP 狀態 | 觸發情境 | 回應格式範例 |
|---|---|---|
| 409 Conflict | `SeatState` 不允許的動作(`IllegalStateTransitionException`) | `{"status":409,"error":"IllegalStateTransition","message":"Action 'reserve' is not allowed in state 'OCCUPIED'"}` |
| 404 Not Found | 座位 ID 不存在(`SeatNotFoundException`) | `{"status":404,"error":"SeatNotFound","message":"..."}` |
| 400 Bad Request | 請求參數不合法(`IllegalArgumentException`) | `{"status":400,"error":"BadRequest","message":"..."}` |
