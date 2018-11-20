package engine;

import engine.exception.GamePropertiesException;
import engine.exception.IllegalXmlFile;
import engine.gameProperties.GameDescriptor;
import engine.interfaceDelegate.*;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import player.Player;
import turn.MoveType;
import turn.Turn;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.awt.*;
import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

public class NinaRowEngine implements Serializable {
    public static final int EMPTY = -1;
    public static final int UNDEFINED = -1;
    public static final int MIN_ROWS = 5;
    public static final int MAX_ROWS = 50;
    public static final int MIN_COLS = 6;
    public static final int MAX_COLS = 30;
    public static final int MIN_TARGET = 2;
    public static final int MIN_PLAYERS = 2;
    public static final int MAX_PLAYERS = 6;

    private int m_BoardRows;
    private int m_BoardCols;
    private int m_Sequence;
    private byte[][] m_Board;
    private Variant m_Variant;
    private BooleanProperty m_IsInitialized = new SimpleBooleanProperty(false);
    private GameStatus m_Status;
    private List<Player> m_Players;
    private final List<Turn> m_TurnsHistory = new ArrayList<Turn>();
    private int m_CurrentTurn;
    private Instant m_StartTime;
    private StringProperty m_CurrentPlayerTurnNameProperty = new SimpleStringProperty();
    private List<List<CellChange>> m_TurnsReplay = new ArrayList<>();
    private Consumer<String> m_ErrorOccurDelegate;
    private Consumer<Turn> m_TurnAddedDelegate;
    private ShowBoardDelegate m_ShowBoardDelegate;
    private UpdateStatusDelegate m_UpdateStatusDelegate;
    private GameOverDelegate m_GameOverDelegate;
    private ChangeTurnDelegate m_ChangeTurnDelegate;
    private UpdateCurrentPlayerNameDelegate m_UpdateCurrentPlayerNameDelegate;
    private IncrementPlayerTurnsCounterDelegate m_IncrementPlayerTurnsCounter;
    private PlayerRetireDelegate m_PlayerRetireDelegate;

    public void setTurnAddedDelegate(Consumer<Turn> i_TurnAddedDelegate) {m_TurnAddedDelegate= i_TurnAddedDelegate;}

    public Consumer<Turn> getTurnAddedDelegate() { return m_TurnAddedDelegate;}

    public void setErrorOccurDelegate(Consumer<String> i_ErrorOccurDelegate) {
        m_ErrorOccurDelegate = i_ErrorOccurDelegate;
    }

    public void setUpdateStatusDelegate(UpdateStatusDelegate i_UpdateStatusDelegate) {
        m_UpdateStatusDelegate = i_UpdateStatusDelegate;
    }

    public UpdateStatusDelegate getUpdateStatusDelegate() {
        return m_UpdateStatusDelegate;
    }

    public void setShowBoardDelegate(ShowBoardDelegate i_ShowBoardDelegate) {
        m_ShowBoardDelegate = i_ShowBoardDelegate;
    }

    public ShowBoardDelegate getShowBoardDelegate() {
        return m_ShowBoardDelegate;
    }

    public StringProperty getCurrentPlayerTurnNameProperty() {
        return m_CurrentPlayerTurnNameProperty;
    }

    public void setGameOverDelegate(GameOverDelegate i_GameOverDelegate) {
        m_GameOverDelegate = i_GameOverDelegate;
    }

    public GameOverDelegate getGameOverDelegate() {
        return m_GameOverDelegate;
    }

    public void setChangeTurnDelegate(ChangeTurnDelegate i_ChangeTurnDelegate) {
        m_ChangeTurnDelegate = i_ChangeTurnDelegate;
    }

    public ChangeTurnDelegate getChangeTurnDelegate() {
        return m_ChangeTurnDelegate;
    }

    public void setUpdateCurrentPlayerNameDelegate(UpdateCurrentPlayerNameDelegate i_UpdateCurrentPlayerNameDelegate) {
        m_UpdateCurrentPlayerNameDelegate = i_UpdateCurrentPlayerNameDelegate;
    }

