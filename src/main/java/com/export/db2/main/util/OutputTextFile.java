/*
 * Copyright 2020 stanislawbartkowski@gmail.com
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package com.export.db2.main.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

class OutputTextFile implements Closeable {

	private PrintWriter pw;

	void open(File fileOut, boolean append) throws IOException {
		if (!fileOut.exists())
			fileOut.createNewFile();
		pw = new PrintWriter(new FileOutputStream(fileOut, append));
	}

	void writeline(String line) {
		pw.println(line);
	}

	void writeline() {
		pw.println();
	}

	public void close() {
		pw.close();
	}

}
