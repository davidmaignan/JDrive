package inf5171.stats;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by david on 2016-12-14.
 */
public class Report {
    private String[] methods;

    private final Map<String, List<Measure>> measures;
    private final int width = 640;
    private final int height = 480;

    public Report(Map<String, List<Measure>> measures, String[] methods) {
        this.measures = measures;
        this.methods = methods;
    }

    public String printStatistic(){

        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%15s|", "nbThreads"));
        builder.append(String.format("%15s|", "nbFiles"));
        builder.append(String.format("%15s|", "nbNodes"));
        builder.append(String.format("%15s|", "Tps (secs)"));
        builder.append(String.format("%15s|", "Acceleration"));
        builder.append("\n");

        for (int i = 0; i < methods.length; i++) {
            builder.append(methods[i]);
            builder.append("\n");
            for (Measure stat: measures.get(methods[i])) {

                double averageSequential = getAverageSequentialByStage(stat.getTotalFiles(), 0);

                builder.append(String.format("%15s|", String.valueOf(stat.getNbThreads())));
                builder.append(String.format("%15s|", String.valueOf(stat.getTotalFiles())));
                builder.append(String.format("%15s|", String.valueOf(stat.getTotalNodes())));
                builder.append(String.format("%15s|", String.valueOf(stat.getFormattedSeconds(0))));
                builder.append(String.format("%15s|", String.valueOf(stat.getFormattedAcc(0, averageSequential))));
                builder.append("\n");
            }
        }

        return builder.toString();
    }

    public List<Integer> getListOfTotalFiles(){
        return measures.get(methods[0]).stream().map(s -> s.getTotalFiles()).distinct().collect(Collectors.toList());
    }

    public List<Integer> getListOfNbThreads(){
        return measures.get(methods[1]).stream().map(s -> s.getNbThreads()).distinct().collect(Collectors.toList());
    }

    public Double getAverageSequentialByStage(int totalFiles, int stage){
        return measures.get(methods[0]).stream()
                .filter(s -> s.getTotalFiles() == totalFiles)
                .map(s -> s.getSeconds(stage)).mapToDouble(Double::new).average().getAsDouble();
    }

    public List<Measure> getListMeasuresByTotalFiles(int key, int totalFiles){
        return measures.get(methods[key]).stream()
                .filter(s -> s.getTotalFiles() == totalFiles )
                .sorted((s1, s2) -> Integer.compare(s1.getNbThreads(), s2.getNbThreads()))
                .collect(Collectors.toList());
    }

    public void generateCharts(){
        getListOfTotalFiles().stream().forEach(
                s -> generateChart(s)
        );

        getListOfTotalFiles().stream().forEach(
                s -> generateAccelerationCharts(s)
        );
    }

    public void generateAccelerationCharts(int totalFiles){
        XYSeries sequential = new XYSeries("Sequential");
        List<Integer> nbThreads= getListOfNbThreads();
        Double seqAvg = getAverageSequentialByStage(totalFiles, 0);

        for (int i = 0; i < nbThreads.size(); i++) {
            sequential.add(nbThreads.get(i), seqAvg);
        }

        XYSeriesCollection dataset = new XYSeriesCollection();

        dataset.addSeries(sequential);
        dataset.addSeries(getSerie(1, totalFiles, seqAvg));
        dataset.addSeries(getSerie(2, totalFiles, seqAvg));

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Nombre de fichiers: " + totalFiles,
                "Nombre de threads",
                "Temps (seconds)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                false,
                false
        );

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(new Color(255, 255, 0xFF));
        plot.setDomainGridlinePaint(new Color(207, 215, 0xff));
        plot.setRangeGridlinePaint(new Color(0, 144, 255));

        chart.getXYPlot().getRangeAxis().setAutoRange(true);

        String reportName =  "acc_report_totalFile_" + totalFiles + ".jpg";
        File file = new File(reportName);

        try {
            ChartUtilities.saveChartAsJPEG(file, chart, width, height);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void generateChart(int totalFiles) {
        XYSeries sequential = new XYSeries("Sequential");

        List<Integer> nbThreads= getListOfNbThreads();

        Double seqAvg = getAverageSequentialByStage(totalFiles, 0);

        for (int i = 0; i < nbThreads.size(); i++) {
            sequential.add(nbThreads.get(i), seqAvg);
        }

        XYSeriesCollection dataset = new XYSeriesCollection();

        dataset.addSeries(sequential);
        dataset.addSeries(getSerie(1, totalFiles));
        dataset.addSeries(getSerie(2, totalFiles));

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Nombre de fichiers: " + totalFiles,
                "Nombre de threads",
                "Temps (seconds)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                false,
                false
        );

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(new Color(255, 255, 0xFF));
        plot.setDomainGridlinePaint(new Color(207, 215, 0xff));
        plot.setRangeGridlinePaint(new Color(0, 144, 255));

        chart.getXYPlot().getRangeAxis().setAutoRange(true);

        String reportName =  "time_report_totalFile_" + totalFiles + ".jpg";
        File file = new File(reportName);

        try {
            ChartUtilities.saveChartAsJPEG(file, chart, width, height);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private XYSeries getSerie(int index, int totalFiles){
        XYSeries serie = new XYSeries(methods[index]);

        List<Measure> measureList = getListMeasuresByTotalFiles(index, totalFiles);
        for (int i = 0; i < measureList.size(); i++) {
            serie.add(measureList.get(i).getNbThreads(), measureList.get(i).getSeconds(0));
        }

        return serie;
    }

    private XYSeries getSerie(int index, int totalFiles, double acceleration){
        XYSeries serie = new XYSeries(methods[index]);

        List<Measure> measureList = getListMeasuresByTotalFiles(index, totalFiles);
        for (int i = 0; i < measureList.size(); i++) {
            serie.add(measureList.get(i).getNbThreads(), acceleration / measureList.get(i).getSeconds(0));
        }

        return serie;
    }
}
