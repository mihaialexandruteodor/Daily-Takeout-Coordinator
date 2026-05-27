# Daily Takeout Coordinator

![](./screenshot.png)

An internal tool for colleagues to coordinate daily lunch orders — no accounts, no setup, just open the app and go.

## How it works

### Joining

On your first visit you are redirected to `/join`, where you enter your name. The app stores it in a cookie (`takeout_user`) that lasts one year, so you only do this once per browser.

### Every day

Each calendar day gets its own fresh session. When you open `/today` you see who else is around and what is happening with lunch.

**Set your status** — pick one:
- **Has Food** — you brought something, you are not ordering
- **Ordering** — you want takeout today

### Proposing a restaurant

Anyone with status **Ordering** can suggest a restaurant. Type the name and submit; it appears as a proposal for the day. The same restaurant can only be proposed once per day.

### Voting

Once proposals are up, anyone can vote for the restaurant they want. Rules:
- You get one vote at a time
- Switching your vote automatically removes your previous one
- You can retract your vote entirely
- Votes are tallied live — no page reloads needed

### Seeing the result

The page shows all participants and their statuses, all proposals with their current vote counts, and highlights which proposal is leading. Everything updates in place via the API without a full page reload.

## Identity model

There is no login or password. Your identity is your chosen name stored in the browser cookie. If you clear cookies or switch browsers you will be asked to enter your name again.


---

## Commands

### Local run (from terminal, although VSCode Spring plugin is recommended)

```
./gradlew bootRun
```

As this is a spring boot app with a Tomcat server, local run is available at `localhost:8080`

---

### Deployment
Deployed to **Render** (free tier, no credit card, auto HTTPS). A `Dockerfile` at the repo root does a two-stage build (`eclipse-temurin:21-jdk-alpine` → `eclipse-temurin:21-jre-alpine`).

> **Note on persistence:** Render's free tier does not include persistent disk, so the H2 file at `/data/takeout` will not survive restarts/redeploys. For a daily lunch tool this is acceptable — data resets each deploy. If persistence is required, consider Oracle Cloud Free Tier (always-free VM with full disk control).

**Deploy steps:**
1. Push repo to GitHub
2. Sign up at [render.com](https://render.com) (no credit card required)
3. New Service → Web Service → connect GitHub repo (Render auto-detects the `Dockerfile`)
4. Set environment variable: `SPRING_PROFILES_ACTIVE=prod`
5. Remove any persistent volume config — not available on the free tier
6. Deploy — a free `*.onrender.com` HTTPS URL is assigned automatically

> **Note on cold starts:** Free web services spin down after 15 minutes of inactivity and take ~1 minute to wake on the next request.