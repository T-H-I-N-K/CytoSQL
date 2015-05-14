package org.bkslab.cytosql.internal.util;


	import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

	public class FileIterator implements Iterable<String>, Iterator<String>{
	  private BufferedReader file;
	  private String nextLine;

	  public FileIterator(String FileName) throws IOException {
	    this(new File(FileName));
	  }

	  public FileIterator(File f) throws IOException {
	    file = new BufferedReader(new FileReader(f));
	    nextLine = file.readLine();
	  }

	  public Iterator<String> iterator() {
	    return this;
	  }

	  public boolean hasNext() {
	    return nextLine != null;
	  }

	  public String next() {
	    String line = nextLine;
	    try {
	      nextLine = file.readLine();
	    } catch (IOException e) {
	      throw new RuntimeException("Error reading file", e);
	    }
	    return line;
	  }

	  public void remove() {
	    throw new NotImplementedException();
	  }
	  
	  public void close() throws IOException {
	    file.close();
	  }
	}


