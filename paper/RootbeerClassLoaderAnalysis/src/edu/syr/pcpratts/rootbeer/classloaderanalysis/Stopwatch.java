
package edu.syr.pcpratts.rootbeer.classloaderanalysis;

public class Stopwatch {
  private long m_start;
  private long m_stop;
  private long m_totalTime;
  private long m_totalStops;

  public Stopwatch(){
    m_totalTime = 0;
    m_totalStops = 0;
  }
  
  public void start() {
    m_start = System.currentTimeMillis(); // start timing
  }

  public void stop() {
    m_stop = System.currentTimeMillis(); // stop timing
    m_totalTime += elapsedTimeMillis();
    m_totalStops++;
  }

  public long elapsedTimeMillis() {
    return m_stop - m_start;
  }
  
  public long getAverageTime(){
    if(m_totalStops > 0){
      return m_totalTime / m_totalStops; 
    } else {
      return 0;
    }
  }

  public @Override String toString() {
    return "elapsedTimeMillis: " + Long.toString(elapsedTimeMillis()); // print execution time
  }

  public void stopAndPrint(String str){
    stop();
    System.out.println(str + toString());
  }
}