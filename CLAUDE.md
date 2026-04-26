# Context

This is a Java project that is an interactive simple basketball simulator game.  It runs as a terminal UI with user keyboard input that may involve multiple key entries for controlling players.

# Relevant objects

## Team

There will be two teams:

- defenders: these are three players that are controlled by the computer.
- players: these three players are controlled by the user.

## Player

Each player exists in a 7x7 grid.  A players location is (i,j) both are integers.
A player will have some playing characteristics.  This will be specified later.  For now, keep things simple and extensible.

## The game

The game is played out on a 7x7 grid simulating a half-court basketball showdown.  The computer controlled defenders will try to block the user controlled players.  We will only consider the play scenario where the players have possession.

A player is focused, either 1, 2 or 3.  Initially 1 is focused.
Display the focused player below the grid.

# Game dynamics

## Player movement

- No two players can occupy the same location.
- Players are controlled by keystrokes: "W", "A", "S", "D".
- User enters "1W" to focus player 1, and move player 1 one position up.  The player will follow the user input only when it is possible.
- User enters "W" to move the focused player up.
- Similarly, "1D", "1S", etc sets player focus, and then move.
- Single letter moves the focused player.

## Ball possession

The ball is possessed only by one of the players.  Introduce a game member that indicates which player has the ball.
The rendering of the grid needs more spacing to create a visual indicator of which player has the ball.  Render "(1)" instead of " 1 " to indicate that player 1 has the ball.  Initially player 1 has the ball.

## Passing

Players have the ability to pass the ball.  The command for passing is simply the receiver's player number.  For example "2" indicates that the ball is to be passed to player 2.

Each player has an associated pass success probability.  At each pass, the ball is passed successfully based on this probability.  If the pass fails, the other team get possession.

### Scoring:

- Each team has a score, initialized to zeros.
- When the players loss possession, the defender team gets +1 score.

## Shooting ball

Place a basket at the bottom center position like this:

```
+---------------------+
| . (1) .  .  .  .  . |
| .  .  .  .  .  3  . |
| .  .  .  2  .  .  . |
| .  .  .  .  .  .  . |
| .  .  .  *  .  .  . |
| .  *  .  .  .  .  . |
| .  .  *  .  .  .  . |
+---------OOO---------+
```

The player with possession can shoot the ball.  The command is just spacebar.
Each player has three success probabilities:
- double midRangeSuccess
- double longRangeSuccess
- double layupSuccess

The success probability of the shot depends on the distance.  The shoot distance is the player row to the basket row.
- layup: row distance < 2
- midRange: 2 <= row distance <= 4
- longRange: otherwise

When the shot successful, increment the player team score by +2.
After the shot (in either case), the defender team scores +1.

# Defender movement

The defender players are matched to the offense players one-to-one.  This means that defense.players[i] is matched to offense.players[i].  Their relative position to the shooting player will affect the player's probability of success.

## Initial defender position

All defenders are positions (i+2, j) where (i,j) is the offense player position.

## Tracking the offense player

After the offender moves to its new position at (row_o, col_o), the matched
defender repositions itself **relative to the offender's new position** (not
relative to its own current position).

The defender's new cell is chosen from the 3x3 neighborhood centered on the
offender, i.e. the 9 cells `(row_o + dr, col_o + dc)` for `dr, dc ∈ {-1, 0, +1}`.

Constraints:
- The candidate cell must be in the grid.
- The candidate cell must not be occupied by another player. (Use a helper
  function `isOccupied(row, col)` for this check.)
- The defender's own current position is allowed (i.e. it may stay put if its
  current cell happens to also be in the offender's 3x3 neighborhood).

This means the defender effectively "sticks" to its matched offender every
update — it always ends up within Chebyshev distance 1 of the offender.

### Preference based movement:

Each candidate cell is scored with one of three preference weights — HIGH,
MEDIUM, or LOW — and the defender picks one at random with probability
proportional to its weight. The HIGH / MEDIUM / LOW values are stored as
fields on the `Defender` (so they can be tuned per stage / per archetype
later).

The preference targets are:

If (row_o < 4), preference(row_o+2, col_o) = HIGH, preference(row_o+1, col_o)
= MEDIUM, all other reachable cells are LOW.

If (row_o >= 4), preference(row_o+1, col_o) = HIGH, preference(row_o+2, col_o)
= MEDIUM, all other reachable cells are LOW.

Note: because the candidate cells are within distance 1 of the offender, the
HIGH and MEDIUM target rows (which sit 1–2 rows *below* the offender) are
generally outside the candidate set, so in practice most candidates fall under
LOW. The HIGH/MEDIUM weights are kept on the `Defender` so future tuning or
expanded movement ranges can use them without further refactoring.


# Class Hierarchy

The `Player` class is an abstract class with the following abstract method:

- `public void specialAbility()`.  The special ability is invoked by user command.
- `public boolean isSpecialAbilityAvailable()`: indicates if the special ability is available.

The `Defender` is a subclass of of `Player` whose special ability does nothing because users do not control the `Defender` players.

- All players can only invoke their special ability at most ONCE during a single game (reset in game reset).
- In the game status lines, show if the current player has special ability like "Focused: P1 (special available)" vs "Focused: P1 (special unavailable)"

The offense team players are of three subclass players:

## Guard

- Higher shooting percentages.
- Higher pass success.

The special ability:

- special ability is only available if the `Guard` player has possession of the ball.
- On the special ability, the player can pass its defender at (row_d, col_d).  With success special ability probability, the Guard can reach (row_d + 1, col_d).  The success probability is a field "Guard.slashSuccess = 0.3".  

## Forward

The special ability:

- special ability is only available if the `Forward` does not have possession of the ball.
- special ability is only available if the forward row >= 4.
- The forward special ability allows the forward to get to row=6, col=4 in the next game update.  The success rate is described by `Forward.cutSuccess = 0.3`.

## Center 

The center special ability:

- only available at row=6 at any column.
- This special ability is applied automatically during each game update.
- The success of the ability allows the center player to get possession *if* a shot failed, preventing the defenders to get point, and allowing the game to continue.

# Storyline

There are three stages.

Stage 1: play until one team reaches 5 points.  The defenders in stage 1, the defenders are `Defender(row, col, 70, 20, 10)`
Stage 2: play until one team reaches 7 points.  Defenders are `Defender(row, col, 90, 10, 5)`
Stage 3: play until one team reaches 9 points.  Defenders are `Defender(row, col, 90, 10, 0.5)`

Create a class `Stage` with constructor:

```
Stage(maxPoints, highWeight, mediumWeight, lowWeight)
```

The story line is:

1. start with a screen of story: "Triple-Stage Basketball Showdown.  Ready?"
2. then it proceeds to a screen describing the stage 1: maxpoints, and defender characterstics.
3. then it starts Stage 1.
4. if fails, it will display a "Sorry, re-enter Triple Stage Basketball Showdown?".  If no, quit the game, if yes, go to 1.
5. if succeeds, proceed to Stage 2.
...

After succeeding in Stage 3.  Show "Congrats to the champion team of the Triple Stage Basketball Showndown."