    public UpdateCurrentPlayerNameDelegate getUpdateCurrentPlayerNameDelegate() {
        return m_UpdateCurrentPlayerNameDelegate;
    }

    public void setIncrementPlayerTurnsCounterDelegate(IncrementPlayerTurnsCounterDelegate i_IncrementPlayerTurnsCounter) {
        m_IncrementPlayerTurnsCounter = i_IncrementPlayerTurnsCounter;
    }

    public IncrementPlayerTurnsCounterDelegate getIncrementPlayerTurnsCounterDelegate() {
        return m_IncrementPlayerTurnsCounter;
    }

    public void setPlayerRetireDelegate(PlayerRetireDelegate i_PlayerRetireDelegate) {
        m_PlayerRetireDelegate = i_PlayerRetireDelegate;
    }

    private void initBoard() {
        m_Board = new byte[getRows()][getCols()];

        for (int i = 0; i < m_BoardRows; i++)
        {
            for (int j = 0; j < m_BoardCols; j++)
            {
                m_Board[i][j] = EMPTY;
            }
        }
    }

    public List<Player> getPlayers() {
        return m_Players;
    }

    public void setPlayers(List<Player> i_Players) { this.m_Players = i_Players; }

    public int getCurrentPlayerTurn() {
        return m_CurrentTurn;
    }

    public List<Turn> getTurnsHistory() {
        return m_TurnsHistory;
    }

    public byte[][] getBoard() {return m_Board; }

    public GameStatus getGameStatus() { return m_Status; }

    public void setGameStatus(GameStatus i_Value) { this.m_Status = i_Value; }

    public int getRows() { return m_BoardRows; }

    public int getCols() { return m_BoardCols; }

    public int getSequence() { return m_Sequence; }

    public BooleanProperty isInitialized() { return m_IsInitialized; }

    public void setIsInitialized(boolean i_Value) { m_IsInitialized.setValue(i_Value); }

    public Variant getVariant () {  return m_Variant; }

    public List<List<CellChange>> getTurnsReplay() {return this.m_TurnsReplay;}


    public void changeTurn() {
        do {
            m_CurrentTurn = (m_CurrentTurn + 1) % m_Players.size();
        } while (m_Players.get(m_CurrentTurn).getIsRetire() || (checkIsBoardFull() && !canCurrentPlayerPopoutDisc()));

        if (m_UpdateCurrentPlayerNameDelegate != null)
        {
            m_UpdateCurrentPlayerNameDelegate.action();
        }
    }

    public void updateCurrentPlayerName() {
        m_CurrentPlayerTurnNameProperty.setValue(m_Players.get(m_CurrentTurn).getName());
    }

    public void initEngineFromFile(String i_FilePath) throws IllegalXmlFile,
            GamePropertiesException, JAXBException
    {
        JAXBContext jc = JAXBContext.newInstance("engine.gameProperties");
        Unmarshaller u = jc.createUnmarshaller();
        File file = new File(i_FilePath);
        GameDescriptor gameProperties = null;
        StringBuilder errorMessage = new StringBuilder();

        try {
            gameProperties = (GameDescriptor) u.unmarshal(file);
        }
        catch (Exception e) {
            throw new IllegalXmlFile();
        }

        if (isLegalProperties(gameProperties, errorMessage))
        {
            List<Player> legalPlayers = getPlayersFromGameProperties(gameProperties);
            Variant variant = getGameVariantFromGameProperties(gameProperties);
            initEngine(gameProperties.getGame().getBoard().getRows(),
                    gameProperties.getGame().getBoard().getColumns().intValue(),
                    gameProperties.getGame().getTarget().intValue(), variant,
                    legalPlayers);
        }
        else
        {
            throw new GamePropertiesException(errorMessage.toString());
        }
    }

