package sample;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Scanner;

interface FileHandling
{
    FileChooser fileChooser = new FileChooser();//file chooser for new,open,save

    public Boolean newFile();
    public Boolean openFile();
    public Boolean save();
    public Boolean saveAs();
    public Boolean closeFile();
}

interface Edit
{
    public Boolean undo();
    public Boolean redo();
    public Boolean cut();
    public Boolean copy();
    public Boolean paste();
}

interface Appearence
{
    String defaultFontColor="#FFFFFF";
    int defaultFontSize = 20;
    String defaultFontStyle = "SansSerif";
    String defaultBackgroundColor = "#343131";

    public String changeFont();
    public int changeFontColor();
    public int changeBackgroundColor();

}



class CodeFile implements Cloneable
{
    private String text;//last saved text
    private String fileType;//type of code file
    private String fileName;
    private String filePath;//directory of file in disk
    private Tab tab;
    private AnchorPane anchorPane;
    private TextArea taEditor;
    private Boolean isSaved;

    public Object clone()throws CloneNotSupportedException{
        return super.clone();
    }

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

    public Boolean getSaved() {
        return isSaved;
    }

    public void setSaved(Boolean saved) {
        isSaved = saved;
    }
}

class TextEditor implements FileHandling,Edit,Appearence
{
    @FXML
    TabPane tabPane;//tab pane that stores multiple text windows
    @FXML
    TextArea taLogs;

    ArrayList <CodeFile> filesArray;

    String backgroundColor;
    String fontColor;
    int fontSize;
    String fontStyle;

    String prevKey="";

    @FXML
    void identation(KeyEvent e) {
        //check key event
        int ind = tabPane.getSelectionModel().getSelectedIndex();
        CodeFile currFile = filesArray.get(ind);

        TextArea taEditor = currFile.getTaEditor();
        int caretPosition = taEditor.getCaretPosition();
        int temp = caretPosition - 1;
        String text = taEditor.getText();
        String s = "";
        int num = 0;
        //taLogs.appendText("\n"+e.getCode().toString());
        if (e.getCode().equals(KeyCode.ENTER)) {

            if (text.charAt(temp - 1) == '{') {
                num += 8;
                s += "\t";
            }
            while (temp >= 1 && text.charAt(temp - 1) != '\n') temp--;

            while (text.charAt(temp) == ' ' || text.charAt(temp) == '\t') {
                if (text.charAt(temp) == ' ') {
                    s += " ";
                    num++;
                }
                if (text.charAt(temp) == '\t') {
                    s += "\t";
                    num += 8;
                }

                temp++;
            }

            taEditor.insertText(caretPosition, s);
            taLogs.appendText("\nIdentation done, " + num + " spaces inserted");

        }
        if(prevKey=="SHIFT" && e.getCode().toString().equals("CLOSE_BRACKET"))
        {
            if(text.charAt(temp)=='\t')
            {
                currFile.getTaEditor().deleteText(temp,temp+1);
            }
        }

        prevKey=e.getCode().toString();

    }

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
        codeFile.setSaved(true);



