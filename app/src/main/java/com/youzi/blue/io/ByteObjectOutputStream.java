package com.youzi.blue.io;



import com.youzi.blue.utils.CRC;
import com.youzi.blue.utils.CombineValue;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ByteObjectOutputStream {
    byte breaking;
    OutputStream outputStream;
    BufferedOutputStream bufferedOutputStream;

    public ByteObjectOutputStream(OutputStream o){
        this(o, (byte) 0x8f);
    }

    public ByteObjectOutputStream(OutputStream outputStream, byte breaking){
        this.breaking=breaking;
        this.outputStream=outputStream;
        bufferedOutputStream = new BufferedOutputStream(outputStream);
    }

    public void writeObject(byte[] bytes) throws IOException {
        writeObject(bytes,0,bytes.length);
    }

    public void writeObject(byte[] bytes ,int offset, int len) throws IOException {
        byte[] crctmp = new byte[len];
        System.arraycopy(bytes,offset,crctmp,0,len);

        int crcCode = CRC.getIntCRC(crctmp);
        crctmp=new byte[len+4];
        System.arraycopy(CombineValue.intToByte(crcCode),0,crctmp,0,4);
        System.arraycopy(bytes,offset,crctmp,4,len);
        for (int i = 0;i<len+4;i++){
            if (crctmp[i]==breaking){
                bufferedOutputStream.write(breaking);
                bufferedOutputStream.write(breaking);
            }else {
                bufferedOutputStream.write(crctmp[i]);
            }
        }
        bufferedOutputStream.write(breaking);
        bufferedOutputStream.write(0);
        bufferedOutputStream.flush();
    }
    public void close() throws IOException {
        bufferedOutputStream.close();
        bufferedOutputStream=null;
        outputStream=null;
    }
}
