package application;

import java.util.StringTokenizer;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.SysexMessage;

public class SysExManager {
	public SysexMessage sendExMsg(MidiDevice dev, String s){
		SysexMessage msg = new SysexMessage();
		byte[] b = getByteArray(s);
		try {
			msg.setMessage(b,b.length);
		} catch (InvalidMidiDataException e) {
			// TODO é©ìÆê∂ê¨Ç≥ÇÍÇΩ catch ÉuÉçÉbÉN
			e.printStackTrace();
		}
		
		Receiver receiver;
		try {
			if (dev == null){
	    		receiver = MidiSystem.getReceiver();
	    		System.out.println("Dev was null!");
	    	}else{
	    		receiver = dev.getReceiver();
	    	}
			receiver.send(msg, 1L);
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

}
