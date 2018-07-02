import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

//INSERT element and value in first category
public class CleanFiles {
	
	static int allelements =0;
	static boolean FILENOTFOUND;
	public static void main(String[] args) throws IOException {
		  takeInputs();
		  System.out.println("File update completed. Find updated files in the newfilescleanscript folder.");
	}
	
	public static void takeInputs() throws IOException {
		mainLoop("oldfilescleanscript/Fauv/"); 
		mainLoop("oldfilescleanscript/IvTrem/");
	     mainLoop("oldfilescleanscript/Montpellier/");
	}
	
	public static void mainLoop(String folder) throws IOException {
		  File dir = new File(folder);
		  File[] directoryListing = dir.listFiles();
		  if (directoryListing != null) {
		    for (File child : directoryListing) {
		    	
		    	insertElement(child);
		    }
		  } else {
		    // Handle the case where dir is not really a directory.
		    // Checking dir.isDirectory() above would not be sufficient
		    // to avoid race conditions with another process that deletes
		    // directories.
		  }
	}
	
	public static void insertElement(File file) throws IOException {
		// Open a temporary file to write to.
		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("temp.mei")));
		BufferedReader br = null;
		int BUFFER_SIZE = 0;
		FileReader reader = null;
		try {
		    reader = new FileReader(file.getPath());
		    br = new BufferedReader(reader);
		    String line;
		    while ((line = br.readLine()) != null) {
		      	if (line.contains("meiversion=")) {
		    		line = replaceType(line,"meiversion", "4.0.0");
		    		line = removeType(line, "xml:id");
		    		
		    	}
				if (line.contains("<scoreDef xml")) {
					line =  line.substring(0, 28) + line.substring(28, line.length());
				}
				if (line.contains("<staffDef xml") && !line.contains("notationtype") && file.getPath().contains("MENSURAL.mei")) {
					line =  line.substring(0, 30) + " notationtype=\"mensural.black\"" + line.substring(30, line.length());
				}
				if (line.contains("&amp;apos;")) {
					line =  line.replace("&amp;apos;", "'");
				}
				if (line.contains("&amp;amp;apos;")) {
					line =  line.replace("&amp;amp;apos;", "'");
				}
				if (line.contains("&amp;amp;amp;apos;")) {
					line =  line.replace("&amp;amp;amp;apos;", "'");
				}
				if (line.contains("barLine")) {
					line =  line.replace("barLine", "barLine form=\"dashed\"");
				}
				if (file.getPath().contains("MENSURAL.mei")) {
					if (line.contains("dur.ges")) {
						line =  removeType(line, "dur.ges");
					}
					if (line.contains("quality")) {
						line =  line.replace("quality", "dur.quality");
					}
				} else {
					if (line.contains("stem.mod")) {
						line =  removeType(line, "stem.mod");
					}
					if (line.contains("artic")) {
						line =  removeType(line, "artic");
					}
				}
				if (line.contains("<staffDef")) {
					String label = getType(line,"label");
					line = removeType(line,"label");
					line = replaceType(line,"xml:id", label);
					line = line + "\n\t\t\t\t\t\t\t\t\t<label>" + label + "</label>";
					
				}
				if (line.contains("<syl")&& line.contains("wordpos=\"m\"")&& !line.contains("con=\"d\"")) {
					line=addCon(line);
				}
				if (!line.contains("<!--") && !line.contains("<instrDef") && !line.contains("<fermata")) { 
				String findnextsyl = "";
				String nextline = "";
				BUFFER_SIZE = 1000;
		    	br.mark(BUFFER_SIZE);
		    	int counter=0;
		    	boolean cont=true;
		    	while (cont==true) {
		    		if ((findnextsyl = br.readLine()) != null && BUFFER_SIZE !=0){
		    			counter++;
		    			if (counter==1) {
		    				nextline=findnextsyl;
		    			}
						if ( findnextsyl.contains("<syl") || counter==8) {
							br.reset();
							cont=false;
						}
		    		} else {
		    			br.reset();
						cont=false;
		    		}
				}
				if (nextline != null && nextline.contains("</layer>")) {
					if (line.contains("barLine form=\"dashed\"")) {
						line =  line.replace("barLine form=\"dashed\"", "barLine form=\"dbl\"");
					}
				}
				
				if (line.contains("<syl") && line.contains("wordpos=\"i\"") && !line.contains("con=\"d\"")) {
					
					 
					if (findnextsyl != null && findnextsyl.contains("<syl") && !findnextsyl.contains("wordpos=\"i\"")) {
					line=addCon(line);
				}
				}
				writer.println(line);
				}
			}
				    	
		    		
		    writer.close();

		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		}finally{
		    reader.close();

		}

		// ... and finally ...
		String path =file.getPath();
		String newpath = "newfiles" + path.substring(8, path.length());
		
		File realName = new File(newpath);
		realName.delete();
		new File("temp.mei").renameTo(realName); // Rename temp file*/
		}
	
	
	
	
	public static String addCon(String line) {
		return line.replace("wordpos", "con=\"d\" wordpos");
	}
	
	public static String getType(String line, String type) {
		String[] splitline = line.split(type);
		String[] splitversion = splitline[1].split("\"");
		return splitversion[1];
	}
	
	public static String replaceType(String line, String type, String replacement) {
		String[] splitline = line.split(type);
		String[] splitversion = splitline[1].split("\"");
		splitversion[1]= replacement;
		String updatedversion = String.join("\"", splitversion);
		splitline[1]=updatedversion;
		return String.join(type, splitline);
	}
	
	public static String removeType(String line, String type) {
		String[] splitline = line.split(type);
		String[] splitversion = splitline[1].split("\"");
		String updatedversion;
		if (splitversion.length>3) {
			String subsplitversion[] = Arrays.copyOfRange(splitversion,  2, splitversion.length);
			
			updatedversion = String.join("\"", subsplitversion);
		} else {
			updatedversion = splitversion[splitversion.length-1];
		}
		splitline[1] = updatedversion;
		splitline[0] = splitline[0].substring(0, splitline[0].length()-1);
		return String.join("", splitline);
	}

	
	public static String insertTabs(String line, String temp) {
		String output="";
		int counter=2;
		for (int i=0; i<line.length(); i++) {
			if (line.charAt(i)=='/' && line.charAt(i-1)=='<') {
				counter=counter-1;
			} if (line.charAt(i)=='\n') {
				counter++;
				if (closedString(findNewLine(line,i))) {
					counter=counter-1;
				}
				output=output+line.charAt(i)+returnTabs(counter);
			} else {
				output=output+line.charAt(i);
			}
		}
		return output;
		
	}
	
	public static String findNewLine(String line,int j) {
		for (int i=j+1; i<line.length(); i++ ) {
			if (line.charAt(i)=='\n') {
				String temp =line.substring(j, i);
				return temp;
			}
		}
		return "";
		
	}
	public static String returnTabs(int counter) {
		String tabs="";
		for (int i=0; i<counter; i++) {
			tabs=tabs + "\t";
		}
		return tabs;
	}
	
	public static boolean closedString(String line) {
		boolean start=false;
		boolean first=false;
		boolean second=false;
		boolean third=false;
		for (int i=0; i<line.length(); i++) {
			if (i<4) {
			if (line.charAt(i)=='\n') {
				start=true;
			}
			if (line.charAt(i)=='<' && start) {
				first=true;
			}
			if (line.charAt(i)=='/' && first) {
				second=true;
			}
			}if (line.charAt(i)=='>' && second) {
				third=true;
				return start && first && second && third;
			}
		}
		return start && first && second && third;
		
	}
	
	
	
	
	public static String replaceAmp(String link) {
		return link.replace("&", "&amp;");
	}

	public static String clefFormat(String input) {
		String[] splitInput = input.split(",");
		String s = String.join("", splitInput);
		return s;
	}
	
}
		
	
