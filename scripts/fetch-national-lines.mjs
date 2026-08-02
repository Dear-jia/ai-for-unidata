/**
 * 抓取 2024/2025/2026 考研国家线（学硕+专硕表）并输出 CSV
 * 输出: backend/src/main/resources/national-lines.csv
 */
import { writeFileSync } from 'node:fs'
import { resolve, dirname } from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const OUT = resolve(__dirname, '../backend/src/main/resources/national-lines.csv')
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120 Safari/537.36'

async function get(url) {
  const res = await fetch(url, { headers: { 'User-Agent': UA } })
  if (!res.ok) throw new Error(`HTTP ${res.status} ${url}`)
  return res.text()
}

function parseTables(html) {
  const tables = [...html.matchAll(/<table[\s\S]*?<\/table>/gi)]
  const rows = []
  for (const t of tables) {
    for (const tr of t[0].matchAll(/<tr[\s\S]*?<\/tr>/gi)) {
      const cells = [...tr[0].matchAll(/<t[dh][^>]*>([\s\S]*?)<\/t[dh]>/gi)]
        .map((c) => c[1].replace(/<[^>]+>/g, '').replace(/&nbsp;/g, ' ').replace(/&gt;/g, '>').replace(/&lt;/g, '<').replace(/&#\d+;/g, '').trim())
      if (cells.length) rows.push(cells)
    }
  }
  return rows
}

function isScoreRow(cells) {
  if (cells.length < 6) return false
  const joined = cells.join(' ')
  if (/^(学科门类|专业名称|地区|备注)/.test(joined)) return false
  if (/^[①②③④⑤⑥⑦]/.test(cells[0] || '')) return false
  if (!/\d{3}/.test(joined)) return false
  if (!/^[\u4e00-\u9fa5A-Za-z[]/.test(cells[0] || '')) return false
  if (cells[0] === '总分' || (cells[0] || '').includes('A类考生') || (cells[0] || '').includes('B类考生')) return false
  return true
}

const num = (s) => {
  const m = String(s ?? '').match(/\d{2,3}/)
  return m ? Number(m[0]) : null
}

const rows = []

// ---- 2026 / 2025：研招网官方表 ----
for (const [year, url] of [
  [2026, 'https://yz.chsi.com.cn/kyzx/kp/202602/20260228/2293449093.html'],
  [2025, 'https://yzst.chsi.com.cn/kyzx/kp/202502/20250224/2293352975.html']
]) {
  const html = await get(url)
  const body = html.match(/id="article_dnull">([\s\S]*?)<div id="dzz">/)?.[1] ?? html
  let count = 0
  let parentDiscipline = ''
  for (const cells of parseTables(body)) {
    if (!isScoreRow(cells)) continue
    let discipline, subjects, totalA, oneA, overA, totalB, oneB, overB, note
    const hasSubjCell = cells.length >= 9 && /^\d{2,3}$/.test((cells[1] || '').trim()) === false
    if (hasSubjCell) {
      // 9 列: 学科门类 | 学科专业 | A总分 | A单科100 | A单科>100 | B总分 | B单科100 | B单科>100 | 骨干总分 | 备注
      discipline = (cells[0] || '').trim()
      subjects = (cells[1] || '').trim()
      totalA = num(cells[2]); oneA = num(cells[3]); overA = num(cells[4])
      totalB = num(cells[5]); oneB = num(cells[6]); overB = num(cells[7])
      note = (cells[9] || '').slice(0, 480)
    } else {
      // 8 列（合并单元格）: 门类/专业 | A总分 | A单科100 | A单科>100 | B总分 | B单科100 | B单科>100 | 骨干总分
      discipline = (cells[0] || '').trim()
      subjects = '各学科专业'
      totalA = num(cells[1]); oneA = num(cells[2]); overA = num(cells[3])
      totalB = num(cells[4]); oneB = num(cells[5]); overB = num(cells[6])
      note = ''
    }
    discipline = discipline.replace(/\[[^\]]+\]/g, '').replace(/[⑤⑥⑦④③①②]/g, '').trim()
    if (!discipline || discipline === '总分') continue
    if (hasSubjCell && /^[\u4e00-\u9fa5]{2,}$/.test(discipline) && !discipline.includes('其他')) parentDiscipline = discipline
    if (discipline === '其他学科专业' && parentDiscipline) {
      discipline = parentDiscipline + '（其他学科专业）'
    }
    rows.push({
      year,
      discipline,
      subjects,
      totalA, oneA, overA, totalB, oneB, overB, note
    })
    count++
  }
  console.log(`${year}: ${count} rows`)
}

// ---- 2024：中公教育汇总表（一区/二区） ----
{
  const url = 'http://m.offcn.com/kaoyan/2024/0312/263457.html'
  const html = await get(url)
  let zone = ''
  let count = 0
  for (const cells of parseTables(html)) {
    const joined = cells.join(' ')
    if (/^一区$/.test(cells[0] || '')) zone = 'A'
    if (/^二区$/.test(cells[0] || '')) zone = 'B'
    if (zone && /^\d{3}$/.test(cells[2] || '')) {
      // 地区 | 专业名称 | 总分 | 单科<100 | 单科>100
      const discipline = (cells[1] || '').replace(/\[[^\]]+\]$/, '').trim()
      if (!discipline || discipline === '专业名称') continue
      const target = rows.find((r) => r.year === 2024 && r.discipline === discipline)
      if (target) {
        if (zone === 'A') {
          target.totalA = num(cells[2]); target.oneA = num(cells[3]); target.overA = num(cells[4])
        } else {
          target.totalB = num(cells[2]); target.oneB = num(cells[3]); target.overB = num(cells[4])
        }
      } else {
        rows.push({
          year: 2024,
          discipline,
          subjects: '各学科专业',
          totalA: zone === 'A' ? num(cells[2]) : null,
          oneA: zone === 'A' ? num(cells[3]) : null,
          overA: zone === 'A' ? num(cells[4]) : null,
          totalB: zone === 'B' ? num(cells[2]) : null,
          oneB: zone === 'B' ? num(cells[3]) : null,
          overB: zone === 'B' ? num(cells[4]) : null,
          note: ''
        })
      }
      count++
    }
  }
  console.log(`2024: ${count} zone-rows`)
}

// ---- 排序：year desc, 原顺序 ----
const out = []
const esc = (v) => `"${String(v ?? '').replaceAll('"', '""')}"`
out.push('year,discipline,subjects,totalA,oneA,overA,totalB,oneB,overB,note')
for (const r of rows.sort((a, b) => b.year - a.year)) {
  out.push([r.year, r.discipline, r.subjects, r.totalA, r.oneA, r.overA, r.totalB, r.oneB, r.overB, r.note].map(esc).join(','))
}
writeFileSync(OUT, out.join('\r\n') + '\r\n', 'utf8')
console.log(`写入 ${OUT}，共 ${rows.length} 行`)
