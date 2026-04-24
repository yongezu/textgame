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
