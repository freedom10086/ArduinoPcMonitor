package me.yluo.androidble;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SendData {
    public DataType type;
    private List<byte[]> datas;
    private int currentPosition = 0;
    private static final int PACKET_SIZE = 20;

    public SendData(DataType type, String data) {
        this.type = type;
        currentPosition = 0;
        byte[] bs = data.getBytes();

        int times = (int) Math.ceil(bs.length / (double) PACKET_SIZE);
        datas = new ArrayList<>(times);

        for (int i = 0; i < times; i++) {
            int end = PACKET_SIZE * (i + 1);
            if (end > bs.length) {
                end = bs.length;
            }
            datas.add(Arrays.copyOfRange(bs, PACKET_SIZE * i, end));
        }
    }

    public boolean haveMoreData() {
        return currentPosition < datas.size();
    }

    public byte[] getData() {
        if (!haveMoreData()) {
            throw new IllegalStateException("已经没有数据了");
        }

        byte[] b = datas.get(currentPosition);
        currentPosition++;
        return b;
    }
}
