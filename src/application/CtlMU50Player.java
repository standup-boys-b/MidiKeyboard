package application;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;

public class CtlMU50Player {
	@FXML private Button btnSeqInit;
	@FXML private Button btnSeqStart;
	@FXML private Button btnSeqPause;
	@FXML private Button btnSeqStop;
	@FXML private Button btnSeqClose;
	@FXML private Button btnSeqStartRec;
	@FXML private Button btnSeqStopRec;

	@FXML private Button btnReadMidFile;
	@FXML private Button btnWriteMidFile;
	@FXML private ComboBox<String> cmbMidiFiles;

	@FXML private Button btnOK;
	@FXML private Button btnSearchDevice;
	@FXML private Button btnOpenDevice;
	@FXML private Button btnCloseDevice;
	@FXML private Button btnLoad;
	@FXML private Button btnSave;
	@FXML private ComboBox<String> cmbFilename;
	@FXML private TextArea txtMsg;
	@FXML private TextArea txtComment;
	@FXML private TextArea txtReserve;
	@FXML private Label lblMsg;
	@FXML private Label lblUpperOctave;
	@FXML private Label lblLowerOctave;
	@FXML private Label lblDevInNull;
	@FXML private Label lblDevOutNull;
	@FXML private TextField txtSplitpoint;

	@FXML private Label lblSplitpoint;
	@FXML private ComboBox<String> cmbOutDevice;
	@FXML private ComboBox<String> cmbInDevice;
	@FXML private CheckBox chkSoundFont;
	
	@FXML private Label lblTest;
	@FXML private GridPane gridPane;
	@FXML private GridPane gpVoiceGroup;
	@FXML private GridPane gpBankList;
	@FXML private GridPane gpLSBList;
	@FXML private Scene s;
	
	private TextField[] txtVoice;
	private TextField[] txtLSB;
	private TextField[] txtShift;
	private TextField[] txtVolume;
	private Label[] lblVoice;
	private CheckBox[] chkOnOff;
	private Label[] lblNoteNo;
	
	private Label[] lblVoiceList;
	
	private boolean isFormLocked = false;
	private  String strUpperKeyChars;
	
	private MidiPlayer mp;
	private MapKeyNote mapK;
	private MapVoiceList mapV;
	private int tmpNoteNo;
	HashMap<String, String> mapPressedChar;
	HashMap<String, String> mapSplitpoint;
	LinkedHashMap<String, String> mapDrumNames;
	
	private JavaMidiSequence seq;
	private SysExManager sxm;
	private Label[] lblVoiceGroup;
	private Label[] lblLSBList;
	
	public boolean isUseSoundFont(){
		return this.chkSoundFont.isSelected();
	}
	
	private void initSplitpoint(){
		mapSplitpoint = new HashMap<String, String>();
		mapSplitpoint.put("0", "C ");
		mapSplitpoint.put("1", "C#");
		mapSplitpoint.put("2", "D ");
		mapSplitpoint.put("3", "D#");
		mapSplitpoint.put("4", "E ");
		mapSplitpoint.put("5", "F ");
		mapSplitpoint.put("6", "F#");
		mapSplitpoint.put("7", "G ");
		mapSplitpoint.put("8", "G#");
		mapSplitpoint.put("9", "A ");
		mapSplitpoint.put("10", "A#");
		mapSplitpoint.put("11", "B ");
	}
	
	@FXML
	public void onBtnOKClicked(ActionEvent e){
		String str = txtComment.getText();
		System.out.println(str);
//		sxm.sendExMsg(dev, str);
		mp.sendExMsg(str);
	}
	
	@FXML
	public void onKeyTyped(KeyEvent e){
		
	}
	
