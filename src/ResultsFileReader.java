import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

public class ResultsFileReader {

	//toolid,cloneid,true/false,fragment1,fragment2
	
	private BufferedReader br;
	
	public ResultsFileReader(Path resultfile) throws FileNotFoundException {
		this.br = new BufferedReader(new FileReader(resultfile.toFile()));
	}
	
	public Clone nextClone() throws IOException {
		String line = br.readLine();
		if(line == null) {
			return null;
		} else {
			String parts[] = line.split(",");
			int toolid = Integer.parseInt(parts[0]);
			int cloneid = Integer.parseInt(parts[1]);	
			Boolean validation;
			if(parts[2].equals("undecided")) {
				validation = null;
			} else if (parts[2].equals("true")) {
				validation = true;
			} else {
				validation = false;
			}
			String fragment1 = parts[3];
			String fragment2 = parts[4];
			Clone clone = new Clone(toolid, cloneid, fragment1, fragment2, "", "");
			clone.setValidation(validation);
			return clone;
		}
		
	}
	
	public void close() throws IOException {
		br.close();
	}
	
}
