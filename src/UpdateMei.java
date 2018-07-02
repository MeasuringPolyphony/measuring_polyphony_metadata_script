import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Scanner;
//INSERT element and value in first category
public class UpdateMei {
	static String[][] alldata = new String[200][50];
	static int allelements =0;
	static boolean FILENOTFOUND;
	public static void main(String[] args) throws IOException { //FIX,MAKE ALL FIELDS WORK
		  parseData();
		  takeInputs();
		  System.out.println("File update completed. Find updated files in the newfiles folder.");
	}
	public static void parseData() throws FileNotFoundException {
		File file = new File("data.tsv");
		Scanner input = new Scanner(file);
		int counter=0;
		while(input.hasNext()) {
		    //or to process line by line
		    String nextLine = input.nextLine();
		     String[] temp = nextLine.split("\t");
		     for (int i=0; i<temp.length; i++) {
		    	 alldata[counter][i]=temp[i];
		     }
		     
		    counter++;
		    
		}
		allelements=counter;
		input.close();
		
	}
	
	public static void takeInputs() throws IOException {
	     //System.out.print("Folder?:"); 
	    // Scanner four = new Scanner(System.in);
	     //String folder = four.next();
		mainLoop("oldfiles/Fauv/"); 
		mainLoop("oldfiles/IvTrem/");
	     mainLoop("oldfiles/Montpellier/");
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
		boolean alreadyFound=false; //only inserts it in first one
		int  j= findFile(file);
		if (j!=0) {
		try {
		    reader = new FileReader(file.getPath());
		    br = new BufferedReader(reader);
		    String line;
		    while ((line = br.readLine()) != null) {
		    	if (line.contains("meiversion=")) {
		    		line = replaceType(line,"meiversion", "4.0.0");
		    		line = removeType(line, "xml:id");
		    		
		    	}
				    	if (line.contains("<meiHead")){ //insert the data in meihead
				    		writer.println(line);
				    		String temp="";
				    		for (int i=0; i<line.length(); i++) {
				    			if (line.charAt(i)=='\t') {
				    				temp=temp+"\t";
				    			}	
				    		}
				    		
				    			String x = createLines(j);
				    			String y = insertTabs(x,temp);
				    			writer.println(y);
				    			alreadyFound=true;
				    		
							//insert before it
						} else if (alreadyFound && line.contains("</meiHead>")) {
							alreadyFound=false;
						} else if (!alreadyFound) {
							if (line.contains("<scoreDef xml") && !line.contains("midi.bpm")) {
								line =  line.substring(0, 28) + " midi.bpm=\"" + alldata[j][31] + "\"" + line.substring(28, line.length());
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
				    	
		    		}
		    writer.close();

		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		}finally{
		    reader.close();

		}

		// ... and finally ...
		String path =file.getPath();
		String newpath = "newfiles/" + path.substring(9, path.length());
		
		File realName = new File(newpath);
		realName.delete();
		new File("temp.mei").renameTo(realName); // Rename temp file*/
		}
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
	
	
	public static int findFile(File file) throws IOException { 
		    
	    	for (int j=0; j<allelements; j++) {
	    		if ((file.getPath().contains(alldata[j][10]) && !alldata[j][10].equals(""))  || (file.getPath().contains(alldata[j][11]) && !alldata[j][11].equals("")) ) { //CREATE FULL
	    			return j;
	    	}   
		   
		    }

		return 0;
	}
	
	public static String createLines(int j) {
		String temp= "\t\t<fileDesc>\n<titleStmt>\n<title>" + alldata[j][0] + ": A Digital Edition</title>"
				+ "\n<editor>Karen Desmond</editor> \n " +
				"<funder>\n<corpName>Social Sciences and Humanities Research Council, Canada (SSHRC) </corpName>\n</funder>\n"
				+ "<funder>\n<corpName> Schulich School of Music, McGill University</corpName> \n</funder> \n "
				+ "<funder>\n<corpName>Brandeis University</corpName>\n</funder>\n "
				+ "<funder>\n<corpName>National Endowment for Humanities (NEH)</corpName> \n</funder> \n "
				+ "<respStmt> \n"
				+ "<persName role=\"project director\" auth=\"VIAF\" auth.uri=\"http://viaf.org/viaf/\" codedval=\"316001213\">Karen Desmond</persName>\n"
				+ initialsToNames(alldata[j][20],alldata[j][19],"encoder") //convert initials to names
				+ initialsToNames(alldata[j][22],alldata[j][21],"proofreader") //for loop
				+ "</respStmt>\n" + "</titleStmt>"
				+ "\n<pubStmt>\n<publisher>\n "
				+ "<persName>Karen Desmond</persName>\n<corpName>Brandeis University</corpName>\n " + "</publisher>"
				+ "\n<date>2018</date> \n<availability>\n<useRestrict>Available for purposes of academic research and teaching only.</useRestrict>\n</availability>"
				+ "\n</pubStmt> \n<seriesStmt> \n<title>Measuring Polyphony: Digital Encodings of Late Medieval Music</title> \n<editor> \n<persName>Karen Desmond</persName> \n"
				+ "</editor>\n<identifier>\n" + 
				"<ref targettype=\"home_page\" target=\"http://www.measuringpolyphony.org\"/>\n" + 
				"</identifier>\n</seriesStmt>\n<sourceDesc>\n<source>\n<titleStmt>\n<title>\n<identifier>"+alldata[j][3]+"</identifier>\n"
				+ "</title>\n</titleStmt>\n<notesStmt>\n"
				+ "<annot>Primary manuscript source for this encoding.</annot>"
				+ "\n<annot label=\"original_clefs\">Original clefs for this source: " + clefFormat(alldata[j][6]) + "</annot>"
				+ "\n<annot label=\"commentary\">" + alldata[j][25] + "</annot>"
				+ "\n</notesStmt>\n<itemList>\n";
				if (!alldata[j][15].equals("")) {
					temp = temp + "<item targettype=\"IIIF\" target=\""+alldata[j][15]+"\" codedval=\""+alldata[j][4]+"\"></item>\n";
				}
				if (!alldata[j][18].equals("")) {
					temp = temp + "<item targettype=\"other_images\" target=\"" + replaceAmp(alldata[j][18]) + "\" motetus=\""+alldata[j][28]+"\" folio=\"" + alldata[j][4]+ "\"></item>\n";
				}
				if (!alldata[j][17].equals("")) {
					temp = temp+"<item targettype=\"DIAMM_source_record\" target=\""+alldata[j][17]+"\"></item>\n";
				}
				
				if (!alldata[j][16].equals("")) {
					temp=temp+"<item targettype=\"DIAMM_composition_record\" target=\""+alldata[j][16]+"\"></item>\n";
				}
				temp=temp+ "</itemList>\n</source>\n<source>\n"
				+ "<titleStmt>\n<title>[Other concordant sources]</title>\n</titleStmt>\n<notesStmt>\n"
				+ "<annot>" + alldata[j][7] + "</annot>\n</notesStmt>\n</source>\n</sourceDesc>\n</fileDesc>\n<encodingDesc> \n"
				+ "<appInfo>\n<application xml:id=\"sibelius\" isodate=\"2016-4-29T09:24:36Z\" version=\"7510\">"
				+ "<name type=\"operating-system\">Mac OS X Mountain Lion</name>"
				+ "</application>\n<application xml:id=\"sibmei\" type=\"plugin\" version=\"2.0.0b3\">\n<name>Sibelius to MEI Exporter (2.0.0b3)</name>\n"
				+ "</application> \n<application xml:id=\"meiMENS\" isodate=\"2016-4-29\">\n<name>CMN-MEI to MensuralMEI Translator</name>\n"
				+ "</application>\n</appInfo>\n<editorialDecl>\n<p>\n" + alldata[j][23] + " " + alldata[j][24] + "</p>\n"
				+ "</editorialDecl>\n<projectDesc>\n<p>\"Measuring Polyphony\" presents digitised versions of polyphonic compositions written during the thirteenth and fourteenth centuries, offering new possibilities for mediating the scholarly and public experience of this richly evocative music within its original manuscript context. The project began at the Schulich School of Music at McGill University, and now continues at Brandeis University. It leverages the potential of the rich digital image repositories of music manuscripts and the community-based standards for encoding music notation of the Music Encoding Initiative (MEI).</p>"
				+ "\n</projectDesc>\n</encodingDesc>\n<workDesc>\n<work>\n<identifier>"
				+ "\n<identifier type=\"catalogue_number\">"+alldata[j][9]+"</identifier>\n</identifier>\n<title>" + alldata[j][0]+"</title>\n"
				+ "<composer>" + alldata[j][1] + "</composer>\n"
				+ makeParts(j)
				+ "\n<classification>\n<termList>\n<term>" + alldata[j][2] + "</term>\n</termList>\n</classification>\n</work>\n"
						+ "</workDesc>\n<revisionDesc>\n" + 
						"<change resp=\"#KD\" isodate=\"2018-05-31\">\n" + 
						"<changeDesc>\n" + 
						"<p></p>\n" + 
						"</changeDesc>\n" + 
						"</change>\n" + 
						"</revisionDesc>\n</meiHead>";
				return temp;
	}
	
	public static String checkTarget(String link,int j, int x) {
		if (alldata[j][x]!="") {
			return link;
		} else {
			return "";
		}
	}
	public static String replaceAmp(String link) {
		return link.replace("&", "&amp;");
	}
	public static String initialsToNames(String date, String input, String role) {
		String[] splitInput = input.split(", ");
		String output="";
		for (int i=0; i<splitInput.length; i++) {
			if (splitInput[i].equals("KD")) {
				output = output + "<persName role=\""+role+"\" isodate=\"" + dateFormat(date) + "\" xml:id=\"" + splitInput[i] + "\">Karen Desmond</persName> \n";
			} else if (splitInput[i].equals("EH")) {
				output = output + "<persName role=\""+role+"\" isodate=\"" + dateFormat(date) + "\" xml:id=\"" + splitInput[i] + "\">Emily Hopkins</persName> \n";
			} else if (splitInput[i].equals("SH")) {
				output = output + "<persName role=\""+role+"\" isodate=\"" + dateFormat(date) + "\" xml:id=\"" + splitInput[i] + "\">Sam Howes</persName> \n";
			} else if (splitInput[i].equals("SM")) {
				output = output + "<persName role=\""+role+"\" isodate=\"" + dateFormat(date) + "\" xml:id=\"" + splitInput[i] + "\">Sadie Menicanin</persName> \n";
			} else if (splitInput[i].equals("DS")) {
				output = output + "<persName role=\""+role+"\" isodate=\"" + dateFormat(date) + "\" xml:id=\"" + splitInput[i] + "\">Daniel Shapiro</persName> \n";
			} else if (splitInput[i].equals("SMK")) {
				output = output + "<persName role=\""+role+"\" isodate=\"" + dateFormat(date) + "\" xml:id=\"" + splitInput[i] + "\">Shawn Mikkelson</persName> \n";
			} else {
				output = output + "<persName role=\""+role+"\" isodate=\"" + dateFormat(date) + "\" xml:id=\"" + splitInput[i] + "\">" + splitInput[i] + "</persName> \n";
			}
			}
		
		return output;
	}
	
	public static String makeParts(int j) {
		String output = "<incip>\n";
		for (int i=0; i<5; i++) {
			if(alldata[j][26+i]!=null) {
			if ( !alldata[j][26+i].equals("")) {
			output=output+ "<incipText label=\""+ alldata[0][26+i] + "\" corresp=\"#"+ alldata[0][26+i] + "\">\n<lg><l>"+alldata[j][26+i]+"</l></lg>\n</incipText>\n";
			}
			}
		}
		output = output + "</incip>\n";
		return output;
	}
	public static String dateFormat(String input) {
		String[] splitInput = input.split("/");
		if (splitInput[splitInput.length-1].length() == 2) {
			splitInput[splitInput.length-1] = "20" + splitInput[splitInput.length-1];
		}
		//IMPORTANT - THIS METHOD RELIES ON HOW THE DATE IS FORMATTED IN THE DATA, CURRENTLY AS MONTH/DAY/YEAR
		if (splitInput.length==3) {
		String temp = splitInput[0];
		splitInput[0]=splitInput[2];
		splitInput[2] = splitInput[1];
		splitInput[1] = temp;
		}
		return String.join("-", splitInput);
	}
	

	public static String clefFormat(String input) {
		String[] splitInput = input.split(",");
		String s = String.join("", splitInput);
		return s;
	}

}
		
	
