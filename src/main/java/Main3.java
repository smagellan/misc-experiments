import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.LoggerFactory;

public class Main3 {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Main3.class);

    public static void main(String[] args) {
        try(OutputStream os = new FileOutputStream("/delme")) {
            os.write(1);
        }catch (IOException ex){
            logger.error("test exception", ex);
        }
    }
}