    private Variant getGameVariantFromGameProperties(GameDescriptor i_GameProperties) {
        String variant = i_GameProperties.getGame().getVariant().toUpperCase();
        return Variant.valueOf(variant);
    }

    private List<Player> getPlayersFromGameProperties(GameDescriptor i_GameProperties) {
        List<engine.gameProperties.Player> playersFromGameProperties = i_GameProperties.getPlayers().getPlayer();
        List<Player> legalPlayers = new ArrayList<>();

        for (engine.gameProperties.Player player : playersFromGameProperties)
        {
            String name = player.getName();
            boolean isHuman = player.getType().equals("Human");
            short id = player.getId();
            legalPlayers.add(new Player(name, isHuman, id));
        }

        return legalPlayers;
    }

    public String  getPlayersDetails() {
        StringBuilder playersDetails = new StringBuilder();

        for (Player player : m_Players)
        {
            playersDetails.append(System.lineSeparator()).append("---- Player #")
                    .append(m_Players.indexOf(player) + 1).append(" -----").append(System.lineSeparator())
                    .append(player).append(System.lineSeparator());
        }

        return playersDetails.toString();
    }

    private void initEngine(int i_BoardRows, int i_BoardCols, int i_Target, Variant i_GameVariant, List<Player> i_Players) {
        m_BoardRows = i_BoardRows;
        m_BoardCols = i_BoardCols;
        m_Sequence = i_Target;
        m_Status = GameStatus.RUNNING;
        m_CurrentTurn = 0;
        m_TurnsHistory.clear();
        m_IsInitialized.setValue(true);
        m_Variant = i_GameVariant;
        m_Players = i_Players;
        m_CurrentPlayerTurnNameProperty.setValue(m_Players.get(0).getName());
        m_TurnsReplay.clear();
        initBoard();
    }

    private boolean isLegalProperties(GameDescriptor i_Game, StringBuilder i_ErrorMessage) {
        boolean isLegalProperties = true;

        if (!isLegalBoard(i_Game, i_ErrorMessage)) {
            isLegalProperties = false;
        }
        if (!isLegalPlayers(i_Game, i_ErrorMessage)) {
            isLegalProperties = false;
        }

        return isLegalProperties;
    }

    private boolean isLegalPlayers(GameDescriptor i_Game, StringBuilder i_ErrorMessage) {
        boolean isLegalPlayers = true;
        List<engine.gameProperties.Player> players = i_Game.getPlayers().getPlayer();

        if (players.size() < MIN_PLAYERS || players.size() > MAX_PLAYERS) {
            isLegalPlayers = false;
            i_ErrorMessage.append("Player's number is illegal.").append(System.lineSeparator());
        }

        if (!isDifferentPlayersID(players)) {
            isLegalPlayers = false;
            i_ErrorMessage.append("Some Players have same ID.");
        }

        return isLegalPlayers;
    }

    private boolean isDifferentPlayersID(List<engine.gameProperties.Player> i_Players) {
        boolean isDifferentID = true;
        int numberOfPlayers = i_Players.size();
        Set<Short> setOfIDs = new HashSet<>();

        for (engine.gameProperties.Player player : i_Players)
        {
            setOfIDs.add(player.getId());
        }

        if (setOfIDs.size() != numberOfPlayers) {
            isDifferentID = false;
        }

        return isDifferentID;
    }

    private boolean isLegalBoard(GameDescriptor i_Game, StringBuilder i_ErrorMessage) {
        boolean isLegalBoardRows = false, isLegalBoardColumns = false, isLegalBoardTarget = false;

        if (i_Game.getGame().getBoard().getRows() >= MIN_ROWS && i_Game.getGame().getBoard().getRows() <= MAX_ROWS) {
            isLegalBoardRows = true;
        }
        else {
            i_ErrorMessage.append("Board's rows number is illegal.").append(System.lineSeparator());

        }
        if (i_Game.getGame().getBoard().getColumns().intValue() >= MIN_COLS && i_Game.getGame().getBoard().getColumns().intValue() <= MAX_COLS) {
            isLegalBoardColumns = true;
        }
        else {
            i_ErrorMessage.append("Board's columns number is illegal.").append(System.lineSeparator());
        }
        if (i_Game.getGame().getTarget().intValue() >= MIN_TARGET && i_Game.getGame().getTarget().intValue() < i_Game.getGame().getBoard().getColumns().intValue() && i_Game.getGame().getTarget().intValue() < i_Game.getGame().getBoard().getRows())
        {
            isLegalBoardTarget = true;
        }
        else {
            i_ErrorMessage.append("Board's target is illegal.").append(System.lineSeparator());
        }

        return isLegalBoardRows && isLegalBoardColumns && isLegalBoardTarget;
    }