	public int getNoteShift(int chno){
		int nn;
		if(1 <= chno && chno <= 4){
			nn = Integer.parseInt(txtShift[chno].getText())
			           + 12*(Integer.parseInt(lblUpperOctave.getText())-2);
		} else{
			nn = Integer.parseInt(txtShift[chno].getText())
			           + 12*(Integer.parseInt(lblLowerOctave.getText())-2);
		}
		return nn;
	}
	public int getUpperOctPosition(){
		return Integer.parseInt(lblUpperOctave.getText());
	}
	public int getLowerOctPosition(){
		return Integer.parseInt(lblLowerOctave.getText());
	}
	public int getVolume(int chno){
		return Integer.parseInt(txtVolume[chno].getText());
	}
	public int getVoiceNo(int chno){
		return Integer.parseInt(txtVoice[chno].getText());
	}
	public boolean isChannelOn(int chno){
		return this.chkOnOff[chno].isSelected();
	}
	@FXML
	public void onKeyPressed(KeyEvent e){
		if (this.txtSplitpoint.isFocused()) return;

		String keyChar = e.getText().toLowerCase();
		lblTest.setText("<" + keyChar + e.getCode().toString());
		
		if (! mapPressedChar.containsKey(keyChar)) {
			mapPressedChar.put(keyChar, "");
			int noteNo = mapK.get(keyChar);
			if (e.isShiftDown()){
				noteNo += 12;
			} else if(e.isControlDown()){
				noteNo -= 12;
			}
			if(keyChar.getBytes().length == 0){return;}
			if (strUpperKeyChars.indexOf(keyChar) >= 0) {
				// upper keys
				for(int i=1; i<=4; i++){
					tmpNoteNo = noteNo + Integer.parseInt(txtShift[i].getText())
					           + 12*(Integer.parseInt(lblUpperOctave.getText())-2);
					if(i == 4){
						lblNoteNo[i].setText(mapDrumNames.get(String.valueOf(tmpNoteNo)));
					} else {
						lblNoteNo[i].setText(String.valueOf(tmpNoteNo));
					}
					if (0 < tmpNoteNo && tmpNoteNo < 129 && chkOnOff[i].isSelected()) {
						int vol = Integer.parseInt(txtVolume[i].getText());
						int voice = Integer.parseInt(txtVoice[i].getText());
						mp.playNote(tmpNoteNo, i, vol);
//						if (seq != null && seq.isRecording()) {
//							System.out.println("1-4 addnoteon");
//							seq.addNoteOnRealtime(i, tmpNoteNo, vol, voice);
//						}
					}
				}
			}else{
				// lower keys
				for(int i=5; i<=8; i++){
					tmpNoteNo = noteNo + Integer.parseInt(txtShift[i].getText())
					           + 12*(Integer.parseInt(lblLowerOctave.getText())-5);
					if(i == 8){
						lblNoteNo[i].setText(mapDrumNames.get(String.valueOf(tmpNoteNo)));
					} else {
						lblNoteNo[i].setText(String.valueOf(tmpNoteNo));
					}
					if (0 < tmpNoteNo && tmpNoteNo < 129 && chkOnOff[i].isSelected()) {
						int vol = Integer.parseInt(txtVolume[i].getText());
						int voice = Integer.parseInt(txtVoice[i].getText());
						mp.playNote(tmpNoteNo, i, vol);
//						if (seq != null && seq.isRecording()) {
//							System.out.println("5-8 addnoteon");
//							seq.addNoteOnRealtime(i, tmpNoteNo, vol, voice);
//						}
					}
				}
			}
		}
	}
	@FXML
	public void onKeyReleased(KeyEvent e){
		if (this.txtSplitpoint.isFocused()) return;
		
		String keyChar = e.getText().toLowerCase();
		lblTest.setText(keyChar + ">");

		if (mapPressedChar.containsKey(keyChar)) {
			mapPressedChar.remove(keyChar);
			int noteNo = mapK.get(keyChar);
			if (e.isShiftDown()){
				noteNo += 12;
			} else if(e.isControlDown()){
				noteNo -= 12;
			}
			if(keyChar.getBytes().length != 0){
				if (strUpperKeyChars.indexOf(keyChar) >= 0) {
					// upper keys
					for(int i=1; i<=4; i++){
						tmpNoteNo = noteNo + Integer.parseInt(txtShift[i].getText())
						           + 12*(Integer.parseInt(lblUpperOctave.getText())-2);
						lblNoteNo[i].setText(null);
						if (0 < tmpNoteNo && tmpNoteNo < 129 && chkOnOff[i].isSelected()) {
							mp.stopNote(tmpNoteNo, i, 90);
							if (seq != null && seq.isRecording()) {
								System.out.println("1-4 addnoteoff");
								seq.addNoteOffRealtime(i, tmpNoteNo, 90);
							}
						}
					}
				}else{
					// lower keys
					for(int i=5; i<=8; i++){
						tmpNoteNo = noteNo + Integer.parseInt(txtShift[i].getText())
						           + 12*(Integer.parseInt(lblLowerOctave.getText())-5);
						lblNoteNo[i].setText(null);
						if (0 < tmpNoteNo && tmpNoteNo < 129 && chkOnOff[i].isSelected()) {
							mp.stopNote(tmpNoteNo, i, 90);
							if (seq != null && seq.isRecording()) {
								System.out.println("5-8 addnoteoff");
								seq.addNoteOffRealtime(i, tmpNoteNo, 90);
							}
						}
					}
				}
			}
		}

		int flag = 0;
		if (e.isShiftDown()){
			flag = 1;
		} else if (e.isControlDown()) {
			flag = 2;
		}
		switch (e.getCode()){
		case ENTER:
			isFormLocked = !isFormLocked;
			toggleEditable(isFormLocked);
			break;
		case ESCAPE:
			isFormLocked = !isFormLocked;
			toggleEditable(isFormLocked);
			break;
		case DELETE:
		case BACK_SPACE:
			mp.allNoteOff();
			System.out.println("all note off");
			break;
		case F1:if(isFormLocked){editChannel(1,flag);} break;
		case F2:if(isFormLocked){editChannel(2,flag);} break;
		case F3:if(isFormLocked){editChannel(3,flag);} break;
		case F4:if(isFormLocked){editChannel(4,flag);} break;
		case F5:if(isFormLocked){editChannel(5,flag);} break;
		case F6:if(isFormLocked){editChannel(6,flag);} break;
		case F7:if(isFormLocked){editChannel(7,flag);} break;
		case F8:if(isFormLocked){editChannel(8,flag);} break;
		case UP:if(isFormLocked){copySetting(e);} break;
		case DOWN:if(isFormLocked){copySetting(e);} break;
		case LEFT:shiftOctave(e);break;
		case RIGHT:shiftOctave(e);break;
		default:
			break;				
		}
	}
	public void copySetting(KeyEvent e){
		if(e.isAltDown()){
			switch(e.getCode()){
			case UP:
				for(int i=5; i<=8; i++){
					txtVoice[i-4].setText(txtVoice[i].getText());
					txtVolume[i-4].setText(txtVolume[i].getText());
					txtShift[i-4].setText(txtShift[i].getText());
					lblVoice[i-4].setText(lblVoice[i].getText());
					chkOnOff[i-4].setSelected(chkOnOff[i].isSelected());
					mp.changeVoice(Integer.parseInt(txtVoice[i-4].getText()), i-4);
				}
				break;
			case DOWN:
				for(int i=1; i<=4; i++){
					txtVoice[i+4].setText(txtVoice[i].getText());
					txtVolume[i+4].setText(txtVolume[i].getText());
					txtShift[i+4].setText(txtShift[i].getText());
					lblVoice[i+4].setText(lblVoice[i].getText());
					chkOnOff[i+4].setSelected(chkOnOff[i].isSelected());
					mp.changeVoice(Integer.parseInt(txtVoice[i+4].getText()), i+4);
				}
				break;
				default:
			}
		}
	}
	public void shiftOctave(KeyEvent e){
		if(!isFormLocked){return;}
		KeyCode k = e.getCode();
		int upperOctave = Integer.parseInt(lblUpperOctave.getText());
		int lowerOctave = Integer.parseInt(lblLowerOctave.getText());

		if(e.isShiftDown()){
			//upperのみ設定
			if(k.equals(KeyCode.RIGHT) && upperOctave < 8){
				upperOctave++;
			} else if(k.equals(KeyCode.LEFT) && upperOctave > 0){
				upperOctave--;
			}
			lblUpperOctave.setText(String.valueOf(upperOctave));
		} else if (e.isControlDown()){
			//lowerのみ設定
			if(k.equals(KeyCode.RIGHT) && lowerOctave < 8){
				lowerOctave++;
			} else if(k.equals(KeyCode.LEFT) && lowerOctave > 0){
				lowerOctave--;
			}
			lblLowerOctave.setText(String.valueOf(lowerOctave));
		} else {
			//両方の設定
			if(k.equals(KeyCode.RIGHT) && upperOctave < 8 && lowerOctave < 8){
				upperOctave++;
				lowerOctave++;
			} else if(k.equals(KeyCode.LEFT) && upperOctave > 0 && lowerOctave > 0){
				upperOctave--;
				lowerOctave--;
			}
			lblUpperOctave.setText(String.valueOf(upperOctave));
			lblLowerOctave.setText(String.valueOf(lowerOctave));
		}
	}
	
