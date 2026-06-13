# SLSMS · Smart Library Space Management System

期末專案 Demo · 物件導向軟體工程
Version 1.0 (Unified Process) + Version 2.0 (Scrum, Sprint 1-2)

一套用 Spring Boot 3 (Java 17) + React 18 (Vite) 打造的圖書館空間管理 Demo,
凸顯三個核心設計模式的實作:
- **State Pattern**(座位狀態機,含「僅本人可操作」的歸屬驗證)
- **Observer Pattern**(噪音警示推播,Version 2.0 擴充為同時監測噪音與溫度)
- **Strategy Pattern**(Version 2.0 新增的使用率/環境警示報表,可替換報表型態)

---

## 系統需求

| 項目 | 版本 |
|------|------|
| JDK | 17 或更新 |
| Maven | 3.8+ (或使用 IDE 內建) |
| Node.js | 18+ |
| npm | 9+ |

---

## 快速啟動

### 路徑 A:一鍵啟動腳本 (推薦)

**macOS / Linux**:
```bash
chmod +x start.sh
./start.sh
```

**Windows**: 雙擊 `start.bat` (會自動開兩個終端機)

腳本會同時啟動 Spring Boot (`:8080`) 和 Vite (`:5173`),
首次跑會自動 `npm install`。等 console 看到 Vite 印出 `Local: http://localhost:5173`,
瀏覽器自動開啟。

### 路徑 B:手動啟動 (如果腳本失敗)

**第 1 步:啟動後端**

開一個終端機:
```bash
cd backend
mvn spring-boot:run
```

或者在 IntelliJ IDEA 裡直接右鍵 `SlsmsApplication.java` → Run。

後端會在 `http://localhost:8080` 啟動,並自動初始化:
- 1 樓層 (1F) / 1 區域 (閱覽 A 區,噪音門檻 65 dB)
- 12 座位 (A1~A4, B1~B4, C1~C4)

確認啟動成功:
```bash
curl http://localhost:8080/api/floors/1/seats
```

**第 2 步:啟動前端**

開新的終端機:
```bash
cd frontend
npm install        # 首次
npm run dev
```

瀏覽器會自動開啟 `http://localhost:5173`。Vite 已設定 `/api/**` proxy 到後端,
不會有 CORS 問題。

### Demo 操作流程

打開瀏覽器後,你可以:

1. **點選任一座位** → 下方面板出現可用動作
2. **點「預約」** → 座位變為「已預約」
3. **點「掃 QR Check-in」** → 出現 QR Code → 點「模擬掃描成功」 → 座位變為「使用中」
4. **點「暫時離開」** → 變為「暫離」
5. **點「回來」** → 回到「使用中」
6. **點「釋放」** → 回到「空閒」

如果做 **非法轉移** (例如對「已使用中」的座位點預約) — 後端會回傳 `409 Conflict`,
畫面顯示紅色錯誤條。這正是 State Pattern 在做的事:每個狀態類別只允許自己合法的轉移。

**歸屬驗證 (Ownership Validation, Version 1.0 新增)**:
- `release` / `leave-temp` / `come-back` 都會檢查 `userId` 是否為座位目前的使用者
- 用不同的 `userId` 對同一座位呼叫上述端點 → 回傳 `409 Conflict`,訊息含「seat is occupied by another user」

**Observer Pattern Demo (噪音 + 溫度,Version 2.0 擴充)**:
- 用噪音模擬器面板的 `+5 dB` / `+20 dB (突發)` 按鈕推升 dB
- 跨過 65 dB 門檻時 → 立刻看到紅色脈動警示條 (`pulsing`)
- 用溫度模擬器面板的 `+10°C (過熱)` / `−10°C (過冷)` 按鈕,跨過 18~28°C 範圍時同樣觸發脈動警示
- 同時頁面底部「近期警示」清單會新增對應紀錄 (`NOISE_HIGH` / `TEMP_HIGH` / `TEMP_LOW`),各自獨立、邊緣觸發(回到正常範圍後警示消失,不會重複新增)
- 後端 console 會印出 `[ADMIN_PUSH]` 與 `[DIGITAL_SIGNAGE]` 兩個觀察者各自處理的 log

**Strategy Pattern Demo (使用率報表,Version 2.0 新增)**:
- 在「使用率報表」面板的下拉選單切換「座位使用率報表」與「環境警示熱點報表(噪音/溫度)」
- 不需重新整理頁面,表格欄位與內容會隨報表類型即時改變
- 兩種報表分別對應 `SeatUsageReportStrategy` 與 `EnvironmentAlertReportStrategy`,皆實作 `ReportStrategy` 介面,由 `ReportService` 依 `type` 動態分派

---

## 跑單元測試

```bash
cd backend
mvn test
```

測試對應 SRS 的測試案例,共 6 個測試檔、34 條測試,全部通過:

| 測試檔 | Test 方法(節錄) | 驗證重點 |
|--------|------------------|---------|
| `SeatStateTransitionTest` | `idle_to_occupied_via_checkin` (TC-02) | Idle → checkIn → Occupied |
| | `occupied_cannot_checkin_again` (TC-03) | 已使用中無法再 Check-in (409) |
| | `reserved_seat_rejects_checkin_by_other_user` | 預約者驗證 |
| | `reserved_timeout_returns_to_idle` (TC-04) | 預約逾時自動釋放 |
| | `occupied_leave_temp_come_back_cycle` (TC-05) | 暫離→回來循環 |
| | 歸屬驗證相關測試 | release/leaveTemp/comeBack 非本人操作回 409 |
| `NoiseObserverTest` | `crossing_threshold_upward_triggers_alert` (TC-06) | 噪音超標推播 |
| | `edge_triggered_no_repeat_alerts_*` | 邊緣觸發,避免警示洪水 |
| | `multiple_observers_all_receive_alert` | 多觀察者同步通知 |
| `TemperatureObserverTest` | 過熱 / 過冷觸發、回到範圍內解除、邊緣觸發 | 沿用 Observer 架構擴充溫度監測 (Version 2.0) |
| `SeatUsageReportStrategyTest` | 整體與各分區座位數/狀態統計、使用率計算 | 座位使用率報表 (Strategy Pattern) |
| `EnvironmentAlertReportStrategyTest` | 噪音/溫度讀數、門檻/範圍、近期警示次數統計 | 環境警示熱點報表 (Strategy Pattern) |
| `ReportServiceTest` | 報表型態分派、未知型態丟例外 (400) | Strategy Pattern 分派器 |

> Demo 前也可以在後端已啟動 (`mvn spring-boot:run`) 的狀態下執行 `acceptance_api_test.ps1`(專案根目錄,PowerShell),
> 會依 `docs/AcceptanceChecklist.md` 逐項呼叫 API、檢查狀態碼與回應內容,
> 跑完印出每項 PASS/FAIL 對照表。

---

## 目錄結構

```
slsms/
├── README.md                         ← 你正在讀的檔案
├── acceptance_api_test.ps1           ← 後端 API 驗收自動化腳本 (對照 AcceptanceChecklist.md)
├── docs/
│   ├── PATTERNS.md                   ← 設計模式落點對照
│   ├── SRS.md / SDD.md               ← 軟體需求 / 設計文件
│   ├── UserManual.md                 ← 使用者手冊
│   ├── AcceptanceChecklist.md        ← FR-01~FR-08 驗收清單 (含 §10 設計模式驗收點)
│   ├── scrum/                        ← Version 2.0 Scrum 文件
│   │   ├── ProductBacklog.md
│   │   ├── Sprint1.md
│   │   └── Sprint2.md
│   └── uml/                          ← PlantUML 原始檔 + 渲染後 PNG
├── backend/                          ← Spring Boot 3 + Java 17
│   ├── pom.xml
│   └── src/main/java/com/oose/slsms/
│       ├── SlsmsApplication.java
│       ├── config/                   ← CORS, DataInitializer
│       ├── domain/
│       │   ├── Seat.java             ← State Pattern Context (含歸屬驗證)
│       │   ├── Zone.java
│       │   ├── Floor.java
│       │   └── state/                ← ★ State Pattern 核心
│       │       ├── SeatState.java        (interface)
│       │       ├── SeatStates.java       (Flyweight holder)
│       │       ├── IdleState.java
│       │       ├── ReservedState.java
│       │       ├── OccupiedState.java
│       │       └── TempAwayState.java
│       ├── observer/                 ← ★ Observer Pattern
│       │   ├── AlertObserver.java        (interface)
│       │   ├── NoiseMonitor.java         (Subject)
│       │   ├── TemperatureMonitor.java   (Subject, Version 2.0 新增)
│       │   ├── AdminPushChannel.java     (Concrete observer)
│       │   ├── DigitalSignageChannel.java
│       │   └── AlertEvent.java
│       ├── report/                   ← ★ Strategy Pattern (Version 2.0 新增)
│       │   ├── ReportStrategy.java       (interface)
│       │   ├── ReportService.java        (依 type 分派)
│       │   ├── ReportTypeDto.java
│       │   ├── SeatUsageReportStrategy.java   + SeatUsageReport / ZoneUsage
│       │   └── EnvironmentAlertReportStrategy.java + EnvironmentAlertReport / ZoneEnvironment
│       ├── repository/               ← Repository Pattern
│       ├── service/
│       ├── controller/               ← REST API (Seat / Sensor / Alert / Report)
│       ├── dto/                      ← 請求 / 回應形狀
│       └── exception/
└── frontend/                         ← React 18 + Vite
    ├── package.json
    ├── vite.config.js                ← /api proxy 到 :8080
    ├── index.html
    └── src/
        ├── main.jsx
        ├── App.jsx                   ← 主畫面 + 2 秒輪詢
        ├── api/slsmsApi.js           ← REST client
        ├── components/
        │   ├── FloorMap.jsx
        │   ├── SeatCell.jsx
        │   ├── ControlPanel.jsx
        │   ├── QRDisplay.jsx
        │   ├── NoiseSimulator.jsx
        │   ├── TemperatureSimulator.jsx  ← Version 2.0 新增
        │   ├── ReportPanel.jsx           ← Version 2.0 新增
        │   └── RecentAlerts.jsx
        └── styles.css
```

