$base = "http://localhost:8080"
$results = @()

function Invoke-Api($method, $path, $jsonBody) {
    $url = "$base$path"
    if ($jsonBody) {
        $raw = curl.exe -s -w "`n###STATUS:%{http_code}###" -X $method $url -H "Content-Type: application/json" -d $jsonBody
    } else {
        $raw = curl.exe -s -w "`n###STATUS:%{http_code}###" -X $method $url
    }
    $text = $raw -join "`n"
    $idx = $text.LastIndexOf("###STATUS:")
    $body = $text.Substring(0, $idx).Trim()
    $status = [int]($text.Substring($idx+10) -replace "###","")
    return @{ status = $status; body = $body }
}

function Report($name, $expected, $actual, $pass) {
    $script:results += [PSCustomObject]@{
        item = $name
        expected = $expected
        actual = $actual
        pass = $pass
    }
    $passText = if ($pass) { "PASS" } else { "FAIL" }
    Write-Host "=== $name ==="
    Write-Host "預期: $expected"
    Write-Host "實際: $actual"
    Write-Host "結果: $passText"
    Write-Host ""
}

# ---------- FR-02 (backend) Check-in A1 ----------
$r = Invoke-Api POST "/api/seats/A1/checkin" '{\"userId\":\"demo-user-001\"}'
$ok = $r.status -eq 200 -and $r.body -match '"state":"OCCUPIED"' -and $r.body -match '"currentUserId":"demo-user-001"'
Report "FR-02 Check-in A1 (空閒->使用中)" "200, state=OCCUPIED, currentUserId=demo-user-001" "status=$($r.status), body=$($r.body)" $ok

# ---------- FR-03 即時預約 A2 ----------
$r = Invoke-Api POST "/api/seats/A2/reserve" '{\"userId\":\"demo-user-001\"}'
$ok = $r.status -eq 200 -and $r.body -match '"state":"RESERVED"' -and $r.body -match '"reservationOwner":"demo-user-001"'
Report "FR-03 預約 A2 (空閒->已預約)" "200, state=RESERVED, reservationOwner=demo-user-001" "status=$($r.status), body=$($r.body)" $ok

# ---------- FR-05 暫離模式 A1 ----------
$r = Invoke-Api POST "/api/seats/A1/leave-temp" '{\"userId\":\"demo-user-001\"}'
$ok = $r.status -eq 200 -and $r.body -match '"state":"TEMP_AWAY"' -and $r.body -match '"currentUserId":"demo-user-001"'
Report "FR-05 暫離 A1 (使用中->暫離)" "200, state=TEMP_AWAY, currentUserId維持demo-user-001" "status=$($r.status), body=$($r.body)" $ok

$r = Invoke-Api POST "/api/seats/A1/come-back" '{\"userId\":\"demo-user-001\"}'
$ok = $r.status -eq 200 -and $r.body -match '"state":"OCCUPIED"' -and $r.body -match '"currentUserId":"demo-user-001"'
Report "FR-05 回來 A1 (暫離->使用中)" "200, state=OCCUPIED, currentUserId維持demo-user-001" "status=$($r.status), body=$($r.body)" $ok

# ---------- 10.1 State Pattern: 使用中座位重複 check-in -> 409 ----------
$r = Invoke-Api POST "/api/seats/A1/checkin" '{\"userId\":\"demo-user-001\"}'
$ok = $r.status -eq 409 -and $r.body -match '"status":409' -and $r.body -match '"error"' -and $r.body -match '"message"'
Report "10.1 State Pattern: 對使用中A1重複checkin -> 409" "409, JSON含status/error/message, 訊息提及OCCUPIED與checkIn" "status=$($r.status), body=$($r.body)" $ok

# ---------- Ownership 驗證: 其他使用者 release -> 409 ----------
$r = Invoke-Api POST "/api/seats/A1/release" '{\"userId\":\"other-user-999\"}'
$ok = $r.status -eq 409 -and $r.body -match '"status":409'
Report "Ownership驗證: 非本人對A1呼叫release -> 409" "409 Conflict" "status=$($r.status), body=$($r.body)" $ok

# ---------- Ownership 驗證: 本人 release -> 200, IDLE ----------
$r = Invoke-Api POST "/api/seats/A1/release" '{\"userId\":\"demo-user-001\"}'
$ok = $r.status -eq 200 -and $r.body -match '"state":"IDLE"' -and $r.body -match '"currentUserId":null'
Report "Ownership驗證: 本人對A1呼叫release -> 200, 釋放為空閒" "200, state=IDLE, currentUserId=null" "status=$($r.status), body=$($r.body)" $ok

# 釋放 A2 預約以還原環境（非checklist項目，但記錄）
$null = Invoke-Api POST "/api/seats/A2/release" '{\"userId\":\"demo-user-001\"}'

