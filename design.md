# Basketball Showdown — Design Summary

## 1. Game Overview

A small terminal-based half-court basketball simulator written in Java. The
user controls an offense of three differentiated players against a
computer-controlled defense of three matched defenders on a 7×7 grid. The
goal is to clear three increasingly difficult stages (the **Triple-Stage
Basketball Showdown**), each ending when one team reaches a stage-specific
score threshold.

### 1.1 Board

- 7×7 grid. Cell coordinates are `(row, col)` with `row` growing downward.
- A basket sits below the grid, centered on column 3, at logical row
  `BASKET_ROW = 7` (one row past the last playable row).
- Render legend:
  - ` n ` — offense player `n` (1, 2, or 3) without the ball
  - `(n)` — offense player `n` holding the ball
  - ` * ` — defender
  - ` . ` — empty cell
  - `OOO` — basket on the bottom border

### 1.2 Players and roles

The offense is composed of three archetypes (one of each):

| Slot | Archetype | Role / special ability |
|------|-----------|------------------------|
| P1   | Guard     | Higher pass / shooting %; **slash** past matched defender |
| P2   | Forward   | **Cut** to (row 6, col 4) when off-ball and `row >= 4`     |
| P3   | Center    | **Auto-rebound** missed shots when at row 6                |

Each defender is matched 1-to-1 to the offense player at the same index. A
defender is computer-controlled and has no user-invoked ability.

### 1.3 Controls

```
1 / 2 / 3          focus player 1 / 2 / 3
(1) / (2) / (3)    pass to player N (focus shifts on success)
W / A / S / D      move focused player one cell up / left / down / right
<space>            shoot with the ball holder
X                  invoke focused player's special ability
C                  reset current stage
Q                  quit
```

Controls are also rendered to the right of the grid, one hint per row.

### 1.4 Game loop per stage

1. Render scoreboard, grid (with side-controls), and a focused-player status
   line `Focused : P1 [Guard] (SPECIAL | LONG-RANGE)`.
2. Read a line of input.
3. Dispatch:
   - Empty line ⇒ shoot.
   - `1`/`2`/`3` ⇒ change focus.
   - `(N)` ⇒ pass to player N.
   - `W/A/S/D` ⇒ move focused player.
   - `X` ⇒ invoke special ability.
   - `C` / `Q` ⇒ reset / quit.
4. After every action, check the stage win/loss thresholds.

### 1.5 Movement rules

- A move is allowed only if the destination is in the grid and unoccupied.
- After every successful offense move, the **defender matched to the focused
  offense player** updates. The defender repositions to one of the 9 cells in
  the 3×3 neighborhood of the offender's new position (so it always ends
  within Chebyshev distance 1 of its mark). Cell selection is weighted by
  HIGH / MEDIUM / LOW preferences stored on the `Defender` (currently most
  candidates fall under LOW, since the HIGH/MEDIUM target rows lie below the
  offender and are typically outside the candidate set).

### 1.6 Passing