        codeFile.setTab(new Tab(codeFile.getFileName()));
        codeFile.setTaEditor(new TextArea());
        codeFile.getTaEditor().setStyle("-fx-control-inner-background:"+backgroundColor+";"+"-fx-text-fill:" + fontColor + "; ");
        codeFile.getTaEditor().setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                identation(keyEvent);
            }
        });
        codeFile.getTaEditor().setFont(Font.font(fontStyle,fontSize));
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
        codeFile.getTaEditor().setStyle("-fx-control-inner-background:"+backgroundColor+";"+"-fx-text-fill:" + fontColor + "; ");
        codeFile.getTaEditor().setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                identation(keyEvent);
            }
        });
        codeFile.getTaEditor().setFont(Font.font(fontStyle,fontSize));
        codeFile.setAnchorPane(new AnchorPane());

        codeFile.getTab().setOnCloseRequest(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                if(closeFile()){}
                else event.consume();//cancel the event
            }
        });

        codeFile.setText(text);
        codeFile.setSaved(true);

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
        int ind = tabPane.getSelectionModel().getSelectedIndex();//current open tab
        CodeFile currFile = filesArray.get(ind);

        if(currFile.getSaved()==false)
        {
            fileChooser.setTitle("Create New File");
            File newFile = fileChooser.showSaveDialog(null);//check window
            if(newFile==null)return false;//error creating file

            currFile.setFilePath(newFile.getPath());
            currFile.setFileName(newFile.getName().substring(0, newFile.getName().lastIndexOf('.')));
            currFile.setFileType(newFile.getName().substring(newFile.getName().lastIndexOf('.') + 1));
            currFile.setSaved(true);
            currFile.getTab().setText(currFile.getFileName());

            taLogs.appendText("\nCreated New File : "+currFile.getFilePath());

            return true;
        }
        else {
            Alert alert = new Alert(
                    Alert.AlertType.CONFIRMATION,
                    "Save This File ?",
                    ButtonType.YES,
                    ButtonType.NO
            );

            alert.showAndWait();
            if (alert.getResult() == ButtonType.NO) return false;
        }

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


    @Override
    @FXML
    public Boolean undo() {

        int ind = tabPane.getSelectionModel().getSelectedIndex();
        CodeFile currFile = filesArray.get(ind);
        currFile.getTaEditor().undo();

        return true;
    }

    @Override
    @FXML
    public Boolean redo() {
        int ind = tabPane.getSelectionModel().getSelectedIndex();
        CodeFile currFile = filesArray.get(ind);
        currFile.getTaEditor().redo();
        return true;
    }

    @Override
    @FXML
    public Boolean cut() {
        int ind = tabPane.getSelectionModel().getSelectedIndex();
        CodeFile currFile = filesArray.get(ind);
        currFile.getTaEditor().cut();
        return true;
    }

    @Override
    @FXML
    public Boolean copy() {
        int ind = tabPane.getSelectionModel().getSelectedIndex();
        CodeFile currFile = filesArray.get(ind);
        currFile.getTaEditor().copy();
        return true;
    }

    @Override
    @FXML
    public Boolean paste() {
        int ind = tabPane.getSelectionModel().getSelectedIndex();
        CodeFile currFile = filesArray.get(ind);
        currFile.getTaEditor().paste();
        return true;
    }

    @Override
    public String changeFont() {

        Alert alert =new Alert(Alert.AlertType.CONFIRMATION);
        //alert.s
        alert.setTitle("Change Font");
        alert.setHeaderText("Choose Font Family and Font size");
        alert.setResizable(true);

        GridPane grid=new GridPane();
        Scene scene = new Scene(new HBox(80), 400, 300);
        HBox box = (HBox) scene.getRoot();
        box.setPadding(new Insets(5, 5, 5, 5));
        ObservableList<String> fonts = FXCollections.observableArrayList(javafx.scene.text.Font.getFamilies());
        ArrayList<Integer> m =new ArrayList<Integer>();
        for(int i=1;i<=30;i+=2){
            m.add(i);
        }
        ObservableList<Integer> sizes = FXCollections.observableArrayList(m);
        // Select font
        ChoiceBox<String> selectFont;
        selectFont = new ChoiceBox<>();
        selectFont.setValue("SansSerif");
        selectFont.setLayoutX(600);
        selectFont.setLayoutY(200);
        selectFont.setItems(fonts);
        ChoiceBox<Integer> selectFontsize;
        selectFontsize = new ChoiceBox<Integer>();
        selectFontsize.setValue(9);
        selectFontsize.setLayoutX(600);
        selectFontsize.setLayoutY(400);
        selectFontsize.setItems(sizes);
        Text text = new Text("New Font");

        selectFont.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> text.setFont(javafx.scene.text.Font.font(newValue, FontWeight.MEDIUM,24)));
        selectFontsize.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> text.setFont(javafx.scene.text.Font.font(selectFont.getValue(),FontWeight.MEDIUM,newValue)));

        text.setFont(Font.font(fontStyle,fontSize));
        box.getChildren().addAll(selectFont,selectFontsize, text);
        grid.getChildren().add(box);
        alert.getDialogPane().setContent(box);
        if(alert.showAndWait().get().getText().equals("OK")){
            fontSize = selectFontsize.getValue();
            fontStyle = selectFont.getValue();

            for(int i=0;i<filesArray.size();i++)
            {
                filesArray.get(i).getTaEditor().setFont(Font.font(fontStyle,fontSize));
            }

            taLogs.appendText("\nChanged Font to : "+fontStyle+" : "+fontSize);

        }


        return null;
    }

    @Override
    public int changeFontColor() {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Change Font Color");
        alert.setHeaderText("Select Color From Color Picker");
        GridPane grid = new GridPane();
        Scene scene = new Scene(new HBox(20), 400, 100);
        HBox box = (HBox) scene.getRoot();
        box.setPadding(new Insets(5, 5, 5, 5));

        final ColorPicker colorPicker = new ColorPicker();

        colorPicker.setValue(Color.valueOf(fontColor));

        final Text text = new Text("This is your new color!!");
        text.setFont(Font.font(fontStyle, fontSize));
        text.setFill(colorPicker.getValue());

        colorPicker.setOnAction(new EventHandler() {
            public void handle(Event t) {
                text.setFill(colorPicker.getValue());
            }
        });

        box.getChildren().addAll(colorPicker, text);
        grid.getChildren().add(box);
        alert.getDialogPane().setContent(box);
        if (alert.showAndWait().get().getText().equals("OK")) {
            fontColor = colorPicker.getValue().toString();
            fontColor = "#" + fontColor.substring(2, fontColor.length() - 2);
            //taLogs.setText(colorPicker.getValue());

            for(int i=0;i<filesArray.size();i++) {

                filesArray.get(i).getTaEditor().setStyle("-fx-control-inner-background:"+backgroundColor+";"+"-fx-text-fill:" + fontColor + "; ");
            }
            taLogs.appendText("\nChanged Font Color : "+fontColor);

        }
        return 0;
    }

    @Override
    @FXML
    public int changeBackgroundColor() {
        Alert alert =new Alert(Alert.AlertType.CONFIRMATION);
        //alert.s
        alert.setTitle("Change Background Color");
        alert.setHeaderText("Select Color From Color Picker");
        GridPane grid=new GridPane();
        Scene scene = new Scene(new HBox(20), 400, 100);
        HBox box = (HBox) scene.getRoot();
        box.setPadding(new Insets(5, 5, 5, 5));

        final ColorPicker colorPicker = new ColorPicker();

        colorPicker.setValue(Color.valueOf(backgroundColor));

        final javafx.scene.text.Text text = new Text("This is your new color!!");
        text.setFont(Font.font (fontStyle, fontSize));
        text.setFill(colorPicker.getValue());

        colorPicker.setOnAction(new EventHandler() {
            public void handle(Event t) {

                text.setFill(colorPicker.getValue());

            }
        });

        box.getChildren().addAll(colorPicker,text);
        grid.getChildren().add(box);
        alert.getDialogPane().setContent(box);
        if(alert.showAndWait().get().getText().equals("OK")){
            backgroundColor = colorPicker.getValue().toString();
            backgroundColor = "#"+backgroundColor.substring(2,backgroundColor.length()-2);
            // taEditor.setStyle("-fx-text-fill:"+taEditor+"; " +"-fx-control-inner-background:"+bgColor+";"); ;

            for(int i=0;i<filesArray.size();i++)
            {
                filesArray.get(i).getTaEditor().setStyle("-fx-control-inner-background:"+backgroundColor+";"+"-fx-text-fill:" + fontColor + "; ");
            }
        taLogs.appendText("\n"+"Changed Background Color to : "+backgroundColor);

        }
        return 0;
    }

    @FXML
    TabPane tabPaneLogs;

    @FXML
    void showHideLogs()
    {
        if(tabPaneLogs.getPrefHeight()!=0)tabPaneLogs.setPrefHeight(0);
        else tabPaneLogs.setPrefHeight(250);
    }


}

