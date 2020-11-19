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
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.Subscription;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Flow;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

interface Search
{
    boolean find();
    boolean findNext();
    boolean replaceAll();
}

class TextCodeArea {

    private static final String[] KEYWORDS = new String[] {
            "abstract", "assert", "boolean", "break", "byte",
            "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else",
            "enum", "extends", "final", "finally", "float",
            "for", "goto", "if", "implements", "import",
            "instanceof", "int", "interface", "long", "native",
            "new", "package", "private", "protected", "public",
            "return", "short", "static", "strictfp", "super",
            "switch", "synchronized", "this", "throw", "throws",
            "transient", "try", "void", "volatile", "while",
            "#include","using","namespace","typedef","long long","int",
            "#define","auto","<<",">>","stack","vector","queue","char","float",
            "double","short","union","return","true","false",
    };

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";
    private static final String ASSIGNMENT_PATTERN = "\\s+\\w+?\\s+=" + "|" + "\\s+\\w+\\[.*\\]?\\s+=";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                    + "|(?<PAREN>" + PAREN_PATTERN + ")"
                    + "|(?<BRACE>" + BRACE_PATTERN + ")"
                    + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                    + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
                    + "|(?<ASSIGNMENT>" + ASSIGNMENT_PATTERN + ")"
    );

    CodeArea codeArea;

    public  TextCodeArea(AnchorPane pane) {
        codeArea = new CodeArea();

        VirtualizedScrollPane sp = new VirtualizedScrollPane(codeArea);
        pane.getChildren().add(sp);
        AnchorPane.setLeftAnchor(sp, 0.0);
        AnchorPane.setRightAnchor(sp, 0.0);
        AnchorPane.setBottomAnchor(sp, 0.0);
        AnchorPane.setTopAnchor(sp, 0.0);
        codeArea.prefWidthProperty().bind(pane.widthProperty());
        codeArea.prefHeightProperty().bind(pane.heightProperty());
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        Subscription cleanupWhenNoLongerNeedIt = codeArea.multiPlainChanges()
                .successionEnds(java.time.Duration.ofMillis(50))
                .subscribe(ignore -> codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText())));
        final Pattern whiteSpace = Pattern.compile( "^\\s+" );
