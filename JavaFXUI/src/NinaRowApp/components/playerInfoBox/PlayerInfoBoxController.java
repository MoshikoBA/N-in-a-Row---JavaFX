package NinaRowApp.components.playerInfoBox;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.text.Text;
import player.Player;
import java.net.URL;
import java.util.ResourceBundle;

public class PlayerInfoBoxController implements Initializable {

    @FXML private SplitPane m_Box;
    @FXML private Text m_TextPlayer;
    @FXML private Label m_NameLabel;
    @FXML private Label m_IdLabel;
    @FXML private Label m_IsHumanLabel;
    @FXML private Label m_DiscColorLabel;
    @FXML private Label m_TurnsPlayedCounterLabel;
    @FXML private Label m_IsActiveLabel;

    private StringProperty m_NameProperty;
    private StringProperty m_IdProperty;
    private StringProperty m_IsHumanProperty;
    private StringProperty m_TurnsPlayedCounterProperty;
    private StringProperty m_IsActiveProperty;

    public PlayerInfoBoxController() {

        m_NameProperty = new SimpleStringProperty();
        m_IdProperty = new SimpleStringProperty();
        m_IsHumanProperty = new SimpleStringProperty();
        m_TurnsPlayedCounterProperty = new SimpleStringProperty();
        m_IsActiveProperty = new SimpleStringProperty();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        m_NameLabel.textProperty().bind(m_NameProperty);
        m_IdLabel.textProperty().bind(m_IdProperty);
        m_IsHumanLabel.textProperty().bind(m_IsHumanProperty);
        m_TurnsPlayedCounterLabel.textProperty().bind(m_TurnsPlayedCounterProperty);
        m_IsActiveLabel.textProperty().bind(m_IsActiveProperty);
    }

    public SplitPane getBox() {return m_Box;}

    public void bindToPlayer(Player i_Player, int i_Index, String i_discColor) {
        m_TextPlayer.setText("----- Player #" + String.valueOf(i_Index + 1) + " -----");
        m_DiscColorLabel.setText(i_discColor);
        m_NameProperty.bind(i_Player.getNameProperty());
        m_IdProperty.bind(i_Player.getIDProperty().asString());
        m_IsHumanProperty.bind(i_Player.getIsHumanProperty().asString());
        m_IsActiveProperty.bind(i_Player.getIsRetireProperty().not().asString());
        m_TurnsPlayedCounterProperty.bind(i_Player.getTurnsCounterProperty().asString());
    }

    public int getPlayerId() {
        return Integer.parseInt(m_IdLabel.getText());
    }

    public void unBindTurnsAndCounter() {
        m_IsActiveProperty.unbind();
        m_TurnsPlayedCounterProperty.unbind();
    }

    public void incrementTurnsCounter() {
        m_TurnsPlayedCounterProperty.setValue(String.valueOf(Integer.parseInt(m_TurnsPlayedCounterLabel.getText()) + 1 ));
    }

    public void decreaseTurnsCounter() {
        m_TurnsPlayedCounterProperty.setValue(String.valueOf(Integer.parseInt(m_TurnsPlayedCounterLabel.getText()) - 1 ));
    }

    public void setIsActive(boolean i_Value) {
        m_IsActiveProperty.setValue(String.valueOf(i_Value));

    }
}