package de.ralleytn.simple.audio;

import com.jcraft.jorbis.JOrbisException;
import com.jcraft.jorbis.VorbisFile;
import de.jarnbjo.vorbis.VorbisStream;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.io.InputStream;

/**
 * Wraps a {@linkplain VorbisStream}.
 * @author Ralph Niemitz/RalleYTN(ralph.niemitz@gmx.de)
 * @version 1.2.2
 * @since 1.2.2
 */
public class VorbisInputStream extends InputStream {

	private VorbisStream source;
	private VorbisFile vf;
	private byte[] comparisonBuffer;
//	private SyncState oy;
//	private StreamState os;
//	private Page og;
//	private Packet op;
//	private Info vi;
//	private Comment vc;
//	private DspState vd;
//	private Block vb;

	/**
	 * @param source the instance of {@linkplain VorbisStream} to wrap
	 * @since 1.2.2
	 */
	VorbisInputStream(VorbisStream source) {

		this.source = source;

//		oy=new SyncState();
//		os=new StreamState();
//		og=new Page();
//		op=new Packet();
//
//		vi=new Info();
//		vc=new Comment();
//		vd=new DspState();
//		vb=new Block(vd);
	}

	private InputStream is;

	VorbisInputStream(InputStream is, VorbisStream source) throws JOrbisException {
		this.is = is;
		this.vf = new VorbisFile(is, null, -1);
		this.source = source;
	}
	
	@Override
	public int read(byte[] buffer) throws IOException {
		return this.read(buffer, 0, buffer.length);
	}
	
	@Override
	public int read(byte[] buffer, int offset, int length) throws IOException {
		int returned = -1;

		int channels = this.source.getIdentificationHeader().getChannels();

		//region NEW CODE
		if(channels == 1) {
			//OK
			returned = vf.read(buffer, buffer.length, 1, 2, 1, null);
		} else /* if channels == 2 */ {
			//kinda noisy
			returned = vf.read(buffer, buffer.length, 1, 1, 1, null);
		}
		if(returned == 0) returned = -1;
		//endregion NEW CODE

//		if(comparisonBuffer == null || comparisonBuffer.length != buffer.length) {
//			comparisonBuffer = new byte[buffer.length];
//		}

		//region OLD CODE
//		int joggReturned = -1;
//		try {
//			joggReturned = this.source.readPcm(comparisonBuffer, offset, length);
//		} catch(EndOfOggStreamException exception) {}
		//endregion OLD_CODE

		//region COMPARISON
//		if(joggReturned != returned) {
//			System.out.println("read lengths didn't match");
//			System.out.println("new: " + returned);
//			System.out.println("old: " + joggReturned);
//		} else {
//			System.out.println("read lengths DID match");
//			boolean matched = true;
//			for(int i = 0; i < buffer.length; i++) {
//				if(buffer[i] != comparisonBuffer[i]) {
//					matched = false;
//					break;
//				}
//			}
//			if(matched) {
//				System.out.println("All matched");
//			}
//		}
		//endregion COMPARISON

//		System.out.println(">>> " + returned);

		return returned;
	}
	
	@Override
	public int read() throws IOException {
		
		return 0;
	}
	
	/**
	 * @return the {@linkplain AudioFormat} of the wrapped {@linkplain VorbisStream}.
	 * @since 1.2.2
	 */
	AudioFormat getAudioFormat() {
		int sampleRate = this.source.getIdentificationHeader().getSampleRate();
		int channels = this.source.getIdentificationHeader().getChannels();
		System.out.println("Sample Rate: " + sampleRate);
		System.out.println("Channels: " + channels);
		if(channels == 1) {
			//OK
			return new AudioFormat(sampleRate, 16, 1, true, true);
		} else /* if channels == 2 */ {
			//kinda noisy
			return new AudioFormat(sampleRate, 8, 2, true, true);
		}
	}
}
