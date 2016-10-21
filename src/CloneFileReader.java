import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

public class CloneFileReader {

	private BufferedReader br;
	
	public CloneFileReader(Path clonefile) throws FileNotFoundException {
		this.br = new BufferedReader(new FileReader(clonefile.toFile()));
	}
	
	public Clone nextClone() throws IOException {
		String line;
		
		while(true) {
			line = br.readLine();
			if(line == null) {
				br.close();
				return null;
			} else if (line.equals("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$")) {
				break;
			}
		}
		
		int toolid = Integer.parseInt(br.readLine());
		int cloneid = Integer.parseInt(br.readLine());
		String file1 = br.readLine();
		String file2 = br.readLine();
		
		br.readLine();
		
		String fragment1 = "";
		line = "";
		while(true) {
			line = br.readLine();
			if(line.equals("----------------------------------------")) {
				break;
			}
			fragment1 += line + "\n";
		}
		
		String fragment2 = "";
		line = "";
		while(true) {
			line = br.readLine();
			if(line.equals("----------------------------------------")) {
				break;
			}
			fragment2 += line + "\n";
		}
		
		return new Clone(toolid, cloneid, file1, file2, fragment1, fragment2);
	}
	
}