package application;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;

public class JavaMidiSequence {
  private Sequencer sequencer;
  private Sequence  sequence;

  public JavaMidiSequence() {
    try {
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
}