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

		int returned = vf.read(buffer, buffer.length, true, 2, true, null);
		if(returned == 0) returned = -1;

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
		return new AudioFormat(sampleRate, 16, channels, true, true);
	}
}
