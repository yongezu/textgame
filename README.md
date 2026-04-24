# Basketball Terminal Simulator

A small half-court basketball simulator that runs in the terminal. You
control three offensive players on a 7×7 grid, move them past three
computer defenders, pass the ball, and shoot at a basket at the bottom
of the court. Pure Java — no third-party dependencies.

## Requirements

- JDK 11+ (developed against OpenJDK 23)
- A POSIX shell (for `build.sh` / `run.sh`)

## Build and Run

```sh
./build.sh    # compiles src/**/*.java into build/
./run.sh      # runs basketball.Main
./run.sh some.Other.Class   # run a different main class
```

`build.sh` puts everything under `build/`. `run.sh` uses `build/` (and
`lib/*` if you later add jars) on the classpath.

## How to Play

Input is line-buffered: type a command, press **Enter**.

| Input        | Action                                                   |
| ------------ | -------------------------------------------------------- |
| `W` / `A` / `S` / `D` | Move the focused player up / left / down / right |
| `1W`, `2D`, `3S`, ... | Focus that player and move in one step          |
| `1`, `2`, `3` (alone) | Pass the ball to that player                    |
| *Space* + Enter       | Shoot the ball (current ball-holder)            |
| `C`                   | Clear / reset the game (scores also reset)       |
| `Q`                   | Quit                                             |

Input is case-insensitive. An empty line (just Enter) is ignored;
a space followed by Enter is the shoot command.

## Rendering

```
+---------------------+
|(1) .  .  .  .  .  . |
| .  .  .  .  .  3  . |
| .  .  .  2  .  .  . |
| .  .  .  .  .  .  . |
| .  .  .  *  .  .  . |
| .  *  .  .  .  .  . |
| .  .  *  .  .  .  . |
+---------OOO---------+
Focused: P1 | Ball: P1 | Shot: long-range | Score  players:0  defenders:0
>
```

- `1`, `2`, `3` — the three user-controlled players
- `(1)` — parentheses mark the current ball-holder
- `*` — a defender
- `.` — empty cell
- `OOO` — the basket, at the bottom-center of the court
- **Focused** — which player moves when you press a single direction key
- **Ball** — who currently holds the ball (or "defenders" if lost)
- **Shot** — the type of shot the ball-holder would take right now
  (depends on row distance to the basket)

## Game Rules

### Movement

- Up/down/left/right, one cell per command.
- No two players can share a cell (both teams are considered).
- Moves that would leave the grid or land on an occupied cell are
  silently rejected.

### Passing

- Command is the receiver's number (`1`, `2`, or `3`).
- Success is rolled against the **passer's** `passSuccess` probability.
- On failure: defenders score **+1** and the play resets to the initial
  layout. Team scores persist across plays.

### Shooting

- Command is spacebar + Enter. The ball-holder takes the shot.
- Shot distance is `BASKET_ROW − shooter_row`, where `BASKET_ROW = 7`
  (the row just below the grid):
  - `distance < 2` → **layup**
  - `2 ≤ distance ≤ 4` → **mid-range**
  - otherwise → **long-range**
- The probability comes from the shooter's corresponding
  `layupSuccess` / `midRangeSuccess` / `longRangeSuccess`.
- On a **make**: players score **+2**.
- In **every** case (make or miss): defenders score **+1**, and the
  play resets.

### Scoring

- Both scores start at 0 and accumulate across plays.
- Only `C` (clear) or `Q` (quit) resets the scores.

## Project Layout

```
basketball/
├── CLAUDE.md          # design notes / spec this simulator is built from
├── README.md
├── build.sh           # javac -d build -cp "lib/*" <sources>
├── run.sh             # java -cp "build:lib/*" basketball.Main
├── lib/               # optional: drop third-party jars here
├── src/basketball/
│   ├── Main.java      # I/O loop, rendering, command parsing
│   ├── Game.java      # game state, movement, passing, shooting, reset
│   ├── Team.java      # fixed-size team of 3 Players
│   └── Player.java    # name, (row, col), probabilities
└── build/             # compiled output (gitignored)
```

### Key Types

- `Game` — holds the two teams, focus, ball-holder, possession state,
  scores, and an RNG. Exposes `move(dir)`, `pass(receiver)`, `shoot()`,
  and `Game.createInitial()` for the starting layout.
- `Team` — a `Player[]` of exactly `Team.SIZE = 3`.
- `Player` — position plus probabilities (`passSuccess`,
  `layupSuccess`, `midRangeSuccess`, `longRangeSuccess`) with defaults.
  Setters are provided so future code can differentiate players.

## Tuning

Default probabilities are set in `Player.java`:

```java
private double passSuccess      = 0.8;
private double layupSuccess     = 0.75;
private double midRangeSuccess  = 0.5;
private double longRangeSuccess = 0.3;
```

You can change them globally by editing the defaults, or per-player by
calling the setters in `Game.createInitial()`.

Initial positions are also set in `Game.createInitial()`.

## Limitations

- The computer defenders are currently stationary — they do not chase
  the ball-holder yet.
- Input is line-buffered (standard JDK only); there is no real-time
  keystroke handling.
