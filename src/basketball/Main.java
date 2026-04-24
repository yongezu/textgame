package basketball;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] args) throws IOException {
        Game game = Game.createInitial();
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            render(game);
            System.out.print("> ");
            String line = in.readLine();
            if (line == null) break;

            if (line.isEmpty()) continue;
            if (line.trim().isEmpty()) {
                Game.ShotResult r = game.shoot();
                if (r == Game.ShotResult.MADE) {
                    System.out.println("Shot MADE! +2 players, +1 defenders. Resetting...");
                } else if (r == Game.ShotResult.MISSED) {
                    System.out.println("Shot missed. +1 defenders. Resetting...");
                }
                continue;
            }

            String cmd = line.trim().toUpperCase();
            if (cmd.equals("Q")) break;
            if (cmd.equals("C")) {
                game = Game.createInitial();
                continue;
            }

            apply(game, cmd);
        }
    }

    private static void apply(Game game, String cmd) {
        if (cmd.length() == 1) {
            char c = cmd.charAt(0);
            if (c >= '1' && c <= '3') {
                int before = game.getDefenseScore();
                game.pass(c - '1');
                if (game.getDefenseScore() > before) {
                    System.out.println("Pass failed! Defenders intercepted. Resetting...");
                }
            } else if ("WASD".indexOf(c) >= 0) {
                game.move(c);
            }
            return;
        }
        char c0 = cmd.charAt(0);
        char c1 = cmd.charAt(1);
        if (c0 >= '1' && c0 <= '3' && "WASD".indexOf(c1) >= 0) {
            game.setFocusedIndex(c0 - '1');
            game.move(c1);
        }
    }

    private static void render(Game game) {
        int inner = Game.GRID_SIZE * 3;
        String topBorder = "+" + "-".repeat(inner) + "+";
        int sideDashes = (inner - 3) / 2;
        String bottomBorder = "+" + "-".repeat(sideDashes) + "OOO"
            + "-".repeat(inner - 3 - sideDashes) + "+";

        System.out.println(topBorder);
        for (int i = 0; i < Game.GRID_SIZE; i++) {
            StringBuilder line = new StringBuilder("|");
            for (int j = 0; j < Game.GRID_SIZE; j++) {
                line.append(cell(game, i, j));
            }
            line.append("|");
            System.out.println(line);
        }
        System.out.println(bottomBorder);
        String ball = game.getPossession() == Game.Possession.OFFENSE
            ? game.getBallHolder().getName()
            : "defenders";
        System.out.println("Focused: " + game.getFocusedPlayer().getName()
            + " | Ball: " + ball
            + " | Shot: " + shotType(game)
            + " | Score  players:" + game.getOffenseScore()
            + "  defenders:" + game.getDefenseScore());
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
