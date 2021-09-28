import gearth.extensions.ExtensionForm;
import gearth.extensions.ExtensionInfo;
import gearth.protocol.HPacket;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@ExtensionInfo(
        Title =  "Random Room Visitor",
        Description =  "Searches and visits random rooms throughout the hotel.",
        Version =  "1.1",
        Author =  "schweppes0x"
)

public class RoomVisitor extends ExtensionForm {
    private String habboUrl;
    private Timer visitingTimer;
    private List<Integer> rooms = new ArrayList<Integer>();
    private int toGenerate = 50;

    public ToggleButton startToggle;
    public Label totalRoomsLabel;
    public TextField toGenerateText;
    public CheckBox aotChk;
    public ListView<String> roomsListView;

    @Override
    protected void initExtension() {
        //Check hotel URL
        onConnect((host, i, s1, s2, hClient) -> {
            switch (host) {
                case "game-nl.habbo.com":
                    habboUrl = "https://www.habbo.nl/";
                    break;
                case "game-br.habbo.com":
                    habboUrl = "https://www.habbo.com.br/";
                    break;
                case "game-tr.habbo.com":
                    habboUrl = "https://www.habbo.com.tr/";
                    break;
                case "game-de.habbo.com":
                    habboUrl = "https://www.habbo.de/";
                    break;
                case "game-fr.habbo.com":
                    habboUrl = "https://www.habbo.fr/";
                    break;
                case "game-fi.habbo.com":
                    habboUrl = "https://www.habbo.fi/";
                    break;
                case "game-es.habbo.com":
                    habboUrl = "https://www.habbo.es/";
                    break;
                case "game-it.habbo.com":
                    habboUrl = "https://www.habboit/";
                    break;
                case "game-s2.habbo.com":
                    habboUrl = "https://sandbox.habbo.com/";
                    break;
                default:
                    habboUrl = "https://habbo.com/";
                    break;
            }});

    }

    @FXML
    private void startVisiting() {
        if(startToggle.isSelected()){

            visitingTimer = new Timer();
            generateRoom(toGenerate);
            visitingTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    System.out.println("running again");
                    System.out.println(rooms.size());
                    if(rooms.size() < 1){
                        generateRoom(toGenerate);
                        try {
                            sleep(toGenerate*40);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if(rooms.size() >= 1){
                        int room = rooms.remove(0);
                        if(sendToServer(new HPacket("{out:GetGuestRoom}{i:" + room + "}{i:0}{i:1}"))){
                            Platform.runLater(()->{
                                totalRoomsLabel.textProperty().set(Integer.parseInt(totalRoomsLabel.getText())-1 + "");
                                roomsListView.getItems().add("Room ID: "+ room);
                            });
                        }
                    }
                }
            },toGenerate*40,1500);


            Platform.runLater(()->{
                startToggle.textProperty().set("Turn OFF");
                startToggle.styleProperty().set("-fx-background-color:#ff5833");

            });
        }else {
            stopVisiting();

        }
    }

    private void sleep(int i) throws Exception {
        try{
            Thread.sleep(i);
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    protected void onHide() {
        stopVisiting();
    }

    private void stopVisiting(){
        if(visitingTimer == null)
            return;

        visitingTimer.cancel();
        visitingTimer = null;

        Platform.runLater(()->{
            startToggle.textProperty().set("Turn ON");
            startToggle.styleProperty().set("-fx-background-color:#ffad33");
        });
    }

    private void generateRoom(int n){
        List<Integer> randomIds = new ArrayList<>();
        List<Integer> finals;

        Random random = new Random();

        for(int i = 0; i < n; i++){
            randomIds.add(30000000 + random.nextInt(10000000));
        }
        System.out.println(randomIds.size());
        finals = randomIds.parallelStream()
                .distinct()
                .map(i -> {
                    try{
                        return IOUtils.toString(new URL(String.format("%Sapi/public/rooms/%d", habboUrl, i)));
                    } catch (Exception e){
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .map(JSONObject::new)
                .filter(jsonObject -> jsonObject.get("doorMode").equals("open"))
                .map(e -> e.getInt("id"))
                .collect(Collectors.toList());

        if(finals.size() > 0){

            rooms.addAll(finals);
            System.out.println("[!] - Finished searching");
            Platform.runLater(()->{
                totalRoomsLabel.textProperty().set(rooms.size()+"");

            });
        }else {
            System.out.println("[!] - No rooms found..");
        }
    }

    public void toggleAOT(javafx.event.ActionEvent actionEvent) {
        primaryStage.setAlwaysOnTop(aotChk.isSelected());
    }

    public void changeValueOfGenerate(KeyEvent keyEvent) {
        try{
            toGenerate = Integer.parseInt(toGenerateText.getText());
        }catch (Exception e){
            //NON-digit was entered
        }
    }
}
