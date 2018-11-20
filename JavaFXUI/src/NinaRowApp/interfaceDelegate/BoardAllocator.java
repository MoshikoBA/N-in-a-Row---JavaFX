package NinaRowApp.interfaceDelegate;

import NinaRowApp.NinaRowApp;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public interface BoardAllocator {
    default GridPane allocateDynamicBoard(int i_Rows, int i_Cols, Color i_CellsColor, boolean i_IsPopout) {
        GridPane board = new GridPane();
        board.getStyleClass().add("board");
        board.setHgap(5);
        board.setVgap(5);
        setBoardProperty(board, i_Rows, i_Cols);

        addActionButtons(board, 1, i_Cols);

        for (int i = 2; i < i_Rows + 2; i ++)
        {
            Button rowIndexButton = new Button(String.valueOf(i -1));
            rowIndexButton.setPrefSize(50, 50);
            rowIndexButton.setMinSize(50, 50);
            rowIndexButton.setMaxSize(50, 50);
            rowIndexButton.setDisable(true);
            rowIndexButton.getStyleClass().add("boardRowIndexCell");
            board.add(rowIndexButton, 1, i);
            for (int j = 2; j < i_Cols + 2; j++)
            {
                Button cell = new Button();
                cell.setPrefSize(50, 50);
                cell.setMinSize(50, 50);
                cell.setMaxSize(50, 50);
                cell.setDisable(true);
                cell.getStyleClass().add("boardCell");
                cell.setBackground(new Background(new BackgroundFill(i_CellsColor, CornerRadii.EMPTY, Insets.EMPTY)));
                board.add(cell, j, i);
            }
        }
        if (i_IsPopout) {
            addActionButtons(board,i_Rows + 2, i_Cols);
        }

        return board;
    }

    default void setBoardProperty(GridPane i_Board, int i_Rows, int i_Cols) {
        RowConstraints growRow1 = new RowConstraints();
        growRow1.setVgrow(Priority.ALWAYS);

        i_Board.getRowConstraints().add(growRow1);

        ColumnConstraints growCol1 = new ColumnConstraints();
        growCol1.setHgrow(Priority.ALWAYS);

        i_Board.getColumnConstraints().add(growCol1);

        for (int i = 1; i <= i_Rows + 2; i ++)
        {
            RowConstraints regRow = new RowConstraints();
            regRow.setVgrow(Priority.NEVER);
            i_Board.getRowConstraints().add(regRow);
        }

        RowConstraints growRow2 = new RowConstraints();
        growRow2.setVgrow(Priority.ALWAYS);
        i_Board.getRowConstraints().add(growRow2);


        for (int i = 1; i <= i_Cols + 1; i ++)
        {
            ColumnConstraints regCol = new ColumnConstraints();
            regCol.setHgrow(Priority.NEVER);
            i_Board.getColumnConstraints().add(regCol);
        }

        ColumnConstraints growCol2 = new ColumnConstraints();
        growCol2.setHgrow(Priority.ALWAYS);
        i_Board.getColumnConstraints().add(growCol2);
    }

    default void addActionButtons(GridPane i_Board, int i_RowIndex, int i_Cols) {

        for (int i = 1; i < i_Cols + 1; i++)
        {
            Button cell = new Button(String.valueOf(i));
            cell.setPrefSize(50, 50);
            cell.setMinSize(50, 50);
            cell.setMaxSize(70, 70);
            cell.getStyleClass().add("boardActionCell");
            i_Board.add(cell, i + 1, i_RowIndex);
        }
    }
}
