$ports = @(8080, 8081, 8082, 8083, 8761)

foreach ($port in $ports) {
    $connections = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue

    foreach ($connection in $connections) {
        $pid = $connection.OwningProcess
        if ($pid -and $pid -ne 0) {
            Write-Host "Stopping process $pid on port $port"
            Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
        }
    }
}
