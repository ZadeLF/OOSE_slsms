# Sprint 2 — 預約 / 暫離逾時自動釋放排程

> 狀態：🔲 規劃中（尚未開始）
> 預計期間：2026-06-15 ~ 2026-06-19（5 個工作日；⚠️ 請小組依實際開發時間調整此區間）
>
> ⚠️ 本文件目前為 Sprint 2 的**計畫草案**。Daily 進度、Sprint Review、Retrospective 中標示
> 「`(Sprint 結束後填寫)`」的段落，請小組在 Sprint 2 實際執行後依站會記錄與回顧會議內容回填，
> 不要直接照抄本文件交件。

## 目錄

1. [Sprint Goal](#sprint-goal)
2. [Sprint Backlog](#sprint-backlog)
3. [Daily 進度摘要](#daily-進度摘要)
4. [Sprint Review](#sprint-review)
5. [Retrospective](#retrospective)

---

## Sprint Goal

> 解決 Sprint 1 Retrospective 中記錄的已知限制：「demo 需手動呼叫 `timeout()`，沒有自動排程」。
> 本 Sprint 對應 [`docs/scrum/ProductBacklog.md`](./ProductBacklog.md) 優先級「高」的
> **US-01 預約 / 暫離逾時自動釋放**，目標是讓 FR-03、FR-05 從「部分完成」升級為「已完成」。

完成後，SRS 中 FR-03、FR-05 與 SDD 第 4 節「排程說明」的 ⚠️ 標註應可移除或更新為已完成。

---

## Sprint Backlog

| # | 項目 | 說明 | 負責人 | 狀態 | 對應 commit |
|---|---|---|---|---|---|
| 1 | 排程任務設計 | 在 `SeatService` 或新增 `SeatTimeoutScheduler`，使用 Spring `@Scheduled` 定期掃描所有座位 | `TBD - 請填入實際負責人` | 🔲 待開始 | — |
| 2 | 「已預約」逾時釋放 | 座位處於 `RESERVED` 且 `sessionStart`/預約時間超過設定值（預設 15 分鐘）時，呼叫 `Seat.timeout()` | `TBD - 請填入實際負責人` | 🔲 待開始 | — |
| 3 | 「暫離」逾時釋放 | 座位處於 `TEMP_AWAY` 且 `tempAwaySince` 超過設定值（預設 30 分鐘）時，呼叫 `Seat.timeout()` | `TBD - 請填入實際負責人` | 🔲 待開始 | — |
| 4 | 逾時時間可設定化 | 將 15 分鐘 / 30 分鐘門檻抽到 `application.properties`（例如 `slsms.reservation.timeout-minutes`、`slsms.temp-away.timeout-minutes`） | `TBD - 請填入實際負責人` | 🔲 待開始 | — |
| 5 | 排程單元測試 | 新增測試驗證「超過門檻 → 自動轉為 Idle」「未超過門檻 → 不變」，透過注入可控制的時間（例如 `Clock`）避免真實等待 15/30 分鐘 | `TBD - 請填入實際負責人` | 🔲 待開始 | — |
| 6 | SRS/SDD 文件更新 | 將 FR-03、FR-05、SDD 第 4 節排程說明更新為「已完成」，並記錄新增的設定項 | `TBD - 請填入實際負責人` | 🔲 待開始 | — |

**範圍說明**：本 Sprint **不包含** US-02（分時段預訂）與 US-11（行動裝置 RWD）；
若團隊人力充足且 US-01 提前完成，可由小組決定是否將 US-11（估點 3，與本 Sprint 主題較無相依）加入本 Sprint 作為加分項目。

---

## Daily 進度摘要

`(Sprint 結束後填寫 — 建議每日站會記錄「昨天完成」「今天計畫」「阻礙」三項)`

- **Day 1（2026-06-15）**：`(待填寫)`
- **Day 2（2026-06-16）**：`(待填寫)`
- **Day 3（2026-06-17）**：`(待填寫)`
- **Day 4（2026-06-18）**：`(待填寫)`
- **Day 5（2026-06-19）**：`(待填寫)`

---

## Sprint Review

`(Sprint 結束後填寫)`

**Demo 內容（計畫）**：
- 將某座位設為「已預約」，調整設定檔中的逾時門檻為較短時間（例如 10 秒，僅供 demo 用）以便現場觀察排程觸發。
- 等待排程執行後，前端樓層圖應自動（不需手動操作）將該座位變回「空閒」。
- 同樣方式 demo「暫離」逾時自動釋放。

**驗收結果**：
- [ ] Sprint Goal 達成（US-01 驗收條件全部通過）
- [ ] `mvn test` 全綠（含新增排程測試）
- [ ] FR-03、FR-05 在 SRS 中狀態更新為已完成
- [ ] Acceptance Checklist（`docs/AcceptanceChecklist.md`）新增對應勾選項並通過

**Stakeholder 回饋**：`(待填寫)`

---

## Retrospective

`(Sprint 結束後填寫，請小組依實際情況記錄，不要照抄 Sprint 1 的內容)`

### 做得好（Keep）
- `(待填寫)`

### 要改進（Problem）
- `(待填寫)`

### 行動項（Action Items）
- `(待填寫 — 例如：根據本 Sprint 結果，決定 Sprint 3 是否排入 US-04 登入機制或 US-06 JPA 持久化)`
