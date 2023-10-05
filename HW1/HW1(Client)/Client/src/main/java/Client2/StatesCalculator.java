package Client2;

import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class StatesCalculator {
  public static void computeStats(String filePath) throws Exception {
    // Create a CSV parser
    try (Reader in = new FileReader(filePath)) {
      Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(in);

      List<Integer> latencies = new ArrayList<>();

      // Extract latency values from the CSV and add to the list
      for (CSVRecord record : records) {
        Integer latency = Integer.parseInt(record.get(2)); // assuming latency is the third column
        latencies.add(latency);
      }

      // Convert list to array for DescriptiveStatistics
      double[] latencyArray = latencies.stream().mapToDouble(d -> d).toArray();

      // Compute statistics
      DescriptiveStatistics stats = new DescriptiveStatistics(latencyArray);

      // Output results
      System.out.println("Mean: " + stats.getMean() + " ms");
      System.out.println("Median: " + stats.getPercentile(50) + " ms");
      System.out.println("99th Percentile: " + stats.getPercentile(99) + " ms");
      System.out.println("Min: " + stats.getMin() + " ms");
      System.out.println("Max: " + stats.getMax() + " ms");
    }
  }
}
