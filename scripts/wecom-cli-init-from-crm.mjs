#!/usr/bin/env node
import { execFileSync } from 'node:child_process'
import crypto from 'node:crypto'
import fs from 'node:fs'
import os from 'node:os'
import path from 'node:path'

const db = {
  host: process.env.CRM_DB_HOST || 'localhost',
  user: process.env.CRM_DB_USER || 'huafu',
  name: process.env.CRM_DB_NAME || 'huafu_crm',
  password: process.env.CRM_DB_PASSWORD || 'huafu_dev_only'
}

function query(sql) {
  return execFileSync('psql', ['-h', db.host, '-U', db.user, '-d', db.name, '-Atc', sql], {
    encoding: 'utf8',
    env: { ...process.env, PGPASSWORD: db.password }
  }).trim()
}

function loadWecomConfig() {
  const rows = query(`
    SELECT config_key || '=' || COALESCE(config_value, '')
    FROM sys_config
    WHERE config_key IN ('wecom.cli_bot_id', 'wecom.cli_bot_secret', 'wecom.cli_config_dir')
    ORDER BY config_key
  `)
  const values = Object.fromEntries(rows.split('\n').filter(Boolean).map(line => line.split(/=(.*)/s).slice(0, 2)))
  const botId = values['wecom.cli_bot_id']?.trim()
  const botSecret = values['wecom.cli_bot_secret']?.trim()
  if (!botId || !botSecret) {
    throw new Error('CRM未配置 wecom.cli_bot_id 或 wecom.cli_bot_secret')
  }
  return {
    botId,
    botSecret,
    configDir: values['wecom.cli_config_dir']?.trim() || path.join(os.homedir(), '.config', 'wecom')
  }
}

function encryptJson(value, key) {
  const nonce = crypto.randomBytes(12)
  const cipher = crypto.createCipheriv('aes-256-gcm', key, nonce)
  const encrypted = Buffer.concat([cipher.update(Buffer.from(JSON.stringify(value))), cipher.final()])
  return Buffer.concat([nonce, encrypted, cipher.getAuthTag()])
}

async function main() {
  const { botId, botSecret, configDir } = loadWecomConfig()
  fs.mkdirSync(configDir, { recursive: true, mode: 0o700 })

  const keyPath = path.join(configDir, '.encryption_key')
  const key = fs.existsSync(keyPath)
    ? Buffer.from(fs.readFileSync(keyPath, 'utf8').trim(), 'base64')
    : crypto.randomBytes(32)
  fs.writeFileSync(keyPath, key.toString('base64'), { mode: 0o600 })

  const bot = { id: botId, secret: botSecret, create_time: Math.floor(Date.now() / 1000) }
  fs.writeFileSync(path.join(configDir, 'bot.enc'), encryptJson(bot, key), { mode: 0o600 })

  const time = Math.floor(Date.now() / 1000)
  const nonce = `mcp_${Date.now()}_${crypto.randomBytes(4).toString('hex')}`
  const signature = crypto.createHash('sha256').update(`${botSecret}${botId}${time}${nonce}`).digest('hex')
  const cliVersion = 'WeComCLI/0.1.8 distribution/npm linux/x64'
  const response = await fetch('https://qyapi.weixin.qq.com/cgi-bin/aibot/cli/get_mcp_config', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'User-Agent': cliVersion },
    body: JSON.stringify({ bot_id: botId, time, nonce, signature, bind_source: 1, cli_version: cliVersion })
  })

  const bodyText = await response.text()
  if (!response.ok) {
    throw new Error(`获取企微CLI配置失败：HTTP ${response.status} ${bodyText.slice(0, 300)}`)
  }
  const body = JSON.parse(bodyText)
  if (body.errcode !== 0) {
    throw new Error(`企微凭证验证失败：${body.errcode} ${body.errmsg || ''}`)
  }
  const configs = Array.isArray(body.list) ? body.list : []
  fs.writeFileSync(path.join(configDir, 'mcp_config.enc'), encryptJson(configs, key), { mode: 0o600 })

  const summary = configs.map(item => ({
    biz_type: item.biz_type,
    is_authed: item.is_authed === true,
    has_url: Boolean(item.url)
  }))
  console.log(JSON.stringify({ ok: true, configDir, configs: summary }, null, 2))
  if (!summary.some(item => item.biz_type === 'msg' && item.has_url)) {
    console.error('未获得 msg 消息能力：企业微信后台需要给该 Bot 开通/授权消息能力，否则群消息不会进入CRM。')
    process.exitCode = 3
  }
}

main().catch(error => {
  console.error(error.message)
  process.exit(1)
})