	public void editChannel(int channelNo, int flag){
		isFormLocked = false;
		toggleEditable(isFormLocked);
		switch(flag){
		case 0:txtVoice[channelNo].requestFocus(); break;
		case 1:txtVolume[channelNo].requestFocus(); break;
		case 2:txtShift[channelNo].requestFocus(); break;
			default:
				
		}
//		txtVoice[channelNo].requestFocus();
	}
	
	@FXML
	public void initialize(){
		mapK = new MapKeyNote();
		mapV = new MapVoiceList();
		mapDrumNames = mapV.getXGDrumNames(0);
		mapPressedChar = new HashMap<String, String>();
		sxm = new SysExManager();
		initSplitpoint();
		
//		strUpperKeyChars = "1234567890-~\\qwertyuiop[";
		strUpperKeyChars = "1234567890-^qwertyuiop@[";
		
		//セッティングDDLの初期化
		ObservableList<String> data = FXCollections.observableArrayList();
		File dir = new File("./settings");
		File[] files = dir.listFiles();
		if(files != null){
			for(int i=0; i<files.length; i++){
				data.add(files[i].getName().replaceFirst(".xml", ""));	
			}
		}
		cmbFilename.getItems().addAll(data);

		//セッティングDDLの初期化
		ObservableList<String> data2 = FXCollections.observableArrayList();
		File dir2 = new File("./midifiles");
		File[] files2 = dir2.listFiles();
		if(files2 != null){
			for(int i=0; i<files2.length; i++){
				data2.add(files2[i].getName());	
			}
		}
		cmbMidiFiles.getItems().addAll(data2);

		//音色ラベルの初期化
		lblVoiceList = new Label[8];
		for(int j=0; j<8; j++){
			lblVoiceList[j] = new Label();
			lblVoiceList[j].setFont(new Font(12.0));
			lblVoiceList[j].setOnMouseClicked(MouseEvent -> {
				changeVoiceByList(MouseEvent);
			});
			gpBankList.add(lblVoiceList[j], 0, j);
		}

		//音色LSBラベルの初期化
		lblLSBList = new Label[21];
		for(int k=1; k<=20; k++){
			lblLSBList[k] = new Label();
			lblLSBList[k].setFont(new Font(12.0));
			lblLSBList[k].setOnMouseClicked(MouseEvent -> {
				changeVoiceByLSB(MouseEvent);
			});
			gpLSBList.add(lblLSBList[k], (k+9)/10-1, (k-1)%10);
		}

		//音色グループラベルの初期化
		lblVoiceGroup = new Label[17];
		int groupNo=0;
		for(groupNo=1; groupNo<=16; groupNo++){
			lblVoiceGroup[groupNo] = new Label(groupNo + " " + mapV.getGroupName(groupNo-1));
			lblVoiceGroup[groupNo].setFont(new Font(12.0));
			lblVoiceGroup[groupNo].setId(String.valueOf(groupNo));
			lblVoiceGroup[groupNo].setOnMouseClicked(MouseEvent -> {
				changeVoiceGroup(MouseEvent);
			});
			gpVoiceGroup.add(lblVoiceGroup[groupNo], (groupNo+7)/8-1, (groupNo-1)%8);
		}

		txtVolume = new TextField[9];
		txtVoice = new TextField[9];
		txtLSB = new TextField[9];
		txtShift = new TextField[9];
		lblVoice = new Label[9];
		chkOnOff = new CheckBox[9];
		lblNoteNo = new Label[9];
		for(int i=1; i<=8; i++){
			txtVoice[i] = new TextField("1");
			txtVoice[i].setId(String.valueOf(i));
			txtVoice[i].setEditable(false);
			txtVoice[i].setPrefWidth(20);

			txtLSB[i] = new TextField("0");
			txtLSB[i].setId(String.valueOf(i));
			txtLSB[i].setEditable(false);
			txtLSB[i].setPrefWidth(20);

			txtVolume[i] = new TextField("90");
			txtVolume[i].setId(String.valueOf(i));
			txtVolume[i].setEditable(false);
			txtVolume[i].setPrefWidth(20);
			
			txtShift[i] = new TextField("0");
			txtShift[i].setId(String.valueOf(i));
			txtShift[i].setEditable(false);
			txtShift[i].setPrefWidth(20);
			
			lblVoice[i] = new Label(mapV.getVoiceName(1));
			if(i==4 || i==8){
				lblVoice[i].setText("Drum Set");
			}
			lblVoice[i].setId(String.valueOf(i));
			lblVoice[i].setPrefWidth(80);
			chkOnOff[i] = new CheckBox();
			chkOnOff[i].setId(String.valueOf(i));
			if( i==1 || i==5) chkOnOff[i].setSelected(true);
			
			lblNoteNo[i] = new Label();
			
			gridPane.add(txtVoice[i], 2, i);
			gridPane.add(txtLSB[i], 3, i);
			gridPane.add(lblVoice[i], 4, i);
			gridPane.add(txtVolume[i], 5, i);
			gridPane.add(txtShift[i], 6, i);
			gridPane.add(chkOnOff[i], 7, i);
			gridPane.add(lblNoteNo[i], 8, i);
			txtVoice[i].setOnKeyPressed(KeyEvent -> {
				changeVoice(KeyEvent);
			});
			txtShift[i].setOnKeyPressed(KeyEvent -> {
				changeShift(KeyEvent);
			});
			txtVolume[i].setOnKeyPressed(KeyEvent -> {
				changeVolume(KeyEvent);
			});
			chkOnOff[i].setOnKeyPressed(KeyEvent -> {
				focusVoice(KeyEvent);
			});
//			txtSplitpoint.setOnKeyPressed(KeyEvent -> {
//				changeSplitpoint(KeyEvent);
//			});
		}
	
	}
	