---

## REST API 對照

| Method | Endpoint | 說明 |
|--------|----------|------|
| GET    | `/api/floors/{id}/seats` | 取得樓層所有座位 |
| GET    | `/api/zones/{zoneId}/seats` | 取得分區所有座位 |
| GET    | `/api/seats/{id}` | 取得單一座位 |
| POST   | `/api/seats/{id}/reserve` | 預約 (body: `{userId}`) |
| POST   | `/api/seats/{id}/checkin` | QR Code Check-in (body: `{userId}`) |
| POST   | `/api/seats/{id}/leave-temp` | 暫離 (body: `{userId}`,需為座位目前使用者) |
| POST   | `/api/seats/{id}/come-back` | 回來 (body: `{userId}`,需為座位目前使用者) |
| POST   | `/api/seats/{id}/release` | 釋放 (body: `{userId}`,需為座位目前使用者/預約者) |
| POST   | `/api/sensors/noise` | 推送噪音 (body: `{zoneId, dB}`) |
| GET    | `/api/sensors/noise/{zoneId}` | 查詢最新噪音、門檻與警示狀態 |
| POST   | `/api/sensors/temperature` | 推送溫度 (body: `{zoneId, celsius}`,Version 2.0 新增) |
| GET    | `/api/sensors/temperature/{zoneId}` | 查詢最新溫度、範圍與警示狀態 (Version 2.0 新增) |
| GET    | `/api/alerts/recent` | 近期警示列表 (含 `NOISE_HIGH` / `TEMP_HIGH` / `TEMP_LOW`) |
| GET    | `/api/alerts/signage/{zoneId}` | 該區數位看板訊息 |
| GET    | `/api/reports` | 列出可用報表型態 (Strategy Pattern, Version 2.0 新增) |
| GET    | `/api/reports/{type}` | 產生指定型態報表 (`seat-usage` / `environment-alerts`,未知型態回 400) |

---

## 上台 Demo 的 SOP

建議照下列順序走:

1. **介紹畫面**:右上「後端已連線」綠燈,12 座位全空閒。
2. **State Pattern + 歸屬驗證 Demo (約 2 分鐘)**:
   - 點 A1 → 預約 → A1 變橘
   - 點 A1 → 掃 QR Check-in → 出現 QR → 模擬掃描 → A1 變紅
   - 點 A1 → 暫時離開 → A1 變紫
   - 點 A1 → 回來 → A1 變紅
   - 用 curl/Postman 以**不同 userId** 對 A1 呼叫 `/release` → 回傳 409,證明「僅本人可操作」
   - 點 A1 → 釋放 (用 demo-user-001) → A1 變綠
   - 切到 IntelliJ 展示 `IdleState.java` vs `OccupiedState.java`,
     強調「不同狀態類別只實作自己合法的方法,非法的會自動丟 IllegalStateTransitionException」
3. **Observer Pattern Demo (噪音 + 溫度,約 1.5 分鐘)**:
   - 點 +20 dB 兩次 → 噪音變紅 → 警示條脈動,「近期警示」新增 `NOISE_HIGH`
   - 點 +10°C(過熱)數次 → 溫度警示條脈動,「近期警示」新增 `TEMP_HIGH`,與噪音警示互不干擾
   - 切到後端 console,展示 `[ADMIN_PUSH]` 和 `[DIGITAL_SIGNAGE]` 兩個觀察者各自寫 log
   - 強調:Version 2.0 新增溫度監測時,**沒有修改 Observer 架構**,只新增了 `TemperatureMonitor` 這個 Subject,
     這就是 Open/Closed Principle。
4. **Strategy Pattern Demo (使用率報表,約 1 分鐘)**:
   - 在「使用率報表」面板切換「座位使用率報表」↔「環境警示熱點報表」,表格欄位即時改變、不需重整頁面
   - 對照樓層圖目前狀態,確認座位使用率報表的各狀態數量一致
   - (可選) curl `GET /api/reports/unknown-type` → 回傳 400,展示未知型態的錯誤處理
5. **跑測試 (約 1 分鐘)**:
   - 終端機跑 `mvn test` → 34 條測試全數 ✅ 綠燈
   - (可選) 跑 `acceptance_api_test.ps1`,展示 21 項後端 API 驗收項目全數 PASS

---