
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;


/**
* A class to display a thermometer widget and display the temperature
* based on a reading from an Arduino IC.
*/
public class ArduinoThermometer
{
   private static SerialMonitor fMonitor;
   private static ArduinoThermometer fThermometer;
   private static ThermometerPanel fThermometerPanel;
   private static MercuryPanel fMercuryPanel;
   
   
   // Draw the window and the thermometer.
   private void display()
   {
      fThermometerPanel = new ThermometerPanel();
      JFrame frame = new JFrame("Arduino Thermometer");
      frame.setResizable(false);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.add(fThermometerPanel);
      frame.pack();
      frame.setVisible(true);
   }

   public static void main(String[] args) 
   {
      javax.swing.SwingUtilities.invokeLater(new Runnable() 
      {
         public void run() 
         {
            fThermometer = new ArduinoThermometer();
            fThermometer.display();
            fMonitor = new SerialMonitor();
            fMonitor.initialize();
            new MonitorReaderThread(fThermometerPanel, fMonitor).start();
         }  
      });
   }
}


/**
* Thread to read temperature values from the serial monitor.
*/
class MonitorReaderThread extends Thread
{
   private ThermometerPanel fPanel;
   private SerialMonitor fMonitor;
   
   public MonitorReaderThread(ThermometerPanel panel, SerialMonitor monitor)
   {
      fPanel = panel;
      fMonitor = monitor;
   }
   
   public void run() 
   {
      while (true)
      {
         try
         {
            fPanel.update(fMonitor.getLatestReading());
            Thread.sleep(1000);
         }
         catch (InterruptedException e)
         {
         }
      }
   }
}

/**
* Panel to display the thermometer markings.
*/
class ThermometerPanel extends JPanel 
{
   private MercuryPanel fMercuryPanel;
   
   public ThermometerPanel()
   {
      this.setLayout(null);
      fMercuryPanel = new MercuryPanel();
      this.add(fMercuryPanel);
      fMercuryPanel.setBounds(25, 20, 190, 500);
   }
   
   
   public Dimension getPreferredSize() 
   {
      return new Dimension(300, 550);
   }
   
   public void update(double temperature)
   {
      fMercuryPanel.update(temperature);
   }
   
   // Draw all of the degree markers and display the correct temperature
   public void paintComponent(Graphics g) 
   {
      super.paintComponent(g);
      
      int degreeYStartPos = 525;
      int tickLabelYStartPos = 520;
      
      for (int x = 12; x > 0; --x)
      {
         g.drawString(new Integer(x * 10).toString() + "\u00B0", 250, degreeYStartPos - (x * 40));
         g.fillRect(230, tickLabelYStartPos - (x * 40), 15, 4);
      }
   }
}


/**
* Panel to display the thermometer "mercury"
*/
class MercuryPanel extends JPanel 
{
   private double fDisplayTemperature;
   private boolean fInitialized = false;
   
   public void update(double temperature)
   {
      fDisplayTemperature = temperature;
      fInitialized = true;
      this.repaint(0, 0, this.getWidth(), this.getHeight());
   }
   
   public void paintComponent(Graphics g) 
   {
      super.paintComponent(g);
      
      g.setColor(Color.WHITE);
      g.fillRect(90, 0, 100, this.getHeight());
      
      if (fInitialized)
      {
         g.setColor(Color.BLACK);
         g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
         g.drawString(new Double(fDisplayTemperature).toString(), 10, 200);
         g.setColor(Color.RED);
         
         // Each degree covers 4 pixels
         g.fillRect(90, 
          this.getHeight() - ((int)fDisplayTemperature * 4), 
          100, 
          (int)fDisplayTemperature * 4);
      }
   }
}



