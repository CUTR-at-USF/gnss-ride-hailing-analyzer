/*
 * Copyright (C) 2021 University of South Florida
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.usf.cutr.grha.io;

import com.univocity.parsers.common.processor.BeanWriterProcessor;
import com.univocity.parsers.csv.CsvWriterSettings;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

public abstract class CsvWriter {
    public <T> ByteArrayOutputStream writeFile(BeanWriterProcessor<T> beanWriterProcessor, List<String> headers, List<T> lt) {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        Writer outputWriter = new OutputStreamWriter(result);
        CsvWriterSettings csvWriterSettings = new CsvWriterSettings();

        csvWriterSettings.setRowWriterProcessor(beanWriterProcessor);
        csvWriterSettings.setHeaders(headers.toArray(new String[headers.size()]));
        com.univocity.parsers.csv.CsvWriter csvWriter =
                new com.univocity.parsers.csv.CsvWriter(outputWriter, csvWriterSettings);
        csvWriter.writeHeaders();
        for (T t : lt) {
            csvWriter.processRecord(t);
        }
        csvWriter.close();
        return result;
    }

}
