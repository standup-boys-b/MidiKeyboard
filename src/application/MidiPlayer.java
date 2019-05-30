package application;

import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Transmitter;
import javax.swing.JCheckBox;

import com.sun.media.sound.SF2SoundbankReader;
import com.sun.media.sound.SoftSynthesizer;

import sun.misc.HexDumpEncoder;

public class MidiPlayer {
	Receiver defRecv;
	Receiver orgRecv;
	Transmitter trans;
	MidiDevice devOut;
	MidiDevice devIn;
	MidiDevice devSeq;
	HexDumpEncoder enc;
	int volume[] = {90,0,0,0,90,0,0,0};
	int shift[] = {0,0,0,0,0,0,0,0};
	boolean isUpperKey;
	JCheckBox[] chkMute;
	File file;
	CtlMU50Player sc;
	JavaMidiSequence seq;
	private ArrayList<MidiDevice> devices;
	
	public MidiPlayer(boolean isUpperKey, JCheckBox[] chkMute, CtlMU50Player sc) {
		try {
			this.isUpperKey = isUpperKey;
			enc = new HexDumpEncoder();
			this.chkMute = chkMute;
			this.sc = sc;

			devices = getDevices();
			dumpDeviceInfo(devices);

	        boolean isUseSoundfont = sc.isUseSoundFont();
	        if(isUseSoundfont){
	        	//--Soundfontを使用する場合--------------------------------------------------------------
				System.out.println("===Preparing Soundfont!!===");
//				file = new File("GeneralUser GS MuseScore v1.442.sf2");
				file = new File("resource/TimGM6mb.sf2");
				System.out.println(file.getAbsolutePath());
				System.out.println("exists? " + file.exists());
//				SF2Soundbank bank = new SF2Soundbank(file);
				SF2SoundbankReader rd = new SF2SoundbankReader();
				Soundbank bank = rd.getSoundbank(file);
				// シンセサイザーの作成
//				SoftSynthesizer synth = new SoftSynthesizer();
				Synthesizer synth = new SoftSynthesizer();
				synth.open();    // これをやらないとエラーになる
				synth.loadAllInstruments(bank);    // openしてからじゃないと読み込んでくれない
				devOut = synth;
	        } else {
	        	//--MidiDeviceを使用する場合--------------------------------------------------------------
				System.out.println("===Preparing MidiDevice!!===");
		        if (devices.size() > 4) {
		        	devOut = devices.get(4); //4,5 しか使えない？1はエラー
		        	devIn = devices.get(1); 
		        }else{
		        	devOut = devices.get(0); //4,5 しか使えない？1はエラー
		        }
		        devOut.open();
		        if (devIn != null) {devIn.open();}
	        }

	        // receiverの準備
	        if (devOut == null){
				defRecv = MidiSystem.getReceiver();
				System.out.println("Dev was null!");
			}else{
				defRecv = devOut.getReceiver();
				orgRecv = new CasioToneReceiver(this);
			}
	        // devInputの準備
	        // USBケーブルのinにつないだデバイス(CasioTone)に対して、Receiver(キー割り振り役)を割り当て
			if (devIn != null) {
				trans = devIn.getTransmitter(); //casiotoneからtranをget
				//casiotoneのtranをcasiotoneのrecvにつないでいる？
				//でも結局、orgRecvはrecvそのものではなく、ただの入力処理クラスで、内部でmpのdefRecvに対して
				//noteonを送信しているから、MU50なりGervillなり適切なsynが発音しているのか。
				//casiotoneのtranをdefRecvに直接つなぐと、おそらくcasiotoneのjam信号が直接defRecvに行く。
				//no,no,orgRecvとは、自作の「CasioToneReceiver」クラス。ここにつなぐことで、jam信号のサニタイズが
				//行われ、うまく発音する。ただ、つなぎ方が変になっているだけ（CasioToneReceiverクラス内で、mpを呼び出して、
				//mp内でdefRecvにnoteonを飛ばしている）
				//レイヤーやスプリットを実現したり、輻輳解消したりする以上、この変な接続は致し方ないか。
				trans.setReceiver(orgRecv);
//				trans.setReceiver(defRecv);
				System.out.println(trans.toString());
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public int getSplitpoint(){
		return sc.getSplitpoint();
	}
	public int getUpperOctPosition(){
		return sc.getUpperOctPosition();
	}
	public int getLowerOctPosition(){
		return sc.getLowerOctPosition();
	}
	
	public ArrayList<String> getDeviceList(){
		ArrayList<String> ary = new ArrayList<String>();
        MidiDevice device;
        MidiDevice.Info info;
		for(int i=0; i<devices.size(); i++){
	        device = devices.get(i);
	        info = device.getDeviceInfo();
			ary.add(String.valueOf(i) + " " + info.getName());
		}
		return ary;	
	}

	public void closeDevice() {
		
		try {
            if (defRecv != null) defRecv.close();
            if (trans != null) trans.close();
            if (devOut != null) devOut.close();
            if (devIn != null) devIn.close();
            if (devSeq != null) devSeq.close();
            System.out.println("Device[" + devOut.getDeviceInfo().getName() + "] is open? : " + devOut.isOpen());
            System.out.println("Device[" + devIn.getDeviceInfo().getName() + "] is open? : " + devIn.isOpen());
            System.out.println("Device[" + devSeq.getDeviceInfo().getName() + "] is open? : " + devSeq.isOpen());
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void openDevice(int devOutNo, int devInNo, int devSeqNo){
		try {
			if (devOut != null) devOut.close();
			devOut = devices.get(devOutNo);
			if (devIn != null) devIn.close();
			devIn = devices.get(devInNo);
			if (devSeq != null) devSeq.close();
			devSeq = devices.get(devSeqNo);
			openDevice();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public void openDevice() {
		try {
            if (devOut != null && !devOut.isOpen()){
            	devOut.open();
                defRecv = devOut.getReceiver();
            }
            if (devIn != null && !devIn.isOpen()){
            	devIn.open();
                trans = devIn.getTransmitter();
                trans.setReceiver(orgRecv);
//              trans.setReceiver(defRecv);
            }
            if (devSeq != null && !devSeq.isOpen()){
            	devSeq.open();
                trans = devSeq.getTransmitter();
                trans.setReceiver(orgRecv);
//              trans.setReceiver(defRecv);
            }
            System.out.println("Device[" + devOut.getDeviceInfo().getName() + "] is open? : " + devOut.isOpen());
            System.out.println("Device[" + devIn.getDeviceInfo().getName() + "] is open? : " + devIn.isOpen());
            System.out.println("Device[" + devSeq.getDeviceInfo().getName() + "] is open? : " + devSeq.isOpen());
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void setVolume(int volume, int ch){
		try {
			this.volume[ch] = volume;
		}catch(Exception e){
			e.printStackTrace();			
		}
	}

	public void setShift(int shift, int ch){
		try {
			this.shift[ch] = shift;
		}catch(Exception e){
			e.printStackTrace();			
		}
	}

	public void changeVoice(int voiceNo, int ch){
		try {
	        ShortMessage msgChangevoice = new ShortMessage();
	        msgChangevoice.setMessage(ShortMessage.PROGRAM_CHANGE, ch, voiceNo-1, 2);
	        defRecv.send(msgChangevoice, 1L);
		}catch(Exception e){
			e.printStackTrace();			
		}
	}


	public void changeVoiceLSB(int voiceNo, int LSBvalue, int ch){
		try {
	        ShortMessage msgChangevoice = new ShortMessage();

	        //LSB変更を送信
	        msgChangevoice.setMessage(ShortMessage.CONTROL_CHANGE, ch, 32, LSBvalue);
	        defRecv.send(msgChangevoice, 1L);

	        //プログラムチェンジを送信
	        ShortMessage msg2 = new ShortMessage();
	        msg2.setMessage(ShortMessage.PROGRAM_CHANGE, ch, voiceNo-1, 2);
	        defRecv.send(msg2, 1L);
		}catch(Exception e){
			e.printStackTrace();			
		}
	}
	public void playNote(int noteNo, int chno){
		if (sc.isChannelOn(chno)){
			playNote(noteNo + sc.getNoteShift(chno), chno, sc.getVolume(chno));
		}
	}
	
	public void stopNote(int noteNo, int chno){
		if (sc.isChannelOn(chno)){
			stopNote(noteNo + sc.getNoteShift(chno), chno, 0);
		}
	}
	public void playNote(int noteNo, int channelNo,int volume) {
		try {
			int tmpChNo = channelNo;
	        ShortMessage noteOn = new ShortMessage();
//	        if(channelNo == 4 || channelNo == 8){
//	        	tmpChNo = 9;
//	        }
            noteOn.setMessage(ShortMessage.NOTE_ON, tmpChNo, noteNo, volume);
            defRecv.send(noteOn, 1L);
            if(seq != null && seq.isRecording()){
            	seq.addNoteOnRealtime(tmpChNo, noteNo, volume, 19);
            }
		} catch (Exception ex) {
        	ex.printStackTrace();
        }
	}

	public void stopNote(int noteNo, int channelNo,int volume) {
		try {
			int tmpChNo = channelNo;
			ShortMessage noteOff = new ShortMessage();
//			if(channelNo == 4 || channelNo == 8){
//	        	tmpChNo = 9;
//	        }
            noteOff.setMessage(ShortMessage.NOTE_OFF, tmpChNo, noteNo, volume);
            defRecv.send(noteOff, 1L);
            if(seq != null && seq.isRecording()){
            	seq.addNoteOffRealtime(tmpChNo, noteNo, volume);
            }
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
	}

	public void allNoteOff() {
		try {
			ShortMessage msg = new ShortMessage();
	        for(int ch=0; ch<9; ch++){
	            msg.setMessage(ShortMessage.CONTROL_CHANGE, ch, 0x78, 0x00);
	            defRecv.send(msg, 1L);
	        }
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
	}
	public SysexMessage sendExMsg(String s){
		SysexMessage msg = new SysexMessage();
		byte[] b = getByteArray(s);
		try {
			msg.setMessage(b,b.length);
			System.out.println("exMsg is " + msg.getMessage());
		} catch (InvalidMidiDataException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		
//		Receiver receiver;
		try {
			if (devOut == null){
//	    		receiver = MidiSystem.getReceiver();
	    		System.out.println("Dev was null!");
	    	}else{
//	    		receiver = dev.getReceiver();
	    	}
			System.out.println("rcv is " + defRecv.toString());
			defRecv.send(msg, 1L);
		} catch(Exception ex){
			ex.printStackTrace();
		}
		return msg;
		
	}
	
	private byte[] getByteArray(String s){
		StringTokenizer st = new StringTokenizer(s);
		byte[] b; b=new byte[st.countTokens()];
		int i=0;
		while(st.hasMoreTokens()){
			b[i] = Integer.decode("0x" + st.nextToken()).byteValue();
			i++;
		}
		for(int j=0;j<b.length;j++){
			System.out.printf("%02x\n",b[j]);
		}
		return b;
		
	}

	private ArrayList<MidiDevice> getDevices() {
        ArrayList<MidiDevice> devices = new ArrayList<MidiDevice>();

        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        for (int i = 0; i < infos.length; i++) {
            MidiDevice.Info info = infos[i];
            MidiDevice dev = null;
            try {
                dev = MidiSystem.getMidiDevice(info);
                devices.add(dev);
            } catch (SecurityException e) {
                System.err.println(e.getMessage());
            } catch (MidiUnavailableException e) {
                System.err.println(e.getMessage());
            }
        }
        return devices;
    }

	private void dumpDeviceInfo(ArrayList<MidiDevice> devices) {
        for (int i = 0; i < devices.size(); i++) {
            MidiDevice device = devices.get(i);
            MidiDevice.Info info = device.getDeviceInfo();
            System.out.println("[" + i + "] devinfo: " + info.toString());
            System.out.println("  name:"        + info.getName());
            System.out.println("  vendor:"      + info.getVendor());
            System.out.println("  version:"     + info.getVersion());
            System.out.println("  description:" + info.getDescription());
            if (device instanceof Synthesizer) {
                System.out.println("  SYNTHESIZER");
            }
            if (device instanceof Sequencer) {
                System.out.println("  SEQUENCER");
            }
            System.out.println("");
        }
    }
	
	public void setSequencer(JavaMidiSequence seq){
		this.seq = seq;
	}
}
