package llc.ufwa.data.resource.linear;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;

import llc.ufwa.data.exception.LinearStreamException;

public class ByteArrayWriter implements LinearStreamWriter {

	private ByteBuffer bufferMainArray;
	private final int bufferSize;

	public ByteArrayWriter(final int originalSize, final int bufferSize) {
		this.bufferSize = bufferSize;
		this.bufferMainArray =  ByteBuffer.allocate(originalSize);
	}

	@Override
	public long length() {
		return bufferMainArray.capacity();
	}

	@Override
	public byte[] read(int index) throws LinearStreamException {

		while ((index + bufferSize) > length()) {
			expandBuffer();
		}

		bufferMainArray.position(index);

		final byte[] buff = new byte[bufferSize];

		try {
			bufferMainArray.get(buff, 0, buff.length);
		}
		catch (BufferUnderflowException e) {
			throw new LinearStreamException(e);
		}
		catch (IndexOutOfBoundsException e) {
			throw new LinearStreamException(e);
		}

		return buff;

	}

	private void expandBuffer() {

		final ByteBuffer clone = ByteBuffer.allocate(bufferMainArray.capacity() * 2);
		bufferMainArray.rewind();
		clone.put(bufferMainArray);
		bufferMainArray = clone;

	}

	@Override
	public void write(int index, byte[] in) throws LinearStreamException {

		while ((index + in.length) > length()) {
			expandBuffer();
		}

		try {
			bufferMainArray.position(index);
			bufferMainArray.put(in, 0, in.length);
		}
		catch (BufferOverflowException e) {
			throw new LinearStreamException(e);
		}
		catch (IndexOutOfBoundsException e) {
			throw new LinearStreamException(e);
		}
		catch (ReadOnlyBufferException e) {
			throw new LinearStreamException(e);
		}

	}
}
