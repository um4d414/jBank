# language: powershell
param(
  [string]$ConsulAddr = $(if ($env:CONSUL_ADDR) { $env:CONSUL_ADDR } else { "http://localhost:8500" }),
  [string]$EnvName = "local",
  [string]$RootDir = ".\configs",
  [int]$WaitTimeout = 30,
  [int]$WaitInterval = 2
)

Write-Host "Import (local) $RootDir\$EnvName -> $ConsulAddr/v1/kv/config/{service}/data"
Write-Host "Consul address: $ConsulAddr"

function Wait-ForConsul {
  $elapsed = 0
  while ($true) {
    try { $leader = Invoke-RestMethod -Uri "$ConsulAddr/v1/status/leader" -Method Get -TimeoutSec 2 -ErrorAction Stop } catch { $leader = $null }
    if ($leader -and $leader -ne "null") { Write-Host "Consul leader: $leader"; return $true }
    if ($elapsed -ge $WaitTimeout) { Write-Error "Consul not ready after $WaitTimeout s"; return $false }
    Start-Sleep -Seconds $WaitInterval
    $elapsed += $WaitInterval
  }
}

if (-not (Wait-ForConsul)) { exit 2 }

$envPath = Join-Path $RootDir $EnvName
if (-not (Test-Path $envPath)) {
  Write-Host "Directory $envPath not found, nothing to import"
  exit 0
}

# collect services from top-level files and directories
$services = @()
Get-ChildItem -Path $envPath -File | ForEach-Object {
  if ($_.Name -match '^(.+)\.(ya?ml|yaml|properties)$') { $svc = $Matches[1]; if (-not ($services -contains $svc)) { $services += $svc } }
}
Get-ChildItem -Path $envPath -Directory | ForEach-Object { if (-not ($services -contains $_.Name)) { $services += $_.Name } }

if ($services.Count -eq 0) { Write-Host "No services found under $envPath"; exit 0 }
Write-Host "Found services: $($services -join ', ')"

foreach ($svc in $services) {
  Write-Host "-> processing service '$svc'"
  $tmp = [System.IO.Path]::GetTempFileName()
  Remove-Item $tmp -ErrorAction SilentlyContinue
  New-Item -ItemType File -Path $tmp | Out-Null

  $topYml = Join-Path $envPath ("$svc.yml")
  $topYaml = Join-Path $envPath ("$svc.yaml")
  $topProps = Join-Path $envPath ("$svc.properties")
  $svcDir = Join-Path $envPath $svc

  try {
    if (Test-Path $topYml) {
      Write-Host "Using top-level: $topYml"
      Get-Content -Path $topYml -Raw | Out-File -FilePath $tmp -Encoding utf8
    } elseif (Test-Path $topYaml) {
      Write-Host "Using top-level: $topYaml"
      Get-Content -Path $topYaml -Raw | Out-File -FilePath $tmp -Encoding utf8
    } elseif (Test-Path $topProps) {
      Write-Host "Using top-level: $topProps"
      Get-Content -Path $topProps -Raw | Out-File -FilePath $tmp -Encoding utf8
    } elseif (Test-Path $svcDir) {
      Write-Host "Concatenating files from directory: $svcDir"
      Get-ChildItem -Path $svcDir -Recurse -File | Sort-Object FullName | ForEach-Object {
        Add-Content -Path $tmp -Value "`n---`n"
        Add-Content -Path $tmp -Value (Get-Content -Path $_.FullName -Raw)
      }
    } else {
      Write-Warning "No source files for service $svc (neither top-level nor folder). Skipping."
      Remove-Item -Path $tmp -Force -ErrorAction SilentlyContinue
      continue
    }

    $key = "config/$svc/data"
    $url = "$ConsulAddr/v1/kv/$key"
    Write-Host "PUT -> $url (size: $(Get-Item $tmp).Length bytes)"
    $bytes = [System.IO.File]::ReadAllBytes($tmp)
    Invoke-RestMethod -Uri $url -Method Put -Body $bytes -ContentType "application/octet-stream" -TimeoutSec 30 | Out-Null
    Write-Host "PUT $key OK"
  } catch {
    Write-Error "Failed PUT $key : $_"
  } finally {
    Remove-Item -Path $tmp -Force -ErrorAction SilentlyContinue
  }
}

Write-Host "Import (local) finished."