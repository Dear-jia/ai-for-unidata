/**
 * 抓取 34 所自划线院校 2025/2026 复试线专栏文章：
 * 1) 正文官方图片 URL + 原文链接 -> school-score-sources.csv
 * 2) 正文 HTML 表格 -> school-score-lines.csv
 */
import { writeFileSync } from 'node:fs'
import { resolve, dirname } from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const OUT = resolve(__dirname, '../backend/src/main/resources/school-score-sources.csv')
const OUT_LINES = resolve(__dirname, '../backend/src/main/resources/school-score-lines.csv')
const LIST_URL = 'https://yz.chsi.com.cn/kyzx/fsfsx34/'
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120 Safari/537.36'
const sleep = (ms) => new Promise((r) => setTimeout(r, ms))

async function get(url) {
  const res = await fetch(url, { headers: { 'User-Agent': UA } })
  if (!res.ok) throw new Error(`HTTP ${res.status} ${url}`)
  return res.text()
}

function schoolFromTitle(title) {
  let t = title.replace(/\d{4}年/g, '').trim()
  const cut = t.indexOf('硕士')
  if (cut > 0) t = t.slice(0, cut)
  t = t.replace(/(全国|入学考试|招生考试|复试|初试)+$/, '').trim()
  return t
}

function parseScoreTables(html) {
  const tables = [...html.matchAll(/<table[\s\S]*?<\/table>/gi)]
  const lines = []
  for (const t of tables) {
    const rows = [...t[0].matchAll(/<tr[\s\S]*?<\/tr>/gi)]
    let parent = ''
    for (const tr of rows) {
      const cells = [...tr[0].matchAll(/<t[dh][^>]*>([\s\S]*?)<\/t[dh]>/gi)]
        .map((c) => c[1].replace(/<[^>]+>/g, '').replace(/&nbsp;/g, ' ').replace(/&gt;/g, '>').replace(/&lt;/g, '<').trim())
      if (cells.length < 3) continue
      const first = cells[0] || ''
      const last = cells[cells.length - 1]
      if (/学科门类|专业名称|总分/.test(first) || /以下为|说明：|说明:/.test(first) || !/^\d{3}$/.test(last)) continue
      const rowText = cells.join(' ')
      if (/^(少数民族|享受|退役大学生|总分同|比学校)/.test(rowText)) continue
      if (/^[\u4e00-\u9fa5]{2,}[\[]/.test(first) || /^[\u4e00-\u9fa5]{2,}$/.test(first)) parent = first
      const discipline = parent || first
      const subjects = cells.length >= 5 ? cells[1] : ''
      lines.push({
        discipline: discipline.replace(/\[[^\]]+\]/g, '').trim(),
        subjects,
        oneHundred: /^\d{2,3}$/.test(cells[cells.length - 3] || '') ? cells[cells.length - 3] : '',
        overHundred: /^\d{2,3}$/.test(cells[cells.length - 2] || '') ? cells[cells.length - 2] : '',
        total: last,
        note: cells.length > 5 ? cells.slice(1, cells.length - 3).filter((c) => c && !/^\d{2,3}$/.test(c)).join(' ') : ''
      })
    }
  }
  return lines
}

const listHtml = await get(LIST_URL)
const links = [...listHtml.matchAll(/<a[^>]+href="(\/kyzx\/fsfsx34\/20\d{4}\/20\d{6}\/\d+\.html)"[^>]*>([\s\S]*?)<\/a>/g)]
  .map((m) => ({ url: 'https://yz.chsi.com.cn' + m[1], text: m[2].replace(/<[^>]+>/g, '').replace(/&nbsp;/g, ' ').trim() }))
  .filter((x) => x.text.length > 5)

const seen = new Set()
const uniqueLinks = links.filter((x) => (seen.has(x.url) ? false : (seen.add(x.url), true)))
console.log('专栏文章总数:', uniqueLinks.length)

const rows = []
const lineRows = []
for (const link of uniqueLinks) {
  const year = /(20\d{2})年/.test(link.text) ? Number(link.text.match(/(20\d{2})年/)[1]) : Number(link.url.match(/\/20(\d{2})0/)?.[1] ?? 0)
  if (year !== 2025 && year !== 2026) continue
  const school = schoolFromTitle(link.text)
  if (!school) continue
  let html
  try {
    html = await get(link.url)
  } catch (e) {
    console.log('FETCH ERR', link.url, e.message)
    continue
  }
  const body = html.match(/id="article_dnull">([\s\S]*?)<div id="dzz">/)?.[1] ?? ''
  const imgs = [...body.matchAll(/<img[^>]+src="([^"]+)"/g)].map((m) => m[1])
    .filter((u) => /news\/img/.test(u) && /\.(png|jpe?g|gif)$/i.test(u))
  const tableLines = parseScoreTables(body)
  if (imgs.length === 0 && tableLines.length === 0) {
    console.log('NO IMG', school, year, link.url)
    continue
  }
  for (const ln of tableLines) {
    lineRows.push({ school, year, ...ln })
  }
  for (const img of imgs) {
    rows.push({ school, year, title: link.text, imageUrl: img, sourceUrl: link.url })
  }
  console.log(`ok ${school} ${year} imgs=${imgs.length} tableRows=${tableLines.length}`)
  await sleep(150)
}

const out = ['school,year,title,imageUrl,sourceUrl']
const esc = (v) => `"${String(v ?? '').replaceAll('"', '""')}"`
for (const r of rows) out.push([r.school, r.year, r.title, r.imageUrl, r.sourceUrl].map(esc).join(','))
writeFileSync(OUT, out.join('\r\n') + '\r\n', 'utf8')

const out2 = ['school,year,discipline,subjects,oneHundred,overHundred,total,note']
for (const r of lineRows) {
  out2.push([r.school, r.year, r.discipline, r.subjects, r.oneHundred, r.overHundred, r.total, r.note].map(esc).join(','))
}
writeFileSync(OUT_LINES, out2.join('\r\n') + '\r\n', 'utf8')

const bySchool = new Map()
for (const r of rows) {
  const k = r.school + '-' + r.year
  bySchool.set(k, (bySchool.get(k) || 0) + 1)
}
console.log(`\n写入 ${OUT}，共 ${rows.length} 行`)
console.log('学校-年份组合数:', bySchool.size)
console.log(`写入 ${OUT_LINES}，共 ${lineRows.length} 行文本分数线`)
