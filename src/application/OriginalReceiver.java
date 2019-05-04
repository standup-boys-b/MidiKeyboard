package application;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;

public class OriginalReceiver implements Receiver {
	
	private byte[] nowMsg;
	private MidiPlayer mp;
	private int keySplitPos = 72;
	private int[] keyTrigger;
	
	public OriginalReceiver(MidiPlayer mp){
		this.mp = mp;
		keyTrigger = new int[128];
		for(int i=0; i<128; i++){
			keyTrigger[i] = 0;
		}
	}
	
	private boolean testTrigger(MidiMessage message, long timeStamp){
		boolean b = false;
		nowMsg = message.getMessage();
		if ( (nowMsg[0] & 0xF0) == 0x90 && nowMsg[2] > 0){
			if(keyTrigger[nowMsg[1]] == 0){
				System.out.println("trig " + nowMsg[1] + " on " + nowMsg[2]);
				keyTrigger[nowMsg[1]] = 1;
				b = true;
			}
		}else if ( (nowMsg[0] & 0xF0) == 0x90 && nowMsg[2] == 0){
			if(keyTrigger[nowMsg[1]] == 1){
				System.out.println("trig " + nowMsg[1] + " off");
				keyTrigger[nowMsg[1]] = 0;
				b = true;
			}
		} else {
			
		}
		return b;
	}
	
	@Override
	public void send(MidiMessage message, long timeStamp) {
		try {
			keySplitPos = mp.getSplitpoint();
			boolean b = testTrigger(message, timeStamp);
			if (!b) {
//				System.out.println("no oper");
				return;
			} else {
				nowMsg = message.getMessage();
				//--キーボード入力を受け取り、発音する部分 SS----------------------------
				if (nowMsg[2] > 0 ){
					// note on
					if (nowMsg[1] < keySplitPos){
						//upper keys
						for(int i=1; i<=4; i++){
							mp.playNote(nowMsg[1], i);
						}
					} else {
						//lower keys
						for(int i=5; i<=8; i++){
							mp.playNote(nowMsg[1], i);
						}
					}
				} else {
					// note off
					if (nowMsg[1] < keySplitPos){
						//upper keys
						for(int i=1; i<=4; i++){
							mp.stopNote(nowMsg[1], i);
						}
					} else {
						//lower keys
						for(int i=5; i<=8; i++){
							mp.stopNote(nowMsg[1], i);
						}
					}
					
				}
				//--キーボード入力を受け取り、発音する部分 EE----------------------------
			}

		} catch (java.lang.StackOverflowError e) {
			// TODO: handle exception
		}

	}

	@Override
	public void close() {
		close();	
	}

}
