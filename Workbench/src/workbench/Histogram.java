package workbench;

import java.util.Arrays;

public class Histogram {

    private int bucketCnt;
    private int bucketSize;
    private long[] histogram;
    private long min = Long.MAX_VALUE;
    private long max = Long.MIN_VALUE;
    private long cnt = 0;
    private double sum = 0;
    private long avg = 0;
    private long maxBar = Long.MIN_VALUE;

    public Histogram(int slots, int slotMax) {
        this.bucketCnt = slots;
        this.bucketSize = slotMax / slots;
        this.histogram = new long[slots];
        init();
    }

    public void init() {
        min = Long.MAX_VALUE;
        max = Long.MIN_VALUE;
        cnt = 0;
        sum = 0;
        avg = 0;
        maxBar=0;
        for (int i = 0; i < this.bucketCnt; i++)
            this.histogram[i] = 0;
    }

    public void add(long measurement) {
        int bucket = (int) measurement / bucketSize;
        if (bucket >= bucketCnt)
            bucket = bucketCnt - 1;

        this.histogram[bucket]++;
        if (this.histogram[bucket] > maxBar)
            maxBar = this.histogram[bucket];

        if (measurement < min)
            min = measurement;
        if (measurement > max)
            max = measurement;

        cnt++;

        sum += measurement;
        avg = (long) (sum / cnt);

    }

    public long[] getHistogram() {
        return histogram;
    }

    public int getBucketCnt() {
        return bucketCnt;
    }

    public int getBucketSize() {
        return bucketSize;
    }

    public long getMin() {
        return min;
    }

    public long getMax() {
        return max;
    }

    public long getCnt() {
        return cnt;
    }

    public double getSum() {
        return avg * cnt;
    }

    public long getAvg() {
        return avg;
    }


    public long getMaxBar() {
        return maxBar;
    }


    public String getASCIIChart() {
        return getASCIIChart(10, 0, "\n", "X", "x", "-");
    }
    
    public String getASCIIChart(int height) {
        return getASCIIChart(height, 0, "\n", "X", "x", "-");
    }

    public String getASCIIChart(int height, int scalingFactor, String endLine, String mark, String markSmall,
                                String empty) {


        StringBuffer result = new StringBuffer();
        boolean magnify = false;

        if (scalingFactor == 0) {
            if (maxBar > height) {
                scalingFactor = (int) (this.maxBar / height);
                magnify = false;
            } else {
                scalingFactor = (int) (height / this.maxBar);
                magnify = true;
            }
        }

        result.append("maxBar=" + maxBar + endLine);
        result.append("magnify=" + magnify + endLine);
        result.append("scalingFactor=" + scalingFactor + endLine);
        long value;
        long barHeight;
        for (int y = height; y > 0; y--) {
            for (int i = 0; i < this.bucketCnt; i++) {
                value = this.histogram[i];
                barHeight = magnify ? value * scalingFactor : value / scalingFactor;

                if (barHeight >= y) {
                    result.append(mark);
                } else {
                    if (y > 1)
                        result.append(empty);
                    else if (value > 0)
                        result.append(markSmall);
                    else
                        result.append(empty);
                }
            }
            result.append(endLine);
        }

        return result.toString();
    }

    public String toString() {
        StringBuffer result = new StringBuffer();

        result.append("[bucketCnt=" + bucketCnt);
        result.append(", bucketSize=" + bucketSize);
        result.append(", min=" + min);
        result.append(", max=" + max);
        result.append(", cnt=" + cnt);
        result.append(", sum=" + sum);
        result.append(", avg=" + avg);
        result.append(", histogram=" + Arrays.toString(histogram));
        result.append("]");

        return result.toString();
    }


    public static void main(String[] args) {
        Histogram histogram = new Histogram(20, 1000);
        histogram.add(101);
        histogram.add(10);
        histogram.add(501);
        histogram.add(19);
        histogram.add(401);
        histogram.add(1000);

        System.out.println(histogram);

        histogram.init();


        histogram.init();
        for (int i = 0; i < 70; i++)
            histogram.add(5);

        for (int i = 0; i < 30; i++)
            histogram.add(10);

        for (int i = 0; i < 60; i++)
            histogram.add(30);
        histogram.add(10);
        histogram.add(10);
        histogram.add(500);
        for (int i = 0; i < 150; i++)
            histogram.add(530);
        
        histogram.add(540);
        histogram.add(101);
        
        for (int i = 0; i < 130; i++)
            histogram.add(1000);
        
        System.out.println(histogram);

        System.out.println(histogram.getASCIIChart());
        System.out.println(Arrays.toString(histogram.getHistogram()));

        histogram.init();
        histogram.add(10);
        histogram.add(10);
        histogram.add(10);
        
        histogram.add(100);
        histogram.add(100);

        histogram.add(900);
        histogram.add(900);
        histogram.add(900);
        histogram.add(900);
        
        System.out.println(histogram);

        System.out.println(histogram.getASCIIChart(5));
        System.out.println(histogram.getASCIIChart(10));
        System.out.println(Arrays.toString(histogram.getHistogram()));


    }
}