    public boolean isCurrentTurnIsHuman() {
        return m_Players.get(m_CurrentTurn).getIsHumanProperty().getValue();
    }


    public void checkForWinning(int i_ColIndex) {
        for (int i = 0; i < m_BoardRows; i++) {
            if (m_Board[i][i_ColIndex] != EMPTY) {
                checkSequenceForSpecificCell(i, i_ColIndex);
            }
        }
    }

    private void checkSequenceForSpecificCell(int i_Row, int i_Col) {
        checkForRowAndColSequence(i_Row, i_Col);
        checkForDiagonalsSequence(i_Row, i_Col);
    }

    private void checkForRowAndColSequence(int i_Row, int i_Col) {
        int colRightDelimiter, rowBottomDelimiter, rowLength = m_Sequence * 2 - 1,
                colLength = m_Sequence * 2 - 1;
        Point rowStartPosition = new Point(), colStartPosition = new Point();

        colStartPosition.x = i_Col - m_Sequence + 1;
        colStartPosition.y = i_Row;
        rowStartPosition.y = i_Row - m_Sequence + 1;
        rowStartPosition.x = i_Col;

        if (colStartPosition.x < 0)
        {
            colStartPosition.x = m_Variant.equals(Variant.CIRCULAR) ?
                    m_BoardCols + i_Col - m_Sequence + 1 : 0;
        }

        if (rowStartPosition.y < 0)
        {
            rowStartPosition.y = m_Variant.equals(Variant.CIRCULAR) ?
                    m_BoardRows + i_Row - m_Sequence + 1 : 0;
        }

        if (!m_Variant.equals(Variant.CIRCULAR))
        {
            colRightDelimiter = i_Col + m_Sequence - 1 > m_BoardCols - 1 ? m_BoardCols - 1 : i_Col + m_Sequence - 1;
            rowLength = colRightDelimiter - colStartPosition.x + 1;

            rowBottomDelimiter = i_Row + m_Sequence - 1 > m_BoardRows - 1 ? m_BoardRows - 1 : i_Row + m_Sequence - 1;
            colLength = rowBottomDelimiter - rowStartPosition.y + 1;
        }

        checkSequence(rowStartPosition, colLength, 0, 1);
        checkSequence(colStartPosition, rowLength, 1, 0);
    }

    private void checkForDiagonalsSequence(int i_Row, int i_Col) {
        int leftToRightDiagonalLength = 1, rightToLeftDiagonalLength = 1;
        Point leftToRightDiagonalStart = new Point(i_Col, i_Row), rightToLeftDiagonalStart = new Point(i_Col, i_Row);

        for (int i = 1; i <= m_Sequence - 1; i++)
        {
            if (i_Row - i >= 0 && i_Col - i >= 0)
            {
                leftToRightDiagonalStart.x = i_Col - i;
                leftToRightDiagonalStart.y = i_Row - i;
                leftToRightDiagonalLength++;
            }
            if (i_Row + i < m_BoardRows && i_Col + i < getCols())
            {
                leftToRightDiagonalLength++;
            }
            if (i_Row - i >= 0 && i_Col + i < m_BoardCols)
            {
                rightToLeftDiagonalStart.x = i_Col + i;
                rightToLeftDiagonalStart.y = i_Row - i;
                rightToLeftDiagonalLength++;
            }
            if (i_Row + i < m_BoardRows && i_Col - i >=0)
            {
                rightToLeftDiagonalLength++;
            }
        }

        checkSequence(leftToRightDiagonalStart, leftToRightDiagonalLength, 1, 1);
        checkSequence(rightToLeftDiagonalStart, rightToLeftDiagonalLength, -1, 1);
    }

