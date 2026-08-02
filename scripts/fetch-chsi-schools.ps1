<#
  .SYNOPSIS
  从中国研究生招生信息网（研招网）院校库抓取全国研究生招生单位列表。

  .DESCRIPTION
  抓取 https://yz.chsi.com.cn/sch/search.do?start=N 全部分页，
  解析招生单位名称、所在地、主管部门、双一流/研究生院/自划线特性，
  输出为 schools.csv（UTF-8 无 BOM），供平台种子数据与批量导入使用。
#>
$ErrorActionPreference = "Stop"

$outFile = Join-Path $PSScriptRoot "..\backend\src\main\resources\schools.csv"
$baseUrl = "https://yz.chsi.com.cn/sch/search.do?start={0}"
$rows = New-Object System.Collections.Generic.List[object]

# 先请求第 1 页，从分页信息里解析总页数（最多 60 页保护）
$first = Invoke-WebRequest -Uri ($baseUrl -f 0) -UseBasicParsing -TimeoutSec 30
$firstContent = [string]$first.Content
$lastPageMatch = [regex]::Match($firstContent, "start=(\d+)'>(\d+)</a>")
$totalPages = 1
if ($lastPageMatch.Success) {
    $totalPages = [int]$lastPageMatch.Groups[2].Value
}
Write-Host "总页数: $totalPages"

for ($page = 0; $page -lt $totalPages; $page++) {
    $start = $page * 20
    if ($page -eq 0) {
        $content = $firstContent
    } else {
        $content = [string](Invoke-WebRequest -Uri ($baseUrl -f $start) -UseBasicParsing -TimeoutSec 30).Content
        Start-Sleep -Milliseconds 250
    }

    # 每个院校条目是一个 <div class="sch-item"> 块
    $items = [regex]::Matches($content, '(?s)<div class="sch-item">.*?</div>\s*</div>\s*</div>')
    foreach ($m in $items) {
        $block = $m.Value
        $nameMatch = [regex]::Match($block, '<a class="name[^"]*"[^>]*>\s*([^<]+?)\s*</a>')
        $schIdMatch = [regex]::Match($block, 'schoolInfo--schId-(\d+)')
        $provinceMatch = [regex]::Match($block, '&#xe6a4;</i>\s*([^<\s]+)')
        $deptMatch = [regex]::Match($block, '主管部门：</span>\s*([^<]+?)\s*(?:<|$)')
        if (-not $nameMatch.Success -or -not $schIdMatch.Success) {
            continue
        }

        $name = $nameMatch.Groups[1].Value.Trim()
        $province = if ($provinceMatch.Success) { $provinceMatch.Groups[1].Value.Trim() } else { "" }
        $dept = if ($deptMatch.Success) { $deptMatch.Groups[1].Value.Trim() } else { "" }
        $isDoubleFirst = $block -match "“双一流”建设高校"
        $hasGradSchool = $block -match "研究生院"
        $selfLine = $block -match "自划线"

        if ($selfLine) { $level = "985/211/双一流" }
        elseif ($isDoubleFirst) { $level = "双一流" }
        elseif ($hasGradSchool) { $level = "研究生院" }
        else { $level = "" }

        $rows.Add([pscustomobject]@{
            name         = $name
            province     = $province
            dept         = $dept
            level        = $level
            admissionUrl = "https://yz.chsi.com.cn/sch/schoolInfo--schId-$($schIdMatch.Groups[1].Value).dhtml"
        })
    }
    Write-Host ("第 {0}/{1} 页完成，累计 {2} 条" -f ($page + 1), $totalPages, $rows.Count)
}

# 去重（按名称）
$unique = $rows | Sort-Object name -Unique
Write-Host "去重后: $($unique.Count) 条"

# 写 CSV（UTF-8 无 BOM，Excel 可用）
$sb = New-Object System.Text.StringBuilder
[void]$sb.AppendLine("name,province,dept,level,admissionUrl")
foreach ($row in $unique) {
    [void]$sb.AppendLine(('"{0}","{1}","{2}","{3}","{4}"' -f
        $row.name, $row.province, $row.dept, $row.level, $row.admissionUrl))
}
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllText($outFile, $sb.ToString(), $utf8NoBom)
Write-Host "已写入: $outFile"