	@FXML
	public void searchDevice(){
		mp = new MidiPlayer(false,null,this);
		cmbOutDevice.getItems().addAll(mp.getDeviceList());
		cmbInDevice.getItems().addAll(mp.getDeviceList());
		
		//デフォルトデバイスを選択する
		int defaultOut = 0;
		int defaultIn = 0;
		if(System.getProperty("os.name").toLowerCase().startsWith("windows")){
			//windowsの場合
			if (cmbOutDevice.getItems().size() > 4) {
				//デバイスを4つ以上検出（つまりUSB-MIDIケーブルがつながっている）した場合
				defaultOut = 4;
				defaultIn = 1;
			} else {
				//上記以外
				defaultOut = 0;
				defaultIn = 0;
			}
		} else {
			//Linuxの場合
			if (cmbOutDevice.getItems().size() > 4) {
				//デバイスを4つ以上検出（つまりUSB-MIDIケーブルがつながっている）した場合
				defaultOut = 3;
				defaultIn = 1;
			} else {
				//上記以外
				defaultOut = 0;
				defaultIn = 0;
			}
		}
		cmbOutDevice.getSelectionModel().select(defaultOut);
		cmbInDevice.getSelectionModel().select(defaultIn);
		
		btnOpenDevice.setDisable(false);
		btnCloseDevice.setDisable(false);
	}
	@FXML
	public void openDevice(){
		if (mp != null){
			mp.openDevice(this.cmbOutDevice.getSelectionModel().getSelectedIndex(),
					this.cmbInDevice.getSelectionModel().getSelectedIndex());

			//seqを有効なsynに接続
			if (seq != null) {
				Transmitter tran = seq.getTransmitter();
				if (tran != null) {
					tran.setReceiver(mp.defRecv);
				}
			}

		}
		cmbOutDevice.setDisable(true);
		cmbInDevice.setDisable(true);
		btnOpenDevice.setDisable(true);
		btnCloseDevice.setDisable(false);
	}
	@FXML
	public void closeDevice(){
		if (mp != null){
			mp.closeDevice();
		}
		cmbOutDevice.setDisable(false);
		cmbInDevice.setDisable(false);
		btnOpenDevice.setDisable(false);
		btnCloseDevice.setDisable(true);
	}
	
