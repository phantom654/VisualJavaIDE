package sample;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.BufferedWriter;

public class DebuggerController {

    @FXML
    TextArea taDOutput,taDError;

    public TextArea getTaDOutput() {
        return taDOutput;
    }

    public void setTaDOutput(TextArea taDOutput) {
        this.taDOutput = taDOutput;
    }

    public TextArea getTaDError() {
        return taDError;
    }

    public void setTaDError(TextArea taDError) {
        this.taDError = taDError;
    }

    public TextField getTfDBreak() {
        return tfDBreak;
    }

    public void setTfDBreak(TextField tfDBreak) {
        this.tfDBreak = tfDBreak;
    }

    public TextField getTfDEnable() {
        return tfDEnable;
    }

    public void setTfDEnable(TextField tfDEnable) {
        this.tfDEnable = tfDEnable;
    }

    public TextField getTfDDisable() {
        return tfDDisable;
    }

    public void setTfDDisable(TextField tfDDisable) {
        this.tfDDisable = tfDDisable;
    }

    public TextField getTfDPrint() {
        return tfDPrint;
    }

    public void setTfDPrint(TextField tfDPrint) {
        this.tfDPrint = tfDPrint;
    }

    public Button getBtnDBreak() {
        return btnDBreak;
    }

    public void setBtnDBreak(Button btnDBreak) {
        this.btnDBreak = btnDBreak;
    }

    public Button getBtnDEnable() {
        return btnDEnable;
    }

    public void setBtnDEnable(Button btnDEnable) {
        this.btnDEnable = btnDEnable;
    }

    public Button getBtnDDisable() {
        return btnDDisable;
    }

    public void setBtnDDisable(Button btnDDisable) {
        this.btnDDisable = btnDDisable;
    }

    public Button getBtnDPrint() {
        return btnDPrint;
    }

    public void setBtnDPrint(Button btnDPrint) {
        this.btnDPrint = btnDPrint;
    }

    public Button getBtnDRun() {
        return btnDRun;
    }

    public void setBtnDRun(Button btnDRun) {
        this.btnDRun = btnDRun;
    }

    public Button getBtnDNext() {
        return btnDNext;
    }

    public void setBtnDNext(Button btnDNext) {
        this.btnDNext = btnDNext;
    }

    public Button getBtnDStep() {
        return btnDStep;
    }

    public void setBtnDStep(Button btnDStep) {
        this.btnDStep = btnDStep;
    }

    public Button getBtnDList() {
        return btnDList;
    }

    public void setBtnDList(Button btnDList) {
        this.btnDList = btnDList;
    }

    public Button getBtnDContinue() {
        return btnDContinue;
    }

    public TextField getTfDCustomCommand() {
        return tfDCustomCommand;
    }

    public void setTfDCustomCommand(TextField tfDCustomCommand) {
        this.tfDCustomCommand = tfDCustomCommand;
    }

    public Button getBtnDCustomCommand() {
        return btnDCustomCommand;
    }

    public void setBtnDCustomCommand(Button btnDCustomCommand) {
        this.btnDCustomCommand = btnDCustomCommand;
    }

    public void setBtnDContinue(Button btnDContinue) {
        this.btnDContinue = btnDContinue;
    }

    public Button getBtnDQuit() {
        return btnDQuit;
    }

    public void setBtnDQuit(Button btnDQuit) {
        this.btnDQuit = btnDQuit;
    }

    @FXML
    TextField tfDBreak,tfDEnable,tfDDisable,tfDPrint,tfDCustomCommand;

    @FXML
    Button btnDBreak,btnDEnable,btnDDisable,btnDPrint,btnDRun,btnDNext,btnDStep,btnDList,btnDContinue,btnDQuit,btnDCustomCommand;

}
