# SRS — 軟體需求規格 (Software Requirements Specification)

> 智慧圖書館空間管理系統 (Smart Library Space Management System, SLSMS) — Version 1.0
>
> 本文件由期中報告的 SRS 章節萃取、整理而成，並依「程式碼實際完成範圍」校正內容。
> 凡標註 **「V2 規劃中」** 的項目，皆尚未在 Version 1.0 程式碼中實作，**不計入 V1 的驗收範圍**，
> 待 Version 2.0 (Scrum) 開發週期再行評估與實作。

## 目錄

1. [專案概述](#1-專案概述)
2. [功能性需求 (Functional Requirements)](#2-功能性需求-functional-requirements)
3. [非功能性需求 (Non-Functional Requirements)](#3-非功能性需求-non-functional-requirements)
4. [使用案例 (Use Cases)](#4-使用案例-use-cases)

---

## 1. 專案概述

### 1.1 動機與背景

公共閱覽室與大學圖書館長期面臨兩大空間使用問題:其一為「無位可坐」——讀者進入閱覽室後須花費大量時間遊走尋位;其二為「惡意佔位」——使用者以個人物品長時間占用座位卻未實際使用,導致空間資源閒置。傳統人工巡查方式既耗費人力,也難以即時介入。

本專案以物件導向軟體工程方法論為基礎,設計並實作一套以 Web 為核心的「智慧圖書館空間管理系統 (SLSMS)」,以下簡稱「本系統」。Version 1.0 採 Unified Process 開發;Version 2.0 將以 Scrum (至少兩個 Sprint) 持續演進。

### 1.2 專案目標

- 應用 SE 實務 (Software Engineering Practices) 與 OO 技術 (Object-Oriented Techniques) 開發一套可運作的軟體系統,而不僅是元件級的範例。
- Version 1.0 以 Unified Process 推進 (Inception → Elaboration → Construction);Version 2.0 以 Scrum 運作至少兩個 Sprint。
- 依需求驅動 (Use-Case Driven) 的方式推導 UML 模型與設計文件 (SRS、SDD、Test Document)。
- 實作至少 2 種核心設計模式:**State Pattern**(座位狀態機)與 **Observer Pattern**(噪音警示推播),並輔以 Singleton/Flyweight、Repository、MVC、DTO 等架構模式。
- 在 Version 1.0 的範圍內達成可驗證的可維護性(單元測試覆蓋核心邏輯);其餘非功能性需求(效能、可靠性、安全性等完整指標)列為 V2 規劃與驗證項目。

### 1.3 系統範圍

系統將實體圖書館空間數位化為「樓層 (Floor) → 區域 (Zone) → 座位 (Seat)」三層階層結構。

| 核心服務 | V1.0 範圍 |
|---|---|
| 動態狀態追蹤 (Dynamic State Management) | ✅ **已完成** — 透過 State Pattern 即時呈現座位狀態(空閒/已預約/使用中/暫離),前端每 2 秒輪詢更新 |
| 預約與 Check-in 流程 | ✅ **已完成(簡化版)** — 即時預約 + QR Code Check-in(QR 為真實編碼,掃描提交動作以「模擬掃描成功」按鈕觸發);**分時段預訂、預約逾時自動釋放的排程機制為 V2 規劃中** |
| 環境感知報警 (Observer-Based Environmental Alert) | ✅ **已完成(Version 2.0)** — 噪音、溫度超標時皆透過同一套 Observer Pattern 推播給管理員推播通道與數位看板 |
| 資源使用分析 (Data Abstraction & Reporting) | ✅ **已完成(Version 2.0)** — 座位使用率報表、環境警示熱點報表(Strategy Pattern),`GET /api/reports/{type}` |

### 1.4 利害關係人 (Stakeholders)

| 利害關係人 | 角色描述 | 主要關注點 | V1.0 狀態 |
|---|---|---|---|
| 讀者 (Reader) | 圖書館使用者,需要使用座位閱讀或自習 | 找位效率、預約便利性、避免被惡意佔位影響 | ✅ 主要操作對象,V1 demo 以單一示範帳號 (`demo-user-001`) 代表,無登入機制 |
| 管理員 (Admin) | 圖書館人員,負責現場巡視與秩序維護 | 即時掌握使用狀況、減少巡查工作量 | ✅ 部分支援 — 可透過 `/api/alerts/recent`、數位看板訊息接收噪音警示;**強制釋放座位、後台管理介面為 V2 規劃中** |
| 系統管理者 (SysAdmin) | 資訊單位,負責系統部署與維運 | 系統穩定性、可擴充性、安全性 | 🔲 V2 規劃中(目前無權限/部署管理機制) |
| 營運單位 | 圖書館決策層 | 空間使用率、能耗成本、讀者滿意度 | ✅ Version 2.0 — 可透過 `GET /api/reports/seat-usage` 取得座位使用率報表 |

---

## 2. 功能性需求 (Functional Requirements)

| 編號 | 需求名稱 | 需求描述 | 優先級 | V1.0 狀態 |
|---|---|---|---|---|
| FR-01 | 查詢座位狀態 | 系統應於 2 秒內以視覺化樓層圖呈現所有座位的即時狀態 (空閒/使用中/暫離/已預約)。 | 高 | ✅ 已完成 — 前端每 2 秒輪詢 `GET /api/floors/{id}/seats` |
| FR-02 | QR Code Check-in | 讀者掃描座位 QR Code 後,系統切換座位狀態為「使用中」。 | 高 | ✅ 已完成(簡化版) — 前端顯示真實編碼的 QR Code(內容為 `SLSMS:CHECKIN:{seatId}`,可被任何 QR Reader 掃描),點擊「模擬掃描成功」呼叫 `POST /api/seats/{id}/checkin`;**相機掃描整合與身分驗證 (JWT) 為 V2 規劃中** |
| FR-03 | 即時預約 | 讀者可選擇空閒座位進行即時預約;預約成功後 15 分鐘內未 Check-in 則自動釋放。 | 高 | ⚠️ **部分完成** — 預約動作 (`POST /api/seats/{id}/reserve`) 已完成;「15 分鐘逾時自動釋放」的狀態轉移 (`timeout()`) 已在 domain 層實作並有單元測試 (TC-05),但**自動排程觸發為 V2 規劃中**,V1 demo 需手動呼叫 |
| FR-04 | 分時段預訂 | 讀者可預訂未來 7 天內、以 30 分鐘為粒度的座位時段;系統需校驗無時段衝突。 | 中 | 🔲 **V2 規劃中** — 未實作 |
| FR-05 | 暫離模式 | 使用中讀者可以將座位切為「暫時離開」最多 30 分鐘;逾時則自動釋放。 | 中 | ⚠️ **部分完成** — 暫離/回來動作 (`leave-temp` / `come-back`) 已完成,且僅限該座位目前的使用者本人操作(其他 `userId` 會收到 409,見 `SeatStateTransitionTest` 的 `*_by_other_user_rejected` 系列測試);「30 分鐘逾時自動釋放」的狀態轉移已在 domain 層實作並有單元測試 (TC-07),但**自動排程觸發為 V2 規劃中** |
| FR-06 | 噪音/溫度警示 | 感測器讀數逾門檻值時,系統應推播警示給該區管理員與數位看板。 | 中 | ✅ **已完成(Version 2.0)** — **噪音**與**溫度**皆透過同一套 Observer Pattern 實作邊緣觸發警示;`AlertEvent.type` 以 `NOISE_HIGH` / `TEMP_HIGH` / `TEMP_LOW` 區分,`AdminPushChannel`、`DigitalSignageChannel` 兩個 Observer 皆可同時接收兩種感測器事件。共 12 條單元測試全綠(`NoiseObserverTest` 5 條 + `TemperatureObserverTest` 7 條) |
| FR-07 | 管理員後台 | 管理員可強制釋放座位、查看歷史紀錄、處理檢舉。 | 中 | 🔲 **V2 規劃中** — 未實作 |
| FR-08 | 使用分析報表 | 系統每日產生熱點/使用率報表,並提供 CSV 匯出。 | 低 | ✅ **已完成(Version 2.0,簡化版)** — 以 Strategy Pattern 實作 `ReportStrategy`,提供「座位使用率報表」(`seat-usage`)與「環境警示熱點報表」(`environment-alerts`)兩種策略,透過 `GET /api/reports/{type}` 取得即時(in-memory)資料;**每日排程產出與 CSV 匯出仍為 V2 後續規劃中** |

---

## 3. 非功能性需求 (Non-Functional Requirements)

| 類別 | 需求 | V1.0 實際狀況 |
|---|---|---|
| 效能 (Performance) | API 回應時間 ≤ 1 秒 (P95);同時 500 名讀者使用不掉線 | ✅ 在 demo 規模(12 個座位、單一使用者)下,in-memory 架構回應遠低於 1 秒;**500 並發測試 (JMeter) 為 V2 規劃中** |
| 可靠性 (Reliability) | 月可用度 ≥ 99.5%;預約資料零遺失(持久化 + 交易) | ⚠️ V1.0 使用 **in-memory (`ConcurrentHashMap`)** 儲存,後端重啟資料即清空;**持久化 (JPA/MySQL) 與交易保證為 V2 規劃中**,可用性指標不適用於目前 demo |
| 可重用性 (Reusability) | 感測器模組可換廠牌(抽象介面) | ⚠️ 噪音感測以 REST 端點接收數值,**`ISensorAdapter` 抽象介面尚未實作**(V2 規劃中);但 State / Observer / Repository 的介面化設計已展現開放封閉原則 |
| 安全性 (Security) | JWT 驗證、HTTPS、資料庫密碼以 bcrypt 加鹽雜湊 | 🔲 **V2 規劃中** — V1.0 所有 API 皆為公開端點,無登入與權限機制,前端以固定示範使用者 ID 操作 |
| 可維護性 (Maintainability) | Cyclomatic Complexity ≤ 10;單元測試覆蓋率 ≥ 70% | ✅ 34 條單元測試全綠,涵蓋 State Pattern(14 條,含使用者歸屬驗證)、Observer Pattern(噪音 5 條 + 溫度 7 條)與 Strategy Pattern 報表子系統(8 條,含策略計算正確性與可替換性);Cyclomatic Complexity 未量測工具驗證 |
| 可用性 (Usability) | 讀者首次使用即上手,完成預約流程 ≤ 4 步;手機 RWD 全相容 | ✅ 操作流程(點選座位 → 選動作 → [QR] → 結果)≤ 4 步;**行動裝置 RWD 相容性尚未專門驗證** |

---

## 4. 使用案例 (Use Cases)

### 4.1 Use-Case 總覽

本系統識別出三類 Actor:**Reader (讀者)**、**Admin (管理員)**、**SensorSystem (環境感測子系統,次級 Actor)**。

| Use Case | Actor | V1.0 狀態 |
|---|---|---|
| UC-01 查詢座位 | Reader | ✅ 已完成 |
| UC-02 即時預約座位 | Reader | ✅ 已完成(`reserve` 動作);extend → UC-03 分時段預訂為 V2 規劃中 |
| UC-03 分時段預訂 | Reader | 🔲 V2 規劃中 |
| UC-04 QR Code 報到 | Reader | ✅ 已完成(簡化版,見下方詳述);include → UC-05 身分驗證為 V2 規劃中 |
| UC-05 身分驗證 | Reader | 🔲 V2 規劃中(JWT) |
| UC-06 暫離與恢復 | Reader | ✅ 已完成(`leave-temp` / `come-back`) |
| UC-07 強制釋放座位 | Admin | 🔲 V2 規劃中 |
| UC-08 處理環境警示 | Admin / SensorSystem | ✅ 已完成(Version 2.0)— 噪音與溫度皆透過 Observer Pattern 推播 |
| UC-09 產出使用分析報表 | Admin | ✅ 已完成(Version 2.0,簡化版)— Strategy Pattern,`GET /api/reports/{type}` |

### 4.2 重點 Use-Case 描述:UC-04 QR Code 報到(V1.0 簡化版)

| 欄位 | 內容 |
|---|---|
| Use Case ID | UC-04 |
| 名稱 | QR Code 報到 (Check-in via QR) |
| 主要 Actor | Reader (讀者) |
| 前置條件 | 座位處於「空閒」或「已預約 (本人)」狀態 |
| 後置條件 (成功) | 座位狀態切換為「使用中」 |
| 主要流程 (Main Flow) | 1. 讀者於前端點擊座位上的「掃 QR Check-in」<br>2. 系統顯示該座位的 QR Code(以 `qrcode.react` 產生,內容為真實編碼字串 `SLSMS:CHECKIN:{seatId}`,可被任何 QR Reader 掃描得到相同內容)<br>3. 讀者點擊「模擬掃描成功」(代表已用裝置掃描並取得上述字串)<br>4. 系統呼叫 `POST /api/seats/{id}/checkin`<br>5. `SeatState.checkIn()` 依目前狀態決定是否允許,允許則切換為「使用中」並回傳成功訊息 |
| 替代流程 A1 | 若座位為「他人預約中」(`reservationOwner` 非本人),系統回傳 409 Conflict,前端顯示紅色錯誤條 |
| 異常流程 E1 | 座位 ID 不存在 → 回傳 404,前端顯示錯誤訊息 |
| **V2 規劃中** | 以相機/瀏覽器 API 實際掃描並解析 QR 內容(取代「模擬掃描成功」按鈕)、JWT 身分驗證、替代流程 A2(JWT 過期導向重新登入) |
