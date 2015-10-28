package styczynski.weblogic.jdbc.debug.report;

import java.util.Arrays;

public class ExecutionHistogram {


    private int bucketCnt;
    private int bucketSize;
    private int slotMax;
    
    private long[] histogram;
    private long min = Long.MAX_VALUE;
    private long max = Long.MIN_VALUE;
    private long cnt = 0;
    private double sum = 0;
    private long avg = 0;
    private long maxBar = Long.MIN_VALUE;

    public ExecutionHistogram(int slots, int slotMax) {

        this.histogram = new long[slots];
        init();
        
        this.bucketCnt = slots;
        this.bucketSize = slotMax / slots;
        this.slotMax = slotMax;

    }
    
    public ExecutionHistogram(ExecutionHistogram other){

        this.histogram = new long[other.bucketCnt];
        init();
        
        this.bucketCnt = other.bucketCnt;
        this.bucketSize = other.bucketSize;
        this.slotMax = other.slotMax;
        this.min = other.min;
        this.max = other.max;
        this.avg = other.avg;
        this.cnt = other.cnt;
        this.sum = other.sum;
        this.maxBar = other.maxBar;
        
        for(int slot=0; slot<other.bucketCnt; slot++) {
            this.histogram[slot] = other.histogram[slot];
        }
        
    }

    public void init() {
        min = Long.MAX_VALUE;
        max = Long.MIN_VALUE;
        cnt = 0;
        sum = 0;
        avg = 0;
        maxBar = 0;
        for (int i = 0; i < this.bucketCnt; i++)
            this.histogram[i] = 0;
    }