public class Controller extends TextEditor implements Initializable{

    Runtime run = Runtime.getRuntime();//runtime to compile and run commands


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

        backgroundColor = defaultBackgroundColor;
        fontColor = defaultFontColor;
        fontSize = defaultFontSize;
        fontStyle = defaultFontStyle;

        //fileChooser.setTitle("Create New File");
//        File newFile = fileChooser.showSaveDialog(null);//check window
//        if(newFile==null)return false;//error creating file

        CodeFile codeFile = new CodeFile();//Make a new File
        codeFile.setFileName("UNSAVED");
        codeFile.setText("");
        codeFile.setSaved(false);


        codeFile.setTab(new Tab(codeFile.getFileName()));
        codeFile.setTaEditor(new TextArea());
        codeFile.getTaEditor().setStyle("-fx-control-inner-background:"+backgroundColor+";"+"-fx-text-fill:" + fontColor + "; ");
        codeFile.getTaEditor().setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                identation(keyEvent);
            }
        });
        codeFile.getTaEditor().setFont(Font.font(fontStyle,fontSize));
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

       // taLogs.appendText("\nCreated new File at : "+codeFile.getFilePath());



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

    @FXML
    public Boolean compile() throws InterruptedException, IOException
    {
        if(save()==false)return false;//save file before compiling
        String command="";
        int ind = tabPane.getSelectionModel().getSelectedIndex();
        CodeFile currFile = filesArray.get(ind);
        taLogs.appendText("\nCompiling "+currFile.getFilePath());
        if(currFile.getFileType().equals("cpp"))//if c++ file
        {
            command="g++ -g "+currFile.getFilePath()+" -o"+currFile.getFileName();
        }

        Process prc = run.exec(command);
        prc.waitFor();//wait for process to execute

        InputStream error = prc.getErrorStream();

        if(error.available()==0) {
            taLogs.appendText("\nCompiled Without Errors");
            return true;
        }
        else
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(error));
            String readline;
            while((readline = br.readLine())!=null) taLogs.appendText("\n"+readline);
        }

        return false;
    }
    @FXML TextArea taInput, taOutput;
    @FXML
    public Boolean run()throws IOException, InterruptedException
    {
        if(compile()==false)return false;//compile before running
        int ind = tabPane.getSelectionModel().getSelectedIndex();
        CodeFile currFile = filesArray.get(ind);
        String command="";

        if(currFile.getFileType().equals("cpp"))
        {
            command = "./"+currFile.getFileName();
        }

        Process prc = run.exec(command);
        InputStream error = prc.getErrorStream();
        InputStream output = prc.getInputStream();

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(prc.getOutputStream()));
        writer.write(taInput.getText()+"\n\n\n\n\n");
        writer.flush();

        prc.waitFor();

        if(error.available()==0)
        {
            taLogs.appendText("\nRunned Sucessfully, Open OUTPUT Tab to check output");
            BufferedReader br = new BufferedReader(new InputStreamReader(output));
            String readline;taOutput.setText("");
            while((readline = br.readLine())!=null) taOutput.appendText(readline+"\n");
            taOutput.appendText("Program exited with exit code : "+prc.exitValue());
        }
        else
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(error));
            String readline;
            while((readline = br.readLine())!=null) taLogs.appendText("\n"+readline);
        }


        return true;
    }


    DebuggerController debuggerController;
    @FXML
    public Boolean debug()throws IOException,InterruptedException
    {
        if(compile()==false)return false;//compile before running

        int ind = tabPane.getSelectionModel().getSelectedIndex();
        CodeFile currFile = filesArray.get(ind);


            //Create a new window for Debugger
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("debugger.fxml"));
            Parent root = (Parent) fxmlLoader.load();
            Stage stage = new Stage();

            stage.setTitle("Debugger");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("DebuggerStyles.css").toExternalForm());
            stage.setScene(scene);
            stage.show();

            debuggerController = fxmlLoader.getController();
            new Debugger(currFile,debuggerController,stage);//start a new debugger session

        return true;
    }

    class Debugger implements Runnable
    {
        CodeFile currFile;
        DebuggerController debuggerController;
        Stage stage;
        Thread thread;

        public Debugger(CodeFile currFile,DebuggerController debuggerController,Stage stage)
        {
            //clone as multiple instances of debug can be called
            try
            {
                this.currFile = (CodeFile) currFile.clone();
            }
            catch (CloneNotSupportedException e){
                e.printStackTrace();
            }
            this.debuggerController = debuggerController;
            this.stage=stage;
            thread=new Thread(this,currFile.getFilePath());
            thread.start();

        }
        @Override
        public void run() {

            //fill the command
            String command="gdb ./"+currFile.getFileName();

            ProcessBuilder processBuilder=new ProcessBuilder("bash","-c",command);
            BufferedWriter out=null;//the output stream where we will give input to process
            Process process = null;
            try {
                process=processBuilder.start();
                InputStream inStream = process.getInputStream();
                OutputStream outStream = process.getOutputStream();

                out = new BufferedWriter(new OutputStreamWriter(outStream));

                //set the events
                BufferedWriter finalOut = out;

                debuggerController.getBtnDBreak().setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        String command="break "+debuggerController.getTfDBreak().getText()+"\n";
                        try {
                            finalOut.write(command, 0, command.length());
                            finalOut.flush();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
                debuggerController.getBtnDEnable().setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        String command="enable "+debuggerController.getTfDEnable().getText()+"\n";
                        try {
                            finalOut.write(command, 0, command.length());
                            finalOut.flush();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
                debuggerController.getBtnDDisable().setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        String command="disable "+debuggerController.getTfDDisable().getText()+"\n";
                        try {
                            finalOut.write(command, 0, command.length());
                            finalOut.flush();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
                debuggerController.getBtnDPrint().setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        String command="info "+debuggerController.getTfDPrint().getText()+"\n";
                        try {
                            finalOut.write(command, 0, command.length());
                            finalOut.flush();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                });

                debuggerController.getBtnDRun().setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        String command="run"+"\n";
                        try {
                            finalOut.write(command, 0, command.length());
                            finalOut.flush();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
                debuggerController.getBtnDNext().setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        String command="next"+"\n";
                        try {
                            finalOut.write(command, 0, command.length());
                            finalOut.flush();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
                debuggerController.getBtnDStep().setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        String command="step"+"\n";
                        try {
                            finalOut.write(command, 0, command.length());
                            finalOut.flush();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
                debuggerController.getBtnDList().setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        String command="list"+"\n";
                        try {
                            finalOut.write(command, 0, command.length());
                            finalOut.flush();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
                debuggerController.getBtnDContinue().setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        String command="continue"+"\n";
                        try {
                            finalOut.write(command, 0, command.length());
                            finalOut.flush();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                });

                debuggerController.getBtnDCustomCommand().setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        String command=debuggerController.getTfDCustomCommand().getText()+"\n";
                        try {
                            finalOut.write(command, 0, command.length());
                            finalOut.flush();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }

                    }
                });
               // processBuilder.redirectErrorStream(true);//merge the error and output stream of the process
                InputStream errorStream = process.getErrorStream();

                Thread errorThread = new Thread(){

                    public void run() {
                        try {
                            while (true) {

                                Thread.sleep(1);//for the interrupts
                                BufferedReader br = new BufferedReader(new InputStreamReader(errorStream));
                                String readline;
                                while ((readline = br.readLine()) != null)
                                    debuggerController.getTaDError().appendText("\n" + readline);
                            }

                        }
                        catch (IOException e) {
                        }
                    catch(InterruptedException e)
                    {

                    }
                    }};
                errorThread.start();

                Thread finalErrorThread = errorThread;
                stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent windowEvent) {
                        String command="quit"+"\n";
                        try {
                            finalOut.write(command, 0, command.length());
                            finalOut.flush();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                        thread.interrupt();//interrupt to stop them
                        finalErrorThread.interrupt();
                    }
                });

                debuggerController.getBtnDQuit().setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {

                        String command="quit"+"\n";
                        try {
                            finalOut.write(command, 0, command.length());
                            finalOut.flush();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                        thread.interrupt();
                        finalErrorThread.interrupt();
                        stage.close();

                    }
                });

                while (true) {

                    Thread.sleep(1);
                    BufferedReader br = new BufferedReader(new InputStreamReader(inStream));
                    String readline;
                    while((readline = br.readLine())!=null) debuggerController.getTaDOutput().appendText("\n"+readline);

                }

            }
            catch (IOException e)
            {
                e.printStackTrace();
            }catch (InterruptedException e) {
                //e.printStackTrace();
                taLogs.appendText("\nFinished debugging : "+currFile.getFilePath());
            }
            finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }



        }

    }


}
