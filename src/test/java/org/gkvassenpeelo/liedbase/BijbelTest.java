package org.gkvassenpeelo.liedbase;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class BijbelTest extends Bijbel {
    
    Bijbel bijbel = new Bijbel();

    @Test
    public void extractBibleTextTest() throws IOException {
        System.out.println(bijbel.extractBibleTextPart(FileUtils.readFileToString(new File("E:\\temp\\bible.txt"), "UTF-8")));
    }

}
