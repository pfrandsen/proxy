package tool;

import org.junit.Test;

public class FileSystemProxyTest {

    @Test
    public void testArguments() {
        String args[] = {"-filesystemProxy", "-root", "rootDir", "-virtualHosts", "a,b,c", "-subdirectories", "d,  ef"};
        FileSystemProxy.main(args);
    }

}
