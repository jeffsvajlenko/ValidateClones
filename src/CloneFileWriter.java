import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CloneFileWriter {
	
	private BufferedWriter bw;
	
	public CloneFileWriter(Path output) throws IOException {
		if(!Files.exists(output))
			Files.createFile(output);
		bw = new BufferedWriter(new FileWriter(output.toFile()));
	}
	
	public void writeClone(Clone clone) throws IOException {
		bw.write("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\n");
		bw.write(clone.getToolID() + "\n");
		bw.write(clone.getCloneId() + "\n");
		bw.write(clone.getFragment1() + "\n");
		bw.write(clone.getFragment2() + "\n");
		bw.write("----------------------------------------\n");
		bw.write(clone.getText1());
		bw.write("----------------------------------------\n");
		bw.write(clone.getText2());
		bw.write("----------------------------------------\n");
		bw.flush();
	}
	
	public void close() throws IOException {
		bw.flush();
		bw.close();
	}	
}