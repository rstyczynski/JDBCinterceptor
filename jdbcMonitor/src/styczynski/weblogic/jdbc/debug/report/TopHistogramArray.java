package styczynski.weblogic.jdbc.debug.report;

import styczynski.weblogic.jdbc.monitor.CFG;

/*
 * Based on TopAlertsArray - look there for details.
 */
public class TopHistogramArray {
    private int maxSize = CFG.getTopHistogramsToStore();
    private ExecutionHistogram[] histogramList;
    private int cnt = 0;

    public TopHistogramArray() {
        histogramList = new ExecutionHistogram[maxSize];
    }

    public TopHistogramArray(int _size) {
        this.maxSize = _size;
        histogramList = new ExecutionHistogram[maxSize];
    }

    public void addHistogram(ExecutionHistogram histogram) {
        //cnt counts in loop from 1 to maxSize
        if (cnt++ == maxSize)
            cnt = 1;

        histogramList[cnt % maxSize] = histogram;

        //System.out.println("cnt=" + cnt + ", cnt % maxSize=" + (cnt % maxSize));

    }

    public ExecutionHistogram[] getHistograms() {
        return histogramList;
    }

    public static void main(String[] args) {
        TopHistogramArray histogramList = new TopHistogramArray();

        histogramList.addHistogram(new ExecutionHistogram(5, 1000, "of 5"));
        histogramList.addHistogram(new ExecutionHistogram(10, 1000, "of 10"));
        histogramList.addHistogram(new ExecutionHistogram(15, 1000, "of 15"));
        histogramList.addHistogram(new ExecutionHistogram(20, 1000, "of 20"));
        histogramList.addHistogram(new ExecutionHistogram(25, 1000, "of 25"));

        for (int i = 0; i < histogramList.maxSize; i++) {
            ExecutionHistogram alert = (ExecutionHistogram) histogramList.getHistograms()[i];
            System.out.println(alert);
        }

        System.out.println("Adding one more");
        histogramList.addHistogram(new ExecutionHistogram(30, 1000, "of 30"));

        for (int i = 0; i < histogramList.maxSize; i++) {
            ExecutionHistogram histogram = (ExecutionHistogram) histogramList.getHistograms()[i];
            System.out.println(histogram);
        }


        System.out.println("Adding one more");
        histogramList.addHistogram(new ExecutionHistogram(35, 1000, "of 35"));

        for (int i = 0; i < histogramList.maxSize; i++) {
            ExecutionHistogram histogram = (ExecutionHistogram) histogramList.getHistograms()[i];
            System.out.println(histogram);
        }

        System.out.println("Adding one more");
        histogramList.addHistogram(new ExecutionHistogram(40, 1000, "of 40"));

        for (int i = 0; i < histogramList.maxSize; i++) {
            ExecutionHistogram histogram = (ExecutionHistogram) histogramList.getHistograms()[i];
            System.out.println(histogram);
        }

        System.out.println("Adding one more");
        histogramList.addHistogram(new ExecutionHistogram(45, 1000, "of 45"));

        for (int i = 0; i < histogramList.maxSize; i++) {
            ExecutionHistogram histogram = (ExecutionHistogram) histogramList.getHistograms()[i];
            System.out.println(histogram);
        }


        System.out.println("Adding one more");
        histogramList.addHistogram(new ExecutionHistogram(50, 1000, "of 50"));

        for (int i = 0; i < histogramList.maxSize; i++) {
            ExecutionHistogram histogram = (ExecutionHistogram) histogramList.getHistograms()[i];
            System.out.println(histogram);
        }


        System.out.println("Adding one more");
        histogramList.addHistogram(new ExecutionHistogram(55, 1000, "of 55"));

        for (int i = 0; i < histogramList.maxSize; i++) {
            ExecutionHistogram histogram = (ExecutionHistogram) histogramList.getHistograms()[i];
            System.out.println(histogram);
        }


        System.out.println("Adding one more");
        histogramList.addHistogram(new ExecutionHistogram(60, 1000, "of 60"));

        for (int i = 0; i < histogramList.maxSize; i++) {
            ExecutionHistogram histogram = (ExecutionHistogram) histogramList.getHistograms()[i];
            System.out.println(histogram);
        }


        System.out.println("Adding one more");
        histogramList.addHistogram(new ExecutionHistogram(65, 1000, "of 65"));

        for (int i = 0; i < histogramList.maxSize; i++) {
            ExecutionHistogram histogram = (ExecutionHistogram) histogramList.getHistograms()[i];
            System.out.println(histogram);
        }
    }


}
