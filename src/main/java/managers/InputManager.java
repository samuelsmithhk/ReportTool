package managers;

import files.InputFileManager;
import files.InputPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class InputManager {

    private static InputManager im;
    private final Logger logger = LoggerFactory.getLogger(InputManager.class);
    private final InputFileManager ifm;

    private InputManager(InputFileManager ifm) {
        this.ifm = ifm;
    }

    public static void initInputManager(InputFileManager ifm) {
        if (im == null) im = new InputManager(ifm);
    }

    public static InputManager getInputManager() throws Exception {
        if (im == null)
            throw new Exception("InputManager needs to be initialized with an instance of InputFileManager");
        return im;
    }

    public void loadNewInputsIfAny() throws Exception {
        logger.info("Checking for new input files");
        if (ifm.newInputs()) {
            CacheManager cm = CacheManager.getCacheManager();
            List<InputPair> newInputs = ifm.parseNewInputs();

            for (InputPair input : newInputs) {
                logger.info("Processing update: {}", input);
                cm.processDealUpdate(input.sourceSystem, input.directory, input.timestamp, input.dealMap);
            }

            cm.purgeOldData();
        }
    }

}
