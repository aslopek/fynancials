# Fynancials User Manual

- [Installation & first launch](#installation--first-launch)
- [Your database](#your-database)
- [Setting up your portfolio](#setting-up-your-portfolio)
- [A tour of the app](#a-tour-of-the-app)
- [Understanding your depot performance numbers](#understanding-your-depot-performance-numbers)
- [Dev mode & direct database access](#dev-mode--direct-database-access)

## Installation & first launch

Fynancials is a desktop app; all data stays on your machine. It needs Java to run its local
backend — if none is found on your system, the app offers to download Amazon Corretto 25 on
first launch (Windows x64 and macOS Apple Silicon) and stores it in a `java/` folder next to
the app. If Java is already installed, that installation is used.

On first launch you are asked for a password. **This one prompt decides how your database is protected**, so read the next section before
typing anything.

## Your database

Everything you enter is stored in a single, AES-encrypted [H2](https://h2database.com) database file. By default it is created in your home
directory as `fynancials.mv.db`.

### The password

- The password you enter at the very first start becomes the encryption password of the newly created database. There is **no recovery** —
  if you lose it, the data in the file is gone.
- If you entered a password, you will be asked for it at every start.
- If you left it **empty**, Fynancials remembers that choice and never asks again for this
  database file. The file is then effectively unprotected — anyone with access to the file
  can open it.
- That "ask / don't ask" choice is recorded per database file in the config file (see below)
  the first time you are prompted. To change it, edit the config file.
- Entering a wrong password makes the backend fail to start: the app will not get past its
  loading screen. Quit, restart, and enter the correct password. Details end up in
  `fynancials.log` next to the application (the file is rewritten on every start).

Be aware that due to the nature of the desktop app, while Fynancials is running, third-party
software running on your system may access and/or manipulate the data by connecting to
Fynancial's HTTP API. This enables you to use third-party add ons on top of Fynancials, but
also means the data is fully protected only while no Fynancials instance is accessing the
database.

### The config file

Startup settings live in `fynancials.config.json` in your home directory (e.g. `C:\Users\<you>\fynancials.config.json`):

```json
{
  "env": {
    "FY_DB_FILE_PATH": "C:\\Users\\you\\fynancials"
  },
  "askForPassword": {
    "C:\\Users\\you\\fynancials": true
  }
}
```

- `env` — environment variables passed to the backend. `FY_DB_FILE_PATH` is the database file
  path **without** the `.mv.db` extension.
- `askForPassword` — one entry per database path: `true` = prompt at every start, `false` =
  never prompt (empty password is used). An entry is written automatically the first time you
  are prompted for that path; edit or delete it to change the behavior.

On Windows, use either escaped backslashes (`C:\\data\\fynancials`) or forward slashes
(`C:/data/fynancials`) in the JSON.

### Moving the database

1. Quit Fynancials.
2. Move `fynancials.mv.db` to the new location (you can also rename it; keep the `.mv.db`
   extension).
3. Edit `fynancials.config.json` and set `env.FY_DB_FILE_PATH` to the new path without the
   `.mv.db` suffix — for `D:\finance\depot.mv.db` that is `"D:\\finance\\depot"`.
4. Start Fynancials and enter your password. Since this is the first prompt for the new path,
   your "ask / don't ask" choice is recorded again; you can delete the old path's
   `askForPassword` entry.

### Switching between and creating databases

`FY_DB_FILE_PATH` also lets you keep several databases (e.g. a real one and a playground):
point it at a different path and restart. If no file exists at the path, a fresh, empty
database is created — encrypted with whatever password you enter at that first prompt.

### Backups

Quit the app and copy `fynancials.mv.db` somewhere safe. That single file is your complete portfolio; restoring a backup means copying it
back (or pointing `FY_DB_FILE_PATH` at the copy).

### Changing the password (advanced)

There is currently no built-in way. If you must, use H2's `ChangeFileEncryption` tool with an
H2 jar matching the bundled version (currently 2.4.x), **after making a backup**:

```shell
java -cp h2-2.4.240.jar org.h2.tools.ChangeFileEncryption -dir <folder> -db <name> -cipher \
AES -decrypt <oldPassword> -encrypt <newPassword>
```

Alternatively, note that switching from "no password" to "password" (or vice versa) is also a
password change — the same tool applies.

## Setting up your portfolio

The recommended order for a fresh database:

### 1. Configure market data sources

For historical security prices, Fynancials ships with preconfigured data sources:

- [Twelve Data](https://twelvedata.com)
- [EODHD](https://eodhd.com).

All require a personal API key (free tiers may be available): register with the provider, then
select the data source under **Settings → Historical Security Prices** and enter your key.
Fynancials is not affiliated with any of the providers; your use of their APIs is subject to
their respective terms and plan limits.

Beyond that, you can connect any HTTP/JSON API of your choice (make sure your usage complies
with the provider's terms). Under **Settings → Historical Security Prices** and **Settings →
Dividend Announcements**, add a data source by selecting a JSON configuration file that
describes how to call the API and how to read its response:

- `urlPatterns` (historical prices: one URL per time span, e.g. a "last 30 days" and a "full
  history" endpoint) or a single `urlPattern` (dividend announcements).
- `jsonPathDate` / `jsonPathValue` / `jsonPathCurrency` — JSON paths into the response.
- `dateFormat` — `TIMESTAMP_SECONDS`, `TIMESTAMP_MILLISECONDS`, or `CUSTOM_STRING` with a
  pattern.
- `requestHeaders` — e.g. for API keys.
- `currencyMappings` and `marketCloseTimes` where needed.

URL patterns and header values support template functions, most importantly `#id()` (replaced
with the security's external ID, e.g. its ticker symbol) and `#date(pattern, daysBack)` (a
formatted date). `#mask(secret)` inserts a value while keeping it out of log output;
`#uuid()`, `#rng(min,max)`, and `#base64(text)` also exist.

Prices, dividend announcements, and ECB exchange rates are refreshed automatically when the app starts.

### 2. Add securities

A fresh database already contains a few example securities — Microsoft, Apple, and both
Alphabet share classes (grouped into an "Alphabet" security group) — so you have something to
explore; edit or delete them freely. Microsoft and Apple come with a prepared historical price
configuration pointing at the preconfigured data sources; it ships disabled — enable it in the
security's detail view once your API key is set.

On the **Securities** page, the add-security wizard walks you through master data (name, ISIN,
symbol, ...), the historical price configuration (which data source to use and the security's
external ID in that source), and an optional dividend announcement configuration. Both
configurations can be changed later in the security's detail view, where you can also record
stock splits and view the historical price chart.

Alternatively, if you have a lot of securities, you may choose to do the CSV import (next
recommended step) before and apply any changes after the automatic import.

### 3. Create a depot and record transactions

On the **Depots** page, create a depot and start recording transactions: buys, sells, dividends,
special dividends, and taxes. You can add them manually or import a CSV file exported from your
broker — the import wizard lets you map the CSV columns to transaction fields, review the
parsed rows, and afterwards download any rows that could not be imported as a separate CSV to
fix and retry.

The CSV import also has a best-effort mechanism to automatically create unknown securities
referenced by the transactions.

If any rows from your CSV could not be imported, you will provided with a CSV file containing
only the failed lines for your reference.

## A tour of the app

### Depots

Everything about one depot, in four tabs (you can create any number of depots and switch via
the header):

- **Positions** — your current holdings with cash-exclusive per-position returns, an allocation
  chart, and a drill-down into the individual purchase lots behind each position.
- **Dividends** — received dividends over time as a bar chart plus per-security yield tables.
- **Performance** — depot value over time with KPI tiles, invested capital, XIRR, and
  configurable benchmarks. See
  [Understanding your depot performance numbers](#understanding-your-depot-performance-numbers).
- **Transactions** — the full transaction history: add, delete, import.

The Transaction tab is available if one depot is selected for disambiguity. If you select
multiple depots at once, the Transactions tab becomes unavailable.

When selecting multiple depots, the Positions, Dividends and Performance tabs treat them as
one depot (merge). Merging depots of different currencies is not possible.

### Securities

The master list of everything you track: add securities, edit master data, manage stock splits,
configure price/announcement data sources, and inspect historical price charts.

### Dividends

Upcoming dividend announcements, grouped by week. New announcements also show up under the notification bell in the header.

### Settings

- **Appearance** — date, currency, and number formats; **Hide Absolute Values** masks currency
  amounts app-wide (handy for screen sharing); **Enable Dev Mode** unlocks direct database
  access (see below).
- **Historical Security Prices / Dividend Announcements** — manage your data sources.
- **Security Groups** — organize your securities into named groups. This can be used to group
  ADR and non-ADR shares of the same company or group shares of dual class listings.

## Understanding your depot performance numbers

Fynancials shows several numbers that all describe "performance" from different angles. This section explains what each one actually means, so the
figures don't feel contradictory.

### There are no withdrawals

Fynancials doesn't model money leaving your depot. When you sell a position or receive a
dividend, the proceeds don't disappear — they become **cash held inside the depot**, available
to fund your next buy (or sit there earning nothing until you reinvest it). If a tax payment
(e.g. "Vorabpauschale") exceeds your available cash, the shortfall is simply treated as
freshly invested capital, the same as a deposit.

This has one consequence worth knowing: **"Invested Capital" only ever grows.** It represents
everything you've ever put into the depot, in stock or in cash — it doesn't shrink when you
sell something, because selling doesn't take money out of the depot.

### Growth (abs. / rel.)

The KPI tiles and chart tooltip on the Depot Performance page show your depot's raw value change
over the selected period — how much your total depot (stocks **and** cash) has moved, in
currency and in percent. It's not automatically corrected for money you deposited during that
period, so if you add fresh capital, that shows up as growth too. Use the "Add Cash to Absolute
Value" checkbox to switch between including and excluding idle cash in this figure. Use "Show
Invested Capital" to compare your depot's total value (with or without cash) against the total
amount you've ever put in.

### XIRR

The XIRR (Extended Internal Rate of Return) figure is your depot's **annualized money-weighted
return**: it treats every deposit as money leaving your pocket and your current total depot
value (stocks + cash) as what you'd get back if you liquidated everything today, then solves
for the constant yearly rate that makes those numbers consistent. It naturally weighs larger,
longer-held contributions more heavily than small, recent ones.

### Fixed Interest Benchmark

This lets you compare your depot against "what if I'd put the same money into a fixed-interest
account instead?" One thing to watch: the interest rate you enter is a **nominal rate**,
compounded at whichever interval you choose (quarterly by default) — not the same kind of
number as XIRR, which is always an effective annual rate. A nominal 16.5% compounded quarterly
works out to roughly 17.5% effective annual — so don't be surprised if the benchmark rate that
visually matches your depot curve differs from your XIRR by about a percentage point. Neither
number is wrong; they're just different conventions.

There are two variants of the Fixed Interest Benchmark: **Cashflow-based** lets you compare
"what if I'd put the same money into a fixed-interest account instead?". **Regular Investments**
lets you compare against a monthly savings plan with either a fixed monthly contribution or
automatically reinvesting whatever you've actually invested into your depot that month. You may
configure the day of deposit; if it falls onto a weekend, it is carried out on the next Monday.

### Position-level returns

The Positions view is the exception: the return shown per holding is deliberately
**cash-exclusive** — it's your gain/loss on that specific position relative to what you paid
for it, same as any broker or stock screener would show. Cash sitting elsewhere in your depot
has no bearing on how one particular stock performed.

Furthermore for each position you currently hold, you see the net income, which is comprised of
dividend, special dividend and tax transactions.

Clicking on a position lets you further drill down into each lot you currently hold. This
includes a CAGR for each lot.

## Dev mode & direct database access

Enable **Dev Mode** under Settings → Appearance and a database button appears in the header. It
opens a dialog with the embedded H2 web console and the connection details (JDBC URL, user,
password) for connecting external tools. This is raw SQL access to your live data — make a
backup before changing anything, and treat the connection details as sensitive.
