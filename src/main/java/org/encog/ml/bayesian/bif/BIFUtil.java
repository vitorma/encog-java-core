package org.encog.ml.bayesian.bif;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.encog.Encog;
import org.encog.ml.bayesian.BayesianError;
import org.encog.ml.bayesian.BayesianEvent;
import org.encog.ml.bayesian.BayesianNetwork;
import org.encog.ml.bayesian.table.TableLine;
import org.encog.parse.tags.write.WriteXML;
import org.encog.util.csv.CSVFormat;
import org.xml.sax.SAXException;

public class BIFUtil {

	public static BayesianNetwork readBIF(String f) {
		return readBIF(new File(f));
	}

	public static BayesianNetwork readBIF(File f) {
		FileInputStream fis = null;

		try {
			fis = new FileInputStream(f);
			return readBIF(fis);
		} catch (IOException ex) {
			throw new BayesianError(ex);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException ex) {
					// who cares at this point.
				}
			}
		}
	}

	public static BayesianNetwork readBIF(InputStream is) {
		try {
			BIFHandler h = new BIFHandler();
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			sp.parse(is, h);
			return h.getNetwork();
		} catch (IOException ex) {
			throw new BayesianError(ex);
		} catch (ParserConfigurationException ex) {
			throw new BayesianError(ex);
		} catch (SAXException ex) {
			throw new BayesianError(ex);
		}
	}
	
	public static void writeBIF(String fn, BayesianNetwork network) {
		writeBIF(new File(fn),network);
	}
	
	public static void writeBIF(File file, BayesianNetwork network) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			writeBIF(fos,network);
		} catch(IOException ex) {
			throw new BayesianError(ex);
		} finally {
			if( fos!=null) {
				try {
					fos.close();
				} catch (IOException e) {
					// don't care at this point
				}
			}
		}
	}
	
	public static void writeBIF(OutputStream os, BayesianNetwork network) {
		WriteXML xml = new WriteXML(os);
		xml.beginDocument();
		xml.addAttribute("version", "3.0");
		xml.beginTag("BIF");
		xml.beginTag("NETWORK");
		xml.addProperty("name", "Bayes Network, Generated by Encog");
		// write variables
		for(BayesianEvent event: network.getEvents()) {
			xml.addAttribute("type", "nature");
			xml.beginTag("VARIABLE");
			xml.addProperty("NAME", event.getLabel());
			for(String str: event.getChoices()) {
				xml.addProperty("OUTCOME", str);
			}
			xml.endTag();
		}
		
		// write relations
		for(BayesianEvent event: network.getEvents()) {
			xml.beginTag("DEFINITION");
			xml.addProperty("FOR", event.getLabel());
			for(BayesianEvent parentEvent: event.getParents()) {
				xml.addProperty("GIVEN", parentEvent.getLabel());
			}
			xml.addAttribute("TABLE", generateTable(event));
			xml.endTag();
		}
		
		xml.endTag();
		xml.endTag();
		xml.endDocument();
		
	}
	
	public static String generateTable(BayesianEvent event) {
		StringBuilder s = new StringBuilder();
		int tableIndex = 0;
		int[] args = new int[event.getParents().size()];
		do {
			for(int result = 0; result<event.getChoices().length;result++) {
				TableLine line = event.getTable().findLine(result,args);
				if( s.length()>0 ) {
					s.append(" ");
				}
				s.append(CSVFormat.EG_FORMAT.format(line.getProbability(), Encog.DEFAULT_PRECISION));	
			}
		} while(BIFUtil.rollArgs(event,args));
		return s.toString();
	}
	
	public static boolean rollArgs(BayesianEvent event, int[] args) {
		int currentIndex = event.getParents().size()-1;
		boolean done = false;
		boolean eof = false;
		
		if( event.getParents().size() == 0 ) {
			done = true;
			eof = true;
		}

		while (!done) {
			int v = (int) args[currentIndex];
			v++;
			if (v >= event.getParents().get(currentIndex).getChoices().length) {
				args[currentIndex] = 0;
			} else {
				args[currentIndex] = v;
				done = true;
				break;
			}

			currentIndex--;

			if (currentIndex < 0) {
				done = true;
				eof = true;
			}
		}

		return !eof;
	}


}