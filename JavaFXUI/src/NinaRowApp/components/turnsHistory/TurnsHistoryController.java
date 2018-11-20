package NinaRowApp.components.turnsHistory;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import turn.Turn;

public class TurnsHistoryController {

    @FXML
    private VBox m_TurnsVBox;

    public void addNewTurn(Turn i_NewTurn) {
        if (Platform.isFxApplicationThread())
        {
            Label newTurn = new Label((m_TurnsVBox.getChildren().size() + 1) + ") " + i_NewTurn.toString());
            newTurn.getStyleClass().add("turn");
            m_TurnsVBox.getChildren().add(newTurn);
        }
    }

    public void reset() {
        m_TurnsVBox.getChildren().remove(0, m_TurnsVBox.getChildren().size());
    }

}
