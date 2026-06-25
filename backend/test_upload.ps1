$ErrorActionPreference = "Stop"

# Create a sample text file
$sampleText = "This is a test document for the knowledge base. It contains some sample text that will be extracted by Apache Tika and chunked into smaller pieces."
Set-Content -Path "test_doc.txt" -Value $sampleText

# 1. Register a test user (ignore error if already exists)
$registerBody = @{
    fullName = "Test User"
    email = "test@example.com"
    password = "password123"
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method Post -Body $registerBody -ContentType "application/json"
    Write-Host "Registered user."
} catch {
    Write-Host "User might already exist."
}

# 2. Login
$loginBody = @{
    email = "test@example.com"
    password = "password123"
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
$token = $loginResponse.data.token
Write-Host "Got token: $token"

# 3. Upload Document using curl.exe because Invoke-RestMethod multipart is annoying in old PowerShell
$curlArgs = @(
    "-X", "POST", "http://localhost:8080/api/documents",
    "-H", "Authorization: Bearer $token",
    "-F", "file=@test_doc.txt",
    "-F", "title=Test Document",
    "-F", "description=Testing async chunks",
    "-F", "visibility=PRIVATE"
)
Write-Host "Uploading document..."
& curl.exe $curlArgs
Write-Host "`nUpload finished."
