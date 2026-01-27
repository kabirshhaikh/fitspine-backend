# Website Video Recorder

Record a video of [https://sphinic.com](https://sphinic.com) (or any website) using Playwright.

## Prerequisites

- **Node.js** (v16 or newer) — [Download](https://nodejs.org/)

## Quick Start

### 1. Install dependencies

```bash
cd website-recorder
npm install
```

This installs Playwright. The first time you run it, Playwright will download browser binaries (Chromium). This may take a minute.

### 2. Run the recorder

```bash
npm run record
```

Or directly:

```bash
node record-website.js
```

- A Chrome window opens, goes to **https://sphinic.com**, and records for **10 seconds**.
- The video is saved in the `recordings` folder.

## Options

| Option        | Description                    | Default              |
|---------------|--------------------------------|----------------------|
| `--url`       | Website URL to record          | `https://sphinic.com`|
| `--duration`  | Recording length in seconds    | `10`                 |
| `--output`    | Folder to save the video       | `./recordings`       |

### Examples

Record sphinic.com for 30 seconds:

```bash
node record-website.js --duration 30
```

Record a different URL for 15 seconds:

```bash
node record-website.js --url https://example.com --duration 15
```

Save to a custom folder:

```bash
node record-website.js --output ./my-videos
```

## Output

- Videos are saved as `.webm` in the `recordings` folder (or your `--output` path).
- Each run creates a new file (e.g. `page-0.webm`).

## Tips

- The browser runs in **visible mode** (`headless: false`) so you can scroll and click while recording.
- To record in the background, change `headless: false` to `headless: true` in `record-website.js`.
- For longer recordings, increase `--duration` (e.g. `--duration 60` for 1 minute).
