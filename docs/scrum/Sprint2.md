# Sprint 2 — 使用率報表（Strategy Pattern）與溫度感測警示（Observer 擴充）

> 狀態：✅ 已完成
> 期間：2026-06-10 ~ 2026-06-12（3 個工作日；⚠️ 請小組依實際開發時間調整此區間）

## 目錄

1. [Sprint Goal](#sprint-goal)
2. [Sprint Backlog](#sprint-backlog)
3. [Daily 進度摘要](#daily-進度摘要)
4. [Sprint Review](#sprint-review)
5. [Retrospective](#retrospective)

---

## Sprint Goal

> 本 Sprint 對應 [`docs/scrum/ProductBacklog.md`](./ProductBacklog.md) 的
> **US-10 溫度感測與 Observer 擴充** 與 **US-09 使用分析報表（Strategy Pattern）**，
> 目標是讓 FR-06（噪音/溫度警示）與 FR-08（使用分析報表）皆從「V2 規劃中」升級為「Version 2.0 已實作」。
>
> 兩個 Story 皆刻意選擇「可直接複用既有架構、成本相對低」的設計：
> - US-10 不新增 Observer 架構，只新增 `TemperatureMonitor` 作為第二個 Subject，呼應 Open/Closed Principle。
> - US-09 以 `ReportStrategy` 介面實作 Strategy Pattern，資料來源全部取自現有記憶體狀態，不引入 JPA/資料庫。

完成後，SRS 中 FR-06、FR-08 與 UC-08、UC-09 的狀態，以及 SDD 第 3.2、3.7 節，均已更新為「Version 2.0 已實作」。

---

## Sprint Backlog

| # | 項目 | 說明 | 負責人 | 狀態 | 對應 commit |
|---|---|---|---|---|---|
| 1 | TemperatureMonitor（US-10） | 新增 `TemperatureMonitor` 作為第二個 Observer Subject，雙邊界（上限/下限）邊緣觸發；`AlertEvent` 新增 `TEMP_HIGH`/`TEMP_LOW` factory | `TBD - 請填入實際負責人` | ✅ 完成 | `6f9f4e8` |
| 2 | 溫度感測 API + Observer 註冊（US-10） | 新增 `POST /api/sensors/temperature`、`GET /api/sensors/temperature/{zoneId}`；`AdminPushChannel`、`DigitalSignageChannel` 建構子同時向 `NoiseMonitor` 與 `TemperatureMonitor` 註冊 | `TBD - 請填入實際負責人` | ✅ 完成 | `6f9f4e8` |
| 3 | 溫度警示單元測試（US-10） | 新增 `TemperatureObserverTest`（7 條）：跨越上/下限觸發、邊界不重複觸發、回到範圍內再次觸發、多 observer 皆收到、噪音與溫度監測器互不影響 | `TBD - 請填入實際負責人` | ✅ 完成（隨 #1/#2 一併提交） | `6f9f4e8` |
| 4 | ReportStrategy 報表子系統（US-09） | 新增 `ReportStrategy` 介面、`SeatUsageReportStrategy`（座位使用率）、`EnvironmentAlertReportStrategy`（環境警示熱點，整合噪音+溫度）、`ReportService`（依 `type()` 派送）、`ReportController`（`GET /api/reports`、`GET /api/reports/{type}`） | `TBD - 請填入實際負責人` | ✅ 完成 | `17d7772` |
| 5 | 報表單元測試（US-09） | 新增 `SeatUsageReportStrategyTest`、`EnvironmentAlertReportStrategyTest`、`ReportServiceTest`（共 8 條),驗證各策略計算正確性與策略可替換（多型）、未知 type 回 400 | `TBD - 請填入實際負責人` | ✅ 完成（隨 #4 一併提交） | `17d7772` |
| 6 | 前端：溫度模擬器 + 報表面板 | 新增 `TemperatureSimulator.jsx`（手動調溫度觸發警示）、`ReportPanel.jsx`（報表型態選單 + 表格呈現),擴充 `api/slsmsApi.js`,整合進 `App.jsx` | `TBD - 請填入實際負責人` | ✅ 完成 | `c02fef7` |
| 7 | SRS/SDD/AcceptanceChecklist/UML 文件更新 | 將 FR-06、FR-08、UC-08、UC-09 更新為「Version 2.0 已實作」;新增 §10.3、§10.4 驗收項目;UML 補上 `TemperatureMonitor`、`ReportStrategy` 相關類別與 UC-09/UC-08c | `TBD - 請填入實際負責人` | ✅ 完成 | `(本次 docs commit)` |

---

## Daily 進度摘要

- **Day 1（2026-06-10）**：完成項目 1、2、3（US-10 溫度感測 Observer 擴充與單元測試),`mvn test` 26/26 全綠。
- **Day 2（2026-06-11）**：完成項目 4、5（US-09 報表 Strategy 子系統與單元測試),`mvn test` 34/34 全綠。
- **Day 3（2026-06-12）**：完成項目 6（前端溫度模擬器、報表面板,`npm run build` 驗證通過),以及項目 7（SRS/SDD/AcceptanceChecklist/UML 文件同步、本 Sprint 文件回填),Sprint Review 與 Retrospective。