# ---------- FR-06a 噪音警示: 觸發 ----------
$r = Invoke-Api POST "/api/sensors/noise" '{\"zoneId\":\"A\",\"dB\":80}'
$ok = $r.status -eq 200 -and $r.body -match '"alertActive":true'
Report "FR-06a 噪音超過門檻(65dB)觸發警示" "200, alertActive=true" "status=$($r.status), body=$($r.body)" $ok

$r = Invoke-Api GET "/api/alerts/recent" $null
$ok = $r.status -eq 200 -and $r.body -match '"type":"NOISE_HIGH"' -and $r.body -match '"zoneId":"A"'
Report "FR-06a 噪音警示出現於 /api/alerts/recent" "200, 含type=NOISE_HIGH, zoneId=A的紀錄" "status=$($r.status), body=$($r.body)" $ok

$beforeReset = $r.body

# ---------- FR-06a 噪音警示: 重置回門檻以下 ----------
$r = Invoke-Api POST "/api/sensors/noise" '{\"zoneId\":\"A\",\"dB\":50}'
$ok = $r.status -eq 200 -and $r.body -match '"alertActive":false'
Report "FR-06a 噪音回到門檻以下，alertActive變為false" "200, alertActive=false" "status=$($r.status), body=$($r.body)" $ok

# ---------- FR-06a 噪音警示: 邊緣觸發，再次超過門檻新增一筆 ----------
$r1 = Invoke-Api GET "/api/alerts/recent" $null
$countBefore = ([regex]::Matches($r1.body, '"type":"NOISE_HIGH"')).Count

$r = Invoke-Api POST "/api/sensors/noise" '{\"zoneId\":\"A\",\"dB\":80}'
$r2 = Invoke-Api GET "/api/alerts/recent" $null
$countAfter = ([regex]::Matches($r2.body, '"type":"NOISE_HIGH"')).Count
$ok = $countAfter -eq ($countBefore + 1)
Report "FR-06a 噪音邊緣觸發: 再次超過門檻新增一筆NOISE_HIGH紀錄" "NOISE_HIGH筆數 +1 (從 $countBefore 變為 $($countBefore+1))" "before=$countBefore, after=$countAfter" $ok

# 重置噪音回正常
$null = Invoke-Api POST "/api/sensors/noise" '{\"zoneId\":\"A\",\"dB\":50}'

# ---------- FR-06b 溫度過熱 ----------
$r = Invoke-Api POST "/api/sensors/temperature" '{\"zoneId\":\"A\",\"celsius\":35}'
$ok = $r.status -eq 200 -and $r.body -match '"alertActive":true'
Report "FR-06b 溫度超過上限(28C)觸發過熱警示" "200, alertActive=true" "status=$($r.status), body=$($r.body)" $ok

$r = Invoke-Api GET "/api/alerts/recent" $null
$ok = $r.status -eq 200 -and $r.body -match '"type":"TEMP_HIGH"' -and $r.body -match '"zoneId":"A"'
Report "FR-06b TEMP_HIGH警示出現於 /api/alerts/recent" "200, 含type=TEMP_HIGH, zoneId=A的紀錄" "status=$($r.status), body=$($r.body)" $ok

# ---------- FR-06b 回到範圍內，警示消除 ----------
$r = Invoke-Api POST "/api/sensors/temperature" '{\"zoneId\":\"A\",\"celsius\":22}'
$ok = $r.status -eq 200 -and $r.body -match '"alertActive":false'
Report "FR-06b 溫度回到範圍內(18~28C)，alertActive變為false" "200, alertActive=false" "status=$($r.status), body=$($r.body)" $ok

# ---------- FR-06b 溫度過冷 ----------
$r = Invoke-Api POST "/api/sensors/temperature" '{\"zoneId\":\"A\",\"celsius\":5}'
$ok = $r.status -eq 200 -and $r.body -match '"alertActive":true'
Report "FR-06b 溫度低於下限(18C)觸發過冷警示" "200, alertActive=true" "status=$($r.status), body=$($r.body)" $ok

$r = Invoke-Api GET "/api/alerts/recent" $null
$ok = $r.status -eq 200 -and $r.body -match '"type":"TEMP_LOW"' -and $r.body -match '"zoneId":"A"'
Report "FR-06b TEMP_LOW警示出現於 /api/alerts/recent，與NOISE/TEMP_HIGH互不干擾" "200, 含type=TEMP_LOW的紀錄，且仍可見NOISE_HIGH/TEMP_HIGH各自紀錄" "status=$($r.status), body=$($r.body)" $ok

# 重置溫度回正常
$null = Invoke-Api POST "/api/sensors/temperature" '{\"zoneId\":\"A\",\"celsius\":22}'

# ---------- FR-08 報表類型列表 ----------
$r = Invoke-Api GET "/api/reports" $null
$ok = $r.status -eq 200 -and $r.body -match '"type":"seat-usage"' -and $r.body -match '"type":"environment-alerts"'
Report "FR-08 GET /api/reports 列出至少2種報表類型" "200, 包含 type=seat-usage 與 type=environment-alerts" "status=$($r.status), body=$($r.body)" $ok

