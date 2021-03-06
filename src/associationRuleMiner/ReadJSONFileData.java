/**
 * 
 */
package associationRuleMiner;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author SriramSankaranarayanan
 *  Read from a JSON file and load structures.
 */
public class ReadJSONFileData {
	List<RepoData> h; // Store the data being read in.
	String fileName;
	
	public List<RepoData> getRepoData() {
		return h;
	}
	/* 
	 * Function: readFileLines
	 *   Read the lines from the input JSON Formatted File.
	 *   TODO: Modify this as @SergioMover changes the format
	 */
	public void readFileLines(BufferedReader in, CommitDateRanges cdr){
		try{
			String line;
			String fPattern="\"(.*)\":(.*)$";
			Pattern r = Pattern.compile(fPattern);
			RepoData curData=null;
			int count = 1;
			while ( (line = in.readLine()) != null){
				line = line.trim();
				int llen = line.length();
				assert(line.charAt(0) == '{' && line.charAt(llen-1)=='}');
				line = line.substring(1,llen-1);
				curData = new RepoData(count);
				count ++;
				List<String> lFields = this.extractFieldsFromLine(line);
				
				for (String s: lFields){
				// Match it with a pattern
					Matcher m = r.matcher(s);
					if (m.find()){
						String key = m.group(1);
						String val = m.group(2);
						val=val.trim();
						int len = val.length();
						//System.out.println("<<"+val+">>");
						if (val == "[]"){
							val = "";
						} else {
							val = val.substring(1,len-1); // Strip first and last characters
						}
						// Java >= 7.0 will match strings appropriately using String.equals method 
						switch (key){
						case "repo":
							curData.setRepo(val);
							break;
						case "child_hash":
							curData.setChildHash(val);
							break;
						case "parent_hash":
							curData.setParentHash(val);
							break;
						case "name":
							curData.setName(val);
							break;
						case "indices":
							// System.out.println(val);
							curData.setIndicesString(val);
							break;
						case "contexts":
							curData.parseContexts(val);
							break;
						case "values":
							
							curData.setValuesString(val);
							break;
						case "commit_date":
							curData.setCommitDate(val,cdr);
							break;
						default:
							System.out.println("Fatal: unknown key "+key);
							assert(false);
						}
					}
				}
				// Do not bother adding repo data without indices.
				if (curData.numIndices() > 0){
					curData.splitRepoDataByContextAndAdd(h);
				}
			}			
			in.close();
		} catch (IOException e){
			System.out.println("Fatal: IOException Caught while reading "+fileName);
			e.printStackTrace();
			assert(false);
		} 
		
	}
	
	private List<String> extractFieldsFromLine(String line) {
		int n = line.length();
		int nListNests = 0;
		List<String> retVal = new ArrayList<String>();
		int curStart = 0;
		for(int i = 0; i < n; ++i){
			char c = line.charAt(i);
			switch (c){
			case ',':
				if (nListNests == 0){
					String t = line.substring(curStart,i);
					retVal.add(t);
					//System.out.println(t);
					curStart = i+1;
				}
				break;
			case '[':
				nListNests++;
				break;
			case ']':
				nListNests--;
				break;
			}
			
		}
		
		String lastStr = line.substring(curStart,n);
		retVal.add(lastStr);
		return retVal;
	}
	/*
	 * Function: Constructor<ReadJSONFileData>
	 *   Constructor function
	 */
	public ReadJSONFileData(String fName, CommitDateRanges cdr){
		try {
			this.fileName = fName;
			this.h=new ArrayList<RepoData>();
			// Open the file into a buffered reader
			BufferedReader in = new BufferedReader( new FileReader(fName));
			readFileLines(in,cdr); // Read all the lines
		} catch (FileNotFoundException e) {
			// Trouble?
			System.out.println("Fatal: could not find file: "+ fName);
			e.printStackTrace();
		}
	}

}


