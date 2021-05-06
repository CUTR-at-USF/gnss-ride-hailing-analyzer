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
import edu.usf.cutr.grha.model.ChicagoTncData;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.List;

public class ChicagoTncWriter extends CsvWriter {
    public List<ChicagoTncData> chicagoTncData;
    public List<String> headers;

    public ChicagoTncWriter(List<ChicagoTncData> chicagoTncData, List<String> headers) {
        this.chicagoTncData = chicagoTncData;
        this.headers = headers;
    }

    public void writeFile() {
        BeanWriterProcessor<ChicagoTncData> beanWriterProcessor =
                new BeanWriterProcessor<>(ChicagoTncData.class);
        ByteArrayOutputStream byteArrayOutputStream =
                writeFile(beanWriterProcessor, headers, chicagoTncData);

        try {
            byteArrayOutputStream.writeTo(new FileOutputStream("output.csv"));
            System.out.println("File Written successfully ");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
