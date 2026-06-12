# Sprint 1 — 補齊 V1 已知落差 + 驗收文件

> 狀態：✅ 已完成
> 期間：2026-06-08 ~ 2026-06-12（5 個工作日；⚠️ 請小組依實際開發時間調整此區間與下方 Daily 進度）

## 目錄

1. [Sprint Goal](#sprint-goal)
2. [Sprint Backlog](#sprint-backlog)
3. [Daily 進度摘要](#daily-進度摘要)
4. [Sprint Review](#sprint-review)
5. [Retrospective](#retrospective)

---

## Sprint Goal

> 在進入 Version 2.0 的 Scrum 開發週期前，先**清除 V1.0「報告寫了但程式沒有」的已知落差**，
> 讓 demo 與 SRS/SDD 的描述一致，並補上可供現場驗收的 Acceptance Checklist，
> 作為後續 Sprint（功能性新增）的穩固基礎。

此 Sprint 不對應 Product Backlog 中的 V2 User Story（US-01 ~ US-12），
而是 V1 → V2 過渡期的「技術債清理 + 文件補齊」工作，內容直接對應 [`docs/SRS.md`](../SRS.md) 第 1.3、2 節與
[`docs/AcceptanceChecklist.md`](../AcceptanceChecklist.md) 的建立。

---

## Sprint Backlog

| # | 項目 | 說明 | 負責人 | 狀態 | 對應 commit |
|---|---|---|---|---|---|
| 1 | 真實 QR Code Check-in | 將前端假 QR SVG 換成 `qrcode.react` 產生的真實編碼 QR（`SLSMS:CHECKIN:{seatId}`） | `TBD - 請填入實際負責人` | ✅ 完成 | `2b1fa9d` |
| 2 | 座位使用者歸屬驗證 | `OccupiedState`/`TempAwayState` 的 `release`/`leaveTemp`/`comeBack` 加入 `userId` 歸屬檢查，非本人回 409 | `TBD - 請填入實際負責人` | ✅ 完成 | `2eb784f` |
| 3 | 新增對應單元測試 | `SeatStateTransitionTest` 新增 4 條 `*_by_other_user_rejected` 測試，確認 19/19 全綠 | `TBD - 請填入實際負責人` | ✅ 完成（隨 #2 一併提交） | `2eb784f` |
| 4 | 同步 SRS / SDD 文件 | 更新 FR-02、FR-05、UC-04、系統範圍表、領域模型描述、狀態機說明與測試數量 | `TBD - 請填入實際負責人` | ✅ 完成 | `6f4f840` |
| 5 | 建立 Acceptance Checklist | 依 FR-01 ~ FR-08 與兩個核心設計模式建立可現場勾選的驗收清單 | `TBD - 請填入實際負責人` | ✅ 完成 | `aefc600` |

---

## Daily 進度摘要

> ⚠️ 以下為依工作項目回填的摘要範例，請小組依實際每日站會記錄調整內容與日期。

- **Day 1（2026-06-08）**：盤點 SRS/SDD 與實際程式碼的落差，篩選出「小成本可補」與「應列 V2」兩類項目；確認本 Sprint 範圍為項目 1、2（經負責人確認後動工）。
- **Day 2（2026-06-09）**：完成項目 2（使用者歸屬驗證），新增 4 條單元測試，`mvn test` 19/19 全綠。
- **Day 3（2026-06-10）**：完成項目 1（真實 QR Code），安裝 `qrcode.react`，`npm run build` 驗證通過；同步調整 `ControlPanel.jsx` 說明文字。
- **Day 4（2026-06-11）**：完成項目 4，同步更新 SRS/SDD 中受影響的章節與測試數量描述。
- **Day 5（2026-06-12）**：完成項目 5，建立 `docs/AcceptanceChecklist.md`；Sprint Review 與 Retrospective。

---

## Sprint Review

**Demo 內容**：
- 在前端選擇空閒座位並點擊「掃 QR Check-in」，展示畫面顯示的 QR Code 可被一般 QR Reader 掃描出 `SLSMS:CHECKIN:{seatId}`。
- 以 curl 對「使用中」座位呼叫 `/release`、`/leave-temp` 並帶入非本人的 `userId`，展示回應為 409 Conflict。
- 走過 `docs/AcceptanceChecklist.md` 的 FR-01 ~ FR-06 勾選項，確認皆可在 demo 中被驗證。

**驗收結果**：
- [ ] Sprint Goal 達成（由 PO / 教學助理 / 小組共同確認）
- [ ] `mvn test` 全綠（19/19）
- [ ] `npm run build` 成功
- [ ] `docs/AcceptanceChecklist.md` 內容與實際 demo 一致

**Stakeholder 回饋**：`TBD - 請於 Sprint Review 會議後填寫`

---

## Retrospective

### 做得好（Keep）
- 採用「先列清單給負責人確認範圍，再動手」的流程，避免一次改動過多檔案造成 review 困難。
- 每個程式改動都同步補測試（4 條新測試），維持 `mvn test` 全綠，降低後續 Sprint 的回歸風險。
- 文件（SRS/SDD/AcceptanceChecklist）與程式碼改動在同一個 Sprint 內同步更新，避免「程式改了但文件忘記改」的落差再次出現。

### 要改進（Problem）
- 本 Sprint 的「使用者歸屬驗證」其實修補了一個潛在 bug（任意 `userId` 都能釋放他人座位），代表 V1 在功能驗證階段未涵蓋多使用者情境，**之後新增功能時應主動思考「換成另一個使用者操作會發生什麼」**。
- Acceptance Checklist 中 §10.1 驗收方式 B（雙視窗 race condition）操作複雜，現場驗收時容易因時機沒抓好而無法重現，需要更穩定的演示方式。

### 行動項（Action Items）
- [ ] 下一個 Sprint（Sprint 2）優先處理 Product Backlog 中優先級「高」的 **US-01 預約/暫離逾時自動釋放**，呼應本 Sprint Review 中「demo 需手動觸發 timeout()」的已知限制。
- [ ] 為 §10.1 驗收方式 B 準備一個更穩定的重現腳本（例如用 curl 連續快速送出兩個 `/reserve` 請求，取代雙視窗手動操作）。
- [ ] 小組於 Sprint 2 開始前，將本文件「負責人」欄位的 `TBD` 全部填上實際分工。
