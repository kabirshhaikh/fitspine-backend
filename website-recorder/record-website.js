#!/usr/bin/env node
/**
 * Website Video Recorder
 * Records a video of https://sphinic.com (or any URL you specify)
 *
 * Usage:
 *   node record-website.js                    # Records sphinic.com for 10 seconds
 *   node record-website.js --url <URL>        # Custom URL
 *   node record-website.js --duration <sec>   # Custom duration in seconds
 *   node record-website.js --output <path>    # Custom output path
 */

const { chromium } = require('playwright');
const path = require('path');
const fs = require('fs');

// Parse command-line arguments
const args = process.argv.slice(2);
function getArg(name, defaultValue) {
  const i = args.indexOf(name);
  return i !== -1 && args[i + 1] ? args[i + 1] : defaultValue;
}

const URL = getArg('--url', 'https://sphinic.com');
const DURATION_SEC = parseInt(getArg('--duration', '10'), 10);
const OUTPUT_DIR = getArg('--output', path.join(__dirname, 'recordings'));

// Ensure output directory exists
if (!fs.existsSync(OUTPUT_DIR)) {
  fs.mkdirSync(OUTPUT_DIR, { recursive: true });
}

async function main() {
  console.log('🎬 Website Video Recorder');
  console.log('------------------------');
  console.log(`URL:       ${URL}`);
  console.log(`Duration:  ${DURATION_SEC} seconds`);
  console.log(`Output:    ${OUTPUT_DIR}`);
  console.log('');

  const browser = await chromium.launch({
    headless: false,  // Show the browser so you can see what's being recorded
    args: ['--start-maximized'],
  });

  const context = await browser.newContext({
    viewport: { width: 1920, height: 1080 },
    recordVideo: {
      dir: OUTPUT_DIR,
      size: { width: 1920, height: 1080 },
    },
  });

  const page = await context.newPage();

  console.log('⏳ Navigating to website...');
  await page.goto(URL, { waitUntil: 'networkidle' });

  console.log(`⏺️  Recording for ${DURATION_SEC} seconds... (you can interact with the page)`);
  await page.waitForTimeout(DURATION_SEC * 1000);

  console.log('⏹️  Stopping recording...');
  await context.close();
  await browser.close();

  console.log('');
  console.log('✅ Recording complete! Check the "recordings" folder for your video.');
}

main().catch((err) => {
  console.error('Error:', err);
  process.exit(1);
});