    private void checkSequence(Point i_StartPosition, int i_Length, int i_ColFactor, int i_RowFactor) {
        int[] playersSequenceCounter = new int[m_Players.size()];

        for (int i = 0; i < i_Length; i++) {
            for (int j = 0; j < m_Players.size(); j++) {
                playersSequenceCounter[j] = m_Board[(i_StartPosition.y + i * i_RowFactor) % m_BoardRows][(i_StartPosition.x + i * i_ColFactor) % m_BoardCols] == j ? playersSequenceCounter[j] + 1 : 0;
                if (playersSequenceCounter[j] == m_Sequence) {
                    m_Players.get(j).setIsWinner(true);
                }
            }
        }
    }


    private void updateGameStatus(int i_ColDelimiter) {
        /*
            if the i_ColDelimiter is equal to the number of board cols, it means that a player retired, so
            we check all the board columns.
            else, we check the specific column that is the i_ColDelimiter.
         */
        long numberOfWinners;
        int startCol = i_ColDelimiter == m_BoardCols ? 0 : i_ColDelimiter;
        int endCol = i_ColDelimiter == m_BoardCols ? m_BoardCols : i_ColDelimiter + 1;

        for (int i = startCol; i < endCol; i++)
        {
            checkForWinning(i);
        }

        numberOfWinners = m_Players.stream().filter(i -> i.getIsWinner()).count();

        if (numberOfWinners > 0) {
            if (numberOfWinners > 1) {
                m_Status = GameStatus.TIE;
            }
            else {
                m_Status = GameStatus.WIN;
            }
        }
        else if (!m_Variant.equals(Variant.POPOUT) && checkIsBoardFull()) {
            m_Status = GameStatus.TIE;
        }

        if (m_Status != GameStatus.RUNNING) {
            m_UpdateStatusDelegate.action();
        }
    }

    private boolean checkIsBoardFull() {
        boolean isBoardFull = true;

        for (int i = 0; i < getCols(); i ++)
        {
            if (m_Board[0][i] == EMPTY)
            {
                isBoardFull = false;
                break;
            }
        }

        return isBoardFull;
    }

    public int getRowIndexToInsert(int i_ColToInsert) {
        int rowToInsert = UNDEFINED;

        for (int i = m_BoardRows - 1; i >= 0; i --) {
            if (m_Board[i][i_ColToInsert] == EMPTY) {
                rowToInsert = i;
                break;
            }
        }

        if (rowToInsert == UNDEFINED && m_ErrorOccurDelegate != null)
        {
            m_ErrorOccurDelegate.accept("Invalid move. The column is full.");
        }
        return rowToInsert;
    }

    public void playTurnByComputer() {
        List<List<Integer>> possibleMoves = new ArrayList<>();
        List<Integer> colsToPopout = getValidColumnsToPlay(m_BoardRows - 1, m_CurrentTurn);
        List<Integer> colsToInsert = getValidColumnsToPlay(0, EMPTY);
        int randomMoveTypeIndex;
        MoveType moveType;

        if (colsToInsert.size() > 0) {
            possibleMoves.add(colsToInsert);
        }

        if (colsToPopout.size() > 0 && m_Variant.equals(Variant.POPOUT)) {
            possibleMoves.add(colsToPopout);
        }

        randomMoveTypeIndex = new Random().nextInt(possibleMoves.size());
        moveType = colsToInsert.size() == 0 ? MoveType.POPOUT_DISC : (colsToPopout.size() == 0 ? MoveType.INSERT_DISC : MoveType.values()[randomMoveTypeIndex]);
        playRandomMoveByComputer(possibleMoves.get(randomMoveTypeIndex),
                moveType);
    }

