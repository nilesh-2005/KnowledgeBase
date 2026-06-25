$ErrorActionPreference = "Stop"

# Login as ADMIN
$loginBody = @{
    email = "test@example.com"
    password = "password123" 
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
    $token = $loginResponse.data.token
    Write-Host "Got token for ADMIN"

    $headers = @{
        "Authorization" = "Bearer $token"
    }

    $docs = Invoke-RestMethod -Uri "http://localhost:8080/api/documents?size=100" -Headers $headers
    Write-Host "Documents fetched:"
    foreach ($doc in $docs.content) {
        Write-Host " - $($doc.title) (Owner: $($doc.ownerId), Visibility: $($doc.visibility))"
    }

} catch {
    Write-Host "Login failed: $_"
}
