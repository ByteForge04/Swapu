[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$dir2 = 'd:\SwapU\' + [char]0x8981 + [char]0x6C42 + [char]0x548C + [char]0x6A21 + [char]0x677F
$files = [System.IO.Directory]::GetFiles($dir2)
foreach ($f in $files) {
    if ($f -match '\.doc$') {
        Write-Host "Found: $f"
    }
}
$targetFile = Join-Path $dir2 ([char]0x6B66 + [char]0x6C49 + [char]0x7406 + [char]0x5DE5 + [char]0x5927 + [char]0x5B66 + [char]0x6BD5 + [char]0x4E1A + [char]0x8BBE + [char]0x8BA1 + [char]0x5468 + [char]0x8BB0 + '.doc')
Write-Host "Target: $targetFile"
$word = New-Object -ComObject Word.Application
$word.Visible = $false
$doc = $word.Documents.Open($targetFile)
$text = $doc.Content.Text
$doc.Close()
$word.Quit()
[Console]::WriteLine($text)
