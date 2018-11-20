package NinaRowApp.components.replay;

import NinaRowApp.components.playerInfoBox.PlayerInfoBoxController;
import NinaRowApp.interfaceDelegate.BoardAllocator;
import NinaRowApp.interfaceDelegate.BoardPainter;
import engine.CellChange;
import engine.NinaRowEngine;
import engine.Variant;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import turn.MoveType;
import turn.Turn;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ReplayStageController implements Initializable, BoardPainter, BoardAllocator {
    @FXML private Button m_NextButton;
    @FXML private Button m_PrevButton;
    @FXML private ScrollPane m_BoardScrollPane;
    @FXML private Label m_CurrentTurnLabel;
    @FXML private FlowPane m_PlayersInfo;

    private NinaRowEngine m_Engine;
    private byte[][] m_LogicBoard;
    private GridPane m_VisualBoard;
    private List<Color> m_DiscsColors;
    private List<String> m_DiscsColorsStrings;
    private int m_TurnIndex;
    private List<PlayerInfoBoxController> m_PlayersBoxesControllers = new ArrayList<>();
    private Turn m_CurrentTurn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void initController(NinaRowEngine i_Engine, List<Color> i_DiscsColor, List<String> i_Colors) {
        m_Engine = i_Engine;
        m_DiscsColors = i_DiscsColor;
        m_DiscsColorsStrings = i_Colors;
        allocateLogicBoard();
        allocateVisualBoard();
        allocatePlayersInfo();
        m_TurnIndex = m_Engine.getTurnsReplay().size() - 1;
        m_PrevButton.setDisable(m_TurnIndex == -1);
        if (m_TurnIndex != -1)
        {
            m_CurrentTurn = m_Engine.getTurnsHistory().get(m_TurnIndex);
            m_CurrentTurnLabel.setText(m_Engine.getTurnsHistory().get(m_TurnIndex).toString());
        }
        showBoard();
    }

    private void allocatePlayersInfo() {
        for (int i = 0; i < m_Engine.getPlayers().size(); i++) {
            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource("/NinaRowApp/components/playerInfoBox/playerInfoBox.fxml");
            fxmlLoader.setLocation(url);
            try {
                SplitPane box = fxmlLoader.load(url.openStream());
                PlayerInfoBoxController playerInfoBoxController = fxmlLoader.getController();
                m_PlayersBoxesControllers.add(playerInfoBoxController);
                m_PlayersInfo.getChildren().add(box);
                if (m_Engine.getPlayers().get(i).getIsRetire()) {
                    box.getStyleClass().add("retirePlayer");
                }
                playerInfoBoxController.bindToPlayer(m_Engine.getPlayers().get(i), i, m_DiscsColorsStrings.get(i));
                playerInfoBoxController.unBindTurnsAndCounter();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    private void showBoard() {
        paintBoard(m_LogicBoard, m_VisualBoard, m_DiscsColors, m_Engine.getCols());
    }

    private void allocateLogicBoard() {
        m_LogicBoard = new byte[m_Engine.getRows()][m_Engine.getCols()];
        for (int i = 0; i < m_Engine.getRows(); i++)
        {
            for (int j = 0; j < m_Engine.getCols(); j++)
            {
                m_LogicBoard[i][j] = m_Engine.getBoard()[i][j];
            }
        }
    }

    private void allocateVisualBoard() {
        m_VisualBoard = allocateDynamicBoard(m_Engine.getRows(), m_Engine.getCols(), m_DiscsColors.get(m_DiscsColors.size() - 1), m_Engine.getVariant().equals(Variant.POPOUT));
        m_BoardScrollPane.setContent(m_VisualBoard);
        disableVisualBoardButtons();
    }

    @FXML
    private void prevButtonWasClicked(ActionEvent event) {
        List<CellChange> currentTurn = m_Engine.getTurnsReplay().get(m_TurnIndex);

        for (int i = currentTurn.size() - 1; i >= 0; i--)
        {
            m_LogicBoard[currentTurn.get(i).getCellLocation().x][currentTurn.get(i).getCellLocation().y] =
                    currentTurn.get(i).getPrevValue();
        }

        m_CurrentTurn = m_Engine.getTurnsHistory().get(m_TurnIndex);

        int playerIndex = getPlayerBoxControllerById(m_Engine.getTurnsHistory().get(m_TurnIndex).getPlayer().getId());
        if (m_CurrentTurn.getMoveType() != MoveType.RETIRE) {
            m_PlayersBoxesControllers.get(playerIndex).decreaseTurnsCounter();

        }
        else {
            m_PlayersBoxesControllers.get(playerIndex).setIsActive(true);
            m_PlayersBoxesControllers.get(playerIndex).getBox().getStyleClass().removeAll("retirePlayer");
        }

        showBoard();
        m_NextButton.setDisable(false);

        m_TurnIndex --;

        if (m_TurnIndex == -1)
        {
            m_PrevButton.setDisable(true);
            m_CurrentTurnLabel.setText("");
        }
        else
        {
            m_CurrentTurnLabel.setText(m_Engine.getTurnsHistory().get(m_TurnIndex).toString());
        }
    }

    private int getPlayerBoxControllerById(int i_Id) {
        int index = 0;

        for (int i = 0; i < m_PlayersBoxesControllers.size(); i++) {
            if (m_PlayersBoxesControllers.get(i).getPlayerId() == i_Id) {
                index = i;
                break;
            }
        }

        return index;
    }

    @FXML
    private void nextButtonWasClicked(ActionEvent event) {
        List<CellChange> currentTurn = m_Engine.getTurnsReplay().get(m_TurnIndex + 1);

        for (int i = 0; i < currentTurn.size(); i++)
        {
            m_LogicBoard[currentTurn.get(i).getCellLocation().x][currentTurn.get(i).getCellLocation().y] =
                    currentTurn.get(i).getNextValue();
        }

        showBoard();
        m_PrevButton.setDisable(false);
        m_TurnIndex ++;

        m_CurrentTurn = m_Engine.getTurnsHistory().get(m_TurnIndex);
        int playerIndex = getPlayerBoxControllerById(m_Engine.getTurnsHistory().get(m_TurnIndex).getPlayer().getId());
        if (m_CurrentTurn.getMoveType() != MoveType.RETIRE) {
            m_PlayersBoxesControllers.get(playerIndex).incrementTurnsCounter();

        }
        else {
            m_PlayersBoxesControllers.get(playerIndex).setIsActive(false);
            m_PlayersBoxesControllers.get(playerIndex).getBox().getStyleClass().add("retirePlayer");

        }

        m_CurrentTurnLabel.setText(m_Engine.getTurnsHistory().get(m_TurnIndex).toString());
        if (m_TurnIndex == m_Engine.getTurnsReplay().size() - 1)
        {
            m_NextButton.setDisable(true);
        }
    }

    private void disableVisualBoardButtons() {
        for (int i = 0; i < m_Engine.getCols(); i++)
        {
            ((Button)m_VisualBoard.getChildren().get(i)).setDisable(true);
            if (m_Engine.getVariant().equals(Variant.POPOUT)) {
                ((Button)m_VisualBoard.getChildren().get((m_Engine.getRows() + 2) * m_Engine.getCols() - 1 + i)).setDisable(true);
            }
        }
    }
}
