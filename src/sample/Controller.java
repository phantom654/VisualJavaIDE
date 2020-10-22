package sample;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

interface FileHandling
{
    FileChooser fileChooser = new FileChooser();//file chooser for new,open,save

    public Boolean newFile();
    public Boolean openFile();
    public Boolean save();
    public Boolean saveAs();
    public Boolean closeFile();
}

class CodeFile
{
    private String text;//last saved text
    private String fileType;//type of code file
    private String fileName;
    private String filePath;//directory of file in disk
    private Tab tab;
    private AnchorPane anchorPane;
    private TextArea taEditor;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Tab getTab() {
        return tab;
    }

    public void setTab(Tab tab) {
        this.tab = tab;
    }

    public AnchorPane getAnchorPane() {
        return anchorPane;
    }

    public void setAnchorPane(AnchorPane anchorPane) {
        this.anchorPane = anchorPane;
    }

    public TextArea getTaEditor() {
        return taEditor;
    }

    public void setTaEditor(TextArea taEditor) {
        this.taEditor = taEditor;
    }
}

class TextEditor implements FileHandling
{
    @FXML
    TabPane tabPane;//tab pane that stores multiple text windows
    @FXML
    TextArea taLogs;

    ArrayList <CodeFile> filesArray;


    @Override
    @FXML
    public Boolean newFile() {

        fileChooser.setTitle("Create New File");
        File newFile = fileChooser.showSaveDialog(null);//check window
        if(newFile==null)return false;//error creating file

        CodeFile codeFile = new CodeFile();//Make a new File
        codeFile.setFilePath(newFile.getPath());
        codeFile.setFileName(newFile.getName().substring(0, newFile.getName().lastIndexOf('.')));
        codeFile.setFileType(newFile.getName().substring(newFile.getName().lastIndexOf('.') + 1));
        codeFile.setText("");

        codeFile.setTab(new Tab(codeFile.getFileName()));
        codeFile.setTaEditor(new TextArea());
        codeFile.setAnchorPane(new AnchorPane());

        codeFile.getTab().setOnCloseRequest(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                if(closeFile()){}
                else event.consume();//cancel the event
            }
        });

        AnchorPane.setTopAnchor(codeFile.getTaEditor(),0.0);
        AnchorPane.setLeftAnchor(codeFile.getTaEditor(),0.0);
        AnchorPane.setRightAnchor(codeFile.getTaEditor(),0.0);
        AnchorPane.setBottomAnchor(codeFile.getTaEditor(),0.0);

        codeFile.getAnchorPane().getChildren().add(codeFile.getTaEditor());
        codeFile.getTab().setContent(codeFile.getAnchorPane());
        tabPane.getTabs().add(codeFile.getTab());
        filesArray.add(codeFile);//add newly created file to the filesArray

        tabPane.getSelectionModel().select(codeFile.getTab());

        taLogs.appendText("\nCreated new File at : "+codeFile.getFilePath());
        return true;

    }

    @Override
    @FXML
    public Boolean openFile() {

        fileChooser.setTitle("Open File");
        File newFile = fileChooser.showOpenDialog(null);//check window
        if(newFile==null)return false;//error opening file

        CodeFile codeFile = new CodeFile();//Make a new codeFile
        codeFile.setFilePath(newFile.getPath());
        codeFile.setFileName(newFile.getName().substring(0, newFile.getName().lastIndexOf('.')));
        codeFile.setFileType(newFile.getName().substring(newFile.getName().lastIndexOf('.') + 1));

        //Read the text from File
        String temp, text="";
        try (BufferedReader buffReader = new BufferedReader(new FileReader(newFile))) {
            while ((temp = buffReader.readLine()) != null) {
                text+=temp+"\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        codeFile.setTab(new Tab(codeFile.getFileName()));
        codeFile.setTaEditor(new TextArea());
        codeFile.setAnchorPane(new AnchorPane());

        codeFile.getTab().setOnCloseRequest(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                if(closeFile()){}
                else event.consume();//cancel the event
            }
        });

        codeFile.setText(text);
        codeFile.getTaEditor().setText(text);//set the text in current file window

        AnchorPane.setTopAnchor(codeFile.getTaEditor(),0.0);//set constraints
        AnchorPane.setLeftAnchor(codeFile.getTaEditor(),0.0);
        AnchorPane.setRightAnchor(codeFile.getTaEditor(),0.0);
        AnchorPane.setBottomAnchor(codeFile.getTaEditor(),0.0);

        codeFile.getAnchorPane().getChildren().add(codeFile.getTaEditor());
        codeFile.getTab().setContent(codeFile.getAnchorPane());
        tabPane.getTabs().add(codeFile.getTab());
        filesArray.add(codeFile);//add newly created codefile to the filesArray

        tabPane.getSelectionModel().select(codeFile.getTab());

        taLogs.appendText("\nOpened new File at : "+codeFile.getFilePath());
        return true;

    }

    @Override
    @FXML
    public Boolean save() {

        Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Save This File ?",
                ButtonType.YES,
                ButtonType.NO
        );

        alert.showAndWait();
        if(alert.getResult()== ButtonType.NO)return false;

        int ind = tabPane.getSelectionModel().getSelectedIndex();//current open tab

        CodeFile currFile = filesArray.get(ind);
        currFile.setText(currFile.getTaEditor().getText());

        //Write file on disk
        try {
            FileWriter fw = new FileWriter(currFile.getFilePath());
            fw.write(currFile.getText());
            fw.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            taLogs.appendText("\nError Saving File");
            return false;
        }

        taLogs.appendText("\nFile + "+currFile.getFilePath()+" Saved succefully on Disk");

        return true;
    }

    @Override
    @FXML
    public Boolean saveAs() {

        int ind = tabPane.getSelectionModel().getSelectedIndex();//current open tab
        String text = filesArray.get(ind).getText();

        //create a newFile ,put text of current file in it and save it
        if(newFile()==false)return false;
        ind = tabPane.getSelectionModel().getSelectedIndex();//current open tab
        filesArray.get(ind).setText(text);
        filesArray.get(ind).getTaEditor().setText(text);
        if(save()==false)return false;

        return true;
    }

    @Override
    @FXML
    public Boolean closeFile() {

        //find if there are any unsaved changes
        int ind = tabPane.getSelectionModel().getSelectedIndex();//current open tab
        CodeFile currFile = filesArray.get(ind);
        String text1 = currFile.getText();
        String text2 = currFile.getTaEditor().getText();

        Boolean hasChanges = false;
        if(text1.length() != text2.length())hasChanges=true;
        else
        {
            for(int i=0;i<text1.length();i++)
                if(text1.charAt(i)!=text2.charAt(i))
                {
                    hasChanges=true;
                    break;
                }
        }

        //if There are unsaved changes, display a alert
        if(hasChanges) {
            Alert alert = new Alert(
                    Alert.AlertType.CONFIRMATION,
                    "Unsaved Changes in "+currFile.getFileName()+" Exit Anyway ?",
                    ButtonType.YES,
                    ButtonType.NO
            );
            alert.setTitle("Unsaved Changes Confirmation");
            alert.showAndWait();
            if (alert.getResult() == ButtonType.NO) return false;//cancel the operation
        }
        filesArray.remove(ind);
        tabPane.getTabs().remove(ind);
        taLogs.appendText("\nSuccessfully closed");
        return true;
    }


}

public class Controller extends TextEditor implements Initializable{

    private Stage primaryStage;


    public void initialize(URL url, ResourceBundle resourceBundle) //initialise the directory and Arraylists
    {
        taLogs.appendText("\nInitialising !!!");
        filesArray = new ArrayList<CodeFile>();//Initialising the fileArray
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser
                .getExtensionFilters()
                .addAll(
                        //add file extentions here
                        new FileChooser.ExtensionFilter("C++", "*.cpp"),
                        new FileChooser.ExtensionFilter("All Files", "*.*"));



    }
    @FXML
    public Boolean exit() {

        for(int i=0;i<filesArray.size();i++)
        {
            tabPane.getSelectionModel().select(0);
            if(closeFile()==false)return false;
        }

        Platform.exit();
        return true;
    }

}