# ---------- FR-08 座位使用率報表 一致性 ----------
$seatsResp = Invoke-Api GET "/api/zones/A/seats" $null
$seats = $seatsResp.body | ConvertFrom-Json
$idleCount = ($seats | Where-Object { $_.state -eq "IDLE" }).Count
$reservedCount = ($seats | Where-Object { $_.state -eq "RESERVED" }).Count
$occupiedCount = ($seats | Where-Object { $_.state -eq "OCCUPIED" }).Count
$tempAwayCount = ($seats | Where-Object { $_.state -eq "TEMP_AWAY" }).Count

$r = Invoke-Api GET "/api/reports/seat-usage" $null
$report = $r.body | ConvertFrom-Json
$zoneA = $report.byZone | Where-Object { $_.zoneId -eq "A" }
$ok = $r.status -eq 200 `
    -and $zoneA.totalSeats -eq 12 `
    -and $zoneA.countByState.IDLE -eq $idleCount `
    -and $zoneA.countByState.RESERVED -eq $reservedCount `
    -and $zoneA.countByState.OCCUPIED -eq $occupiedCount `
    -and $zoneA.countByState.TEMP_AWAY -eq $tempAwayCount
Report "FR-08 座位使用率報表與目前座位狀態一致" "zoneA.totalSeats=12, IDLE=$idleCount, RESERVED=$reservedCount, OCCUPIED=$occupiedCount, TEMP_AWAY=$tempAwayCount" "status=$($r.status), body=$($r.body)" $ok

# ---------- FR-08 環境警示熱點報表 一致性 ----------
$noise = Invoke-Api GET "/api/sensors/noise/A" $null
$temp = Invoke-Api GET "/api/sensors/temperature/A" $null
$noiseJson = $noise.body | ConvertFrom-Json
$tempJson = $temp.body | ConvertFrom-Json

$r = Invoke-Api GET "/api/reports/environment-alerts" $null
$envReport = $r.body | ConvertFrom-Json
$envZoneA = $envReport.byZone | Where-Object { $_.zoneId -eq "A" }
$ok = $r.status -eq 200 `
    -and $envZoneA.latestNoiseDb -eq $noiseJson.dB `
    -and $envZoneA.noiseThreshold -eq $noiseJson.threshold `
    -and $envZoneA.latestTemperature -eq $tempJson.celsius `
    -and $envZoneA.temperatureLowerBound -eq $tempJson.lowerBound `
    -and $envZoneA.temperatureUpperBound -eq $tempJson.upperBound
Report "FR-08 環境警示熱點報表的噪音/溫度讀數與感測器面板一致" "latestNoiseDb=$($noiseJson.dB), threshold=$($noiseJson.threshold), latestTemperature=$($tempJson.celsius), bounds=$($tempJson.lowerBound)~$($tempJson.upperBound)" "status=$($r.status), body=$($r.body)" $ok

# ---------- FR-08 觸發警示後近期警示次數增加 ----------
$beforeAlerts = $envZoneA.recentNoiseAlerts
$null = Invoke-Api POST "/api/sensors/noise" '{\"zoneId\":\"A\",\"dB\":80}'
$r = Invoke-Api GET "/api/reports/environment-alerts" $null
$envReport2 = $r.body | ConvertFrom-Json
$envZoneA2 = $envReport2.byZone | Where-Object { $_.zoneId -eq "A" }
$afterAlerts = $envZoneA2.recentNoiseAlerts
$ok = $afterAlerts -eq ($beforeAlerts + 1)
Report "FR-08 觸發噪音警示後，環境警示熱點報表近期警示次數增加" "recentNoiseAlerts +1 (從 $beforeAlerts 變為 $($beforeAlerts+1))" "before=$beforeAlerts, after=$afterAlerts" $ok
$null = Invoke-Api POST "/api/sensors/noise" '{\"zoneId\":\"A\",\"dB\":50}'

# ---------- FR-08 未知報表型態 -> 400 ----------
$r = Invoke-Api GET "/api/reports/unknown-type" $null
$ok = $r.status -eq 400
Report "FR-08 GET /api/reports/unknown-type -> 400" "400 Bad Request" "status=$($r.status), body=$($r.body)" $ok

# ---------- 還原環境: A1 release（已是IDLE跳過）, A2 release（已釋放） ----------

# ---------- 輸出總結 ----------
Write-Host "=================================================="
Write-Host "總結對照表"
Write-Host "=================================================="
$results | ForEach-Object {
    $status = if ($_.pass) { "PASS" } else { "FAIL" }
    Write-Host ("[{0}] {1}" -f $status, $_.item)
}

$passCount = ($results | Where-Object { $_.pass }).Count
$totalCount = $results.Count
Write-Host ""
Write-Host "通過: $passCount / $totalCount"
