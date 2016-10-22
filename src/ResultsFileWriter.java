import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class ResultsFileWriter {
	
	private BufferedWriter bw;
	
	public ResultsFileWriter(Path resultfile) throws IOException {
		this.bw = new BufferedWriter(new FileWriter(resultfile.toFile()));
	}
	
	public void write(Clone clone) throws IOException {
		bw.write(clone.resultString() + "\n");
	}
	
	public void close() throws IOException {
		bw.close();
	}
	
}
