package cn.huoxian.iast.api;


import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author owefsad
 */
public class WrapperOutputStreamCopier extends ServletOutputStream {

    private final OutputStream out;
    private final ByteArrayOutputStream copier;

    WrapperOutputStreamCopier(OutputStream out) {
        this.out = out;
        this.copier = new ByteArrayOutputStream();
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
        copier.write(b);
    }

    byte[] getCopy() {
        return copier.toByteArray();
    }

    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {

    }
}
