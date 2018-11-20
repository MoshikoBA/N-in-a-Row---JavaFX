package NinaRowApp.components.ninaRowApp;

import NinaRowApp.components.playersInfo.PlayersInfoController;
import NinaRowApp.components.turnsHistory.TurnsHistoryController;
import NinaRowApp.interfaceDelegate.BoardAllocator;
import NinaRowApp.interfaceDelegate.BoardPainter;
import NinaRowApp.components.replay.ReplayStageController;
import NinaRowApp.tasks.ComputerTurnTask;
import NinaRowApp.tasks.LoadXmlTask;
import engine.GameStatus;
import engine.NinaRowEngine;
import engine.Variant;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.StringExpression;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import turn.MoveType;
import turn.Turn;
import javafx.beans.binding.Bindings;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class NinaRowController implements Initializable, BoardPainter, BoardAllocator {

    private NinaRowEngine m_Engine;
    private Scene m_Scene;

    @FXML private VBox rightBorderVBox;
    @FXML private Button m_LoadXmlButton;
    @FXML private Button m_StartGameButton;
    @FXML private Label m_CurrentTurnLabel;
    @FXML private ScrollPane m_BoardScrollPane;
    @FXML private Button m_RetireButton;
    @FXML private Button m_RestartGameButton;
    @FXML private Button m_ReplayButton;
    @FXML private ComboBox<String> m_SkinComboBox;
    @FXML private Label m_ComputerTurnProcess;
    @FXML private Button m_StopGameButton;
    @FXML private Text m_AppTitle;
    @FXML private Label m_VariantLabel;
    @FXML private Label m_Seconds;
    @FXML private Label m_Minutes;

    private Map<Color, String> m_PlayersDiscColor;
    private PlayersInfoController m_PlayersInfoController;
    private TurnsHistoryController m_TurnsHistoryController;
    private ReplayStageController m_ReplayStageController;
    private GridPane m_Board;
    private StringExpression m_CurrentTurnExpression;
    private Timeline m_TimeLine;


    public NinaRowController() {
        m_Engine = new NinaRowEngine();
        m_Engine.setErrorOccurDelegate(this::showErrorMessage);
        m_Engine.setUpdateStatusDelegate(this::statusUpdated);
        m_Engine.setShowBoardDelegate(this::showBoard);
        m_Engine.setGameOverDelegate(this::GameOverHandle);
        m_Engine.setChangeTurnDelegate(this::changeTurn);
        m_Engine.setUpdateCurrentPlayerNameDelegate(this::updateCurrentPlayerName);
        m_Engine.setIncrementPlayerTurnsCounterDelegate(this::incrementPlayerTurnsCounter);
        initPlayersDiscColorList();
    }

    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        allocatePlayersInfoController();
        allocateTurnsHistoryController();
        m_Engine.setTurnAddedDelegate(m_TurnsHistoryController::addNewTurn);
        m_RestartGameButton.setOnAction(this::restartGameButtonWasClicked);
        m_Engine.setPlayerRetireDelegate(this::updateRetirePlayerBox);
        ObservableList<String> skinOptions = FXCollections.observableArrayList("Default", "Water", "Fire", "Grass");
        m_SkinComboBox.setItems(skinOptions);
        m_SkinComboBox.setValue("Default");
    }

    private void initPlayersDiscColorList() {
        m_PlayersDiscColor = new LinkedHashMap<>();
        m_PlayersDiscColor.put(Color.RED, "Red");
        m_PlayersDiscColor.put(Color.BLUE, "Blue");
        m_PlayersDiscColor.put(Color.GREEN, "Green");
        m_PlayersDiscColor.put(Color.YELLOW, "Yellow");
        m_PlayersDiscColor.put(Color.PURPLE, "Purple");
        m_PlayersDiscColor.put(Color.PINK, "Pink");
        m_PlayersDiscColor.put(Color.LIGHTGRAY, "Default");
    }

    public void setScene(Scene i_Scene) {
        m_Scene = i_Scene;
    }

    private void allocateTurnsHistoryController() {
        FXMLLoader fxmlLoader = new FXMLLoader();
        URL url = getClass().getResource("/NinaRowApp/components/turnsHistory/turnsHistory.fxml");
        fxmlLoader.setLocation(url);
        try
        {
            Node turnsHistory = fxmlLoader.load(url.openStream());
            rightBorderVBox.getChildren().add(turnsHistory);

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        m_TurnsHistoryController = fxmlLoader.getController();
    }

    private void allocatePlayersInfoController() {
        FXMLLoader fxmlLoader = new FXMLLoader();
        URL url = getClass().getResource("/NinaRowApp/components/playersInfo/playersInfo.fxml");
        fxmlLoader.setLocation(url);
        try
        {
            Node playersInfo = fxmlLoader.load(url.openStream());
            rightBorderVBox.getChildren().add(playersInfo);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        m_PlayersInfoController = fxmlLoader.getController();
    }

    @FXML
     public void LoadXmlButtonWasClicked(ActionEvent event) {
        if (m_Engine.isInitialized().getValue()) {
            resetGame();
        }

        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter xmlFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().addAll(xmlFilter);
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));
        File file = fileChooser.showOpenDialog(null);

        if (file != null)
        {
            LoadXmlTask task = new LoadXmlTask( (str) -> {
                boolean isSucceed = false;
                try
                {
                    m_Engine.initEngineFromFile(str);
                    Platform.runLater(() -> m_AppTitle.setText(m_Engine.getSequence() + " in a Row"));
                    Platform.runLater(() -> m_VariantLabel.setText(m_Engine.getVariant().toString()));
                    isSucceed = true;
                }
                catch (Exception e)
                {
                    Platform.runLater(() -> this.showErrorMessage(e.getMessage()));
                }
                finally {
                    boolean finalIsSucceed = isSucceed;
                    Platform.runLater(() -> setButtonsDisabilityAfterLoadingXml(finalIsSucceed));
                }
            }
                    , file.getPath());

            new Thread(task).start();
        }
    }

    private void showErrorMessage(String i_ErrorMessage) {
        if (Platform.isFxApplicationThread())
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(i_ErrorMessage);
            alert.setHeight(100);
            alert.setWidth(200);
            alert.showAndWait();
        }

    }

    @FXML
    public void startGameButtonWasClicked(ActionEvent event) {
        m_PlayersInfoController.allocatePlayersInfoBoxes(m_Engine.getPlayers(), m_PlayersDiscColor);

        m_CurrentTurnExpression = Bindings.concat("Current player's turn: ",
                m_Engine.getCurrentPlayerTurnNameProperty());
        m_CurrentTurnLabel.textProperty().bind(m_CurrentTurnExpression);
        m_StartGameButton.setDisable(true);
        m_LoadXmlButton.setDisable(true);
        m_RetireButton.setDisable(false);
        m_RestartGameButton.setDisable(true);
        m_ReplayButton.setDisable(false);
        m_SkinComboBox.setDisable(false);
        m_StopGameButton.setDisable(false);
        m_AppTitle.setText(m_Engine.getSequence() + " in a Row");
        m_VariantLabel.setText(m_Engine.getVariant().toString());
        m_TimeLine = new Timeline(new KeyFrame(Duration.millis(1000), actionEvent -> tickTimer()));
        m_TimeLine.setCycleCount(Animation.INDEFINITE);
        m_TimeLine.play();
        allocateBoard();
    }

    private void tickTimer() {
        Platform.runLater(() -> {
            int minutes = Integer.parseInt(m_Minutes.getText());
            int seconds = Integer.parseInt(m_Seconds.getText());

            minutes = seconds + 1 == 60 ? minutes + 1 : minutes;
            seconds = seconds + 1 == 60 ? 0 : seconds + 1;
            m_Seconds.setText(String.format("%02d", seconds));
            m_Minutes.setText(String.format("%02d",minutes));
        });
    }

    @FXML
    private void retireButtonWasClicked(ActionEvent event) {
        m_Engine.retireGame();
    }

    private void allocateBoard()
    {
        m_Board = allocateDynamicBoard(m_Engine.getRows(), m_Engine.getCols(), (Color)m_PlayersDiscColor.keySet().toArray()[m_PlayersDiscColor.size() - 1], m_Engine.getVariant().equals(Variant.POPOUT));
        m_BoardScrollPane.setContent(m_Board);
        addPlayMoveButtonsAction();
    }

    private void addPlayMoveButtonsAction() {
        for (int i = 0; i < m_Engine.getCols(); i ++)
        {
            ((Button)m_Board.getChildren().get(i)).setOnAction(this::playMoveButtonWasClicked);
            if (m_Engine.getVariant().equals(Variant.POPOUT)) {
                ((Button)m_Board.getChildren().get((m_Engine.getCols() + 1) * (m_Engine.getRows() + 1) - 1 + i)).setOnAction(this::playMoveButtonWasClicked);

            }
        }
    }

    private void insertButtonWasClicked(ActionEvent event) {

        int colIndex = Integer.parseInt(((Button)event.getSource()).getText()) - 1;
        int rowIndex = m_Engine.getRowIndexToInsert(colIndex);

        if (rowIndex != -1)
        {
            m_Engine.executeMove(new Turn(m_Engine.getPlayers().get(m_Engine.getCurrentPlayerTurn()), MoveType.INSERT_DISC, colIndex, rowIndex));
        }
    }

    private void playMoveButtonWasClicked(ActionEvent event) {
        int colIndex = Integer.parseInt(((Button)event.getSource()).getText()) - 1;
        MoveType moveType = GridPane.getRowIndex((Button)event.getSource()) == 1 ?
                MoveType.INSERT_DISC : MoveType.POPOUT_DISC;

        int rowIndex = moveType.equals(MoveType.INSERT_DISC) ?
                m_Engine.getRowIndexToInsert(colIndex) : m_Engine.getRows() - 1;

        boolean isLegalMove = moveType.equals(MoveType.INSERT_DISC) ?
                rowIndex != m_Engine.UNDEFINED : m_Engine.isLegalPopoutMove(colIndex);

        if (isLegalMove)
        {
            m_Engine.executeMove(new Turn(m_Engine.getPlayers().get(m_Engine.getCurrentPlayerTurn()), moveType, colIndex, rowIndex));
        }
    }

    private void statusUpdated() {
        if (Platform.isFxApplicationThread())
        {
            StringBuilder message = new StringBuilder();
            if (!m_Engine.getGameStatus().equals(GameStatus.RUNNING)) {
                if (m_Engine.getPlayers().stream().filter(i -> i.getIsWinner()).count() == 0) {
                    message.append("Tie, board is full.");
                }
                else {
                    m_Engine.getPlayers().stream().filter(i -> i.getIsWinner()).forEach(i -> message.append(i.getName())
                            .append(" win").append(System.lineSeparator()));
                }

                showRoundOverMessage(message.toString());
            }
        }

    }

    private void showRoundOverMessage(String i_Message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Round Over");
        alert.setHeaderText(i_Message);
        alert.setHeight(100);
        alert.setWidth(200);
        alert.showAndWait();

    }

    private void showBoard(int i_ColDelimiter) {
        if (Platform.isFxApplicationThread())
        {
            paintBoard(m_Engine.getBoard(), m_Board,  m_PlayersDiscColor.keySet().stream().collect(Collectors.toList()), i_ColDelimiter);
        }
    }

    private void GameOverHandle() {
        m_TimeLine.stop();
        disableBoardButtons();
        m_LoadXmlButton.setDisable(false);
        m_RetireButton.setDisable(true);
        m_RestartGameButton.setDisable(false);
        m_StopGameButton.setDisable(true);
    }

    private void disableBoardButtons() {
        for (int i = 0; i < m_Engine.getCols(); i ++)
        {
            ((Button)m_Board.getChildren().get(i)).setDisable(true);
            if (m_Engine.getVariant().equals(Variant.POPOUT)) {
                ((Button)m_Board.getChildren().get((m_Engine.getCols() + 1) * (m_Engine.getRows() + 1) - 1 + i)).setDisable(true);

            }
        }
    }

    private void resetGame() {
        if (Platform.isFxApplicationThread())
        {
            m_TurnsHistoryController.reset();
            m_PlayersInfoController.reset();
            m_CurrentTurnLabel.textProperty().unbind();
            m_CurrentTurnLabel.setText("");
            m_BoardScrollPane.setContent(null);
            m_ReplayButton.setDisable(true);
            m_AppTitle.setText("N in a Row");
            m_VariantLabel.setText("");
            m_Minutes.setText("00");
            m_Seconds.setText("00");
            m_TimeLine.stop();
            m_Engine.restartGame();
        }
    }

    @FXML
    private void replayButtonWasClicked(ActionEvent event) {
        FXMLLoader fxmlLoader = new FXMLLoader();
        URL url = getClass().getResource("/NinaRowApp/components/replay/replayStage.fxml");
        fxmlLoader.setLocation(url);
        Parent root = null;
        try {
            root = fxmlLoader.load(url.openStream());
            m_ReplayStageController = fxmlLoader.getController();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Replay");
        if (!m_SkinComboBox.getValue().equals("Default"))
        {
            root.getStylesheets().add("/NinaRowApp/css/" + m_SkinComboBox.getValue().toLowerCase() + "/NinaRow.css");
            root.getStylesheets().add("/NinaRowApp/css/" + m_SkinComboBox.getValue().toLowerCase() + "/PlayerInfoBox.css");
            root.getStylesheets().add("/NinaRowApp/css/" + m_SkinComboBox.getValue().toLowerCase() + "/PlayersInfo.css");
        }

        stage.setScene(new Scene(root, 900, 700));
        stage.setMinWidth(650);
        stage.setMinHeight(560);
        m_ReplayStageController.initController(m_Engine, m_PlayersDiscColor.keySet().stream().collect(Collectors.toList()), m_PlayersDiscColor.values().stream().collect(Collectors.toList()));
        stage.show();
    }

    private void setButtonsDisabilityAfterLoadingXml(boolean i_IsSucceed) {
        m_StartGameButton.setDisable(!i_IsSucceed);
        m_RestartGameButton.setDisable(true);
    }

    @FXML
    private void skinWasChanged(ActionEvent event) {
        String styleSheetName = m_SkinComboBox.getValue().toLowerCase();

        m_Scene.getStylesheets().remove(0, m_Scene.getStylesheets().size());
        if (!styleSheetName.equals("default"))
        {
            m_Scene.getStylesheets().add(getClass().getResource("/NinaRowApp/css/" + styleSheetName + "/PlayerInfoBox.css").toExternalForm());
            m_Scene.getStylesheets().add(getClass().getResource("/NinaRowApp/css/" + styleSheetName + "/TurnsHistory.css").toExternalForm());
            m_Scene.getStylesheets().add(getClass().getResource("/NinaRowApp/css/" + styleSheetName + "/PlayersInfo.css").toExternalForm());
            m_Scene.getStylesheets().add(getClass().getResource("/NinaRowApp/css/" + styleSheetName + "/NinaRow.css").toExternalForm());

        }
    }

    private void playComputerTurn() {

        ComputerTurnTask computerTurnTask = new ComputerTurnTask(m_Engine);
        m_ComputerTurnProcess.textProperty().bind(computerTurnTask.messageProperty());

        new Thread(computerTurnTask).start();
    }

    private void updateCurrentPlayerName() {
        if (Platform.isFxApplicationThread())
        {
            m_Engine.updateCurrentPlayerName();
        }
    }

    private void incrementPlayerTurnsCounter() {
        if (Platform.isFxApplicationThread())
        {
            m_Engine.incrementPlayerTurnsCounter();
        }
    }

    @FXML
    private void stopGameButtonWasClicked(ActionEvent event) {
        showErrorMessage("Game has been stopped");
        resetGame();
    }

    private void updateRetirePlayerBox() {
        SplitPane retirePlayer = m_PlayersInfoController.getSplits().get(m_Engine.getCurrentPlayerTurn());
        retirePlayer.getStyleClass().add("retirePlayer");
    }

    private void changeTurn() {
        if (Platform.isFxApplicationThread())
        {
            m_Engine.changeTurn();
            if (!m_Engine.isCurrentTurnIsHuman())
            {
                playComputerTurn();
            }
        }
    }

    private void restartGameButtonWasClicked(ActionEvent event) {
        resetGame();
        startGameButtonWasClicked(event);
    }
}
