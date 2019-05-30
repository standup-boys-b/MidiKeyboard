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
	        	//--Soundfont���g�p����ꍇ--------------------------------------------------------------
				System.out.println("===Preparing Soundfont!!===");
//				file = new File("GeneralUser GS MuseScore v1.442.sf2");
				file = new File("resource/TimGM6mb.sf2");
				System.out.println(file.getAbsolutePath());
				System.out.println("exists? " + file.exists());
//				SF2Soundbank bank = new SF2Soundbank(file);
				SF2SoundbankReader rd = new SF2SoundbankReader();
				Soundbank bank = rd.getSoundbank(file);
				// �V���Z�T�C�U�[�̍쐬
//				SoftSynthesizer synth = new SoftSynthesizer();
				Synthesizer synth = new SoftSynthesizer();
				synth.open();    // ��������Ȃ��ƃG���[�ɂȂ�
				synth.loadAllInstruments(bank);    // open���Ă��炶��Ȃ��Ɠǂݍ���ł���Ȃ�
				devOut = synth;
	        } else {
	        	//--MidiDevice���g�p����ꍇ--------------------------------------------------------------
				System.out.println("===Preparing MidiDevice!!===");
		        if (devices.size() > 4) {
		        	devOut = devices.get(4); //4,5 �����g���Ȃ��H1�̓G���[
		        	devIn = devices.get(1); 
		        }else{
		        	devOut = devices.get(0); //4,5 �����g���Ȃ��H1�̓G���[
		        }
		        devOut.open();
		        if (devIn != null) {devIn.open();}
	        }

	        // receiver�̏���
	        if (devOut == null){
				defRecv = MidiSystem.getReceiver();
				System.out.println("Dev was null!");
			}else{
				defRecv = devOut.getReceiver();
				orgRecv = new CasioToneReceiver(this);
			}
	        // devInput�̏���
	        // USB�P�[�u����in�ɂȂ����f�o�C�X(CasioTone)�ɑ΂��āAReceiver(�L�[����U���)�����蓖��
			if (devIn != null) {
				trans = devIn.getTransmitter(); //casiotone����tran��get
				//casiotone��tran��casiotone��recv�ɂȂ��ł���H
				//�ł����ǁAorgRecv��recv���̂��̂ł͂Ȃ��A�����̓��͏����N���X�ŁA������mp��defRecv�ɑ΂���
				//noteon�𑗐M���Ă��邩��AMU50�Ȃ�Gervill�Ȃ�K�؂�syn���������Ă���̂��B
				//casiotone��tran��defRecv�ɒ��ڂȂ��ƁA�����炭casiotone��jam�M��������defRecv�ɍs���B
				//no,no,orgRecv�Ƃ́A����́uCasioToneReceiver�v�N���X�B�����ɂȂ����ƂŁAjam�M���̃T�j�^�C�Y��
				//�s���A���܂���������B�����A�Ȃ������ςɂȂ��Ă��邾���iCasioToneReceiver�N���X���ŁAmp���Ăяo���āA
				//mp����defRecv��noteon���΂��Ă���j
				//���C���[��X�v���b�g������������A�t�s���������肷��ȏ�A���̕ςȐڑ��͒v�����Ȃ����B
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

	        //LSB�ύX�𑗐M
	        msgChangevoice.setMessage(ShortMessage.CONTROL_CHANGE, ch, 32, LSBvalue);
	        defRecv.send(msgChangevoice, 1L);

	        //�v���O�����`�F���W�𑗐M
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
			// TODO �����������ꂽ catch �u���b�N
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