    private void playRandomMoveByComputer(List<Integer> i_PossibleColsToPlay, MoveType i_MoveType) {
        int colToPlay = i_PossibleColsToPlay.get(new Random().nextInt(i_PossibleColsToPlay.size())) ;
        int rowToPlay = i_MoveType == MoveType.POPOUT_DISC ?
                m_BoardRows - 1 : getRowIndexToInsert(colToPlay);

        executeMove(new Turn(m_Players.get(m_CurrentTurn), i_MoveType, colToPlay, rowToPlay));
    }

    private List<Integer> getValidColumnsToPlay(int i_RowToCheck, int i_CellValueToCheck) {
        List<Integer> colsToPlay = new ArrayList<>();

        for (int i = 0; i < m_BoardCols; i++) {
            if (m_Board[i_RowToCheck][i] == i_CellValueToCheck) {
                colsToPlay.add(i);
            }
        }

        return colsToPlay;
    }

    public void executeMove(Turn i_newTurn) {
        addTurn(i_newTurn);
        if (m_IncrementPlayerTurnsCounter != null) {
            m_IncrementPlayerTurnsCounter.action();
        }

        int chosenColumn = i_newTurn.getColumn(), chosenRow = i_newTurn.getRow();
        MoveType moveType = i_newTurn.getMoveType();
        byte cellValueBeforeChange = m_Board[chosenRow][chosenColumn];
        m_Board[chosenRow][chosenColumn] = moveType.equals(MoveType.INSERT_DISC) ?
                (byte) m_CurrentTurn : EMPTY;

        addCellChange(new CellChange(new Point(chosenRow, chosenColumn), cellValueBeforeChange, m_Board[chosenRow][chosenColumn]));

        if (moveType.equals(MoveType.POPOUT_DISC)) {
            minimizeColAfterRemoveDisc(chosenColumn);
        }

        if (m_ShowBoardDelegate != null)
        {
            m_ShowBoardDelegate.action(chosenColumn);
        }

        updateGameStatus(chosenColumn);
        if (m_Status == GameStatus.RUNNING && m_ChangeTurnDelegate != null)
        {
            m_ChangeTurnDelegate.action();
        }

        if (m_GameOverDelegate != null && m_Status != GameStatus.RUNNING)
        {
            m_GameOverDelegate.action();
        }
    }

    public void addTurn(Turn i_newTurn) {
        m_TurnsHistory.add(i_newTurn);
        if (m_TurnAddedDelegate != null)
        {
            m_TurnAddedDelegate.accept(i_newTurn);
        }
    }

    public void restartGame() {
        m_Players.stream().forEach(Player::resetTurnsCounter);
        m_Players.stream().forEach(i -> i.setIsRetire(false));
        m_Players.stream().forEach(i -> i.setIsWinner(false));
        initEngine(m_BoardRows, m_BoardCols, m_Sequence, m_Variant, m_Players);
        setStartTime();
    }

    public void setStartTime() {
        m_StartTime = Instant.now();
    }

    public Duration getGameDuration() {
        return Duration.between(m_StartTime ,Instant.now());
    }

    public String getGameDurationAsString() {
        Duration gameDuration = getGameDuration();
        return String.format("%02d:%02d \n", gameDuration.toMinutes() % 60, gameDuration.getSeconds() % 60);
    }

    public boolean isLegalPopoutMove(int i_ColToPopout) {
        boolean isLegalPopoutMove = m_Board[m_BoardRows - 1][i_ColToPopout] == m_CurrentTurn;

        if (!isLegalPopoutMove && m_ErrorOccurDelegate != null)
        {
            m_ErrorOccurDelegate.accept("Invalid move. The column is empty or you try to popout" +
                    " disc that is not yours");
        }

        return isLegalPopoutMove;
    }

