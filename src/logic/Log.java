package logic;

import java.util.logging.*;


public class Log {

	//Class which creates  2 log (console e file) 
	
	private final static Logger log = Logger.getLogger( Logger.GLOBAL_LOGGER_NAME );     
    
    public static void setupLogger() {
    	
        LogManager.getLogManager().reset();
        log.setLevel(Level.ALL);
        
        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(Level.FINE);
        log.addHandler(ch);

        try {
            FileHandler fh = new FileHandler("Logger.log", true);
            fh.setLevel(Level.FINE);
            log.addHandler(fh);
        } catch (java.io.IOException e) {            
            // don't stop my program but log out to console.
            log.log(Level.SEVERE, "File logger not working.", e);
        }
         /* 
         Different Levels in order.
          OFF
          SEVERE
          WARNING
          INFO
          CONFIG
          FINE
          FINER
          FINEST
          ALL
        */
    } 
    
    public static void infoLog(String msg ) {
    	
    	log.info(msg);
    }
    
   
}
