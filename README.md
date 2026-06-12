# SLSMS · Smart Library Space Management System

期中專案 Demo · 物件導向軟體工程 · Version 1.0 (Unified Process)

一套用 Spring Boot 3 (Java 17) + React 18 (Vite) 打造的圖書館空間管理 Demo,
重點凸顯兩個核心設計模式的實作: **State Pattern** (座位狀態機) 和 **Observer Pattern** (噪音警示推播)。

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

**Observer Pattern Demo**:
- 用噪音模擬器面板的 `+5 dB` / `+20 dB (突發)` 按鈕推升 dB
- 跨過 65 dB 門檻時 → 立刻看到紅色脈動警示條 (`pulsing`)
- 同時頁面底部「近期警示」清單會新增一筆紀錄
- 後端 console 會印出 `[ADMIN_PUSH]` 與 `[DIGITAL_SIGNAGE]` 兩個觀察者各自處理的 log

---

## 跑單元測試

```bash
cd backend
mvn test
```

測試對應期中報告 SRS 的測試案例:

| Test 方法 | 對應測試案例 | 驗證重點 |
|-----------|------------|---------|
| `idle_to_occupied_via_checkin` | TC-02 | Idle → checkIn → Occupied |
| `occupied_cannot_checkin_again` | TC-03 | 已使用中無法再 Check-in (409) |
| `reserved_seat_rejects_checkin_by_other_user` | — | 預約者驗證 |
| `reserved_timeout_returns_to_idle` | TC-04 | 預約逾時自動釋放 |
| `occupied_leave_temp_come_back_cycle` | TC-05 | 暫離→回來循環 |
| `crossing_threshold_upward_triggers_alert` | TC-06 | 噪音超標推播 |
| `edge_triggered_no_repeat_alerts_*` | — | 邊緣觸發,避免警示洪水 |
| `multiple_observers_all_receive_alert` | — | 多觀察者同步通知 |

---

## 目錄結構

```
slsms/
├── README.md                         ← 你正在讀的檔案
├── docs/
│   └── PATTERNS.md                   ← 設計模式落點對照
├── backend/                          ← Spring Boot 3 + Java 17
│   ├── pom.xml
│   └── src/main/java/com/oose/slsms/
│       ├── SlsmsApplication.java
│       ├── config/                   ← CORS, DataInitializer
│       ├── domain/
│       │   ├── Seat.java             ← State Pattern Context
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
│       │   ├── AdminPushChannel.java     (Concrete observer)
│       │   ├── DigitalSignageChannel.java
│       │   └── AlertEvent.java
│       ├── repository/               ← Repository Pattern
│       ├── service/
│       ├── controller/               ← REST API
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
        │   └── RecentAlerts.jsx
        └── styles.css
```

---

## REST API 對照

| Method | Endpoint | 說明 |
|--------|----------|------|
| GET    | `/api/floors/{id}/seats` | 取得樓層所有座位 |
| GET    | `/api/seats/{id}` | 取得單一座位 |
| POST   | `/api/seats/{id}/reserve` | 預約 (body: `{userId}`) |
| POST   | `/api/seats/{id}/checkin` | QR Code Check-in |
| POST   | `/api/seats/{id}/leave-temp` | 暫離 |
| POST   | `/api/seats/{id}/come-back` | 回來 |
| POST   | `/api/seats/{id}/release` | 釋放 |
| POST   | `/api/sensors/noise` | 推送噪音 (body: `{zoneId, dB}`) |
| GET    | `/api/sensors/noise/{zoneId}` | 查詢最新噪音 |
| GET    | `/api/alerts/recent` | 近期警示列表 |
| GET    | `/api/alerts/signage/{zoneId}` | 該區數位看板訊息 |

---

## 上台 Demo 的 SOP

5 分鐘的 demo,建議照下列順序走:

1. **介紹畫面**:右上「後端已連線」綠燈,12 座位全空閒。
2. **State Pattern Demo (約 2 分鐘)**:
   - 點 A1 → 預約 → A1 變橘
   - 點 A1 → 掃 QR Check-in → 出現 QR → 模擬掃描 → A1 變紅
   - 點 A1 → 暫時離開 → A1 變紫
   - 點 A1 → 回來 → A1 變紅
   - 點 A1 → 釋放 → A1 變綠
   - 切到 IntelliJ 展示 `IdleState.java` vs `OccupiedState.java`,
     強調「不同狀態類別只實作自己合法的方法,非法的會自動丟 IllegalStateTransitionException」
3. **Observer Pattern Demo (約 1 分鐘)**:
   - 點 +20 dB 兩次 → 噪音變紅 → 警示條脈動
   - 切到後端 console,展示 `[ADMIN_PUSH]` 和 `[DIGITAL_SIGNAGE]` 兩個觀察者各自寫 log
   - 強調:增加新通道 (例如 LINE Notify) 只需要新增一個實作 `AlertObserver` 的類別,
     不需要改 NoiseMonitor。這就是 Open/Closed Principle。
4. **跑測試 (約 1 分鐘)**:
   - 終端機跑 `mvn test` → 所有測試 ✅ 綠燈

---