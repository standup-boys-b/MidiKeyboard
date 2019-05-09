package application;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;

public class CasioToneReceiver implements Receiver {
	
	private byte[] nowMsg;
	private MidiPlayer mp;
	private int keySplitPos = 72;
	private int[] keyTrigger;
	
	// �R���X�g���N�^
	public CasioToneReceiver(MidiPlayer mp){
		this.mp = mp;
		keyTrigger = new int[128];
		for(int i=0; i<128; i++){
			keyTrigger[i] = 0;
		}
	}
	
	// �M�����t�s��h�����߂̃��\�b�h�B��M��������M���̂����擪������catch���A�㑱�͎̂Ă�
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
	
	// upper/lower�̐U�蕪���A���C���[���A�t�s�h�~���s����������MIDIout�ɉ��t�M���𑗂�
	@Override
	public void send(MidiMessage message, long timeStamp) {
		
		try {
			keySplitPos = mp.getSplitpoint();
			int upperOctPosition = mp.getUpperOctPosition();
			int lowerOctPosition = mp.getLowerOctPosition();

			boolean b = testTrigger(message, timeStamp);
			if (!b) {
//				System.out.println("no oper");
				return;
			} else {
				nowMsg = message.getMessage();
				//--�L�[�{�[�h���͂��󂯎��A�������镔�� SS----------------------------
				if (nowMsg[2] > 0 ){
					// note on
					if (nowMsg[1] < keySplitPos){
						//upper keys
						for(int i=1; i<=4; i++){
							mp.playNote(nowMsg[1] + 12*upperOctPosition, i);
						}
					} else {
						//lower keys
						for(int i=5; i<=8; i++){
							mp.playNote(nowMsg[1] + 12*lowerOctPosition, i);
						}
					}
				} else {
					// note off
					if (nowMsg[1] < keySplitPos){
						//upper keys
						for(int i=1; i<=4; i++){
							mp.stopNote(nowMsg[1] + 12*upperOctPosition, i);
						}
					} else {
						//lower keys
						for(int i=5; i<=8; i++){
							mp.stopNote(nowMsg[1] + 12*lowerOctPosition, i);
						}
					}
					
				}
				//--�L�[�{�[�h���͂��󂯎��A�������镔�� EE----------------------------
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
