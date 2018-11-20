package NinaRowApp.interfaceDelegate;

import NinaRowApp.NinaRowApp;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

import java.util.List;

public interface BoardPainter { // if i_ColDelimiter == number of col of the logic board,
                                // we paint all the board, else, we paint only the i_ColDelimiter col index.
    default void paintBoard(byte[][] i_LogicBoard, GridPane i_VisualBoard, List<Color> i_cellColors, int i_ColDelimiter) {

        int boardRows = i_LogicBoard.length, boardCols = i_LogicBoard[0].length;
        int startCOl = i_ColDelimiter == i_LogicBoard[0].length ? 0 : i_ColDelimiter;
        int endCol = i_ColDelimiter == i_LogicBoard[0].length ? i_LogicBoard[0].length : i_ColDelimiter + 1;
        Color defaultColor = i_cellColors.get(i_cellColors.size() - 1);

        for (int i = 0; i < boardRows; i++)
        {
            for (int j = startCOl; j < endCol; j++)
            {
                int playerIndex = i_LogicBoard[i][j];
                Color color = playerIndex == -1 ? defaultColor : i_cellColors.get(playerIndex);

                ((Button)i_VisualBoard.getChildren().get((i + 1) * (boardCols + 1) + j)).setBackground(
                        new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY)));
            }
        }
    }
}
