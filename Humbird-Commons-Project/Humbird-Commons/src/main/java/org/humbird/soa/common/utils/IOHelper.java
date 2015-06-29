package org.humbird.soa.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

/**
 * IO helper class.
 *
 * @version 
 */
public final class IOHelper {

    public static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    private static final Logger LOG = LoggerFactory.getLogger(IOHelper.class);
    private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");

    private IOHelper() {
        // Utility Class
    }
    
    /**
     * Use this function instead of new String(byte[]) to avoid surprises from non-standard default encodings.
     */
    public static String newStringFromBytes(byte[] bytes) {
        try {
            return new String(bytes, UTF8_CHARSET.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Impossible failure: Charset.forName(\"UTF-8\") returns invalid name.", e);
        }
    }

    /**
     * Use this function instead of new String(byte[], int, int) 
     * to avoid surprises from non-standard default encodings.
     */
    public static String newStringFromBytes(byte[] bytes, int start, int length) {
        try {
            return new String(bytes, start, length, UTF8_CHARSET.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Impossible failure: Charset.forName(\"UTF-8\") returns invalid name.", e);
        }
    }

    /**
     * Wraps the passed <code>in</code> into a {@link java.io.BufferedInputStream}
     * object and returns that. If the passed <code>in</code> is already an
     * instance of {@link java.io.BufferedInputStream} returns the same passed
     * <code>in</code> reference as is (avoiding double wrapping).
     * 
     * @param in the wrapee to be used for the buffering support
     * @return the passed <code>in</code> decorated through a
     *         {@link java.io.BufferedInputStream} object as wrapper
     */
    public static BufferedInputStream buffered(InputStream in) {
        ObjectHelper.notNull(in, "in");
        return (in instanceof BufferedInputStream) ? (BufferedInputStream)in : new BufferedInputStream(in);
    }

    /**
     * Wraps the passed <code>out</code> into a {@link java.io.BufferedOutputStream}
     * object and returns that. If the passed <code>out</code> is already an
     * instance of {@link java.io.BufferedOutputStream} returns the same passed
     * <code>out</code> reference as is (avoiding double wrapping).
     * 
     * @param out the wrapee to be used for the buffering support
     * @return the passed <code>out</code> decorated through a
     *         {@link java.io.BufferedOutputStream} object as wrapper
     */
    public static BufferedOutputStream buffered(OutputStream out) {
        ObjectHelper.notNull(out, "out");
        return (out instanceof BufferedOutputStream) ? (BufferedOutputStream)out : new BufferedOutputStream(out);
    }

    /**
     * Wraps the passed <code>reader</code> into a {@link java.io.BufferedReader} object
     * and returns that. If the passed <code>reader</code> is already an
     * instance of {@link java.io.BufferedReader} returns the same passed
     * <code>reader</code> reference as is (avoiding double wrapping).
     * 
     * @param reader the wrapee to be used for the buffering support
     * @return the passed <code>reader</code> decorated through a
     *         {@link java.io.BufferedReader} object as wrapper
     */
    public static BufferedReader buffered(Reader reader) {
        ObjectHelper.notNull(reader, "reader");
        return (reader instanceof BufferedReader) ? (BufferedReader)reader : new BufferedReader(reader);
    }

    /**
     * Wraps the passed <code>writer</code> into a {@link java.io.BufferedWriter} object
     * and returns that. If the passed <code>writer</code> is already an
     * instance of {@link java.io.BufferedWriter} returns the same passed
     * <code>writer</code> reference as is (avoiding double wrapping).
     * 
     * @param writer the wrapee to be used for the buffering support
     * @return the passed <code>writer</code> decorated through a
     *         {@link java.io.BufferedWriter} object as wrapper
     */
    public static BufferedWriter buffered(Writer writer) {
        ObjectHelper.notNull(writer, "writer");
        return (writer instanceof BufferedWriter) ? (BufferedWriter)writer : new BufferedWriter(writer);
    }

    /**
     * A factory method which creates an {@link java.io.IOException} from the given
     * exception and message
     *
     * @deprecated IOException support nested exception in Java 1.6. Will be removed in Camel 3.0
     */
    @Deprecated
    public static IOException createIOException(Throwable cause) {
        return createIOException(cause.getMessage(), cause);
    }

    /**
     * A factory method which creates an {@link java.io.IOException} from the given
     * exception and message
     *
     * @deprecated IOException support nested exception in Java 1.6. Will be removed in Camel 3.0
     */
    @Deprecated
    public static IOException createIOException(String message, Throwable cause) {
        IOException answer = new IOException(message);
        answer.initCause(cause);
        return answer;
    }

    public static int copy(InputStream input, OutputStream output) throws IOException {
        return copy(input, output, DEFAULT_BUFFER_SIZE);
    }

    public static int copy(final InputStream input, final OutputStream output, int bufferSize) throws IOException {
        return copy(input, output, bufferSize, false);
    }

    public static int copy(final InputStream input, final OutputStream output, int bufferSize, boolean flushOnEachWrite) throws IOException {
        if (input instanceof ByteArrayInputStream) {
            // optimized for byte array as we only need the max size it can be
            input.mark(0);
            input.reset();
            bufferSize = input.available();
        } else {
            int avail = input.available();
            if (avail > bufferSize) {
                bufferSize = avail;
            }
        }

        if (bufferSize > 262144) {
            // upper cap to avoid buffers too big
            bufferSize = 262144;
        }

        if (LOG.isTraceEnabled()) {
            LOG.trace("Copying InputStream: {} -> OutputStream: {} with buffer: {} and flush on each write {}",
                    new Object[]{input, output, bufferSize, flushOnEachWrite});
        }

        final byte[] buffer = new byte[bufferSize];
        int n = input.read(buffer);
        int total = 0;
        while (-1 != n) {
            output.write(buffer, 0, n);
            if (flushOnEachWrite) {
                output.flush();
            }
            total += n;
            n = input.read(buffer);
        }
        if (!flushOnEachWrite) {
            // flush at end, if we didn't do it during the writing
            output.flush();
        }
        return total;
    }
    
    public static void copyAndCloseInput(InputStream input, OutputStream output) throws IOException {
        copyAndCloseInput(input, output, DEFAULT_BUFFER_SIZE);
    }
    
    public static void copyAndCloseInput(InputStream input, OutputStream output, int bufferSize) throws IOException {
        copy(input, output, bufferSize);
        close(input, null, LOG);
    }

    public static int copy(final Reader input, final Writer output, int bufferSize) throws IOException {
        final char[] buffer = new char[bufferSize];
        int n = input.read(buffer);
        int total = 0;
        while (-1 != n) {
            output.write(buffer, 0, n);
            total += n;
            n = input.read(buffer);
        }
        output.flush();
        return total;
    }

    /**
     * Forces any updates to this channel's file to be written to the storage device that contains it.
     *
     * @param channel the file channel
     * @param name the name of the resource
     * @param log the log to use when reporting warnings, will use this class's own {@link org.slf4j.Logger} if <tt>log == null</tt>
     */
    public static void force(FileChannel channel, String name, Logger log) {
        try {
            if (channel != null) {
                channel.force(true);
            }
        } catch (Exception e) {
            if (log == null) {
                // then fallback to use the own Logger
                log = LOG;
            }
            if (name != null) {
                log.warn("Cannot force FileChannel: " + name + ". Reason: " + e.getMessage(), e);
            } else {
                log.warn("Cannot force FileChannel. Reason: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Forces any updates to a FileOutputStream be written to the storage device that contains it.
     *
     * @param os the file output stream
     * @param name the name of the resource
     * @param log the log to use when reporting warnings, will use this class's own {@link org.slf4j.Logger} if <tt>log == null</tt>
     */
    public static void force(FileOutputStream os, String name, Logger log) {
        try {
            if (os != null) {
                os.getFD().sync();
            }
        } catch (Exception e) {
            if (log == null) {
                // then fallback to use the own Logger
                log = LOG;
            }
            if (name != null) {
                log.warn("Cannot sync FileDescriptor: " + name + ". Reason: " + e.getMessage(), e);
            } else {
                log.warn("Cannot sync FileDescriptor. Reason: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Closes the given writer, logging any closing exceptions to the given log.
     * An associated FileOutputStream can optionally be forced to disk.
     *
     * @param writer the writer to close
     * @param os an underlying FileOutputStream that will to be forced to disk according to the the force parameter
     * @param name the name of the resource
     * @param log the log to use when reporting warnings, will use this class's own {@link org.slf4j.Logger} if <tt>log == null</tt>
     * @param force forces the FileOutputStream to disk
     */
    public static void close(Writer writer, FileOutputStream os, String name, Logger log, boolean force) {
        if (writer != null && force) {
            // flush the writer prior to syncing the FD
            try {
                writer.flush();
            } catch (Exception e) {
                if (log == null) {
                    // then fallback to use the own Logger
                    log = LOG;
                }
                if (name != null) {
                    log.warn("Cannot flush Writer: " + name + ". Reason: " + e.getMessage(), e);
                } else {
                    log.warn("Cannot flush Writer. Reason: " + e.getMessage(), e);
                }
            }
            force(os, name, log);
        }
        close(writer, name, log);
    }

    /**
     * Closes the given resource if it is available, logging any closing exceptions to the given log.
     *
     * @param closeable the object to close
     * @param name the name of the resource
     * @param log the log to use when reporting closure warnings, will use this class's own {@link org.slf4j.Logger} if <tt>log == null</tt>
     */
    public static void close(Closeable closeable, String name, Logger log) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                if (log == null) {
                    // then fallback to use the own Logger
                    log = LOG;
                }
                if (name != null) {
                    log.warn("Cannot close: " + name + ". Reason: " + e.getMessage(), e);
                } else {
                    log.warn("Cannot close. Reason: " + e.getMessage(), e);
                }
            }
        }
    }
    
    /**
     * Closes the given resource if it is available and don't catch the exception
     *
     * @param closeable the object to close
     * @throws java.io.IOException
      */
    public static void closeWithException(Closeable closeable) throws IOException {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                // don't catch the exception here
                throw e;
            }
        }
    }

    /**
     * Closes the given channel if it is available, logging any closing exceptions to the given log.
     * The file's channel can optionally be forced to disk.
     *
     * @param channel the file channel
     * @param name the name of the resource
     * @param log the log to use when reporting warnings, will use this class's own {@link org.slf4j.Logger} if <tt>log == null</tt>
     * @param force forces the file channel to disk
     */
    public static void close(FileChannel channel, String name, Logger log, boolean force) {
        if (force) {
            force(channel, name, log);
        }
        close(channel, name, log);
    }

    /**
     * Closes the given resource if it is available.
     *
     * @param closeable the object to close
     * @param name the name of the resource
     */
    public static void close(Closeable closeable, String name) {
        close(closeable, name, LOG);
    }

    /**
     * Closes the given resource if it is available.
     *
     * @param closeable the object to close
     */
    public static void close(Closeable closeable) {
        close(closeable, null, LOG);
    }

    /**
     * Closes the given resources if they are available.
     * 
     * @param closeables the objects to close
     */
    public static void close(Closeable... closeables) {
        for (Closeable closeable : closeables) {
            close(closeable);
        }
    }

    public static void validateCharset(String charset) throws UnsupportedCharsetException {
        if (charset != null) {
            if (Charset.isSupported(charset)) {
                Charset.forName(charset);
                return;
            }
        }
        throw new UnsupportedCharsetException(charset);
    }

    /**
     * This method will take off the quotes and double quotes of the charset
     */
    public static String normalizeCharset(String charset) {
        if (charset != null) {
            String answer = charset.trim();
            if (answer.startsWith("'") || answer.startsWith("\"")) {
                answer = answer.substring(1);
            }
            if (answer.endsWith("'") || answer.endsWith("\"")) {
                answer = answer.substring(0, answer.length() - 1);
            }
            return answer.trim();
        } else {
            return null;
        }
    }

    /**
     */
    public static String getCharsetName() {
        return getCharsetName(true);
    }

    /**
     * Gets the charset name if set as header or property {@link }.
     * <b>Notice:</b> The lookup from the header has priority over the property.
     *
     * @param useDefault should we fallback and use JVM default charset if no property existed?
     * @return the charset, or <tt>null</tt> if no found
     */
    public static String getCharsetName(boolean useDefault) {
        if (useDefault) {
            return getDefaultCharsetName();
        } else {
            return null;
        }
    }
    
    private static String getDefaultCharsetName() {
        return ObjectHelper.getSystemProperty("UTF-8", "UTF-8");
    }

    /**
     * Loads the entire stream into memory as a String and returns it.
     * <p/>
     * <b>Notice:</b> This implementation appends a <tt>\n</tt> as line
     * terminator at the of the text.
     * <p/>
     * Warning, don't use for crazy big streams :)
     */
    public static String loadText(InputStream in) throws IOException {
        StringBuilder builder = new StringBuilder();
        InputStreamReader isr = new InputStreamReader(in);
        try {
            BufferedReader reader = buffered(isr);
            while (true) {
                String line = reader.readLine();
                if (line != null) {
                    builder.append(line);
                    builder.append("\n");
                } else {
                    break;
                }
            }
            return builder.toString();
        } finally {
            close(isr, in);
        }
    }
    
    /**
     * Get the charset name from the content type string
     * @param contentType
     * @return the charset name, or <tt>UTF-8</tt> if no found
     */
    public static String getCharsetNameFromContentType(String contentType) {
        String[] values = contentType.split(";"); 
        String charset = "";

        for (String value : values) {
            value = value.trim();
            if (value.toLowerCase().startsWith("charset=")) {
                // Take the charset name
                charset = value.substring(8);
            }
        }
        if ("".equals(charset)) {
            charset = "UTF-8"; 
        }
        return IOHelper.normalizeCharset(charset);

    }
}
