package put.ai.games.betterplayer;

import java.util.List;

import put.ai.games.game.Board;
import put.ai.games.game.Move;
import put.ai.games.game.Player;

public class BetterPlayer extends Player {

    private static final int MAX_DEPTH = 3;

    @Override
    public String getName() {
        return "Bartosz Kozlowski 155869 Agnieszka Maleszka 155941";
    }

    @Override
    public Move nextMove(Board board) {
        long startTime = System.currentTimeMillis();
        long timeLimit = getTime();
        List<Move> moves = board.getMovesFor(getColor());

        if (moves.size() == 1) {
            return moves.get(0);
        }

        // Wartości do alfa–beta
        double alpha = Double.NEGATIVE_INFINITY;
        double beta = Double.POSITIVE_INFINITY;
        double bestValue = Double.NEGATIVE_INFINITY;
        Move bestMove = moves.get(0);

        for (Move m : moves) {
            board.doMove(m);
            double value = minValue(board, 1, alpha, beta, startTime, timeLimit);
            board.undoMove(m);

            if (value > bestValue) {
                bestValue = value;
                bestMove = m;
            }
            alpha = Math.max(alpha, bestValue);
        }

        return bestMove;
    }


    private double minValue(Board board, int depth, double alpha, double beta, long startTime, long timeLimit) {
        if (System.currentTimeMillis() - startTime > timeLimit - 5) {
            return evaluateBoard(board);
        }

        Player.Color winner = board.getWinner(getColor());
        if (winner != null || depth >= MAX_DEPTH) {
            return evaluateBoard(board);
        }

        double value = Double.POSITIVE_INFINITY;
        List<Move> moves = board.getMovesFor(getOpponent(getColor()));
        if (moves.isEmpty()) {
            return evaluateBoard(board);
        }

        for (Move m : moves) {
            board.doMove(m);
            value = Math.min(value, maxValue(board, depth + 1, alpha, beta, startTime, timeLimit));
            board.undoMove(m);

            if (value <= alpha) {
                return value;
            }
            beta = Math.min(beta, value);
        }
        return value;
    }


    private double maxValue(Board board, int depth, double alpha, double beta, long startTime, long timeLimit) {
        if (System.currentTimeMillis() - startTime > timeLimit - 5) {
            return evaluateBoard(board);
        }

        Player.Color winner = board.getWinner(getColor());
        if (winner != null || depth >= MAX_DEPTH) {
            return evaluateBoard(board);
        }

        double value = Double.NEGATIVE_INFINITY;
        List<Move> moves = board.getMovesFor(getColor());
        if (moves.isEmpty()) {
            return evaluateBoard(board);
        }

        for (Move m : moves) {
            board.doMove(m);
            value = Math.max(value, minValue(board, depth + 1, alpha, beta, startTime, timeLimit));
            board.undoMove(m);

            if (value >= beta) {
                return value;
            }
            alpha = Math.max(alpha, value);
        }
        return value;
    }

    private double evaluateBoard(Board board) {
        Player.Color winner = board.getWinner(getColor());
        if (winner == getColor()) {
            return 1_000_000.0; // wygrana
        } else if (winner == getOpponent(getColor())) {
            return -1_000_000.0; // przegrana
        } else if (winner == Player.Color.EMPTY) {
            // Remis
            return 0.0;
        }

        double score = 0.0;
        int size = board.getSize();
        Player.Color me = getColor();
        Player.Color opp = getOpponent(me);

        for (int r = 0; r < size; r++) {
            int countMe = 0;
            int countOpp = 0;
            for (int c = 0; c < size; c++) {
                if (board.getState(r, c) == me) countMe++;
                if (board.getState(r, c) == opp) countOpp++;
            }
            score += rowColumnScore(countMe, countOpp);
        }

        for (int c = 0; c < size; c++) {
            int countMe = 0;
            int countOpp = 0;
            for (int r = 0; r < size; r++) {
                if (board.getState(r, c) == me) countMe++;
                if (board.getState(r, c) == opp) countOpp++;
            }
            score += rowColumnScore(countMe, countOpp);
        }

        return score;
    }

    private double rowColumnScore(int countMe, int countOpp) {
        return (countMe - countOpp) * 10.0;
    }
}