---

## Sprint Review

**Demo 內容**：
- 在「A 區溫度感測器」面板點擊「+10°C（過熱）」，展示警示橫幅以脈動效果出現,「近期警示」清單新增一筆 `TEMP_HIGH` 紀錄;點擊「重置」後橫幅消失;再點擊「−10°C（過冷）」展示 `TEMP_LOW` 警示。
- 同時觸發噪音與溫度警示,展示兩者各自獨立顯示,「近期警示」清單同時包含 `NOISE_HIGH` 與 `TEMP_*` 紀錄。
- 在「使用率報表」面板切換「座位使用率報表」與「環境警示熱點報表」,展示表格欄位隨報表類型改變,且環境警示熱點報表的「近期警示」次數會反映剛才觸發的噪音/溫度警示。
- 以 curl 對 `GET /api/reports/unknown-type` 呼叫,展示回應 HTTP 400。

**驗收結果**：
- [x] Sprint Goal 達成（US-09、US-10 驗收條件全部通過）
- [x] `mvn test` 全綠（34/34，新增 15 條：溫度 Observer 7 條 + 報表 Strategy 8 條）
- [x] `npm run build` 成功
- [x] FR-06、FR-08 在 SRS 中狀態更新為「Version 2.0 已實作」
- [x] `docs/AcceptanceChecklist.md` 新增 FR-06b、FR-08、§10.3、§10.4 對應勾選項

**Stakeholder 回饋**：`TBD - 請於 Sprint Review 會議後填寫`

---

## Retrospective

### 做得好（Keep）
- 兩個 Story 都選擇「複用既有設計模式架構」而非新建一套機制（`TemperatureMonitor` 複用 `AlertObserver`；`ReportStrategy` 與 Spring 的 `List<Bean>` 注入機制自然契合),驗證了 V1.0 的 State/Observer/Repository 抽象設計確實對擴充友善（Open/Closed Principle）。
- 報表子系統刻意不引入 JPA/資料庫,直接讀取現有 in-memory 狀態,讓 US-09 在一個 Sprint 內就能交付可 demo 的端點與畫面,避免被 US-06（持久化）阻塞。
- 延續 Sprint 1 的作法,程式改動與單元測試在同一個 commit 內一起提交（共新增 15 條測試,34/34 全綠),文件同步在程式完成後立即進行。

### 要改進（Problem）
- `EnvironmentAlertReportStrategy` 同時依賴 `SeatRepository`、`NoiseMonitor`、`TemperatureMonitor`、`AdminPushChannel` 四個元件,建構子參數較多;若未來再新增第三種感測器,建議評估是否需要抽出一個共用的「環境讀數彙整」介面,避免這個類別繼續增肥。
- 報表資料目前皆為「即時快照」,沒有時間序列概念,因此「熱點/時段報表」實際上只能反映 `AdminPushChannel` 緩衝區內最近 50 筆警示,無法回答「昨天哪個時段最常超標」這類問題——這點需要 US-06 持久化後才能真正解決。

### 行動項（Action Items）
- [ ] 下一個 Sprint 優先評估 Product Backlog 中 **US-06 JPA/MySQL 持久化**,以便報表子系統未來能支援「依日期/時段」的歷史分析,而非僅即時快照。
- [ ] 評估是否需要將 `EnvironmentAlertReportStrategy` 的多個依賴抽出共用介面（待第三種感測器類型出現時再決定是否值得抽象）。
- [ ] 小組於下一個 Sprint 開始前,將本文件「負責人」欄位的 `TBD` 全部填上實際分工。