    public void retireGame() {
        addTurn(new Turn(m_Players.get(m_CurrentTurn), MoveType.RETIRE));
        if (m_PlayerRetireDelegate != null)
        {
            m_PlayerRetireDelegate.action();
        }
        removeDiscsOfRetiredPlayer();
        if (m_ShowBoardDelegate != null) {
            m_ShowBoardDelegate.action(m_BoardCols);
        }

        reorderBoardAfterRetirement();
        if (m_ShowBoardDelegate != null)
        {
            m_ShowBoardDelegate.action(m_BoardCols);
        }
        m_Players.get(m_CurrentTurn).setIsRetire(true);
        updateGameStatusAfterRetirement();
        if (m_Status == GameStatus.RUNNING && m_ChangeTurnDelegate != null)
        {
            m_ChangeTurnDelegate.action();
        }
        if (m_Status != GameStatus.RUNNING && m_GameOverDelegate != null)
        {
            m_GameOverDelegate.action();
        }
    }

    private void removeDiscsOfRetiredPlayer() {
        for (int i = 0; i < m_BoardRows; i++) {
            for (int j = 0; j < m_BoardCols; j++) {
                if (m_Board[i][j] == m_CurrentTurn)
                {
                    m_Board[i][j] = EMPTY;
                    addCellChange(new CellChange(new Point(i, j), (byte) m_CurrentTurn, (byte)EMPTY));
                }
            }
        }
    }

    private void reorderBoardAfterRetirement() {
        for (int i = 0; i < m_BoardCols; i++) {
            minimizeColAfterRemoveDisc(i);
        }
    }

    private void updateGameStatusAfterRetirement() {
        if (m_Players.stream().filter(i -> !i.getIsRetire()).count() < 2)
        {
            m_Status = GameStatus.WIN;
            m_Players.stream().filter(i -> !i.getIsRetire()).forEach(i -> i.setIsWinner(true));
            if (m_UpdateStatusDelegate != null)
            {
                m_UpdateStatusDelegate.action();
            }
        }
        else
        {
            updateGameStatus(m_BoardCols);
        }
    }

    private void minimizeColAfterRemoveDisc(int i_ColToRemove)
    {
        int newRowLocationForDisc;
        byte numberOfPlayer;

        for (int i = m_BoardRows - 1; i >= 0; i--)
        {
            numberOfPlayer = m_Board[i][i_ColToRemove];
            newRowLocationForDisc = i;
            while (newRowLocationForDisc + 1 < m_BoardRows &&
                    m_Board[newRowLocationForDisc + 1][i_ColToRemove] == EMPTY
                    && m_Board[i][i_ColToRemove] != EMPTY)
            {
                newRowLocationForDisc++;
            }

            m_Board[i][i_ColToRemove] = EMPTY;
            addCellChange(new CellChange(new Point(i, i_ColToRemove), numberOfPlayer, (byte) EMPTY));
            m_Board[newRowLocationForDisc][i_ColToRemove] = numberOfPlayer;
            addCellChange(new CellChange(new Point(newRowLocationForDisc, i_ColToRemove), (byte) EMPTY, numberOfPlayer));
        }
    }

    private void addCellChange(CellChange i_CellChange) {
        if (m_TurnsReplay.size() < m_TurnsHistory.size())
        {
            m_TurnsReplay.add(new ArrayList<>());
        }

        m_TurnsReplay.get(m_TurnsReplay.size() - 1).add(i_CellChange);
    }

    public void incrementPlayerTurnsCounter() {
        Turn playedTurn = m_TurnsHistory.get(m_TurnsHistory.size() - 1);
        playedTurn.getPlayer().incrementTurnsCounter();
    }

    private boolean canCurrentPlayerPopoutDisc() {
        boolean canPopout = false;

        if (m_Variant.equals(Variant.POPOUT))
        {
            for (int i = 0; i < m_BoardCols; i++) {
                if (m_Board[m_BoardRows - 1][i] == m_CurrentTurn) {
                    canPopout = true;
                }
            }
        }

        return canPopout;
    }
}