package llc.ufwa.data.resource.linear;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RandomAccessFileWriter implements LinearStreamWriter {
	
	private static final Logger logger = LoggerFactory.getLogger(RandomAccessFileWriter.class);

	private final RandomAccessFile random;
	private final String ACCESS_MODE = "rws";
	
	public RandomAccessFileWriter(final File file) throws FileNotFoundException {
		random = new RandomAccessFile(file, ACCESS_MODE);
	}
	
	@Override
	public void seek(long index) {
		try {
			random.seek(index);
		} 
		catch (IOException e) {
			logger.error("IOException <1>: " + e.getMessage());
		}
	}

	@Override
	public int read(byte[] buff) {
		
		int read = -1;
		
		try {
			read = random.read(buff);
		} 
		catch (IOException e) {
			logger.error("IOException <2>: " + e.getMessage());
		}
		
		return read;
		
	}
	
	@Override
	public void read(long index, byte[] buff) {
		this.seek(index);
		this.read(buff);
	}
	
	@Override
	public void write(byte[] in) {
		try {
			random.write(in);
		} 
		catch (IOException e) {
			logger.error("IOException <3>: " + e.getMessage());
		}
	}

	@Override
	public void write(long index, byte[] in) {
		this.seek(index);
		this.write(in);
	}
	
	public void close() {
		try {
			random.close();
		}
		catch (IOException e) {
			logger.error("IOException <4>: " + e.getMessage());
		}
	}

}
