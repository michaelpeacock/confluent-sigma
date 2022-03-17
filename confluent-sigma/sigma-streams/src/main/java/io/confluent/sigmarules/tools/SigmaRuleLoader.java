/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

package io.confluent.sigmarules.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.confluent.sigmarules.SigmaStreamsApp;
import io.confluent.sigmarules.models.SigmaRule;
import io.confluent.sigmarules.rules.SigmaRulesStore;
import io.confluent.sigmarules.utilities.SigmaOptions;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SigmaRuleLoader {
    private SigmaRulesStore sigmaRulesStore;
    private ObjectMapper mapper;

    public SigmaRuleLoader(SigmaOptions options) {
        sigmaRulesStore = new SigmaRulesStore(options.getProperties());
        mapper = new ObjectMapper(new YAMLFactory());
    }

    public void loadSigmaFile(String filename) {
        try {
            String rule = Files.readString(Path.of(filename));
            SigmaRule sigmaRule = mapper.readValue(rule, SigmaRule.class);
            String key = sigmaRule.getTitle();

            System.out.println("Adding sigma rule: " + key);
            System.out.println(sigmaRule.toString());
            sigmaRulesStore.addRule(key, rule);
        } catch (IOException e) {
            System.out.println("Failed to load: " + filename);

            e.printStackTrace();
        }
    }

    public void loadSigmaDirectory(String dirName) {
        List<String> fileList;
        try (Stream<Path> walk = Files.walk(Paths.get(dirName))) {
            // We want to find only regular files
            fileList = walk.filter(Files::isRegularFile)
                    .map(x -> x.toString()).collect(Collectors.toList());

            for(String file : fileList) {
                loadSigmaFile(file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setOptions(Options options) {
        options.addOption("f", "file", true, "Path to sigma rule file.");
        options.addOption("d", "dir", true, "Path to directory contain sigma rules.");
        options.addOption("c", "config", true, "Path to properties file");
    }

    public static void main(String[] args) {
        Options options = new Options();
        setOptions(options);

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args, false);

            if((cmd.hasOption("c")) && (cmd.hasOption("f") || cmd.hasOption("d"))) {
                InputStream input = new FileInputStream(cmd.getOptionValue("c"));
                Properties properties = new Properties();
                properties.load(input);

                SigmaOptions sigmaOptions = new SigmaOptions();
                sigmaOptions.setProperties(properties);

                SigmaRuleLoader sigma = new SigmaRuleLoader(sigmaOptions);
                if(cmd.hasOption("f")) {
                    sigma.loadSigmaFile(cmd.getOptionValue("f"));
                }

                if(cmd.hasOption("d")) {
                    sigma.loadSigmaDirectory(cmd.getOptionValue("d"));
                }
            } else {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("sigmal_rule_loader", options, true);

                System.exit(0);
            }
        } catch (ParseException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.exit(0);
    }
}
