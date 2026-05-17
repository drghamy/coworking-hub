$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$frontend = Join-Path $root "frontend"

Write-Host "Starting frontend on http://localhost:5500"
Start-Process -FilePath "cmd.exe" -ArgumentList "/k", "cd /d `"$frontend`" && python -m http.server 5500"
