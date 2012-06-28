
import java.io.InputStream;
import java.io.OutputStream;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.util.Enumeration;

public class SerialMonitor implements SerialPortEventListener
{
   private static final String kPortName = "/dev/tty.usbserial-A600eosa";
   private static final int kTimeOut = 2000;
   private static final int kDataRate = 9600;

   private SerialPort fSerialPort;
   private InputStream fInput;
   private StringBuffer fInputBuffer = new StringBuffer();
   private double fLatestReading;
   
   public void initialize()
   {
      CommPortIdentifier portId = null;
      Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
      
      while (portEnum.hasMoreElements())
      {
         CommPortIdentifier currPortId = (CommPortIdentifier)portEnum.nextElement();
         if (currPortId.getName().equals(kPortName))
         {
            portId = currPortId;
         }
      }
      
      if (null == portId)
      {
         System.out.println("Could not find COM port.");
         return;
      }
      
      try
      {
         // open serial port and use class name for the appName
         fSerialPort = (SerialPort)portId.open(this.getClass().getName(), kTimeOut);
         fSerialPort.setSerialPortParams(kDataRate,
          SerialPort.DATABITS_8,
          SerialPort.STOPBITS_1,
          SerialPort.PARITY_NONE);
          
          fInput = fSerialPort.getInputStream();
          fSerialPort.addEventListener(this);
          fSerialPort.notifyOnDataAvailable(true);
      }
      catch (Exception e)
      {
         System.err.println(e.toString());
      }
   }
   
   
   public double getLatestReading()
   {
      return fLatestReading;
   }
    
   /**
   * This should be called when you stop using the port.
   * This will prevent port locking on platforms like Linux.
   */
   public synchronized void close()
   {
      if (null != fSerialPort)
      {
         fSerialPort.removeEventListener();
         fSerialPort.close();
      }
   }
   
      
   /**
   * Handle an event on the serial port. Read the data and print it.
   */
   public synchronized void serialEvent(SerialPortEvent event)
   {
      if (SerialPortEvent.DATA_AVAILABLE == event.getEventType())
      {
         try
         {
            int available = fInput.available();
            byte chunk[] = new byte[available];
            fInput.read(chunk, 0, available);
            
            // We use an input buffer as the read/write operations
            // aren't synched across the device and we can get partial
            // results.
            fInputBuffer.append(new String(chunk));
            
            // Assuming we never get more than one full token in the buffer at once
            int delimiterIndex = fInputBuffer.indexOf("|");
            if (-1 != delimiterIndex)
            {
               fLatestReading = Double.valueOf(fInputBuffer.substring(0, delimiterIndex)).doubleValue();            
               fInputBuffer.replace(0, delimiterIndex + 1, "");
            }
         }
         catch (Exception e)
         {
            System.err.println(e.toString());
         }
      }
   }
}

 
