package application;

import java.io.File;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;

public class JavaMidiSequence {
  private Sequencer sequencer;
  private Sequence  sequence;
  private boolean isRecording;
  private long startRecTime;

  public JavaMidiSequence() {
    try {
    	isRecording = false;
      sequencer = MidiSystem.getSequencer();
      sequencer.open();
      sequence  = new Sequence(Sequence.PPQ, 480);
      sequence.createTrack();
      int chNo = 1;
      int prgNo = 19;
      int length = 480;
      int duration = 480;
      int velocity = 90;
      addNote(chNo, prgNo, 60, length*0, duration, velocity);
      addNote(chNo, prgNo, 62, length*1, duration, velocity);
      addNote(chNo, prgNo, 64, length*2, duration, velocity);
      addNote(chNo, prgNo, 65, length*3, duration, velocity);
      addNote(chNo, prgNo, 67, length*4, duration, velocity);

      addNote(chNo, prgNo, 67, length*5, duration, 0);
      addNote(chNo, prgNo, 60, length*6, duration*4, velocity);
      addNote(chNo, prgNo, 64, length*6, duration*4, velocity);
      addNote(chNo, prgNo, 67, length*6, duration*4, velocity);
      sequencer.setSequence(sequence);
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * �v���O�����`�F���W�ƃm�[�g�I��/�I�t����\�������MIDI�C�x���g��ǉ�
   */
  public void addNote(int channel, int progNumber, int noteNumber, int position, int duration, int velocity) {
    try {
      // �v���O�����`�F���W�C�x���g����
      ShortMessage progChange = new ShortMessage();
      progChange.setMessage(ShortMessage.PROGRAM_CHANGE, channel - 1, progNumber - 1, 0);
      MidiEvent progChangeEvent = new MidiEvent(progChange, position);

      // �m�[�g�I���C�x���g����
      ShortMessage noteOn = new ShortMessage();
      noteOn.setMessage(ShortMessage.NOTE_ON, channel - 1, noteNumber, velocity); //velocity=90 fixed
      MidiEvent noteOnEvent = new MidiEvent(noteOn, position);

      // �m�[�g�I�t�C�x���g����
      ShortMessage noteOff = new ShortMessage();
      noteOff.setMessage(ShortMessage.NOTE_OFF, channel - 1, noteNumber, 0);
      MidiEvent noteOffEvent = new MidiEvent(noteOff, position + duration);

      // �C�x���g�Q���V�[�P���X�֒ǉ�
      sequence.getTracks()[0].add(progChangeEvent);
      sequence.getTracks()[0].add(noteOnEvent);
      sequence.getTracks()[0].add(noteOffEvent);
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  public void writeMidiFile(String filename){
	  try {
		  MidiSystem.write(sequence, 0,new File(filename));
	  } catch (IOException e) {
		  // TODO �����������ꂽ catch �u���b�N
		  e.printStackTrace();
	  }
  }
  public void readMidiFile(String filename){
	  try {
		  sequence = MidiSystem.getSequence(new File(filename));
		  sequencer.setSequence(sequence);
	  } catch (InvalidMidiDataException | IOException e) {
		  // TODO �����������ꂽ catch �u���b�N
		  e.printStackTrace();
	  }
  }
  /**
   * �V�[�P���X���Đ�
   */
  public void play() {
    try {
      sequencer.start();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * �V�[�P���X���ꎞ��~
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
   * �V�[�P���X���~
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
   * �V�[�P���T�����
   */
  public void close() {
    sequencer.close();
  }
  
  public boolean isRecording(){
	  return isRecording;
  }
  public void startRec(){
    try {
		sequence  = new Sequence(Sequence.PPQ, 480);
        sequence.createTrack();
        sequencer.setSequence(sequence);
//        sequencer.recordEnable(sequence.getTracks()[0], -1);
//        sequencer.startRecording();
        
        isRecording = true;
        startRecTime = System.currentTimeMillis();
        System.out.println("start rec at:" + startRecTime);
        
	} catch (InvalidMidiDataException   e) {
		// TODO �����������ꂽ catch �u���b�N
		e.printStackTrace();
	}
  }
  
  public void addNoteOnRealtime(int channel, int noteNumber, int velocity, int progNumber){

	  try {
		  long position = System.currentTimeMillis()-startRecTime;
		  System.out.println("position:" + position);

	      // �v���O�����`�F���W�C�x���g����
	      ShortMessage progChange = new ShortMessage();
	      progChange.setMessage(ShortMessage.PROGRAM_CHANGE, channel - 1, progNumber - 1, 0);
	      MidiEvent progChangeEvent = new MidiEvent(progChange, position-10);

	      // �m�[�g�I���C�x���g����
		  ShortMessage noteOn = new ShortMessage();
		  noteOn.setMessage(ShortMessage.NOTE_ON, channel - 1, noteNumber, velocity);
		  MidiEvent noteOnEvent = new MidiEvent(noteOn, position);

	      sequence.getTracks()[0].add(progChangeEvent);
	      sequence.getTracks()[0].add(noteOnEvent);

	  } catch (InvalidMidiDataException e) {
		  e.printStackTrace();
	  }

  }
  public void addNoteOffRealtime(int channel, int noteNumber, int velocity){

	  try {
		  long position = System.currentTimeMillis()-startRecTime;
		  System.out.println("note off position:" + position);

		  // �m�[�g�I�t�C�x���g����
		  ShortMessage noteOff = new ShortMessage();
		  noteOff.setMessage(ShortMessage.NOTE_OFF, channel - 1, noteNumber, 0);
		  MidiEvent noteOffEvent = new MidiEvent(noteOff, position);

	      sequence.getTracks()[0].add(noteOffEvent);

	  } catch (InvalidMidiDataException e) {
		  e.printStackTrace();
	  }

  }
  
  public void stopRec(){
//	  sequencer.stopRecording();
	  isRecording = false;
	  System.out.println("stop rec at:" + System.currentTimeMillis());
  }
}