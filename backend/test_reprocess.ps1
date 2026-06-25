$ErrorActionPreference = "Stop"

# Login
$loginBody = @{
    email = "test@example.com"
    password = "password123"
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
$token = $loginResponse.data.token
Write-Host "Got token"

$headers = @{
    "Authorization" = "Bearer $token"
}

$docIds = @(
    "29f679e6-5f96-46cf-bb29-a8ed70e35a65",
    "9c853e57-4322-49d7-9bdf-d648006793cc",
    "6eabad0c-be42-4a27-8090-2f868f39a15b",
    "b1f92569-2466-4824-8633-118a4978c7c8"
)

foreach ($id in $docIds) {
    Write-Host "Reprocessing document $id"
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:8080/api/documents/$id/reprocess" -Method Post -Headers $headers
        Write-Host "Status: $($response.status)"
    } catch {
        Write-Host "Failed to reprocess $id : $_"
    }
}
