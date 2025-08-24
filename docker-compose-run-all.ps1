docker-compose up -d --build
if ($LASTEXITCODE -ne 0) { throw "docker-compose up failed" }

$consulUrl = "http://localhost:8500/v1/status/leader"
$timeout = 60
$interval = 2
$elapsed = 0
while ($true) {
  try {
    $leader = Invoke-RestMethod -Uri $consulUrl -Method Get -TimeoutSec 2 -ErrorAction Stop
  } catch {
    $leader = $null
  }
  if ($leader -and $leader -ne "null") { break }
  if ($elapsed -ge $timeout) { Write-Warning "Consul not ready after $timeout s, continue anyway"; break }
  Start-Sleep -Seconds $interval
  $elapsed += $interval
}

docker-compose run --rm consul-import