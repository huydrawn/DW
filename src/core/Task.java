package core;

public interface Task {
	void execute() throws Exception;

	public void run() throws Exception;

	public String jobDescription();
}