	@FXML
	public void toggleEditable(boolean editable){
		for(int i=1; i<=8; i++){
			txtVoice[i].setDisable(editable);
			txtLSB[i].setDisable(true);
			txtShift[i].setDisable(editable);
			txtVolume[i].setDisable(editable);
		}
//		btnOpenDevice.setDisable(editable);
//		btnCloseDevice.setDisable(editable);
		btnOK.setDisable(editable);
		cmbFilename.setDisable(editable);
//		txtComment.setDisable(editable);
		txtReserve.setDisable(editable);
		txtSplitpoint.setDisable(editable);
	}
	
	@FXML
	public void changeSplitpoint(KeyEvent e){
		KeyCode k = e.getCode();
		int splitpoint = Integer.parseInt(txtSplitpoint.getText());
		switch (k){
			case UP: if(splitpoint <= 115) splitpoint+=12; break;
			case DOWN:if(splitpoint >= 12) splitpoint-=12; break;
			case LEFT:if(splitpoint >= 2) splitpoint-=1; break;
			case RIGHT:if(splitpoint <= 126) splitpoint+=1; break;
			default:
		}
		txtSplitpoint.setText(String.valueOf(splitpoint));
		int a = splitpoint / 12;
		int b = splitpoint % 12;
		lblSplitpoint.setText(mapSplitpoint.get(String.valueOf(b)) + String.valueOf(a));
	}
	public int getSplitpoint(){
		return Integer.parseInt(txtSplitpoint.getText());
	}
	public void changeVoiceGroup(MouseEvent e){
		int voiceNo = 0;
		int groupNo = Integer.parseInt(((Label)e.getSource()).getId());
		
		for(int i=0; i<8; i++){
			voiceNo = (groupNo-1)*8 + i + 1;
			lblVoiceList[i].setText(voiceNo + " " + mapV.getVoiceName(voiceNo));
			lblVoiceList[i].setId(String.valueOf(voiceNo));
		}
		for(int j=1; j<=20; j++){
			lblLSBList[j].setText("");
			lblLSBList[j].setId("");
		}
	}
	public void setLSBList(MouseEvent e){
		int PCno = Integer.parseInt(((Label)e.getSource()).getId());
		LinkedHashMap<String, String> m = mapV.getXGVoiceNames(PCno);
		int i=1;
		String LSBvalue;
		String LSBname;
		for(int j=1; j<=20; j++){
			lblLSBList[j].setText("");
			lblLSBList[j].setId("");
		}
		for(Map.Entry<String, String> entry : m.entrySet()){
			LSBvalue = entry.getKey();
			LSBname = entry.getValue();
			lblLSBList[i].setText(LSBvalue + " " + LSBname);
			lblLSBList[i].setId(LSBvalue);
			i++;
		}
	}
	public void changeVoiceByLSB(MouseEvent e){
		String tmpLSBVoiceName;
		//空欄のLabelをクリックした場合は何もしない
		if( ((Label)e.getSource()).getId().equals("")){
			return;
		}
		
		//チャンネルの判別（選択されていない場合は何もしない）
		int channelNo = 0;
		for (int i=1;i<=8; i++){
			if(txtVoice[i].isFocused()){
				channelNo = i;
				break;
			}
		}
		if (channelNo == 0) return;

		//選択されているチャンネルを、選択した音色に変更する
		TextField t = txtVoice[channelNo];
		int LSBvalue = Integer.parseInt(((Label)e.getSource()).getId()); 
		int voiceNo = Integer.parseInt(t.getText());
		txtLSB[channelNo].setText(String.valueOf(LSBvalue));
		tmpLSBVoiceName = ((Label)e.getSource()).getText();
		tmpLSBVoiceName = tmpLSBVoiceName.substring(tmpLSBVoiceName.indexOf(" ")+1);
//		lblVoice[channelNo].setText(((Label)e.getSource()).getText());
		lblVoice[channelNo].setText(tmpLSBVoiceName);
		mp.changeVoiceLSB(voiceNo, LSBvalue, channelNo);
		
	}
	public void changeVoiceByList(MouseEvent e){

		//LSBリストの表示
		setLSBList(e);

		//チャンネルの判別（選択されていない場合は何もしない）
		int channelNo = 0;
		for (int i=1;i<=8; i++){
			if(txtVoice[i].isFocused()){
				channelNo = i;
				break;
			}
		}
		if (channelNo == 0) return;

		//選択されているチャンネルを、選択した音色に変更する
		TextField t = txtVoice[channelNo];
		int voiceNo = Integer.parseInt(((Label)e.getSource()).getId()); 
		t.setText(String.valueOf(voiceNo));
		lblVoice[channelNo].setText(mapV.getVoiceName(voiceNo));
		mp.changeVoice(voiceNo, channelNo);
	}
	
