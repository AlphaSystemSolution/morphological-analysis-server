package com.alphasystem.morphologicalanalysis;

import com.alphasystem.morphologicalanalysis.util.DataInitializationTool;
import com.alphasystem.morphologicalanalysis.util.Script;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;

/**
 * @author sali
 */
// @SpringBootApplication
public class MorphologicalAnalysisDataInitializerApplication implements CommandLineRunner {

    @Autowired private DataInitializationTool dataInitializationTool;

    @Override
    public void run(String... strings) throws Exception {
        for (Script script : Script.values()) {
            dataInitializationTool.createAllChapters(script);
        }
        System.exit(0);
    }

    public static void main(String[] args) {
        SpringApplication.run(MorphologicalAnalysisDataInitializerApplication.class, args);
    }
}
