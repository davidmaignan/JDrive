package inf5171.stats;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by david on 2016-12-14.
 */
public class Report {
    private String[] keys = new String[]{"sequential", "prod/con", "pool"};

    private Map<String, List<Measure>> measures;
    private Measure sequential;
    private int length = 300;
    private int nbColumns = 4;

    public Report(Map<String, List<Measure>> measures){
        this.measures = measures;
    }

    public List<Integer> getListOfTotalFiles(){
        return measures.get(keys[0]).stream().map( s -> s.getTotalFiles()).distinct().collect(Collectors.toList());
    }

    public List<Integer> getListOfNbThreads(){
        return measures.get(keys[1]).stream().map( s -> s.getNbThreads()).distinct().collect(Collectors.toList());
    }

    public Double getAverageSequentialByStage(int totalFiles, int stage){
        return measures.get(keys[0]).stream()
                .filter(s -> s.getTotalFiles() == totalFiles)
                .map(s -> s.getElapsedTime(stage)).mapToDouble(Double::new).average().getAsDouble();
    }

    public Measure getSequential(int totalFiles){
        return measures.get(keys[0]).stream().filter(
                s -> s.getTotalFiles() == totalFiles ).findFirst().orElse(null);
    }

    public String printHeader(){
        StringBuilder builder = new StringBuilder("");


        return builder.toString();
    }

    public String printReport(int key){
        StringBuilder builder = new StringBuilder("");




        return builder.toString();
    }


//    private long getAcceleration(String key){
//
//    }
}