	public void changeVoice(KeyEvent e){
		System.out.println(((TextField)e.getSource()).getId());
		System.out.println(e.getCode().toString());
		KeyCode k = e.getCode();
		TextField t = (TextField)e.getSource();
		int channelNo = Integer.parseInt(t.getId());
		int voiceNo = Integer.parseInt(t.getText()); 
		
		switch (k){
		case UP: if(voiceNo <= 119) voiceNo+=8; break;
		case DOWN:if(voiceNo >= 9) voiceNo-=8; break;
		case LEFT:if(voiceNo >= 2) voiceNo-=1; break;
		case RIGHT:if(voiceNo <= 126) voiceNo+=1; break;
		case F1:if(channelNo==1){txtVolume[channelNo].requestFocus();}break;
		case F2:if(channelNo==2){txtVolume[channelNo].requestFocus();}break;
		case F3:if(channelNo==3){txtVolume[channelNo].requestFocus();}break;
		case F4:if(channelNo==4){txtVolume[channelNo].requestFocus();}break;
		case F5:if(channelNo==5){txtVolume[channelNo].requestFocus();}break;
		case F6:if(channelNo==6){txtVolume[channelNo].requestFocus();}break;
		case F7:if(channelNo==7){txtVolume[channelNo].requestFocus();}break;
		case F8:if(channelNo==8){txtVolume[channelNo].requestFocus();}break;
			default:
		}
		t.setText(String.valueOf(voiceNo));
		if (k == KeyCode.ESCAPE || k == KeyCode.ENTER){
			//この場合は何もしない
		} else{
			lblVoice[channelNo].setText(mapV.getVoiceName(voiceNo));
			mp.changeVoice(voiceNo, channelNo);
		}
	}
	public void changeVolume(KeyEvent e){
		KeyCode k = e.getCode();
		TextField t = (TextField)e.getSource();
		int channelNo = Integer.parseInt(t.getId());
		int volume = Integer.parseInt(t.getText()); 
		
		switch (k){
		case UP: if(volume <= 119) volume+=10; break;
		case DOWN:if(volume >= 9) volume-=10; break;
		case LEFT:if(volume >= 2) volume-=1; break;
		case RIGHT:if(volume <= 126) volume+=1; break;
		case F1:if(channelNo==1){txtShift[channelNo].requestFocus();}break;
		case F2:if(channelNo==2){txtShift[channelNo].requestFocus();}break;
		case F3:if(channelNo==3){txtShift[channelNo].requestFocus();}break;
		case F4:if(channelNo==4){txtShift[channelNo].requestFocus();}break;
		case F5:if(channelNo==5){txtShift[channelNo].requestFocus();}break;
		case F6:if(channelNo==6){txtShift[channelNo].requestFocus();}break;
		case F7:if(channelNo==7){txtShift[channelNo].requestFocus();}break;
		case F8:if(channelNo==8){txtShift[channelNo].requestFocus();}break;
			default:
		}
		t.setText(String.valueOf(volume));
	}
	public void changeShift(KeyEvent e){
		KeyCode k = e.getCode();
		TextField t = (TextField)e.getSource();
		int channelNo = Integer.parseInt(t.getId());
		int shift = Integer.parseInt(t.getText()); 
		
		switch (k){
		case UP: if(shift <= 24) shift+=12; break;
		case DOWN:if(shift >= -24) shift-=12; break;
		case LEFT:if(shift >= -35) shift-=1; break;
		case RIGHT:if(shift <= 35) shift+=1; break;
		case SPACE:shift = 0; break;
		case F1:if(channelNo==1){chkOnOff[channelNo].requestFocus();}break;
		case F2:if(channelNo==2){chkOnOff[channelNo].requestFocus();}break;
		case F3:if(channelNo==3){chkOnOff[channelNo].requestFocus();}break;
		case F4:if(channelNo==4){chkOnOff[channelNo].requestFocus();}break;
		case F5:if(channelNo==5){chkOnOff[channelNo].requestFocus();}break;
		case F6:if(channelNo==6){chkOnOff[channelNo].requestFocus();}break;
		case F7:if(channelNo==7){chkOnOff[channelNo].requestFocus();}break;
		case F8:if(channelNo==8){chkOnOff[channelNo].requestFocus();}break;
			default:
		}
		t.setText(String.valueOf(shift));
	}
	@FXML
	public void focusVoice(KeyEvent e){
		KeyCode k = e.getCode();
		CheckBox t = (CheckBox)e.getSource();
		int channelNo = Integer.parseInt(t.getId());
		switch (k){
		case F1:if(channelNo==1){txtVoice[channelNo].requestFocus();}break;
		case F2:if(channelNo==2){txtVoice[channelNo].requestFocus();}break;
		case F3:if(channelNo==3){txtVoice[channelNo].requestFocus();}break;
		case F4:if(channelNo==4){txtVoice[channelNo].requestFocus();}break;
		case F5:if(channelNo==5){txtVoice[channelNo].requestFocus();}break;
		case F6:if(channelNo==6){txtVoice[channelNo].requestFocus();}break;
		case F7:if(channelNo==7){txtVoice[channelNo].requestFocus();}break;
		case F8:if(channelNo==8){txtVoice[channelNo].requestFocus();}break;
			default:
		}
		
	}
	
