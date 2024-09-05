package sg.edu.nus.comp.cs4218.app;

import sg.edu.nus.comp.cs4218.Application;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;

public interface MkdirInterface extends Application {
    /**
     * Create folder from the given folder names. Do nothing if folder already exists. If folder
     * name is a path format, create the folder that satisfies the path specification.
     *
     * @param folderName Array of string of folder names to be created
     * @throws Exception
     */
    void createFolder(String... folderName) throws AbstractApplicationException;
}
