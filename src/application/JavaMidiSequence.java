package application;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;

public class JavaMidiSequence {
	private Sequencer sequencer;
	private Sequence  sequence;
	private boolean isRecording;
	private long startRecTime;
	private int currentTrackNo;
	private int[] recCh;

	public JavaMidiSequence(CtlMU50Player ctl) {
		try {
			isRecording = false;
			
			sequencer = MidiSystem.getSequencer(false);
			sequencer.open();
			sequence  = new Sequence(Sequence.PPQ, 480);
			sequence.createTrack();
			currentTrackNo = sequence.getTracks().length-1;
			System.out.println("current track no:" + currentTrackNo);
			recCh = new int[10];

			for(int i=1; i<=8; i++){
				// プログラムチェンジイベント生成
				ShortMessage progChange = new ShortMessage();
				recCh[i] = ctl.getRecCh(i);
				if(recCh[i] != 0){
					progChange.setMessage(ShortMessage.PROGRAM_CHANGE, recCh[i], ctl.getVoiceNo(i)-1, 0);
					MidiEvent progChangeEvent = new MidiEvent(progChange, 0L);
					sequence.getTracks()[currentTrackNo].add(progChangeEvent);
				}
			}
			sequencer.setSequence(sequence);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * プログラムチェンジとノートオン/オフから構成されるMIDIイベントを追加
	 */
	public void addNote(int channel, int progNumber, int noteNumber, int position, int duration, int velocity) {
		try {
			// プログラムチェンジイベント生成
			ShortMessage progChange = new ShortMessage();
			progChange.setMessage(ShortMessage.PROGRAM_CHANGE, channel - 1, progNumber - 1, 0);
			MidiEvent progChangeEvent = new MidiEvent(progChange, position);

			// ノートオンイベント生成
			ShortMessage noteOn = new ShortMessage();
			noteOn.setMessage(ShortMessage.NOTE_ON, channel - 1, noteNumber, velocity); //velocity=90 fixed
			MidiEvent noteOnEvent = new MidiEvent(noteOn, position);

			// ノートオフイベント生成
			ShortMessage noteOff = new ShortMessage();
			noteOff.setMessage(ShortMessage.NOTE_OFF, channel - 1, noteNumber, 0);
			MidiEvent noteOffEvent = new MidiEvent(noteOff, position + duration);

			// イベント群をシーケンスへ追加
			sequence.getTracks()[0].add(progChangeEvent);
			sequence.getTracks()[0].add(noteOnEvent);
			sequence.getTracks()[0].add(noteOffEvent);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void writeMidiFile(String filename){
		try {
			MidiSystem.write(sequence, 1, new File(filename));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void readMidiFile(String filename){
		try {
			sequence = MidiSystem.getSequence(new File(filename));
			sequencer.setSequence(sequence);
			sequencer.setSequence(sequence);

		} catch (InvalidMidiDataException | IOException e) {
			e.printStackTrace();
		}
	}
	public void playSong(ArrayList<SongPart> list){
		
		Thread th = new Thread(){
			public void run(){
				System.out.println("songplay start in another thread");
				for(SongPart sp : list){
					try {
						sequence = MidiSystem.getSequence(new File("./midifiles/" +sp.filename));
						sequencer.setSequence(sequence);
						Long partLength = sequence.getTickLength();
						sequencer.setLoopStartPoint(0L);
						sequencer.setLoopEndPoint(partLength);
						sequencer.setLoopCount(sp.loopCount-1);
						sequencer.start();
						try {
							Thread.sleep(partLength * (sp.loopCount));
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
					} catch (InvalidMidiDataException | IOException e) {
						e.printStackTrace();
					}
				}
				System.out.println("songplay end in another thread");
			}
		};
		th.start();
	}
	
	/**
	 * シーケンスを再生
	 * @param loopCount 
	 */
	public void play(boolean isLoopOn, int loopCount) {
		try {
			if(isLoopOn){
				sequencer.setLoopStartPoint(0L);
				sequencer.setLoopEndPoint(sequence.getTickLength());
				sequencer.setLoopCount(loopCount-1);
			} else {
				sequencer.setLoopCount(0);
			}
			sequencer.start();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * シーケンスを一時停止
	 */
	public long pause() {
		long pos = 0L;
		if(sequencer.isRunning()) {
			sequencer.stop();
			pos = sequencer.getTickPosition();
		}
		return pos;
	}

	/**
	 * シーケンスを停止
	 */
	public long stop() {
		long pos = 0L;
		if(sequencer.isRunning()) {
			sequencer.stop();
			pos = sequencer.getTickPosition();
		}
		sequencer.setTickPosition(0);
		return pos;
	}

	/**
	 * シーケンサを閉じる
	 */
	public void close() {
		sequencer.close();
	}

	public boolean isRecording(){
		return isRecording;
	}
	public void startRec(){
		try {
			sequence.createTrack();
			sequencer.setSequence(sequence);
			currentTrackNo = sequence.getTracks().length-1;
			System.out.println("new track no:" + currentTrackNo);


			isRecording = true;
			startRecTime = System.currentTimeMillis();
			sequencer.setTickPosition(0L);
			sequencer.start();
			System.out.println("start rec at:" + startRecTime);

		} catch (InvalidMidiDataException   e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	public void stopRec(){
		isRecording = false;
		sequencer.stop();

		sequencer.setTickPosition(0L);
		System.out.println("stop rec at:" + System.currentTimeMillis());
	}  
	public void addNoteOnRealtime(int channel, int noteNumber, int velocity, int progNumber){

		try {
			long position = System.currentTimeMillis()-startRecTime;
			System.out.println("position:" + position);

			// ノートオンイベント生成
			if(recCh[channel]!=0){
				ShortMessage noteOn = new ShortMessage();
				noteOn.setMessage(ShortMessage.NOTE_ON, recCh[channel], noteNumber, velocity);
				MidiEvent noteOnEvent = new MidiEvent(noteOn, position);

				sequence.getTracks()[currentTrackNo].add(noteOnEvent);
			}

		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}

	}
	public void addNoteOffRealtime(int channel, int noteNumber, int velocity){

		try {
			long position = System.currentTimeMillis()-startRecTime;
			System.out.println("note off position:" + position);

			// ノートオフイベント生成
			if(recCh[channel]!=0){
				ShortMessage noteOff = new ShortMessage();
				noteOff.setMessage(ShortMessage.NOTE_OFF, recCh[channel], noteNumber, 0);
				MidiEvent noteOffEvent = new MidiEvent(noteOff, position);

				sequence.getTracks()[currentTrackNo].add(noteOffEvent);
			}

		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}

	}


	public Transmitter getTransmitter(){
		try {
			return sequencer.getTransmitter();
		} catch (MidiUnavailableException e) {
			e.printStackTrace();
			return null;
		}
	}
}