	@FXML
	public void btnSeqInitClicked(){
		txtComment.setText("init");
		seq = new JavaMidiSequence(this);
		if(mp != null) mp.setSequencer(seq);
	}
	@FXML
	public void btnSeqStartClicked(){
		txtComment.setText("start");
		seq.play();
	}
	@FXML
	public void btnSeqPauseClicked(){
		long pos = seq.pause();
		txtComment.setText("pause at " + pos);
	}
	@FXML
	public void btnSeqStopClicked(){
		long pos = seq.stop();
		txtComment.setText("stop at " + pos);
	}
	@FXML
	public void btnSeqCloseClicked(){
		txtComment.setText("cloooooose");
		seq.close();
	}
	
	@FXML
	public void btnSeqStartRecClicked(){
		txtComment.setText("start rec");
		seq.startRec();
	}
	@FXML
	public void btnSeqStopRecClicked(){
		txtComment.setText("stop rec");
		seq.stopRec();
	}
	
	@FXML
	public void onBtnReadMidFileClicked(){
		String filename = "./midifiles/" + cmbMidiFiles.getSelectionModel().getSelectedItem().toString();
		seq.readMidiFile(filename);
	}
	@FXML
	public void onBtnWriteMidFileClicked(){
		String filename = "./midifiles/" + cmbMidiFiles.getSelectionModel().getSelectedItem().toString();
		seq.writeMidiFile(filename);
	}
	
	@FXML
	public void onLoadClicked(){
		String filename = "./settings/" + cmbFilename.getSelectionModel().getSelectedItem().toString();
		System.out.println(filename);
		loadSetting(filename);
	}
	@FXML
	public void onSaveClicked(){
		String filename = "./settings/" + cmbFilename.getSelectionModel().getSelectedItem().toString();
		saveSetting(filename);
	}
	private void saveSetting(String fileName) {
        // Documentインスタンスの生成
        DocumentBuilder documentBuilder = null;
        try {
             documentBuilder = DocumentBuilderFactory.newInstance()
                       .newDocumentBuilder();
        } catch (ParserConfigurationException e) {
             e.printStackTrace();
             lblTest.setText("save failure!");
        }
        Document document = documentBuilder.newDocument();

        // XML文書の作成
        Element setting = document.createElement("setting");
        setting.setAttribute("UpperOct", lblUpperOctave.getText());
        setting.setAttribute("LowerOct", lblLowerOctave.getText());
        setting.setAttribute("SplitPoint", txtSplitpoint.getText());
        document.appendChild(setting);
        for(int i=1; i<=8; i++){
            Element channel = document.createElement("channel");
            channel.setAttribute("ch", String.valueOf(i));
            channel.setAttribute("VoiceNo", txtVoice[i].getText());
            channel.setAttribute("LSBvalue", txtLSB[i].getText());
            channel.setAttribute("Shift", txtShift[i].getText());
            channel.setAttribute("Volume", txtVolume[i].getText());
            channel.setAttribute("Mute", Boolean.toString(chkOnOff[i].isSelected()));
            channel.appendChild(document.createTextNode(lblVoice[i].getText()));
            setting.appendChild(channel);
        	
        }

        // XMLファイルの作成
        File file = new File(fileName + ".xml");
        write(file, document);
		
	}

