/**
 * 从中国研究生招生信息网（研招网）院校库抓取全国研究生招生单位列表。
 * 用法: node scripts/fetch-chsi-schools.mjs
 * 输出: backend/src/main/resources/schools.csv（UTF-8）
 */
import { writeFileSync } from 'node:fs'
import { resolve, dirname } from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const OUT = resolve(__dirname, '../backend/src/main/resources/schools.csv')
const BASE = 'https://yz.chsi.com.cn/sch/search.do?start='
const sleep = (ms) => new Promise((r) => setTimeout(r, ms))

async function fetchPage(start) {
  const res = await fetch(BASE + start, {
    headers: { 'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120 Safari/537.36' }
  })
  if (!res.ok) throw new Error(`HTTP ${res.status} @ start=${start}`)
  return res.text()
}

function parseSchools(html) {
  const items = html.match(/<div class="sch-item">[\s\S]*?<\/div>\s*<\/div>\s*<\/div>/g) || []
  const rows = []
  for (const block of items) {
    const name = block.match(/<a class="name[^"]*"[^>]*>\s*([^<]+?)\s*<\/a>/)?.[1]?.trim()
    const schId = block.match(/schoolInfo--schId-(\d+)/)?.[1]
    const province = block.match(/&#xe6a4;<\/i>\s*([^<\s]+)/)?.[1]?.trim()
    const dept = block.match(/主管部门：<\/span>\s*([^<]+?)\s*(?:<|$)/)?.[1]?.trim()
    if (!name || !schId) continue
    const isDoubleFirst = block.includes('“双一流”建设高校')
    const hasGradSchool = block.includes('研究生院')
    const selfLine = block.includes('自划线')
    const level = selfLine ? '985/211/双一流' : isDoubleFirst ? '双一流' : hasGradSchool ? '研究生院' : ''
    rows.push({
      name,
      province: province || '',
      dept: dept || '',
      level,
      admissionUrl: `https://yz.chsi.com.cn/sch/schoolInfo--schId-${schId}.dhtml`
    })
  }
  return rows
}

function toCsv(rows) {
  const esc = (v) => `"${String(v ?? '').replaceAll('"', '""')}"`
  const lines = ['name,province,dept,level,admissionUrl']
  for (const r of rows) lines.push([r.name, r.province, r.dept, r.level, r.admissionUrl].map(esc).join(','))
  return lines.join('\r\n') + '\r\n'
}

// 第一页解析总页数（取所有页码链接中的最大值）
const first = await fetchPage(0)
const pageNums = [...first.matchAll(/start=\d+'>(\d+)<\/a>/g)].map((m) => Number(m[1]))
const totalPages = Math.max(1, ...pageNums)
console.log(`总页数: ${totalPages}`)

const all = new Map()
for (let page = 0; page < totalPages; page++) {
  const html = page === 0 ? first : await fetchPage(page * 20)
  for (const row of parseSchools(html)) {
    if (!all.has(row.name)) all.set(row.name, row)
  }
  await sleep(200)
  console.log(`第 ${page + 1}/${totalPages} 页完成，累计 ${all.size} 条`)
}

const rows = [...all.values()]
writeFileSync(OUT, toCsv(rows), 'utf8')
console.log(`完成，共 ${rows.length} 个招生单位，已写入 ${OUT}`)
