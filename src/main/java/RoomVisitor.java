import gearth.extensions.ExtensionForm;
import gearth.extensions.ExtensionInfo;
import gearth.protocol.HPacket;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

@ExtensionInfo(
        Title =  "Random Room Visitor",
        Description =  "Searches and visits random rooms throughout the hotel.",
        Version =  "1.0",
        Author =  "schweppes0x"
)

public class RoomVisitor extends ExtensionForm {

    private String apiUrl;
    private ArrayList<Room> rooms = new ArrayList<Room>();
    private int totalRoomsFound = 0;

    public ListView roomsListView;
    public Label totalRoomsLabel;
    public CheckBox aotChk;
    public ToggleButton startToggle;

    @Override
    protected void initExtension() {
        //Check hotel URL
        onConnect((host, i, s1, s2, hClient) -> {
            switch (host) {
                case "game-nl.habbo.com":
                    apiUrl = "https://www.habbo.nl/api/public/rooms/";
                    break;
                case "game-br.habbo.com":
                    apiUrl = "https://www.habbo.com.br/api/public/rooms/";
                    break;
                case "game-tr.habbo.com":
                    apiUrl = "https://www.habbo.com.tr/api/public/rooms/";
                    break;
                case "game-de.habbo.com":
                    apiUrl = "https://www.habbo.de/api/public/rooms/";
                    break;
                case "game-fr.habbo.com":
                    apiUrl = "https://www.habbo.fr/api/public/rooms/";
                    break;
                case "game-fi.habbo.com":
                    apiUrl = "https://www.habbo.fi/api/public/rooms/";
                    break;
                case "game-es.habbo.com":
                    apiUrl = "https://www.habbo.es/api/public/rooms/";
                    break;
                case "game-it.habbo.com":
                    apiUrl = "https://www.habboit/api/public/rooms/";
                    break;
                case "game-s2.habbo.com":
                    apiUrl = "https://sandbox.habbo.com/api/public/rooms/";
                    break;
            }});
    }



    //Search for valid rooms through hotel
    public void GenerateRoomID() {
        while (startToggle.isSelected()) {
            int randomID = ThreadLocalRandom.current().nextInt(10000000, 40000000 + 1);
            try {
                JSONObject roomDataJSON = new JSONObject(IOUtils.toString(new URL(apiUrl + randomID).openStream(), StandardCharsets.UTF_8));
                if (roomDataJSON.get("doorMode").equals("open")) {
                    Room foundRoom = new Room((int) roomDataJSON.get("id"), roomDataJSON.get("name").toString());
                    if (!rooms.contains(foundRoom)) {
                        rooms.add(foundRoom);
                        //ROOM FOUND
                        Platform.runLater(() -> {
                            totalRoomsLabel.textProperty().setValue(++totalRoomsFound+"");
                            roomsListView.getItems().add(foundRoom.toString());
                        });
                    }
                }
            }catch (Exception e) {
                // NO ROOM FOUND
            }
        }
    }


    public void toggleVisiting(ActionEvent actionEvent) {
        if(startToggle.isSelected()){
            //Start searching & visiting rooms
            startToggle.textProperty().setValue("STOP");
            startToggle.styleProperty().setValue("-fx-background-color:#ba4f7d");
            new Thread(this::GenerateRoomID).start();
            new Thread(this::checkShouldVisit).start();
        }else {
            //Halt all behavior
            startToggle.textProperty().setValue("START");
            startToggle.styleProperty().setValue("-fx-background-color:#ffad33");

        }
    }

    //check if we should visit the room
    private void checkShouldVisit() {
        while(rooms.size() < 5){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        while (startToggle.isSelected() && rooms.size() > 0){
            visitNextRoom();
        }
    }

    //visit the 0th room in our rooms ArrayList and wait for 1.8 seconds
    private void visitNextRoom() {
        try{
            Thread.sleep(1800);
            sendToServer(new HPacket("{out:GetGuestRoom}{i:" + rooms.remove(0).getID() + "}{i:0}{i:1}"));
        }
        catch (Exception e){
            System.out.println(e);
        };Platform.runLater(()->{
            roomsListView.getItems().remove(0);
            totalRoomsLabel.textProperty().setValue(--totalRoomsFound+"");
        });
    }

    //AOT
    public void toggleAOT(ActionEvent actionEvent) {
        primaryStage.setAlwaysOnTop(aotChk.isSelected());
    }
}