- Each player has `passSuccess` (Guard's is boosted).
- `(N)` rolls against the passer's `passSuccess`. On success the receiver
  becomes ball holder and is auto-focused. On failure, defenders score +1 and
  the play resets.

### 1.7 Shooting

- Distance to basket = `BASKET_ROW - shooter.row`.
- Bucket: `< 2` ⇒ layup, `2..4` ⇒ mid-range, `> 4` ⇒ long-range.
- Probability is `bucketSuccess * openness`, where `openness` is `0.5 / 0.75 /
  1.0` depending on the matched defender's Chebyshev distance from the
  shooter (≤1 / 2 / >2).
- On success: layup/mid → `+2` for offense, long-range → `+3` ("Three
  Pointer!!!"). Defenders also get `+1`. Play resets.
- On miss: if any offense Center is at row 6 and has not yet used its
  special, an automatic rebound is rolled with `Center.reboundSuccess`. On
  success, the Center receives the ball and focus, defenders score 0, and
  play continues. Otherwise defenders score `+1` and play resets.

### 1.8 Special abilities

Every offense player may invoke their special ability **at most once per
stage**. The lockout (`Player.specialUsed`) only resets when a brand-new
`Game` is created (start of stage / restart).

- **Guard.specialAbility (slash)**: available only while holding the ball.
  Roll against `slashSuccess`. On success, jump to `(matchedDefender.row + 1,
  matchedDefender.col)` if that cell is in-bounds and unoccupied.
- **Forward.specialAbility (cut)**: available only when *not* holding the
  ball and the Forward's row is ≥ 4. On success, teleport to `(6, 4)` if
  that cell is open.
- **Center.specialAbility**: user-invocation is a no-op; the rebound is
  applied automatically by `Game.shoot()` on a missed shot.
- **Defender.specialAbility**: no-op; not user-controllable.

### 1.9 Storyline

Three stages, gated by score targets and tougher defenders:

| Stage | Target points | Defender weights (H, M, L) |
|-------|---------------|----------------------------|
| 1     | 5             | 70, 20, 10                 |
| 2     | 7             | 90, 10, 5                  |
| 3     | 9             | 90, 10, 0.5                |

Flow:

1. Splash screen: *"Triple-Stage Basketball Showdown. Ready?"*
2. Per-stage description screen.
3. Play the stage. First side to the target points ends it.
4. If defenders reach the target first, prompt
   *"Sorry, re-enter Triple Stage Basketball Showdown? (y/n)"* —
   `y` restarts the showdown from stage 1, `n` quits.
5. If players reach the target first, advance to the next stage.
6. After clearing stage 3, show the champion banner.

---

## 2. Java Class Architecture

### 2.1 Package layout

All sources live under `src/basketball/` in package `basketball`:

```
Main.java       — entry point, REPL, storyline driver, rendering
Stage.java      — stage parameters (target points + defender weights)
Game.java       — game state and rules (movement, pass, shoot, defender AI)
Team.java       — fixed-size container of three Players
Player.java     — abstract base; positions and shooting/pass characteristics
Guard.java      — offense archetype: slash special
Forward.java    — offense archetype: cut special
Center.java     — offense archetype: auto-rebound special
Defender.java   — computer-controlled defender; no-op special, has movement weights
```

### 2.2 Class hierarchy

```
                Player (abstract)
                ┌───────┬───────┬─────────┬──────────┐
                │       │       │         │          │
              Guard  Forward  Center   Defender
```

`Player` declares the abstract pair:

```java
public abstract void specialAbility();
public abstract boolean isSpecialAbilityAvailable();
```

and provides shared characteristics:

- Position: `row`, `col`, `setPosition(row, col)`.
- Shooting / passing %: `passSuccess`, `layupSuccess`, `midRangeSuccess`,
  `longRangeSuccess`.
- Special ability state: `specialUsed`, `lastSpecialResult`
  (`SpecialResult { NONE, UNAVAILABLE, SUCCESS, FAIL }`),
  `onGameReset()` to clear them.
- Back-reference to `Game` (`game`) so subclasses can read board state and
  shared RNG when computing their special.

#### Subclass responsibilities

- **`Guard`** — overrides default percentages upward; field
  `slashSuccess = 0.30`. `specialAbility()` requires possession, attempts a
  jump to `(matchedDefender.row + 1, matchedDefender.col)`, marks
  `specialUsed`.
- **`Forward`** — field `cutSuccess = 0.30`. Available only when not holding
  the ball and `row >= 4`. `specialAbility()` teleports to `(6, 4)` on
  success.
- **`Center`** — field `reboundSuccess = 0.40`. User-invocation is a no-op;
  exposes `tryRebound()` which `Game.shoot()` calls on a missed shot.
  `isSpecialAbilityAvailable()` is true at `row == 6` (used for the status
  line indicator).
- **`Defender`** — `specialAbility()` no-op, `isSpecialAbilityAvailable()`
  false. Holds the AI movement weights `highWeight`, `mediumWeight`,
  `lowWeight` with both a default constructor (80 / 15 / 5) and a secondary
  constructor that takes the three weights — this is what `Stage`-aware
  game creation uses.

### 2.3 `Team`

Thin container: a `name` and a fixed-size `Player[]` of length `Team.SIZE = 3`.
Exposes `getPlayer(i)` and `getPlayers()`.

### 2.4 `Game`

Owns all mutable game state and rules:

- Two `Team`s (`offense`, `defense`).
- Indices: `focusedIndex`, `ballHolderIndex`.
- Possession: `Possession { OFFENSE, DEFENSE }` (currently always OFFENSE
  during play; turnovers immediately reset).
- Scores: `offenseScore`, `defenseScore`.
- Game counter: `gameCounter`, incremented on each `resetPlay()` (i.e.
  every shot or turnover) — drives the `#N` shown in the header.
- Shared `Random rng`.

Public API exercised by `Main` and the special abilities:

```
move(char dir) : boolean              // W/A/S/D
pass(int receiverIndex) : boolean
shoot() : ShotResult                  // MADE | MADE_THREE | MISSED |
                                      // REBOUNDED | NO_POSSESSION
invokeSpecial() : Player.SpecialResult

setFocusedIndex(int)
getFocusedPlayer() / getBallHolder() / ...
getOffenseScore() / getDefenseScore() / getGameCounter()

inBounds(row, col) : boolean
isOccupied(row, col) : boolean
getMatchedDefender(Player) : Player
getRng() : Random
```

Internals worth noting:

- `Game(Team, Team)` calls `setGame(this)` on every player so abilities can
  query state without parameters.
- `resetPlay()` restores positions from a freshly-built template `Game`,
  resets focus / ball holder, and bumps `gameCounter`. It does **not** clear
  per-game special locks.
- `updateDefenders()` runs only for the matched defender of the currently
  focused offense player. It enumerates the 3×3 neighborhood **of the
  offender's new position**, drops off-grid / occupied cells (using
  `isOccupiedExcluding`), classifies each remaining cell as HIGH / MEDIUM
  / LOW, and picks one weighted-randomly using the defender's weights.
- `shoot()` integrates the Center auto-rebound branch and three-point
  scoring.
- `createInitial()` and `createInitial(Stage)` build a fresh game with
  default offense layout (P1 Guard `(0,1)`, P2 Forward `(2,2)`, P3 Center
  `(1,4)`) and matched defenders at `(row+2, col)` of each offender. The
  stage-aware variant injects defender weights from the `Stage`.

### 2.5 `Stage`

Plain immutable carrier:

```java
public class Stage {
    public final int    maxPoints;
    public final double highWeight, mediumWeight, lowWeight;
    public Stage(int maxPoints, double high, double medium, double low) { ... }
}
```

Three concrete stages live as a `static final` array in `Main.STAGES`.

### 2.6 `Main`

Owns I/O and the storyline state machine. Responsibilities:

- `main(String[])` — outer loop calling `runShowdown()` until the user
  declines a re-entry or wins the championship.
- `runShowdown()` — splash screen, walks through `STAGES`, calls
  `playStage()` for each, and emits the champion banner on full clear.
- `playStage(Stage, stageNum)` — owns the per-stage REPL: render → read →
  dispatch (`apply` / `shoot`) → check stage end. Returns `1` (cleared),
  `-1` (failed), or `-2` (user quit).
- `apply(Game, cmd)` — parses single-letter and `(N)` forms.
- Rendering helpers:
  - `render(Game, Stage, stageNum)` — header (`Stage N (target M) #G
    Score: players X | defenders Y`), grid with side controls, focused-
    player status line.
  - `controlHint(row)` — control text per grid row.
  - `cell(Game, row, col)` — single-cell glyph.
  - `shotType(Game)` — layup / mid-range / long-range / n/a.

### 2.7 Notable invariants and design notes

- The 7×7 board exists in the grid; the basket lives just outside it on the
  bottom border (`BASKET_ROW = GRID_SIZE`). Shot distance is therefore
  always ≥ 1.
- Special-ability lockout (`specialUsed`) is **per game / per stage**, not
  per possession. A new `Game` (start of stage, `C` reset) clears it; a
  `resetPlay()` after a shot or turnover does not.
- `Game.gameCounter` counts plays (every `resetPlay`), not stages.
- Defender movement is offender-centered (the 3×3 around the offender),
  meaning defenders effectively stick to their mark every turn rather than
  drifting freely.
- The HIGH/MEDIUM/LOW weights are kept on `Defender` (with a constructor
  that takes them) so each `Stage` can dial defender stickiness without
  changing any other code.
