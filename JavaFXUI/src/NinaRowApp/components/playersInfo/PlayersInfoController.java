package NinaRowApp.components.playersInfo;

import NinaRowApp.components.playerInfoBox.PlayerInfoBoxController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import player.Player;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlayersInfoController {

    @FXML private VBox m_PlayersInfoVBox;
    private List<PlayerInfoBoxController> m_PlayersInfoBoxControllers = new ArrayList<PlayerInfoBoxController>();

    public List<SplitPane> getSplits() {
        List<SplitPane> list = new ArrayList<>();

        for (Node node : m_PlayersInfoVBox.getChildren()) {
            list.add((SplitPane)node);
        }

        return list;
    }

    public void allocatePlayersInfoBoxes(List<Player> i_Players, Map<Color, String> i_DiscsColors) {
        for (int i= 0; i < i_Players.size(); i++) {
            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource("/NinaRowApp/components/playerInfoBox/playerInfoBox.fxml");
            fxmlLoader.setLocation(url);
            try
            {
                Node node = fxmlLoader.load(url.openStream());
                m_PlayersInfoVBox.getChildren().add(node);
                PlayerInfoBoxController currentController = fxmlLoader.getController();
                currentController.bindToPlayer(i_Players.get(i), i, (String) i_DiscsColors.values().toArray()[i]);
                m_PlayersInfoBoxControllers.add(currentController);

            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            m_PlayersInfoVBox.setPrefHeight(i_Players.size() * 245);

        }
    }

    public void reset() {
        m_PlayersInfoVBox.getChildren().remove(0, m_PlayersInfoVBox.getChildren().size());
    }
}
