package logic;


import java.util.logging.Logger;
import java.io.IOException;
import java.util.logging.FileHandler;


public class HelloWorld {
	
	private static Logger logger;
	
	public void hello() throws SecurityException, IOException {
		
		//Hello world print
		
		boolean append = true;
        FileHandler handler = new FileHandler("default.log", append);
        
		logger = Logger.getLogger(HelloWorld.class.getName());
		logger.addHandler(handler);
		logger.info("This is the real Hello Wolrd ! ");
		
	}

}
