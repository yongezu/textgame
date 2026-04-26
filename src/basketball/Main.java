package basketball;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {
    private static final Stage[] STAGES = {
        new Stage(5, 70, 20, 10),
        new Stage(7, 90, 10, 5),
        new Stage(9, 90, 10, 0.5),
    };

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        boolean play = true;
        while (play) {
            play = runShowdown(in);
        }
    }

    private static boolean runShowdown(BufferedReader in) throws IOException {
        System.out.println();
        System.out.println("==========================================");
        System.out.println("  Triple-Stage Basketball Showdown.  Ready?");
        System.out.println("==========================================");
        System.out.print("(press <enter> to begin, Q to quit) > ");
        String l = in.readLine();
        if (l == null) return false;
        if (l.trim().equalsIgnoreCase("Q")) return false;

        for (int s = 0; s < STAGES.length; s++) {
            Stage stage = STAGES[s];
            System.out.println();
            System.out.println("------------------------------------------");
            System.out.println("  Stage " + (s + 1) + ": first to " + stage.maxPoints + " points wins.");
            System.out.println("  Defender weights: HIGH=" + stage.highWeight
                + "  MEDIUM=" + stage.mediumWeight + "  LOW=" + stage.lowWeight);
            System.out.println("------------------------------------------");
            System.out.print("(press <enter> to start stage) > ");
            if (in.readLine() == null) return false;

            int outcome = playStage(in, stage, s + 1);
            if (outcome == -2) return false; // user quit
            if (outcome == -1) {
                System.out.println();
                System.out.print("Sorry, re-enter Triple Stage Basketball Showdown? (y/n) > ");
                String ans = in.readLine();
                if (ans == null) return false;
                return ans.trim().equalsIgnoreCase("y");
            }
        }

        System.out.println();
        System.out.println("******************************************");
        System.out.println("  Congrats to the champion team of the");
        System.out.println("  Triple Stage Basketball Showdown.");
        System.out.println("******************************************");
        return false;
    }

    /**
     * Returns: 1 if players win, -1 if defenders win, -2 if user quit.
     */
    private static int playStage(BufferedReader in, Stage stage, int stageNum) throws IOException {
        Game game = Game.createInitial(stage);
        while (true) {
            render(game, stage, stageNum);
            System.out.print("> ");
            String line = in.readLine();
            if (line == null) return -2;

            System.out.println();
            System.out.println();

            if (line.isEmpty()) continue;
            if (line.trim().isEmpty()) {
                Game.ShotResult r = game.shoot();
                if (r == Game.ShotResult.MADE) {
                    System.out.println("Shot MADE! +2 players, +1 defenders.");
                } else if (r == Game.ShotResult.MADE_THREE) {
                    System.out.println("Three Pointer!!! +3 players, +1 defenders.");
                } else if (r == Game.ShotResult.MISSED) {
                    System.out.println("Shot missed. +1 defenders.");
                } else if (r == Game.ShotResult.REBOUNDED) {
                    System.out.println("Shot missed but Center rebounded! Possession retained.");
                }
            } else {
                String cmd = line.trim().toUpperCase();
                if (cmd.equals("Q")) return -2;
                if (cmd.equals("C")) {
                    game = Game.createInitial(stage);
                    continue;
                }
                apply(game, cmd);
            }

            if (game.getOffenseScore() >= stage.maxPoints) {
                render(game, stage, stageNum);
                System.out.println();
                System.out.println(">>> Stage " + stageNum + " cleared! <<<");
                return 1;
            }
            if (game.getDefenseScore() >= stage.maxPoints) {
                render(game, stage, stageNum);
                System.out.println();
                System.out.println(">>> Stage " + stageNum + " lost. <<<");
                return -1;
            }
        }
    }

    private static void apply(Game game, String cmd) {
        if (cmd.length() == 1) {
            char c = cmd.charAt(0);
            if (c >= '1' && c <= '3') {
                game.setFocusedIndex(c - '1');
            } else if ("WASD".indexOf(c) >= 0) {
                game.move(c);
            } else if (c == 'X') {
                Player.SpecialResult r = game.invokeSpecial();
                if (r == Player.SpecialResult.SUCCESS) {
                    System.out.println("Special ability succeeded!");
                } else if (r == Player.SpecialResult.FAIL) {
                    System.out.println("Special ability failed.");
                } else if (r == Player.SpecialResult.UNAVAILABLE) {
                    System.out.println("Special ability unavailable.");
                }
            }
            return;
        }
        if (cmd.length() == 3 && cmd.charAt(0) == '(' && cmd.charAt(2) == ')') {
            char c1 = cmd.charAt(1);
            if (c1 >= '1' && c1 <= '3') {
                int idx = c1 - '1';
                int before = game.getDefenseScore();
                boolean ok = game.pass(idx);
                if (game.getDefenseScore() > before) {
                    System.out.println("Pass failed! Defenders intercepted.");
                } else if (ok) {
                    game.setFocusedIndex(idx);
                }
            }
        }
    }

    private static void render(Game game, Stage stage, int stageNum) {
        int inner = Game.GRID_SIZE * 3;
        String topBorder = "+" + "-".repeat(inner) + "+";
        int sideDashes = (inner - 3) / 2;
        String bottomBorder = "+" + "-".repeat(sideDashes) + "OOO"
            + "-".repeat(inner - 3 - sideDashes) + "+";

        System.out.println("Stage " + stageNum + " (target " + stage.maxPoints + ")"
            + "   #" + game.getGameCounter()
            + "   Score: players " + game.getOffenseScore()
            + "  |  defenders " + game.getDefenseScore());
        System.out.println(topBorder);
        for (int i = 0; i < Game.GRID_SIZE; i++) {
            StringBuilder line = new StringBuilder("|");
            for (int j = 0; j < Game.GRID_SIZE; j++) {
                line.append(cell(game, i, j));
            }
            line.append("|    ");
            line.append(controlHint(i));
            System.out.println(line);
        }
        System.out.println(bottomBorder);
        Player f = game.getFocusedPlayer();
        String special = f.isSpecialAbilityAvailable() ? "SPECIAL" : "-";
        String shot = shotType(game).toUpperCase();
        System.out.println("Focused : " + f.getName() + " [" + f.archetype() + "] (" + special + " | " + shot + ")");
    }

    private static String controlHint(int row) {
        switch (row) {
            case 0: return "1/2/3   focus player";
            case 1: return "(1/2/3) pass + focus";
            case 2: return "W/A/S/D move focused";
            case 3: return "<space> shoot";
            case 4: return "X       special";
            case 5: return "C       reset stage";
            case 6: return "Q       quit";
            default: return "";
        }
    }

    private static String shotType(Game game) {
        if (game.getPossession() != Game.Possession.OFFENSE) return "n/a";
        int distance = Game.BASKET_ROW - game.getBallHolder().getRow();
        if (distance < 2) return "layup";
        if (distance <= 4) return "mid-range";
        return "long-range";
    }

    private static String cell(Game game, int row, int col) {
        Player[] offense = game.getOffense().getPlayers();
        boolean offenseHasBall = game.getPossession() == Game.Possession.OFFENSE;
        for (int k = 0; k < offense.length; k++) {
            Player p = offense[k];
            if (p.getRow() == row && p.getCol() == col) {
                char glyph = (char) ('1' + k);
                return (offenseHasBall && k == game.getBallHolderIndex())
                    ? "(" + glyph + ")"
                    : " " + glyph + " ";
            }
        }
        for (Player d : game.getDefense().getPlayers()) {
            if (d.getRow() == row && d.getCol() == col) {
                return " * ";
            }
        }
        return " . ";
    }
}
