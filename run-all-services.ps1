$root = Split-Path -Parent $MyInvocation.MyCommand.Path

$services = @(
    @{ Name = "discovery"; Path = Join-Path $root "discovery"; Delay = 18 },
    @{ Name = "user"; Path = Join-Path $root "user"; Delay = 10 },
    @{ Name = "booking"; Path = Join-Path $root "booking"; Delay = 10 },
    @{ Name = "invoice"; Path = Join-Path $root "invoice"; Delay = 10 },
    @{ Name = "gateway"; Path = Join-Path $root "gateway"; Delay = 0 }
)

foreach ($service in $services) {
    Write-Host "Starting $($service.Name)..."
    Start-Process -FilePath "cmd.exe" -ArgumentList "/k", "cd /d `"$($service.Path)`" && mvnw.cmd spring-boot:run"

    if ($service.Delay -gt 0) {
        Start-Sleep -Seconds $service.Delay
    }
}

Write-Host ""
Write-Host "Services are starting:"
Write-Host "- Eureka:  http://localhost:8761"
Write-Host "- Gateway: http://localhost:8080"
Write-Host "- User:    http://localhost:8081"
Write-Host "- Booking: http://localhost:8082"
Write-Host "- Invoice: http://localhost:8083"