    private boolean write(File file, Document document) {

        // Transformerインスタンスの生成
        Transformer transformer = null;
        try {
             TransformerFactory transformerFactory = TransformerFactory
                       .newInstance();
             transformer = transformerFactory.newTransformer();
        } catch (TransformerConfigurationException e) {
             e.printStackTrace();
             lblTest.setText("save failure!");
             return false;
        }

        // Transformerの設定
        transformer.setOutputProperty("indent", "yes"); //改行指定
        transformer.setOutputProperty("encoding", "UTF-8"); // エンコーディング

        // XMLファイルの作成
        try {
             transformer.transform(new DOMSource(document), new StreamResult(
                       file));
        } catch (TransformerException e) {
             e.printStackTrace();
             lblTest.setText("save failure!");
             return false;
        }

        return true;
   }
    
    private void loadSetting(String filename){
		int ch = 0;
		String voiceNo, shift, volume, mute, voiceName, LSBvalue;
		String upperOct, lowerOct, splitPoint;
		try {
			DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
			DocumentBuilder bulder=factory.newDocumentBuilder();
			InputStream input=new URL("file:./" + filename + ".xml").openStream();
	
			Document dc=bulder.parse(input);
			 
			XPathFactory xpf = XPathFactory.newInstance();
			XPath xp = xpf.newXPath();
			XPathExpression xpe1;
	
			NamedNodeMap nnm;
			xpe1 = xp.compile("/setting/channel");
 			NodeList nl1 = (NodeList)xpe1.evaluate(dc, XPathConstants.NODESET);
 			for(int i=0; i<nl1.getLength(); i++){
 				nnm = nl1.item(i).getAttributes();
 				if(nnm!=null){
 					try {
 						ch = Integer.parseInt(nnm.getNamedItem("ch").getTextContent().trim());
 	 					voiceNo = nnm.getNamedItem("VoiceNo").getTextContent().trim();
 	 					shift = nnm.getNamedItem("Shift").getTextContent().trim();
 	 					volume = nnm.getNamedItem("Volume").getTextContent().trim();
 	 					mute = nnm.getNamedItem("Mute").getTextContent().trim();
 	 					voiceName = nl1.item(i).getTextContent().trim();
 	 					txtVoice[ch].setText(voiceNo);
 	 					txtShift[ch].setText(shift);
 	 					txtVolume[ch].setText(volume);
 	 					chkOnOff[ch].setSelected(Boolean.parseBoolean(mute));
 	 					lblVoice[ch].setText(voiceName);

 	 					mp.changeVoice(Integer.parseInt(voiceNo), ch);

 	 					//LSBが保存されている場合は、ロードする
 	 					try{
 	 						LSBvalue = nnm.getNamedItem("LSBvalue").getTextContent().trim();
 	 						txtLSB[ch].setText(LSBvalue);
 	 						mp.changeVoiceLSB(Integer.parseInt(voiceNo), Integer.parseInt(LSBvalue), ch);
 	 					}catch(Exception e){
 	 						txtLSB[ch].setText("0");
// 	 						e.printStackTrace();
 	 					}
 	 					
 	 					
 					}catch(Exception e){
 						e.printStackTrace();	
 					}
 				}
 			}
 			//追加の要素のロード
 			xpe1 = xp.compile("/setting");
 			nl1 = (NodeList)xpe1.evaluate(dc, XPathConstants.NODESET);
 			for(int i=0; i<nl1.getLength(); i++){
 				nnm = nl1.item(i).getAttributes();
 				if(nnm!=null){
 					
 					try {
						upperOct = nnm.getNamedItem("UpperOct").getTextContent().trim();
						lowerOct = nnm.getNamedItem("LowerOct").getTextContent().trim();
						splitPoint = nnm.getNamedItem("SplitPoint").getTextContent().trim();
						
						lblUpperOctave.setText(upperOct);
						lblLowerOctave.setText(lowerOct);
						txtSplitpoint.setText(splitPoint);
					} catch (Exception e) {
						System.out.println("Octave, Splitpoint wasn't found.");
					}
 				}
 			}
 			
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