    public void add(long measurement) {
        
        int bucketNo = (int) measurement / bucketSize;
        
        if (bucketNo >= bucketCnt)
            bucketNo = bucketCnt - 1;

        this.histogram[bucketNo]++;
        
        if (this.histogram[bucketNo] > maxBar)
            maxBar = this.histogram[bucketNo];

        if (measurement < min) min = measurement;
        if (measurement > max) max = measurement;

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

    public String getASCIIChart(int height, int scalingFactor, 
                                String endLine, String mark, String markSmall, String empty) {

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

        //TODO Debug
        //result.append("maxBar=" + maxBar + endLine);
        //result.append("magnify=" + magnify + endLine);
        //result.append("scalingFactor=" + scalingFactor + endLine);
        long value;
        long barHeight;
        
        //TODO it have to be parametrized
        final int yLabelShift = 5;
        final int yLabelDensity = 2;
        final int xLabelDensity = 25;
        int yLabel = 0;
        int yLabelLength = 0;
        for (int y = height; y > 0; y--) {
            
            if ( y % yLabelDensity == 0) {
                //print y axis scaling information
                if(magnify)
                    yLabel = y / scalingFactor;
                else
                    yLabel = y * scalingFactor;
                
                //mesuring len is slow - does matter here. it's UI
                yLabelLength = String.valueOf(yLabel).length();
                
                for(int i=0; i< yLabelShift - yLabelLength; i++) result.append(" ");
                
                result.append(yLabel);
                result.append(" ");
            } else {
                for(int i=0; i<= yLabelShift; i++)
                    result.append(" ");
            }
            result.append("|");
                
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

        

        //add X axis markers
        for(int i=0; i<= yLabelShift; i++) result.append(" "); //shift to X axis start
        for (int col = 0; col < this.bucketCnt; col++) {
            if ( col % xLabelDensity == 0) {
                result.append("|");
            } else {
                result.append("-");
            }
        }
        result.append("|");
        result.append(endLine);
        
        //add X axis labels
        for(int i=0; i<= yLabelShift; i++) result.append(" "); //shift to X axis start
        int compensateNextTime = 0;
        int xLabel = 0;
        int xLabelLength = 0;
        int col = 0;
        for (col = 0; col < this.bucketCnt; col++) {
            if ( col % xLabelDensity == 0) {
                
                xLabel = col * bucketSize;
                //mesuring len is slow - does matter here. it's UI
                xLabelLength = String.valueOf(xLabel).length();
                
                result.replace(result.length()-(xLabelLength/2) - compensateNextTime, result.length(), String.valueOf(xLabel));
                compensateNextTime = xLabelLength/2;
                
                result.append(" ");
            } else {
                result.append(" ");
            }
        }
        //put last label
        xLabel = col * bucketSize;
        xLabelLength = String.valueOf(xLabel).length();
        result.replace(result.length()-(xLabelLength/2) - compensateNextTime, result.length(), String.valueOf(xLabel));
        
        return result.toString();
    }

    public void merge(ExecutionHistogram other){
        //check if both are compatible
        if (other.bucketCnt != this.bucketCnt) {
            throw new RuntimeException("Cannot merge histogram with different bucket count!");
        }
        if (other.bucketSize != this.bucketSize) {
            throw new RuntimeException("Cannot merge histogram with different bucket size!");
        }
     
        //maxBar - maximum bar seen in histogram. After merge it is sum of both 
        this.maxBar = this.maxBar + other.maxBar;
        
        //merge min, max, avg
        if (other.min < this.min) this.min = other.min;  
        if (other.max > this.max) this.max = other.max;  
        this.avg = (this.avg + other.avg) / 2;
        
        //merge cnt, sum
        this.sum = this.sum + other.sum;
        this.cnt = this.cnt + other.cnt;
        
        //merge histograms
        for(int slot=0; slot<other.bucketCnt; slot++) {
            this.histogram[slot] = this.histogram[slot] + other.histogram[slot];
        }
    }


    @Override
    protected Object clone() throws CloneNotSupportedException {
        ExecutionHistogram histogram = new ExecutionHistogram(this.bucketCnt, this.slotMax);
        histogram.min = this.min;
        histogram.max = this.max;
        histogram.avg = this.avg;
        histogram.cnt = this.cnt;
        histogram.sum = this.sum;
        histogram.maxBar = this.maxBar;
        
        for(int slot=0; slot<this.bucketCnt; slot++) {
            histogram.histogram[slot] = this.histogram[slot];
        }
        
        return histogram;
    }

    @Override
    public boolean equals(Object obj) {
        if ( !(obj instanceof ExecutionHistogram) ) return false;
        if ( this.bucketSize != ((ExecutionHistogram)obj).bucketSize ) return false;
        if ( this.slotMax != ((ExecutionHistogram)obj).slotMax ) return false;
        if ( this.maxBar != ((ExecutionHistogram)obj).maxBar ) return false;
        
        if ( this.min != ((ExecutionHistogram)obj).min ) return false;
        if ( this.max != ((ExecutionHistogram)obj).max ) return false;
        if ( this.avg != ((ExecutionHistogram)obj).avg ) return false;
        if ( this.sum != ((ExecutionHistogram)obj).sum ) return false;
        if ( this.cnt != ((ExecutionHistogram)obj).cnt ) return false;
        
        for(int slot=0; slot<this.bucketCnt; slot++) {
            if ( this.histogram[slot] != ((ExecutionHistogram)obj).histogram[slot] ) return false;
      
        }
        
        return true;
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
        ExecutionHistogram histogram = new ExecutionHistogram(20, 1000);
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


        ExecutionHistogram histogram2 = new ExecutionHistogram(20, 1000);
        histogram2.add(10);
        histogram2.add(10);
        histogram2.add(10);

        histogram2.add(100);
        histogram2.add(100);

        histogram2.add(900);
        histogram2.add(900);
        histogram2.add(900);
        histogram2.add(900);
        System.out.println(Arrays.toString(histogram2.getHistogram()));    
        
        System.out.println("Last two are the same:" + histogram.equals(histogram2));
        
        histogram.merge(histogram2);
        System.out.println(histogram);
        System.out.println(histogram2);

        try {
            ExecutionHistogram histogram3 = (ExecutionHistogram) histogram.clone();
            System.out.println(histogram3);
        } catch (CloneNotSupportedException e) {
            System.out.println("Error: cannot clone for some reason!");
        }


    }
}