//        codeArea.addEventHandler( KeyEvent.KEY_PRESSED, key -> {
//            if (key.getCode() == KeyCode.ENTER) {
//                int pos = codeArea.getCaretPosition();
//                int par = codeArea.getCurrentParagraph();
//                Matcher matcher = whiteSpace.matcher(codeArea.getParagraph(par-1).getSegments().get(0));
//                if (matcher.find()) Platform.runLater(() -> codeArea.insertText(pos, matcher.group()));
//            }
//        });
//        cleanupWhenNoLongerNeedIt.unsubscribe();    // to stop and clean up
    }

    private static StyleSpans<Collection<String>> computeHighlighting(String text) {
        int lastKwEnd = 0;
        Matcher matcher = PATTERN.matcher(text);
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

        while(matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORD") != null ? "keyword" :
                            matcher.group("PAREN") != null ? "paren" :
                                    matcher.group("BRACE") != null ? "brace" :
                                            matcher.group("BRACKET") != null ? "bracket" :
                                                    matcher.group("SEMICOLON") != null ? "semicolon" :
                                                            matcher.group("STRING") != null ? "string" :
                                                                    matcher.group("COMMENT") != null ? "comment" :
                                                                            matcher.group("ASSIGNMENT") != null ? "assignment" :
                                                                                    null; /* never happens */ assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    public void setText(String s)
    {
        codeArea.deleteText(0,codeArea.getText().length());
        codeArea.insertText(0,s);
    }

}



class CodeFile implements Cloneable
{
    private String text;//last saved text
    private String fileType;//type of code file
    private String fileName;
    private String filePath;//directory of file in disk
    private Tab tab;
    private AnchorPane anchorPane;
    private TextCodeArea taEditor;
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

    public TextCodeArea getTaEditor() {
        return taEditor;
    }

    public void setTaEditor(TextCodeArea taEditor) {
        this.taEditor = taEditor;
    }

    public Boolean getSaved() {
        return isSaved;
    }

    public void setSaved(Boolean saved) {
        isSaved = saved;
    }
}

class TextEditor implements FileHandling,Edit,Appearence,Search
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

        TextCodeArea taEditor = currFile.getTaEditor();
        int caretPosition = taEditor.codeArea.getCaretPosition();
        int temp = caretPosition - 1;
        String text = taEditor.codeArea.getText();
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
//            System.out.println(s);
            taEditor.codeArea.insertText(caretPosition, s);
            taLogs.appendText("\nIdentation done, " + num + " spaces inserted");

        }
        if(prevKey=="SHIFT" && e.getCode().toString().equals("CLOSE_BRACKET"))
        {
            if(text.charAt(temp)=='\t')
            {
                currFile.getTaEditor().codeArea.deleteText(temp,temp+1);
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


        codeFile.setAnchorPane(new AnchorPane());
        codeFile.setTab(new Tab(codeFile.getFileName()));
        codeFile.setTaEditor(new TextCodeArea(codeFile.getAnchorPane()));
        codeFile.getTaEditor().codeArea.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                identation(keyEvent);
            }
        });
        codeFile.getTaEditor().codeArea.setStyle("-fx-background-color:"+backgroundColor+";"+"-fx-text-fill:" + fontColor + "; "+"-fx-font-family:"+fontStyle+";"+"-fx-font-size: "+fontSize+";");


        codeFile.getTab().setOnCloseRequest(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                if(closeFile()){}
                else event.consume();//cancel the event
            }
        });

        AnchorPane.setTopAnchor(codeFile.getTaEditor().codeArea,0.0);
        AnchorPane.setLeftAnchor(codeFile.getTaEditor().codeArea,0.0);
        AnchorPane.setRightAnchor(codeFile.getTaEditor().codeArea,0.0);
        AnchorPane.setBottomAnchor(codeFile.getTaEditor().codeArea,0.0);

        codeFile.getAnchorPane().getChildren().add(codeFile.getTaEditor().codeArea);
        codeFile.getTab().setContent(codeFile.getAnchorPane());
        tabPane.getTabs().add(codeFile.getTab());
        filesArray.add(codeFile);//add newly created file to the filesArray
        //codeFile.getTaEditor().codeArea.setFocusTraversable();

        tabPane.getSelectionModel().select(codeFile.getTab());

        taLogs.appendText("\nCreated new File at : "+codeFile.getFilePath());
        codeFile.getTaEditor().codeArea.setFocusTraversable(true);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                codeFile.getTaEditor().codeArea.requestFocus();
            }
        });
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

        codeFile.setAnchorPane(new AnchorPane());
        codeFile.setTab(new Tab(codeFile.getFileName()));
        codeFile.setTaEditor(new TextCodeArea(codeFile.getAnchorPane()));
        codeFile.getTaEditor().codeArea.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                identation(keyEvent);
            }
        });
        codeFile.getTaEditor().codeArea.setStyle("-fx-background-color:"+backgroundColor+";"+"-fx-text-fill:" + fontColor + "; "+"-fx-font-family:"+fontStyle+";"+"-fx-font-size: "+fontSize+";");


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

        AnchorPane.setTopAnchor(codeFile.getTaEditor().codeArea,0.0);//set constraints
        AnchorPane.setLeftAnchor(codeFile.getTaEditor().codeArea,0.0);
        AnchorPane.setRightAnchor(codeFile.getTaEditor().codeArea,0.0);
        AnchorPane.setBottomAnchor(codeFile.getTaEditor().codeArea,0.0);

        codeFile.getAnchorPane().getChildren().add(codeFile.getTaEditor().codeArea);
        codeFile.getTab().setContent(codeFile.getAnchorPane());
        tabPane.getTabs().add(codeFile.getTab());
        filesArray.add(codeFile);//add newly created codefile to the filesArray
        codeFile.getTaEditor().codeArea.requestFocus();

        tabPane.getSelectionModel().select(codeFile.getTab());

        taLogs.appendText("\nOpened new File at : "+codeFile.getFilePath());
        codeFile.getTaEditor().codeArea.setFocusTraversable(true);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                codeFile.getTaEditor().codeArea.requestFocus();
            }
        });
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

        currFile.setText(currFile.getTaEditor().codeArea.getText());

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
        String text2 = currFile.getTaEditor().codeArea.getText();

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
        currFile.getTaEditor().codeArea.undo();

        return true;
    }

    @Override
    @FXML
    public Boolean redo() {
        int ind = tabPane.getSelectionModel().getSelectedIndex();
        CodeFile currFile = filesArray.get(ind);
        currFile.getTaEditor().codeArea.redo();
        return true;
    }

    @Override
    @FXML
    public Boolean cut() {
        int ind = tabPane.getSelectionModel().getSelectedIndex();
        CodeFile currFile = filesArray.get(ind);
        currFile.getTaEditor().codeArea.cut();
        return true;
    }

    @Override
    @FXML
    public Boolean copy() {
        int ind = tabPane.getSelectionModel().getSelectedIndex();
        CodeFile currFile = filesArray.get(ind);
        currFile.getTaEditor().codeArea.copy();
        return true;
    }

    @Override
    @FXML
    public Boolean paste() {
        int ind = tabPane.getSelectionModel().getSelectedIndex();
        CodeFile currFile = filesArray.get(ind);
        currFile.getTaEditor().codeArea.paste();
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
                //filesArray.get(i).getTaEditor().setFont(Font.font(fontStyle,fontSize));
                filesArray.get(i).getTaEditor().codeArea.setStyle("-fx-background-color:"+backgroundColor+";"+"-fx-text-fill:" + fontColor + "; "+"-fx-font-family:"+fontStyle+";"+"-fx-font-size: "+fontSize+";");
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

//                filesArray.get(i).getTaEditor().codeArea.setStyle("-fx-control-inner-background:"+backgroundColor+";"+"-fx-text-fill:" + fontColor + "; ");
                filesArray.get(i).getTaEditor().codeArea.setStyle("-fx-background-color:"+backgroundColor+";"+"-fx-text-fill:" + fontColor + "; "+"-fx-font-family:"+fontStyle+";"+"-fx-font-size: "+fontSize+";");
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
                //filesArray.get(i).getTaEditor().codeArea.setStyle("-fx-background-color:"+backgroundColor+";"+"-fx-text-fill: ;:" + fontColor + "; ");
                filesArray.get(i).getTaEditor().codeArea.setStyle("-fx-background-color:"+backgroundColor+";"+"-fx-text-fill:" + fontColor + "; "+"-fx-font-family:"+fontStyle+";"+"-fx-font-size: "+fontSize+";");
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

    ArrayList <Integer> indices;int currInd;
    @Override
    public boolean find() {

        indices = new ArrayList<>();

        int ind = tabPane.getSelectionModel().getSelectedIndex();
        CodeFile currFile = filesArray.get(ind);

         String text = currFile.getTaEditor().codeArea.getText();
        ///////
         String pattern=taFind.getText();

                //Z-Funtion :
                //` is used as the delimiter
                String s=pattern+"`"+text;

                int n=s.length();
                int z[]= new int[n];

                for(int i=1,l=0,r=0;i<n;i++)
                {
                    if (i <= r)
                        z[i] = Math.min(r - i + 1, z[i - l]);
                    while (i + z[i] < n && s.charAt(z[i]) == s.charAt(i + z[i]))
                        ++z[i];
                    if (i + z[i] - 1 > r)
                        l = i; r = i + z[i] - 1;
                }

                for(int i=pattern.length();i<s.length();i++)
                {
                    if(z[i]==pattern.length())indices.add(i-pattern.length()-1);
                }

                currInd=0;

        if(currInd>=indices.size())currInd=0;

                if(indices.size() >currInd) {
                    currFile.getTaEditor().codeArea.selectRange(indices.get(currInd), indices.get(currInd) + pattern.length());

                    currFile.getTaEditor().codeArea.requestFocus();
                }
        return true;
    }

    @FXML
    HBox hboxSearch;
    @FXML
    Button btnCloseSearch;
    @FXML
    TextArea taFind;
    @FXML
    TextArea taReplace;
    @FXML
    Button btnFind;
    @FXML
    Button btnFindNext;
    @FXML
    Button btnReplaceAll;
    @FXML
    void Search()
    {
        hboxSearch.setPrefHeight(35);
        btnCloseSearch.setPrefHeight(30);
        taFind.setPrefHeight(30);
        taReplace.setPrefHeight(30);
        btnFind.setPrefHeight(30);
        btnFindNext.setPrefHeight(30);
        btnReplaceAll.setPrefHeight(30);

        hboxSearch.setMinHeight(35);
        btnCloseSearch.setMinHeight(30);
        taFind.setMinHeight(30);
        taReplace.setMinHeight(30);
        btnFind.setMinHeight(30);
        btnFindNext.setMinHeight(30);
        btnReplaceAll.setMinHeight(30);

        hboxSearch.setMaxHeight(35);
        btnCloseSearch.setMaxHeight(30);
        taFind.setMaxHeight(30);
        taReplace.setMaxHeight(30);
        btnFind.setMaxHeight(30);
        btnFindNext.setMaxHeight(30);
        btnReplaceAll.setMaxHeight(30);


    }
    @FXML
    void closeSearch()
    {
        hboxSearch.setPrefHeight(0);
        btnCloseSearch.setPrefHeight(0);
        taFind.setPrefHeight(0);
        taReplace.setPrefHeight(0);
        btnFind.setPrefHeight(0);
        btnFindNext.setPrefHeight(0);
        btnReplaceAll.setPrefHeight(0);

        hboxSearch.setMinHeight(0);
        btnCloseSearch.setMinHeight(0);
        taFind.setMinHeight(0);
        taReplace.setMinHeight(0);
        btnFind.setMinHeight(0);
        btnFindNext.setMinHeight(0);
        btnReplaceAll.setMinHeight(0);

        hboxSearch.setMaxHeight(0);
        btnCloseSearch.setMaxHeight(0);
        taFind.setMaxHeight(0);
        taReplace.setMaxHeight(0);
        btnFind.setMaxHeight(0);
        btnFindNext.setMaxHeight(0);
        btnReplaceAll.setMaxHeight(0);
    }

    @Override
    public boolean findNext() {

        int ind = tabPane.getSelectionModel().getSelectedIndex();
        CodeFile currFile = filesArray.get(ind);

        String pattern=taFind.getText();

        currInd++;
        if(currInd>=indices.size())currInd=0;

        if(indices.size() >currInd) {
            currFile.getTaEditor().codeArea.selectRange(indices.get(currInd), indices.get(currInd) + pattern.length());
            currFile.getTaEditor().codeArea.requestFocus();
        }

        return true;
    }

    @Override
    public boolean replaceAll() {

        if(find()==false)return false;

        int ind = tabPane.getSelectionModel().getSelectedIndex();
        CodeFile currFile = filesArray.get(ind);

        String pattern=taFind.getText();

        String text = currFile.getTaEditor().codeArea.getText();
        int more=0;
        for(int i=0;i<indices.size();i++)
        {
            text=text.substring(0,indices.get(i)+more)+taReplace.getText()+text.substring(indices.get(i)+pattern.length()+more);
            more+=taReplace.getText().length()-taFind.getText().length();
        }

        currFile.getTaEditor().setText(text);

        indices.clear();


        return true;
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

        codeFile.setAnchorPane(new AnchorPane());
        codeFile.setTab(new Tab(codeFile.getFileName()));
        codeFile.setTaEditor(new TextCodeArea(codeFile.getAnchorPane()));
        codeFile.getTaEditor().codeArea.setStyle("-fx-background-color:"+backgroundColor+";"+"-fx-text-fill:" + fontColor + "; "+"-fx-font-family:"+fontStyle+";"+"-fx-font-size: "+fontSize+";");

        codeFile.getTaEditor().codeArea.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                identation(keyEvent);
            }
        });


        codeFile.getTab().setOnCloseRequest(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                if(closeFile()){}
                else event.consume();//cancel the event
            }
        });

        AnchorPane.setTopAnchor(codeFile.getTaEditor().codeArea,0.0);
        AnchorPane.setLeftAnchor(codeFile.getTaEditor().codeArea,0.0);
        AnchorPane.setRightAnchor(codeFile.getTaEditor().codeArea,0.0);
        AnchorPane.setBottomAnchor(codeFile.getTaEditor().codeArea,0.0);

        codeFile.getAnchorPane().getChildren().add(codeFile.getTaEditor().codeArea);
        codeFile.getTab().setContent(codeFile.getAnchorPane());
        tabPane.getTabs().add(codeFile.getTab());
        filesArray.add(codeFile);//add newly created file to the filesArray
        codeFile.getTaEditor().codeArea.setFocusTraversable(true);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                codeFile.getTaEditor().codeArea.requestFocus();
            }
        });

        tabPane.getSelectionModel().select(codeFile.getTab());

       // taLogs.appendText("\nCreated new File at : "+codeFile.getFilePath());

        btnCloseSearch.fire();

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
            tabPaneLogs.getSelectionModel().select(2);
        }
        else
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(error));
            String readline;
            while((readline = br.readLine())!=null) taLogs.appendText("\n"+readline);
        }
        //taLogs.positionCaret(0);

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

